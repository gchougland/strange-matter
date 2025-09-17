package com.hexvane.strangematter.client.screen.minigames;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CognitionMinigame extends ResearchMinigame {
    
    // UI Constants
    private static final int GRID_SIZE = 3; // 3x3 grid
    private static final int SYMBOL_SIZE = 12; // Size of each enchanting symbol
    private static final int GRID_SPACING = 2; // Spacing between symbols
    private static final int TOTAL_GRID_SIZE = (SYMBOL_SIZE + GRID_SPACING) * GRID_SIZE - GRID_SPACING; // Total grid size
    
    // Game mechanics
    private static final int PATTERN_LENGTH = 3; // Always 3 symbols
    private static final int DISPLAY_DURATION_TICKS = 60; // 3 seconds at 20 TPS
    private static final int PATTERN_REDISPLAY_TICKS = 100; // 5 seconds between pattern displays
    private static final int DRIFT_DELAY_TICKS = 600; // 30 seconds before drift starts
    
    // Enchanting symbols - using more visually distinct runic symbols
    private static final String[] ENCHANTING_SYMBOLS = {
        "ᚠ", "ᚢ", "ᚦ", "ᚨ", "ᚱ", "ᚲ", "ᚷ", "ᚹ", "ᚺ" // More distinct runic symbols
    };
    
    // Colors
    private static final int SYMBOL_COLOR = 0xFF3dc7c7; // Same as instability gauge
    private static final int BORDER_COLOR = 0xFF3dc7c7; // Cyan border for symbol buttons
    private static final int HIGHLIGHT_COLOR = 0x80FFFFFF; // Semi-transparent white for highlights
    private static final int CORRECT_COLOR = 0x8000FF00; // Semi-transparent green for correct
    private static final int INCORRECT_COLOR = 0x80FF0000; // Semi-transparent red for incorrect
    
    // Game state
    private List<Integer> targetPattern = new ArrayList<>(); // The pattern to match
    private List<Integer> playerInput = new ArrayList<>(); // Player's current input
    private int displayTicks = 0; // How long the pattern has been displayed
    private boolean patternDisplayed = false; // Whether we're showing the pattern
    private int redisplayTicks = 0; // Ticks until next pattern re-display
    private int driftTicks = 0; // Ticks since last drift
    
    // Sequential pattern display
    private int currentSymbolIndex = 0; // Which symbol in the pattern is currently being shown
    private int symbolDisplayTicks = 0; // How long the current symbol has been displayed
    private static final int SYMBOL_DISPLAY_DURATION = 20; // 1 second per symbol at 20 TPS
    private Random random = new Random();
    
    // Button states
    private boolean[] buttonHovered = new boolean[GRID_SIZE * GRID_SIZE];
    private boolean[] buttonPressed = new boolean[GRID_SIZE * GRID_SIZE];
    private int buttonCooldown = 0;
    private static final int BUTTON_COOLDOWN_TICKS = 5;
    
    // Minecraft instance for sound and player access
    private final Minecraft minecraft;
    
    public CognitionMinigame() {
        super(ResearchType.COGNITION);
        this.minecraft = Minecraft.getInstance();
    }
    
    @Override
    protected void onActivate() {
        generateNewPattern();
        setState(MinigameState.UNSTABLE);
    }
    
    @Override
    protected void onDeactivate() {
        // Reset state
        targetPattern.clear();
        playerInput.clear();
        displayTicks = 0;
        patternDisplayed = false;
        redisplayTicks = 0;
        driftTicks = 0;
        currentSymbolIndex = 0;
        symbolDisplayTicks = 0;
    }
    
    public void updateMinigame() {
        if (!isActive) return;
        
        // Update sequential pattern display
        if (patternDisplayed) {
            symbolDisplayTicks++;
            if (symbolDisplayTicks >= SYMBOL_DISPLAY_DURATION) {
                // Move to next symbol
                currentSymbolIndex++;
                symbolDisplayTicks = 0;
                
                // If we've shown all symbols, stop displaying
                if (currentSymbolIndex >= targetPattern.size()) {
                    patternDisplayed = false;
                    currentSymbolIndex = 0;
                }
            }
        } else {
            // Update redisplay timer
            redisplayTicks++;
            if (redisplayTicks >= PATTERN_REDISPLAY_TICKS) {
                // Re-display the pattern from the beginning
                patternDisplayed = true;
                currentSymbolIndex = 0;
                symbolDisplayTicks = 0;
                redisplayTicks = 0;
            }
        }
        
        // Update button cooldown
        if (buttonCooldown > 0) {
            buttonCooldown--;
        }
        
        // Check stability
        checkStability();
        
        // Handle drift
        if (getState() == MinigameState.STABLE) {
            driftTicks++;
            if (driftTicks >= DRIFT_DELAY_TICKS) {
                // Generate new pattern and reset
                generateNewPattern();
                driftTicks = 0;
                setState(MinigameState.UNSTABLE);
            }
        } else {
            driftTicks = 0;
        }
    }
    
    @Override
    protected void renderActive(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        
        // Calculate grid position (centered in panel)
        int gridX = centerX - TOTAL_GRID_SIZE / 2;
        int gridY = centerY - TOTAL_GRID_SIZE / 2;
        
        // Render the 3x3 grid of symbols
        renderSymbolGrid(guiGraphics, gridX, gridY);
        
        // Render pattern display if active
        if (patternDisplayed) {
            renderPatternDisplay(guiGraphics, gridX, gridY);
        }
        
        // Render player input feedback
        renderInputFeedback(guiGraphics, gridX, gridY);
    }
    
    @Override
    protected void renderInactive(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Don't render anything when inactive - the shutters will be handled by the base class
    }
    
    @Override
    protected boolean handleClick(int mouseX, int mouseY, int button, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (!isActive || button != 0 || buttonCooldown > 0) return false;
        
        int centerX = panelX + panelWidth / 2;
        int centerY = panelY + panelHeight / 2;
        int gridX = centerX - TOTAL_GRID_SIZE / 2;
        int gridY = centerY - TOTAL_GRID_SIZE / 2;
        
        // Check if click is within grid
        if (mouseX >= gridX && mouseX < gridX + TOTAL_GRID_SIZE &&
            mouseY >= gridY && mouseY < gridY + TOTAL_GRID_SIZE) {
            
            // Calculate which symbol was clicked
            int relativeX = mouseX - gridX;
            int relativeY = mouseY - gridY;
            
            int symbolX = relativeX / (SYMBOL_SIZE + GRID_SPACING);
            int symbolY = relativeY / (SYMBOL_SIZE + GRID_SPACING);
            
            if (symbolX >= 0 && symbolX < GRID_SIZE && symbolY >= 0 && symbolY < GRID_SIZE) {
                int symbolIndex = symbolY * GRID_SIZE + symbolX;
                
                // Add to player input
                playerInput.add(symbolIndex);
                
                // Check if pattern is complete
                if (playerInput.size() >= targetPattern.size()) {
                    checkPattern();
                }
                
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
    
    public void updateHoverStates(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        if (!isActive) return;
        
        int centerX = panelX + panelWidth / 2;
        int centerY = panelY + panelHeight / 2;
        int gridX = centerX - TOTAL_GRID_SIZE / 2;
        int gridY = centerY - TOTAL_GRID_SIZE / 2;
        
        // Reset hover states
        for (int i = 0; i < buttonHovered.length; i++) {
            buttonHovered[i] = false;
        }
        
        // Check if mouse is within grid
        if (mouseX >= gridX && mouseX < gridX + TOTAL_GRID_SIZE &&
            mouseY >= gridY && mouseY < gridY + TOTAL_GRID_SIZE) {
            
            int relativeX = mouseX - gridX;
            int relativeY = mouseY - gridY;
            
            int symbolX = relativeX / (SYMBOL_SIZE + GRID_SPACING);
            int symbolY = relativeY / (SYMBOL_SIZE + GRID_SPACING);
            
            if (symbolX >= 0 && symbolX < GRID_SIZE && symbolY >= 0 && symbolY < GRID_SIZE) {
                int symbolIndex = symbolY * GRID_SIZE + symbolX;
                buttonHovered[symbolIndex] = true;
            }
        }
    }
    
    private void generateNewPattern() {
        targetPattern.clear();
        playerInput.clear();
        
        // Generate a random 3-symbol pattern with no duplicates
        List<Integer> availableSymbols = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
            availableSymbols.add(i);
        }
        
        for (int i = 0; i < PATTERN_LENGTH; i++) {
            int randomIndex = random.nextInt(availableSymbols.size());
            int symbol = availableSymbols.remove(randomIndex);
            targetPattern.add(symbol);
        }
        
        // Display the pattern sequentially
        patternDisplayed = true;
        currentSymbolIndex = 0;
        symbolDisplayTicks = 0;
    }
    
    private void checkPattern() {
        boolean correct = true;
        
        // Check if player input matches target pattern
        if (playerInput.size() != targetPattern.size()) {
            correct = false;
        } else {
            for (int i = 0; i < targetPattern.size(); i++) {
                if (!playerInput.get(i).equals(targetPattern.get(i))) {
                    correct = false;
                    break;
                }
            }
        }
        
        if (correct) {
            setState(MinigameState.STABLE);
            // Play success sound
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(StrangeMatterSounds.MINIGAME_STABLE.get(), 0.4f, 1.0f);
            }
        } else {
            setState(MinigameState.UNSTABLE);
            // Play failure sound
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(StrangeMatterSounds.MINIGAME_UNSTABLE.get(), 0.3f, 1.0f);
            }
            // Reset player input
            playerInput.clear();
        }
    }
    
    private void checkStability() {
        // Stability is determined by whether the current pattern is correct
        // This is handled in checkPattern()
    }
    
    private void renderSymbolGrid(GuiGraphics guiGraphics, int gridX, int gridY) {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                int symbolIndex = y * GRID_SIZE + x;
                int symbolX = gridX + x * (SYMBOL_SIZE + GRID_SPACING);
                int symbolY = gridY + y * (SYMBOL_SIZE + GRID_SPACING);
                
                // Render symbol background
                guiGraphics.fill(symbolX, symbolY, symbolX + SYMBOL_SIZE, symbolY + SYMBOL_SIZE, 0xFF2A2A2A);
                
                // Render cyan border
                guiGraphics.fill(symbolX - 1, symbolY - 1, symbolX + SYMBOL_SIZE + 1, symbolY, BORDER_COLOR); // Top border
                guiGraphics.fill(symbolX - 1, symbolY + SYMBOL_SIZE, symbolX + SYMBOL_SIZE + 1, symbolY + SYMBOL_SIZE + 1, BORDER_COLOR); // Bottom border
                guiGraphics.fill(symbolX - 1, symbolY - 1, symbolX, symbolY + SYMBOL_SIZE + 1, BORDER_COLOR); // Left border
                guiGraphics.fill(symbolX + SYMBOL_SIZE, symbolY - 1, symbolX + SYMBOL_SIZE + 1, symbolY + SYMBOL_SIZE + 1, BORDER_COLOR); // Right border
                
                // Render enchanting symbol using text
                String symbol = ENCHANTING_SYMBOLS[symbolIndex];
                guiGraphics.drawCenteredString(
                    minecraft.font,
                    symbol,
                    symbolX + SYMBOL_SIZE / 2,
                    symbolY + SYMBOL_SIZE / 2 - minecraft.font.lineHeight / 2,
                    SYMBOL_COLOR
                );
                
                // Render hover highlight
                if (buttonHovered[symbolIndex]) {
                    guiGraphics.fill(symbolX - 2, symbolY - 2, symbolX + SYMBOL_SIZE + 2, symbolY + SYMBOL_SIZE + 2, HIGHLIGHT_COLOR);
                }
            }
        }
    }
    
    private void renderPatternDisplay(GuiGraphics guiGraphics, int gridX, int gridY) {
        // Only highlight the current symbol being displayed
        if (currentSymbolIndex < targetPattern.size()) {
            int symbolIndex = targetPattern.get(currentSymbolIndex);
            int x = symbolIndex % GRID_SIZE;
            int y = symbolIndex / GRID_SIZE;
            int symbolX = gridX + x * (SYMBOL_SIZE + GRID_SPACING);
            int symbolY = gridY + y * (SYMBOL_SIZE + GRID_SPACING);
            
            // Render pattern highlight for current symbol
            guiGraphics.fill(symbolX - 2, symbolY - 2, symbolX + SYMBOL_SIZE + 2, symbolY + SYMBOL_SIZE + 2, 0x80FFFF00); // Yellow highlight
        }
    }
    
    private void renderInputFeedback(GuiGraphics guiGraphics, int gridX, int gridY) {
        // Render feedback for player input
        for (int i = 0; i < playerInput.size(); i++) {
            int symbolIndex = playerInput.get(i);
            int x = symbolIndex % GRID_SIZE;
            int y = symbolIndex / GRID_SIZE;
            int symbolX = gridX + x * (SYMBOL_SIZE + GRID_SPACING);
            int symbolY = gridY + y * (SYMBOL_SIZE + GRID_SPACING);
            
            // Check if this input is correct
            boolean correct = i < targetPattern.size() && playerInput.get(i).equals(targetPattern.get(i));
            int color = correct ? CORRECT_COLOR : INCORRECT_COLOR;
            
            // Render input feedback
            guiGraphics.fill(symbolX - 1, symbolY - 1, symbolX + SYMBOL_SIZE + 1, symbolY + SYMBOL_SIZE + 1, color);
        }
    }
    
    // Getters and setters for state saving/loading
    public List<Integer> getTargetPattern() { return new ArrayList<>(targetPattern); }
    public void setTargetPattern(List<Integer> targetPattern) { this.targetPattern = new ArrayList<>(targetPattern); }
    
    public List<Integer> getPlayerInput() { return new ArrayList<>(playerInput); }
    public void setPlayerInput(List<Integer> playerInput) { this.playerInput = new ArrayList<>(playerInput); }
    
    public int getDisplayTicks() { return displayTicks; }
    public void setDisplayTicks(int displayTicks) { this.displayTicks = displayTicks; }
    
    public boolean isPatternDisplayed() { return patternDisplayed; }
    public void setPatternDisplayed(boolean patternDisplayed) { this.patternDisplayed = patternDisplayed; }
    
    public int getRedisplayTicks() { return redisplayTicks; }
    public void setRedisplayTicks(int redisplayTicks) { this.redisplayTicks = redisplayTicks; }
    
    public int getCurrentSymbolIndex() { return currentSymbolIndex; }
    public void setCurrentSymbolIndex(int currentSymbolIndex) { this.currentSymbolIndex = currentSymbolIndex; }
    
    public int getSymbolDisplayTicks() { return symbolDisplayTicks; }
    public void setSymbolDisplayTicks(int symbolDisplayTicks) { this.symbolDisplayTicks = symbolDisplayTicks; }
    
    public int getDriftTicks() { return driftTicks; }
    public void setDriftTicks(int driftTicks) { this.driftTicks = driftTicks; }
    
    public int getButtonCooldown() { return buttonCooldown; }
    public void setButtonCooldown(int buttonCooldown) { this.buttonCooldown = buttonCooldown; }
}
