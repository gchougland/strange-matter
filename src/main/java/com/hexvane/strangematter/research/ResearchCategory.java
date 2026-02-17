package com.hexvane.strangematter.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Represents a research category that groups related research nodes together.
 * Categories can be hidden until certain research nodes are unlocked.
 */
public class ResearchCategory {
    private final String id;
    private final Component displayName;
    private final ResourceLocation iconTexture;
    private final ItemStack iconItem;
    private final String unlockRequirement; // Research node ID that must be unlocked, null if always visible
    private final int order; // Display order priority (lower = earlier)
    
    public ResearchCategory(String id, Component displayName, ResourceLocation iconTexture, ItemStack iconItem, 
                           String unlockRequirement, int order) {
        this.id = id;
        this.displayName = displayName;
        this.iconTexture = iconTexture;
        this.iconItem = iconItem;
        this.unlockRequirement = unlockRequirement;
        this.order = order;
    }
    
    public String getId() {
        return id;
    }
    
    public Component getDisplayName() {
        return displayName;
    }
    
    public ResourceLocation getIconTexture() {
        return iconTexture;
    }
    
    public ItemStack getIconItem() {
        return iconItem;
    }
    
    public String getUnlockRequirement() {
        return unlockRequirement;
    }
    
    public int getOrder() {
        return order;
    }
    
    public boolean hasIconTexture() {
        return iconTexture != null;
    }
    
    public boolean hasIconItem() {
        return iconItem != null && !iconItem.isEmpty();
    }
    
    /**
     * Check if this category requires a research node to be unlocked before it becomes visible.
     */
    public boolean hasUnlockRequirement() {
        return unlockRequirement != null && !unlockRequirement.isEmpty();
    }
}
