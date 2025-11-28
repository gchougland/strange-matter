package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

/**
 * Handles color tinting for shard crystal blocks
 */
@Mod.EventBusSubscriber(modid = StrangeMatterMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
        BlockColors blockColors = event.getBlockColors();
        
        // Register color handler for each crystal block
        registerCrystalColor(blockColors, StrangeMatterMod.SHADE_SHARD_CRYSTAL, SHADE_COLOR);
        registerCrystalColor(blockColors, StrangeMatterMod.GRAVITIC_SHARD_CRYSTAL, GRAVITIC_COLOR);
        registerCrystalColor(blockColors, StrangeMatterMod.ENERGETIC_SHARD_CRYSTAL, ENERGETIC_COLOR);
        registerCrystalColor(blockColors, StrangeMatterMod.INSIGHT_SHARD_CRYSTAL, INSIGHT_COLOR);
        registerCrystalColor(blockColors, StrangeMatterMod.CHRONO_SHARD_CRYSTAL, CHRONO_COLOR);
        registerCrystalColor(blockColors, StrangeMatterMod.SPATIAL_SHARD_CRYSTAL, SPATIAL_COLOR);
        
        // Register color handler for each shard lamp block
        registerCrystalColor(blockColors, StrangeMatterMod.SHADE_SHARD_LAMP, SHADE_COLOR);
        registerCrystalColor(blockColors, StrangeMatterMod.GRAVITIC_SHARD_LAMP, GRAVITIC_COLOR);
        registerCrystalColor(blockColors, StrangeMatterMod.ENERGETIC_SHARD_LAMP, ENERGETIC_COLOR);
        registerCrystalColor(blockColors, StrangeMatterMod.INSIGHT_SHARD_LAMP, INSIGHT_COLOR);
        registerCrystalColor(blockColors, StrangeMatterMod.CHRONO_SHARD_LAMP, CHRONO_COLOR);
        registerCrystalColor(blockColors, StrangeMatterMod.SPATIAL_SHARD_LAMP, SPATIAL_COLOR);
    }
    
    private static void registerCrystalColor(BlockColors blockColors, RegistryObject<Block> block, int color) {
        blockColors.register((state, level, pos, tintIndex) -> {
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
        event.getItemColors().register((stack, tintIndex) -> {
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
            } else if (block == StrangeMatterMod.SHADE_SHARD_LAMP.get()) {
                return (0xFF << 24) | ((SHADE_COLOR >> 16) & 0xFF) << 16 | ((SHADE_COLOR >> 8) & 0xFF) << 8 | (SHADE_COLOR & 0xFF);
            } else if (block == StrangeMatterMod.GRAVITIC_SHARD_LAMP.get()) {
                return (0xFF << 24) | ((GRAVITIC_COLOR >> 16) & 0xFF) << 16 | ((GRAVITIC_COLOR >> 8) & 0xFF) << 8 | (GRAVITIC_COLOR & 0xFF);
            } else if (block == StrangeMatterMod.ENERGETIC_SHARD_LAMP.get()) {
                return (0xFF << 24) | ((ENERGETIC_COLOR >> 16) & 0xFF) << 16 | ((ENERGETIC_COLOR >> 8) & 0xFF) << 8 | (ENERGETIC_COLOR & 0xFF);
            } else if (block == StrangeMatterMod.INSIGHT_SHARD_LAMP.get()) {
                return (0xFF << 24) | ((INSIGHT_COLOR >> 16) & 0xFF) << 16 | ((INSIGHT_COLOR >> 8) & 0xFF) << 8 | (INSIGHT_COLOR & 0xFF);
            } else if (block == StrangeMatterMod.CHRONO_SHARD_LAMP.get()) {
                return (0xFF << 24) | ((CHRONO_COLOR >> 16) & 0xFF) << 16 | ((CHRONO_COLOR >> 8) & 0xFF) << 8 | (CHRONO_COLOR & 0xFF);
            } else if (block == StrangeMatterMod.SPATIAL_SHARD_LAMP.get()) {
                return (0xFF << 24) | ((SPATIAL_COLOR >> 16) & 0xFF) << 16 | ((SPATIAL_COLOR >> 8) & 0xFF) << 8 | (SPATIAL_COLOR & 0xFF);
            }
            return 0xFFFFFFFF; // White/default color
        },
        StrangeMatterMod.SHADE_SHARD_CRYSTAL_ITEM.get(),
        StrangeMatterMod.GRAVITIC_SHARD_CRYSTAL_ITEM.get(),
        StrangeMatterMod.ENERGETIC_SHARD_CRYSTAL_ITEM.get(),
        StrangeMatterMod.INSIGHT_SHARD_CRYSTAL_ITEM.get(),
        StrangeMatterMod.CHRONO_SHARD_CRYSTAL_ITEM.get(),
        StrangeMatterMod.SPATIAL_SHARD_CRYSTAL_ITEM.get(),
        StrangeMatterMod.SHADE_SHARD_LAMP_ITEM.get(),
        StrangeMatterMod.GRAVITIC_SHARD_LAMP_ITEM.get(),
        StrangeMatterMod.ENERGETIC_SHARD_LAMP_ITEM.get(),
        StrangeMatterMod.INSIGHT_SHARD_LAMP_ITEM.get(),
        StrangeMatterMod.CHRONO_SHARD_LAMP_ITEM.get(),
        StrangeMatterMod.SPATIAL_SHARD_LAMP_ITEM.get()
        );
    }
}

