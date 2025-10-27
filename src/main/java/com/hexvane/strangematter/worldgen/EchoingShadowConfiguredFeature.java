package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.EchoingShadowEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;

public class EchoingShadowConfiguredFeature extends Feature<NoneFeatureConfiguration> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("StrangeMatter:EchoingShadowFeature");
    private static int callCount = 0;
    
    public EchoingShadowConfiguredFeature() {
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
        
        // Spawn the anomaly a few blocks above the surface
        int anomalyY = surfaceInfo.surfacePos.getY() + 2 + random.nextInt(3); // 2-4 blocks above surface
        BlockPos anomalyPos = new BlockPos(origin.getX(), anomalyY, origin.getZ());
        
        // Check if the area is clear of trees before placing the anomaly
        if (!WorldGenUtils.isAreaClearOfTrees(level, anomalyPos, 2)) {
            return false; // Skip placement if area is not clear of trees
        }
        
        // Place shadow-themed terrain modifications
        // Reduced radius from 3 to 2 for better performance while maintaining visual impact
        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                
                // Only place grass within the circle and with some randomness for patchiness
                if (distance <= radius && random.nextFloat() < 0.7f) { // 70% chance for patchiness
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
        
        // Spawn the echoing shadow entity above the surface
        EchoingShadowEntity anomaly = new EchoingShadowEntity(StrangeMatterMod.ECHOING_SHADOW.get(), level.getLevel());
        anomaly.moveTo(anomalyPos.getX() + 0.5, anomalyPos.getY(), anomalyPos.getZ() + 0.5, 0.0f, 0.0f);
        
        return level.getLevel().addFreshEntity(anomaly);
    }
}
