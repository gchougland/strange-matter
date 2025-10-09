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
    private double targetForce = 0.0; // The force that needs to be offset
    private double sliderForce = 0.0; // The force applied by the slider
    private double cubePosition = 0.5; // Position in tube (0.0 = bottom, 1.0 = top)
    private double cubeVelocity = 0.0; // Current velocity of the cube
    
    // Force limits
    private static final double MAX_FORCE = 0.3; // Keep original value - this affects both slider and target force generation
    private static final double MAX_TARGET_FORCE = 0.2; // Smaller range for target force generation to make it more manageable
    private static final double AUTO_SNAP_THRESHOLD = 0.1; // Auto-snap when within this range of target (30% of total range for more forgiving)
    
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
    private double targetForceDrift = 0.0;
    private double driftSpeed = 0.001; // How fast the target force drifts
    
    // Control states
    private boolean sliderActive = false;
    private int lastSliderX = 0;
    
    // Hover states
    private boolean sliderHovered = false;
    
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
        targetForceDrift = 0.0;
        
        // Randomize target force
        targetForce = -MAX_TARGET_FORCE + Math.random() * (2 * MAX_TARGET_FORCE);
        
        // Set initial values
        sliderForce = 0.0;
        cubePosition = 0.5;
        cubeVelocity = 0.0;
        
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
        // Calculate net force (target force + slider force)
        double netForce = targetForce + targetForceDrift + sliderForce;
        
        // Auto-snap the effective force when close enough (but don't change slider position)
        double targetSliderForce = -(targetForce + targetForceDrift);
        if (Math.abs(sliderForce - targetSliderForce) < AUTO_SNAP_THRESHOLD) {
            netForce = 0.0; // Perfect balance for physics, but keep slider position
        }
        
        // Apply physics (simplified gravity simulation)
        double acceleration = netForce * 0.01; // Scale down for reasonable movement
        cubeVelocity += acceleration;
        cubeVelocity *= 0.95; // Damping to prevent infinite oscillation
        
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
        if (isDrifting) {
            // Gradually drift the target force
            targetForceDrift += (Math.random() - 0.5) * driftSpeed;
            targetForceDrift = Math.max(-MAX_FORCE, Math.min(MAX_FORCE, targetForceDrift));
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
        
        // Calculate handle position (-MAX_FORCE to +MAX_FORCE maps to 0 to width)
        double normalizedForce = (sliderForce + MAX_FORCE) / (2 * MAX_FORCE);
        int handleX = x + (int) (normalizedForce * (width - SLIDER_HANDLE_WIDTH));
        int handleY = y - (SLIDER_HANDLE_HEIGHT - height) / 2;
        
        // Check if mouse is over handle for highlighting
        boolean handleHovered = (mouseX >= handleX && mouseX < handleX + SLIDER_HANDLE_WIDTH &&
                                mouseY >= handleY && mouseY < handleY + SLIDER_HANDLE_HEIGHT);
        
        // Render slider handle with highlighting
        if (handleHovered) {
            // Render highlighted handle (brighter)
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
                targetForceDrift = 0.0;
            }
            
            equilibriumTicks++;
            if (equilibriumTicks >= requiredEquilibriumTicks) {
                // Been in equilibrium long enough, start drift timer
                driftTicks++;
                int driftDelay = getDriftDelayTicks();
                if (driftTicks >= driftDelay && !isDrifting) {
                    isDrifting = true;
                }
            }
        } else {
            // Not in equilibrium, reset counters
            equilibriumTicks = 0;
            driftTicks = 0;
            isDrifting = false;
            targetForceDrift = 0.0;
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
        
        // Check if clicking on slider (updated position)
        int sliderX = panelX + panelWidth / 2 - SLIDER_WIDTH / 2;
        int sliderY = panelY + panelHeight - 8;
        
        if (mouseX >= sliderX && mouseX < sliderX + SLIDER_WIDTH &&
            mouseY >= sliderY && mouseY < sliderY + SLIDER_HEIGHT) {
            
            sliderActive = true;
            lastSliderX = mouseX;
            
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
        
        // Check slider hover (updated position)
        int sliderX = panelX + panelWidth / 2 - SLIDER_WIDTH / 2;
        int sliderY = panelY + panelHeight - 8;
        
        sliderHovered = (mouseX >= sliderX && mouseX < sliderX + SLIDER_WIDTH &&
                        mouseY >= sliderY && mouseY < sliderY + SLIDER_HEIGHT);
    }
    
    // Handle mouse drag and release through the base class system
    public void handleMouseDrag(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (!isActive || !sliderActive) return;
        
        // Calculate slider position relative to panel
        int sliderX = panelX + panelWidth / 2 - SLIDER_WIDTH / 2;
        
        // Convert mouse position to force value (0.0 to 1.0 across slider width)
        double relativeX = (mouseX - sliderX) / (double) SLIDER_WIDTH;
        relativeX = Math.max(0.0, Math.min(1.0, relativeX)); // Clamp to slider bounds
        
        // Convert to force range (-MAX_FORCE to +MAX_FORCE)
        sliderForce = (relativeX - 0.5) * 2.0 * MAX_FORCE;
        
        // Reset drift when actively adjusting
        isDrifting = false;
        targetForceDrift = 0.0;
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
    
    public double getSliderForce() {
        return sliderForce;
    }
    
    public double getTargetForce() {
        return targetForce;
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
    
    public double getTargetForceDrift() {
        return targetForceDrift;
    }
    
    // Setter methods for state restoration
    public void setSliderActive(boolean sliderActive) {
        this.sliderActive = sliderActive;
    }
    
    public void setSliderForce(double sliderForce) {
        this.sliderForce = sliderForce;
    }
    
    public void setTargetForce(double targetForce) {
        this.targetForce = targetForce;
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
    
    public void setTargetForceDrift(double targetForceDrift) {
        this.targetForceDrift = targetForceDrift;
    }
}
