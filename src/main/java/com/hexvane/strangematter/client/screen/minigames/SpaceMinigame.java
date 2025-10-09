package com.hexvane.strangematter.client.screen.minigames;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class SpaceMinigame extends ResearchMinigame {
    
    // UI Constants
    private static final int IMAGE_WIDTH = 32; // Warped image width
    private static final int IMAGE_HEIGHT = 32; // Warped image height
    private static final int CONTROL_SIZE = 12; // Size of control buttons/sliders
    private static final int CONTROL_SPACING = 5; // Spacing between controls
    
    // Warp parameters
    private static final double MAX_WARP = 1.0; // Maximum warp amount
    
    // Config-driven getters
    private double getWarpAdjustment() {
        return com.hexvane.strangematter.Config.spaceWarpAdjustment;
    }
    
    private double getStabilityThreshold() {
        return com.hexvane.strangematter.Config.spaceStabilityThreshold;
    }
    
    private int getDriftDelayTicks() {
        return com.hexvane.strangematter.Config.spaceDriftDelayTicks;
    }
    
    // Textures
    private static final ResourceLocation SPACE_IMAGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/space_image.png");
    private static final ResourceLocation LEFT_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/left_button.png");
    private static final ResourceLocation RIGHT_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/right_button.png");
    
    // Game state
    private double warpAmount = 1.0; // Current warp amount (1.0 = fully warped, 0.0 = clear)
    private double targetWarp = 0.0; // Target warp amount (always 0.0 for clear image)
    private double warpX = 0.0; // X-axis warp offset
    private double warpY = 0.0; // Y-axis warp offset
    
    // Stability tracking
    private boolean isStable = false;
    private int stableTicks = 0;
    private boolean isDrifting = false;
    private int driftTicks = 0;
    private double warpDrift = 0.0;
    
    // Button states
    private boolean leftButtonHovered = false;
    private boolean rightButtonHovered = false;
    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;
    
    // Button press cooldown
    private int buttonCooldown = 0;
    private static final int BUTTON_COOLDOWN_TICKS = 5; // Prevent rapid clicking
    
    private Minecraft minecraft;
    
    public SpaceMinigame() {
        super(ResearchType.SPACE);
        this.minecraft = Minecraft.getInstance();
    }
    
    @Override
    public void activate() {
        super.activate();
        
        // Reset all values
        warpAmount = 1.0; // Start fully warped
        targetWarp = 0.0; // Target is clear image
        warpX = 0.0;
        warpY = 0.0;
        isStable = false;
        stableTicks = 0;
        isDrifting = false;
        driftTicks = 0;
        warpDrift = 0.0;
        leftButtonHovered = false;
        rightButtonHovered = false;
        leftButtonPressed = false;
        rightButtonPressed = false;
        buttonCooldown = 0;
        
        // Randomize initial warp direction (left or right)
        if (Math.random() < 0.5) {
            warpAmount = 0.8 + Math.random() * 0.2; // 0.8 to 1.0 (fully warped)
        } else {
            warpAmount = 0.0 + Math.random() * 0.2; // 0.0 to 0.2 (mostly clear)
        }
        
        // Randomize initial warp offset
        warpX = (Math.random() - 0.5) * 0.5; // -0.25 to 0.25
        warpY = (Math.random() - 0.5) * 0.5; // -0.25 to 0.25
        
        // Start in unstable state since image is warped
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
        
        // Calculate control positions (below the image)
        int imageX = panelX + panelWidth / 2 - IMAGE_WIDTH / 2;
        int imageY = panelY + panelHeight / 2 - IMAGE_HEIGHT / 2 - 10; // Above center
        int controlsY = imageY + IMAGE_HEIGHT + 10; // Below image
        
        // Left/Right controls (warp amount)
        int leftButtonX = panelX + panelWidth / 2 - CONTROL_SIZE - CONTROL_SPACING;
        int rightButtonX = panelX + panelWidth / 2 + CONTROL_SPACING;
        
        
        if (button == 0 && buttonCooldown <= 0) { // Left mouse button
            // Left button (decrease warp)
            if (mouseX >= leftButtonX && mouseX < leftButtonX + CONTROL_SIZE &&
                mouseY >= controlsY && mouseY < controlsY + CONTROL_SIZE) {
                
                leftButtonPressed = true;
                adjustWarp(-1.0); // Direction multiplied by config step in adjustWarp()
                buttonCooldown = BUTTON_COOLDOWN_TICKS;
                
                // Play click sound
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(StrangeMatterSounds.RESEARCH_MACHINE_NOTE_INSERT.get(), 0.3f, 1.0f);
                }
                return true;
            }
            
            // Right button (increase warp)
            if (mouseX >= rightButtonX && mouseX < rightButtonX + CONTROL_SIZE &&
                mouseY >= controlsY && mouseY < controlsY + CONTROL_SIZE) {
                
                rightButtonPressed = true;
                adjustWarp(1.0); // Direction multiplied by config step in adjustWarp()
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
        
        // Calculate control positions
        int imageY = panelY + panelHeight / 2 - IMAGE_HEIGHT / 2 - 10;
        int controlsY = imageY + IMAGE_HEIGHT + 10;
        
        int leftButtonX = panelX + panelWidth / 2 - CONTROL_SIZE - CONTROL_SPACING;
        int rightButtonX = panelX + panelWidth / 2 + CONTROL_SPACING;
        // Update button hover states
        leftButtonHovered = (mouseX >= leftButtonX && mouseX < leftButtonX + CONTROL_SIZE &&
                            mouseY >= controlsY && mouseY < controlsY + CONTROL_SIZE);
        rightButtonHovered = (mouseX >= rightButtonX && mouseX < rightButtonX + CONTROL_SIZE &&
                             mouseY >= controlsY && mouseY < controlsY + CONTROL_SIZE);
    }
    
    @Override
    public void updateMinigame() {
        if (!isActive) return;
        
        // Update button cooldown
        if (buttonCooldown > 0) {
            buttonCooldown--;
        }
        
        // Add small random drift even when not stable (makes it more challenging)
        if (Math.random() < 0.01) { // 1% chance per tick
            double randomDrift = (Math.random() - 0.5) * 0.01; // Small random drift
            warpAmount = Math.max(0.0, Math.min(MAX_WARP, warpAmount + randomDrift));
        }
        
        // Check stability
        checkStability();
        
        // Update drift
        updateDrift();
    }
    
    private void checkStability() {
        // Check if warp is close to target (0.0)
        double threshold = getStabilityThreshold();
        boolean warpMatch = Math.abs(warpAmount - targetWarp) < threshold;
        
        if (warpMatch) {
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
        int driftDelay = getDriftDelayTicks();
        if (isStable && stableTicks >= driftDelay) {
            if (!isDrifting) {
                isDrifting = true;
                driftTicks = 0;
            } else {
                driftTicks++;
                // Add small random drift to warp amount (can go in either direction)
                double driftSpeed = 0.02; // Increased drift speed
                warpDrift += (Math.random() - 0.5) * driftSpeed;
                warpDrift = Math.max(-0.2, Math.min(0.2, warpDrift)); // Increased drift range
                warpAmount = Math.max(0.0, Math.min(MAX_WARP, warpAmount + warpDrift));
            }
        } else {
            isDrifting = false;
            warpDrift = 0.0;
        }
    }
    
    private void adjustWarp(double adjustment) {
        double step = getWarpAdjustment();
        warpAmount += adjustment * step;
        warpAmount = Math.max(0.0, Math.min(MAX_WARP, warpAmount));
        
        // Reset drift when actively adjusting
        isDrifting = false;
        warpDrift = 0.0;
        driftTicks = 0;
    }
    
    
    @Override
    public void renderActive(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        // Render warped image
        int imageX = x + width / 2 - IMAGE_WIDTH / 2;
        int imageY = y + height / 2 - IMAGE_HEIGHT / 2 - 10; // Above center
        
        // Render border around the image
        renderImageBorder(guiGraphics, imageX, imageY);
        
        // Set scissor area to clip the warped image within the border
        guiGraphics.enableScissor(imageX, imageY, imageX + IMAGE_WIDTH, imageY + IMAGE_HEIGHT);
        
        // Apply programmatic warp effect
        renderWarpedImage(guiGraphics, imageX, imageY);
        
        // Disable scissor area
        guiGraphics.disableScissor();
        
        // Render controls
        renderControls(guiGraphics, x, y, width, height);
    }
    
    private void renderImageBorder(GuiGraphics guiGraphics, int x, int y) {
        // Render a border around the image area
        int borderColor = 0xFF3dc7c7; // Same color as instability text
        
        // Top border
        guiGraphics.fill(x - 1, y - 1, x + IMAGE_WIDTH + 1, y, borderColor);
        // Bottom border
        guiGraphics.fill(x - 1, y + IMAGE_HEIGHT, x + IMAGE_WIDTH + 1, y + IMAGE_HEIGHT + 1, borderColor);
        // Left border
        guiGraphics.fill(x - 1, y - 1, x, y + IMAGE_HEIGHT + 1, borderColor);
        // Right border
        guiGraphics.fill(x + IMAGE_WIDTH, y - 1, x + IMAGE_WIDTH + 1, y + IMAGE_HEIGHT + 1, borderColor);
    }
    
    private void renderWarpedImage(GuiGraphics guiGraphics, int x, int y) {
        // Calculate warp parameters
        float warpFactor = (float) Math.max(0.0, Math.min(1.0, warpAmount));
        float centerX = x + IMAGE_WIDTH / 2.0f;
        float centerY = y + IMAGE_HEIGHT / 2.0f;
        
        // Create a mesh with 8x8 vertices (64 total)
        int meshSize = 8;
        float[][] meshX = new float[meshSize][meshSize];
        float[][] meshY = new float[meshSize][meshSize];
        
        // Initialize mesh vertices
        for (int i = 0; i < meshSize; i++) {
            for (int j = 0; j < meshSize; j++) {
                // Base position (normal grid)
                float baseX = x + (i * IMAGE_WIDTH) / (meshSize - 1);
                float baseY = y + (j * IMAGE_HEIGHT) / (meshSize - 1);
                
                // Calculate distance from center
                float distX = baseX - centerX;
                float distY = baseY - centerY;
                float distance = (float) Math.sqrt(distX * distX + distY * distY);
                float maxDistance = (float) Math.sqrt(IMAGE_WIDTH * IMAGE_WIDTH + IMAGE_HEIGHT * IMAGE_HEIGHT) / 2.0f;
                
                // Apply warp effect (spiral/vortex)
                float warpStrength = warpFactor * (1.0f - distance / maxDistance);
                float angle = (float) Math.atan2(distY, distX) + warpStrength * 3.0f; // Spiral effect
                float warpRadius = distance * (1.0f + warpStrength * 0.5f);
                
                // Calculate warped position
                meshX[i][j] = centerX + (float) Math.cos(angle) * warpRadius;
                meshY[i][j] = centerY + (float) Math.sin(angle) * warpRadius;
                
                // Add additional offset from controls
                meshX[i][j] += (float) this.warpX * warpFactor * 10.0f;
                meshY[i][j] += (float) this.warpY * warpFactor * 10.0f;
            }
        }
        
        // Render the mesh as triangles (quads divided into triangles)
        for (int i = 0; i < meshSize - 1; i++) {
            for (int j = 0; j < meshSize - 1; j++) {
                // Calculate UV coordinates for this quad
                float u1 = (float) i / (meshSize - 1);
                float v1 = (float) j / (meshSize - 1);
                float u2 = (float) (i + 1) / (meshSize - 1);
                float v2 = (float) (j + 1) / (meshSize - 1);
                
                // Get the four corner vertices
                float x1 = meshX[i][j];
                float y1 = meshY[i][j];
                float x2 = meshX[i + 1][j];
                float y2 = meshY[i + 1][j];
                float x3 = meshX[i + 1][j + 1];
                float y3 = meshY[i + 1][j + 1];
                float x4 = meshX[i][j + 1];
                float y4 = meshY[i][j + 1];
                
                // Render as two triangles to form a quad
                // Triangle 1: (x1,y1) -> (x2,y2) -> (x3,y3)
                renderTriangle(guiGraphics, x1, y1, x2, y2, x3, y3, u1, v1, u2, v1, u2, v2);
                // Triangle 2: (x1,y1) -> (x3,y3) -> (x4,y4)
                renderTriangle(guiGraphics, x1, y1, x3, y3, x4, y4, u1, v1, u2, v2, u1, v2);
            }
        }
    }
    
    private void renderTriangle(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, float x3, float y3,
                               float u1, float v1, float u2, float v2, float u3, float v3) {
        // For now, we'll render as a simple rectangle approximation
        // This is a simplified version - a full triangle renderer would be more complex
        float minX = Math.min(Math.min(x1, x2), x3);
        float maxX = Math.max(Math.max(x1, x2), x3);
        float minY = Math.min(Math.min(y1, y2), y3);
        float maxY = Math.max(Math.max(y1, y2), y3);
        
        // Calculate UV bounds
        float minU = Math.min(Math.min(u1, u2), u3);
        float maxU = Math.max(Math.max(u1, u2), u3);
        float minV = Math.min(Math.min(v1, v2), v3);
        float maxV = Math.max(Math.max(v1, v2), v3);
        
        // Render as a rectangle (simplified approach)
        int width = (int) (maxX - minX);
        int height = (int) (maxY - minY);
        if (width > 0 && height > 0) {
            guiGraphics.blit(SPACE_IMAGE_TEXTURE, 
                           (int) minX, (int) minY,
                           (int) (minU * IMAGE_WIDTH), (int) (minV * IMAGE_HEIGHT),
                           width, height,
                           IMAGE_WIDTH, IMAGE_HEIGHT);
        }
    }
    
    @Override
    public void renderInactive(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Don't render anything when inactive - the shutters will be handled by the base class
        // This prevents the minigame from showing through the shutters
    }
    
    private void renderControls(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int imageY = y + height / 2 - IMAGE_HEIGHT / 2 - 10;
        int controlsY = imageY + IMAGE_HEIGHT + 10;
        
        int leftButtonX = x + width / 2 - CONTROL_SIZE - CONTROL_SPACING;
        int rightButtonX = x + width / 2 + CONTROL_SPACING;
        // Render left button (decrease warp)
        renderButton(guiGraphics, LEFT_BUTTON_TEXTURE, leftButtonX, controlsY, 
                    leftButtonPressed, leftButtonHovered);
        
        // Render right button (increase warp)
        renderButton(guiGraphics, RIGHT_BUTTON_TEXTURE, rightButtonX, controlsY, 
                    rightButtonPressed, rightButtonHovered);
    }
    
    private void renderButton(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, 
                             boolean pressed, boolean hovered) {
        if (pressed) {
            guiGraphics.setColor(0.8f, 0.8f, 0.8f, 1.0f);
        } else if (hovered) {
            guiGraphics.setColor(1.2f, 1.2f, 1.2f, 1.0f);
        } else {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        guiGraphics.blit(texture, x, y, 0, 0, CONTROL_SIZE, CONTROL_SIZE, CONTROL_SIZE, CONTROL_SIZE);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    
    // Getters and setters for state saving/loading
    public double getWarpAmount() { return warpAmount; }
    public void setWarpAmount(double warpAmount) { this.warpAmount = warpAmount; }
    
    public double getTargetWarp() { return targetWarp; }
    public void setTargetWarp(double targetWarp) { this.targetWarp = targetWarp; }
    
    public double getWarpX() { return warpX; }
    public void setWarpX(double warpX) { this.warpX = warpX; }
    
    public double getWarpY() { return warpY; }
    public void setWarpY(double warpY) { this.warpY = warpY; }
    
    public boolean isStable() { return isStable; }
    public void setStable(boolean stable) { isStable = stable; }
    
    public int getStableTicks() { return stableTicks; }
    public void setStableTicks(int stableTicks) { this.stableTicks = stableTicks; }
    
    public boolean isDrifting() { return isDrifting; }
    public void setDrifting(boolean drifting) { isDrifting = drifting; }
    
    public int getDriftTicks() { return driftTicks; }
    public void setDriftTicks(int driftTicks) { this.driftTicks = driftTicks; }
    
    public double getWarpDrift() { return warpDrift; }
    public void setWarpDrift(double warpDrift) { this.warpDrift = warpDrift; }
    
    public int getButtonCooldown() { return buttonCooldown; }
    public void setButtonCooldown(int buttonCooldown) { this.buttonCooldown = buttonCooldown; }
}
