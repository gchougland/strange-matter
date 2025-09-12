package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.block.CrystalizedEctoplasmBlock;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrystalizedEctoplasmConfiguredFeature extends Feature<NoneFeatureConfiguration> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("StrangeMatter:CrystalizedEctoplasmFeature");
    private static int callCount = 0;
    
    public CrystalizedEctoplasmConfiguredFeature() {
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
        
        // Try to find a valid placement position
        BlockPos placementPos = findValidPlacement(level, origin, random);
        if (placementPos == null) {
            return false;
        }
        
        // Determine the facing direction based on the surface
        Direction facing = determineFacing(level, placementPos);
        if (facing == null) {
            LOGGER.debug("Could not determine facing direction for crystalized ectoplasm at {}", placementPos);
            return false;
        }
        
        // Create the block state with the correct facing
        BlockState crystalState = StrangeMatterMod.CRYSTALIZED_ECTOPLASM_BLOCK.get().defaultBlockState()
            .setValue(CrystalizedEctoplasmBlock.FACING, facing);
        
        // Place the block
        if (level.setBlock(placementPos, crystalState, 2)) {
            return true;
        }
        
        return false;
    }
    
    private BlockPos findValidPlacement(WorldGenLevel level, BlockPos origin, RandomSource random) {
        // Try multiple positions around the origin
        for (int attempts = 0; attempts < 20; attempts++) {
            int x = origin.getX() + random.nextInt(7) - 3;
            int y = origin.getY() + random.nextInt(7) - 3;
            int z = origin.getZ() + random.nextInt(7) - 3;
            
            BlockPos pos = new BlockPos(x, y, z);
            
            // Check if this position is air (where we want to place the crystal)
            if (!level.getBlockState(pos).isAir()) {
                continue;
            }
            
            // Check all 6 directions for a valid support block
            for (Direction direction : Direction.values()) {
                BlockPos supportPos = pos.relative(direction);
                BlockState supportState = level.getBlockState(supportPos);
                
                if (isValidSupportBlock(supportState)) {
                    // Found a valid support block, this position is good
                    return pos;
                }
            }
        }
        
        return null;
    }
    
    private boolean isValidSupportBlock(BlockState state) {
        // Allow placement on stone, deepslate, and other solid blocks
        return state.isSolid() && 
               !state.isAir() && 
               !state.is(Blocks.WATER) && 
               !state.is(Blocks.LAVA) &&
               !state.getBlock().getDescriptionId().contains("leaves");
    }
    
    private Direction determineFacing(WorldGenLevel level, BlockPos pos) {
        // Check all 6 directions to find the support block
        for (Direction direction : Direction.values()) {
            BlockPos supportPos = pos.relative(direction);
            BlockState supportState = level.getBlockState(supportPos);
            
            if (isValidSupportBlock(supportState)) {
                // Crystal is attached to a block in this direction
                // The crystal should point away from the support block (opposite direction)
                return direction.getOpposite();
            }
        }
        
        return null;
    }
}
