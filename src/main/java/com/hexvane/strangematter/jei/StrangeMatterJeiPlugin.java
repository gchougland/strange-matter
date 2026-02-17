package com.hexvane.strangematter.jei;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.jei.categories.RealityForgeCategory;
import com.hexvane.strangematter.recipe.RealityForgeRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
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
        
        addInfoPages(registration);

        java.util.List<com.hexvane.strangematter.jei.recipes.RealityForgeRecipe> recipes = loadAllRealityForgeRecipes();
        if (!recipes.isEmpty()) {
            registration.addRecipes(RealityForgeCategory.RECIPE_TYPE, recipes);
        }
    }

    private java.util.List<com.hexvane.strangematter.jei.recipes.RealityForgeRecipe> loadAllRealityForgeRecipes() {
        java.util.List<com.hexvane.strangematter.jei.recipes.RealityForgeRecipe> recipes = new java.util.ArrayList<>();
        try {
            Level level = getClientLevel();
            if (level == null) {
                return recipes;
            }
            RecipeManager recipeManager = level.getRecipeManager();
            if (recipeManager == null) {
                return recipes;
            }
            RecipeType<RealityForgeRecipe> recipeType = StrangeMatterMod.REALITY_FORGE_RECIPE_TYPE.get();
            if (recipeType == null) {
                return recipes;
            }
            for (RealityForgeRecipe recipe : recipeManager.getAllRecipesFor(recipeType)) {
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
            
            String requiredResearch = recipe.getRequiredResearch();
            return new com.hexvane.strangematter.jei.recipes.RealityForgeRecipe(inputs, resultStack, shardInputs, requiredResearch);
        } catch (Exception e) {
            System.err.println("Failed to convert recipe " + recipe.getId() + " to JEI format: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private void addInfoPages(IRecipeRegistration registration) {
        // Reality Forge info page
        registration.addIngredientInfo(
            new ItemStack(StrangeMatterMod.REALITY_FORGE_ITEM.get()),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.strangematter.reality_forge.info")
        );

        // Resonance Condenser info page
        registration.addIngredientInfo(
            new ItemStack(StrangeMatterMod.RESONANCE_CONDENSER_ITEM.get()),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.strangematter.resonance_condenser.info")
        );

        // Research Notes info page
        registration.addIngredientInfo(
            new ItemStack(StrangeMatterMod.RESEARCH_NOTES.get()),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.strangematter.research_notes.info")
        );
    }
}

