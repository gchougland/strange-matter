package com.hexvane.strangematter.client.screen.minigames;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import com.hexvane.strangematter.StrangeMatterMod;

public class EnergyMinigame extends ResearchMinigame {
    
    private final Minecraft minecraft;
    
    // Textures
    private static final ResourceLocation DIAL_OFF_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/dial_off.png");
    private static final ResourceLocation DIAL_ON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/dial_on.png");
    private static final ResourceLocation RIGHT_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/right_button.png");
    private static final ResourceLocation LEFT_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/left_button.png");
    
    // UI Constants
    private static final int DIAL_SIZE = 10;
    private static final int BUTTON_SIZE = 10;
    private static final int WAVE_AREA_HEIGHT = 40;
    private static final int WAVE_AREA_Y_OFFSET = 10;
    
    // Wave parameters
    private double targetAmplitude = 1.0;
    private double targetPeriod = 2.0;
    private double currentAmplitude = 0.5;
    private double currentPeriod = 1.5;
    
    // Min/max limits for easier matching
    private static final double MIN_AMPLITUDE = 0.5;
    private static final double MAX_AMPLITUDE = 1.5;
    private static final double MIN_PERIOD = 1.0;
    private static final double MAX_PERIOD = 1.25; // Reduced range so only 5 clicks needed max
    
    // Control states
    private boolean amplitudeDialActive = false;
    private boolean periodDialActive = false;
    
    // Alignment tracking
    private boolean isAligned = false;
    private int alignmentTicks = 0;
    private int driftTicks = 0;
    
    // Config-driven getters
    private int getRequiredAlignmentTicks() {
        return com.hexvane.strangematter.Config.energyRequiredAlignmentTicks;
    }
    
    private int getDriftDelayTicks() {
        return com.hexvane.strangematter.Config.energyDriftDelayTicks;
    }
    
    private double getAmplitudeStep() {
        return com.hexvane.strangematter.Config.energyAmplitudeStep;
    }
    
    private double getPeriodStep() {
        return com.hexvane.strangematter.Config.energyPeriodStep;
    }
    
    // Drift parameters
    private double amplitudeDrift = 0.0;
    private double periodDrift = 0.0;
    private double amplitudeDriftTarget = 0.0;
    private double periodDriftTarget = 0.0;
    
    // Animation parameters
    private int animationTicks = 0;
    
    // Hover states
    private boolean amplitudeDialHovered = false;
    private boolean periodDialHovered = false;
    private boolean leftButtonHovered = false;
    private boolean rightButtonHovered = false;
    
    public EnergyMinigame() {
        super(ResearchType.ENERGY);
        this.minecraft = Minecraft.getInstance();
    }
    
    @Override
    protected void onActivate() {
        // Reset alignment state when activated
        isAligned = false;
        alignmentTicks = 0;
        driftTicks = 0;
        amplitudeDrift = 0.0;
        periodDrift = 0.0;
        amplitudeDriftTarget = 0.0;
        periodDriftTarget = 0.0;
        
        // Randomize target values within min/max bounds
        targetAmplitude = MIN_AMPLITUDE + Math.random() * (MAX_AMPLITUDE - MIN_AMPLITUDE);
        targetPeriod = MIN_PERIOD + Math.random() * (MAX_PERIOD - MIN_PERIOD);
        
        // Set initial values
        currentAmplitude = 0.5;
        currentPeriod = 1.5;
    }
    
    @Override
    protected void onDeactivate() {
        // Reset dials when deactivated
        amplitudeDialActive = false;
        periodDialActive = false;
    }
    
    private boolean checkAlignment() {
        double amplitudeDiff = Math.abs(currentAmplitude - targetAmplitude);
        double periodDiff = Math.abs(currentPeriod - targetPeriod);
        
        // Consider aligned if both amplitude and period are within 0.1 of target
        return amplitudeDiff < 0.1 && periodDiff < 0.1;
    }
    
    private void applyDrift() {
        // Set new drift targets randomly
        if (Math.random() < 0.1) { // 10% chance each tick to change targets
            amplitudeDriftTarget = (Math.random() - 0.5) * 0.1; // -0.05 to 0.05
            periodDriftTarget = (Math.random() - 0.5) * 0.1; // -0.05 to 0.05
        }
        
        // Gradually move current drift toward targets
        double driftSpeed = 0.005; // How fast drift changes
        if (amplitudeDrift < amplitudeDriftTarget) {
            amplitudeDrift = Math.min(amplitudeDriftTarget, amplitudeDrift + driftSpeed);
        } else if (amplitudeDrift > amplitudeDriftTarget) {
            amplitudeDrift = Math.max(amplitudeDriftTarget, amplitudeDrift - driftSpeed);
        }
        
        if (periodDrift < periodDriftTarget) {
            periodDrift = Math.min(periodDriftTarget, periodDrift + driftSpeed);
        } else if (periodDrift > periodDriftTarget) {
            periodDrift = Math.max(periodDriftTarget, periodDrift - driftSpeed);
        }
        
        // Apply drift to current values
        currentAmplitude += amplitudeDrift;
        currentPeriod += periodDrift;
        
        // Keep values in min/max bounds
        currentAmplitude = Math.max(MIN_AMPLITUDE, Math.min(MAX_AMPLITUDE, currentAmplitude));
        currentPeriod = Math.max(MIN_PERIOD, Math.min(MAX_PERIOD, currentPeriod));
    }
    
    @Override
    protected void renderActive(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        // Render sine waves in the top center (slightly wider)
        renderSineWaves(guiGraphics, x + width / 2 - 20, y + WAVE_AREA_Y_OFFSET, 40, WAVE_AREA_HEIGHT - 10);
        
        // Render controls at the bottom
        renderControls(guiGraphics, x, y, width, height);
    }
    
    @Override
    protected void renderInactive(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Title is now rendered by base class, no need to render here
        
        // Don't render interactive elements (sine waves and controls) when inactive
        // The stability indicator and title are handled by the base class
    }
    
    private void renderSineWaves(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Draw dark grey background rectangle
        guiGraphics.fill(x, y, x + width, y + height, 0xFF181B24);
        
        int centerY = y + height / 2;
        
        // Render target wave (blue)
        renderSineWave(guiGraphics, x, centerY, width, height, targetAmplitude, targetPeriod, 0xFF0000FF);
        
        // Render current wave (green)
        renderSineWave(guiGraphics, x, centerY, width, height, currentAmplitude, currentPeriod, 0xFF00FF00);
    }
    
    private void renderSineWave(GuiGraphics guiGraphics, int x, int centerY, int width, int height, 
                              double amplitude, double period, int color) {
        // Calculate animated offset based on time
        double timeOffset = (double) animationTicks * 0.1; // Slow animation
        
        for (int i = 0; i < width; i++) {
            double normalizedX = (double) i / width * Math.PI * 4; // 2 full cycles
            double normalizedY = Math.sin((normalizedX + timeOffset) / period) * amplitude;
            int pixelY = centerY - (int) (normalizedY * height / 4); // Scale to fit height
            
            // Draw thicker line segment (2 pixels wide) with proper gap filling
            guiGraphics.fill(x + i, pixelY, x + i + 1, pixelY + 2, color);
        }
    }
    
    private void renderControls(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int centerX = x + width / 2;
        
        // Dials positioned slightly lower
        int dialY = y + height - 30;
        
        // Amplitude dial (left side) - adjusted for 10x10 size
        int amplitudeDialX = centerX - 13;
        ResourceLocation amplitudeDialTexture = amplitudeDialActive ? DIAL_ON_TEXTURE : DIAL_OFF_TEXTURE;
        guiGraphics.blit(amplitudeDialTexture, amplitudeDialX, dialY, 0, 0, DIAL_SIZE, DIAL_SIZE, DIAL_SIZE, DIAL_SIZE);
        
        // Amplitude dial hover highlight
        if (amplitudeDialHovered) {
            guiGraphics.fill(amplitudeDialX - 1, dialY - 1, amplitudeDialX + DIAL_SIZE + 1, dialY + DIAL_SIZE + 1, 0x80FFFFFF);
        }
        
        // Period dial (right side) - adjusted for 10x10 size
        int periodDialX = centerX + 3;
        ResourceLocation periodDialTexture = periodDialActive ? DIAL_ON_TEXTURE : DIAL_OFF_TEXTURE;
        guiGraphics.blit(periodDialTexture, periodDialX, dialY, 0, 0, DIAL_SIZE, DIAL_SIZE, DIAL_SIZE, DIAL_SIZE);
        
        // Period dial hover highlight
        if (periodDialHovered) {
            guiGraphics.fill(periodDialX - 1, dialY - 1, periodDialX + DIAL_SIZE + 1, dialY + DIAL_SIZE + 1, 0x80FFFFFF);
        }
        
        // Arrow buttons (center, further apart, below dials) - adjusted for 10x10 size
        int buttonY = y + height - 15;
        int leftButtonX = centerX - 13;
        int rightButtonX = centerX + 3;
        
        // Check if buttons should be disabled
        boolean leftDisabled = (amplitudeDialActive && isAmplitudeAtMin()) || (periodDialActive && isPeriodAtMin());
        boolean rightDisabled = (amplitudeDialActive && isAmplitudeAtMax()) || (periodDialActive && isPeriodAtMax());
        
        // Render buttons with disabled state
        if (leftDisabled) {
            guiGraphics.blit(LEFT_BUTTON_TEXTURE, leftButtonX, buttonY, 0, 0, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
            guiGraphics.fill(leftButtonX, buttonY, leftButtonX + BUTTON_SIZE, buttonY + BUTTON_SIZE, 0x80000000);
        } else {
            guiGraphics.blit(LEFT_BUTTON_TEXTURE, leftButtonX, buttonY, 0, 0, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        }
        
        if (rightDisabled) {
            guiGraphics.blit(RIGHT_BUTTON_TEXTURE, rightButtonX, buttonY, 0, 0, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
            guiGraphics.fill(rightButtonX, buttonY, rightButtonX + BUTTON_SIZE, buttonY + BUTTON_SIZE, 0x80000000);
        } else {
            guiGraphics.blit(RIGHT_BUTTON_TEXTURE, rightButtonX, buttonY, 0, 0, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        }
        
        // Button hover highlights (only when not disabled)
        if (leftButtonHovered && !leftDisabled) {
            guiGraphics.fill(leftButtonX - 1, buttonY - 1, leftButtonX + BUTTON_SIZE + 1, buttonY + BUTTON_SIZE + 1, 0x80FFFFFF);
        }
        if (rightButtonHovered && !rightDisabled) {
            guiGraphics.fill(rightButtonX - 1, buttonY - 1, rightButtonX + BUTTON_SIZE + 1, buttonY + BUTTON_SIZE + 1, 0x80FFFFFF);
        }
        
        // Labels (smaller text, same color as Instability gauge, slightly lower)
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.7f, 0.7f, 1.0f);
        guiGraphics.drawCenteredString(minecraft.font, "AMP", 
            (int)((amplitudeDialX + DIAL_SIZE / 2) / 0.7f), (int)((dialY - 5) / 0.7f), 0xFF3dc7c7);
        guiGraphics.drawCenteredString(minecraft.font, "PER", 
            (int)((periodDialX + DIAL_SIZE / 2) / 0.7f), (int)((dialY - 5) / 0.7f), 0xFF3dc7c7);
        guiGraphics.pose().popPose();
    }
    
    @Override
    protected boolean handleClick(int mouseX, int mouseY, int button, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (!isActive) return false;
        
        // Use absolute coordinates like research nodes do - much more reliable!
        // mouseX and mouseY are absolute screen coordinates, just like research nodes
        
        // Calculate absolute positions of controls using panel position + relative offsets
        int centerX = panelX + panelWidth / 2;
        
        // Dials positioned slightly lower - match renderControls exactly
        int dialY = panelY + panelHeight - 32;
        int amplitudeDialX = centerX - 13;
        int periodDialX = centerX + 3;
        
        // Check amplitude dial click using absolute coordinates like research nodes
        if (mouseX >= amplitudeDialX && mouseX <= amplitudeDialX + DIAL_SIZE && 
            mouseY >= dialY && mouseY <= dialY + DIAL_SIZE) {
            amplitudeDialActive = !amplitudeDialActive;
            // Play dial toggle sound
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(StrangeMatterSounds.ENERGY_DIAL_TOGGLE.get(), 0.6f, 1.0f);
            }
            return true;
        }
        
        // Check period dial click using absolute coordinates like research nodes
        if (mouseX >= periodDialX && mouseX <= periodDialX + DIAL_SIZE && 
            mouseY >= dialY && mouseY <= dialY + DIAL_SIZE) {
            periodDialActive = !periodDialActive;
            // Play dial toggle sound
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(StrangeMatterSounds.ENERGY_DIAL_TOGGLE.get(), 0.6f, 1.0f);
            }
            return true;
        }
        
        // Arrow buttons - match renderControls exactly
        int buttonY = panelY + panelHeight - 15;
        int leftButtonX = centerX - 13;
        int rightButtonX = centerX + 3;
        
        // Check if buttons should be disabled
        boolean leftDisabled = (amplitudeDialActive && isAmplitudeAtMin()) || (periodDialActive && isPeriodAtMin());
        boolean rightDisabled = (amplitudeDialActive && isAmplitudeAtMax()) || (periodDialActive && isPeriodAtMax());
        
        if (mouseX >= leftButtonX && mouseX <= leftButtonX + BUTTON_SIZE && 
            mouseY >= buttonY && mouseY <= buttonY + BUTTON_SIZE && !leftDisabled) {
            // Left arrow - decrease
            adjustValues(-1);
            // Play button click sound
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(StrangeMatterSounds.ENERGY_BUTTON_CLICK.get(), 0.5f, 1.0f);
            }
            return true;
        } else if (mouseX >= rightButtonX && mouseX <= rightButtonX + BUTTON_SIZE && 
                   mouseY >= buttonY && mouseY <= buttonY + BUTTON_SIZE && !rightDisabled) {
            // Right arrow - increase
            adjustValues(1);
            // Play button click sound
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(StrangeMatterSounds.ENERGY_BUTTON_CLICK.get(), 0.5f, 1.0f);
            }
            return true;
        }
        
        return false;
    }
    
    private void adjustValues(int direction) {
        if (amplitudeDialActive) {
            double step = getAmplitudeStep();
            currentAmplitude += direction * step;
            currentAmplitude = Math.max(MIN_AMPLITUDE, Math.min(MAX_AMPLITUDE, currentAmplitude));
            
            // Auto-match if close to target
            if (Math.abs(currentAmplitude - targetAmplitude) < step * 1.6) {
                currentAmplitude = targetAmplitude;
            }
        }
        
        if (periodDialActive) {
            double step = getPeriodStep();
            currentPeriod += direction * step;
            currentPeriod = Math.max(MIN_PERIOD, Math.min(MAX_PERIOD, currentPeriod));
            
            // Auto-match if close to target
            if (Math.abs(currentPeriod - targetPeriod) < step * 1.6) {
                currentPeriod = targetPeriod;
            }
        }
    }
    
    private boolean isAmplitudeAtMin() {
        return currentAmplitude <= MIN_AMPLITUDE + 0.001;
    }
    
    private boolean isAmplitudeAtMax() {
        return currentAmplitude >= MAX_AMPLITUDE - 0.001;
    }
    
    private boolean isPeriodAtMin() {
        return currentPeriod <= MIN_PERIOD + 0.001;
    }
    
    private boolean isPeriodAtMax() {
        return currentPeriod >= MAX_PERIOD - 0.001;
    }
    
    @Override
    protected void updateMinigame() {
        if (!isActive) return;
        
        // Increment animation ticks for wave animation
        animationTicks++;
        
        // Check if waves are aligned
        boolean currentlyAligned = checkAlignment();
        
        if (currentlyAligned) {
            if (!isAligned) {
                // Just became aligned
                isAligned = true;
                alignmentTicks = 0;
                driftTicks = 0; // Reset drift countdown when becoming aligned
                // Play wave align sound
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(StrangeMatterSounds.ENERGY_WAVE_ALIGN.get(), 0.4f, 1.0f);
                }
            } else {
                alignmentTicks++;
                int requiredTicks = getRequiredAlignmentTicks();
                if (alignmentTicks >= requiredTicks) {
                    // Been aligned long enough, start drifting
                    driftTicks++;
                    int driftDelay = getDriftDelayTicks();
                    if (driftTicks >= driftDelay) {
                        applyDrift();
                    }
                    setState(MinigameState.STABLE);
                } else {
                    setState(MinigameState.UNSTABLE);
                }
            }
        } else {
            // Not aligned, reset alignment tracking
            if (isAligned) {
                // Just became misaligned
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(StrangeMatterSounds.ENERGY_WAVE_MISALIGN.get(), 0.3f, 1.0f);
                }
            }
            isAligned = false;
            alignmentTicks = 0;
            driftTicks = 0;
            amplitudeDrift = 0.0;
            periodDrift = 0.0;
            setState(MinigameState.UNSTABLE);
        }
    }
    
    @Override
    protected void setState(MinigameState newState) {
        MinigameState oldState = this.state;
        super.setState(newState);
        
        // Play state change sounds
        if (oldState != newState && minecraft != null && minecraft.player != null) {
            switch (newState) {
                case STABLE:
                    minecraft.player.playSound(StrangeMatterSounds.MINIGAME_STABLE.get(), 0.4f, 1.0f);
                    break;
                case UNSTABLE:
                    minecraft.player.playSound(StrangeMatterSounds.MINIGAME_UNSTABLE.get(), 0.3f, 1.0f);
                    break;
            }
        }
    }
    
    public void updateHoverStates(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        // Use absolute coordinates like research nodes do - much more reliable!
        // Calculate absolute positions of controls using panel position + relative offsets
        int centerX = panelX + panelWidth / 2;
        
        // Dials positioned slightly lower - match renderControls exactly
        int dialY = panelY + panelHeight - 32;
        int amplitudeDialX = centerX - 13;
        int periodDialX = centerX + 3;
        
        // Check dial hover states using absolute coordinates like research nodes
        amplitudeDialHovered = mouseX >= amplitudeDialX && mouseX <= amplitudeDialX + DIAL_SIZE && 
                              mouseY >= dialY && mouseY <= dialY + DIAL_SIZE;
        periodDialHovered = mouseX >= periodDialX && mouseX <= periodDialX + DIAL_SIZE && 
                           mouseY >= dialY && mouseY <= dialY + DIAL_SIZE;
        
        // Arrow buttons - match renderControls exactly
        int buttonY = panelY + panelHeight - 15;
        int leftButtonX = centerX - 13;
        int rightButtonX = centerX + 3;
        
        // Check button hover states using absolute coordinates like research nodes
        leftButtonHovered = mouseX >= leftButtonX && mouseX <= leftButtonX + BUTTON_SIZE && 
                           mouseY >= buttonY && mouseY <= buttonY + BUTTON_SIZE;
        rightButtonHovered = mouseX >= rightButtonX && mouseX <= rightButtonX + BUTTON_SIZE && 
                            mouseY >= buttonY && mouseY <= buttonY + BUTTON_SIZE;
    }
    
    // State management methods for persistence
    public boolean isAmplitudeDialActive() { return amplitudeDialActive; }
    public boolean isPeriodDialActive() { return periodDialActive; }
    public double getCurrentAmplitude() { return currentAmplitude; }
    public double getCurrentPeriod() { return currentPeriod; }
    public double getTargetAmplitude() { return targetAmplitude; }
    public double getTargetPeriod() { return targetPeriod; }
    
    public void setAmplitudeDialActive(boolean active) { this.amplitudeDialActive = active; }
    public void setPeriodDialActive(boolean active) { this.periodDialActive = active; }
    public void setCurrentAmplitude(double amplitude) { this.currentAmplitude = amplitude; }
    public void setCurrentPeriod(double period) { this.currentPeriod = period; }
    public void setTargetAmplitude(double amplitude) { this.targetAmplitude = amplitude; }
    public void setTargetPeriod(double period) { this.targetPeriod = period; }
}
