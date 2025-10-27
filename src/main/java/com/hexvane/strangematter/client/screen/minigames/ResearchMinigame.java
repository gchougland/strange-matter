package com.hexvane.strangematter.client.screen.minigames;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public abstract class ResearchMinigame {
    
    // Texture locations for stability indicators
    private static final ResourceLocation INDICATOR_GREEN = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/indicator_light_green.png");
    private static final ResourceLocation INDICATOR_RED = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/indicator_light_red.png");
    private static final ResourceLocation SHUTTER_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/minigame_shutter.png");
    
    public enum MinigameState {
        INACTIVE,    // Panel is closed/inactive
        STABLE,      // Minigame is in stable state
        UNSTABLE     // Minigame is in unstable state
    }
    
    protected final ResearchType researchType;
    protected MinigameState state;
    protected boolean isActive;
    
    // Shutter animation fields
    private float leftShutterOffset = 0.0f;  // 0.0 = closed, 1.0 = fully open
    private float rightShutterOffset = 0.0f; // 0.0 = closed, 1.0 = fully open
    private boolean shuttersAnimating = false;
    private int animationTicks = 0;
    private static final int ANIMATION_DURATION = 20; // 1 second at 20 TPS
    
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
        startShutterAnimation(true); // Open shutters
        onActivate();
    }
    
    /**
     * Called when the research ends (success or failure)
     */
    public void deactivate() {
        this.isActive = false;
        this.state = MinigameState.INACTIVE;
        startShutterAnimation(false); // Close shutters
        onDeactivate();
    }
    
    /**
     * Called every tick while the minigame is active
     */
    public void tick() {
        // Update shutter animation
        if (shuttersAnimating) {
            animationTicks++;
            float progress = (float) animationTicks / ANIMATION_DURATION;
            if (progress >= 1.0f) {
                progress = 1.0f;
                shuttersAnimating = false;
            }
            
            // Apply S-curve easing function for smooth animation
            float easedProgress = sCurveEasing(progress);
            
            if (isActive) {
                // Opening shutters
                leftShutterOffset = easedProgress;
                rightShutterOffset = easedProgress;
            } else {
                // Closing shutters
                leftShutterOffset = 1.0f - easedProgress;
                rightShutterOffset = 1.0f - easedProgress;
            }
        }
        
        if (isActive) {
            updateMinigame();
        }
    }
    
    /**
     * Render the minigame panel
     */
    public void render(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        // Always render title (outside scissor area so it's always visible)
        renderTitle(guiGraphics, x, y, width, height);
        
        // Always render stability indicator (it handles its own visibility) - render on top of everything
        renderStabilityIndicator(guiGraphics, x, y, width);
        
        // If shutters are closed or partially closed, render minigame content under scissor area
        if (leftShutterOffset < 1.0f || rightShutterOffset < 1.0f) {
            // Enable scissor area for minigame content (same as shutter area)
            guiGraphics.enableScissor(x + 3, y + 2, x + width - 6, y + height - 4);
            
            // Render minigame content under the scissor area (offset to match scissor area)
            if (isActive) {
                renderActive(guiGraphics, x + 3, y + 2, width - 6, height - 4, mouseX, mouseY);
            } else {
                renderInactive(guiGraphics, x + 3, y + 2, width - 6, height - 4);
            }
            
            // Render shutters on top of the minigame content (still within scissor area)
            renderShutters(guiGraphics, x+3, y+2, width-6, height-4);
            
            guiGraphics.disableScissor();
        } else {
            // Shutters are fully open, render minigame content normally without scissor
            if (isActive) {
                renderActive(guiGraphics, x, y, width, height, mouseX, mouseY);
            } else {
                renderInactive(guiGraphics, x, y, width, height);
            }
        }
    }
    
    /**
     * Handle mouse clicks within the minigame area
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (isActive && mouseX >= panelX && mouseX < panelX + panelWidth && 
            mouseY >= panelY && mouseY < panelY + panelHeight) {
            return handleClick((int)mouseX, (int)mouseY, button, panelX, panelY, panelWidth, panelHeight);
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
    protected abstract boolean handleClick(int mouseX, int mouseY, int button, int panelX, int panelY, int panelWidth, int panelHeight);
    
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
    
    public boolean isAnimating() {
        return shuttersAnimating;
    }
    
    protected void setState(MinigameState newState) {
        this.state = newState;
    }
    
    /**
     * Render the minigame title
     */
    private void renderTitle(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Get the title from the research type using proper language entry
        String title = researchType.getDisplayName();
        
        // Render title centered above the panel (not on it)
        guiGraphics.drawCenteredString(
            net.minecraft.client.Minecraft.getInstance().font, 
            title, 
            x + width / 2, 
            y - 10, // Above the panel
            0xFF3dc7c7 // Same color as Instability text
        );
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
    
    /**
     * Start shutter animation (open or close)
     */
    private void startShutterAnimation(boolean opening) {
        shuttersAnimating = true;
        animationTicks = 0;
        
        // Set initial shutter positions
        if (opening) {
            leftShutterOffset = 0.0f;
            rightShutterOffset = 0.0f;
        } else {
            leftShutterOffset = 1.0f;
            rightShutterOffset = 1.0f;
        }
    }
    
    /**
     * Render the animated shutters
     */
    private void renderShutters(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Calculate shutter positions based on animation progress
        int shutterWidth = width / 2; // Each shutter covers half the panel
        
        // Left shutter (slides left when opening)
        int leftShutterX = (int) (x - shutterWidth * leftShutterOffset);
        guiGraphics.blit(SHUTTER_TEXTURE, leftShutterX, y, 0, 0, shutterWidth, height, shutterWidth, height);
        
        // Right shutter (slides right when opening)
        int rightShutterX = (int) (x + width - shutterWidth * (1.0f - rightShutterOffset));
        guiGraphics.blit(SHUTTER_TEXTURE, rightShutterX, y, shutterWidth, 0, shutterWidth, height, shutterWidth, height);
    }
    
    /**
     * S-curve easing function for smooth animation
     * Uses a sigmoid function to create a natural S-curve
     */
    private float sCurveEasing(float t) {
        // Sigmoid function: f(t) = 1 / (1 + e^(-k(t-0.5)))
        // k controls the steepness of the curve (higher = steeper)
        float k = 12.0f; // Adjust this value to control curve steepness
        
        // Shift and scale t to [-6, 6] range for better sigmoid behavior
        float x = k * (t - 0.5f);
        
        // Clamp x to prevent overflow
        x = Math.max(-6.0f, Math.min(6.0f, x));
        
        // Calculate sigmoid
        float sigmoid = 1.0f / (1.0f + (float) Math.exp(-x));
        
        return sigmoid;
    }
}
