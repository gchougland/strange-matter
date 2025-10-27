package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        callCount++;
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        
        LOGGER.info("WarpGateAnomalyFeature.place() called #{} at position: {} in dimension: {}", 
                   callCount, origin, level.getLevel().dimension().location());
        
        // Use WORLD_SURFACE heightmap to find the top surface
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, origin.getX(), origin.getZ());
        BlockPos surfacePos = new BlockPos(origin.getX(), surfaceY, origin.getZ());
        
        // Find the actual solid ground below the surface (in case it's leaves, air, etc.)
        BlockPos groundPos = surfacePos;
        while (groundPos.getY() > level.getMinBuildHeight() + 10) {
            var blockState = level.getBlockState(groundPos);
            if (blockState.isSolid() && !blockState.isAir() && 
                !blockState.getBlock().getDescriptionId().contains("leaves")) {
                break; // Found solid ground (grass, dirt, stone, sand, etc. are all fine)
            }
            groundPos = groundPos.below();
        }
        
        // Check if we found valid solid ground
        if (groundPos.getY() <= level.getMinBuildHeight() + 10) {
            LOGGER.info("No solid ground found below surface at {}, skipping placement", surfacePos);
            return false;
        }
        
        LOGGER.info("Found solid ground at {} (surface was at {})", groundPos, surfacePos);
        
        // Spawn the warp gate a few blocks above the surface
        int anomalyY = surfaceY + 2;
        BlockPos anomalyPos = new BlockPos(origin.getX(), anomalyY, origin.getZ());
        
        LOGGER.info("Surface Y: {}, Warp Gate Y: {}, Final position: {}", surfaceY, anomalyY, anomalyPos);
        
        // Place anomalous grass in a patchy circle following terrain contour
        int radius = 5; // Slightly larger radius for warp gates
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                
                // Only place grass within the circle and with some randomness for patchiness
                if (distance <= radius && random.nextFloat() < 0.8f) { // 80% chance for patchiness
                    // Find the surface height at this offset position
                    int offsetSurfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, 
                        origin.getX() + x, origin.getZ() + z);
                    BlockPos offsetSurfacePos = new BlockPos(origin.getX() + x, offsetSurfaceY, origin.getZ() + z);
                    
                    // Find solid ground below this surface position
                    BlockPos grassPos = offsetSurfacePos;
                    while (grassPos.getY() > level.getMinBuildHeight() + 10) {
                        var blockState = level.getBlockState(grassPos);
                        if (blockState.isSolid() && !blockState.isAir() && 
                            !blockState.getBlock().getDescriptionId().contains("leaves")) {
                            break; // Found solid ground at this position
                        }
                        grassPos = grassPos.below();
                    }
                    
                    // Only place grass if we found valid solid ground
                    if (grassPos.getY() > level.getMinBuildHeight() + 10) {
                        var blockState = level.getBlockState(grassPos);
                        if (blockState.isSolid() && !blockState.isAir() && 
                            !blockState.getBlock().getDescriptionId().contains("leaves")) {
                            level.setBlock(grassPos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 3);
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
        
        boolean success = level.getLevel().addFreshEntity(anomaly);
        LOGGER.info("Successfully placed warp gate anomaly at: {} - Success: {}", anomalyPos, success);
        
        return success;
    }
}
