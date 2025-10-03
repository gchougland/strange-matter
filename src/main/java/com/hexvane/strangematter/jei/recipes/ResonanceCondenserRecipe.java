package com.hexvane.strangematter.jei.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;

public class ResonanceCondenserRecipe {
    private final List<Ingredient> inputs;
    private final ItemStack output;
    private final Map<String, Integer> shardInputs;

    public ResonanceCondenserRecipe(List<Ingredient> inputs, ItemStack output, Map<String, Integer> shardInputs) {
        this.inputs = inputs;
        this.output = output;
        this.shardInputs = shardInputs;
    }

    public List<Ingredient> getInputs() {
        return inputs;
    }

    public ItemStack getOutput() {
        return output;
    }

    public Map<String, Integer> getShardInputs() {
        return shardInputs;
    }
}
