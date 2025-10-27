package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.hexvane.strangematter.energy.EnergyAttachment;
import net.neoforged.neoforge.energy.IEnergyStorage;
import com.hexvane.strangematter.energy.ResonanceEnergyStorage;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.Config;
import net.neoforged.neoforge.capabilities.Capabilities;

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
    
    // Prevent infinite recursion when notifying adjacent conduits
    private boolean isUpdatingConnections = false;
    
    
    public ResonantConduitBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.RESONANT_CONDUIT_BLOCK_ENTITY.get(), pos, state);
        // No energy storage - pure conduit
    }
    
    @Override
    public void onLoad() {
        super.onLoad();
        // Initialize connections when the block entity is loaded
        updateConnections();
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, ResonantConduitBlockEntity blockEntity) {
        if (level.isClientSide) return;
        
        blockEntity.networkUpdateCounter++;
        
        // Update network cache periodically or when invalid
        if (!blockEntity.networkCacheValid || blockEntity.networkUpdateCounter >= Config.resonantConduitNetworkUpdateInterval) {
            blockEntity.updateNetworkCache();
            blockEntity.networkUpdateCounter = 0;
        }
        
        // Update connections periodically to ensure they stay current
        if (blockEntity.networkUpdateCounter % 20 == 0) { // Every second
            blockEntity.updateConnections();
        }
        
        // Perform direct energy routing every tick
        blockEntity.performDirectEnergyRouting();
    }
    
    /**
     * Called when a neighboring block changes - invalidate cache
     */
    public void onNeighborChanged() {
        networkCacheValid = false;
        notifyNeighborChanged();
    }
    
    /**
     * Update connection state for adjacent blocks
     */
    private void updateConnections() {
        if (level == null || isUpdatingConnections) return;
        
        isUpdatingConnections = true;
        
        Arrays.fill(connectedSides, false);
        
        // Check each direction for connections
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);
            
            if (adjacentEntity != null) {
                if (adjacentEntity instanceof ResonantConduitBlockEntity) {
                    // Always connect to other conduits
                    connectedSides[direction.ordinal()] = true;
                } else if (adjacentEntity instanceof BaseMachineBlockEntity machine) {
                    // Check if this side of the machine can accept or output energy
                    Direction oppositeDirection = direction.getOpposite();
                    boolean canConnect = machine.canAcceptEnergyFrom(oppositeDirection) || 
                                       machine.canOutputEnergyTo(oppositeDirection);
                    connectedSides[direction.ordinal()] = canConnect;
                } else if (getEnergyStorageFromEntity(adjacentEntity) != null) {
                    // For other energy-capable blocks, allow connection
                    connectedSides[direction.ordinal()] = true;
                }
            }
        }
        
        setChanged();
        
        // Notify adjacent conduits to update their connections
        // This is necessary because neighbor change events only fire for the placed block
        notifyAdjacentConduits();
        
        isUpdatingConnections = false;
    }
    
    /**
     * Notify adjacent conduits to update their connections
     * Only notifies conduits, not machines, to prevent unnecessary updates
     */
    private void notifyAdjacentConduits() {
        if (level == null) return;
        
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);
            
            if (adjacentEntity instanceof ResonantConduitBlockEntity adjacentConduit) {
                // Only update if the adjacent conduit isn't already updating
                // This prevents infinite loops while still allowing necessary updates
                adjacentConduit.updateConnections();
            }
        }
    }
    
    /**
     * Notify this conduit that a neighbor has changed
     * This should be called when any block is placed/removed next to this conduit
     */
    public void notifyNeighborChanged() {
        if (level == null) return;
        
        // Update this conduit's connections
        updateConnections();
        
        // Also notify adjacent conduits so they update their connections to this one
        notifyAdjacentConduits();
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
                IEnergyStorage storage = getEnergyStorageFromEntity(adjacentEntity);
                if (storage != null) {
                    // Check if it should be a source (only generators)
                    if (storage.canExtract() && storage.getEnergyStored() > 0 && 
                        shouldBeEnergySource(adjacentEntity)) {
                        cachedSources.put(adjacentPos, distance + 1);
                    }
                    // Check if it should be a sink (only consumers)
                    if (storage.canReceive() && storage.getEnergyStored() < storage.getMaxEnergyStored() && 
                        shouldBeEnergySink(adjacentEntity)) {
                        cachedSinks.put(adjacentPos, distance + 1);
                    }
                }
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
            
            IEnergyStorage sourceStorage = getEnergyStorageFromEntity(sourceEntity);
            if (sourceStorage != null) {
                if (!sourceStorage.canExtract() || sourceStorage.getEnergyStored() <= 0) continue;
                
                // Try to route energy to sinks
                for (Map.Entry<BlockPos, Integer> sinkEntry : cachedSinks.entrySet()) {
                    BlockPos sinkPos = sinkEntry.getKey();
                    int sinkDistance = sinkEntry.getValue();
                    
                    BlockEntity sinkEntity = level.getBlockEntity(sinkPos);
                    if (sinkEntity == null) continue;
                    
                    IEnergyStorage sinkStorage = getEnergyStorageFromEntity(sinkEntity);
                    if (sinkStorage != null) {
                        if (!sinkStorage.canReceive() || sinkStorage.getEnergyStored() >= sinkStorage.getMaxEnergyStored()) continue;
                        
                        // Check if this transfer is role-compatible
                        if (!isRoleCompatibleTransfer(sourceEntity, sinkEntity)) {
                            continue;
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
                    }
                }
            }
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
    
    // Conduits don't expose energy capabilities - they just route energy
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        
        // Save connection states
        byte[] connectionBytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            connectionBytes[i] = (byte) (connectedSides[i] ? 1 : 0);
        }
        tag.putByteArray("connections", connectionBytes);
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        
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
     * Check if a machine should be treated as an energy source
     */
    private boolean shouldBeEnergySource(BlockEntity entity) {
        if (entity instanceof BaseMachineBlockEntity machine) {
            BaseMachineBlockEntity.MachineEnergyRole role = machine.getEnergyRole();
            return role == BaseMachineBlockEntity.MachineEnergyRole.GENERATOR || 
                   role == BaseMachineBlockEntity.MachineEnergyRole.BOTH;
        }
        // For non-machine entities, allow them to be sources if they can extract
        return true;
    }
    
    /**
     * Check if a machine should be treated as an energy sink
     */
    private boolean shouldBeEnergySink(BlockEntity entity) {
        if (entity instanceof BaseMachineBlockEntity machine) {
            BaseMachineBlockEntity.MachineEnergyRole role = machine.getEnergyRole();
            return role == BaseMachineBlockEntity.MachineEnergyRole.CONSUMER || 
                   role == BaseMachineBlockEntity.MachineEnergyRole.BOTH;
        }
        // For non-machine entities, allow them to be sinks if they can receive
        return true;
    }
    
    /**
     * Get energy storage from a block entity using the proper energy system
     */
    private IEnergyStorage getEnergyStorageFromEntity(BlockEntity entity) {
        if (entity == null) return null;
        
        // For Strange Matter machines, get the internal energy storage directly
        if (entity instanceof BaseMachineBlockEntity machineEntity) {
            return machineEntity.getEnergyStorage();
        }
        
        // For other blocks, try the standard NeoForge capability
        if (level != null) {
            IEnergyStorage standardStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, entity.getBlockPos(), null);
            if (standardStorage != null) {
                return standardStorage;
            }
        }
        
        // Fall back to our custom EnergyAttachment system for non-machine blocks
        if (EnergyAttachment.hasEnergyStorage(entity)) {
            return EnergyAttachment.getEnergyStorage(entity);
        }
        
        return null;
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
