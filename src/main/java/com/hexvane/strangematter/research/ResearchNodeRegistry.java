package com.hexvane.strangematter.research;

import com.hexvane.strangematter.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class ResearchNodeRegistry {
    private static final Map<String, ResearchNode> nodes = new HashMap<>();
    private static final Map<String, List<ResearchNode>> nodesByCategory = new HashMap<>();
    
    public static void register(ResearchNode node) {
        nodes.put(node.getId(), node);
        nodesByCategory.computeIfAbsent(node.getCategory(), k -> new ArrayList<>()).add(node);
    }
    
    /**
     * Apply config multiplier and overrides to research costs
     */
    private static Map<ResearchType, Integer> applyConfigCosts(String nodeId, Map<ResearchType, Integer> defaultCosts) {
        if (defaultCosts.isEmpty()) {
            return defaultCosts; // Don't modify unlocked nodes
        }
        
        // Check for individual override first
        int overrideCost = getIndividualCostOverride(nodeId);
        Map<ResearchType, Integer> costs = new HashMap<>(defaultCosts);
        
        if (overrideCost > 0) {
            // Apply individual override - distribute evenly among research types
            int costPerType = Math.max(1, overrideCost / costs.size());
            for (ResearchType type : costs.keySet()) {
                costs.put(type, costPerType);
            }
        } else {
            // Apply global multiplier
            double multiplier = Config.researchCostMultiplier;
            for (Map.Entry<ResearchType, Integer> entry : costs.entrySet()) {
                int newCost = Math.max(1, (int) Math.round(entry.getValue() * multiplier));
                costs.put(entry.getKey(), newCost);
            }
        }
        
        return costs;
    }
    
    /**
     * Get individual cost override for a specific node
     */
    private static int getIndividualCostOverride(String nodeId) {
        return switch (nodeId) {
            case "anomaly_resonator" -> Config.anomalyResonatorCost;
            case "resonance_condenser" -> Config.resonanceCondenserCost;
            case "containment_basics" -> Config.containmentBasicsCost;
            case "echoform_imprinter" -> Config.echoformImprinterCost;
            case "reality_forge" -> Config.realityForgeCost;
            case "warp_gun" -> Config.warpGunCost;
            case "graviton_hammer" -> Config.gravitonHammerCost;
            case "stasis_projector" -> Config.stasisProjectorCost;
            case "rift_stabilizer" -> Config.riftStabilizerCost;
            case "gravity_anomalies" -> Config.gravityAnomaliesCost;
            case "temporal_anomalies" -> Config.temporalAnomaliesCost;
            case "spatial_anomalies" -> Config.spatialAnomaliesCost;
            case "energy_anomalies" -> Config.energyAnomaliesCost;
            case "shadow_anomalies" -> Config.shadowAnomaliesCost;
            case "cognitive_anomalies" -> Config.cognitiveAnomaliesCost;
            case "levitation_pad" -> Config.levitationPadCost;
            case "hoverboard" -> Config.hoverboardCost;
            default -> -1; // Use default
        };
    }
    
    public static ResearchNode getNode(String id) {
        return nodes.get(id);
    }
    
    public static List<ResearchNode> getNodesByCategory(String category) {
        return nodesByCategory.getOrDefault(category, Collections.emptyList());
    }
    
    public static List<String> getCategories() {
        return new ArrayList<>(nodesByCategory.keySet());
    }
    
    public static Collection<ResearchNode> getAllNodes() {
        return nodes.values();
    }
    
    public static void initializeDefaultNodes() {
        // Clear existing nodes and categories
        nodes.clear();
        nodesByCategory.clear();
        ResearchCategoryRegistry.clear();
        
        // Initialize default categories
        initializeDefaultCategories();
        
        // Also reset custom research registry when reloading
        try {
            Class<?> customRegistry = Class.forName("com.hexvane.strangematter.kubejs.CustomResearchRegistry");
            customRegistry.getMethod("reset").invoke(null);
        } catch (Exception e) {
            // KubeJS not loaded or class not found - this is fine
        }
        
        /*
         * Research Tree Layout:
         * - Default unlocked research forms a central hub around "Research"
         * - Reality Forge is the gateway to advanced crafting
         * - Anomaly type specializations branch from "Anomaly Types"
         * - All connections are within reasonable distances to prevent line disappearing
         */
        
        // ===== DEFAULT UNLOCKED RESEARCH (Center Group) =====
        
        // Main research introduction node (unlocked by default) - CENTER
        register(new ResearchNode(
            "research",
            "general",
            0, 0,
            Map.of(), // No costs - unlocked by default
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.RESEARCH_NOTES.get()),
            true,
            List.of() // No prerequisites - this is the starting node
        ));
        
        // Field Scanner tool (unlocked by default) - LEFT
        register(new ResearchNode(
            "field_scanner",
            "general",
            -80, 0,
            Map.of(), // No costs - unlocked by default
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.FIELD_SCANNER.get()),
            false,
            List.of("research") // Requires main research
        ));
        
        // Anomaly shard ore generation and collection (unlocked by default) - RIGHT
        register(new ResearchNode(
            "anomaly_shards",
            "general",
            80, 0,
            Map.of(), // No costs - unlocked by default
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.GRAVITIC_SHARD.get()),
            true,
            List.of("research") // Requires main research
        ));
        
        // Understanding different anomaly types (unlocked by default) - TOP
        register(new ResearchNode(
            "anomaly_types",
            "general",
            0, -80,
            Map.of(), // No costs - unlocked by default
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(Items.BOOK),
            true,
            List.of("research") // Requires main research
        ));
        
        // Resonite ore collection and usage (unlocked by default) - BOTTOM LEFT
        register(new ResearchNode(
            "resonite",
            "general",
            -80, 80,
            Map.of(), // No costs - unlocked by default
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.RAW_RESONITE.get()),
            true,
            List.of("research") // Requires main research
        ));
        
        // Tinfoil Hat protective armor (locked) - Connected to resonite
        register(new ResearchNode(
            "tinfoil_hat",
            "general",
            -160, 80,
            applyConfigCosts("tinfoil_hat", Map.of(ResearchType.ENERGY, 10, ResearchType.COGNITION, 10)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.TINFOIL_HAT.get()),
            true,
            List.of("resonite") // Requires resonite research
        ));
        
        // Resonant Power description and Resonant Burner power generation (unlocked by default) - BOTTOM RIGHT
        register(new ResearchNode(
            "resonant_energy",
            "general",
            80, 80,
            Map.of(), // No costs - unlocked by default
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.RESONANT_BURNER_ITEM.get()),
            true,
            List.of("research") // Requires main research
        ));
        
        // ===== LOCKED RESEARCH (Connected to unlocked nodes) =====
        
        // Anomaly Resonator tool (locked) - Connected to field_scanner
        register(new ResearchNode(
            "anomaly_resonator",
            "general",
            -160, 0,
            applyConfigCosts("anomaly_resonator", Map.of(ResearchType.ENERGY, 10, ResearchType.SPACE, 10)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.ANOMALY_RESONATOR.get()),
            true,
            List.of("field_scanner") // Requires field scanner
        ));
        
        // Reality Forge machine (locked) - Connected to resonite and anomaly_types
        // This stays in "general" category
        register(new ResearchNode(
            "reality_forge",
            "general",
            -80, 240,
            applyConfigCosts("reality_forge", Map.of(ResearchType.ENERGY, 5, ResearchType.SPACE, 5, ResearchType.TIME, 5)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.REALITY_FORGE_ITEM.get()),
            true,
            List.of("resonite") // Requires resonite and anomaly types
        ));
        
        // ===== REALITY FORGE CATEGORY NODES =====
        // Only nodes that directly require reality_forge are moved to this category
        // All prerequisites stay as "reality_forge" to maintain connections
        
        // Duplicate Reality Forge node in reality_forge category (same position as original, auto-unlocks when category unlocks)
        register(new ResearchNode(
            "reality_forge_category",
            "reality_forge",
            -80, 240, // Same position as original reality_forge node
            applyConfigCosts("reality_forge", Map.of(ResearchType.ENERGY, 5, ResearchType.SPACE, 5, ResearchType.TIME, 5)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.REALITY_FORGE_ITEM.get()),
            true,
            List.of() // No prerequisites - auto-unlocks when category becomes visible
        ));
        
        // Resonance Condenser machine (locked) - Connected to reality_forge_category
        register(new ResearchNode(
            "resonance_condenser",
            "reality_forge",
            -160, 160,
            applyConfigCosts("resonance_condenser", Map.of(ResearchType.ENERGY, 25, ResearchType.SPACE, 15)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.RESONANCE_CONDENSER_ITEM.get()),
            true,
            List.of("reality_forge_category") // Requires reality_forge_category node in this category
        ));
        
        // Echo Vacuum tool and Containment capsule system (locked) - Connected to reality_forge_category
        register(new ResearchNode(
            "containment_basics",
            "reality_forge",
            -80, 400,
            applyConfigCosts("containment_basics", Map.of(ResearchType.ENERGY, 20, ResearchType.SHADOW, 15)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.ECHO_VACUUM.get()),
            true,
            List.of("reality_forge_category") // Requires reality_forge_category node in this category
        ));
        
        // Echoform Imprinter tool (locked) - Connected to containment_basics (in reality_forge category)
        register(new ResearchNode(
            "echoform_imprinter",
            "reality_forge",
            80, 400,
            applyConfigCosts("echoform_imprinter", Map.of(ResearchType.SHADOW, 25, ResearchType.COGNITION, 20)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.ECHOFORM_IMPRINTER.get()),
            true,
            List.of("containment_basics") // Requires containment_basics
        ));
        
        // Warp Gun weapon (locked) - Connected to containment_basics (in reality_forge category)
        register(new ResearchNode(
            "warp_gun",
            "reality_forge",
            0, 320,
            applyConfigCosts("warp_gun", Map.of(ResearchType.SPACE, 15, ResearchType.ENERGY, 10)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.WARP_GUN.get()),
            true,
            List.of("containment_basics") // Requires containment_basics
        ));
        
        // Chrono Blister gadget (locked) - Connected to temporal_anomalies (in reality_forge category)
        register(new ResearchNode(
            "chrono_blister",
            "reality_forge",
            80, 320,
            applyConfigCosts("chrono_blister", Map.of(ResearchType.TIME, 15, ResearchType.ENERGY, 10)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.CHRONO_BLISTER.get()),
            true,
            List.of("containment_basics") // Requires containment_basics
        ));
        
        // Graviton Hammer tool (locked) - Connected to containment_basics (in reality_forge category)
        register(new ResearchNode(
            "graviton_hammer",
            "reality_forge",
            -80, 480,
            applyConfigCosts("graviton_hammer", Map.of(ResearchType.GRAVITY, 20, ResearchType.ENERGY, 5)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.GRAVITON_HAMMER.get()),
            true,
            List.of("containment_basics") // Requires containment_basics
        ));
        
        // Stasis Projector device (locked) - Connected to reality_forge_category
        register(new ResearchNode(
            "stasis_projector",
            "reality_forge",
            80, 240,
            applyConfigCosts("stasis_projector", Map.of(ResearchType.GRAVITY, 5, ResearchType.TIME, 5)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.STASIS_PROJECTOR_ITEM.get()),
            true,
            List.of("reality_forge_category") // Requires reality_forge_category node in this category
        ));
        
        register(new ResearchNode(
            "rift_stabilizer",
            "reality_forge",
            0, 160,
            applyConfigCosts("rift_stabilizer", Map.of(ResearchType.ENERGY, 20, ResearchType.SPACE, 10)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.RIFT_STABILIZER_ITEM.get()),
            true,
            List.of("reality_forge_category") // Requires reality_forge_category node in this category
        ));
        
        // Levitation Pad device (locked) - Connected to reality_forge_category
        register(new ResearchNode(
            "levitation_pad",
            "reality_forge",
            -240, 240,
            applyConfigCosts("levitation_pad", Map.of(ResearchType.GRAVITY, 15, ResearchType.ENERGY, 10)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.LEVITATION_PAD_ITEM.get()),
            true,
            List.of("reality_forge_category") // Requires reality_forge_category node in this category
        ));
        
        // Hoverboard vehicle (locked) - Connected to containment_basics (in reality_forge category)
        register(new ResearchNode(
            "hoverboard",
            "reality_forge",
            -240, 400,
            applyConfigCosts("hoverboard", Map.of(ResearchType.GRAVITY, 10, ResearchType.ENERGY, 15)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.HOVERBOARD.get()),
            true,
            List.of("containment_basics") // Requires containment_basics
        ));
        
        // ===== ANOMALY TYPES (SPECIALIZED RESEARCH) =====
        
        // Gravity anomaly research (locked) - Connected to anomaly_types
        register(new ResearchNode(
            "gravity_anomalies",
            "general",
            -80, -160,
            applyConfigCosts("gravity_anomalies", Map.of(ResearchType.GRAVITY, 5)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.GRAVITIC_SHARD.get()),
            true,
            List.of("anomaly_types") // Requires anomaly types
        ));
        
        // Temporal anomaly research (locked) - Connected to anomaly_types
        register(new ResearchNode(
            "temporal_anomalies",
            "general",
            0, -160,
            applyConfigCosts("temporal_anomalies", Map.of(ResearchType.TIME, 5)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.CHRONO_SHARD.get()),
            true,
            List.of("anomaly_types") // Requires anomaly types
        ));
        
        // Spatial anomaly research (locked) - Connected to anomaly_types
        register(new ResearchNode(
            "spatial_anomalies",
            "general",
            80, -160,
            applyConfigCosts("spatial_anomalies", Map.of(ResearchType.SPACE, 5)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.SPATIAL_SHARD.get()),
            true,
            List.of("anomaly_types") // Requires anomaly types
        ));
        
        // Energy anomaly research (locked) - Connected to anomaly_types
        register(new ResearchNode(
            "energy_anomalies",
            "general",
            -80, -240,
            applyConfigCosts("energy_anomalies", Map.of(ResearchType.ENERGY, 5)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.ENERGETIC_SHARD.get()),
            true,
            List.of("anomaly_types") // Requires anomaly types
        ));
        
        // Shadow anomaly research (locked) - Connected to anomaly_types
        register(new ResearchNode(
            "shadow_anomalies",
            "general",
            0, -240,
            applyConfigCosts("shadow_anomalies", Map.of(ResearchType.SHADOW, 5)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.SHADE_SHARD.get()),
            true,
            List.of("anomaly_types") // Requires anomaly types
        ));
        
        // Cognitive anomaly research (locked) - Connected to anomaly_types
        register(new ResearchNode(
            "cognitive_anomalies",
            "general",
            80, -240,
            applyConfigCosts("cognitive_anomalies", Map.of(ResearchType.COGNITION, 5)),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.INSIGHT_SHARD.get()),
            true,
            List.of("anomaly_types") // Requires anomaly types
        ));
        
        // Initialize custom research nodes from KubeJS (if available)
        try {
            Class<?> customRegistry = Class.forName("com.hexvane.strangematter.kubejs.CustomResearchRegistry");
            customRegistry.getMethod("initializeCustomResearch").invoke(null);
        } catch (Exception e) {
            // KubeJS not loaded or class not found - this is fine
        }
        
        // Initialize custom research nodes from KubeJS (if available)
        try {
            Class<?> customRegistry = Class.forName("com.hexvane.strangematter.kubejs.CustomResearchRegistry");
            customRegistry.getMethod("initializeCustomResearch").invoke(null);
        } catch (Exception e) {
            // KubeJS not loaded or class not found - this is fine
        }
        
        // Initialize custom categories from KubeJS (if available)
        try {
            Class<?> customRegistry = Class.forName("com.hexvane.strangematter.kubejs.CustomResearchRegistry");
            customRegistry.getMethod("initializeCustomCategories").invoke(null);
        } catch (Exception e) {
            // KubeJS not loaded or class not found - this is fine
        }
        
        // Load saved node positions on client side (after all nodes are registered)
        loadSavedPositions();
    }
    
    /**
     * Initialize default research categories.
     */
    private static void initializeDefaultCategories() {
        // General category - always visible, uses research_notes icon
        ResearchCategoryRegistry.register(new ResearchCategory(
            "general",
            Component.translatable("research.category.strangematter.general"),
            null,
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.RESEARCH_NOTES.get()),
            null, // No unlock requirement - always visible
            0 // First category
        ));
        
        // Reality Forge category - hidden until reality_forge research unlocks
        ResearchCategoryRegistry.register(new ResearchCategory(
            "reality_forge",
            Component.translatable("research.category.strangematter.reality_forge"),
            null,
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.REALITY_FORGE_ITEM.get()),
            "reality_forge", // Unlock requirement: reality_forge research node
            1 // Second category
        ));
    }
    
    /**
     * Load saved node positions from file (client-side only).
     * This is called after all nodes are registered, including KubeJS nodes.
     */
    private static void loadSavedPositions() {
        // Only load on client side
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist != net.minecraftforge.api.distmarker.Dist.CLIENT) {
            return;
        }
        
        try {
            Class<?> positionManager = Class.forName("com.hexvane.strangematter.client.research.ResearchNodePositionManager");
            Object instance = positionManager.getMethod("getInstance").invoke(null);
            positionManager.getMethod("loadPositions").invoke(instance);
        } catch (Exception e) {
            // Position manager not available or failed to load - this is fine
            // It might not be available during server-side initialization
        }
    }
}