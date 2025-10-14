package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import com.hexvane.strangematter.energy.ResonanceEnergyStorage;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.Config;

import java.util.*;

/**
 * Block entity for Resonant Conduits that transfer resonant energy between machines.
 * Pure conduit - no energy storage, just routes energy directly from sources to sinks.
 */
public class ResonantConduitBlockEntity extends BlockEntity {
    
    // Connection state for each direction
    private final boolean[] connectedSides = new boolean[6];
    
    // Cached network information - updated on block changes
    private final Map<BlockPos, Integer> cachedSources = new HashMap<>(); // Source position -> distance
    private final Map<BlockPos, Integer> cachedSinks = new HashMap<>();   // Sink position -> distance
    private boolean networkCacheValid = false;
    
    // Performance optimization - only update network cache when needed
    private int networkUpdateCounter = 0;
    
    
    public ResonantConduitBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.RESONANT_CONDUIT_BLOCK_ENTITY.get(), pos, state);
        // No energy storage - pure conduit
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, ResonantConduitBlockEntity blockEntity) {
        if (level.isClientSide) return;
        
        blockEntity.networkUpdateCounter++;
        
        // Update network cache periodically or when invalid
        if (!blockEntity.networkCacheValid || blockEntity.networkUpdateCounter >= Config.resonantConduitNetworkUpdateInterval) {
            blockEntity.updateNetworkCache();
            blockEntity.networkUpdateCounter = 0;
        }
        
        // Perform direct energy routing every tick
        blockEntity.performDirectEnergyRouting();
    }
    
    /**
     * Called when a neighboring block changes - invalidate cache
     */
    public void onNeighborChanged() {
        networkCacheValid = false;
        updateConnections();
    }
    
    /**
     * Update connection state for adjacent blocks
     */
    private void updateConnections() {
        if (level == null) return;
        
        Arrays.fill(connectedSides, false);
        
        // Check each direction for connections
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);
            
            if (adjacentEntity != null) {
                // Check if it's another conduit or an energy-capable block
                if (adjacentEntity instanceof ResonantConduitBlockEntity || 
                    adjacentEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).isPresent()) {
                    connectedSides[direction.ordinal()] = true;
                }
            }
        }
        
        setChanged();
    }
    
    /**
     * Update the network cache with sources and sinks
     */
    private void updateNetworkCache() {
        if (level == null) return;
        
        cachedSources.clear();
        cachedSinks.clear();
        
        // Find all sources and sinks in the network
        findNetworkSourcesAndSinks(worldPosition, 0, new HashSet<>());
        
        networkCacheValid = true;
    }
    
    /**
     * Recursively find all sources and sinks in the conduit network
     */
    private void findNetworkSourcesAndSinks(BlockPos pos, int distance, Set<BlockPos> visited) {
        if (distance > Config.resonantConduitMaxNetworkSize || visited.contains(pos)) return;
        
        visited.add(pos);
        
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            if (visited.contains(adjacentPos)) continue;
            
            BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);
            if (adjacentEntity == null) continue;
            
            if (adjacentEntity instanceof ResonantConduitBlockEntity) {
                // Continue searching through conduit network
                findNetworkSourcesAndSinks(adjacentPos, distance + 1, visited);
            } else {
                // Check if it's an energy source or sink
                adjacentEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(storage -> {
                    if (storage.canExtract() && storage.getEnergyStored() > 0) {
                        // It's a source
                        cachedSources.put(adjacentPos, distance + 1);
                    }
                    if (storage.canReceive() && storage.getEnergyStored() < storage.getMaxEnergyStored()) {
                        // It's a sink
                        cachedSinks.put(adjacentPos, distance + 1);
                    }
                });
            }
        }
    }
    
    /**
     * Perform direct energy routing from sources to sinks
     */
    private void performDirectEnergyRouting() {
        if (level == null || !networkCacheValid) return;
        
        // Route energy from all sources to all sinks
        for (Map.Entry<BlockPos, Integer> sourceEntry : cachedSources.entrySet()) {
            BlockPos sourcePos = sourceEntry.getKey();
            int sourceDistance = sourceEntry.getValue();
            
            BlockEntity sourceEntity = level.getBlockEntity(sourcePos);
            if (sourceEntity == null) continue;
            
            sourceEntity.getCapability(ForgeCapabilities.ENERGY).ifPresent(sourceStorage -> {
                if (!sourceStorage.canExtract() || sourceStorage.getEnergyStored() <= 0) return;
                
                // Try to route energy to sinks
                for (Map.Entry<BlockPos, Integer> sinkEntry : cachedSinks.entrySet()) {
                    BlockPos sinkPos = sinkEntry.getKey();
                    int sinkDistance = sinkEntry.getValue();
                    
                    BlockEntity sinkEntity = level.getBlockEntity(sinkPos);
                    if (sinkEntity == null) continue;
                    
                    sinkEntity.getCapability(ForgeCapabilities.ENERGY).ifPresent(sinkStorage -> {
                        if (!sinkStorage.canReceive() || sinkStorage.getEnergyStored() >= sinkStorage.getMaxEnergyStored()) return;
                        
                        // Check if this transfer is role-compatible
                        if (!isRoleCompatibleTransfer(sourceEntity, sinkEntity)) {
                            return;
                        }
                        
                        // Calculate transfer amount based on distance with configurable penalty
                        int totalDistance = sourceDistance + sinkDistance;
                        double distanceMultiplier = Math.max(0.1, 1.0 - (totalDistance * Config.resonantConduitDistancePenalty));
                        int transferRate = Math.max(1, (int)(Config.resonantConduitTransferRate * distanceMultiplier));
                        
                        int energyToTransfer = Math.min(
                            transferRate,
                            Math.min(
                                sourceStorage.getEnergyStored(),
                                sinkStorage.getMaxEnergyStored() - sinkStorage.getEnergyStored()
                            )
                        );
                        
                        if (energyToTransfer > 0) {
                            // Direct transfer from source to sink
                            int energyReceived = sinkStorage.receiveEnergy(energyToTransfer, false);
                            if (energyReceived > 0) {
                                sourceStorage.extractEnergy(energyReceived, false);
                            }
                        }
                    });
                }
            });
        }
    }
    
    
    
    /**
     * Get the connection state for a specific direction
     */
    public boolean isConnected(Direction direction) {
        return connectedSides[direction.ordinal()];
    }
    
    /**
     * Get all connected directions
     */
    public List<Direction> getConnectedDirections() {
        List<Direction> connected = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (connectedSides[direction.ordinal()]) {
                connected.add(direction);
            }
        }
        return connected;
    }
    
    /**
     * Get the number of connections (used for determining model type)
     */
    public int getConnectionCount() {
        int count = 0;
        for (boolean connected : connectedSides) {
            if (connected) count++;
        }
        return count;
    }
    
    /**
     * Check if this conduit has a specific connection pattern
     */
    public boolean hasStraightConnection() {
        // Check for opposite connections (straight line)
        for (Direction direction : Direction.values()) {
            Direction opposite = direction.getOpposite();
            if (connectedSides[direction.ordinal()] && connectedSides[opposite.ordinal()]) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasCornerConnection() {
        int connectionCount = getConnectionCount();
        return connectionCount == 2 && !hasStraightConnection();
    }
    
    public boolean hasIntersectionConnection() {
        return getConnectionCount() >= 3;
    }
    
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        // Conduits don't expose energy capabilities - they just route energy
        return super.getCapability(cap, side);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        
        // Save connection states
        byte[] connectionBytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            connectionBytes[i] = (byte) (connectedSides[i] ? 1 : 0);
        }
        tag.putByteArray("connections", connectionBytes);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        
        // Load connection states
        if (tag.contains("connections")) {
            byte[] connectionBytes = tag.getByteArray("connections");
            for (int i = 0; i < 6 && i < connectionBytes.length; i++) {
                connectedSides[i] = connectionBytes[i] != 0;
            }
        }
        
        // Invalidate network cache on load - will be rebuilt on next update
        networkCacheValid = false;
    }
    
    /**
     * Force update connections and network cache (called when blocks change nearby)
     */
    public void forceUpdateConnections() {
        networkCacheValid = false;
        updateConnections();
    }
    
    /**
     * Check if energy transfer between two block entities is role-compatible.
     * This prevents generators from transferring energy to other generators.
     */
    private boolean isRoleCompatibleTransfer(BlockEntity sourceEntity, BlockEntity sinkEntity) {
        // If both entities are BaseMachineBlockEntity instances, check their roles
        if (sourceEntity instanceof BaseMachineBlockEntity sourceMachine && 
            sinkEntity instanceof BaseMachineBlockEntity sinkMachine) {
            
            BaseMachineBlockEntity.MachineEnergyRole sourceRole = sourceMachine.getEnergyRole();
            BaseMachineBlockEntity.MachineEnergyRole sinkRole = sinkMachine.getEnergyRole();
            
            // Only allow transfers from generators to consumers
            return sourceRole == BaseMachineBlockEntity.MachineEnergyRole.GENERATOR && 
                   sinkRole == BaseMachineBlockEntity.MachineEnergyRole.CONSUMER;
        }
        
        // For non-BaseMachineBlockEntity instances (like conduits), allow transfers
        return true;
    }
}
