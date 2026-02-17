package com.hexvane.strangematter.kubejs;

import com.hexvane.strangematter.research.ResearchNode;
import com.hexvane.strangematter.research.ResearchTypeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class for creating research nodes through KubeJS.
 * Provides a fluent API for constructing research nodes with validation.
 */
public class ResearchNodeBuilder {
    private String id;
    private String category = "custom";
    private int x = 0;
    private int y = 0;
    private Map<String, Integer> researchCosts = new HashMap<>();
    private ResourceLocation iconTexture = null;
    private ItemStack iconItem = ItemStack.EMPTY;
    private boolean requiresMultipleAspects = false;
    private List<String> prerequisites = new ArrayList<>();
    
    public ResearchNodeBuilder(String id) {
        this.id = id;
    }
    
    /**
     * Set the category for this research node.
     * Common categories: "general", "basic", "advanced", "anomalies", "custom"
     */
    public ResearchNodeBuilder category(String category) {
        this.category = category;
        return this;
    }
    
    /**
     * Set the X coordinate for the node in the research tree GUI.
     */
    public ResearchNodeBuilder x(int x) {
        this.x = x;
        return this;
    }
    
    /**
     * Set the Y coordinate for the node in the research tree GUI.
     */
    public ResearchNodeBuilder y(int y) {
        this.y = y;
        return this;
    }
    
    /**
     * Set the position (X, Y) for the node in the research tree GUI.
     */
    public ResearchNodeBuilder position(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    /**
     * Add a research cost for a specific type.
     * Built-in: "gravity", "time", "space", "energy", "shadow", "cognition".
     * Custom type ids registered via createResearchPointType() are also valid.
     */
    public ResearchNodeBuilder cost(String typeName, int amount) {
        if (ResearchTypeHelper.isKnownType(typeName) && amount > 0) {
            this.researchCosts.put(typeName, amount);
        }
        return this;
    }
    
    /**
     * Set multiple research costs at once.
     */
    public ResearchNodeBuilder costs(Map<String, Integer> costs) {
        for (Map.Entry<String, Integer> entry : costs.entrySet()) {
            cost(entry.getKey(), entry.getValue());
        }
        return this;
    }
    
    /**
     * Set a texture icon for this research node.
     * Example: "strangematter:textures/ui/custom_icon.png"
     */
    public ResearchNodeBuilder iconTexture(String texturePath) {
        this.iconTexture = ResourceLocation.parse(texturePath);
        return this;
    }
    
    /**
     * Set an item icon for this research node.
     * Example: "minecraft:diamond"
     */
    public ResearchNodeBuilder iconItem(String itemId) {
        try {
            ResourceLocation itemLocation = ResourceLocation.parse(itemId);
            net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(itemLocation)
                .ifPresent(item -> this.iconItem = new ItemStack(item));
        } catch (Exception e) {
            // Invalid item ID, will fall back to default icon
        }
        return this;
    }
    
    /**
     * Set whether this node requires multiple research aspects to unlock.
     * If true, ALL specified costs must be met. If false, ANY single cost type can unlock it.
     */
    public ResearchNodeBuilder requiresMultipleAspects(boolean requires) {
        this.requiresMultipleAspects = requires;
        return this;
    }
    
    /**
     * Add a prerequisite research node that must be unlocked first.
     */
    public ResearchNodeBuilder prerequisite(String prerequisiteId) {
        if (!this.prerequisites.contains(prerequisiteId)) {
            this.prerequisites.add(prerequisiteId);
        }
        return this;
    }
    
    /**
     * Add multiple prerequisite research nodes.
     */
    public ResearchNodeBuilder prerequisites(List<String> prerequisiteIds) {
        for (String prereq : prerequisiteIds) {
            prerequisite(prereq);
        }
        return this;
    }
    
    /**
     * Build and return the research node.
     * Validates that all required fields are set.
     */
    public ResearchNode build() {
        // Validation
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("Research node ID cannot be null or empty");
        }
        
        // If no icon is set, try to use a default
        if (iconTexture == null && (iconItem == null || iconItem.isEmpty())) {
            // Use default research icon
            iconTexture = ResourceLocation.parse("strangematter:textures/ui/research_gui_node.png");
        }
        
        return new ResearchNode(
            id,
            category,
            x,
            y,
            new HashMap<>(researchCosts),
            iconTexture,
            iconItem.copy(),
            requiresMultipleAspects,
            new ArrayList<>(prerequisites) // Copy to prevent external modification
        );
    }
    
    /**
     * Get the ID of this research node.
     */
    public String getId() {
        return id;
    }
}

