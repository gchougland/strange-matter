package com.hexvane.strangematter.kubejs;

import com.hexvane.strangematter.research.ResearchCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Builder class for creating research categories through KubeJS.
 * Provides a fluent API for constructing research categories.
 */
public class ResearchCategoryBuilder {
    private String id;
    private Component displayName;
    private ResourceLocation iconTexture = null;
    private ItemStack iconItem = ItemStack.EMPTY;
    private String unlockRequirement = null;
    private int order = 100; // Default order (higher = later)
    private String rootNodeId = null; // Node to auto-focus when this category is selected
    
    public ResearchCategoryBuilder(String id) {
        this.id = id;
    }
    
    /**
     * Set the display name for this category.
     * Can be a plain string or a translation key.
     */
    public ResearchCategoryBuilder name(String name) {
        // Check if it looks like a translation key
        if (name.contains(".")) {
            this.displayName = Component.translatable(name);
        } else {
            this.displayName = Component.literal(name);
        }
        return this;
    }
    
    /**
     * Set a texture icon for this category tab.
     * Example: "strangematter:textures/ui/custom_category_icon.png"
     */
    public ResearchCategoryBuilder iconTexture(String texturePath) {
        this.iconTexture = ResourceLocation.parse(texturePath);
        return this;
    }
    
    /**
     * Set an item icon for this category tab.
     * Example: "minecraft:diamond"
     */
    public ResearchCategoryBuilder iconItem(String itemId) {
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
     * Set the research node ID that must be unlocked before this category becomes visible.
     * If not set, the category is always visible.
     */
    public ResearchCategoryBuilder unlockRequirement(String researchNodeId) {
        this.unlockRequirement = researchNodeId;
        return this;
    }
    
    /**
     * Set the display order priority for this category.
     * Lower numbers appear first. Default is 100.
     */
    public ResearchCategoryBuilder order(int order) {
        this.order = order;
        return this;
    }
    
    /**
     * Set the research node ID to auto-focus (center in view) when this category tab is selected.
     */
    public ResearchCategoryBuilder rootNode(String researchNodeId) {
        this.rootNodeId = researchNodeId;
        return this;
    }
    
    /**
     * Build and return the research category.
     */
    public ResearchCategory build() {
        // Validation
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("Research category ID cannot be null or empty");
        }
        
        // Default display name if not set
        if (displayName == null) {
            displayName = Component.translatable("research.category.strangematter." + id);
        }
        
        // Default icon if not set
        if (iconTexture == null && (iconItem == null || iconItem.isEmpty())) {
            // Use default research notes icon
            iconItem = new ItemStack(com.hexvane.strangematter.StrangeMatterMod.RESEARCH_NOTES.get());
        }
        
        return new ResearchCategory(
            id,
            displayName,
            iconTexture,
            iconItem.copy(),
            unlockRequirement,
            order,
            rootNodeId
        );
    }
    
    /**
     * Get the ID of this research category.
     */
    public String getId() {
        return id;
    }
}
