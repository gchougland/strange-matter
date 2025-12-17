package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.RegistryObject;
import javax.annotation.Nonnull;

public class TemporalBloomConfiguredFeature extends BaseAnomalyConfiguredFeature {
    
    @Override
    public boolean place(@Nonnull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        
        // Use WorldGenUtils to efficiently find surface and ground
        WorldGenUtils.SurfaceInfo surfaceInfo = WorldGenUtils.findSurfaceAndGround(level, origin.getX(), origin.getZ());
        if (surfaceInfo == null) {
            return false; // No valid ground found
        }
        
        // Spawn the anomaly a few blocks above the surface
        int anomalyY = surfaceInfo.surfacePos.getY() + 2 + random.nextInt(3); // 2-4 blocks above surface
        BlockPos anomalyPos = new BlockPos(origin.getX(), anomalyY, origin.getZ());
        
        // Place a marker block that will spawn the entity on the next server tick
        // This defers entity spawning from the world generation thread to the main server thread
        var markerBlock = (com.hexvane.strangematter.block.AnomalySpawnerMarkerBlock) StrangeMatterMod.ANOMALY_SPAWNER_MARKER_BLOCK.get();
        BlockState markerState = markerBlock.defaultBlockState()
            .setValue(com.hexvane.strangematter.block.AnomalySpawnerMarkerBlock.TYPE, com.hexvane.strangematter.block.AnomalySpawnerMarkerBlock.SpawnType.TEMPORAL)
            .setValue(com.hexvane.strangematter.block.AnomalySpawnerMarkerBlock.ATTEMPTS, 0);
        if (!level.setBlock(anomalyPos, markerState, 3)) {
            return false;
        }
        level.scheduleTick(anomalyPos, markerBlock, 1);
        
        // Place terrain modification (grass and ores) using base class
        placeAnomalousGrass(level, origin, random);
        placeOres(level, origin, random);
        
        // Place temporal-themed terrain (wheat fields) - special terrain for temporal blooms
        placeTemporalTerrain(level, origin, random);
        
        return true;
    }
    
    /**
     * Places temporal-themed terrain (wheat fields) around the origin position.
     * This is special terrain modification specific to temporal blooms.
     * Places farmland directly on the surface with wheat on top.
     */
    private void placeTemporalTerrain(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int radius = getTerrainModificationRadius();
        float chance = getGrassPlacementChance();
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                
                // Only place within the circle and with some randomness for patchiness
                if (distance <= radius && random.nextFloat() < chance) {
                    // Find suitable surface position for farmland
                    BlockPos surfacePos = WorldGenUtils.findAnomalousGrassPosition(level, 
                        origin.getX() + x, origin.getZ() + z);
                    
                    if (surfacePos != null) {
                        BlockState currentState = level.getBlockState(surfacePos);
                        
                        // Check if we can place farmland here (ground blocks)
                        if (WorldGenUtils.isActualGround(currentState)) {
                            // Occasionally place tilled soil with wheat seeds
                            if (random.nextFloat() < 0.7f) { // 70% chance for wheat fields
                                // Place farmland (tilled soil)
                                level.setBlock(surfacePos, net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState(), 3);
                                
                                // Place wheat seeds on the farmland
                                BlockPos abovePos = surfacePos.above();
                                if (level.getBlockState(abovePos).isAir()) {
                                    level.setBlock(abovePos, net.minecraft.world.level.block.Blocks.WHEAT.defaultBlockState()
                                        .setValue(net.minecraft.world.level.block.CropBlock.AGE, random.nextInt(3)), 3);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    protected RegistryObject<Block> getShardOreBlock() {
        return StrangeMatterMod.CHRONO_SHARD_ORE_BLOCK;
    }
}
