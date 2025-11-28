package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.Config;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.WorldGenLevel;

import java.util.Set;

/**
 * Utility class for efficient world generation operations.
 * Provides optimized methods for common world generation tasks.
 */
public class WorldGenUtils {
    private static final Set<Block> EXCLUDED_TREE_BLOCKS = Set.of(
            Blocks.OAK_LEAVES,
            Blocks.ACACIA_LEAVES,
            Blocks.BIRCH_LEAVES,
            Blocks.DARK_OAK_LEAVES,
            Blocks.JUNGLE_LEAVES,
            Blocks.SPRUCE_LEAVES,
            Blocks.CHERRY_LEAVES,
            Blocks.MANGROVE_LEAVES,
            Blocks.OAK_LOG,
            Blocks.ACACIA_LOG,
            Blocks.BIRCH_LOG,
            Blocks.DARK_OAK_LOG,
            Blocks.JUNGLE_LOG,
            Blocks.SPRUCE_LOG,
            Blocks.CHERRY_LOG,
            Blocks.MANGROVE_LOG,
            Blocks.STRIPPED_OAK_LOG,
            Blocks.STRIPPED_ACACIA_LOG,
            Blocks.STRIPPED_BIRCH_LOG,
            Blocks.STRIPPED_DARK_OAK_LOG,
            Blocks.STRIPPED_JUNGLE_LOG,
            Blocks.STRIPPED_SPRUCE_LOG,
            Blocks.STRIPPED_CHERRY_LOG,
            Blocks.STRIPPED_MANGROVE_LOG,
            Blocks.OAK_WOOD,
            Blocks.ACACIA_WOOD,
            Blocks.BIRCH_WOOD,
            Blocks.DARK_OAK_WOOD,
            Blocks.JUNGLE_WOOD,
            Blocks.SPRUCE_WOOD,
            Blocks.CHERRY_WOOD,
            Blocks.MANGROVE_WOOD,
            Blocks.STRIPPED_OAK_WOOD,
            Blocks.STRIPPED_ACACIA_WOOD,
            Blocks.STRIPPED_BIRCH_WOOD,
            Blocks.STRIPPED_DARK_OAK_WOOD,
            Blocks.STRIPPED_JUNGLE_WOOD,
            Blocks.STRIPPED_SPRUCE_WOOD,
            Blocks.STRIPPED_CHERRY_WOOD,
            Blocks.STRIPPED_MANGROVE_WOOD
    );
    
    /**
     * Efficiently checks if a block state represents solid ground.
     * Avoids expensive string operations by using block tags and direct comparisons.
     * Excludes all tree-related blocks (logs, leaves, etc.)
     */
    @SuppressWarnings("deprecation")
    public static boolean isSolidGround(BlockState state) {
        if (!state.isSolid() || state.isAir()) {
            return false;
        }

        return !EXCLUDED_TREE_BLOCKS.contains(state.getBlock());
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
        return EXCLUDED_TREE_BLOCKS.contains(state.getBlock());
    }
    
    /**
     * Finds a suitable position for placing anomalous grass at the given x,z coordinates.
     * Returns the ground position if found, or null if no suitable position exists.
     */
    public static BlockPos findAnomalousGrassPosition(WorldGenLevel level, int x, int z) {
        SurfaceInfo surfaceInfo = findSurfaceAndGround(level, x, z);
        if (surfaceInfo == null) {
            return null;
        }
        BlockPos groundPos = surfaceInfo.groundPos;
        BlockState state = level.getBlockState(groundPos);
        if (isSolidGround(state)) {
            return groundPos;
        }
        return null;
    }
    
    /**
     * Checks if a block state represents actual ground suitable for placing blocks on.
     * This is a simplified check that returns true for most solid blocks.
     */
    public static boolean isActualGround(BlockState state) {
        return isSolidGround(state);
    }
    
    /**
     * Places anomalous grass in a patchy circle around the origin position.
     * Uses config values and the provided parameters for terrain modification.
     * 
     * @param level The world generation level
     * @param origin The center position for grass placement
     * @param radius The radius of the circle
     * @param chance The chance (0.0-1.0) for each block to have grass placed
     * @param random The random source for patchiness
     */
    public static void placeAnomalousGrassPatch(WorldGenLevel level, BlockPos origin, int radius, float chance, RandomSource random) {
        if (!Config.enableAnomalousGrass) {
            return;
        }
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                
                // Only place grass within the circle and with some randomness for patchiness
                if (distance <= radius && random.nextFloat() < chance) {
                    BlockPos grassPos = findAnomalousGrassPosition(level, 
                        origin.getX() + x, origin.getZ() + z);
                    
                    if (grassPos != null) {
                        level.setBlock(grassPos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 3);
                    }
                }
            }
        }
    }
    
    /**
     * Places resonite ore and shard ore underneath the anomaly.
     * Uses config values for ore spawn chances and replacement blocks.
     * 
     * @param level The world generation level
     * @param origin The center position for ore placement
     * @param radius The radius of the circle
     * @param random The random source for ore placement
     * @param shardOreBlock The shard ore block type to place
     */
    public static void placeAnomalyOres(WorldGenLevel level, BlockPos origin, int radius, RandomSource random, Block shardOreBlock) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                
                if (distance <= radius) {
                    BlockPos columnStart = origin.offset(x, -1, z);
                    
                    // Place resonite ore
                    placeOreColumn(level, columnStart, random, 
                        StrangeMatterMod.RESONITE_ORE_BLOCK.get().defaultBlockState(), 
                        Config.resoniteOreSpawnChanceNearAnomaly);
                    
                    // Place shard ore
                    placeOreColumn(level, columnStart, random, 
                        shardOreBlock.defaultBlockState(), 
                        Config.shardOreSpawnChanceNearAnomaly);
                }
            }
        }
    }
    
    /**
     * Places an ore column starting from a base position.
     * Finds the first replaceable block and places ore downward with a chance.
     */
    private static void placeOreColumn(WorldGenLevel level, BlockPos startPos, RandomSource random, 
                                      BlockState oreState, double chance) {
        if (startPos == null || oreState == null || chance <= 0.0) {
            return;
        }
        
        if (random.nextDouble() >= chance) {
            return;
        }
        
        // Find the first replaceable block
        BlockPos basePos = findFirstReplaceableOreBlock(level, startPos);
        if (basePos == null) {
            return;
        }
        
        int minY = level.getMinBuildHeight();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(basePos.getX(), basePos.getY(), basePos.getZ());
        int columnLength = 1 + random.nextInt(3); // 1-3 blocks per column
        
        for (int placed = 0; placed < columnLength && mutable.getY() >= minY; ) {
            BlockState state = level.getBlockState(mutable);
            
            if (!canReplaceWithOre(state)) {
                // If we haven't placed anything yet, keep searching downward for a valid starting block
                if (placed == 0) {
                    mutable.move(0, -1, 0);
                    continue;
                }
                break;
            }
            
            BlockPos placePos = mutable.immutable();
            level.setBlock(placePos, oreState, 3);
            placed++;
            
            mutable.move(0, -1, 0);
        }
    }
    
    /**
     * Find the first replaceable block for ore placement.
     */
    private static BlockPos findFirstReplaceableOreBlock(WorldGenLevel level, BlockPos startPos) {
        if (startPos == null || level == null) {
            return null;
        }
        
        int minY = level.getMinBuildHeight();
        int maxY = Math.min(startPos.getY(), level.getMaxBuildHeight() - 1);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(startPos.getX(), maxY, startPos.getZ());
        
        for (int y = maxY; y >= minY; y--) {
            mutable.set(startPos.getX(), y, startPos.getZ());
            BlockState state = level.getBlockState(mutable);
            if (state.isAir()) {
                continue;
            }
            
            if (canReplaceWithOre(state)) {
                return mutable.immutable();
            }
        }
        
        return null;
    }
    
    /**
     * Check if a block can be replaced with ore.
     */
    private static boolean canReplaceWithOre(BlockState state) {
        return Config.anomalyOreReplaceableBlocks.contains(state.getBlock());
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
