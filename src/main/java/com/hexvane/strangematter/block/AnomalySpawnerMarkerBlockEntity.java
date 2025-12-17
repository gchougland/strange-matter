package com.hexvane.strangematter.block;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.GravityAnomalyEntity;
import com.hexvane.strangematter.entity.TemporalBloomEntity;
import com.hexvane.strangematter.entity.ThoughtwellEntity;
import com.hexvane.strangematter.entity.EchoingShadowEntity;
import com.hexvane.strangematter.entity.EnergeticRiftEntity;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Block entity that spawns an anomaly entity on the next server tick.
 * This is used to defer entity spawning from world generation threads to the main server thread.
 */
public class AnomalySpawnerMarkerBlockEntity extends BlockEntity {
    
    private static final String ENTITY_TYPE_KEY = "EntityType";
    private static final String SPAWN_X_KEY = "SpawnX";
    private static final String SPAWN_Y_KEY = "SpawnY";
    private static final String SPAWN_Z_KEY = "SpawnZ";
    private static final String SPAWN_YAW_KEY = "SpawnYaw";
    private static final String SPAWN_PITCH_KEY = "SpawnPitch";
    private static final String WARP_GATE_ACTIVE_KEY = "WarpGateActive";
    
    private String entityTypeId;
    private double spawnX;
    private double spawnY;
    private double spawnZ;
    private float spawnYaw;
    private float spawnPitch;
    private boolean warpGateActive;
    private boolean hasSpawned = false;
    private int spawnAttempts = 0;
    private static final int MAX_SPAWN_ATTEMPTS = 3;
    
    public AnomalySpawnerMarkerBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.ANOMALY_SPAWNER_MARKER_BLOCK_ENTITY.get(), pos, state);
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, AnomalySpawnerMarkerBlockEntity blockEntity) {
        // Always remove if already spawned or no entity type
        if (blockEntity.hasSpawned || blockEntity.entityTypeId == null) {
            removeMarkerBlock(level, pos);
            return;
        }
        
        // Increment attempt counter
        blockEntity.spawnAttempts++;
        blockEntity.setChanged(); // Save attempt count
        
        // Try to spawn the entity
        boolean spawned = blockEntity.spawnEntity(level);
        
        // Remove the block if:
        // 1. Successfully spawned
        // 2. Reached max attempts (even if spawning failed)
        if (spawned) {
            blockEntity.hasSpawned = true;
            blockEntity.setChanged(); // Save state before removal
            // Remove immediately and schedule another removal attempt next tick as backup
            removeMarkerBlock(level, pos);
            // Schedule removal again next tick to ensure it's gone
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.getServer().execute(() -> {
                    BlockState currentState = serverLevel.getBlockState(pos);
                    if (currentState.getBlock() instanceof AnomalySpawnerMarkerBlock) {
                        serverLevel.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                    }
                });
            }
        } else if (blockEntity.spawnAttempts >= MAX_SPAWN_ATTEMPTS) {
            // Final attempt failed, remove anyway to prevent stuck blocks
            StrangeMatterMod.LOGGER.warn("Marker block at {} failed to spawn entity after {} attempts, removing", 
                pos, blockEntity.spawnAttempts);
            removeMarkerBlock(level, pos);
        }
        // If spawning failed but we haven't reached max attempts yet, 
        // the block will remain and try again on the next tick
    }
    
    /**
     * Safely removes the marker block, ensuring it happens on the server thread.
     * Uses multiple methods to ensure removal happens.
     */
    private static void removeMarkerBlock(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return; // Don't remove on client
        }
        
        // Explicitly remove the block entity first. During worldgen/proto-chunk transitions it's possible to end up with
        // orphaned BE NBT if only the block state is changed. Clearing the BE proactively prevents
        // "Tried to load a DUMMY block entity ... but found minecraft:air" warnings on future loads.
        level.removeBlockEntity(pos);
        
        // Use setBlock to air as primary method (more reliable than removeBlock)
        BlockState airState = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        level.setBlock(pos, airState, 3); // Flag 3 = notify neighbors and update clients
        
        // Also try removeBlock as backup (some edge cases where setBlock might not work)
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Schedule on next tick as well to ensure removal
            serverLevel.getServer().execute(() -> {
                if (level.getBlockState(pos).getBlock() instanceof AnomalySpawnerMarkerBlock) {
                    level.removeBlockEntity(pos);
                    level.setBlock(pos, airState, 3);
                }
            });
        }
    }
    
    private boolean spawnEntity(Level level) {
        if (level.isClientSide || entityTypeId == null) {
            return false;
        }
        
        try {
            ResourceLocation entityLocation = ResourceLocation.parse(entityTypeId);
            Entity entity = createEntity(entityLocation, level);
            
            if (entity != null) {
                entity.moveTo(spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
                
                // Special handling for WarpGateAnomalyEntity
                if (entity instanceof WarpGateAnomalyEntity warpGate && warpGateActive) {
                    warpGate.setActive(true);
                }
                
                // Only return true if entity was actually added to the world
                return level.addFreshEntity(entity);
            } else {
                StrangeMatterMod.LOGGER.warn("Failed to create entity of type {} for marker at {}", entityTypeId, worldPosition);
            }
        } catch (Exception e) {
            StrangeMatterMod.LOGGER.error("Failed to spawn entity from marker at {}: {}", worldPosition, e.getMessage(), e);
        }
        
        return false;
    }
    
    private Entity createEntity(ResourceLocation entityLocation, Level level) {
        // Map entity types to their constructors
        String path = entityLocation.getPath();
        
        return switch (path) {
            case "gravity_anomaly" -> new GravityAnomalyEntity(StrangeMatterMod.GRAVITY_ANOMALY.get(), level);
            case "temporal_bloom" -> new TemporalBloomEntity(StrangeMatterMod.TEMPORAL_BLOOM.get(), level);
            case "thoughtwell" -> new ThoughtwellEntity(StrangeMatterMod.THOUGHTWELL.get(), level);
            case "echoing_shadow" -> new EchoingShadowEntity(StrangeMatterMod.ECHOING_SHADOW.get(), level);
            case "energetic_rift" -> new EnergeticRiftEntity(StrangeMatterMod.ENERGETIC_RIFT.get(), level);
            case "warp_gate_anomaly" -> new WarpGateAnomalyEntity(StrangeMatterMod.WARP_GATE_ANOMALY_ENTITY.get(), level);
            default -> {
                // Fallback: try to get from registry
                var entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityLocation);
                if (entityType != null) {
                    yield entityType.create(level);
                }
                StrangeMatterMod.LOGGER.warn("Unknown entity type: {}", entityLocation);
                yield null;
            }
        };
    }
    
    /**
     * Set the entity type and spawn position for this marker
     */
    public void setEntityData(String entityTypeId, double x, double y, double z, float yaw, float pitch) {
        this.entityTypeId = entityTypeId;
        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;
        this.spawnYaw = yaw;
        this.spawnPitch = pitch;
        setChanged();
    }
    
    /**
     * Set additional data for WarpGateAnomalyEntity
     */
    public void setWarpGateActive(boolean active) {
        this.warpGateActive = active;
        setChanged();
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (entityTypeId != null) {
            tag.putString(ENTITY_TYPE_KEY, entityTypeId);
        }
        tag.putDouble(SPAWN_X_KEY, spawnX);
        tag.putDouble(SPAWN_Y_KEY, spawnY);
        tag.putDouble(SPAWN_Z_KEY, spawnZ);
        tag.putFloat(SPAWN_YAW_KEY, spawnYaw);
        tag.putFloat(SPAWN_PITCH_KEY, spawnPitch);
        tag.putBoolean(WARP_GATE_ACTIVE_KEY, warpGateActive);
        tag.putBoolean("HasSpawned", hasSpawned);
        tag.putInt("SpawnAttempts", spawnAttempts);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        entityTypeId = tag.contains(ENTITY_TYPE_KEY) ? tag.getString(ENTITY_TYPE_KEY) : null;
        spawnX = tag.getDouble(SPAWN_X_KEY);
        spawnY = tag.getDouble(SPAWN_Y_KEY);
        spawnZ = tag.getDouble(SPAWN_Z_KEY);
        spawnYaw = tag.getFloat(SPAWN_YAW_KEY);
        spawnPitch = tag.getFloat(SPAWN_PITCH_KEY);
        warpGateActive = tag.getBoolean(WARP_GATE_ACTIVE_KEY);
        hasSpawned = tag.getBoolean("HasSpawned");
        spawnAttempts = tag.contains("SpawnAttempts") ? tag.getInt("SpawnAttempts") : 0;
    }
}

