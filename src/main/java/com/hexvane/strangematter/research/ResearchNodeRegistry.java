package com.hexvane.strangematter.research;

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
        // Clear existing nodes
        nodes.clear();
        nodesByCategory.clear();
        
        // Foundation research - basic tools and understanding (unlocked by default)
        register(new ResearchNode(
            "foundation",
            "Foundation Research",
            "Basic understanding of anomaly physics and containment principles.",
            "general",
            0, 0,
            Map.of(), // No costs - unlocked by default
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.RESEARCH_NOTES.get()),
            true,
            List.of() // No prerequisites - this is the starting node
        ));
        
        // Basic scanner research (unlocked by default)
        register(new ResearchNode(
            "basic_scanner",
            "Resonance Scanner",
            "A device for detecting and analyzing anomaly signatures in the field.",
            "general",
            -100, -50,
            Map.of(), // No costs - unlocked by default
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(com.hexvane.strangematter.StrangeMatterMod.ANOMALY_RESONATOR.get()),
            false,
            List.of("foundation") // Requires foundation research
        ));
        
        // Gravity research branch
        register(new ResearchNode(
            "gravity_control",
            "Gravitational Manipulation",
            "Understanding and controlling gravitational forces within anomaly fields.",
            "general",
            -150, 100,
            Map.of(ResearchType.GRAVITY, 20, ResearchType.ENERGY, 10),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(Items.IRON_INGOT),
            true,
            List.of("foundation") // Requires foundation research
        ));
        
        // Time research branch
        register(new ResearchNode(
            "temporal_stability",
            "Temporal Stability",
            "Methods for stabilizing temporal distortions and controlling time flow.",
            "general",
            150, 100,
            Map.of(ResearchType.TIME, 25, ResearchType.SPACE, 15),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(Items.CLOCK),
            true,
            List.of("foundation") // Requires foundation research
        ));
        
        // Space research branch
        register(new ResearchNode(
            "spatial_anchoring",
            "Spatial Anchoring",
            "Techniques for anchoring spatial distortions and creating stable portals.",
            "general",
            100, -100,
            Map.of(ResearchType.SPACE, 20, ResearchType.ENERGY, 15),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(Items.ENDER_PEARL),
            true,
            List.of("basic_scanner") // Requires basic scanner
        ));
        
        // Shadow research branch
        register(new ResearchNode(
            "shadow_manipulation",
            "Shadow Manipulation",
            "Understanding and harnessing shadow-based anomalies and perception effects.",
            "general",
            -100, -150,
            Map.of(ResearchType.SHADOW, 18, ResearchType.COGNITION, 12),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(Items.INK_SAC),
            true,
            List.of("basic_scanner") // Requires basic scanner
        ));
        
        // Energy research branch
        register(new ResearchNode(
            "resonant_energy",
            "Resonant Energy Systems",
            "Advanced systems for harnessing and stabilizing resonant energy from anomalies.",
            "general",
            0, 150,
            Map.of(ResearchType.ENERGY, 30, ResearchType.GRAVITY, 10),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(Items.GLOWSTONE),
            true,
            List.of("gravity_control", "temporal_stability") // Requires both gravity and temporal research
        ));
        
        // Cognition research branch
        register(new ResearchNode(
            "cognitive_interface",
            "Cognitive Interface",
            "Advanced mental interfaces for interacting with anomaly phenomena.",
            "general",
            -150, -100,
            Map.of(ResearchType.COGNITION, 25, ResearchType.SHADOW, 10),
            ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png"),
            new ItemStack(Items.EMERALD),
            true,
            List.of("shadow_manipulation") // Requires shadow manipulation
        ));
    }
}
