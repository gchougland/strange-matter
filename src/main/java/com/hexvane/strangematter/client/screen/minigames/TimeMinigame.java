package com.hexvane.strangematter.client.screen.minigames;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class TimeMinigame extends ResearchMinigame {
    
    // UI Constants
    private static final int CLOCK_WIDTH = 34; // Clock width
    private static final int CLOCK_HEIGHT = 33; // Clock height
    private static final int MINUTE_HAND_LENGTH = 12;
    private static final int SECOND_HAND_LENGTH = 14;
    private static final int BUTTON_SIZE = 12;
    private static final int LIGHT_SIZE = 8; // Updated to 8x8
    
    // Colors
    private static final int MINUTE_HAND_COLOR = 0xFF39e8e1; // Cyan
    private static final int SECOND_HAND_COLOR = 0xFF9641ba; // Purple
    
    // Speed and timing
    private static final double MAX_SPEED = 2.0; // Maximum speed multiplier
    private static final double SPEED_ADJUSTMENT = 0.1; // How much speed changes per button press
    private static final double AUTO_SNAP_THRESHOLD = 0.15; // Auto-snap when within this range of target speed
    private static final int DRIFT_DELAY_TICKS = 200; // Ticks before drift starts (same as energy minigame)
    
    // Textures
    private static final ResourceLocation CLOCK_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/time_clock.png");
    private static final ResourceLocation LEFT_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/left_button.png");
    private static final ResourceLocation RIGHT_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/right_button.png");
    private static final ResourceLocation LIGHT_ON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/time_light_on.png");
    private static final ResourceLocation LIGHT_OFF_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/time_light_off.png");
    
    // Game state
    private double clockSpeed = 1.0; // Current speed multiplier (1.0 = normal speed)
    private double targetSpeed = 1.0; // Target speed to match (always 1.0 for blinking light)
    private double clockAngle = 0.0; // Current angle of the second hand (0-360 degrees)
    private double minuteAngle = 0.0; // Current angle of the minute hand (0-360 degrees)
    private int secondCounter = 0; // Counter for seconds (0-59)
    private int minuteCounter = 0; // Counter for minutes (0-59)
    
    // Light blinking
    private boolean lightOn = false;
    private int lightTick = 0;
    
    // Stability tracking
    private boolean isStable = false;
    private int stableTicks = 0;
    private boolean isDrifting = false;
    private int driftTicks = 0;
    private double speedDrift = 0.0;
    
    // Button states
    private boolean leftButtonHovered = false;
    private boolean rightButtonHovered = false;
    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;
    
    // Button press cooldown
    private int buttonCooldown = 0;
    private static final int BUTTON_COOLDOWN_TICKS = 5; // Prevent rapid clicking
    
    private Minecraft minecraft;
    
    public TimeMinigame() {
        super(ResearchType.TIME);
        this.minecraft = Minecraft.getInstance();
    }
    
    @Override
    public void activate() {
        super.activate();
        
        // Reset all values
        clockSpeed = 1.0;
        targetSpeed = 1.0;
        clockAngle = 0.0;
        minuteAngle = 0.0;
        secondCounter = 0;
        minuteCounter = 0;
        lightOn = false;
        lightTick = 0;
        isStable = false;
        stableTicks = 0;
        isDrifting = false;
        driftTicks = 0;
        speedDrift = 0.0;
        leftButtonHovered = false;
        rightButtonHovered = false;
        leftButtonPressed = false;
        rightButtonPressed = false;
        buttonCooldown = 0;
        
        // Randomize initial speed (either too fast or too slow)
        if (Math.random() < 0.5) {
            clockSpeed = 0.3 + Math.random() * 0.4; // 0.3 to 0.7 (slow)
        } else {
            clockSpeed = 1.3 + Math.random() * 0.4; // 1.3 to 1.7 (fast)
        }
        
        // Randomize starting rotation of minute hand (0-360 degrees)
        minuteAngle = Math.random() * 360.0;
        
        // Start in unstable state since speed won't match target initially
        setState(MinigameState.UNSTABLE);
    }
    
    @Override
    protected void onActivate() {
        // Additional activation logic if needed
    }
    
    @Override
    public void deactivate() {
        super.deactivate();
        leftButtonPressed = false;
        rightButtonPressed = false;
    }
    
    @Override
    protected void onDeactivate() {
        // Reset button states
        leftButtonPressed = false;
        rightButtonPressed = false;
        leftButtonHovered = false;
        rightButtonHovered = false;
    }
    
    @Override
    protected boolean handleClick(int mouseX, int mouseY, int button, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (!isActive) return false;
        
        // Check button clicks (buttons below clock)
        int leftButtonX = panelX + panelWidth / 2 - BUTTON_SIZE - 5;
        int rightButtonX = panelX + panelWidth / 2 + 5;
        int buttonY = panelY + panelHeight / 2 + CLOCK_HEIGHT / 2 + 5;
        
        if (button == 0 && buttonCooldown <= 0) { // Left mouse button
            if (mouseX >= leftButtonX && mouseX < leftButtonX + BUTTON_SIZE &&
                mouseY >= buttonY && mouseY < buttonY + BUTTON_SIZE) {
                
                leftButtonPressed = true;
                adjustSpeed(-SPEED_ADJUSTMENT);
                buttonCooldown = BUTTON_COOLDOWN_TICKS;
                
                // Play click sound
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(StrangeMatterSounds.RESEARCH_MACHINE_NOTE_INSERT.get(), 0.3f, 1.0f);
                }
                return true;
            }
            
            if (mouseX >= rightButtonX && mouseX < rightButtonX + BUTTON_SIZE &&
                mouseY >= buttonY && mouseY < buttonY + BUTTON_SIZE) {
                
                rightButtonPressed = true;
                adjustSpeed(SPEED_ADJUSTMENT);
                buttonCooldown = BUTTON_COOLDOWN_TICKS;
                
                // Play click sound
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(StrangeMatterSounds.RESEARCH_MACHINE_NOTE_INSERT.get(), 0.3f, 1.0f);
                }
                return true;
            }
        }
        
        return false;
    }
    
    public void handleMouseDrag(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        // Not used for this minigame
    }
    
    public void handleMouseRelease(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight, int button) {
        if (!isActive) return;
        
        if (button == 0) { // Left mouse button
            leftButtonPressed = false;
            rightButtonPressed = false;
        }
    }
    
    public void updateHoverStates(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (!isActive) return;
        
        // Update button hover states (buttons below clock)
        int leftButtonX = panelX + panelWidth / 2 - BUTTON_SIZE - 5;
        int rightButtonX = panelX + panelWidth / 2 + 5;
        int buttonY = panelY + panelHeight / 2 + CLOCK_HEIGHT / 2 + 5;
        
        leftButtonHovered = (mouseX >= leftButtonX && mouseX < leftButtonX + BUTTON_SIZE &&
                            mouseY >= buttonY && mouseY < buttonY + BUTTON_SIZE);
        rightButtonHovered = (mouseX >= rightButtonX && mouseX < rightButtonX + BUTTON_SIZE &&
                             mouseY >= buttonY && mouseY < buttonY + BUTTON_SIZE);
    }
    
    @Override
    public void updateMinigame() {
        if (!isActive) return;
        
        // Update button cooldown
        if (buttonCooldown > 0) {
            buttonCooldown--;
        }
        
        // Update clock
        updateClock();
        
        // Update light blinking
        updateLight();
        
        // Check stability
        checkStability();
        
        // Update drift
        updateDrift();
    }
    
    private void updateClock() {
        // Update clock angle based on speed
        double effectiveSpeed = clockSpeed + speedDrift;
        clockAngle += effectiveSpeed * 6.0; // 6 degrees per second at normal speed
        
        // Normalize angle
        while (clockAngle >= 360.0) {
            clockAngle -= 360.0;
            secondCounter++;
        }
        while (clockAngle < 0.0) {
            clockAngle += 360.0;
            secondCounter--;
        }
        
        // Update minute hand
        if (secondCounter >= 60) {
            secondCounter = 0;
            minuteCounter++;
            minuteAngle += 6.0; // 6 degrees per minute
        } else if (secondCounter < 0) {
            secondCounter = 59;
            minuteCounter--;
            minuteAngle -= 6.0;
        }
        
        // Normalize minute angle
        while (minuteAngle >= 360.0) minuteAngle -= 360.0;
        while (minuteAngle < 0.0) minuteAngle += 360.0;
    }
    
    private void updateLight() {
        lightTick++;
        
        // Light blinks once per second (20 ticks)
        if (lightTick >= 20) {
            lightTick = 0;
            lightOn = !lightOn;
        }
    }
    
    private void checkStability() {
        // Check if speed is close to target (1.0)
        boolean speedMatch = Math.abs(clockSpeed - targetSpeed) < AUTO_SNAP_THRESHOLD;
        
        if (speedMatch) {
            if (!isStable) {
                isStable = true;
                stableTicks = 0;
                setState(MinigameState.STABLE); // Set the minigame state to stable
            } else {
                stableTicks++;
            }
        } else {
            isStable = false;
            stableTicks = 0;
            setState(MinigameState.UNSTABLE); // Set the minigame state to unstable
        }
    }
    
    private void updateDrift() {
        if (isStable && stableTicks >= DRIFT_DELAY_TICKS) {
            if (!isDrifting) {
                isDrifting = true;
                driftTicks = 0;
            } else {
                driftTicks++;
                // Add small random drift
                double driftSpeed = 0.01;
                speedDrift += (Math.random() - 0.5) * driftSpeed;
                speedDrift = Math.max(-0.1, Math.min(0.1, speedDrift)); // Clamp drift
            }
        } else {
            isDrifting = false;
            speedDrift = 0.0;
        }
    }
    
    private void adjustSpeed(double adjustment) {
        clockSpeed += adjustment;
        clockSpeed = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, clockSpeed));
        
        // Reset drift when actively adjusting
        isDrifting = false;
        speedDrift = 0.0;
        driftTicks = 0;
    }
    
    @Override
    public void renderActive(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        // Render clock background
        int clockX = x + width / 2 - CLOCK_WIDTH / 2;
        int clockY = y + height / 2 - CLOCK_HEIGHT / 2;
        
        guiGraphics.blit(CLOCK_TEXTURE, clockX, clockY, 0, 0, CLOCK_WIDTH, CLOCK_HEIGHT, CLOCK_WIDTH, CLOCK_HEIGHT);
        
        // Render clock hands
        renderHands(guiGraphics, clockX + CLOCK_WIDTH / 2, clockY + CLOCK_HEIGHT / 2);
        
        // Render buttons
        renderButtons(guiGraphics, x, y, width, height);
        
        // Render light
        renderLight(guiGraphics, x, y, width, height);
    }
    
    @Override
    public void renderInactive(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Don't render anything when inactive - the shutters will be handled by the base class
        // This prevents the minigame from showing through the shutters
    }
    
    private void renderHands(GuiGraphics guiGraphics, int centerX, int centerY) {
        // Convert angles to radians
        double minuteRad = Math.toRadians(minuteAngle - 90); // -90 to start at 12 o'clock
        double secondRad = Math.toRadians(clockAngle - 90);
        
        // Calculate hand endpoints
        int minuteEndX = centerX + (int) (Math.cos(minuteRad) * MINUTE_HAND_LENGTH);
        int minuteEndY = centerY + (int) (Math.sin(minuteRad) * MINUTE_HAND_LENGTH);
        int secondEndX = centerX + (int) (Math.cos(secondRad) * SECOND_HAND_LENGTH);
        int secondEndY = centerY + (int) (Math.sin(secondRad) * SECOND_HAND_LENGTH);
        
        // Render minute hand (behind second hand) - draw line from center to endpoint
        drawLine(guiGraphics, centerX, centerY, minuteEndX, minuteEndY, MINUTE_HAND_COLOR);
        
        // Render second hand (on top) - draw line from center to endpoint
        drawLine(guiGraphics, centerX, centerY, secondEndX, secondEndY, SECOND_HAND_COLOR);
    }
    
    // Helper method to draw a line between two points
    private void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        
        int x = x1;
        int y = y1;
        
        while (true) {
            guiGraphics.fill(x, y, x + 1, y + 1, color);
            
            if (x == x2 && y == y2) break;
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }
    
    private void renderButtons(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int leftButtonX = x + width / 2 - BUTTON_SIZE - 5;
        int rightButtonX = x + width / 2 + 5;
        int buttonY = y + height / 2 + CLOCK_HEIGHT / 2 + 5;
        
        // Render left button
        if (leftButtonPressed) {
            guiGraphics.setColor(0.8f, 0.8f, 0.8f, 1.0f);
        } else if (leftButtonHovered) {
            guiGraphics.setColor(1.2f, 1.2f, 1.2f, 1.0f);
        } else {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        guiGraphics.blit(LEFT_BUTTON_TEXTURE, leftButtonX, buttonY, 0, 0, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        
        // Render right button
        if (rightButtonPressed) {
            guiGraphics.setColor(0.8f, 0.8f, 0.8f, 1.0f);
        } else if (rightButtonHovered) {
            guiGraphics.setColor(1.2f, 1.2f, 1.2f, 1.0f);
        } else {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        guiGraphics.blit(RIGHT_BUTTON_TEXTURE, rightButtonX, buttonY, 0, 0, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    private void renderLight(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int lightX = x + width / 2 - LIGHT_SIZE / 2;
        int lightY = y + height / 2 - CLOCK_HEIGHT / 2 - LIGHT_SIZE - 5;
        
        // Use proper light textures
        ResourceLocation lightTexture = lightOn ? LIGHT_ON_TEXTURE : LIGHT_OFF_TEXTURE;
        guiGraphics.blit(lightTexture, lightX, lightY, 0, 0, LIGHT_SIZE, LIGHT_SIZE, LIGHT_SIZE, LIGHT_SIZE);
    }
    
    // Getters and setters for state saving/loading
    public double getClockSpeed() { return clockSpeed; }
    public void setClockSpeed(double clockSpeed) { this.clockSpeed = clockSpeed; }
    
    public double getTargetSpeed() { return targetSpeed; }
    public void setTargetSpeed(double targetSpeed) { this.targetSpeed = targetSpeed; }
    
    public double getClockAngle() { return clockAngle; }
    public void setClockAngle(double clockAngle) { this.clockAngle = clockAngle; }
    
    public double getMinuteAngle() { return minuteAngle; }
    public void setMinuteAngle(double minuteAngle) { this.minuteAngle = minuteAngle; }
    
    public int getSecondCounter() { return secondCounter; }
    public void setSecondCounter(int secondCounter) { this.secondCounter = secondCounter; }
    
    public int getMinuteCounter() { return minuteCounter; }
    public void setMinuteCounter(int minuteCounter) { this.minuteCounter = minuteCounter; }
    
    public boolean isLightOn() { return lightOn; }
    public void setLightOn(boolean lightOn) { this.lightOn = lightOn; }
    
    public int getLightTick() { return lightTick; }
    public void setLightTick(int lightTick) { this.lightTick = lightTick; }
    
    public boolean isStable() { return isStable; }
    public void setStable(boolean stable) { isStable = stable; }
    
    public int getStableTicks() { return stableTicks; }
    public void setStableTicks(int stableTicks) { this.stableTicks = stableTicks; }
    
    public boolean isDrifting() { return isDrifting; }
    public void setDrifting(boolean drifting) { isDrifting = drifting; }
    
    public int getDriftTicks() { return driftTicks; }
    public void setDriftTicks(int driftTicks) { this.driftTicks = driftTicks; }
    
    public double getSpeedDrift() { return speedDrift; }
    public void setSpeedDrift(double speedDrift) { this.speedDrift = speedDrift; }
    
    public int getButtonCooldown() { return buttonCooldown; }
    public void setButtonCooldown(int buttonCooldown) { this.buttonCooldown = buttonCooldown; }
}
