package com.hexvane.strangematter.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class ResearchNode {
    private final String id;
    private final String category;
    private final int x;
    private final int y;
    private final Map<ResearchType, Integer> researchCosts;
    private final ResourceLocation iconTexture;
    private final ItemStack iconItem;
    private final boolean requiresMultipleAspects;
    private final List<String> prerequisites;
    
    public ResearchNode(String id, String category, int x, int y, 
                       Map<ResearchType, Integer> researchCosts, ResourceLocation iconTexture, ItemStack iconItem, boolean requiresMultipleAspects, List<String> prerequisites) {
        this.id = id;
        this.category = category;
        this.x = x;
        this.y = y;
        this.researchCosts = researchCosts;
        this.iconTexture = iconTexture;
        this.iconItem = iconItem;
        this.requiresMultipleAspects = requiresMultipleAspects;
        this.prerequisites = prerequisites;
    }
    
    public String getId() {
        return id;
    }
    
    public Component getDisplayName() {
        return Component.translatable("research.strangematter." + id + ".name");
    }
    
    public Component getDisplayDescription() {
        return Component.translatable("research.strangematter." + id + ".description");
    }
    
    public String getCategory() {
        return category;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public Map<ResearchType, Integer> getResearchCosts() {
        return researchCosts;
    }
    
    public int getTotalCost() {
        return researchCosts.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public boolean requiresMultipleAspects() {
        return requiresMultipleAspects;
    }
    
    public ResourceLocation getIconTexture() {
        return iconTexture;
    }
    
    public ItemStack getIconItem() {
        return iconItem;
    }
    
    public boolean hasIconTexture() {
        return iconTexture != null;
    }
    
    public boolean hasIconItem() {
        return iconItem != null && !iconItem.isEmpty();
    }
    
    public boolean canAfford(Map<ResearchType, Integer> playerPoints) {
        for (Map.Entry<ResearchType, Integer> cost : researchCosts.entrySet()) {
            int playerPointsForType = playerPoints.getOrDefault(cost.getKey(), 0);
            if (playerPointsForType < cost.getValue()) {
                return false;
            }
        }
        return true;
    }
    
    public List<String> getPrerequisites() {
        return prerequisites;
    }
    
    public boolean hasPrerequisites() {
        return prerequisites != null && !prerequisites.isEmpty();
    }
}
