package com.hexvane.strangematter.kubejs;

import com.hexvane.strangematter.research.ResearchNode;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for custom research nodes and info pages added through KubeJS.
 * This allows modpack creators to add their own research content.
 */
public class CustomResearchRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, List<ResearchInfoPage>> customPages = new HashMap<>();
    private static final List<ResearchNode> customNodes = new ArrayList<>();
    // Keep permanent copies that don't get cleared on reset
    private static final List<ResearchNode> permanentCustomNodes = new ArrayList<>();
    private static final Map<String, List<ResearchInfoPage>> permanentCustomPages = new HashMap<>();
    private static boolean initialized = false;
    
    /**
     * Register a custom research node.
     * This should be called during KubeJS initialization.
     */
    public static void registerNode(ResearchNode node) {
        // Always add to permanent list (survives resets)
        if (!permanentCustomNodes.contains(node)) {
            permanentCustomNodes.add(node);
        }
        // Also add to working list if not initialized yet
        if (!initialized) {
            customNodes.add(node);
        }
    }
    
    /**
     * Register info pages for a research node.
     * The nodeId should match the ID of a research node (vanilla or custom).
     */
    public static void registerInfoPages(String nodeId, List<ResearchInfoPage> pages) {
        LOGGER.info("[Strange Matter KubeJS] registerInfoPages called for {} with {} pages", nodeId, (pages != null ? pages.size() : 0));
        if (!initialized) {
            customPages.put(nodeId, new ArrayList<>(pages));
            // Also store in permanent map so it survives resets
            permanentCustomPages.put(nodeId, new ArrayList<>(pages));
            LOGGER.info("[Strange Matter KubeJS] Registered {} pages for {}", pages.size(), nodeId);
        } else {
            LOGGER.warn("[Strange Matter KubeJS] Cannot register pages - already initialized!");
        }
    }
    
    /**
     * Get custom info pages for a specific research node.
     * Returns null if no custom pages are registered.
     * Always checks permanent storage first.
     */
    public static List<ResearchInfoPage> getInfoPages(String nodeId) {
        // Always return from permanent storage (survives resets)
        return permanentCustomPages.get(nodeId);
    }
    
    /**
     * Check if a research node has custom info pages.
     * Always checks permanent storage.
     */
    public static boolean hasCustomPages(String nodeId) {
        return permanentCustomPages.containsKey(nodeId);
    }
    
    /**
     * Get all custom research nodes.
     */
    public static List<ResearchNode> getCustomNodes() {
        return new ArrayList<>(customNodes);
    }
    
    /**
     * Initialize all custom research nodes.
     * This should be called after vanilla nodes are initialized
     * but before the research system is used.
     */
    public static void initializeCustomResearch() {
        // Always re-register from the permanent list
        // This handles cases where the main registry was cleared
        for (ResearchNode node : permanentCustomNodes) {
            com.hexvane.strangematter.research.ResearchNodeRegistry.register(node);
        }
        
        initialized = true;
        
        LOGGER.info("[Strange Matter KubeJS] Initialized {} custom research nodes", permanentCustomNodes.size());
    }
    
    /**
     * Reset the registry (useful for reloading).
     * Note: This does NOT clear permanentCustomNodes - those persist across resets.
     */
    public static void reset() {
        customPages.clear();
        customNodes.clear();
        initialized = false;
        // Do NOT clear permanent storage - they need to survive resets
        LOGGER.info("[Strange Matter KubeJS] Reset custom research registry (keeping {} permanent nodes and {} page sets)", 
            permanentCustomNodes.size(), permanentCustomPages.size());
    }
    
    /**
     * Check if custom research has been initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }
}

