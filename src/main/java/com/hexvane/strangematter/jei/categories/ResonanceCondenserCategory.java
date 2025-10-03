package com.hexvane.strangematter.jei.categories;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.jei.recipes.ResonanceCondenserRecipe;
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

public class ResonanceCondenserCategory implements IRecipeCategory<ResonanceCondenserRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "resonance_condenser");
    public static final RecipeType<ResonanceCondenserRecipe> RECIPE_TYPE = RecipeType.create(StrangeMatterMod.MODID, "resonance_condenser", ResonanceCondenserRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ResonanceCondenserCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(176, 80);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(StrangeMatterMod.RESONANCE_CONDENSER_BLOCK.get()));
    }

    @Override
    public RecipeType<ResonanceCondenserRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.strangematter.resonance_condenser");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ResonanceCondenserRecipe recipe, IFocusGroup focuses) {
        // 3x3 crafting grid layout
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, 30, 8)
            .addIngredients(recipe.getInputs().get(0)); // Top-left
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, 48, 8)
            .addIngredients(recipe.getInputs().get(1)); // Top-center
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, 66, 8)
            .addIngredients(recipe.getInputs().get(2)); // Top-right
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, 30, 26)
            .addIngredients(recipe.getInputs().get(3)); // Middle-left
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, 48, 26)
            .addIngredients(recipe.getInputs().get(4)); // Middle-center
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, 66, 26)
            .addIngredients(recipe.getInputs().get(5)); // Middle-right
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, 30, 44)
            .addIngredients(recipe.getInputs().get(6)); // Bottom-left
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, 48, 44)
            .addIngredients(recipe.getInputs().get(7)); // Bottom-center
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, 66, 44)
            .addIngredients(recipe.getInputs().get(8)); // Bottom-right
        
        // Output slot
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT, 124, 26)
            .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(ResonanceCondenserRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }
}
