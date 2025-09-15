package com.hexvane.strangematter.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
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
    private static final ResourceLocation RUNIC_BACKGROUND = new ResourceLocation(StrangeMatterMod.MODID, "textures/ui/runic_background.png");
    private static final int BACKGROUND_TEXTURE_WIDTH = 191;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 193;
    
    // Colors
    private static final int PURPLE_BORDER_COLOR = 0xFF502b71;
    private static final int BUTTON_HOVER_COLOR = 0xFF268e97;
    private static final int BUTTON_CLICK_COLOR = 0xFF1a6b73; // Darker shade
    private static final int BUTTON_TEXT_COLOR = 0xFF3dc7c7; // Same as instability gauge
    
    // Button texture
    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation(StrangeMatterMod.MODID, "textures/ui/button.png");
    
    // Minigame panel texture
    private static final ResourceLocation MINIGAME_PANEL_TEXTURE = new ResourceLocation(StrangeMatterMod.MODID, "textures/ui/minigame_panel.png");
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
    
    @Override
    protected void init() {
        super.init();
        
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
        int startY = guiY + 60;
        int spacing = 8;
        
        ResearchType[] types = ResearchType.values();
        for (int i = 0; i < types.length; i++) {
            int col = i / 2;  // 3 columns (0, 1, 2)
            int row = i % 2;  // 2 rows (0, 1)
            int panelX = startX + col * (MINIGAME_PANEL_WIDTH + spacing);
            int panelY = startY + row * (MINIGAME_PANEL_HEIGHT + spacing);
            panelPositions.put(types[i], (panelX << 16) | panelY); // Pack X and Y into single int
        }
    }
    
    private ResearchMinigame createPlaceholderMinigame(ResearchType type) {
        if (type == ResearchType.ENERGY) {
            return new com.hexvane.strangematter.client.screen.minigames.EnergyMinigame();
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
                // Render placeholder active content
                guiGraphics.drawCenteredString(minecraft.font, type.getName(), x + width/2, y + height/2 - 4, 0xFFFFFF);
                guiGraphics.drawCenteredString(minecraft.font, "ACTIVE", x + width/2, y + height/2 + 8, 
                    getState() == ResearchMinigame.MinigameState.STABLE ? 0x00FF00 : 0xFF0000);
            }
            
            @Override
            protected void renderInactive(GuiGraphics guiGraphics, int x, int y, int width, int height) {
                // Render placeholder inactive content
                guiGraphics.drawCenteredString(minecraft.font, type.getName(), x + width/2, y + height/2 - 4, 0x888888);
                guiGraphics.drawCenteredString(minecraft.font, "INACTIVE", x + width/2, y + height/2 + 8, 0x888888);
            }
            
            @Override
            protected boolean handleClick(int relativeX, int relativeY, int button) {
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
        // Render background
        renderBackground(guiGraphics);
        
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
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
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
        for (Map.Entry<ResearchType, ResearchMinigame> entry : minigames.entrySet()) {
            ResearchType type = entry.getKey();
            ResearchMinigame minigame = entry.getValue();
            Integer packedPosition = panelPositions.get(type);
            
            if (packedPosition != null) {
                int panelX = (packedPosition >> 16) & 0xFFFF;
                int panelY = packedPosition & 0xFFFF;
                
                // Update hover states for Energy minigame
                if (minigame instanceof com.hexvane.strangematter.client.screen.minigames.EnergyMinigame) {
                    com.hexvane.strangematter.client.screen.minigames.EnergyMinigame energyMinigame = 
                        (com.hexvane.strangematter.client.screen.minigames.EnergyMinigame) minigame;
                    int relativeX = mouseX - panelX;
                    int relativeY = mouseY - panelY;
                    energyMinigame.updateHoverStates(relativeX, relativeY);
                }
                
                // Render panel background
                guiGraphics.blit(MINIGAME_PANEL_TEXTURE, panelX, panelY, 0, 0, 
                    MINIGAME_PANEL_WIDTH, MINIGAME_PANEL_HEIGHT, 
                    MINIGAME_PANEL_WIDTH, MINIGAME_PANEL_HEIGHT);
                
                // Render minigame content
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
                    
                    if (minigame.mouseClicked((int) mouseX, (int) mouseY, button, panelX, panelY, MINIGAME_PANEL_WIDTH, MINIGAME_PANEL_HEIGHT)) {
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
                // TODO: Open help dialog
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left mouse button
            helpButtonPressed = false;
            exitButtonPressed = false;
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
            blockEntity.setClientInstabilityLevel(Math.max(0.0f, oldInstability - 0.004f));
        } else if (anyUnstable) {
            // At least one minigame is unstable, increase instability
            blockEntity.setClientInstabilityLevel(Math.min(1.0f, oldInstability + 0.0005f));
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
            this.onClose();
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
            this.onClose();
        }
    }
    
    private void sendResearchCompletionToServer(boolean success) {
        // Send completion/failure packet to server
        com.hexvane.strangematter.network.ResearchCompletionPacket packet = 
            new com.hexvane.strangematter.network.ResearchCompletionPacket(blockEntity.getBlockPos(), success);
        
        com.hexvane.strangematter.network.NetworkHandler.INSTANCE.sendToServer(packet);
    }
    
    @Override
    public void onClose() {
        // Stop idle sound when closing GUI
        stopIdleSound();
        super.onClose();
    }
}
