package com.hexvane.strangematter.jei.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;

public class RealityForgeRecipe {
    private final List<Ingredient> inputs;
    private final ItemStack output;
    private final Map<String, Integer> shardInputs;
    /** Required research node id; null or empty means no gate. Used to show locked overlay in JEI when hide mode is on. */
    private final String requiredResearchId;

    public RealityForgeRecipe(List<Ingredient> inputs, ItemStack output, Map<String, Integer> shardInputs) {
        this(inputs, output, shardInputs, null);
    }

    public RealityForgeRecipe(List<Ingredient> inputs, ItemStack output, Map<String, Integer> shardInputs, String requiredResearchId) {
        this.inputs = inputs;
        this.output = output;
        this.shardInputs = shardInputs;
        this.requiredResearchId = requiredResearchId;
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

    public String getRequiredResearchId() {
        return requiredResearchId;
    }

    public boolean isLockedByResearch() {
        return requiredResearchId != null && !requiredResearchId.isEmpty();
    }
}
