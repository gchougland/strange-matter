package com.hexvane.strangematter.recipe;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.block.RealityForgeBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class RealityForgeRecipeRegistry {
    
    private static final List<RealityForgeRecipe> recipes = new ArrayList<>();
    
    public static void register(RealityForgeRecipe recipe) {
        recipes.add(recipe);
    }
    
    public static RealityForgeRecipe findMatchingRecipe(RealityForgeBlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            System.out.println("DEBUG: findMatchingRecipe - level is null");
            return null;
        }
        
        // Use Minecraft's recipe manager to find matching recipes
        var recipeManager = level.getRecipeManager();
        var recipeType = com.hexvane.strangematter.StrangeMatterMod.REALITY_FORGE_RECIPE_TYPE.get();
        
        var allRecipes = recipeManager.getAllRecipesFor(recipeType);
        
        for (var recipe : allRecipes) {
            // Create a RecipeInput wrapper around the block entity
            var recipeInput = new net.minecraft.world.item.crafting.RecipeInput() {
                @Override
                public int size() {
                    return blockEntity.getContainerSize();
                }
                
                @Override
                public net.minecraft.world.item.ItemStack getItem(int slot) {
                    return blockEntity.getItem(slot);
                }
                
                @Override
                public boolean isEmpty() {
                    return blockEntity.isEmpty();
                }
            };
            
            if (recipe.value().matches(recipeInput, level)) {
                return recipe.value();
            }
        }
        return null;
    }
    
    public static List<RealityForgeRecipe> getAllRecipes() {
        return new ArrayList<>(recipes);
    }
    
    public static RealityForgeRecipe findRecipeByResult(ResourceLocation resultItemId, Level level) {
        if (level == null) return null;
        
        var recipeManager = level.getRecipeManager();
        var recipeType = com.hexvane.strangematter.StrangeMatterMod.REALITY_FORGE_RECIPE_TYPE.get();
        
        var allRecipes = recipeManager.getAllRecipesFor(recipeType);
        
        for (var recipe : allRecipes) {
            ResourceLocation recipeResultId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(recipe.value().getResultItem(null).getItem());
            if (recipeResultId != null && recipeResultId.equals(resultItemId)) {
                return recipe.value();
            }
        }
        
        return null;
    }
    
    public static void clear() {
        recipes.clear();
    }
    
    public static void initializeDefaultRecipes() {
        clear();
        
        // Warp Gun Recipe
        // This will be added via JSON files in the data folder
        // For now, we'll add it programmatically for testing
        registerWarpGunRecipe();
    }
    
    private static void registerWarpGunRecipe() {
        // This is a placeholder - actual recipes should be loaded from JSON files
        // The JSON files will be in src/main/resources/data/strangematter/recipes/reality_forge/
    }
}
