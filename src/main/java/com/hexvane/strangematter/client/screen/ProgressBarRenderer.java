package com.hexvane.strangematter.client.screen;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ProgressBarRenderer {

    private static final ResourceLocation PROGRESS_BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/condenser_bubbles.png");

    // Progress bar dimensions (full texture is 32x16, but we only show 16x16)
    private static final int PROGRESS_BAR_WIDTH = 32;
    private static final int PROGRESS_BAR_HEIGHT = 16;

    /**
     * Render the progress bar at the specified position
     */
    public static void renderProgressBar(GuiGraphics guiGraphics, int x, int y, int progressLevel, int maxProgressLevel) {
        if (maxProgressLevel > 0) {
            float progressPercentage = (float) progressLevel / (float) maxProgressLevel;
            
            // First, always render the full unfilled background (left 16x16 of texture)
            
            guiGraphics.enableScissor(x, y, x + 16, y + PROGRESS_BAR_HEIGHT);

            guiGraphics.blit(PROGRESS_BAR_TEXTURE,
                x, y,
                0, 0, // Source X, Y (left 16x16 of texture)
                32, 16, // Only render the left 16x16 portion
                32, 16); // Source width and height (16x16)
                
            guiGraphics.disableScissor();
            
            // Then render the filled portion (right 16x16 of texture) based on progress
            if (progressPercentage > 0) {
                int fillHeight = (int) (PROGRESS_BAR_HEIGHT * progressPercentage);
                
                if (fillHeight > 0) {
                    // Enable scissor test to crop the right 16x16 portion and from bottom
                    guiGraphics.enableScissor(x, y + (PROGRESS_BAR_HEIGHT - fillHeight), x + 16, y + PROGRESS_BAR_HEIGHT);
                    
                    // Render the full 32x16 texture (scissor will only show the right 16x16 portion)
                    guiGraphics.blit(PROGRESS_BAR_TEXTURE,
                        x, y, // Render at the original position
                        16, 0, // Source X, Y (full texture)
                        PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT, // Full texture size
                        32, 16); // Source width and height (full 32x16 texture)
                    
                    // Disable scissor test
                    guiGraphics.disableScissor();
                }
            }
        }
    }

    /**
     * Check if mouse is over the progress bar
     */
    public static boolean isMouseOverProgressBar(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + PROGRESS_BAR_WIDTH &&
               mouseY >= y && mouseY < y + PROGRESS_BAR_HEIGHT;
    }

    /**
     * Get the tooltip component for the progress bar
     */
    public static Component getProgressBarTooltip(int progressLevel, int maxProgressLevel) {
        return Component.translatable("gui.strangematter.progress", progressLevel, maxProgressLevel);
    }
}
