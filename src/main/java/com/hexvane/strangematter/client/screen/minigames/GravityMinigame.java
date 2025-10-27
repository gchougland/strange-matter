package com.hexvane.strangematter.client.screen.minigames;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import com.hexvane.strangematter.client.screen.components.BubblingParticleSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import com.hexvane.strangematter.StrangeMatterMod;

public class GravityMinigame extends ResearchMinigame {
    
    private final Minecraft minecraft;
    
    // Textures
    private static final ResourceLocation GRAVITY_TUBE_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/gravity_tube.png");
    private static final ResourceLocation GRAVITY_CUBE_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/gravity_cube.png");
    private static final ResourceLocation SLIDER_BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/slider_bar.png");
    private static final ResourceLocation SLIDER_HANDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/slider_handle.png");
    
    // UI Constants
    private static final int TUBE_WIDTH = 19;
    private static final int TUBE_HEIGHT = 43;
    private static final int CUBE_WIDTH = 12;
    private static final int CUBE_HEIGHT = 15;
    private static final int SLIDER_WIDTH = 34;
    private static final int SLIDER_HEIGHT = 3;
    private static final int SLIDER_HANDLE_WIDTH = 4;
    private static final int SLIDER_HANDLE_HEIGHT = 8;
    
    // Physics parameters
    private int targetGravity = 0; // The target gravity value (-5 to 5, never 0)
    private int sliderValue = 0; // The slider value (-5 to 5, center is 0)
    private double cubePosition = 0.5; // Position in tube (0.0 = bottom, 1.0 = top)
    private double cubeVelocity = 0.0; // Current velocity of the cube
    
    // Gravity limits
    private static final int MIN_GRAVITY = -5;
    private static final int MAX_GRAVITY = 5;
    private static final int SLIDER_NOTCHES = 11; // -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5
    
    // Equilibrium detection
    private static final double CENTER_POSITION = 0.5; // Center of the tube
    
    // Config-driven getters
    private double getBalanceThreshold() {
        return com.hexvane.strangematter.Config.gravityBalanceThreshold;
    }
    
    private int getDriftDelayTicks() {
        return com.hexvane.strangematter.Config.gravityDriftDelayTicks;
    }
    
    // Stability tracking
    private boolean isInEquilibrium = false;
    private int equilibriumTicks = 0;
    private int requiredEquilibriumTicks = 100; // 5 seconds at 20 TPS
    private int driftTicks = 0;
    private boolean isDrifting = false;
    
    // Drift parameters
    private boolean needsNewTarget = false; // Flag to indicate when to choose new target gravity
    
    // Control states
    private boolean sliderActive = false;
    
    // Particle system for tube effect
    private BubblingParticleSystem particleSystem;
    
    public GravityMinigame() {
        super(ResearchType.GRAVITY);
        this.minecraft = Minecraft.getInstance();
    }
    
    @Override
    protected void onActivate() {
        // Reset state when activated
        isInEquilibrium = false;
        equilibriumTicks = 0;
        driftTicks = 0;
        isDrifting = false;
        needsNewTarget = false;
        
        // Randomize target gravity (between -5 and 5, but never 0)
        do {
            targetGravity = MIN_GRAVITY + (int)(Math.random() * (MAX_GRAVITY - MIN_GRAVITY + 1));
        } while (targetGravity == 0);
        
        // Ensure target gravity is properly constrained
        targetGravity = Math.max(MIN_GRAVITY, Math.min(MAX_GRAVITY, targetGravity));
        
        // Set initial values - slider starts at center (0)
        sliderValue = 0;
        cubePosition = 0.5;
        cubeVelocity = 0.0;
        
        // Start in unstable state since slider won't match target initially
        setState(MinigameState.UNSTABLE);
        
        // Initialize particle system (will be positioned properly in render method)
        particleSystem = null; // Will be created in render method with proper coordinates
    }
    
    @Override
    protected void onDeactivate() {
        // Reset slider when deactivated
        sliderActive = false;
    }
    
    private boolean checkEquilibrium() {
        // Check if cube is in the center zone (between the purple lines)
        double threshold = getBalanceThreshold();
        return Math.abs(cubePosition - CENTER_POSITION) < threshold;
    }
    
    private void updatePhysics() {
        // Calculate net force: target gravity + slider value (opposite for balance)
        // When balanced: targetGravity + sliderValue = 0
        int netForce = targetGravity + sliderValue;
        
        if (netForce == 0) {
            // Perfectly balanced - cube should move toward center and stay there
            double centerForce = (CENTER_POSITION - cubePosition) * 0.1; // Force toward center
            cubeVelocity += centerForce;
            cubeVelocity *= 0.9; // Stronger damping when balanced
        } else {
            // Not balanced - apply gravity force
            double acceleration = netForce * 0.01; // Scale down for reasonable movement
            cubeVelocity += acceleration;
            cubeVelocity *= 0.95; // Normal damping
        }
        
        // Update position
        cubePosition += cubeVelocity;
        
        // Clamp position to tube bounds
        cubePosition = Math.max(0.0, Math.min(1.0, cubePosition));
        
        // Stop velocity at bounds
        if (cubePosition <= 0.0 || cubePosition >= 1.0) {
            cubeVelocity = 0.0;
        }
    }
    
    private void updateDrift() {
        if (needsNewTarget) {
            // Choose a new random target gravity (between -5 and 5, but never 0)
            do {
                targetGravity = MIN_GRAVITY + (int)(Math.random() * (MAX_GRAVITY - MIN_GRAVITY + 1));
            } while (targetGravity == 0);
            
            // Ensure target gravity is properly constrained
            targetGravity = Math.max(MIN_GRAVITY, Math.min(MAX_GRAVITY, targetGravity));
            
            needsNewTarget = false;
            isDrifting = false;
            driftTicks = 0;
        }
    }
    
    @Override
    protected void renderActive(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        // Render the gravity tube and cube
        renderGravityTube(guiGraphics, x + width / 2 - TUBE_WIDTH / 2, y + 10, TUBE_WIDTH, TUBE_HEIGHT);
        
        // Render the force slider (positioned lower to avoid overlap)
        renderForceSlider(guiGraphics, x + width / 2 - SLIDER_WIDTH / 2, y + height - 8, SLIDER_WIDTH, SLIDER_HEIGHT, mouseX, mouseY);
    }
    
    @Override
    protected void renderInactive(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Title is rendered by base class, no need to render here
        // Don't render interactive elements when inactive
    }
    
    private void renderGravityTube(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Initialize particle system if needed
        if (particleSystem == null) {
            particleSystem = BubblingParticleSystem.createGravityTubeEffect(x+2, y+20, width-10, height-15);
        }
        
        // Render the tube background
        guiGraphics.blit(GRAVITY_TUBE_TEXTURE, x, y, 0, 0, width, height, width, height);

        // Update and render particles
        particleSystem.update();
        particleSystem.render(guiGraphics);
        
        // Render equilibrium notches (purple lines)
        int centerY = y + height / 2;
        int notchWidth = 4;
        int notchHeight = 2;
        
        // Top notch
        guiGraphics.fill(x - notchWidth, centerY - 10, x + width + notchWidth, centerY - 10 + notchHeight, 0xFF800080);
        // Bottom notch
        guiGraphics.fill(x - notchWidth, centerY + 10, x + width + notchWidth, centerY + 10 + notchHeight, 0xFF800080);
        
        // Render the cube at its current position
        int cubeY = y + (int) ((1.0 - cubePosition) * (height - CUBE_HEIGHT));
        guiGraphics.blit(GRAVITY_CUBE_TEXTURE, x + (width - CUBE_WIDTH) / 2, cubeY, 0, 0, CUBE_WIDTH, CUBE_HEIGHT, CUBE_WIDTH, CUBE_HEIGHT);
    }
    
    private void renderForceSlider(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        // Render slider bar
        guiGraphics.blit(SLIDER_BAR_TEXTURE, x, y, 0, 0, width, height, width, height);
        
        // Render notches for each position (-5 to 5)
        for (int i = 0; i < SLIDER_NOTCHES; i++) {
            int notchX = x + (int) ((i / (double)(SLIDER_NOTCHES - 1)) * (width - 1));
            int notchY = y - 2;
            int notchHeight = 4;
            guiGraphics.fill(notchX, notchY, notchX + 1, notchY + notchHeight, 0xFF666666);
        }
        
        // Calculate handle position (sliderValue from -5 to 5 maps to 0 to width)
        double normalizedValue = (sliderValue - MIN_GRAVITY) / (double)(MAX_GRAVITY - MIN_GRAVITY);
        int handleX = x + (int) (normalizedValue * (width - SLIDER_HANDLE_WIDTH));
        int handleY = y - (SLIDER_HANDLE_HEIGHT - height) / 2;
        
        // Check if mouse is over handle for highlighting
        boolean handleHovered = (mouseX >= handleX && mouseX < handleX + SLIDER_HANDLE_WIDTH &&
                                mouseY >= handleY && mouseY < handleY + SLIDER_HANDLE_HEIGHT);
        
        // Render slider handle with highlighting
        if (sliderActive) {
            // Render active handle (being dragged) - brightest with stronger glow
            guiGraphics.blit(SLIDER_HANDLE_TEXTURE, handleX, handleY, 0, 0, SLIDER_HANDLE_WIDTH, SLIDER_HANDLE_HEIGHT, SLIDER_HANDLE_WIDTH, SLIDER_HANDLE_HEIGHT);
            // Add a strong glow effect for active state
            guiGraphics.fill(handleX - 2, handleY - 2, handleX + SLIDER_HANDLE_WIDTH + 2, handleY + SLIDER_HANDLE_HEIGHT + 2, 0x80FFFFFF);
        } else if (handleHovered) {
            // Render highlighted handle (hovered) - medium brightness
            guiGraphics.blit(SLIDER_HANDLE_TEXTURE, handleX, handleY, 0, 0, SLIDER_HANDLE_WIDTH, SLIDER_HANDLE_HEIGHT, SLIDER_HANDLE_WIDTH, SLIDER_HANDLE_HEIGHT);
            // Add a subtle glow effect
            guiGraphics.fill(handleX - 1, handleY - 1, handleX + SLIDER_HANDLE_WIDTH + 1, handleY + SLIDER_HANDLE_HEIGHT + 1, 0x40FFFFFF);
        } else {
            // Render normal handle
            guiGraphics.blit(SLIDER_HANDLE_TEXTURE, handleX, handleY, 0, 0, SLIDER_HANDLE_WIDTH, SLIDER_HANDLE_HEIGHT, SLIDER_HANDLE_WIDTH, SLIDER_HANDLE_HEIGHT);
        }
        
        // Render "Force" label with smaller size and same color as instability gauge
        String label = "Force";
        int labelWidth = minecraft.font.width(label);
        guiGraphics.drawString(minecraft.font, label, x + (width - labelWidth) / 2, y - 12, 0xFF3dc7c7); // Same color as instability gauge
    }
    
    @Override
    protected void updateMinigame() {
        updatePhysics();
        updateDrift();
        
        // Check for equilibrium
        boolean wasInEquilibrium = isInEquilibrium;
        isInEquilibrium = checkEquilibrium();
        
        if (isInEquilibrium) {
            if (!wasInEquilibrium) {
                // Just reached equilibrium, reset drift timer
                driftTicks = 0;
                isDrifting = false;
            }
            
            equilibriumTicks++;
            if (equilibriumTicks >= requiredEquilibriumTicks) {
                // Been in equilibrium long enough, start drift timer
                driftTicks++;
                int driftDelay = getDriftDelayTicks();
                if (driftTicks >= driftDelay && !isDrifting) {
                    isDrifting = true;
                    needsNewTarget = true; // Flag to choose new target gravity
                }
            }
        } else {
            // Not in equilibrium, reset counters
            equilibriumTicks = 0;
            driftTicks = 0;
            isDrifting = false;
        }
        
        // Update state based on equilibrium
        if (isInEquilibrium && equilibriumTicks >= requiredEquilibriumTicks) {
            this.state = MinigameState.STABLE;
        } else {
            this.state = MinigameState.UNSTABLE;
        }
    }
    
    @Override
    protected boolean handleClick(int mouseX, int mouseY, int button, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (!isActive) return false;
        
        // Check if clicking on slider (including handle area)
        int sliderX = panelX + panelWidth / 2 - SLIDER_WIDTH / 2;
        int sliderY = panelY + panelHeight - 8;
        
        // Expand click area to include the handle height above and below the bar
        int clickAreaY = sliderY - (SLIDER_HANDLE_HEIGHT - SLIDER_HEIGHT) / 2;
        int clickAreaHeight = SLIDER_HANDLE_HEIGHT;
        
        if (mouseX >= sliderX && mouseX < sliderX + SLIDER_WIDTH &&
            mouseY >= clickAreaY && mouseY < clickAreaY + clickAreaHeight) {
            
            sliderActive = true;
            
            // Immediately update slider value to match click position
            double relativeX = (mouseX - sliderX) / (double) SLIDER_WIDTH;
            relativeX = Math.max(0.0, Math.min(1.0, relativeX)); // Clamp to slider bounds
            
            // Convert to discrete slider value (-5 to 5) with snap to nearest notch
            double normalizedValue = relativeX * (SLIDER_NOTCHES - 1);
            int nearestNotch = (int) Math.round(normalizedValue);
            sliderValue = MIN_GRAVITY + nearestNotch;
            
            // Clamp to valid range
            sliderValue = Math.max(MIN_GRAVITY, Math.min(MAX_GRAVITY, sliderValue));
            
            // Reset drift when actively adjusting
            isDrifting = false;
            driftTicks = 0;
            
            // Play click sound
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(StrangeMatterSounds.RESEARCH_MACHINE_NOTE_INSERT.get(), 0.3f, 1.0f);
            }
            return true;
        }
        
        return false;
    }
    
    public void updateHoverStates(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (!isActive) return;
        
        // Hover states are handled in the render method for visual feedback
    }
    
    // Handle mouse drag and release through the base class system
    public void handleMouseDrag(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (!isActive || !sliderActive) return;
        
        // Calculate slider position relative to panel
        int sliderX = panelX + panelWidth / 2 - SLIDER_WIDTH / 2;
        
        // Convert mouse position to slider value (0.0 to 1.0 across slider width)
        // Allow dragging slightly outside the slider bounds for better responsiveness
        double relativeX = (mouseX - sliderX) / (double) SLIDER_WIDTH;
        relativeX = Math.max(0.0, Math.min(1.0, relativeX)); // Clamp to slider bounds
        
        // Convert to discrete slider value (-5 to 5) with snap to nearest notch
        double normalizedValue = relativeX * (SLIDER_NOTCHES - 1);
        int nearestNotch = (int) Math.round(normalizedValue);
        sliderValue = MIN_GRAVITY + nearestNotch;
        
        // Clamp to valid range
        sliderValue = Math.max(MIN_GRAVITY, Math.min(MAX_GRAVITY, sliderValue));
        
        // Reset drift when actively adjusting
        isDrifting = false;
        driftTicks = 0;
    }
    
    public void handleMouseRelease(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight, int button) {
        if (!isActive) return;
        
        sliderActive = false;
    }
    
    @Override
    public boolean isStable() {
        return state == MinigameState.STABLE;
    }
    
    @Override
    public boolean isActive() {
        return isActive;
    }
    
    // Getter methods for state saving
    public boolean isSliderActive() {
        return sliderActive;
    }
    
    public int getSliderValue() {
        return sliderValue;
    }
    
    public int getTargetGravity() {
        return targetGravity;
    }
    
    public double getCubePosition() {
        return cubePosition;
    }
    
    public double getCubeVelocity() {
        return cubeVelocity;
    }
    
    public boolean isInEquilibrium() {
        return isInEquilibrium;
    }
    
    public int getEquilibriumTicks() {
        return equilibriumTicks;
    }
    
    public int getDriftTicks() {
        return driftTicks;
    }
    
    public boolean isDrifting() {
        return isDrifting;
    }
    
    public boolean getNeedsNewTarget() {
        return needsNewTarget;
    }
    
    public int getRequiredSliderValue() {
        // The slider value needed to balance the target gravity
        return -targetGravity;
    }
    
    // Setter methods for state restoration
    public void setSliderActive(boolean sliderActive) {
        this.sliderActive = sliderActive;
    }
    
    public void setSliderValue(int sliderValue) {
        this.sliderValue = sliderValue;
    }
    
    public void setTargetGravity(int targetGravity) {
        this.targetGravity = targetGravity;
    }
    
    public void setCubePosition(double cubePosition) {
        this.cubePosition = cubePosition;
    }
    
    public void setCubeVelocity(double cubeVelocity) {
        this.cubeVelocity = cubeVelocity;
    }
    
    public void setInEquilibrium(boolean isInEquilibrium) {
        this.isInEquilibrium = isInEquilibrium;
    }
    
    public void setEquilibriumTicks(int equilibriumTicks) {
        this.equilibriumTicks = equilibriumTicks;
    }
    
    public void setDriftTicks(int driftTicks) {
        this.driftTicks = driftTicks;
    }
    
    public void setDrifting(boolean isDrifting) {
        this.isDrifting = isDrifting;
    }
    
    public void setNeedsNewTarget(boolean needsNewTarget) {
        this.needsNewTarget = needsNewTarget;
    }
    
    public void setRequiredSliderValue(int requiredValue) {
        // This is a helper method - it sets the target gravity to the opposite of the required value
        targetGravity = -requiredValue;
    }
}
