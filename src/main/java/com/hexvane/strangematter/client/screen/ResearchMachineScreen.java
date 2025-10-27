package com.hexvane.strangematter.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.client.screen.components.InstabilityGauge;
import com.hexvane.strangematter.client.screen.minigames.ResearchMinigame;
import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.block.ResearchMachineBlockEntity;
import com.hexvane.strangematter.sound.StrangeMatterSounds;

import java.util.*;

public class ResearchMachineScreen extends Screen {
    
    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 240;
    private static final int BORDER_THICKNESS = 4;
    
    // Background texture (191x193)
    private static final ResourceLocation RUNIC_BACKGROUND = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/runic_background.png");
    private static final int BACKGROUND_TEXTURE_WIDTH = 191;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 193;
    
    // Colors
    private static final int PURPLE_BORDER_COLOR = 0xFF502b71;
    private static final int BUTTON_HOVER_COLOR = 0xFF268e97;
    private static final int BUTTON_CLICK_COLOR = 0xFF1a6b73; // Darker shade
    private static final int BUTTON_TEXT_COLOR = 0xFF3dc7c7; // Same as instability gauge
    
    // Button texture
    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/button.png");
    
    // Minigame panel texture
    private static final ResourceLocation MINIGAME_PANEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/minigame_panel.png");
    private static final int MINIGAME_PANEL_WIDTH = 55;
    private static final int MINIGAME_PANEL_HEIGHT = 81;
    
    private final ResearchMachineBlockEntity blockEntity;
    private int guiX;
    private int guiY;
    private InstabilityGauge instabilityGauge;
    
    // Minigame panels - one for each research type
    private Map<ResearchType, ResearchMinigame> minigames = new HashMap<>();
    private Map<ResearchType, Integer> panelPositions = new HashMap<>();
    private int tickCounter = 0;
    private boolean completionPacketSent = false;
    private boolean idleSoundPlaying = false;
    
    // Button positions and states
    private int helpButtonX, helpButtonY;
    private int exitButtonX, exitButtonY;
    private boolean helpButtonHovered = false;
    private boolean exitButtonHovered = false;
    private boolean helpButtonPressed = false;
    private boolean exitButtonPressed = false;
    
    public ResearchMachineScreen(BlockEntity blockEntity) {
        super(Component.translatable("gui.strangematter.research_machine.title"));
        this.blockEntity = (ResearchMachineBlockEntity) blockEntity;
        initializeMinigames();
    }
    
    /**
     * Handles state synchronization from the server
     */
    public void handleStateSync(ResearchMachineBlockEntity.MachineState state, String researchId, 
                               Set<ResearchType> activeTypes, float instabilityLevel, int researchTicks) {
        // Update instability gauge
        if (instabilityGauge != null) {
            instabilityGauge.setFillLevel(instabilityLevel);
        }
        
        // Update minigames based on state and active research types
        // Only activate minigames when actually researching
        boolean isResearching = state == ResearchMachineBlockEntity.MachineState.RESEARCHING;
        
        for (Map.Entry<ResearchType, ResearchMinigame> entry : minigames.entrySet()) {
            ResearchType type = entry.getKey();
            ResearchMinigame minigame = entry.getValue();
            
            if (isResearching && activeTypes.contains(type)) {
                // Activate minigame if it's not already active and we're researching
                if (!minigame.isActive()) {
                    minigame.activate();
                }
            } else {
                // Deactivate minigame if it's currently active
                if (minigame.isActive()) {
                    minigame.deactivate();
                }
            }
        }
        
        // Update tick counter for any time-based UI elements
        this.tickCounter = researchTicks;
        
        // Force a GUI refresh to update the display
        if (minecraft != null) {
            minecraft.execute(() -> {
                // Refresh the GUI state based on the new synchronized data
                refreshGuiState();
            });
        }
    }
    
    /**
     * Refreshes the GUI state based on the current block entity state
     * This is called after state synchronization to update the display
     */
    private void refreshGuiState() {
        if (blockEntity == null) return;
        
        // Update instability gauge with current state
        if (instabilityGauge != null) {
            instabilityGauge.setFillLevel(blockEntity.getInstabilityLevel());
        }
        
        // Update minigames based on current state and active research types
        // Only activate minigames when actually researching
        boolean isResearching = blockEntity.getCurrentState() == ResearchMachineBlockEntity.MachineState.RESEARCHING;
        
        for (Map.Entry<ResearchType, ResearchMinigame> entry : minigames.entrySet()) {
            ResearchType type = entry.getKey();
            ResearchMinigame minigame = entry.getValue();
            
            if (isResearching && blockEntity.getActiveResearchTypes().contains(type)) {
                // Activate minigame if it's not already active and we're researching
                if (!minigame.isActive()) {
                    minigame.activate();
                }
            } else {
                // Deactivate minigame if it's currently active
                if (minigame.isActive()) {
                    minigame.deactivate();
                }
            }
        }
        
        // Update tick counter
        this.tickCounter = blockEntity.getResearchTicks();
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Request current state from server to ensure client is synchronized
        if (blockEntity != null) {
            com.hexvane.strangematter.network.RequestResearchMachineStatePacket packet = 
                new com.hexvane.strangematter.network.RequestResearchMachineStatePacket(blockEntity.getBlockPos());
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(packet);
        }
        
        // Check if machine is locked to another player
        if (blockEntity != null && minecraft != null && minecraft.player != null) {
            java.util.UUID playerId = minecraft.player.getUUID();
            if (blockEntity.isPlayerLocked(playerId)) {
                // Machine is locked to another player, close GUI
                minecraft.player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("gui.strangematter.research_machine.locked"), 
                    true
                );
                onClose();
                return;
            }
            
            // Lock machine to this player
            blockEntity.lockToPlayer(playerId);
        }
        
        // Play open sound
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(StrangeMatterSounds.RESEARCH_MACHINE_OPEN.get(), 0.8f, 1.0f);
        }
        
        // Center the GUI on screen
        this.guiX = (this.width - GUI_WIDTH) / 2;
        this.guiY = (this.height - GUI_HEIGHT) / 2;
        
        // Initialize the instability gauge on the right side, centered vertically
        int gaugeX = guiX + GUI_WIDTH - 55; // 20 pixels from the right edge
        int gaugeY = guiY + (GUI_HEIGHT - 140) / 2; // Center vertically (assuming 70 height of the gauge)
        this.instabilityGauge = new InstabilityGauge(gaugeX, gaugeY);
        
        // Initialize button positions (bottom right corner)
        this.helpButtonX = guiX + GUI_WIDTH - 100;
        this.helpButtonY = guiY + GUI_HEIGHT - 20;
        this.exitButtonX = guiX + GUI_WIDTH - 50;
        this.exitButtonY = guiY + GUI_HEIGHT - 20;
        
        // Initialize minigame panel positions
        initializePanelPositions();
        
        // Restore minigame state if available
        restoreMinigameState();
        
        // If research is already in progress, activate minigames for active research types
        if (blockEntity != null && blockEntity.getCurrentState() == ResearchMachineBlockEntity.MachineState.RESEARCHING) {
            for (ResearchType type : blockEntity.getActiveResearchTypes()) {
                ResearchMinigame minigame = minigames.get(type);
                if (minigame != null && !minigame.isActive()) {
                    minigame.activate();
                }
            }
        }
    }
    
    private void initializeMinigames() {
        // Initialize placeholder minigames for each research type
        for (ResearchType type : ResearchType.values()) {
            minigames.put(type, createPlaceholderMinigame(type));
        }
    }
    
    private void initializePanelPositions() {
        // Arrange panels in a 3x2 grid in the center-left area
        int startX = guiX + 20;
        int startY = guiY + 60; // Original position
        int spacing = 8;
        
        ResearchType[] types = ResearchType.values();
        for (int i = 0; i < types.length; i++) {
            int col = i / 2;  // 3 columns (0, 1, 2)
            int row = i % 2;  // 2 rows (0, 1)
            int panelX = startX + col * (MINIGAME_PANEL_WIDTH + spacing);
            // Move ONLY the top row down to avoid overlap with button, bottom row stays at original position
            int panelY = startY + row * (MINIGAME_PANEL_HEIGHT + spacing) + (row == 0 ? -5 : 0);
            panelPositions.put(types[i], (panelX << 16) | panelY); // Pack X and Y into single int
        }
    }
    
    private ResearchMinigame createPlaceholderMinigame(ResearchType type) {
        if (type == ResearchType.ENERGY) {
            return new com.hexvane.strangematter.client.screen.minigames.EnergyMinigame();
        }
        
        if (type == ResearchType.GRAVITY) {
            return new com.hexvane.strangematter.client.screen.minigames.GravityMinigame();
        }
        
        if (type == ResearchType.TIME) {
            return new com.hexvane.strangematter.client.screen.minigames.TimeMinigame();
        }
        
        if (type == ResearchType.SPACE) {
            return new com.hexvane.strangematter.client.screen.minigames.SpaceMinigame();
        }
        
        if (type == ResearchType.SHADOW) {
            return new com.hexvane.strangematter.client.screen.minigames.ShadowMinigame();
        }
        
        if (type == ResearchType.COGNITION) {
            return new com.hexvane.strangematter.client.screen.minigames.CognitionMinigame();
        }
        
        // Default placeholder for other types
        return new ResearchMinigame(type) {
            @Override
            protected void onActivate() {
                // Placeholder
            }
            
            @Override
            protected void onDeactivate() {
                // Placeholder
            }
            
            @Override
            protected void updateMinigame() {
                // Placeholder
            }
            
            @Override
            protected void renderActive(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
                // Only render state - title is handled by base class
                guiGraphics.drawCenteredString(minecraft.font, "ACTIVE", x + width/2, y + height/2, 
                    getState() == ResearchMinigame.MinigameState.STABLE ? 0x00FF00 : 0xFF0000);
            }
            
            @Override
            protected void renderInactive(GuiGraphics guiGraphics, int x, int y, int width, int height) {
                // No content when inactive - title is handled by base class
            }
            
            @Override
            protected boolean handleClick(int mouseX, int mouseY, int button, int panelX, int panelY, int panelWidth, int panelHeight) {
                // Placeholder - toggle state on click
                if (button == 0) { // Left click
                    setState(getState() == ResearchMinigame.MinigameState.STABLE ? 
                        ResearchMinigame.MinigameState.UNSTABLE : ResearchMinigame.MinigameState.STABLE);
                    return true;
                }
                return false;
            }
        };
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render GUI elements
        renderGuiBackground(guiGraphics);
        renderBorder(guiGraphics);
        
        // Render minigame panels
        renderMinigamePanels(guiGraphics, mouseX, mouseY, partialTick);
        
        // Render center content based on machine state
        renderCenterContent(guiGraphics, mouseX, mouseY);
        
        // Render the instability gauge
        if (instabilityGauge != null) {
            instabilityGauge.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        
        // Render buttons
        renderButtons(guiGraphics, mouseX, mouseY);
        
        // Render widgets explicitly instead of calling super.render() to avoid blurred background
        for (net.minecraft.client.gui.components.Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
    
    private void renderGuiBackground(GuiGraphics guiGraphics) {
        // Calculate how many tiles we need to cover the GUI area
        int tilesX = (GUI_WIDTH + BACKGROUND_TEXTURE_WIDTH - 1) / BACKGROUND_TEXTURE_WIDTH;
        int tilesY = (GUI_HEIGHT + BACKGROUND_TEXTURE_HEIGHT - 1) / BACKGROUND_TEXTURE_HEIGHT;
        
        // Render tiled background
        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                int sourceWidth = Math.min(BACKGROUND_TEXTURE_WIDTH, GUI_WIDTH - (x * BACKGROUND_TEXTURE_WIDTH));
                int sourceHeight = Math.min(BACKGROUND_TEXTURE_HEIGHT, GUI_HEIGHT - (y * BACKGROUND_TEXTURE_HEIGHT));
                
                guiGraphics.blit(
                    RUNIC_BACKGROUND,
                    guiX + (x * BACKGROUND_TEXTURE_WIDTH),
                    guiY + (y * BACKGROUND_TEXTURE_HEIGHT),
                    0, 0,
                    sourceWidth, sourceHeight,
                    BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT
                );
            }
        }
    }
    
    private void renderMinigamePanels(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Update instability gauge particles
        if (instabilityGauge != null) {
            instabilityGauge.update();
        }
        
        for (Map.Entry<ResearchType, ResearchMinigame> entry : minigames.entrySet()) {
            ResearchType type = entry.getKey();
            ResearchMinigame minigame = entry.getValue();
            Integer packedPosition = panelPositions.get(type);
            
            if (packedPosition != null) {
                int panelX = (packedPosition >> 16) & 0xFFFF;
                int panelY = packedPosition & 0xFFFF;
                
                // Update minigame (for shutter animations and other updates)
                minigame.tick();
                
                // Update hover states for minigames that support it
                if (minigame instanceof com.hexvane.strangematter.client.screen.minigames.EnergyMinigame) {
                    com.hexvane.strangematter.client.screen.minigames.EnergyMinigame energyMinigame = 
                        (com.hexvane.strangematter.client.screen.minigames.EnergyMinigame) minigame;
                    energyMinigame.updateHoverStates(mouseX, mouseY, panelX, panelY, MINIGAME_PANEL_WIDTH, MINIGAME_PANEL_HEIGHT);
                } else if (minigame instanceof com.hexvane.strangematter.client.screen.minigames.ShadowMinigame) {
                    com.hexvane.strangematter.client.screen.minigames.ShadowMinigame shadowMinigame = 
                        (com.hexvane.strangematter.client.screen.minigames.ShadowMinigame) minigame;
                    shadowMinigame.updateHoverStates(mouseX, mouseY, panelX, panelY, MINIGAME_PANEL_WIDTH, MINIGAME_PANEL_HEIGHT);
                } else if (minigame instanceof com.hexvane.strangematter.client.screen.minigames.CognitionMinigame) {
                    com.hexvane.strangematter.client.screen.minigames.CognitionMinigame cognitionMinigame = 
                        (com.hexvane.strangematter.client.screen.minigames.CognitionMinigame) minigame;
                    cognitionMinigame.updateHoverStates(mouseX, mouseY, panelX, panelY, MINIGAME_PANEL_WIDTH, MINIGAME_PANEL_HEIGHT);
                }
                
                // Always render panel background
                guiGraphics.blit(MINIGAME_PANEL_TEXTURE, panelX, panelY, 0, 0, 
                    MINIGAME_PANEL_WIDTH, MINIGAME_PANEL_HEIGHT, 
                    MINIGAME_PANEL_WIDTH, MINIGAME_PANEL_HEIGHT);
                
                // Always render minigame content (minigame handles inactive state internally)
                minigame.render(guiGraphics, panelX, panelY, MINIGAME_PANEL_WIDTH, MINIGAME_PANEL_HEIGHT, mouseX, mouseY);
            }
        }
    }
    
    private void renderCenterContent(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ResearchMachineBlockEntity.MachineState state = blockEntity.getCurrentState();
        int centerX = guiX + GUI_WIDTH / 2;
        int topY = guiY + 15; // Position at the top
        
        switch (state) {
            case IDLE:
                // Show "Insert research notes to start research"
                guiGraphics.drawCenteredString(minecraft.font, 
                    Component.translatable("gui.strangematter.research_machine.insert_notes"), 
                    centerX, topY, 0xFFFFFF);
                break;
                
            case READY:
                // Show "Begin Research" button
                int buttonWidth = 100;
                int buttonHeight = 20;
                int buttonX = centerX - buttonWidth / 2;
                int buttonY = topY;
                
                // Check if mouse is over button
                boolean buttonHovered = mouseX >= buttonX && mouseX < buttonX + buttonWidth && 
                                      mouseY >= buttonY && mouseY < buttonY + buttonHeight;
                
                // Render button background
                int buttonColor = buttonHovered ? BUTTON_HOVER_COLOR : 0xFFFFFFFF;
                guiGraphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, buttonColor);
                
                // Render button text
                guiGraphics.drawCenteredString(minecraft.font, 
                    Component.translatable("gui.strangematter.research_machine.begin_research"), 
                    centerX, topY + 6, BUTTON_TEXT_COLOR);
                break;
                
            case RESEARCHING:
                // Show research progress
                guiGraphics.drawCenteredString(minecraft.font, 
                    Component.translatable("gui.strangematter.research_machine.researching"), 
                    centerX, topY, 0xFFFFFF);
                break;
                
            case COMPLETED:
                // Show success message
                guiGraphics.drawCenteredString(minecraft.font, 
                    Component.translatable("gui.strangematter.research_machine.completed"), 
                    centerX, topY, 0x00FF00);
                break;
                
            case FAILED:
                // Show failure message
                guiGraphics.drawCenteredString(minecraft.font, 
                    Component.translatable("gui.strangematter.research_machine.failed"), 
                    centerX, topY, 0xFF0000);
                break;
        }
    }
    
    private void renderBorder(GuiGraphics guiGraphics) {
        // Top border
        guiGraphics.fill(guiX - BORDER_THICKNESS, guiY - BORDER_THICKNESS, 
                        guiX + GUI_WIDTH + BORDER_THICKNESS, guiY, PURPLE_BORDER_COLOR);
        
        // Bottom border
        guiGraphics.fill(guiX - BORDER_THICKNESS, guiY + GUI_HEIGHT, 
                        guiX + GUI_WIDTH + BORDER_THICKNESS, guiY + GUI_HEIGHT + BORDER_THICKNESS, PURPLE_BORDER_COLOR);
        
        // Left border
        guiGraphics.fill(guiX - BORDER_THICKNESS, guiY - BORDER_THICKNESS, 
                        guiX, guiY + GUI_HEIGHT + BORDER_THICKNESS, PURPLE_BORDER_COLOR);
        
        // Right border
        guiGraphics.fill(guiX + GUI_WIDTH, guiY - BORDER_THICKNESS, 
                        guiX + GUI_WIDTH + BORDER_THICKNESS, guiY + GUI_HEIGHT + BORDER_THICKNESS, PURPLE_BORDER_COLOR);
    }
    
    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Update button hover states
        updateButtonHoverStates(mouseX, mouseY);
        
        // Render HELP button
        renderButton(guiGraphics, helpButtonX, helpButtonY, "HELP", helpButtonHovered, helpButtonPressed);
        
        // Render EXIT button
        renderButton(guiGraphics, exitButtonX, exitButtonY, "EXIT", exitButtonHovered, exitButtonPressed);
    }
    
    private void updateButtonHoverStates(int mouseX, int mouseY) {
        // HELP button bounds (48x16 button size)
        helpButtonHovered = mouseX >= helpButtonX && mouseX < helpButtonX + 48 && 
                           mouseY >= helpButtonY && mouseY < helpButtonY + 16;
        
        // EXIT button bounds
        exitButtonHovered = mouseX >= exitButtonX && mouseX < exitButtonX + 48 && 
                           mouseY >= exitButtonY && mouseY < exitButtonY + 16;
        
        // Debug: Log hover states occasionally
        if (helpButtonHovered) {
            System.out.println("Help button hovered! Mouse: " + mouseX + "," + mouseY + " Button: " + helpButtonX + "," + helpButtonY);
        }
    }
    
    private void renderButton(GuiGraphics guiGraphics, int x, int y, String text, boolean hovered, boolean pressed) {
        // Determine button color based on state
        int buttonColor = 0xFFFFFFFF; // Default white
        if (pressed) {
            buttonColor = BUTTON_CLICK_COLOR;
        } else if (hovered) {
            buttonColor = BUTTON_HOVER_COLOR;
        }
        
        // Render button background with color tint
        guiGraphics.setColor(
            ((buttonColor >> 16) & 0xFF) / 255.0f,
            ((buttonColor >> 8) & 0xFF) / 255.0f,
            (buttonColor & 0xFF) / 255.0f,
            1.0f
        );
        
        // Render button texture (48x16 size)
        guiGraphics.blit(BUTTON_TEXTURE, x, y, 0, 0, 48, 16, 48, 16);
        
        // Reset color
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // Render button text
        int textWidth = minecraft.font.width(text);
        int textX = x + (48 - textWidth) / 2; // Center text horizontally
        int textY = y + (16 - 7) / 2; // Center text vertically (assuming 7 pixel font height)
        
        guiGraphics.drawString(
            minecraft.font,
            text,
            textX, textY,
            BUTTON_TEXT_COLOR
        );
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left mouse button
            // Check minigame panel clicks first
            for (Map.Entry<ResearchType, ResearchMinigame> entry : minigames.entrySet()) {
                ResearchType type = entry.getKey();
                ResearchMinigame minigame = entry.getValue();
                Integer packedPosition = panelPositions.get(type);
                
                if (packedPosition != null) {
                    int panelX = (packedPosition >> 16) & 0xFFFF;
                    int panelY = packedPosition & 0xFFFF;
                    
                    if (minigame.mouseClicked(mouseX, mouseY, button, panelX, panelY, MINIGAME_PANEL_WIDTH, MINIGAME_PANEL_HEIGHT)) {
                        return true;
                    }
                }
            }
            
            // Check center button click
            if (blockEntity.getCurrentState() == ResearchMachineBlockEntity.MachineState.READY) {
                int centerX = guiX + GUI_WIDTH / 2;
                int topY = guiY + 15;
                int buttonWidth = 100;
                int buttonHeight = 20;
                int buttonX = centerX - buttonWidth / 2;
                int buttonY = topY;
                
                if (mouseX >= buttonX && mouseX < buttonX + buttonWidth && 
                    mouseY >= buttonY && mouseY < buttonY + buttonHeight) {
                    blockEntity.beginResearch();
                    // Play begin research sound
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(StrangeMatterSounds.RESEARCH_MACHINE_BEGIN_RESEARCH.get(), 0.8f, 1.0f);
                    }
                    // Start idle loop sound
                    startIdleSound();
                    // Activate minigames for active research types
                    for (ResearchType type : blockEntity.getActiveResearchTypes()) {
                        ResearchMinigame minigame = minigames.get(type);
                        if (minigame != null) {
                            minigame.activate();
                        }
                    }
                    // Reset completion flag for new research
                    completionPacketSent = false;
                    return true;
                }
            }
            
            // Check help/exit buttons
            if (helpButtonHovered) {
                helpButtonPressed = true;
                
                // Ensure research nodes are initialized
                com.hexvane.strangematter.research.ResearchNodeRegistry.initializeDefaultNodes();
                
                // Open the research node info screen to the minigames page (page 2, 0-indexed)
                com.hexvane.strangematter.research.ResearchNode researchNode = 
                    com.hexvane.strangematter.research.ResearchNodeRegistry.getNode("research");
                
                if (researchNode != null) {
                    this.minecraft.setScreen(new com.hexvane.strangematter.client.screen.ResearchNodeInfoScreen(researchNode, this, 2));
                }
                return true;
            } else if (exitButtonHovered) {
                exitButtonPressed = true;
                this.onClose();
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void startIdleSound() {
        if (!idleSoundPlaying && minecraft != null && minecraft.player != null) {
            idleSoundPlaying = true;
            // Play the idle loop sound at a low volume
            minecraft.player.playSound(StrangeMatterSounds.RESEARCH_MACHINE_IDLE_LOOP.get(), 0.3f, 1.0f);
        }
    }
    
    private void stopIdleSound() {
        if (idleSoundPlaying && minecraft != null && minecraft.player != null) {
            idleSoundPlaying = false;
            // Stop the idle sound by playing it at volume 0
            minecraft.player.playSound(StrangeMatterSounds.RESEARCH_MACHINE_IDLE_LOOP.get(), 0.0f, 1.0f);
        }
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) { // Left mouse button
            // Handle minigame drag events
            for (Map.Entry<ResearchType, ResearchMinigame> entry : minigames.entrySet()) {
                ResearchType type = entry.getKey();
                ResearchMinigame minigame = entry.getValue();
                Integer packedPosition = panelPositions.get(type);
                
                if (packedPosition != null) {
                    int panelX = (packedPosition >> 16) & 0xFFFF;
                    int panelY = packedPosition & 0xFFFF;
                    
                    // Handle specific minigame drag events
                    if (minigame instanceof com.hexvane.strangematter.client.screen.minigames.GravityMinigame) {
                        com.hexvane.strangematter.client.screen.minigames.GravityMinigame gravityMinigame = 
                            (com.hexvane.strangematter.client.screen.minigames.GravityMinigame) minigame;
                        gravityMinigame.handleMouseDrag((int)mouseX, (int)mouseY, panelX, panelY, MINIGAME_PANEL_WIDTH, MINIGAME_PANEL_HEIGHT);
                    }
                }
            }
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left mouse button
            helpButtonPressed = false;
            exitButtonPressed = false;
            
            // Handle minigame release events
            for (Map.Entry<ResearchType, ResearchMinigame> entry : minigames.entrySet()) {
                ResearchType type = entry.getKey();
                ResearchMinigame minigame = entry.getValue();
                Integer packedPosition = panelPositions.get(type);
                
                if (packedPosition != null) {
                    int panelX = (packedPosition >> 16) & 0xFFFF;
                    int panelY = packedPosition & 0xFFFF;
                    
                    // Handle specific minigame release events
                    if (minigame instanceof com.hexvane.strangematter.client.screen.minigames.GravityMinigame) {
                        com.hexvane.strangematter.client.screen.minigames.GravityMinigame gravityMinigame = 
                            (com.hexvane.strangematter.client.screen.minigames.GravityMinigame) minigame;
                        gravityMinigame.handleMouseRelease((int)mouseX, (int)mouseY, panelX, panelY, MINIGAME_PANEL_WIDTH, MINIGAME_PANEL_HEIGHT, button);
                    }
                }
            }
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        
        // Close on ESC
        if (keyCode == 256) { // ESC key
            this.onClose();
            return true;
        }
        
        return false;
    }
    
    @Override
    public void tick() {
        super.tick();
        tickCounter++;
        
        // Update all minigames
        for (ResearchMinigame minigame : minigames.values()) {
            minigame.tick();
        }
        
        // Update research state if researching (client-side)
        if (blockEntity.getCurrentState() == ResearchMachineBlockEntity.MachineState.RESEARCHING) {
            // Update minigames and calculate instability on client side
            Map<ResearchType, Boolean> minigameStates = new HashMap<>();
            for (Map.Entry<ResearchType, ResearchMinigame> entry : minigames.entrySet()) {
                ResearchMinigame minigame = entry.getValue();
                if (minigame.isActive()) {
                    minigameStates.put(entry.getKey(), minigame.isStable());
                }
            }
            
            // Update instability level on client side
            updateInstabilityLevel(minigameStates);
            
            // Update instability gauge
            if (instabilityGauge != null) {
                instabilityGauge.setFillLevel(blockEntity.getInstabilityLevel());
            }
        }
        
        
        
        // Force screen refresh every 10 ticks to show updated state
        if (tickCounter % 10 == 0) {
            // The render method will automatically pick up the new state from blockEntity
        }
    }
    
    private void updateInstabilityLevel(Map<ResearchType, Boolean> minigameStates) {
        // Check if all active minigames are stable
        boolean allStable = true;
        boolean anyUnstable = false;
        
        for (ResearchType type : blockEntity.getActiveResearchTypes()) {
            Boolean isStable = minigameStates.get(type);
            if (isStable != null) {
                if (isStable) {
                    // Minigame is stable
                } else {
                    anyUnstable = true;
                }
            } else {
                allStable = false;
            }
        }
        
        // Update instability level
        float oldInstability = blockEntity.getInstabilityLevel();
        if (allStable && !anyUnstable) {
            // All minigames are stable, decrease instability
            float decreaseRate = (float) com.hexvane.strangematter.Config.instabilityDecreaseRate;
            blockEntity.setClientInstabilityLevel(Math.max(0.0f, oldInstability - decreaseRate));
        } else if (anyUnstable) {
            // At least one minigame is unstable, increase instability
            // More active minigames = SLOWER increase (divide by number of active minigames)
            int activeMinigameCount = blockEntity.getActiveResearchTypes().size();
            float baseIncreaseRate = (float) com.hexvane.strangematter.Config.instabilityBaseIncreaseRate;
            float actualIncreaseRate = activeMinigameCount > 0 ? baseIncreaseRate / activeMinigameCount : baseIncreaseRate;
            blockEntity.setClientInstabilityLevel(Math.min(1.0f, oldInstability + actualIncreaseRate));
        }
        
        // Instability warning sounds removed per user request
        
        // Check for completion or failure (with padding for visual bar)
        float currentInstability = blockEntity.getInstabilityLevel();
        if (currentInstability <= 0.05f && !completionPacketSent) {
            // Research completed successfully - send completion packet to server
            stopIdleSound();
            sendResearchCompletionToServer(true);
            completionPacketSent = true;
            
            // Deactivate all minigames and close GUI immediately
            for (ResearchMinigame minigame : minigames.values()) {
                if (minigame.isActive()) {
                    minigame.deactivate();
                }
            }
            forceClose();
        } else if (currentInstability >= 0.95f && !completionPacketSent) {
            // Research failed due to anomaly - send failure packet to server
            stopIdleSound();
            sendResearchCompletionToServer(false);
            completionPacketSent = true;
            
            // Deactivate all minigames and close GUI immediately
            for (ResearchMinigame minigame : minigames.values()) {
                if (minigame.isActive()) {
                    minigame.deactivate();
                }
            }
            forceClose();
        }
    }
    
    private void sendResearchCompletionToServer(boolean success) {
        // Send completion/failure packet to server
        com.hexvane.strangematter.network.ResearchCompletionPacket packet = 
            new com.hexvane.strangematter.network.ResearchCompletionPacket(blockEntity.getBlockPos(), success);
        
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(packet);
    }
    
    @Override
    public void onClose() {
        // Save minigame state before closing
        if (blockEntity != null) {
            saveMinigameState();
            // Unlock the machine
            blockEntity.unlockPlayer();
        }
        
        // Stop idle sound when closing GUI
        stopIdleSound();
        super.onClose();
    }
    
    /**
     * Force close the GUI (used for completion/failure scenarios)
     */
    private void forceClose() {
        // Don't save state for completion/failure - server handles it
        stopIdleSound();
        super.onClose();
    }
    
    /**
     * Save the current minigame state to the block entity
     */
    private void saveMinigameState() {
        if (blockEntity == null) return;
        
        // Create a map of minigame states
        java.util.Map<ResearchType, java.util.Map<String, Object>> minigameStates = new java.util.HashMap<>();
        
        for (Map.Entry<ResearchType, ResearchMinigame> entry : minigames.entrySet()) {
            ResearchType type = entry.getKey();
            ResearchMinigame minigame = entry.getValue();
            
            java.util.Map<String, Object> state = new java.util.HashMap<>();
            state.put("isActive", minigame.isActive());
            state.put("state", minigame.getState().toString());
            
            // Save specific minigame data
            if (minigame instanceof com.hexvane.strangematter.client.screen.minigames.EnergyMinigame) {
                com.hexvane.strangematter.client.screen.minigames.EnergyMinigame energyMinigame = 
                    (com.hexvane.strangematter.client.screen.minigames.EnergyMinigame) minigame;
                state.put("amplitudeDialActive", energyMinigame.isAmplitudeDialActive());
                state.put("periodDialActive", energyMinigame.isPeriodDialActive());
                state.put("currentAmplitude", energyMinigame.getCurrentAmplitude());
                state.put("currentPeriod", energyMinigame.getCurrentPeriod());
                state.put("targetAmplitude", energyMinigame.getTargetAmplitude());
                state.put("targetPeriod", energyMinigame.getTargetPeriod());
            } else if (minigame instanceof com.hexvane.strangematter.client.screen.minigames.GravityMinigame) {
                com.hexvane.strangematter.client.screen.minigames.GravityMinigame gravityMinigame = 
                    (com.hexvane.strangematter.client.screen.minigames.GravityMinigame) minigame;
                state.put("sliderActive", gravityMinigame.isSliderActive());
                state.put("sliderValue", gravityMinigame.getSliderValue());
                state.put("targetGravity", gravityMinigame.getTargetGravity());
                state.put("cubePosition", gravityMinigame.getCubePosition());
                state.put("cubeVelocity", gravityMinigame.getCubeVelocity());
                state.put("isInEquilibrium", gravityMinigame.isInEquilibrium());
                state.put("equilibriumTicks", gravityMinigame.getEquilibriumTicks());
                state.put("driftTicks", gravityMinigame.getDriftTicks());
                state.put("isDrifting", gravityMinigame.isDrifting());
                state.put("needsNewTarget", gravityMinigame.getNeedsNewTarget());
            }
            
            minigameStates.put(type, state);
        }
        
        // Send state to server
        com.hexvane.strangematter.network.MinigameStatePacket packet = 
            new com.hexvane.strangematter.network.MinigameStatePacket(blockEntity.getBlockPos(), minigameStates);
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(packet);
    }
    
    /**
     * Restore minigame state from the block entity
     */
    private void restoreMinigameState() {
        if (blockEntity == null) return;
        
        // Get saved minigame states from block entity
        java.util.Map<ResearchType, java.util.Map<String, Object>> savedStates = blockEntity.getMinigameStates();
        if (savedStates == null || savedStates.isEmpty()) return;
        
        for (Map.Entry<ResearchType, java.util.Map<String, Object>> entry : savedStates.entrySet()) {
            ResearchType type = entry.getKey();
            java.util.Map<String, Object> state = entry.getValue();
            ResearchMinigame minigame = minigames.get(type);
            
            if (minigame == null || state == null) continue;
            
            // Restore basic state
            boolean wasActive = (Boolean) state.getOrDefault("isActive", false);
            if (wasActive) {
                minigame.activate();
            }
            
            // Restore specific minigame data
            if (minigame instanceof com.hexvane.strangematter.client.screen.minigames.EnergyMinigame) {
                com.hexvane.strangematter.client.screen.minigames.EnergyMinigame energyMinigame = 
                    (com.hexvane.strangematter.client.screen.minigames.EnergyMinigame) minigame;
                
                energyMinigame.setAmplitudeDialActive((Boolean) state.getOrDefault("amplitudeDialActive", false));
                energyMinigame.setPeriodDialActive((Boolean) state.getOrDefault("periodDialActive", false));
                energyMinigame.setCurrentAmplitude((Double) state.getOrDefault("currentAmplitude", 1.0));
                energyMinigame.setCurrentPeriod((Double) state.getOrDefault("currentPeriod", 1.0));
                energyMinigame.setTargetAmplitude((Double) state.getOrDefault("targetAmplitude", 1.0));
                energyMinigame.setTargetPeriod((Double) state.getOrDefault("targetPeriod", 1.0));
            } else if (minigame instanceof com.hexvane.strangematter.client.screen.minigames.GravityMinigame) {
                com.hexvane.strangematter.client.screen.minigames.GravityMinigame gravityMinigame = 
                    (com.hexvane.strangematter.client.screen.minigames.GravityMinigame) minigame;
                
                gravityMinigame.setSliderActive((Boolean) state.getOrDefault("sliderActive", false));
                gravityMinigame.setSliderValue((Integer) state.getOrDefault("sliderValue", 0));
                gravityMinigame.setTargetGravity((Integer) state.getOrDefault("targetGravity", 0));
                gravityMinigame.setCubePosition((Double) state.getOrDefault("cubePosition", 0.5));
                gravityMinigame.setCubeVelocity((Double) state.getOrDefault("cubeVelocity", 0.0));
                gravityMinigame.setInEquilibrium((Boolean) state.getOrDefault("isInEquilibrium", false));
                gravityMinigame.setEquilibriumTicks((Integer) state.getOrDefault("equilibriumTicks", 0));
                gravityMinigame.setDriftTicks((Integer) state.getOrDefault("driftTicks", 0));
                gravityMinigame.setDrifting((Boolean) state.getOrDefault("isDrifting", false));
                gravityMinigame.setNeedsNewTarget((Boolean) state.getOrDefault("needsNewTarget", false));
            }
        }
    }
}
