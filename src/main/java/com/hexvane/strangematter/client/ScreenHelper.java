package com.hexvane.strangematter.client;

import com.hexvane.strangematter.client.screen.ResearchMachineScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Helper class for opening client-only screens
 * Isolated to prevent Screen class loading on server
 */
public class ScreenHelper {
    
    public static void openResearchMachineScreen(BlockEntity blockEntity) {
        Minecraft.getInstance().setScreen(new ResearchMachineScreen(blockEntity));
    }
}

