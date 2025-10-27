package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import javax.annotation.Nonnull;

public class WarpGateAnomalyConfiguredFeature extends Feature<NoneFeatureConfiguration> {
    
    public WarpGateAnomalyConfiguredFeature() {
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
        
        // Spawn the warp gate a few blocks above the surface
        int anomalyY = surfaceInfo.surfacePos.getY() + 2;
        BlockPos anomalyPos = new BlockPos(origin.getX(), anomalyY, origin.getZ());
        
        // Place anomalous grass in a patchy circle following terrain contour
        placeAnomalousGrass(level, origin, random, 5, 0.8f); // Slightly larger radius for warp gates
        
        // Place resonite ore in a small area underneath the anomalous grass
        placeResoniteOre(level, origin, random);
        
        // Spawn the warp gate anomaly entity above the surface
        WarpGateAnomalyEntity anomaly = new WarpGateAnomalyEntity(StrangeMatterMod.WARP_GATE_ANOMALY_ENTITY.get(), level.getLevel());
        anomaly.moveTo(anomalyPos.getX() + 0.5, anomalyPos.getY(), anomalyPos.getZ() + 0.5, 0.0f, 0.0f);
        anomaly.setActive(true); // Make sure it's active
        
        return level.getLevel().addFreshEntity(anomaly);
    }
    
    /**
     * Places anomalous grass in a patchy circle around the origin position.
     * Uses WorldGenUtils for efficient ground detection.
     */
    private void placeAnomalousGrass(WorldGenLevel level, BlockPos origin, RandomSource random, int radius, float chance) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                
                // Only place grass within the circle and with some randomness for patchiness
                if (distance <= radius && random.nextFloat() < chance) {
                    BlockPos grassPos = WorldGenUtils.findAnomalousGrassPosition(level, 
                        origin.getX() + x, origin.getZ() + z);
                    
                    if (grassPos != null) {
                        level.setBlock(grassPos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 3);
                    }
                }
            }
        }
    }
    
    /**
     * Places resonite ore in a small area underneath the origin position.
     */
    private void placeResoniteOre(WorldGenLevel level, BlockPos origin, RandomSource random) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -4; y <= -1; y++) {
                    BlockPos orePos = origin.offset(x, y, z);
                    
                    // Small chance to place ore
                    if (random.nextFloat() < 0.2f) {
                        if (level.getBlockState(orePos).is(net.minecraft.world.level.block.Blocks.STONE)) {
                            level.setBlock(orePos, StrangeMatterMod.RESONITE_ORE_BLOCK.get().defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }
}
