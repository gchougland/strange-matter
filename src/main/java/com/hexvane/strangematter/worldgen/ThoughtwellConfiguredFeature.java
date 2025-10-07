package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.ThoughtwellEntity;
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

public class ThoughtwellConfiguredFeature extends Feature<NoneFeatureConfiguration> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("StrangeMatter:ThoughtwellFeature");
    private static int callCount = 0;
    
    public ThoughtwellConfiguredFeature() {
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
        
        LOGGER.info("ThoughtwellFeature.place() called #{} at position: {} in dimension: {}", 
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
        
        // Let the entity handle terrain generation using the base class method
        // This ensures consistent behavior and includes shard ore spawning

        int radius = 3;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);

                if (distance <= radius && random.nextFloat() < 0.6f) { // 60% chance for patchiness
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
                            // Occasionally place some cognitive-themed blocks above the grass
                            if (random.nextFloat() < 0.4f) { // 40% chance for cognitive features
                                BlockPos abovePos = grassPos.above();
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
        
        // Spawn the thoughtwell entity above the surface
        ThoughtwellEntity anomaly = new ThoughtwellEntity(StrangeMatterMod.THOUGHTWELL.get(), level.getLevel());
        anomaly.moveTo(anomalyPos.getX() + 0.5, anomalyPos.getY(), anomalyPos.getZ() + 0.5, 0.0f, 0.0f);
        
        boolean success = level.getLevel().addFreshEntity(anomaly);
        LOGGER.info("Successfully placed thoughtwell at: {} - Success: {}", anomalyPos, success);
        
        return success;
    }
    
}
