package com.hexvane.strangematter.client.screen.minigames;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Axis;

public class ShadowMinigame extends ResearchMinigame {
    
    // UI Constants
    private static final int PANEL_WIDTH = 40; // Panel width for the scene
    private static final int PANEL_HEIGHT = 40; // Panel height for the scene
    private static final int CUBE_SIZE = 12; // Size of the cube
    private static final int LIGHT_SIZE = 6; // Size of the light source indicator
    private static final int CONTROL_SIZE = 8; // Size of control buttons (smaller)
    private static final int CONTROL_SPACING = 6; // Spacing between controls
    
    // Light and shadow parameters
    private static final double MIN_LIGHT_DISTANCE = 20.0; // Minimum distance from cube
    private static final double MAX_LIGHT_DISTANCE = 50.0; // Maximum distance from cube
    private static final double LIGHT_ANGLE_RANGE = 120.0; // Degrees of side-to-side movement
    private static final double ALIGNMENT_THRESHOLD = 3.0; // How close shadows need to be for stability
    private static final int DRIFT_DELAY_TICKS = 600; // Ticks before drift starts
    
    // Textures
    private static final ResourceLocation CUBE_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/shadow_cube.png");
    private static final ResourceLocation LIGHT_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/shadow_light.png");
    private static final ResourceLocation LEFT_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/left_button.png");
    private static final ResourceLocation RIGHT_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/right_button.png");
    private static final ResourceLocation UP_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/left_button.png"); // Reuse left button
    private static final ResourceLocation DOWN_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/right_button.png"); // Reuse right button
    
    // Colors
    private static final int TARGET_SHADOW_COLOR = 0xFF404040; // Dark gray for target shadow
    private static final int BLOCK_SHADOW_COLOR = 0xFF202020; // Darker gray for block shadow
    private static final int LIGHT_COLOR = 0xFFFFD700; // Gold color for light source
    private static final int BACKGROUND_COLOR = 0xFF2A2A2A; // Dark background for the panel
    private static final int BORDER_COLOR = 0xFF3dc7c7; // Same color as instability text
    
    // Game state
    private double lightDistance = 20.0; // Distance from cube to light source
    private double lightAngle = 0.0; // Angle of light source (0 = directly right, 90 = directly up)
    private double targetShadowAngle = 0.0; // Angle of the target shadow
    private double targetShadowLength = 15.0; // Length of the target shadow
    
    // Stability tracking
    private boolean isStable = false;
    private int stableTicks = 0;
    private boolean isDrifting = false;
    private int driftTicks = 0;
    private double lightDrift = 0.0;
    
    // Button states
    private boolean leftButtonHovered = false;
    private boolean rightButtonHovered = false;
    private boolean upButtonHovered = false;
    private boolean downButtonHovered = false;
    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;
    private boolean upButtonPressed = false;
    private boolean downButtonPressed = false;
    
    // Button press cooldown
    private int buttonCooldown = 0;
    private static final int BUTTON_COOLDOWN_TICKS = 5; // Prevent rapid clicking
    
    private Minecraft minecraft;
    
    public ShadowMinigame() {
        super(ResearchType.SHADOW);
        this.minecraft = Minecraft.getInstance();
    }
    
    @Override
    public void activate() {
        super.activate();
        
        // Reset all values
        lightDistance = 20.0;
        lightAngle = 0.0;
        isStable = false;
        stableTicks = 0;
        isDrifting = false;
        driftTicks = 0;
        lightDrift = 0.0;
        leftButtonHovered = false;
        rightButtonHovered = false;
        upButtonHovered = false;
        downButtonHovered = false;
        leftButtonPressed = false;
        rightButtonPressed = false;
        upButtonPressed = false;
        downButtonPressed = false;
        buttonCooldown = 0;
        
        // Randomize target shadow (on opposite side of light range)
        targetShadowAngle = (Math.random() - 0.5) * LIGHT_ANGLE_RANGE + 180.0; // Random angle on opposite side
        // Generate target shadow length within achievable range
        // Minimum achievable shadow length: (50 - 50 + 20) * 0.8 = 16
        // Maximum achievable shadow length: (50 - 20 + 20) * 0.8 = 40
        double minAchievableLength = (MAX_LIGHT_DISTANCE - MAX_LIGHT_DISTANCE + MIN_LIGHT_DISTANCE) * 0.8;
        double maxAchievableLength = (MAX_LIGHT_DISTANCE - MIN_LIGHT_DISTANCE + MIN_LIGHT_DISTANCE) * 0.8;
        targetShadowLength = minAchievableLength + Math.random() * (maxAchievableLength - minAchievableLength);
        
        // Randomize initial light position
        lightAngle = (Math.random() - 0.5) * LIGHT_ANGLE_RANGE;
        lightDistance = MIN_LIGHT_DISTANCE + Math.random() * (MAX_LIGHT_DISTANCE - MIN_LIGHT_DISTANCE);
        
        // Start in unstable state
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
        upButtonPressed = false;
        downButtonPressed = false;
    }
    
    @Override
    protected void onDeactivate() {
        // Reset button states
        leftButtonPressed = false;
        rightButtonPressed = false;
        upButtonPressed = false;
        downButtonPressed = false;
        leftButtonHovered = false;
        rightButtonHovered = false;
        upButtonHovered = false;
        downButtonHovered = false;
    }
    
    @Override
    protected boolean handleClick(int mouseX, int mouseY, int button, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (!isActive) return false;
        
        // Calculate control positions (keyboard arrow layout)
        int panelX_center = panelX + panelWidth / 2;
        int panelY_center = panelY + panelHeight / 2;
        
        // Position controls in keyboard arrow layout
        // First row: [][UP][]
        // Second row: [Left][Down][Right]
        int centerX = panelX + panelWidth / 2; // Use actual panel width, not PANEL_WIDTH constant
        int centerY = panelY + panelHeight / 2; // Use actual panel height, not PANEL_HEIGHT constant
        
        // Position buttons at the center bottom of the panel
        int buttonRowY = panelY + panelHeight - 15; // 15 pixels from bottom of panel
        
        // Up button (top center) - right above down button
        int upButtonX = centerX - CONTROL_SIZE / 2;
        int upButtonY = buttonRowY - CONTROL_SIZE - 2;
        
        // Down button (bottom center) - perfectly centered horizontally
        int downButtonX = centerX - CONTROL_SIZE / 2;
        int downButtonY = buttonRowY;
        
        // Left button (bottom left) - at down button's side
        int leftButtonX = centerX - CONTROL_SIZE - CONTROL_SPACING;
        int leftButtonY = buttonRowY;
        
        // Right button (bottom right) - at down button's side
        int rightButtonX = centerX + CONTROL_SPACING;
        int rightButtonY = buttonRowY;
        
        if (button == 0 && buttonCooldown <= 0) { // Left mouse button
            // Left button (rotate light left)
            if (mouseX >= leftButtonX && mouseX < leftButtonX + CONTROL_SIZE &&
                mouseY >= leftButtonY && mouseY < leftButtonY + CONTROL_SIZE) {
                
                leftButtonPressed = true;
                adjustLightAngle(-5.0); // Rotate 5 degrees left
                buttonCooldown = BUTTON_COOLDOWN_TICKS;
                
                // Play click sound
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(StrangeMatterSounds.RESEARCH_MACHINE_NOTE_INSERT.get(), 0.3f, 1.0f);
                }
                return true;
            }
            
            // Right button (rotate light right)
            if (mouseX >= rightButtonX && mouseX < rightButtonX + CONTROL_SIZE &&
                mouseY >= rightButtonY && mouseY < rightButtonY + CONTROL_SIZE) {
                
                rightButtonPressed = true;
                adjustLightAngle(5.0); // Rotate 5 degrees right
                buttonCooldown = BUTTON_COOLDOWN_TICKS;
                
                // Play click sound
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(StrangeMatterSounds.RESEARCH_MACHINE_NOTE_INSERT.get(), 0.3f, 1.0f);
                }
                return true;
            }
            
            // Up button (move light closer)
            if (mouseX >= upButtonX && mouseX < upButtonX + CONTROL_SIZE &&
                mouseY >= upButtonY && mouseY < upButtonY + CONTROL_SIZE) {
                
                upButtonPressed = true;
                adjustLightDistance(-2.0); // Move closer
                buttonCooldown = BUTTON_COOLDOWN_TICKS;
                
                // Play click sound
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(StrangeMatterSounds.RESEARCH_MACHINE_NOTE_INSERT.get(), 0.3f, 1.0f);
                }
                return true;
            }
            
            // Down button (move light further)
            if (mouseX >= downButtonX && mouseX < downButtonX + CONTROL_SIZE &&
                mouseY >= downButtonY && mouseY < downButtonY + CONTROL_SIZE) {
                
                downButtonPressed = true;
                adjustLightDistance(2.0); // Move further
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
            upButtonPressed = false;
            downButtonPressed = false;
        }
    }
    
    public void updateHoverStates(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (!isActive) return;
        
        // Calculate control positions (keyboard arrow layout)
        int panelX_center = panelX + panelWidth / 2;
        int panelY_center = panelY + panelHeight / 2;
        
        // Position controls in keyboard arrow layout
        // First row: [][UP][]
        // Second row: [Left][Down][Right]
        int centerX = panelX + panelWidth / 2; // Use actual panel width, not PANEL_WIDTH constant
        int centerY = panelY + panelHeight / 2; // Use actual panel height, not PANEL_HEIGHT constant
        
        // Position buttons at the center bottom of the panel
        int buttonRowY = panelY + panelHeight - 15; // 15 pixels from bottom of panel
        
        // Up button (top center) - right above down button
        int upButtonX = centerX - CONTROL_SIZE / 2;
        int upButtonY = buttonRowY - CONTROL_SIZE - 2;
        
        // Down button (bottom center) - perfectly centered horizontally
        int downButtonX = centerX - CONTROL_SIZE / 2;
        int downButtonY = buttonRowY;
        
        // Left button (bottom left) - at down button's side
        int leftButtonX = centerX - CONTROL_SIZE - CONTROL_SPACING;
        int leftButtonY = buttonRowY;
        
        // Right button (bottom right) - at down button's side
        int rightButtonX = centerX + CONTROL_SPACING;
        int rightButtonY = buttonRowY;
        
        // Update button hover states
        leftButtonHovered = (mouseX >= leftButtonX && mouseX < leftButtonX + CONTROL_SIZE &&
                            mouseY >= leftButtonY && mouseY < leftButtonY + CONTROL_SIZE);
        rightButtonHovered = (mouseX >= rightButtonX && mouseX < rightButtonX + CONTROL_SIZE &&
                             mouseY >= rightButtonY && mouseY < rightButtonY + CONTROL_SIZE);
        upButtonHovered = (mouseX >= upButtonX && mouseX < upButtonX + CONTROL_SIZE &&
                          mouseY >= upButtonY && mouseY < upButtonY + CONTROL_SIZE);
        downButtonHovered = (mouseX >= downButtonX && mouseX < downButtonX + CONTROL_SIZE &&
                            mouseY >= downButtonY && mouseY < downButtonY + CONTROL_SIZE);
    }
    
    @Override
    public void updateMinigame() {
        if (!isActive) return;
        
        // Update button cooldown
        if (buttonCooldown > 0) {
            buttonCooldown--;
        }
        
        // Check stability
        checkStability();
        
        // Update drift
        updateDrift();
    }
    
    private void checkStability() {
        // Calculate current block shadow properties
        double blockShadowAngle = lightAngle + 180.0; // Shadow is opposite to light
        double blockShadowLength = calculateShadowLength();
        
        // Check if block shadow aligns with target shadow
        double angleDiff = Math.abs(blockShadowAngle - targetShadowAngle);
        double lengthDiff = Math.abs(blockShadowLength - targetShadowLength);
        
        // Normalize angle difference (handle 360-degree wraparound)
        if (angleDiff > 180.0) {
            angleDiff = 360.0 - angleDiff;
        }
        
        boolean shadowAligned = angleDiff < ALIGNMENT_THRESHOLD && lengthDiff < ALIGNMENT_THRESHOLD;
        
        if (shadowAligned) {
            if (!isStable) {
                isStable = true;
                stableTicks = 0;
                setState(MinigameState.STABLE);
            } else {
                stableTicks++;
            }
        } else {
            isStable = false;
            stableTicks = 0;
            setState(MinigameState.UNSTABLE);
        }
    }
    
    private void updateDrift() {
        if (isStable && stableTicks >= DRIFT_DELAY_TICKS) {
            if (!isDrifting) {
                isDrifting = true;
                driftTicks = 0;
            } else {
                driftTicks++;
                // Add small random drift to light position
                double driftSpeed = 0.5;
                lightDrift += (Math.random() - 0.5) * driftSpeed;
                lightDrift = Math.max(-2.0, Math.min(2.0, lightDrift)); // Clamp drift
                lightAngle += lightDrift;
                lightAngle = Math.max(-LIGHT_ANGLE_RANGE/2, Math.min(LIGHT_ANGLE_RANGE/2, lightAngle));
            }
        } else {
            isDrifting = false;
            lightDrift = 0.0;
        }
    }
    
    private double calculateShadowLength() {
        // Shadow length is inversely proportional to light distance
        // Closer light = shorter shadow, further light = longer shadow
        return (MAX_LIGHT_DISTANCE - lightDistance + MIN_LIGHT_DISTANCE) * 0.8;
    }
    
    private void adjustLightAngle(double adjustment) {
        lightAngle += adjustment;
        lightAngle = Math.max(-LIGHT_ANGLE_RANGE/2, Math.min(LIGHT_ANGLE_RANGE/2, lightAngle));
        
        // Reset drift when actively adjusting
        isDrifting = false;
        lightDrift = 0.0;
        driftTicks = 0;
    }
    
    private void adjustLightDistance(double adjustment) {
        lightDistance += adjustment;
        lightDistance = Math.max(MIN_LIGHT_DISTANCE, Math.min(MAX_LIGHT_DISTANCE, lightDistance));
        
        // Reset drift when actively adjusting
        isDrifting = false;
        lightDrift = 0.0;
        driftTicks = 0;
    }
    
    @Override
    public void renderActive(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        // Calculate panel position
        int panelX = x + width / 2 - PANEL_WIDTH / 2;
        int panelY = y + height / 2 - PANEL_HEIGHT / 2 - 10; // Above center
        
        // Render the shadow scene
        renderShadowScene(guiGraphics, panelX, panelY);
        
        // Render controls
        renderControls(guiGraphics, x, y, width, height);
    }
    
    @Override
    public void renderInactive(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Don't render anything when inactive - the shutters will be handled by the base class
        // This prevents the minigame from showing through the shutters
    }
    
    private void renderShadowScene(GuiGraphics guiGraphics, int panelX, int panelY) {
        int centerX = panelX + PANEL_WIDTH / 2;
        int centerY = panelY + PANEL_HEIGHT / 2;
        
        // Render background square
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, BACKGROUND_COLOR);
        
        // Render border around background
        guiGraphics.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelY, BORDER_COLOR); // Top border
        guiGraphics.fill(panelX - 1, panelY + PANEL_HEIGHT, panelX + PANEL_WIDTH + 1, panelY + PANEL_HEIGHT + 1, BORDER_COLOR); // Bottom border
        guiGraphics.fill(panelX - 1, panelY - 1, panelX, panelY + PANEL_HEIGHT + 1, BORDER_COLOR); // Left border
        guiGraphics.fill(panelX + PANEL_WIDTH, panelY - 1, panelX + PANEL_WIDTH + 1, panelY + PANEL_HEIGHT + 1, BORDER_COLOR); // Right border
        
        // Set scissor area to clip shadows within the border
        guiGraphics.enableScissor(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT);
        
        // Render target shadow (fake shadow using block texture) - behind cube
        renderFakeShadow(guiGraphics, centerX, centerY, targetShadowAngle, targetShadowLength, 0xFF404040); // Dark grey
        
        // Render block shadow (fake shadow using block texture) - behind cube
        double blockShadowAngle = lightAngle + 180.0; // Shadow is opposite to light
        double blockShadowLength = calculateShadowLength();
        renderFakeShadow(guiGraphics, centerX, centerY, blockShadowAngle, blockShadowLength, 0xFF000000); // Black
        
        // Render cube (with texture) - on top of shadows
        int cubeX = centerX - CUBE_SIZE / 2;
        int cubeY = centerY - CUBE_SIZE / 2;
        guiGraphics.blit(CUBE_TEXTURE, cubeX, cubeY, 0, 0, CUBE_SIZE, CUBE_SIZE, CUBE_SIZE, CUBE_SIZE);
        
        // Render light source (with texture)
        double lightRad = Math.toRadians(lightAngle);
        int lightX = centerX + (int) (Math.cos(lightRad) * lightDistance * 0.3); // Scale for display
        int lightY = centerY + (int) (Math.sin(lightRad) * lightDistance * 0.3);
        guiGraphics.blit(LIGHT_TEXTURE, lightX - LIGHT_SIZE/2, lightY - LIGHT_SIZE/2, 0, 0, LIGHT_SIZE, LIGHT_SIZE, LIGHT_SIZE, LIGHT_SIZE);
        
        // Disable scissor area
        guiGraphics.disableScissor();
    }
    
    private void renderFakeShadow(GuiGraphics guiGraphics, int centerX, int centerY, double angle, double length, int tintColor) {
        // Convert angle to radians
        double rad = Math.toRadians(angle);
        
        // Calculate shadow dimensions (stretched based on length)
        int shadowWidth = CUBE_SIZE + (int) (length * 0.3); // Stretch width based on length
        int shadowHeight = CUBE_SIZE + (int) (length * 0.2); // Stretch height based on length
        
        // Calculate shadow position - stem from the block edge, not center
        // The shadow should start from the edge of the cube closest to the light
        int shadowX = centerX - shadowWidth / 2 + (int) (Math.cos(rad) * length * 0.4);
        int shadowY = centerY - shadowHeight / 2 + (int) (Math.sin(rad) * length * 0.4);
        
        // Apply tint color to the texture
        guiGraphics.setColor(
            ((tintColor >> 16) & 0xFF) / 255.0f,
            ((tintColor >> 8) & 0xFF) / 255.0f,
            (tintColor & 0xFF) / 255.0f,
            0.7f // Semi-transparent
        );
        
        // Apply rotation transformation for the shadow
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(shadowX + shadowWidth / 2.0, shadowY + shadowHeight / 2.0, 0);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees((float) angle));
        guiGraphics.pose().translate(-shadowWidth / 2.0, -shadowHeight / 2.0, 0);
        
        // Render the block texture as a stretched, rotated shadow
        guiGraphics.blit(CUBE_TEXTURE, 0, 0, 0, 0, shadowWidth, shadowHeight, shadowWidth, shadowHeight);
        
        guiGraphics.pose().popPose();
        
        // Reset color
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    private void renderControls(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int panelX_center = x + width / 2;
        int panelY_center = y + height / 2;
        
        // Position controls in keyboard arrow layout
        // First row: [][UP][]
        // Second row: [Left][Down][Right]
        int centerX = x + width / 2; // Use actual panel width, not PANEL_WIDTH constant
        int centerY = y + height / 2; // Use actual panel height, not PANEL_HEIGHT constant
        
        // Position buttons at the center bottom of the panel
        int buttonRowY = y + height - 15; // 15 pixels from bottom of panel
        
        // Up button (top center) - right above down button
        int upButtonX = centerX - CONTROL_SIZE / 2;
        int upButtonY = buttonRowY - CONTROL_SIZE - 2;
        
        // Down button (bottom center) - perfectly centered horizontally
        int downButtonX = centerX - CONTROL_SIZE / 2;
        int downButtonY = buttonRowY;
        
        // Left button (bottom left) - at down button's side
        int leftButtonX = centerX - CONTROL_SIZE - CONTROL_SPACING;
        int leftButtonY = buttonRowY;
        
        // Right button (bottom right) - at down button's side
        int rightButtonX = centerX + CONTROL_SPACING;
        int rightButtonY = buttonRowY;
        
        // Render up button (move light closer) - top center
        renderRotatedButton(guiGraphics, LEFT_BUTTON_TEXTURE, upButtonX, upButtonY, 
                           upButtonPressed, upButtonHovered, 90); // 90 degrees clockwise
        
        // Render left button (rotate light left) - bottom left
        renderButton(guiGraphics, LEFT_BUTTON_TEXTURE, leftButtonX, leftButtonY, 
                    leftButtonPressed, leftButtonHovered);
        
        // Render down button (move light further) - bottom center
        renderRotatedButton(guiGraphics, RIGHT_BUTTON_TEXTURE, downButtonX, downButtonY, 
                           downButtonPressed, downButtonHovered, 90); // 90 degrees clockwise
        
        // Render right button (rotate light right) - bottom right
        renderButton(guiGraphics, RIGHT_BUTTON_TEXTURE, rightButtonX, rightButtonY, 
                    rightButtonPressed, rightButtonHovered);
        
        // Render hover highlights (same as Energy minigame - AFTER button textures)
        if (upButtonHovered) {
            guiGraphics.fill(upButtonX - 1, upButtonY - 1, upButtonX + CONTROL_SIZE + 1, upButtonY + CONTROL_SIZE + 1, 0x80FFFFFF);
        }
        if (leftButtonHovered) {
            guiGraphics.fill(leftButtonX - 1, leftButtonY - 1, leftButtonX + CONTROL_SIZE + 1, leftButtonY + CONTROL_SIZE + 1, 0x80FFFFFF);
        }
        if (downButtonHovered) {
            guiGraphics.fill(downButtonX - 1, downButtonY - 1, downButtonX + CONTROL_SIZE + 1, downButtonY + CONTROL_SIZE + 1, 0x80FFFFFF);
        }
        if (rightButtonHovered) {
            guiGraphics.fill(rightButtonX - 1, rightButtonY - 1, rightButtonX + CONTROL_SIZE + 1, rightButtonY + CONTROL_SIZE + 1, 0x80FFFFFF);
        }
    }
    
    private void renderButton(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, 
                             boolean pressed, boolean hovered) {
        // Render button texture
        guiGraphics.blit(texture, x, y, 0, 0, CONTROL_SIZE, CONTROL_SIZE, CONTROL_SIZE, CONTROL_SIZE);
        
        // Render pressed state (dark overlay)
        if (pressed) {
            guiGraphics.fill(x, y, x + CONTROL_SIZE, y + CONTROL_SIZE, 0x80000000);
        }
    }
    
    private void renderRotatedButton(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, 
                                   boolean pressed, boolean hovered, int rotationDegrees) {
        // Apply rotation transformation
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + CONTROL_SIZE / 2.0, y + CONTROL_SIZE / 2.0, 0);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(rotationDegrees));
        guiGraphics.pose().translate(-CONTROL_SIZE / 2.0, -CONTROL_SIZE / 2.0, 0);
        
        // Render the texture
        guiGraphics.blit(texture, 0, 0, 0, 0, CONTROL_SIZE, CONTROL_SIZE, CONTROL_SIZE, CONTROL_SIZE);
        
        guiGraphics.pose().popPose();
        
        // Render pressed state (dark overlay)
        if (pressed) {
            guiGraphics.fill(x, y, x + CONTROL_SIZE, y + CONTROL_SIZE, 0x80000000);
        }
    }
    
    // Getters and setters for state saving/loading
    public double getLightDistance() { return lightDistance; }
    public void setLightDistance(double lightDistance) { this.lightDistance = lightDistance; }
    
    public double getLightAngle() { return lightAngle; }
    public void setLightAngle(double lightAngle) { this.lightAngle = lightAngle; }
    
    public double getTargetShadowAngle() { return targetShadowAngle; }
    public void setTargetShadowAngle(double targetShadowAngle) { this.targetShadowAngle = targetShadowAngle; }
    
    public double getTargetShadowLength() { return targetShadowLength; }
    public void setTargetShadowLength(double targetShadowLength) { this.targetShadowLength = targetShadowLength; }
    
    public boolean isStable() { return isStable; }
    public void setStable(boolean stable) { isStable = stable; }
    
    public int getStableTicks() { return stableTicks; }
    public void setStableTicks(int stableTicks) { this.stableTicks = stableTicks; }
    
    public boolean isDrifting() { return isDrifting; }
    public void setDrifting(boolean drifting) { isDrifting = drifting; }
    
    public int getDriftTicks() { return driftTicks; }
    public void setDriftTicks(int driftTicks) { this.driftTicks = driftTicks; }
    
    public double getLightDrift() { return lightDrift; }
    public void setLightDrift(double lightDrift) { this.lightDrift = lightDrift; }
    
    public int getButtonCooldown() { return buttonCooldown; }
    public void setButtonCooldown(int buttonCooldown) { this.buttonCooldown = buttonCooldown; }
}
