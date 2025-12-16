package com.hexvane.strangematter.client.research;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hexvane.strangematter.research.ResearchNode;
import com.hexvane.strangematter.research.ResearchNodeRegistry;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Client-side manager for research node position overrides.
 * Allows saving and loading custom node positions for debug/layout purposes.
 */
@OnlyIn(Dist.CLIENT)
public class ResearchNodePositionManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int GRID_SIZE = 80;
    private static final String CONFIG_DIR = "config/strangematter";
    private static final String POSITIONS_FILE = "research_node_positions.json";
    
    private static ResearchNodePositionManager instance;
    private final Map<String, NodePosition> positionOverrides = new HashMap<>();
    private boolean positionsLoaded = false;
    
    private ResearchNodePositionManager() {
    }
    
    public static ResearchNodePositionManager getInstance() {
        if (instance == null) {
            instance = new ResearchNodePositionManager();
        }
        return instance;
    }
    
    /**
     * Get the effective X coordinate for a node (override if exists, otherwise original).
     */
    public int getEffectiveX(ResearchNode node) {
        if (node == null) return 0;
        NodePosition override = positionOverrides.get(node.getId());
        return override != null ? override.x : node.getX();
    }
    
    /**
     * Get the effective Y coordinate for a node (override if exists, otherwise original).
     */
    public int getEffectiveY(ResearchNode node) {
        if (node == null) return 0;
        NodePosition override = positionOverrides.get(node.getId());
        return override != null ? override.y : node.getY();
    }
    
    /**
     * Get the effective X coordinate for a node by ID.
     */
    public int getEffectiveX(String nodeId) {
        ResearchNode node = ResearchNodeRegistry.getNode(nodeId);
        if (node == null) return 0;
        return getEffectiveX(node);
    }
    
    /**
     * Get the effective Y coordinate for a node by ID.
     */
    public int getEffectiveY(String nodeId) {
        ResearchNode node = ResearchNodeRegistry.getNode(nodeId);
        if (node == null) return 0;
        return getEffectiveY(node);
    }
    
    /**
     * Set a position override for a node.
     * The position will be snapped to the grid.
     */
    public void setPosition(String nodeId, int x, int y) {
        int snappedX = snapToGrid(x);
        int snappedY = snapToGrid(y);
        positionOverrides.put(nodeId, new NodePosition(snappedX, snappedY));
    }
    
    /**
     * Remove a position override, reverting to original position.
     */
    public void clearPosition(String nodeId) {
        positionOverrides.remove(nodeId);
    }
    
    /**
     * Check if a node has a position override.
     */
    public boolean hasOverride(String nodeId) {
        return positionOverrides.containsKey(nodeId);
    }
    
    /**
     * Snap a coordinate to the grid.
     */
    public static int snapToGrid(int value) {
        return (int) Math.round(value / (double) GRID_SIZE) * GRID_SIZE;
    }
    
    /**
     * Get the grid size.
     */
    public static int getGridSize() {
        return GRID_SIZE;
    }
    
    /**
     * Load saved positions from file.
     * Should be called after all nodes are registered (including KubeJS nodes).
     */
    public void loadPositions() {
        if (positionsLoaded) {
            return; // Already loaded
        }
        
        Path configPath = Paths.get(CONFIG_DIR);
        Path filePath = configPath.resolve(POSITIONS_FILE);
        
        if (!Files.exists(filePath)) {
            LOGGER.debug("[Research Debug] No saved positions file found at {}", filePath);
            positionsLoaded = true;
            return;
        }
        
        try (FileReader reader = new FileReader(filePath.toFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            
            if (json.has("nodes") && json.get("nodes").isJsonObject()) {
                JsonObject nodesObj = json.getAsJsonObject("nodes");
                int loadedCount = 0;
                
                for (String nodeId : nodesObj.keySet()) {
                    JsonObject nodePos = nodesObj.getAsJsonObject(nodeId);
                    if (nodePos.has("x") && nodePos.has("y")) {
                        int x = nodePos.get("x").getAsInt();
                        int y = nodePos.get("y").getAsInt();
                        
                        // Only apply if the node exists
                        if (ResearchNodeRegistry.getNode(nodeId) != null) {
                            positionOverrides.put(nodeId, new NodePosition(x, y));
                            loadedCount++;
                        } else {
                            LOGGER.debug("[Research Debug] Skipping position for unknown node: {}", nodeId);
                        }
                    }
                }
                
                LOGGER.info("[Research Debug] Loaded {} node position overrides from {}", loadedCount, filePath);
            }
        } catch (Exception e) {
            LOGGER.error("[Research Debug] Failed to load positions from {}", filePath, e);
        }
        
        positionsLoaded = true;
    }
    
    /**
     * Save current position overrides to file.
     */
    public void savePositions() {
        Path configPath = Paths.get(CONFIG_DIR);
        Path filePath = configPath.resolve(POSITIONS_FILE);
        
        try {
            // Create config directory if it doesn't exist
            Files.createDirectories(configPath);
            
            JsonObject root = new JsonObject();
            JsonObject nodesObj = new JsonObject();
            
            for (Map.Entry<String, NodePosition> entry : positionOverrides.entrySet()) {
                JsonObject nodePos = new JsonObject();
                nodePos.addProperty("x", entry.getValue().x);
                nodePos.addProperty("y", entry.getValue().y);
                nodesObj.add(entry.getKey(), nodePos);
            }
            
            root.add("nodes", nodesObj);
            
            // Write to file with pretty printing
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                gson.toJson(root, writer);
            }
            
            LOGGER.info("[Research Debug] Saved {} node position overrides to {}", positionOverrides.size(), filePath);
        } catch (IOException e) {
            LOGGER.error("[Research Debug] Failed to save positions to {}", filePath, e);
        }
    }
    
    /**
     * Clear all position overrides.
     */
    public void clearAll() {
        positionOverrides.clear();
    }
    
    /**
     * Simple position holder class.
     */
    private static class NodePosition {
        final int x;
        final int y;
        
        NodePosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
