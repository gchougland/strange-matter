package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;

public class WarpGateAnomalyConfiguredFeature extends Feature<NoneFeatureConfiguration> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("StrangeMatter:WarpGateAnomalyFeature");
    private static int callCount = 0;
    
    public WarpGateAnomalyConfiguredFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }
    
    public static int getCallCount() {
        return callCount;
    }
    
    @Override
    public boolean place(@Nonnull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        callCount++;
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        
        // Use optimized utility to find surface and ground
        WorldGenUtils.SurfaceInfo surfaceInfo = WorldGenUtils.findSurfaceAndGround(level, origin.getX(), origin.getZ());
        if (surfaceInfo == null) {
            return false; // No valid ground found
        }
        
        // Spawn the warp gate a few blocks above the surface
        int anomalyY = surfaceInfo.surfacePos.getY() + 2;
        BlockPos anomalyPos = new BlockPos(origin.getX(), anomalyY, origin.getZ());
        
        // Check if the area is clear of trees before placing the anomaly
        if (!WorldGenUtils.isAreaClearOfTrees(level, anomalyPos, 2)) {
            return false; // Skip placement if area is not clear of trees
        }
        
        // Place anomalous grass in a patchy circle following terrain contour
        // Reduced radius from 5 to 3 for better performance while maintaining visual impact
        int radius = 3;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                
                // Only place grass within the circle and with some randomness for patchiness
                if (distance <= radius && random.nextFloat() < 0.8f) { // 80% chance for patchiness
                    // Use optimized ground detection for each position
                    WorldGenUtils.SurfaceInfo offsetSurfaceInfo = WorldGenUtils.findSurfaceAndGround(level, 
                        origin.getX() + x, origin.getZ() + z);
                    
                    if (offsetSurfaceInfo != null) {
                        BlockPos grassPos = offsetSurfaceInfo.groundPos;
                        // Use more efficient block state check
                        if (WorldGenUtils.isSolidGround(level.getBlockState(grassPos))) {
                            level.setBlock(grassPos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
        
        // Place resonite ore in a small area underneath the anomalous grass
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
        
        // Spawn the warp gate anomaly entity above the surface
        WarpGateAnomalyEntity anomaly = new WarpGateAnomalyEntity(StrangeMatterMod.WARP_GATE_ANOMALY_ENTITY.get(), level.getLevel());
        anomaly.moveTo(anomalyPos.getX() + 0.5, anomalyPos.getY(), anomalyPos.getZ() + 0.5, 0.0f, 0.0f);
        anomaly.setActive(true); // Make sure it's active
        
        return level.getLevel().addFreshEntity(anomaly);
    }
}
