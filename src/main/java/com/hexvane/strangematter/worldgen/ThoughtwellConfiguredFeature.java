package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.ThoughtwellEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import javax.annotation.Nonnull;

public class ThoughtwellConfiguredFeature extends Feature<NoneFeatureConfiguration> {
    
    public ThoughtwellConfiguredFeature() {
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
        
        // Place cognitive-themed terrain
        placeCognitiveTerrain(level, origin, random, 3, 0.6f);
        
        // Spawn the thoughtwell entity above the surface
        ThoughtwellEntity anomaly = new ThoughtwellEntity(StrangeMatterMod.THOUGHTWELL.get(), level.getLevel());
        anomaly.moveTo(anomalyPos.getX() + 0.5, anomalyPos.getY(), anomalyPos.getZ() + 0.5, 0.0f, 0.0f);
        
        return level.getLevel().addFreshEntity(anomaly);
    }
    
    /**
     * Places cognitive-themed terrain (bookshelves, lecterns) around the origin position.
     * Uses WorldGenUtils for efficient ground detection.
     */
    private void placeCognitiveTerrain(WorldGenLevel level, BlockPos origin, RandomSource random, int radius, float chance) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                
                if (distance <= radius && random.nextFloat() < chance) {
                    BlockPos grassPos = WorldGenUtils.findAnomalousGrassPosition(level, 
                        origin.getX() + x, origin.getZ() + z);
                    
                    if (grassPos != null) {
                        // Place anomalous grass at the suitable position
                        level.setBlock(grassPos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 3);
                        
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
    
}
