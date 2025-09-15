package com.hexvane.strangematter.client.screen.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import com.hexvane.strangematter.StrangeMatterMod;

public class InstabilityGauge {
    
    // Textures
    private static final ResourceLocation TUBE_TEXTURE = new ResourceLocation(StrangeMatterMod.MODID, "textures/ui/instability_tube.png");
    private static final ResourceLocation FILL_TEXTURE = new ResourceLocation(StrangeMatterMod.MODID, "textures/ui/instability_tube_fill.png");
    
    // Colors
    private static final int TEXT_COLOR = 0xFF3dc7c7;
    
    // Dimensions (adjust these based on your actual texture sizes)
    private static final int GAUGE_WIDTH = 50;
    private static final int GAUGE_HEIGHT = 140;
    
    private int x;
    private int y;
    private float fillLevel = 0.5f; // Default to half full (50%)
    
    public InstabilityGauge(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setFillLevel(float fillLevel) {
        this.fillLevel = Math.max(0.0f, Math.min(1.0f, fillLevel));
    }
    
    public float getFillLevel() {
        return fillLevel;
    }
    
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render the fill texture with scissoring
        renderFill(guiGraphics);
        
        // Render the tube texture on top
        guiGraphics.blit(TUBE_TEXTURE, x, y, 0, 0, GAUGE_WIDTH, GAUGE_HEIGHT, GAUGE_WIDTH, GAUGE_HEIGHT);
        
        // Render vertical text
        renderVerticalText(guiGraphics);
    }
    
    private void renderFill(GuiGraphics guiGraphics) {
        // Calculate the fill height based on the fill level
        int fillHeight = (int) (GAUGE_HEIGHT * fillLevel);
        
        if (fillHeight > 0) {
            // Render the fill texture with UV coordinates to show only the filled portion
            // The fill texture should be rendered from bottom up
            int fillY = y + GAUGE_HEIGHT - fillHeight;
            
            guiGraphics.blit(
                FILL_TEXTURE, 
                x, fillY, 
                0, GAUGE_HEIGHT - fillHeight, // UV offset to show only filled portion
                GAUGE_WIDTH, fillHeight, 
                GAUGE_WIDTH, GAUGE_HEIGHT
            );
        }
    }
    
    private void renderVerticalText(GuiGraphics guiGraphics) {
        String text = "INSTABILITY";
        
        // Calculate position for vertical text (to the left of the gauge)
        int textX = x + 7;
        int textY = y + (GAUGE_HEIGHT / 2) + (text.length()) + 10; // Center vertically (smaller text)

        // Render the text rotated 90 degrees clockwise (vertical reading from top to bottom)
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(textX, textY, 0);
        guiGraphics.pose().scale(0.8f, 0.8f, 1.0f);
        guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-90));
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            text,
            0, 0,
            TEXT_COLOR
        );
        guiGraphics.pose().popPose();
    }
    
    
    // Getter and setter for position (for future functionality)
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    // Get gauge dimensions (for layout purposes)
    public int getWidth() {
        return GAUGE_WIDTH + 20; // Include space for text
    }
    
    public int getHeight() {
        return GAUGE_HEIGHT;
    }
}
