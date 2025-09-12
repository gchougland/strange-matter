package com.hexvane.strangematter.event;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
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
    
    private static void placeAnomalousGrassAndOre(ServerLevel level, BlockPos centerPos) {
        var random = level.getRandom();
        
        
        // Generate anomalous grass in a patch underneath, following terrain contours
        int radius = 5; // Same as manually created gates
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x*x + z*z <= radius*radius) { // Circular pattern
                    BlockPos checkPos = centerPos.offset(x, 0, z);
                    
                    // Find the surface height at this position
                    BlockPos surfacePos = findSurfaceHeight(level, checkPos);
                    
                    // Check if we should place grass here (not always 100% chance)
                    if (random.nextFloat() < 0.8f) {
                        var currentBlock = level.getBlockState(surfacePos);
                        if (currentBlock.is(Blocks.GRASS_BLOCK) || 
                            currentBlock.is(Blocks.DIRT) ||
                            currentBlock.is(Blocks.COARSE_DIRT) ||
                            currentBlock.is(Blocks.PODZOL) ||
                            currentBlock.is(Blocks.STONE)) {
                            level.setBlock(surfacePos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 3);
                            
                            // Place resonite ore underneath this grass block
                            placeOreUnderGrass(level, surfacePos, random);
                        }
                    }
                }
            }
        }
        
    }
    
    private static BlockPos findSurfaceHeight(ServerLevel level, BlockPos pos) {
        // Start from a reasonable height and work down to find the surface
        int startY = Math.max(level.getMaxBuildHeight() - 10, pos.getY() + 50);
        
        for (int y = startY; y >= level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            var blockState = level.getBlockState(checkPos);
            
            // Found a solid block that could be the surface
            if (blockState.isSolid() && !blockState.isAir()) {
                return checkPos;
            }
        }
        
        // Fallback to original position if we can't find surface
        return pos.below(1);
    }
    
    private static void placeOreUnderGrass(ServerLevel level, BlockPos grassPos, net.minecraft.util.RandomSource random) {
        // Place resonite ore sparsely - only directly under this grass block, going 10 layers deep
        // Only place ore directly under this grass block (no spreading), and only 15% of the time
        if (random.nextFloat() < 0.15f) { // 15% chance to place any ore at all
            for (int y = 1; y <= 10; y++) { // 10 layers deep
                BlockPos orePos = grassPos.below(y);
                
                // Only place ore if there's solid ground or anomalous grass above it
                var blockAbove = level.getBlockState(orePos.above(y));
                if (blockAbove.is(StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get()) || 
                    blockAbove.isSolid()) {
                    // Even if we decide to place ore, only 15% chance for each layer
                    if (random.nextFloat() < 0.15f) {
                        level.setBlock(orePos, StrangeMatterMod.RESONITE_ORE_BLOCK.get().defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
