package com.hexvane.strangematter.jei;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.jei.categories.RealityForgeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class StrangeMatterJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new RealityForgeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        
        // Register Reality Forge recipes (recipes crafted IN the Reality Forge)
        java.util.List<com.hexvane.strangematter.jei.recipes.RealityForgeRecipe> recipes = loadRealityForgeRecipes(registration);
        
        registration.addRecipes(RealityForgeCategory.RECIPE_TYPE, recipes);
        
        // Add info pages for machines
        addInfoPages(registration);
    }
    
    private void addInfoPages(IRecipeRegistration registration) {
        // Reality Forge info page
        registration.addIngredientInfo(
            new net.minecraft.world.item.ItemStack(StrangeMatterMod.REALITY_FORGE_ITEM.get()),
            mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
            net.minecraft.network.chat.Component.translatable("jei.strangematter.reality_forge.info")
        );
        
        // Resonance Condenser info page
        registration.addIngredientInfo(
            new net.minecraft.world.item.ItemStack(StrangeMatterMod.RESONANCE_CONDENSER_ITEM.get()),
            mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
            net.minecraft.network.chat.Component.translatable("jei.strangematter.resonance_condenser.info")
        );
    }
    
    private java.util.List<com.hexvane.strangematter.jei.recipes.RealityForgeRecipe> loadRealityForgeRecipes(IRecipeRegistration registration) {
        java.util.List<com.hexvane.strangematter.jei.recipes.RealityForgeRecipe> recipes = new java.util.ArrayList<>();
        
        try {
            // Check if we have a valid level and recipe manager
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null) {
                System.out.println("Level is null, cannot load recipes yet");
                return recipes;
            }
            
            // Get the recipe manager from Minecraft
            net.minecraft.world.item.crafting.RecipeManager recipeManager = minecraft.level.getRecipeManager();
            if (recipeManager == null) {
                System.out.println("Recipe manager is null, cannot load recipes yet");
                return recipes;
            }
            
            // Get the reality forge recipe type
            net.minecraft.world.item.crafting.RecipeType<com.hexvane.strangematter.recipe.RealityForgeRecipe> recipeType = 
                StrangeMatterMod.REALITY_FORGE_RECIPE_TYPE.get();
            if (recipeType == null) {
                System.out.println("Reality forge recipe type is null");
                return recipes;
            }
            
            // Get all reality forge recipes from the recipe manager
            java.util.List<com.hexvane.strangematter.recipe.RealityForgeRecipe> realityForgeRecipes = 
                recipeManager.getAllRecipesFor(recipeType);
            
            // If no recipes found
            if (realityForgeRecipes.isEmpty()) {
                return recipes;
            }
            
            // Convert each recipe to JEI format
            for (com.hexvane.strangematter.recipe.RealityForgeRecipe recipe : realityForgeRecipes) {
                com.hexvane.strangematter.jei.recipes.RealityForgeRecipe jeiRecipe = convertToJeiRecipe(recipe);
                if (jeiRecipe != null) {
                    recipes.add(jeiRecipe);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Failed to load reality forge recipes from recipe manager: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recipes;
    }
    
    private com.hexvane.strangematter.jei.recipes.RealityForgeRecipe convertToJeiRecipe(com.hexvane.strangematter.recipe.RealityForgeRecipe recipe) {
        try {
            // Get the ingredients directly from the recipe (already in 3x3 format)
            net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> ingredients = recipe.getIngredients();
            
            // Debug: Print each ingredient
            for (int i = 0; i < ingredients.size(); i++) {
                net.minecraft.world.item.crafting.Ingredient ingredient = ingredients.get(i);
            }
            
            // Get the shard requirements from the recipe
            java.util.Map<String, Integer> shardInputs = recipe.getShardRequirements();
            
            // Get the result from the recipe (need to provide RegistryAccess)
            net.minecraft.world.item.ItemStack resultStack = recipe.getResultItem(
                net.minecraft.client.Minecraft.getInstance().level.registryAccess()
            );
            
            // Convert ingredients to list format for JEI
            java.util.List<net.minecraft.world.item.crafting.Ingredient> inputs = new java.util.ArrayList<>(ingredients);
            
            // Create JEI recipe
            return new com.hexvane.strangematter.jei.recipes.RealityForgeRecipe(inputs, resultStack, shardInputs);
            
        } catch (Exception e) {
            System.err.println("Failed to convert recipe " + recipe.getId() + " to JEI format: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    
}
