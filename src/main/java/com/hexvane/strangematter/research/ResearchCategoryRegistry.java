package com.hexvane.strangematter.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry for managing research categories.
 * Categories can be hidden until certain research nodes are unlocked.
 */
public class ResearchCategoryRegistry {
    private static final Map<String, ResearchCategory> categories = new HashMap<>();
    
    /**
     * Register a research category.
     */
    public static void register(ResearchCategory category) {
        categories.put(category.getId(), category);
    }
    
    /**
     * Get a category by ID.
     */
    public static ResearchCategory getCategory(String id) {
        return categories.get(id);
    }
    
    /**
     * Get all registered categories.
     */
    public static Collection<ResearchCategory> getAllCategories() {
        return categories.values();
    }
    
    /**
     * Get all visible categories (filtered by unlock status).
     * Categories are sorted by order priority.
     */
    public static List<ResearchCategory> getVisibleCategories(ResearchData researchData) {
        return categories.values().stream()
            .filter(category -> isCategoryVisible(category, researchData))
            .sorted(Comparator.comparingInt(ResearchCategory::getOrder))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a category is visible based on unlock requirements.
     */
    public static boolean isCategoryVisible(ResearchCategory category, ResearchData researchData) {
        if (!category.hasUnlockRequirement()) {
            return true; // No requirement, always visible
        }
        
        // Check if the unlock requirement research node is unlocked
        return researchData.hasUnlockedResearch(category.getUnlockRequirement());
    }
    
    /**
     * Check if a category is visible (convenience method with category ID).
     */
    public static boolean isCategoryVisible(String categoryId, ResearchData researchData) {
        ResearchCategory category = categories.get(categoryId);
        if (category == null) {
            return false; // Category doesn't exist
        }
        return isCategoryVisible(category, researchData);
    }
    
    /**
     * Clear all categories (useful for reloading).
     */
    public static void clear() {
        categories.clear();
    }
}
