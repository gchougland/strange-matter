package com.hexvane.strangematter.event;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StrangeMatterMod.MODID)
public class WarpGateSpawnEventHandler {
    
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Check if this is a warp gate entity joining the level
        if (event.getEntity() instanceof WarpGateAnomalyEntity warpGate) {
            // Schedule block placement for the next tick to ensure the entity is fully spawned
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().tell(new net.minecraft.server.TickTask(1, () -> {
                    placeAnomalousGrassAndOre(serverLevel, warpGate.blockPosition());
                }));
            }
        }
    }
    
    /**
     * Place anomalous grass and ore around the warp gate
     */
    private static void placeAnomalousGrassAndOre(ServerLevel level, BlockPos centerPos) {
        final int TERRAIN_MODIFICATION_RADIUS = 8;
        
        // Create a patch of anomalous grass and resonite ore underneath
        for (int x = -TERRAIN_MODIFICATION_RADIUS; x <= TERRAIN_MODIFICATION_RADIUS; x++) {
            for (int z = -TERRAIN_MODIFICATION_RADIUS; z <= TERRAIN_MODIFICATION_RADIUS; z++) {
                // Only modify blocks within a circular radius
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= TERRAIN_MODIFICATION_RADIUS) {
                    BlockPos pos = centerPos.offset(x, 0, z);
                    
                    // Find the surface block for grass placement
                    BlockPos surfacePos = findSurfaceHeight(level, pos);
                    if (surfacePos != null) {
                        // Check if we should place anomalous grass
                        BlockState currentState = level.getBlockState(surfacePos);
                        BlockPos targetPos = surfacePos;
                        
                        // If there's snow or vegetation on top, check the block underneath
                        if (currentState.is(Blocks.SNOW) || currentState.is(Blocks.SNOW_BLOCK) ||
                            currentState.is(Blocks.TALL_GRASS) || currentState.is(Blocks.FERN) ||
                            currentState.is(Blocks.LARGE_FERN) || currentState.is(Blocks.DEAD_BUSH) ||
                            currentState.is(Blocks.SWEET_BERRY_BUSH) || currentState.is(Blocks.GRASS)) {
                            // Check the block underneath
                            BlockPos belowPos = surfacePos.below();
                            BlockState belowState = level.getBlockState(belowPos);
                            if (belowState.is(Blocks.GRASS_BLOCK) || belowState.is(Blocks.DIRT) ||
                                belowState.is(Blocks.COARSE_DIRT) || belowState.is(Blocks.PODZOL)) {
                                targetPos = belowPos;
                                currentState = belowState;
                            }
                        }
                        
                        // Place anomalous grass on suitable surface blocks
                        if (currentState.is(Blocks.GRASS_BLOCK) || currentState.is(Blocks.DIRT) || 
                            currentState.is(Blocks.COARSE_DIRT) || currentState.is(Blocks.PODZOL) ||
                            currentState.is(Blocks.SAND) || currentState.is(Blocks.RED_SAND) ||
                            currentState.is(Blocks.TERRACOTTA) || currentState.is(Blocks.WHITE_TERRACOTTA) ||
                            currentState.is(Blocks.ORANGE_TERRACOTTA) || currentState.is(Blocks.MAGENTA_TERRACOTTA) ||
                            currentState.is(Blocks.LIGHT_BLUE_TERRACOTTA) || currentState.is(Blocks.YELLOW_TERRACOTTA) ||
                            currentState.is(Blocks.LIME_TERRACOTTA) || currentState.is(Blocks.PINK_TERRACOTTA) ||
                            currentState.is(Blocks.GRAY_TERRACOTTA) || currentState.is(Blocks.LIGHT_GRAY_TERRACOTTA) ||
                            currentState.is(Blocks.CYAN_TERRACOTTA) || currentState.is(Blocks.PURPLE_TERRACOTTA) ||
                            currentState.is(Blocks.BLUE_TERRACOTTA) || currentState.is(Blocks.BROWN_TERRACOTTA) ||
                            currentState.is(Blocks.GREEN_TERRACOTTA) || currentState.is(Blocks.RED_TERRACOTTA) ||
                            currentState.is(Blocks.BLACK_TERRACOTTA) || currentState.is(Blocks.SNOW) ||
                            currentState.is(Blocks.SNOW_BLOCK)) {
                            level.setBlock(targetPos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 3);
                        }
                    }
                    
                    // Place ore underground regardless of grass placement
                    if (surfacePos != null) {
                        int surfaceY = surfacePos.getY();
                        int oreY = surfaceY - (1 + level.getRandom().nextInt(5)); // 1-5 blocks below surface
                        
                        // Place resonite ore (60% chance)
                        if (level.getRandom().nextFloat() < 0.6f) {
                            BlockPos orePos = new BlockPos(pos.getX(), oreY, pos.getZ());
                            BlockState oreState = level.getBlockState(orePos);
                            if (canReplaceWithOre(oreState)) {
                                level.setBlock(orePos, StrangeMatterMod.RESONITE_ORE_BLOCK.get().defaultBlockState(), 3);
                            }
                        }
                        
                        // Place spatial shard ore (30% chance)
                        if (level.getRandom().nextFloat() < 0.3f) {
                            int shardOreY = surfaceY - (1 + level.getRandom().nextInt(5)); // 1-5 blocks below surface
                            BlockPos shardOrePos = new BlockPos(pos.getX(), shardOreY, pos.getZ());
                            BlockState shardOreState = level.getBlockState(shardOrePos);
                            if (canReplaceWithOre(shardOreState)) {
                                level.setBlock(shardOrePos, StrangeMatterMod.SPATIAL_SHARD_ORE_BLOCK.get().defaultBlockState(), 3);
                            }
                        }
                    } else {
                        // Fallback: use warp gate position - 5 blocks if no surface found
                        int warpGateY = centerPos.getY() - 5;
                        int oreY = warpGateY - (1 + level.getRandom().nextInt(5)); // 1-5 blocks below warp gate
                        
                        // Place resonite ore (60% chance)
                        if (level.getRandom().nextFloat() < 0.5f) {
                            BlockPos orePos = new BlockPos(pos.getX(), oreY, pos.getZ());
                            BlockState oreState = level.getBlockState(orePos);
                            if (canReplaceWithOre(oreState)) {
                                level.setBlock(orePos, StrangeMatterMod.RESONITE_ORE_BLOCK.get().defaultBlockState(), 3);
                            }
                        }
                        
                        // Place spatial shard ore (30% chance)
                        if (level.getRandom().nextFloat() < 0.2f) {
                            int shardOreY = warpGateY - (1 + level.getRandom().nextInt(5)); // 1-5 blocks below warp gate
                            BlockPos shardOrePos = new BlockPos(pos.getX(), shardOreY, pos.getZ());
                            BlockState shardOreState = level.getBlockState(shardOrePos);
                            if (canReplaceWithOre(shardOreState)) {
                                level.setBlock(shardOrePos, StrangeMatterMod.SPATIAL_SHARD_ORE_BLOCK.get().defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Find the surface height at the given x,z coordinates
     */
    private static BlockPos findSurfaceHeight(Level level, BlockPos pos) {
        // Start from a reasonable height and work down
        int startY = Math.min(pos.getY() + 10, level.getMaxBuildHeight() - 1);
        
        for (int y = startY; y >= level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState state = level.getBlockState(checkPos);
            
            // Look for ground-level blocks (not leaves, logs, etc.)
            if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || 
                state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL) ||
                state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) ||
                state.is(Blocks.STONE) || state.is(Blocks.SANDSTONE) ||
                state.is(Blocks.RED_SANDSTONE) || state.is(Blocks.TERRACOTTA) ||
                state.is(Blocks.WHITE_TERRACOTTA) || state.is(Blocks.ORANGE_TERRACOTTA) ||
                state.is(Blocks.MAGENTA_TERRACOTTA) || state.is(Blocks.LIGHT_BLUE_TERRACOTTA) ||
                state.is(Blocks.YELLOW_TERRACOTTA) || state.is(Blocks.LIME_TERRACOTTA) ||
                state.is(Blocks.PINK_TERRACOTTA) || state.is(Blocks.GRAY_TERRACOTTA) ||
                state.is(Blocks.LIGHT_GRAY_TERRACOTTA) || state.is(Blocks.CYAN_TERRACOTTA) ||
                state.is(Blocks.PURPLE_TERRACOTTA) || state.is(Blocks.BLUE_TERRACOTTA) ||
                state.is(Blocks.BROWN_TERRACOTTA) || state.is(Blocks.GREEN_TERRACOTTA) ||
                state.is(Blocks.RED_TERRACOTTA) || state.is(Blocks.BLACK_TERRACOTTA) ||
                state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) {
                return checkPos;
            }
        }
        
        return null;
    }
    
    /**
     * Check if a block can be replaced with ore
     */
    private static boolean canReplaceWithOre(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) ||
               state.is(Blocks.ANDESITE) || state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) ||
               state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE) ||
               state.is(Blocks.TERRACOTTA) || state.is(Blocks.WHITE_TERRACOTTA) ||
               state.is(Blocks.ORANGE_TERRACOTTA) || state.is(Blocks.MAGENTA_TERRACOTTA) ||
               state.is(Blocks.LIGHT_BLUE_TERRACOTTA) || state.is(Blocks.YELLOW_TERRACOTTA) ||
               state.is(Blocks.LIME_TERRACOTTA) || state.is(Blocks.PINK_TERRACOTTA) ||
               state.is(Blocks.GRAY_TERRACOTTA) || state.is(Blocks.LIGHT_GRAY_TERRACOTTA) ||
               state.is(Blocks.CYAN_TERRACOTTA) || state.is(Blocks.PURPLE_TERRACOTTA) ||
               state.is(Blocks.BLUE_TERRACOTTA) || state.is(Blocks.BROWN_TERRACOTTA) ||
               state.is(Blocks.GREEN_TERRACOTTA) || state.is(Blocks.RED_TERRACOTTA) ||
               state.is(Blocks.BLACK_TERRACOTTA);
    }
}
