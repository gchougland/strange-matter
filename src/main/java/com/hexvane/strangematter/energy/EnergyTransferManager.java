package com.hexvane.strangematter.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import com.hexvane.strangematter.block.BaseMachineBlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages energy transfer between machines and conduits.
 * Provides robust energy routing and transfer logic.
 */
public class EnergyTransferManager {
    
    private final MachineEnergyStorage sourceStorage;
    private final EnergySideManager sideManager;
    private final BlockPos pos;
    private final Level level;
    
    public EnergyTransferManager(MachineEnergyStorage sourceStorage, EnergySideManager sideManager, BlockPos pos, Level level) {
        this.sourceStorage = sourceStorage;
        this.sideManager = sideManager;
        this.pos = pos;
        this.level = level;
    }
    
    /**
     * Attempt to receive energy from adjacent blocks
     * Returns the amount of energy actually received
     */
    public int tryReceiveEnergy(int maxReceive) {
        if (maxReceive <= 0 || !sourceStorage.canReceive()) {
            return 0;
        }
        
        int totalReceived = 0;
        int remainingToReceive = Math.min(maxReceive, sourceStorage.getMaxReceive());
        
        // Debug output (can be removed in production)
        // System.out.println("EnergyTransferManager: Trying to receive " + remainingToReceive + " energy from " + sideManager.getInputSides().length + " input sides");
        
        // Try to receive from each input side
        for (Direction side : sideManager.getInputSides()) {
            if (remainingToReceive <= 0) break;
            
            IEnergyStorage adjacentStorage = sideManager.getAdjacentEnergyStorage(level, pos, side);
            if (adjacentStorage != null) {
                // System.out.println("EnergyTransferManager: Found energy storage on " + side + " - canExtract: " + adjacentStorage.canExtract() + ", energy: " + adjacentStorage.getEnergyStored());
                
                // Check if the adjacent machine can actually output energy to this side
                if (adjacentStorage.canExtract() && canAdjacentMachineOutputToSide(level, pos, side)) {
                int energyToReceive = Math.min(remainingToReceive, adjacentStorage.extractEnergy(remainingToReceive, false));
                if (energyToReceive > 0) {
                    int actuallyReceived = sourceStorage.receiveEnergy(energyToReceive, false);
                    if (actuallyReceived > 0) {
                        // Actually extract from the adjacent storage
                        adjacentStorage.extractEnergy(actuallyReceived, true);
                        totalReceived += actuallyReceived;
                        remainingToReceive -= actuallyReceived;
                        // System.out.println("EnergyTransferManager: Received " + actuallyReceived + " energy from " + side + " (total: " + totalReceived + ")");
                    }
                }
                } else {
                    // System.out.println("EnergyTransferManager: Energy storage found on " + side + " but cannot extract");
                }
            } else {
                // System.out.println("EnergyTransferManager: No energy storage found on " + side);
            }
        }
        
        return totalReceived;
    }
    
    /**
     * Attempt to send energy to adjacent blocks
     * Returns the amount of energy actually sent
     */
    public int trySendEnergy(int maxSend) {
        if (maxSend <= 0 || !sourceStorage.canExtract()) {
            return 0;
        }
        
        int totalSent = 0;
        int remainingToSend = Math.min(maxSend, sourceStorage.getMaxExtract());
        
        // Try to send to each output side
        for (Direction side : sideManager.getOutputSides()) {
            if (remainingToSend <= 0) break;
            
            IEnergyStorage adjacentStorage = sideManager.getAdjacentEnergyStorage(level, pos, side);
            if (adjacentStorage != null && adjacentStorage.canReceive()) {
                int energyToSend = Math.min(remainingToSend, adjacentStorage.receiveEnergy(remainingToSend, false));
                if (energyToSend > 0) {
                    int actuallyExtracted = sourceStorage.extractEnergy(energyToSend, false);
                    if (actuallyExtracted > 0) {
                        // Actually insert into the adjacent storage
                        adjacentStorage.receiveEnergy(actuallyExtracted, true);
                        totalSent += actuallyExtracted;
                        remainingToSend -= actuallyExtracted;
                    }
                }
            }
        }
        
        return totalSent;
    }
    
    /**
     * Perform balanced energy transfer (both send and receive)
     * This is useful for machines that can both generate and consume energy
     */
    public EnergyTransferResult performBalancedTransfer(int maxTransfer) {
        int received = tryReceiveEnergy(maxTransfer);
        int sent = trySendEnergy(maxTransfer);
        
        return new EnergyTransferResult(received, sent);
    }
    
    /**
     * Find all adjacent energy storages that can provide energy
     */
    public List<AdjacentEnergySource> findEnergySources() {
        List<AdjacentEnergySource> sources = new ArrayList<>();
        
        for (Direction side : sideManager.getInputSides()) {
            IEnergyStorage storage = sideManager.getAdjacentEnergyStorage(level, pos, side);
            if (storage != null && storage.canExtract() && storage.getEnergyStored() > 0) {
                sources.add(new AdjacentEnergySource(side, storage));
            }
        }
        
        return sources;
    }
    
    /**
     * Find all adjacent energy storages that can accept energy
     */
    public List<AdjacentEnergySink> findEnergySinks() {
        List<AdjacentEnergySink> sinks = new ArrayList<>();
        
        for (Direction side : sideManager.getOutputSides()) {
            IEnergyStorage storage = sideManager.getAdjacentEnergyStorage(level, pos, side);
            if (storage != null && storage.canReceive() && storage.getEnergyStored() < storage.getMaxEnergyStored()) {
                sinks.add(new AdjacentEnergySink(side, storage));
            }
        }
        
        return sinks;
    }
    
    /**
     * Check if this machine is connected to any energy sources
     */
    public boolean hasEnergySources() {
        return !findEnergySources().isEmpty();
    }
    
    /**
     * Check if this machine is connected to any energy sinks
     */
    public boolean hasEnergySinks() {
        return !findEnergySinks().isEmpty();
    }
    
    /**
     * Check if this machine is connected to any energy-capable blocks
     */
    public boolean hasEnergyConnections() {
        return hasEnergySources() || hasEnergySinks();
    }
    
    /**
     * Get the total energy available from all connected sources
     */
    public int getTotalAvailableEnergy() {
        int total = 0;
        for (AdjacentEnergySource source : findEnergySources()) {
            total += source.storage.getEnergyStored();
        }
        return total;
    }
    
    /**
     * Get the total energy capacity of all connected sinks
     */
    public int getTotalSinkCapacity() {
        int total = 0;
        for (AdjacentEnergySink sink : findEnergySinks()) {
            total += sink.storage.getMaxEnergyStored() - sink.storage.getEnergyStored();
        }
        return total;
    }
    
    /**
     * Result of energy transfer operations
     */
    public static class EnergyTransferResult {
        public final int received;
        public final int sent;
        
        public EnergyTransferResult(int received, int sent) {
            this.received = received;
            this.sent = sent;
        }
        
        public int getNetTransfer() {
            return received - sent;
        }
        
        public boolean hasTransfer() {
            return received > 0 || sent > 0;
        }
    }
    
    /**
     * Represents an adjacent energy source
     */
    public static class AdjacentEnergySource {
        public final Direction side;
        public final IEnergyStorage storage;
        
        public AdjacentEnergySource(Direction side, IEnergyStorage storage) {
            this.side = side;
            this.storage = storage;
        }
    }
    
    /**
     * Check if the adjacent machine can output energy to the specified side
     */
    private boolean canAdjacentMachineOutputToSide(Level level, BlockPos pos, Direction side) {
        BlockPos adjacentPos = pos.relative(side);
        BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);
        
        if (adjacentEntity instanceof BaseMachineBlockEntity machineEntity) {
            // Check if the adjacent machine can output energy to the opposite side
            Direction oppositeSide = side.getOpposite();
            return machineEntity.canOutputEnergyTo(oppositeSide);
        }
        
        // For non-machine entities, assume they can output to any side
        return true;
    }
    
    /**
     * Represents an adjacent energy sink
     */
    public static class AdjacentEnergySink {
        public final Direction side;
        public final IEnergyStorage storage;
        
        public AdjacentEnergySink(Direction side, IEnergyStorage storage) {
            this.side = side;
            this.storage = storage;
        }
    }
}
