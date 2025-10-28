package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Handles color tinting for shard crystal blocks
 */
@EventBusSubscriber(modid = StrangeMatterMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ShardCrystalColorHandler {
    
    // Color definitions for each shard type (RGB values)
    private static final int SHADE_COLOR = 0x8752d0;      // Shadow: #8752d0
    private static final int GRAVITIC_COLOR = 0xd75335;   // Gravity: #d75335
    private static final int ENERGETIC_COLOR = 0x3abde8;  // Energy: #3abde8
    private static final int INSIGHT_COLOR = 0x54c55b;    // Cognition: #54c55b
    private static final int CHRONO_COLOR = 0xe6b538;     // Time: #e6b538
    private static final int SPATIAL_COLOR = 0x3d88dd;      // Space: #3d88dd
    
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        // Register color handler for each crystal block
        registerCrystalColor(event, StrangeMatterMod.SHADE_SHARD_CRYSTAL, SHADE_COLOR);
        registerCrystalColor(event, StrangeMatterMod.GRAVITIC_SHARD_CRYSTAL, GRAVITIC_COLOR);
        registerCrystalColor(event, StrangeMatterMod.ENERGETIC_SHARD_CRYSTAL, ENERGETIC_COLOR);
        registerCrystalColor(event, StrangeMatterMod.INSIGHT_SHARD_CRYSTAL, INSIGHT_COLOR);
        registerCrystalColor(event, StrangeMatterMod.CHRONO_SHARD_CRYSTAL, CHRONO_COLOR);
        registerCrystalColor(event, StrangeMatterMod.SPATIAL_SHARD_CRYSTAL, SPATIAL_COLOR);
    }
    
    private static void registerCrystalColor(RegisterColorHandlersEvent.Block event, DeferredHolder<Block, ? extends Block> block, int color) {
        event.register((state, level, pos, tintIndex) -> {
            // Extract RGB components from the color
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            // Return as ARGB (alpha=255, full opacity for texture tinting)
            return (0xFF << 24) | (r << 16) | (g << 8) | b;
        }, block.get());
    }
    
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Register the same color handler for items
        event.register((stack, tintIndex) -> {
            Block block = Block.byItem(stack.getItem());
            if (block == StrangeMatterMod.SHADE_SHARD_CRYSTAL.get()) {
                return (0xFF << 24) | ((SHADE_COLOR >> 16) & 0xFF) << 16 | ((SHADE_COLOR >> 8) & 0xFF) << 8 | (SHADE_COLOR & 0xFF);
            } else if (block == StrangeMatterMod.GRAVITIC_SHARD_CRYSTAL.get()) {
                return (0xFF << 24) | ((GRAVITIC_COLOR >> 16) & 0xFF) << 16 | ((GRAVITIC_COLOR >> 8) & 0xFF) << 8 | (GRAVITIC_COLOR & 0xFF);
            } else if (block == StrangeMatterMod.ENERGETIC_SHARD_CRYSTAL.get()) {
                return (0xFF << 24) | ((ENERGETIC_COLOR >> 16) & 0xFF) << 16 | ((ENERGETIC_COLOR >> 8) & 0xFF) << 8 | (ENERGETIC_COLOR & 0xFF);
            } else if (block == StrangeMatterMod.INSIGHT_SHARD_CRYSTAL.get()) {
                return (0xFF << 24) | ((INSIGHT_COLOR >> 16) & 0xFF) << 16 | ((INSIGHT_COLOR >> 8) & 0xFF) << 8 | (INSIGHT_COLOR & 0xFF);
            } else if (block == StrangeMatterMod.CHRONO_SHARD_CRYSTAL.get()) {
                return (0xFF << 24) | ((CHRONO_COLOR >> 16) & 0xFF) << 16 | ((CHRONO_COLOR >> 8) & 0xFF) << 8 | (CHRONO_COLOR & 0xFF);
            } else if (block == StrangeMatterMod.SPATIAL_SHARD_CRYSTAL.get()) {
                return (0xFF << 24) | ((SPATIAL_COLOR >> 16) & 0xFF) << 16 | ((SPATIAL_COLOR >> 8) & 0xFF) << 8 | (SPATIAL_COLOR & 0xFF);
            }
            return 0xFFFFFFFF; // White/default color
        },
        StrangeMatterMod.SHADE_SHARD_CRYSTAL_ITEM.get(),
        StrangeMatterMod.GRAVITIC_SHARD_CRYSTAL_ITEM.get(),
        StrangeMatterMod.ENERGETIC_SHARD_CRYSTAL_ITEM.get(),
        StrangeMatterMod.INSIGHT_SHARD_CRYSTAL_ITEM.get(),
        StrangeMatterMod.CHRONO_SHARD_CRYSTAL_ITEM.get(),
        StrangeMatterMod.SPATIAL_SHARD_CRYSTAL_ITEM.get()
        );
    }
}

