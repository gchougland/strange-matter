package com.hexvane.strangematter.client.screen;

import com.hexvane.strangematter.client.research.ResearchNodePositionManager;
import com.hexvane.strangematter.network.ResearchDataClientHandler;
import com.hexvane.strangematter.research.ResearchData;
import com.hexvane.strangematter.research.ResearchNode;
import com.hexvane.strangematter.research.ResearchNodeRegistry;
import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResearchTabletScreen extends Screen {
    private static final ResourceLocation RESEARCH_TABLET_BACKGROUND = ResourceLocation.parse("strangematter:textures/ui/research_tablet_background.png");
    private static final ResourceLocation RESEARCH_TABLET_OVERLAY = ResourceLocation.parse("strangematter:textures/ui/research_tablet_overlay.png");
    private static final ResourceLocation RESEARCH_NODE_TEXTURE = ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png");
    
    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 240;
    private static final int DRAGGABLE_AREA_WIDTH = 246;
    private static final int DRAGGABLE_AREA_HEIGHT = 161;
    private static final int BACKGROUND_TILE_SIZE = 64;
    
    private int guiX, guiY;
    private int dragOffsetX = 0, dragOffsetY = 0;
    private boolean isDragging = false;
    private int lastMouseX, lastMouseY;
    private String selectedCategory = "general";
    
    // Store saved drag position when opening research panes
    private int savedDragOffsetX = 0, savedDragOffsetY = 0;
    private int refreshCounter = 0;
    
    // Track hovered node for sound effects
    private ResearchNode lastHoveredNode = null;
    
    private final Map<String, Button> categoryButtons = new HashMap<>();
    
    // Debug mode state
    private boolean debugMode = false;
    private ResearchNodePositionManager positionManager;
    
    // Node dragging state (for debug mode)
    private ResearchNode draggedNode = null;
    
    public ResearchTabletScreen() {
        super(Component.translatable("gui.strangematter.research_tablet"));
        
        // Initialize research nodes
        ResearchNodeRegistry.initializeDefaultNodes();
        
        // Initialize position manager
        this.positionManager = ResearchNodePositionManager.getInstance();
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Play open sound
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(StrangeMatterSounds.RESEARCH_TABLET_OPEN.get(), 0.7f, 1.0f);
        }
        
        this.guiX = (this.width - GUI_WIDTH) / 2;
        this.guiY = (this.height - GUI_HEIGHT) / 2;
        
        // Initialize drag offset to center the view on the screen (start at center of draggable area)
        this.dragOffsetX = 0; // Start at center horizontally
        this.dragOffsetY = 0; // Start at center vertically
        
        // TODO: Re-enable category tabs when needed
        // createCategoryButtons();
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Toggle debug mode with F3+D
        if (keyCode == GLFW.GLFW_KEY_D && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            debugMode = !debugMode;
            if (debugMode) {
                // Save positions when entering debug mode
                positionManager.savePositions();
            }
            return true;
        }
        
        // Save positions with Ctrl+S in debug mode
        if (debugMode && keyCode == GLFW.GLFW_KEY_S && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            positionManager.savePositions();
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.literal("§aSaved research node positions"));
            }
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    private void createCategoryButtons() {
        List<String> categories = ResearchNodeRegistry.getCategories();
        int buttonWidth = 60;
        int buttonHeight = 20;
        int buttonSpacing = 5;
        int totalButtonWidth = categories.size() * buttonWidth + (categories.size() - 1) * buttonSpacing;
        int startX = guiX + (GUI_WIDTH - totalButtonWidth) / 2;
        int buttonY = guiY + 20; // Added more padding from top
        
        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            Button button = Button.builder(
                Component.translatable("research.category.strangematter." + category),
                (btn) -> {
                    selectedCategory = category;
                    // Play page turn sound
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(StrangeMatterSounds.RESEARCH_TABLET_PAGE_TURN.get(), 0.6f, 1.0f);
                    }
                }
            )
            .bounds(startX + i * (buttonWidth + buttonSpacing), buttonY, buttonWidth, buttonHeight)
            .build();
            
            categoryButtons.put(category, button);
            addRenderableWidget(button);
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        
        // Render main GUI background as grey box
        int bPadding = 10;
        guiGraphics.fill(guiX + bPadding, guiY + bPadding, guiX + GUI_WIDTH - bPadding, guiY + GUI_HEIGHT - bPadding, 0xFF404040);
        
        // Render draggable research area (without item icons)
        renderDraggableArea(guiGraphics, mouseX, mouseY);
        
        // Render semi-transparent background for research points
        renderResearchPointsBackground(guiGraphics);
        
        // Render research points display
        renderResearchPoints(guiGraphics);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Render overlay last to appear on top of everything including item icons
        RenderSystem.setShaderTexture(0, RESEARCH_TABLET_OVERLAY);
        guiGraphics.blit(RESEARCH_TABLET_OVERLAY, guiX, guiY, 0, 0, GUI_WIDTH, GUI_HEIGHT, 320, 240);
        
        // Render debug mode overlay
        if (debugMode) {
            renderDebugOverlay(guiGraphics, mouseX, mouseY);
        }
        
        // Render research node tooltips on top of everything
        renderResearchNodeTooltips(guiGraphics, mouseX, mouseY);
    }
    
    /**
     * Render debug mode overlay showing mouse coordinates and debug mode indicator.
     */
    private void renderDebugOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int draggableX = guiX + (GUI_WIDTH - DRAGGABLE_AREA_WIDTH) / 2;
        int draggableY = guiY + 21;
        
        // Check if mouse is in draggable area
        if (mouseX >= draggableX && mouseX <= draggableX + DRAGGABLE_AREA_WIDTH &&
            mouseY >= draggableY && mouseY <= draggableY + DRAGGABLE_AREA_HEIGHT) {
            
            // Convert mouse position to research coordinate space
            int researchX = mouseX - draggableX - DRAGGABLE_AREA_WIDTH / 2 - dragOffsetX;
            int researchY = mouseY - draggableY - DRAGGABLE_AREA_HEIGHT / 2 - dragOffsetY;
            
            // Snap to grid
            int gridX = ResearchNodePositionManager.snapToGrid(researchX);
            int gridY = ResearchNodePositionManager.snapToGrid(researchY);
            
            // Render coordinate display
            String coordText = String.format("X: %d, Y: %d", researchX, researchY);
            String gridText = String.format("Grid: X: %d, Y: %d", gridX, gridY);
            
            int textX = mouseX + 10;
            int textY = mouseY - 30;
            
            // Background for text
            int textWidth = Math.max(font.width(coordText), font.width(gridText)) + 4;
            guiGraphics.fill(textX - 2, textY - 2, textX + textWidth + 2, textY + 24, 0x80000000);
            guiGraphics.renderOutline(textX - 2, textY - 2, textWidth + 4, 24, 0xFF00FF00);
            
            // Render text
            guiGraphics.drawString(this.font, coordText, textX, textY, 0xFFFFFF);
            guiGraphics.drawString(this.font, gridText, textX, textY + 12, 0x00FF00);
        }
        
        // Render debug mode indicator in top-left corner
        String debugText = "DEBUG MODE - Ctrl+D to toggle, Ctrl+S to save";
        int indicatorX = guiX + 5;
        int indicatorY = guiY + 5;
        int indicatorWidth = font.width(debugText) + 4;
        int indicatorHeight = 12;
        
        guiGraphics.fill(indicatorX - 2, indicatorY - 2, indicatorX + indicatorWidth + 2, indicatorY + indicatorHeight + 2, 0x80000000);
        guiGraphics.renderOutline(indicatorX - 2, indicatorY - 2, indicatorWidth + 4, indicatorHeight + 2, 0xFFFF0000);
        guiGraphics.drawString(this.font, debugText, indicatorX, indicatorY, 0xFFFF00);
    }
    
    private void renderDraggableArea(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int draggableX = guiX + (GUI_WIDTH - DRAGGABLE_AREA_WIDTH) / 2;
        int draggableY = guiY + 21;
        
        // Enable scissor to clip the draggable area
        guiGraphics.enableScissor(draggableX, draggableY, draggableX + DRAGGABLE_AREA_WIDTH, draggableY + DRAGGABLE_AREA_HEIGHT);
        
        // Calculate how many tiles we need to cover the area plus extra buffer to prevent popping
        int bufferTiles = 2; // Extra tiles on each side to prevent popping
        int tilesX = (DRAGGABLE_AREA_WIDTH + bufferTiles * 2 * BACKGROUND_TILE_SIZE) / BACKGROUND_TILE_SIZE + 3;
        int tilesY = (DRAGGABLE_AREA_HEIGHT + bufferTiles * 2 * BACKGROUND_TILE_SIZE) / BACKGROUND_TILE_SIZE + 3;
        
        int startTileX = (dragOffsetX % BACKGROUND_TILE_SIZE) - (dragOffsetX < 0 ? BACKGROUND_TILE_SIZE : 0) - (bufferTiles * BACKGROUND_TILE_SIZE);
        int startTileY = (dragOffsetY % BACKGROUND_TILE_SIZE) - (dragOffsetY < 0 ? BACKGROUND_TILE_SIZE : 0) - (bufferTiles * BACKGROUND_TILE_SIZE);
        
        // Render tiled background with buffer to prevent popping
        RenderSystem.setShaderTexture(0, RESEARCH_TABLET_BACKGROUND);
        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                int tileX = draggableX + startTileX + (x * BACKGROUND_TILE_SIZE);
                int tileY = draggableY + startTileY + (y * BACKGROUND_TILE_SIZE);
                guiGraphics.blit(RESEARCH_TABLET_BACKGROUND, tileX, tileY, 0, 0, BACKGROUND_TILE_SIZE, BACKGROUND_TILE_SIZE, BACKGROUND_TILE_SIZE, BACKGROUND_TILE_SIZE);
            }
        }
        
        // Render research node connections (lines between nodes)
        renderResearchNodeConnections(guiGraphics, draggableX, draggableY);
        
        // Render research nodes (inside scissor area)
        renderResearchNodes(guiGraphics, draggableX, draggableY, mouseX, mouseY);
        
        // Disable scissor
        guiGraphics.disableScissor();
    }
    
    private void renderResearchNodeConnections(GuiGraphics guiGraphics, int draggableX, int draggableY) {
        List<ResearchNode> nodes = ResearchNodeRegistry.getNodesByCategory(selectedCategory);
        ResearchData researchData = ResearchDataClientHandler.getClientResearchData();
        
        // Collect all lines with their priority for proper z-ordering
        List<ConnectionLine> allLines = new ArrayList<>();
        
        for (ResearchNode node : nodes) {
            if (!node.hasPrerequisites()) {
                continue; // Skip nodes without prerequisites
            }
            
            int nodeX = draggableX + DRAGGABLE_AREA_WIDTH / 2 + positionManager.getEffectiveX(node) + dragOffsetX;
            int nodeY = draggableY + DRAGGABLE_AREA_HEIGHT / 2 + positionManager.getEffectiveY(node) + dragOffsetY;
            
            // Node position for line calculations
            
            // Collect lines to prerequisite nodes
            for (String prerequisiteId : node.getPrerequisites()) {
                ResearchNode prerequisite = ResearchNodeRegistry.getNode(prerequisiteId);
                if (prerequisite != null) {
                    int prereqX = draggableX + DRAGGABLE_AREA_WIDTH / 2 + positionManager.getEffectiveX(prerequisite) + dragOffsetX;
                    int prereqY = draggableY + DRAGGABLE_AREA_HEIGHT / 2 + positionManager.getEffectiveY(prerequisite) + dragOffsetY;
                    
                    // Prerequisite position for line calculations
                    
                    // Always add line to collection (render lines at all times)
                    // Determine line color and priority based on unlock status
                    boolean isUnlocked = researchData.getUnlockedResearch().contains(node.getId());
                    boolean prereqUnlocked = researchData.getUnlockedResearch().contains(prerequisiteId);
                    
                    int lineColor;
                    int priority; // Higher number = renders on top (drawn later)
                    if (isUnlocked && prereqUnlocked) {
                        lineColor = 0x41B280; // Teal for unlocked connections
                        priority = 3; // Highest priority - renders on top (drawn last)
                    } else if (prereqUnlocked) {
                        lineColor = 0xFFFFFF; // White for partially unlocked
                        priority = 2; // Middle priority
                    } else {
                        lineColor = 0x808080; // Grey for locked connections
                        priority = 1; // Lowest priority - renders at bottom (drawn first)
                    }
                    
                    // Add line to collection
                    int centerX1 = prereqX + 16;
                    int centerY1 = prereqY + 16;
                    int centerX2 = nodeX + 16;
                    int centerY2 = nodeY + 16;
                    
                    allLines.add(new ConnectionLine(centerX1, centerY1, centerX2, centerY2, lineColor, prerequisiteId, node.getId(), priority));
                }
            }
        }
        
        // Sort lines by priority (grey first, then white, then teal on top)
        allLines.sort((a, b) -> Integer.compare(a.priority, b.priority));
        
        // Draw all lines in sorted order
        for (ConnectionLine line : allLines) {
            drawLine(guiGraphics, line.x1, line.y1, line.x2, line.y2, line.color, line.nodeId1, line.nodeId2);
        }
    }
    
    // Helper method to check if all prerequisites are unlocked
    private boolean hasPrerequisitesUnlocked(ResearchNode node, ResearchData researchData) {
        for (String prerequisiteId : node.getPrerequisites()) {
            if (!researchData.getUnlockedResearch().contains(prerequisiteId)) {
                return false;
            }
        }
        return true;
    }
    
    // Helper method to get the research node that was clicked
    private ResearchNode getClickedNode(double mouseX, double mouseY) {
        int draggableX = guiX + 1 + (GUI_WIDTH - DRAGGABLE_AREA_WIDTH) / 2;
        int draggableY = guiY + 15;
        
        
        for (ResearchNode node : ResearchNodeRegistry.getNodesByCategory(selectedCategory)) {
            int nodeX = draggableX + DRAGGABLE_AREA_WIDTH / 2 + positionManager.getEffectiveX(node) + dragOffsetX;
            int nodeY = draggableY + DRAGGABLE_AREA_HEIGHT / 2 + positionManager.getEffectiveY(node) + dragOffsetY;
            
            // Check if click is within this node's bounds (32x32 pixels)
            if (mouseX >= nodeX && mouseX <= nodeX + 32 &&
                mouseY >= nodeY && mouseY <= nodeY + 32) {
                return node;
            }
        }
        return null;
    }
    
    // Helper method to open information page for a research node
    private void openNodeInformationPage(ResearchNode node) {
        // Save current drag position before opening information page
        savedDragOffsetX = dragOffsetX;
        savedDragOffsetY = dragOffsetY;
        
        // Open the research node information screen
        this.minecraft.setScreen(new ResearchNodeInfoScreen(node, this));
    }
    
    // Method to restore drag position when returning from information page
    public void restoreDragPosition() {
        dragOffsetX = savedDragOffsetX;
        dragOffsetY = savedDragOffsetY;
    }
    
    // Helper method to generate obfuscated text (Standard Galactic Alphabet)
    private String generateObfuscatedText(String originalText) {
        // Use §k for obfuscated text effect (Standard Galactic Alphabet)
        return "§k" + originalText;
    }
    
    // Helper method to generate wrapped obfuscated text that respects word boundaries
    private List<Component> generateWrappedObfuscatedText(String originalText, int maxWidth) {
        List<Component> wrappedLines = new ArrayList<>();
        
        // Split the original text into words
        String[] words = originalText.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            // Test if adding this word would exceed the width
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            if (this.font.width(testLine) <= maxWidth) {
                // Add to current line
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                // Start a new line
                if (currentLine.length() > 0) {
                    wrappedLines.add(Component.literal("§7§k" + currentLine.toString()));
                    currentLine = new StringBuilder(word);
                } else {
                    // Single word is too long, just add it anyway
                    wrappedLines.add(Component.literal("§7§k" + word));
                }
            }
        }
        
        // Add the last line if there's content
        if (currentLine.length() > 0) {
            wrappedLines.add(Component.literal("§7§k" + currentLine.toString()));
        }
        
        return wrappedLines;
    }
    
    // Helper class to store line information for sorting
    private static class ConnectionLine {
        final int x1, y1, x2, y2, color, priority;
        final String nodeId1, nodeId2;
        
        ConnectionLine(int x1, int y1, int x2, int y2, int color, String nodeId1, String nodeId2, int priority) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
            this.nodeId1 = nodeId1;
            this.nodeId2 = nodeId2;
            this.priority = priority;
        }
    }
    
    private void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color, String nodeId1, String nodeId2) {
        // Circuit board style line drawing with multiple segments and right angles
        // This creates a PCB-like trace pattern
        
        // Make lines brighter and more visible
        int brightColor = color | 0xFF000000; // Ensure alpha is fully opaque
        
        // Extract color components
        float r = ((brightColor >> 16) & 0xFF) / 255.0F;
        float g = ((brightColor >> 8) & 0xFF) / 255.0F;
        float b = (brightColor & 0xFF) / 255.0F;
        
        RenderSystem.setShaderColor(r, g, b, 1.0F);
        
        // Make lines thinner for circuit board style (3 pixels wide)
        int lineThickness = 3;
        int halfThickness = lineThickness / 2; // 1 pixel on each side of center
        
        if (x1 != x2 && y1 != y2) {
            // Create circuit board routing with multiple segments
            drawCircuitBoardTrace(guiGraphics, x1, y1, x2, y2, brightColor, halfThickness, nodeId1, nodeId2);
        } else if (x1 != x2) {
            // Pure horizontal line
            int startX = Math.min(x1, x2);
            int endX = Math.max(x1, x2);
            guiGraphics.fill(startX, y1 - halfThickness, endX, y1 + halfThickness, brightColor);
        } else if (y1 != y2) {
            // Pure vertical line
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            guiGraphics.fill(x1 - halfThickness, startY, x1 + halfThickness, endY, brightColor);
        }
        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    private void drawCircuitBoardTrace(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color, int halfThickness, String nodeId1, String nodeId2) {
        // Create a circuit board style trace with multiple segments
        // This creates a more complex routing pattern
        
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        
        // Determine routing pattern based on distance and direction
        if (dx > 80 || dy > 80) {
            // Long connections - use 3-segment routing with intermediate waypoint
            drawLongCircuitTrace(guiGraphics, x1, y1, x2, y2, color, halfThickness, nodeId1, nodeId2);
        } else {
            // Short connections - use simple L-shaped routing
            drawShortCircuitTrace(guiGraphics, x1, y1, x2, y2, color, halfThickness);
        }
    }
    
    private void drawShortCircuitTrace(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color, int halfThickness) {
        // Simple L-shaped trace for short connections
        // Ensure proper connection to both nodes
        
        // Horizontal segment from x1 to x2
        int startX = Math.min(x1, x2);
        int endX = Math.max(x1, x2);
        guiGraphics.fill(startX, y1 - halfThickness, endX + halfThickness, y1 + halfThickness, color);
        
        // Vertical segment from y1 to y2 at x2
        int startY = Math.min(y1, y2);
        int endY = Math.max(y1, y2);
        guiGraphics.fill(x2 - halfThickness, startY - halfThickness, x2 + halfThickness, endY + halfThickness, color);
        
        // Add via (connection point) at the corner
        drawVia(guiGraphics, x2, y1, color);
    }
    
    private void drawLongCircuitTrace(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color, int halfThickness, String nodeId1, String nodeId2) {
        // Multi-segment trace for long connections with intermediate waypoint
        // Use completely stable positioning that never changes during drag
        
        // Create a stable waypoint based on node positions (not frame-dependent)
        int waypointX = x1 + (x2 - x1) / 2;
        int waypointY = y1 + (y2 - y1) / 2;
        
        // Use a completely stable offset based on the node IDs (never changes)
        // Hash the node IDs to create a deterministic but varied offset
        int connectionHash = Math.abs(nodeId1.hashCode() + nodeId2.hashCode());
        int stableOffset = (connectionHash % 16) - 8; // -8 to +8 pixel offset
        
        // First segment: horizontal from start to waypoint
        int startX = Math.min(x1, waypointX + stableOffset);
        int endX = Math.max(x1, waypointX + stableOffset);
        guiGraphics.fill(startX, y1 - halfThickness, endX + halfThickness, y1 + halfThickness, color);
        
        // Second segment: vertical from waypoint to intermediate Y
        int intermediateY = y1 + (y2 - y1) / 3;
        guiGraphics.fill(waypointX + stableOffset - halfThickness, y1 - halfThickness, 
                        waypointX + stableOffset + halfThickness, intermediateY, color);
        
        // Third segment: horizontal to final X (ensure it reaches x2)
        int finalX = x2; // Always end at the target node
        guiGraphics.fill(waypointX + stableOffset, intermediateY - halfThickness, 
                        finalX, intermediateY + halfThickness, color);
        
        // Fourth segment: vertical to end (ensure it reaches y2)
        guiGraphics.fill(finalX - halfThickness, intermediateY, 
                        finalX + halfThickness, y2, color);
        
        // Add vias at connection points
        drawVia(guiGraphics, waypointX + stableOffset, y1, color);
        drawVia(guiGraphics, waypointX + stableOffset, intermediateY, color);
        drawVia(guiGraphics, finalX, intermediateY, color);
    }
    
    private void drawVia(GuiGraphics guiGraphics, int x, int y, int color) {
        // Draw a small circular via (connection point) like on a PCB
        int viaSize = 4;
        int viaHalf = viaSize / 2;
        
        // Draw a small square via (easier than circle with current tools)
        guiGraphics.fill(x - viaHalf, y - viaHalf, x + viaHalf, y + viaHalf, color);
        
        // Add a slightly darker center to make it look more 3D
        int darkerColor = (color & 0xFF000000) | ((int)(((color >> 16) & 0xFF) * 0.7) << 16) | 
                         ((int)(((color >> 8) & 0xFF) * 0.7) << 8) | 
                         ((int)((color & 0xFF) * 0.7));
        guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, darkerColor);
    }
    
    private void renderResearchNodes(GuiGraphics guiGraphics, int draggableX, int draggableY, int mouseX, int mouseY) {
        List<ResearchNode> nodes = ResearchNodeRegistry.getNodesByCategory(selectedCategory);
        ResearchData researchData = ResearchDataClientHandler.getClientResearchData();
        
        for (ResearchNode node : nodes) {
            int nodeX = draggableX + DRAGGABLE_AREA_WIDTH / 2 + positionManager.getEffectiveX(node) + dragOffsetX;
            int nodeY = draggableY + DRAGGABLE_AREA_HEIGHT / 2 + positionManager.getEffectiveY(node) + dragOffsetY;
            
            // Check if node is within visible area
            if (nodeX + 32 >= draggableX && nodeX <= draggableX + DRAGGABLE_AREA_WIDTH &&
                nodeY + 32 >= draggableY && nodeY <= draggableY + DRAGGABLE_AREA_HEIGHT) {
                
                boolean isUnlocked = researchData.getUnlockedResearch().contains(node.getId());
                boolean canAfford = node.canAfford(getPlayerResearchPoints(researchData));
                boolean prerequisitesUnlocked = hasPrerequisitesUnlocked(node, researchData);
                boolean isHovered = mouseX >= nodeX && mouseX <= nodeX + 32 && mouseY >= nodeY && mouseY <= nodeY + 32;
                
                // Highlight dragged node in debug mode
                boolean isDragged = debugMode && draggedNode == node;
                
                // Check for hover sound
                if (isHovered && lastHoveredNode != node) {
                    // Play hover sound
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(StrangeMatterSounds.RESEARCH_TABLET_NODE_HOVER.get(), 0.3f, 1.0f);
                    }
                    lastHoveredNode = node;
                } else if (!isHovered && lastHoveredNode == node) {
                    lastHoveredNode = null;
                }
                
                // Render node background
                RenderSystem.setShaderTexture(0, RESEARCH_NODE_TEXTURE);
                int color = isUnlocked ? 0x41B280 : (prerequisitesUnlocked ? 0xFFFFFF : 0x808080);
                RenderSystem.setShaderColor(
                    ((color >> 16) & 0xFF) / 255.0F,
                    ((color >> 8) & 0xFF) / 255.0F,
                    (color & 0xFF) / 255.0F,
                    1.0F
                );
                
                guiGraphics.blit(RESEARCH_NODE_TEXTURE, nodeX, nodeY, 0, 0, 32, 32, 32, 32);
                
                // Render node icon
                if (node.hasIconItem()) {
                    // Reset color to white before rendering item to avoid tinting
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    // Render item as 3D model like in inventory
                    guiGraphics.renderItem(node.getIconItem(), nodeX + 8, nodeY + 8);
                } else if (node.hasIconTexture()) {
                    RenderSystem.setShaderTexture(0, node.getIconTexture());
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    guiGraphics.blit(node.getIconTexture(), nodeX + 8, nodeY + 8, 0, 0, 16, 16, 16, 16);
                }
                
                // Render hover effect
                if (isHovered && !debugMode) {
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
                    guiGraphics.fill(nodeX, nodeY, nodeX + 32, nodeY + 32, 0xFFFFFFFF);
                }
                
                // Render debug mode drag indicator
                if (isDragged) {
                    RenderSystem.setShaderColor(1.0F, 1.0F, 0.0F, 0.7F);
                    guiGraphics.fill(nodeX - 2, nodeY - 2, nodeX + 34, nodeY + 34, 0xFFFFFF00);
                }
                
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }
    
    private void renderResearchNodeTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<ResearchNode> nodes = ResearchNodeRegistry.getNodesByCategory(selectedCategory);
        ResearchData researchData = ResearchDataClientHandler.getClientResearchData();
        int draggableX = guiX + 1 + (GUI_WIDTH - DRAGGABLE_AREA_WIDTH) / 2;
        int draggableY = guiY + 15;
        
        for (ResearchNode node : nodes) {
            int nodeX = draggableX + DRAGGABLE_AREA_WIDTH / 2 + positionManager.getEffectiveX(node) + dragOffsetX;
            int nodeY = draggableY + DRAGGABLE_AREA_HEIGHT / 2 + positionManager.getEffectiveY(node) + dragOffsetY;
            
            // Check if node is within visible area and being hovered
            if (nodeX + 32 >= draggableX && nodeX <= draggableX + DRAGGABLE_AREA_WIDTH &&
                nodeY + 32 >= draggableY && nodeY <= draggableY + DRAGGABLE_AREA_HEIGHT) {
                
                boolean isHovered = mouseX >= nodeX && mouseX <= nodeX + 32 && mouseY >= nodeY && mouseY <= nodeY + 32;
                
                if (isHovered) {
                    renderTooltip(guiGraphics, node, researchData, mouseX, mouseY);
                }
            }
        }
    }
    
    private void renderTooltip(GuiGraphics guiGraphics, ResearchNode node, ResearchData researchData, int mouseX, int mouseY) {
        List<TooltipLine> tooltipLines = new ArrayList<>();
        
        boolean isUnlocked = researchData.getUnlockedResearch().contains(node.getId());
        boolean prerequisitesUnlocked = hasPrerequisitesUnlocked(node, researchData);
        
        // Add research name (show obfuscated text if prerequisites not unlocked)
        if (prerequisitesUnlocked || isUnlocked) {
            tooltipLines.add(new TooltipLine(node.getDisplayName(), null));
        } else {
            // Generate obfuscated text matching the length of the display name
            String displayName = node.getDisplayName().getString();
            String obfuscatedText = generateObfuscatedText(displayName);
            tooltipLines.add(new TooltipLine(Component.literal(obfuscatedText), null));
        }
        
        // Add unlock status
        if (isUnlocked) {
            tooltipLines.add(new TooltipLine(Component.translatable("gui.strangematter.research_tablet.unlocked").withStyle(style -> style.withColor(0x00FF00)), null));
        } else {
            tooltipLines.add(new TooltipLine(Component.translatable("gui.strangematter.research_tablet.locked").withStyle(style -> style.withColor(0xFF0000)), null));
            
            // Add research point costs (only if prerequisites are unlocked and there are costs)
            if (prerequisitesUnlocked && !node.getResearchCosts().isEmpty()) {
                for (Map.Entry<ResearchType, Integer> cost : node.getResearchCosts().entrySet()) {
                    int playerPoints = researchData.getResearchPoints(cost.getKey());
                    int requiredPoints = cost.getValue();
                    boolean canAfford = playerPoints >= requiredPoints;
                    String color = canAfford ? "§a" : "§c";
                    
                    Component costLine = Component.literal(color + "• " + requiredPoints);
                    tooltipLines.add(new TooltipLine(costLine, cost.getKey()));
                }
            }
        }
        
        // Add description with text wrapping (show question marks if prerequisites not unlocked)
        String descriptionText = node.getDisplayDescription().getString();
        if (!descriptionText.isEmpty()) {
            if (prerequisitesUnlocked || isUnlocked) {
                List<Component> wrappedDescription = wrapText(Component.literal("§7" + descriptionText), 200);
                for (Component descLine : wrappedDescription) {
                    tooltipLines.add(new TooltipLine(descLine, null));
                }
            } else {
                // Generate obfuscated text that respects word boundaries and wrapping
                List<Component> wrappedObfuscatedText = generateWrappedObfuscatedText(descriptionText, 200);
                for (Component descLine : wrappedObfuscatedText) {
                    tooltipLines.add(new TooltipLine(descLine, null));
                }
            }
        }
        
        // Calculate tooltip dimensions
        int maxWidth = 0;
        for (TooltipLine line : tooltipLines) {
            maxWidth = Math.max(maxWidth, this.font.width(line.text));
        }
        
        int tooltipWidth = maxWidth + 8;
        int tooltipHeight = tooltipLines.size() * 12 + 8;
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;
        
        // Ensure tooltip stays on screen
        if (tooltipX + tooltipWidth > this.width) {
            tooltipX = mouseX - tooltipWidth - 12;
        }
        if (tooltipY + tooltipHeight > this.height) {
            tooltipY = mouseY - tooltipHeight - 12;
        }
        
        // Render tooltip background
        guiGraphics.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, 0xC0100010);
        guiGraphics.renderOutline(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 0xFF251837);
        
        // Render tooltip text and icons together
        int textY = tooltipY + 4;
        
        for (TooltipLine line : tooltipLines) {
            if (line.researchType != null) {
                // Render icon first, then text offset to the right
                ResourceLocation iconTexture = line.researchType.getIconResourceLocation();
                RenderSystem.setShaderTexture(0, iconTexture);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                guiGraphics.blit(iconTexture, tooltipX + 4, textY, 0, 0, 8, 8, 8, 8);
                
                // Render text offset to the right of the icon
                guiGraphics.drawString(this.font, line.text, tooltipX + 16, textY, 0xFFFFFF);
            } else {
                // Regular text line
                guiGraphics.drawString(this.font, line.text, tooltipX + 4, textY, 0xFFFFFF);
            }
            
            textY += 12;
        }
    }
    
    // Helper class to store tooltip line data
    private static class TooltipLine {
        final Component text;
        final ResearchType researchType;
        
        TooltipLine(Component text, ResearchType researchType) {
            this.text = text;
            this.researchType = researchType;
        }
    }
    
    private List<Component> wrapText(Component text, int maxWidth) {
        List<Component> wrappedLines = new ArrayList<>();
        String textString = text.getString();
        
        // Extract formatting codes from the beginning of the text
        String formattingCode = "";
        if (textString.startsWith("§")) {
            int codeEnd = 2; // Formatting codes are 2 characters (e.g., "§7")
            if (textString.length() > codeEnd) {
                formattingCode = textString.substring(0, codeEnd);
                textString = textString.substring(codeEnd); // Remove formatting code for processing
            }
        }
        
        // Split by words
        String[] words = textString.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            
            if (this.font.width(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                // Current line is full, start a new one
                if (currentLine.length() > 0) {
                    // Add formatting code to the beginning of each line
                    Component wrappedLine = Component.literal(formattingCode + currentLine.toString());
                    wrappedLines.add(wrappedLine);
                    currentLine = new StringBuilder(word);
                } else {
                    // Single word is too long, just add it anyway with formatting
                    Component wrappedLine = Component.literal(formattingCode + word);
                    wrappedLines.add(wrappedLine);
                }
            }
        }
        
        // Add the last line if it's not empty
        if (currentLine.length() > 0) {
            Component wrappedLine = Component.literal(formattingCode + currentLine.toString());
            wrappedLines.add(wrappedLine);
        }
        
        return wrappedLines;
    }
    
    
    private void renderResearchPointsBackground(GuiGraphics guiGraphics) {
        // Calculate the area needed for research points display
        int startX = guiX + GUI_WIDTH - 110;
        int startY = guiY + 25;
        
        // Calculate dimensions based on the research points content
        // Title: "Research:" + 6 research types with spacing
        int width = 95; // Width of the research points area
        int height = 10 + (ResearchType.values().length * 9) + 5; // Title + all types + padding
        
        // Render semi-transparent black background
        guiGraphics.fill(startX - 5, startY - 5, startX + width, startY + height, 0x80000000);
    }
    
    private void renderResearchPoints(GuiGraphics guiGraphics) {
        ResearchData researchData = ResearchDataClientHandler.getClientResearchData();
        int startX = guiX + GUI_WIDTH - 110;
        int startY = guiY + 25;
        
        // Render smaller title
        guiGraphics.drawString(this.font, "Research:", startX, startY, 0xFFFFFF);
        
        int yOffset = 10;
        for (ResearchType type : ResearchType.values()) {
            int points = researchData.getResearchPoints(type);
            
            // Render smaller research type icon (8x8 instead of 12x12)
            ResourceLocation icon = type.getIconResourceLocation();
            guiGraphics.blit(icon, startX, startY + yOffset, 0, 0, 8, 8, 8, 8);
            
            // Render full research type name + points
            Component text = Component.literal(type.getName() + ": " + points);
            guiGraphics.drawString(this.font, text, startX + 10, startY + yOffset + 1, 0xFFFFFF);
            
            yOffset += 9; // Reduced spacing from 14 to 9
        }
    }
    
    private Map<ResearchType, Integer> getPlayerResearchPoints(ResearchData researchData) {
        Map<ResearchType, Integer> points = new HashMap<>();
        for (ResearchType type : ResearchType.values()) {
            points.put(type, researchData.getResearchPoints(type));
        }
        return points;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int draggableX = guiX + 1 + (GUI_WIDTH - DRAGGABLE_AREA_WIDTH) / 2;
            int draggableY = guiY + 15;
            
            // Check if click is within draggable area
            if (mouseX >= draggableX && mouseX <= draggableX + DRAGGABLE_AREA_WIDTH &&
                mouseY >= draggableY && mouseY <= draggableY + DRAGGABLE_AREA_HEIGHT) {
                
                // First check for research node clicks
                ResearchNode clickedNode = getClickedNode(mouseX, mouseY);
                if (clickedNode != null) {
                    if (debugMode) {
                        // In debug mode, start dragging the node
                        draggedNode = clickedNode;
                        return true;
                    } else {
                        // Normal mode: handle research node clicks
                        ResearchData researchData = ResearchDataClientHandler.getClientResearchData();
                        boolean isUnlocked = researchData.getUnlockedResearch().contains(clickedNode.getId());
                        
                        if (isUnlocked) {
                            // Open information page for unlocked node
                            openNodeInformationPage(clickedNode);
                            // Play node click sound
                            minecraft.player.playSound(StrangeMatterSounds.RESEARCH_TABLET_NODE_CLICK.get(), 0.7f, 1.0f);
                            return true;
                        } else {
                            // Check if player can afford this research
                            boolean canAfford = true;
                            for (Map.Entry<ResearchType, Integer> entry : clickedNode.getResearchCosts().entrySet()) {
                                ResearchType type = entry.getKey();
                                int cost = entry.getValue();
                                int currentPoints = researchData.getResearchPoints(type);
                                if (currentPoints < cost) {
                                    canAfford = false;
                                    break;
                                }
                            }
                            
                            if (canAfford) {
                                // Spend research points and give research note
                                spendResearchPointsAndGiveNote(clickedNode);
                                // Play research note create sound
                                minecraft.player.playSound(StrangeMatterSounds.RESEARCH_NOTE_CREATE.get(), 0.8f, 1.0f);
                                return true;
                            } else {
                                // Show message that player can't afford it and play locked sound
                                minecraft.player.sendSystemMessage(Component.translatable("research.strangematter.cannot_afford"));
                                minecraft.player.playSound(StrangeMatterSounds.RESEARCH_NODE_LOCKED_CLICK.get(), 0.6f, 1.0f);
                                return true;
                            }
                        }
                    }
                }
                
                // If no node was clicked, start dragging the view (panning works in both modes)
                this.isDragging = true;
                this.lastMouseX = (int) mouseX;
                this.lastMouseY = (int) mouseY;
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            if (debugMode && draggedNode != null) {
                // Dragging a node in debug mode
                int draggableX = guiX + 1 + (GUI_WIDTH - DRAGGABLE_AREA_WIDTH) / 2;
                int draggableY = guiY + 15;
                
                // Convert mouse position to research coordinate space
                int researchX = (int) mouseX - draggableX - DRAGGABLE_AREA_WIDTH / 2 - dragOffsetX;
                int researchY = (int) mouseY - draggableY - DRAGGABLE_AREA_HEIGHT / 2 - dragOffsetY;
                
                // Update node position (will be snapped to grid by position manager)
                positionManager.setPosition(draggedNode.getId(), researchX, researchY);
                
                return true;
            } else if (isDragging) {
                // Dragging the view (panning - works in both normal and debug mode)
                int deltaXInt = (int) (mouseX - lastMouseX);
                int deltaYInt = (int) (mouseY - lastMouseY);
                
                this.dragOffsetX += deltaXInt;
                this.dragOffsetY += deltaYInt;
                
                this.lastMouseX = (int) mouseX;
                this.lastMouseY = (int) mouseY;
                return true;
            }
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (debugMode && draggedNode != null) {
                // Finished dragging node - save positions
                positionManager.savePositions();
                draggedNode = null;
                this.isDragging = false;
                return true;
            }
            this.isDragging = false;
            return true;
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void tick() {
        super.tick();
        refreshCounter++;
        
        // Refresh display every 20 ticks (1 second) to pick up research data changes
        if (refreshCounter % 20 == 0) {
            // Force a re-render to pick up any changes in research data
        }
    }
    
    private void spendResearchPointsAndGiveNote(ResearchNode node) {
        // Send packet to server to spend research points and create research note
        com.hexvane.strangematter.network.SpendResearchPointsPacket packet = 
            new com.hexvane.strangematter.network.SpendResearchPointsPacket(node.getResearchCosts(), node.getId());
        
        com.hexvane.strangematter.network.NetworkHandler.INSTANCE.sendToServer(packet);
    }
}
