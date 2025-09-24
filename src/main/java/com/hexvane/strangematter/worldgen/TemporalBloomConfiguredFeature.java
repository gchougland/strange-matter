package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.TemporalBloomEntity;
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

public class TemporalBloomConfiguredFeature extends Feature<NoneFeatureConfiguration> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("StrangeMatter:TemporalBloomFeature");
    private static int callCount = 0;
    
    public TemporalBloomConfiguredFeature() {
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
        
        LOGGER.info("TemporalBloomFeature.place() called #{} at position: {} in dimension: {}", 
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
        
        // Spawn the anomaly a few blocks above the surface
        int anomalyY = surfaceY + 2 + random.nextInt(3); // 2-4 blocks above surface
        BlockPos anomalyPos = new BlockPos(origin.getX(), anomalyY, origin.getZ());
        
        LOGGER.info("Surface Y: {}, Anomaly Y: {}, Final position: {}", surfaceY, anomalyY, anomalyPos);
        
        // Place anomalous grass in a patchy circle following terrain contour
        int radius = 3;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                
                // Only place grass within the circle and with some randomness for patchiness
                if (distance <= radius && random.nextFloat() < 0.7f) { // 70% chance for patchiness
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
                            
                            // Occasionally place tilled soil with wheat seeds above the grass
                            if (random.nextFloat() < 0.7f) { // 70% chance for wheat fields
                                BlockPos abovePos = grassPos.above();
                                if (level.getBlockState(abovePos).isAir()) {
                                    // Place farmland (tilled soil)
                                    level.setBlock(abovePos, net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState(), 3);
                                    
                                    // Place wheat seeds on the farmland
                                    BlockPos wheatPos = abovePos.above();
                                    if (level.getBlockState(wheatPos).isAir()) {
                                        level.setBlock(wheatPos, net.minecraft.world.level.block.Blocks.WHEAT.defaultBlockState()
                                            .setValue(net.minecraft.world.level.block.CropBlock.AGE, random.nextInt(3)), 3);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Generate resonite ore clumps underneath the anomaly
        generateResoniteOreClumps(level, random, groundPos);
        
        // Spawn the temporal bloom entity above the surface
        TemporalBloomEntity anomaly = new TemporalBloomEntity(StrangeMatterMod.TEMPORAL_BLOOM.get(), level.getLevel());
        anomaly.moveTo(anomalyPos.getX() + 0.5, anomalyPos.getY(), anomalyPos.getZ() + 0.5, 0.0f, 0.0f);
        
        boolean success = level.getLevel().addFreshEntity(anomaly);
        LOGGER.info("Successfully placed temporal bloom at: {} - Success: {}", anomalyPos, success);
        
        return success;
    }
    
    private void generateResoniteOreClumps(WorldGenLevel level, RandomSource random, BlockPos centerPos) {
        // Generate 2-4 ore clumps directly under the anomaly
        int clumpCount = 2 + random.nextInt(3); // 2-4 clumps
        
        for (int i = 0; i < clumpCount; i++) {
            // Small offset around the center (within 3 blocks)
            int offsetX = random.nextInt(7) - 3; // -3 to +3
            int offsetZ = random.nextInt(7) - 3; // -3 to +3
            
            BlockPos clumpCenter = centerPos.offset(offsetX, 0, offsetZ);
            
            // Generate ore in a small clump (3-6 blocks)
            int oreCount = 3 + random.nextInt(4); // 3-6 ores per clump
            
            for (int j = 0; j < oreCount; j++) {
                // Random position within 2 blocks of clump center
                int oreX = clumpCenter.getX() + random.nextInt(5) - 2; // -2 to +2
                int oreZ = clumpCenter.getZ() + random.nextInt(5) - 2; // -2 to +2
                
                // Find a suitable Y position directly under the anomaly (within 16 blocks)
                int oreY = centerPos.getY() - (1 + random.nextInt(16)); // 1-16 blocks below
                BlockPos orePos = new BlockPos(oreX, oreY, oreZ);
                
                // Check if the position is valid and within world bounds
                if (orePos.getY() >= level.getMinBuildHeight() + 10) {
                    var blockState = level.getBlockState(orePos);
                    if (blockState.is(net.minecraft.world.level.block.Blocks.STONE) || 
                        blockState.is(net.minecraft.world.level.block.Blocks.DEEPSLATE)) {
                        level.setBlock(orePos, StrangeMatterMod.RESONITE_ORE_BLOCK.get().defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
