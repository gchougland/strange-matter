package com.hexvane.strangematter.client.screen.minigames;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public abstract class ResearchMinigame {
    
    // Texture locations for stability indicators
    private static final ResourceLocation INDICATOR_GREEN = new ResourceLocation(StrangeMatterMod.MODID, "textures/ui/indicator_light_green.png");
    private static final ResourceLocation INDICATOR_RED = new ResourceLocation(StrangeMatterMod.MODID, "textures/ui/indicator_light_red.png");
    
    public enum MinigameState {
        INACTIVE,    // Panel is closed/inactive
        STABLE,      // Minigame is in stable state
        UNSTABLE     // Minigame is in unstable state
    }
    
    protected final ResearchType researchType;
    protected MinigameState state;
    protected boolean isActive;
    
    public ResearchMinigame(ResearchType researchType) {
        this.researchType = researchType;
        this.state = MinigameState.INACTIVE;
        this.isActive = false;
    }
    
    /**
     * Called when the research begins and this minigame should become active
     */
    public void activate() {
        this.isActive = true;
        this.state = MinigameState.STABLE;
        onActivate();
    }
    
    /**
     * Called when the research ends (success or failure)
     */
    public void deactivate() {
        this.isActive = false;
        this.state = MinigameState.INACTIVE;
        onDeactivate();
    }
    
    /**
     * Called every tick while the minigame is active
     */
    public void tick() {
        if (isActive) {
            updateMinigame();
        }
    }
    
    /**
     * Render the minigame panel
     */
    public void render(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        if (isActive) {
            renderActive(guiGraphics, x, y, width, height, mouseX, mouseY);
        } else {
            renderInactive(guiGraphics, x, y, width, height);
        }
    }
    
    /**
     * Handle mouse clicks within the minigame area
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (isActive && mouseX >= panelX && mouseX < panelX + panelWidth && 
            mouseY >= panelY && mouseY < panelY + panelHeight) {
            return handleClick(mouseX - panelX, mouseY - panelY, button);
        }
        return false;
    }
    
    // Abstract methods to be implemented by specific minigames
    
    /**
     * Called when the minigame is activated
     */
    protected abstract void onActivate();
    
    /**
     * Called when the minigame is deactivated
     */
    protected abstract void onDeactivate();
    
    /**
     * Called every tick to update the minigame state
     */
    protected abstract void updateMinigame();
    
    /**
     * Render the minigame when it's active
     */
    protected abstract void renderActive(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY);
    
    /**
     * Render the minigame when it's inactive
     */
    protected abstract void renderInactive(GuiGraphics guiGraphics, int x, int y, int width, int height);
    
    /**
     * Handle mouse clicks within the minigame
     */
    protected abstract boolean handleClick(int relativeX, int relativeY, int button);
    
    // Getters
    
    public ResearchType getResearchType() {
        return researchType;
    }
    
    public MinigameState getState() {
        return state;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public boolean isStable() {
        return state == MinigameState.STABLE;
    }
    
    public boolean isUnstable() {
        return state == MinigameState.UNSTABLE;
    }
    
    protected void setState(MinigameState newState) {
        this.state = newState;
    }
    
    /**
     * Render a stability indicator in the top right corner of the minigame panel
     */
    protected void renderStabilityIndicator(GuiGraphics guiGraphics, int x, int y, int width) {
        // Position indicator in top right corner
        int indicatorX = x + width - 8;
        int indicatorY = y + 2;
        
        // Only show indicator when active
        if (isActive) {
            ResourceLocation indicatorTexture;
            if (state == MinigameState.STABLE) {
                indicatorTexture = INDICATOR_GREEN;
            } else {
                indicatorTexture = INDICATOR_RED;
            }
            
            // Draw the indicator texture (assuming 6x6 pixel texture)
            guiGraphics.blit(indicatorTexture, indicatorX, indicatorY, 0, 0, 6, 6, 6, 6);
        }
    }
}
