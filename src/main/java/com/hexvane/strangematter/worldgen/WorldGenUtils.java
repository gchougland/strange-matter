package com.hexvane.strangematter.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.WorldGenLevel;

/**
 * Utility class for efficient world generation operations.
 * Provides optimized methods for common world generation tasks.
 */
public class WorldGenUtils {
    
    /**
     * Efficiently checks if a block state represents solid ground.
     * Avoids expensive string operations by using block tags and direct comparisons.
     * Excludes all tree-related blocks (logs, leaves, etc.)
     */
    public static boolean isSolidGround(BlockState state) {
        if (!state.isSolid() || state.isAir()) {
            return false;
        }
        
        // Exclude all tree-related blocks (logs, leaves, etc.)
        return !state.is(Blocks.OAK_LEAVES) && 
               !state.is(Blocks.ACACIA_LEAVES) &&
               !state.is(Blocks.BIRCH_LEAVES) &&
               !state.is(Blocks.DARK_OAK_LEAVES) &&
               !state.is(Blocks.JUNGLE_LEAVES) &&
               !state.is(Blocks.SPRUCE_LEAVES) &&
               !state.is(Blocks.CHERRY_LEAVES) &&
               !state.is(Blocks.MANGROVE_LEAVES) &&
               // Exclude all log types
               !state.is(Blocks.OAK_LOG) &&
               !state.is(Blocks.ACACIA_LOG) &&
               !state.is(Blocks.BIRCH_LOG) &&
               !state.is(Blocks.DARK_OAK_LOG) &&
               !state.is(Blocks.JUNGLE_LOG) &&
               !state.is(Blocks.SPRUCE_LOG) &&
               !state.is(Blocks.CHERRY_LOG) &&
               !state.is(Blocks.MANGROVE_LOG) &&
               // Exclude stripped logs
               !state.is(Blocks.STRIPPED_OAK_LOG) &&
               !state.is(Blocks.STRIPPED_ACACIA_LOG) &&
               !state.is(Blocks.STRIPPED_BIRCH_LOG) &&
               !state.is(Blocks.STRIPPED_DARK_OAK_LOG) &&
               !state.is(Blocks.STRIPPED_JUNGLE_LOG) &&
               !state.is(Blocks.STRIPPED_SPRUCE_LOG) &&
               !state.is(Blocks.STRIPPED_CHERRY_LOG) &&
               !state.is(Blocks.STRIPPED_MANGROVE_LOG) &&
               // Exclude wood blocks
               !state.is(Blocks.OAK_WOOD) &&
               !state.is(Blocks.ACACIA_WOOD) &&
               !state.is(Blocks.BIRCH_WOOD) &&
               !state.is(Blocks.DARK_OAK_WOOD) &&
               !state.is(Blocks.JUNGLE_WOOD) &&
               !state.is(Blocks.SPRUCE_WOOD) &&
               !state.is(Blocks.CHERRY_WOOD) &&
               !state.is(Blocks.MANGROVE_WOOD) &&
               // Exclude stripped wood
               !state.is(Blocks.STRIPPED_OAK_WOOD) &&
               !state.is(Blocks.STRIPPED_ACACIA_WOOD) &&
               !state.is(Blocks.STRIPPED_BIRCH_WOOD) &&
               !state.is(Blocks.STRIPPED_DARK_OAK_WOOD) &&
               !state.is(Blocks.STRIPPED_JUNGLE_WOOD) &&
               !state.is(Blocks.STRIPPED_SPRUCE_WOOD) &&
               !state.is(Blocks.STRIPPED_CHERRY_WOOD) &&
               !state.is(Blocks.STRIPPED_MANGROVE_WOOD);
    }
    
    /**
     * Efficiently finds solid ground below a given position.
     * Stops at a reasonable depth to avoid infinite loops.
     * 
     * @param level The world generation level
     * @param startPos The starting position to search from
     * @return The position of solid ground, or null if none found
     */
    public static BlockPos findSolidGround(WorldGenLevel level, BlockPos startPos) {
        BlockPos pos = startPos;
        int minY = level.getMinBuildHeight() + 10;
        
        while (pos.getY() > minY) {
            BlockState state = level.getBlockState(pos);
            if (isSolidGround(state)) {
                return pos;
            }
            pos = pos.below();
        }
        
        return null; // No solid ground found
    }
    
    /**
     * Finds the surface position and solid ground in one efficient operation.
     * 
     * @param level The world generation level
     * @param x X coordinate
     * @param z Z coordinate
     * @return SurfaceInfo containing both surface and ground positions, or null if invalid
     */
    public static SurfaceInfo findSurfaceAndGround(WorldGenLevel level, int x, int z) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        BlockPos surfacePos = new BlockPos(x, surfaceY, z);
        
        BlockPos groundPos = findSolidGround(level, surfacePos);
        if (groundPos == null) {
            return null;
        }
        
        return new SurfaceInfo(surfacePos, groundPos);
    }
    
    /**
     * Checks if a position and surrounding area is clear of trees for anomaly spawning.
     * This prevents anomalies from spawning inside tree trunks or dense foliage.
     * 
     * @param level The world generation level
     * @param pos The position to check
     * @param radius The radius to check around the position
     * @return true if the area is clear of trees, false otherwise
     */
    public static boolean isAreaClearOfTrees(WorldGenLevel level, BlockPos pos, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -2; y <= 3; y++) { // Check from 2 blocks below to 3 blocks above
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                    
                    // If any block in the area is tree-related, the area is not clear
                    if (isTreeBlock(state)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Checks if a block state is tree-related (logs, leaves, wood, etc.)
     */
    public static boolean isTreeBlock(BlockState state) {
        return !isSolidGround(state) && state.isSolid() && !state.isAir();
    }
    
    /**
     * Data class to hold surface and ground position information.
     */
    public static class SurfaceInfo {
        public final BlockPos surfacePos;
        public final BlockPos groundPos;
        
        public SurfaceInfo(BlockPos surfacePos, BlockPos groundPos) {
            this.surfacePos = surfacePos;
            this.groundPos = groundPos;
        }
    }
}
