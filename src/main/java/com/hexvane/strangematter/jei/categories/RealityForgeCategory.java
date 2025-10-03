package com.hexvane.strangematter.jei.categories;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.jei.recipes.RealityForgeRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RealityForgeCategory implements IRecipeCategory<RealityForgeRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "reality_forge");
    public static final RecipeType<RealityForgeRecipe> RECIPE_TYPE = RecipeType.create(StrangeMatterMod.MODID, "reality_forge", RealityForgeRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/gui/jei/reality_forge_recipe_background.png");

    public RealityForgeCategory(IGuiHelper guiHelper) {
        // Use the provided texture as the background (texture physical size is likely 100x46, display size 200x92)
        this.background = guiHelper.createDrawable(BACKGROUND_TEXTURE, 0, 0, 200, 92);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(StrangeMatterMod.REALITY_FORGE_BLOCK.get()));
    }

    @Override
    public RecipeType<RealityForgeRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.strangematter.reality_forge");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RealityForgeRecipe recipe, IFocusGroup focuses) {
        java.util.List<net.minecraft.world.item.crafting.Ingredient> inputs = recipe.getInputs();
        System.out.println("JEI Category: Recipe has " + inputs.size() + " inputs");
        
        if (inputs.isEmpty()) {
            System.out.println("ERROR: Recipe has no inputs, cannot display in JEI");
            return;
        }
        
        // Ensure we have at least 9 inputs (3x3 grid)
        while (inputs.size() < 9) {
            inputs.add(net.minecraft.world.item.crafting.Ingredient.EMPTY);
        }
        
        // 3x3 crafting grid layout (moved to center-right)
        int gridStartX = 92; // Start after shard columns
        int gridStartY = 20;
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, gridStartX, gridStartY)
            .addIngredients(inputs.get(0)); // Top-left
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, gridStartX + 18, gridStartY)
            .addIngredients(inputs.get(1)); // Top-center
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, gridStartX + 36, gridStartY)
            .addIngredients(inputs.get(2)); // Top-right
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, gridStartX, gridStartY + 18)
            .addIngredients(inputs.get(3)); // Middle-left
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, gridStartX + 18, gridStartY + 18)
            .addIngredients(inputs.get(4)); // Middle-center
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, gridStartX + 36, gridStartY + 18)
            .addIngredients(inputs.get(5)); // Middle-right
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, gridStartX, gridStartY + 36)
            .addIngredients(inputs.get(6)); // Bottom-left
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, gridStartX + 18, gridStartY + 36)
            .addIngredients(inputs.get(7)); // Bottom-center
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, gridStartX + 36, gridStartY + 36)
            .addIngredients(inputs.get(8)); // Bottom-right
        
        // Anomaly shard slots (external forge slots) - 2 columns on the left
        int shardIndex = 0;
        for (java.util.Map.Entry<String, Integer> shardEntry : recipe.getShardInputs().entrySet()) {
            String shardType = shardEntry.getKey();
            int shardCount = shardEntry.getValue();
            
            // Create ingredient for specific shard type
            net.minecraft.world.item.crafting.Ingredient shardIngredient;
            switch (shardType) {
                case "spatial":
                    shardIngredient = net.minecraft.world.item.crafting.Ingredient.of(StrangeMatterMod.SPATIAL_SHARD.get());
                    break;
                case "energetic":
                    shardIngredient = net.minecraft.world.item.crafting.Ingredient.of(StrangeMatterMod.ENERGETIC_SHARD.get());
                    break;
                case "gravitic":
                    shardIngredient = net.minecraft.world.item.crafting.Ingredient.of(StrangeMatterMod.GRAVITIC_SHARD.get());
                    break;
                case "temporal":
                    shardIngredient = net.minecraft.world.item.crafting.Ingredient.of(StrangeMatterMod.CHRONO_SHARD.get());
                    break;
                case "shade":
                    shardIngredient = net.minecraft.world.item.crafting.Ingredient.of(StrangeMatterMod.SHADE_SHARD.get());
                    break;
                case "insight":
                    shardIngredient = net.minecraft.world.item.crafting.Ingredient.of(StrangeMatterMod.INSIGHT_SHARD.get());
                    break;
                default:
                    shardIngredient = net.minecraft.world.item.crafting.Ingredient.of(StrangeMatterMod.ENERGETIC_SHARD.get());
                    break;
            }
            
            // Calculate position: left column (0-2) or right column (3-5)
            int shardSlotX = (shardIndex < 3) ? 20 : 50;
            int shardSlotY = 20 + ((shardIndex % 3) * 18);
            
            // Add shard slot
            builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, shardSlotX, shardSlotY)
                .addIngredients(shardIngredient)
                .setSlotName("shard_" + shardType);
            
            shardIndex++;
        }
        
        // Output slot (far right)
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT, 172, 38)
            .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(RealityForgeRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw shard quantity indicators for 2-column layout
        int shardIndex = 0;
        for (java.util.Map.Entry<String, Integer> shardEntry : recipe.getShardInputs().entrySet()) {
            int shardCount = shardEntry.getValue();
            
            // Calculate position: left column (0-2) or right column (3-5)
            int shardSlotX = (shardIndex < 3) ? 20 : 50;
            int shardSlotY = 20 + ((shardIndex % 3) * 18);
            
            // Always draw quantity (including 1x)
            String quantityText = shardCount + "x";
            // Position text to the left of shard slot
            int textX = shardSlotX - 12; // 12 pixels to the left of the slot
            guiGraphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                quantityText,
                textX, shardSlotY + 4,
                0xFFFFFF
            );
            
            shardIndex++;
        }
        
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }
}
