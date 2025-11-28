package com.hexvane.strangematter.block;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nonnull;

/**
 * Block entity used to defer anomaly entity spawning until the main thread.
 * This prevents threading issues during worldgen by placing a marker block
 * that gets processed on the next server tick.
 */
public class AnomalySpawnMarkerBlockEntity extends BlockEntity {
    
    public ResourceLocation entityTypeLocation;
    public Vec3 spawnPosition;
    private boolean processed = false;
    
    public AnomalySpawnMarkerBlockEntity(BlockPos pos, BlockState state) {
        super(com.hexvane.strangematter.StrangeMatterMod.ANOMALY_SPAWN_MARKER_BLOCK_ENTITY.get(), pos, state);
    }
    
    /**
     * Tick handler for the spawn marker - spawns the entity on the next server tick
     */
    public static void tick(Level level, BlockPos pos, BlockState state, AnomalySpawnMarkerBlockEntity blockEntity) {
        if (level.isClientSide || blockEntity.processed) {
            return;
        }
        
        blockEntity.processed = true;
        
        // Get the entity type from registry
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(blockEntity.entityTypeLocation);
        if (entityType == null) {
            StrangeMatterMod.LOGGER.error("Failed to find entity type: {}", blockEntity.entityTypeLocation);
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        
        // Create and spawn the entity
        Entity entity = entityType.create(level);
        if (entity != null) {
            entity.moveTo(blockEntity.spawnPosition.x(), blockEntity.spawnPosition.y(), blockEntity.spawnPosition.z(), 0.0f, 0.0f);
            
            // For WarpGateAnomalyEntity, set active state
            if (entity instanceof com.hexvane.strangematter.entity.WarpGateAnomalyEntity warpGateEntity) {
                warpGateEntity.setActive(true);
            }
            
            level.addFreshEntity(entity);
            StrangeMatterMod.LOGGER.debug("Spawned entity {} at {}", blockEntity.entityTypeLocation, blockEntity.spawnPosition);
        } else {
            StrangeMatterMod.LOGGER.error("Failed to create entity: {}", blockEntity.entityTypeLocation);
        }
        
        // Remove the marker block
        level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
    }
    
    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (entityTypeLocation != null) {
            tag.putString("entity_type", entityTypeLocation.toString());
        }
        if (spawnPosition != null) {
            tag.putDouble("spawn_x", spawnPosition.x());
            tag.putDouble("spawn_y", spawnPosition.y());
            tag.putDouble("spawn_z", spawnPosition.z());
        }
        tag.putBoolean("processed", processed);
    }
    
    @Override
    public void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("entity_type")) {
            entityTypeLocation = ResourceLocation.parse(tag.getString("entity_type"));
        }
        if (tag.contains("spawn_x") && tag.contains("spawn_y") && tag.contains("spawn_z")) {
            spawnPosition = new Vec3(
                tag.getDouble("spawn_x"),
                tag.getDouble("spawn_y"),
                tag.getDouble("spawn_z")
            );
        }
        processed = tag.getBoolean("processed");
    }
    
    public ResourceLocation getEntityTypeLocation() {
        return entityTypeLocation;
    }
    
    public Vec3 getSpawnPosition() {
        return spawnPosition;
    }
}

