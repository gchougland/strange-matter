package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.level.block.Block;
import javax.annotation.Nonnull;

public class ThoughtwellConfiguredFeature extends BaseAnomalyConfiguredFeature {
    
    @Override
    public boolean place(@Nonnull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        
        // Use optimized utility to find surface and ground
        WorldGenUtils.SurfaceInfo surfaceInfo = WorldGenUtils.findSurfaceAndGround(level, origin.getX(), origin.getZ());
        if (surfaceInfo == null) {
            return false; // No valid ground found
        }
        
        // Spawn the anomaly a few blocks above the surface
        int anomalyY = surfaceInfo.surfacePos.getY() + 2 + random.nextInt(3); // 2-4 blocks above surface
        BlockPos anomalyPos = new BlockPos(origin.getX(), anomalyY, origin.getZ());
        
        // Check if the area is clear of trees before placing the anomaly
        if (!WorldGenUtils.isAreaClearOfTrees(level, anomalyPos, 2)) {
            return false; // Skip placement if area is not clear of trees
        }
        
        // Place terrain modification (grass and ores) using base class
        placeAnomalousGrass(level, origin, random);
        placeOres(level, origin, random);
        
        // Place cognitive-themed terrain (bookshelves, lecterns) - special terrain for thoughtwell
        placeCognitiveTerrain(level, origin, random);
        
        // Place a marker block that will spawn the entity on the next server tick
        // This defers entity spawning from the world generation thread to the main server thread
        level.setBlock(anomalyPos, StrangeMatterMod.ANOMALY_SPAWN_MARKER_BLOCK.get().defaultBlockState(), 3);
        var blockEntity = level.getBlockEntity(anomalyPos);
        if (blockEntity instanceof com.hexvane.strangematter.block.AnomalySpawnMarkerBlockEntity marker) {
            marker.entityTypeLocation = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "thoughtwell");
            marker.spawnPosition = new net.minecraft.world.phys.Vec3(anomalyPos.getX() + 0.5, anomalyPos.getY(), anomalyPos.getZ() + 0.5);
            marker.setChanged();
        }
        
        return true;
    }
    
    /**
     * Places cognitive-themed terrain (bookshelves, lecterns) around the origin position.
     * This is special terrain modification specific to thoughtwells.
     * Places cognitive blocks directly on the surface.
     */
    private void placeCognitiveTerrain(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int radius = getTerrainModificationRadius();
        float chance = getGrassPlacementChance();
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                
                // Only place within the circle and with some randomness for patchiness
                if (distance <= radius && random.nextFloat() < chance) {
                    // Find suitable surface position for cognitive blocks
                    BlockPos surfacePos = WorldGenUtils.findAnomalousGrassPosition(level, 
                        origin.getX() + x, origin.getZ() + z);
                    
                    if (surfacePos != null) {
                        BlockState currentState = level.getBlockState(surfacePos);
                        
                        // Check if we have solid ground to place blocks on
                        if (WorldGenUtils.isActualGround(currentState)) {
                            // Occasionally place some cognitive-themed blocks above the ground
                            if (random.nextFloat() < 0.4f) { // 40% chance for cognitive features
                                BlockPos abovePos = surfacePos.above();
                                if (level.getBlockState(abovePos).isAir()) {
                                    // Place some books or other cognitive-themed blocks
                                    if (random.nextFloat() < 0.5f) {
                                        level.setBlock(abovePos, net.minecraft.world.level.block.Blocks.BOOKSHELF.defaultBlockState(), 3);
                                    } else if (random.nextFloat() < 0.3f) {
                                        // Calculate direction from lectern to center of anomaly
                                        int deltaX = origin.getX() - abovePos.getX();
                                        int deltaZ = origin.getZ() - abovePos.getZ();
                                        
                                        // Determine the facing direction (lectern faces the opposite direction it's placed)
                                        net.minecraft.core.Direction facing;
                                        if (Math.abs(deltaX) > Math.abs(deltaZ)) {
                                            facing = deltaX > 0 ? net.minecraft.core.Direction.WEST : net.minecraft.core.Direction.EAST;
                                        } else {
                                            facing = deltaZ > 0 ? net.minecraft.core.Direction.NORTH : net.minecraft.core.Direction.SOUTH;
                                        }
                                        
                                        level.setBlock(abovePos, net.minecraft.world.level.block.Blocks.LECTERN.defaultBlockState()
                                            .setValue(net.minecraft.world.level.block.LecternBlock.FACING, facing), 3);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    protected DeferredHolder<Block, ? extends Block> getShardOreBlock() {
        return StrangeMatterMod.INSIGHT_SHARD_ORE_BLOCK;
    }
}
