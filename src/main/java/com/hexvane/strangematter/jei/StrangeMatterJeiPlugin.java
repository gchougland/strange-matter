package com.hexvane.strangematter.jei;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.jei.categories.RealityForgeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

@JeiPlugin
public class StrangeMatterJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        // Only register on client side to avoid loading client-only GUI classes on server
        // JEI plugins are loaded on both sides, so we must guard against server loading
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registration.addRecipeCategories(new RealityForgeCategory(registration.getJeiHelpers().getGuiHelper()));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Only run on client side
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        
        // Register Reality Forge recipes
        java.util.List<com.hexvane.strangematter.jei.recipes.RealityForgeRecipe> recipes = loadRealityForgeRecipes(registration);
        if (!recipes.isEmpty()) {
            registration.addRecipes(RealityForgeCategory.RECIPE_TYPE, recipes);
        }
        
        // Add info pages for machines
        addInfoPages(registration);
    }
    
    private java.util.List<com.hexvane.strangematter.jei.recipes.RealityForgeRecipe> loadRealityForgeRecipes(IRecipeRegistration registration) {
        java.util.List<com.hexvane.strangematter.jei.recipes.RealityForgeRecipe> recipes = new java.util.ArrayList<>();
        
        try {
            // Get recipe manager from the level - use client world access through a helper method
            net.minecraft.world.level.Level level = getClientLevel();
            if (level == null) {
                System.out.println("Level is null, cannot load recipes yet");
                return recipes;
            }
            
            net.minecraft.world.item.crafting.RecipeManager recipeManager = level.getRecipeManager();
            if (recipeManager == null) {
                System.out.println("Recipe manager is null, cannot load recipes yet");
                return recipes;
            }
            
            // Get all reality forge recipes
            net.minecraft.world.item.crafting.RecipeType<com.hexvane.strangematter.recipe.RealityForgeRecipe> recipeType = 
                StrangeMatterMod.REALITY_FORGE_RECIPE_TYPE.get();
            if (recipeType == null) {
                return recipes;
            }
            
            java.util.List<com.hexvane.strangematter.recipe.RealityForgeRecipe> realityForgeRecipes = 
                recipeManager.getAllRecipesFor(recipeType);
            
            // Convert each recipe to JEI format
            for (com.hexvane.strangematter.recipe.RealityForgeRecipe recipe : realityForgeRecipes) {
                com.hexvane.strangematter.jei.recipes.RealityForgeRecipe jeiRecipe = convertToJeiRecipe(recipe, level);
                if (jeiRecipe != null) {
                    recipes.add(jeiRecipe);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Failed to load reality forge recipes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recipes;
    }
    
    private net.minecraft.world.level.Level getClientLevel() {
        // Safe way to get client level - this method is only called on client side
        return net.minecraft.client.Minecraft.getInstance().level;
    }
    
    private com.hexvane.strangematter.jei.recipes.RealityForgeRecipe convertToJeiRecipe(
            com.hexvane.strangematter.recipe.RealityForgeRecipe recipe, 
            net.minecraft.world.level.Level level) {
        try {
            net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> ingredients = recipe.getIngredients();
            java.util.Map<String, Integer> shardInputs = recipe.getShardRequirements();
            net.minecraft.world.item.ItemStack resultStack = recipe.getResultItem(level.registryAccess());
            
            java.util.List<net.minecraft.world.item.crafting.Ingredient> inputs = new java.util.ArrayList<>(ingredients);
            
            return new com.hexvane.strangematter.jei.recipes.RealityForgeRecipe(inputs, resultStack, shardInputs);
        } catch (Exception e) {
            System.err.println("Failed to convert recipe " + recipe.getId() + " to JEI format: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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
}

