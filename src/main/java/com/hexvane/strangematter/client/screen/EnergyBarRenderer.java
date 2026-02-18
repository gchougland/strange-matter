package com.hexvane.strangematter.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.hexvane.strangematter.StrangeMatterMod;

/**
 * Reusable energy bar renderer for machine screens.
 * Provides consistent energy bar display across all machines.
 */
public class EnergyBarRenderer {
    
    private static final ResourceLocation ENERGY_BAR_BACKGROUND = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/energy_bar_background.png");
    private static final ResourceLocation ENERGY_BAR_FILL = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/energy_bar_fill.png");
    
    // Energy bar dimensions
    private static final int ENERGY_BAR_WIDTH = 160;
    private static final int ENERGY_BAR_HEIGHT = 16;
    private static final int ENERGY_FILL_PADDING_LEFT = 21;
    private static final int ENERGY_FILL_PADDING_RIGHT = 2;
    private static final int ENERGY_FILL_PADDING_TOP = 4;
    private static final int ENERGY_FILL_PADDING_BOTTOM = 5;
    
    /**
     * Render the energy bar at the specified position
     */
    public static void renderEnergyBar(GuiGraphics guiGraphics, int x, int y, int energyStored, int maxEnergyStored) {
        // Render energy bar background
        guiGraphics.blit(ENERGY_BAR_BACKGROUND, x, y, 0, 0, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT);
        
        // Render energy bar fill
        if (maxEnergyStored > 0) {
            float energyPercentage = (float) energyStored / (float) maxEnergyStored;
            int availableWidth = ENERGY_BAR_WIDTH - ENERGY_FILL_PADDING_LEFT - ENERGY_FILL_PADDING_RIGHT - 2;
            int fillWidth = (int) (availableWidth * energyPercentage);
            
            if (fillWidth > 0) {
                // Set scissor area to crop the fill texture
                int fillX = x + ENERGY_FILL_PADDING_LEFT;
                int fillY = y + ENERGY_FILL_PADDING_TOP;
                int fillHeight = ENERGY_BAR_HEIGHT - ENERGY_FILL_PADDING_TOP - ENERGY_FILL_PADDING_BOTTOM;
                
                // Enable scissor test
                guiGraphics.enableScissor(fillX, fillY, fillX + fillWidth, fillY + fillHeight);
                
                // Render the full fill texture (it will be clipped by scissor)
                guiGraphics.blit(ENERGY_BAR_FILL,
                    fillX, fillY,
                    0, 0,
                    ENERGY_BAR_WIDTH-9, ENERGY_BAR_HEIGHT,
                    ENERGY_BAR_WIDTH-9, ENERGY_BAR_HEIGHT);
                
                // Disable scissor test
                guiGraphics.disableScissor();
            }
        }
    }
    
    /**
     * Check if mouse is over the energy bar
     */
    public static boolean isMouseOverEnergyBar(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + ENERGY_BAR_WIDTH &&
               mouseY >= y && mouseY < y + ENERGY_BAR_HEIGHT;
    }
    
    /**
     * Get the tooltip component for the energy bar
     */
    public static Component getEnergyBarTooltip(int energyStored, int maxEnergyStored) {
        return Component.translatable("gui.strangematter.resonance_energy", energyStored, maxEnergyStored, com.hexvane.strangematter.Config.energyUnitDisplay);
    }
    
    /**
     * Get the standard energy bar width
     */
    public static int getEnergyBarWidth() {
        return ENERGY_BAR_WIDTH;
    }
    
    /**
     * Get the standard energy bar height
     */
    public static int getEnergyBarHeight() {
        return ENERGY_BAR_HEIGHT;
    }
}
