package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.TemporalBloomEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import javax.annotation.Nonnull;

public class TemporalBloomConfiguredFeature extends Feature<NoneFeatureConfiguration> {
    
    public TemporalBloomConfiguredFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }
    
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
        
        // Place temporal-themed terrain (wheat fields)
        placeTemporalTerrain(level, origin, random, 3, 0.7f);
        
        // Spawn the temporal bloom entity above the surface
        TemporalBloomEntity anomaly = new TemporalBloomEntity(StrangeMatterMod.TEMPORAL_BLOOM.get(), level.getLevel());
        anomaly.moveTo(anomalyPos.getX() + 0.5, anomalyPos.getY(), anomalyPos.getZ() + 0.5, 0.0f, 0.0f);
        
        return level.getLevel().addFreshEntity(anomaly);
    }
    
    /**
     * Places temporal-themed terrain (wheat fields) around the origin position.
     * Uses WorldGenUtils for efficient ground detection.
     */
    private void placeTemporalTerrain(WorldGenLevel level, BlockPos origin, RandomSource random, int radius, float chance) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                
                // Only place grass within the circle and with some randomness for patchiness
                if (distance <= radius && random.nextFloat() < chance) {
                    BlockPos grassPos = WorldGenUtils.findAnomalousGrassPosition(level, 
                        origin.getX() + x, origin.getZ() + z);
                    
                    if (grassPos != null) {
                        level.setBlock(grassPos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 3);
                        
                        // Occasionally place tilled soil with wheat seeds
                        if (random.nextFloat() < 0.7f) { // 70% chance for wheat fields
                            // Place farmland (tilled soil)
                            level.setBlock(grassPos, net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState(), 3);
                            
                            // Place wheat seeds on the farmland
                            BlockPos abovePos = grassPos.above();
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
