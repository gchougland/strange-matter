package com.hexvane.strangematter.recipe;

import com.google.gson.JsonObject;
import com.hexvane.strangematter.block.RealityForgeBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class RealityForgeRecipe implements Recipe<Container> {
    
    private final ResourceLocation id;
    private final ItemStack result;
    private final NonNullList<Ingredient> ingredients;
    private final Map<String, Integer> shardRequirements;
    private final String requiredResearch;
    
    public RealityForgeRecipe(ResourceLocation id, ItemStack result, NonNullList<Ingredient> ingredients, Map<String, Integer> shardRequirements, String requiredResearch) {
        this.id = id;
        this.result = result;
        this.ingredients = ingredients;
        this.shardRequirements = shardRequirements;
        this.requiredResearch = requiredResearch;
    }
    
    @Override
    public boolean matches(Container container, Level level) {
        // Check if the crafting grid matches
        for (int i = 0; i < 9; i++) {
            if (!ingredients.get(i).test(container.getItem(i))) {
                return false;
            }
        }
        
        // Check shard requirements
        if (container instanceof RealityForgeBlockEntity realityForge) {
            Map<String, Integer> storedShards = realityForge.getStoredShards();
            for (Map.Entry<String, Integer> requirement : shardRequirements.entrySet()) {
                String shardType = requirement.getKey();
                int required = requirement.getValue();
                int available = storedShards.getOrDefault(shardType, 0);
                if (available < required) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Check if the recipe can be crafted by a specific player
     */
    public boolean canCraftByPlayer(Player player) {
        // If no research requirement, anyone can craft
        if (requiredResearch == null || requiredResearch.isEmpty()) {
            return true;
        }
        
        // Check if player has unlocked the required research
        com.hexvane.strangematter.research.ResearchData researchData = com.hexvane.strangematter.research.ResearchData.get(player);
        return researchData.hasUnlockedResearch(requiredResearch);
    }
    
    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.copy();
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }
    
    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result;
    }
    
    @Override
    public ResourceLocation getId() {
        return id;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return com.hexvane.strangematter.StrangeMatterMod.REALITY_FORGE_RECIPE_SERIALIZER.get();
    }
    
    @Override
    public RecipeType<?> getType() {
        return com.hexvane.strangematter.StrangeMatterMod.REALITY_FORGE_RECIPE_TYPE.get();
    }
    
    public Map<String, Integer> getShardRequirements() {
        return new HashMap<>(shardRequirements);
    }
    
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }
    
    public String getRequiredResearch() {
        return requiredResearch;
    }
    
    public static class Serializer implements RecipeSerializer<RealityForgeRecipe> {
        
        @Override
        public RealityForgeRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            
            NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
            com.google.gson.JsonArray patternArray = GsonHelper.getAsJsonArray(json, "pattern");
            String[] pattern = new String[patternArray.size()];
            for (int i = 0; i < patternArray.size(); i++) {
                pattern[i] = patternArray.get(i).getAsString();
            }
            if (pattern.length != 3) {
                throw new IllegalArgumentException("Pattern must have exactly 3 rows");
            }
            
            JsonObject key = GsonHelper.getAsJsonObject(json, "key");
            for (int i = 0; i < 3; i++) {
                String row = pattern[i];
                if (row.length() != 3) {
                    throw new IllegalArgumentException("Each pattern row must have exactly 3 characters");
                }
                for (int j = 0; j < 3; j++) {
                    char c = row.charAt(j);
                    if (c != ' ') {
                        ingredients.set(i * 3 + j, Ingredient.fromJson(key.getAsJsonObject(String.valueOf(c))));
                    }
                }
            }
            
            Map<String, Integer> shardRequirements = new HashMap<>();
            if (json.has("shards")) {
                JsonObject shards = GsonHelper.getAsJsonObject(json, "shards");
                for (String key2 : shards.keySet()) {
                    shardRequirements.put(key2, GsonHelper.getAsInt(shards, key2));
                }
            }
            
            // Get research requirement (optional field)
            String requiredResearch = null;
            if (json.has("required_research")) {
                requiredResearch = GsonHelper.getAsString(json, "required_research");
            }
            
            return new RealityForgeRecipe(recipeId, result, ingredients, shardRequirements, requiredResearch);
        }
        
        @Override
        public RealityForgeRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ItemStack result = buffer.readItem();
            
            NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
            for (int i = 0; i < 9; i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }
            
            Map<String, Integer> shardRequirements = new HashMap<>();
            int shardCount = buffer.readInt();
            for (int i = 0; i < shardCount; i++) {
                String shardType = buffer.readUtf();
                int amount = buffer.readInt();
                shardRequirements.put(shardType, amount);
            }
            
            // Read research requirement
            String requiredResearch = null;
            if (buffer.readBoolean()) {
                requiredResearch = buffer.readUtf();
            }
            
            return new RealityForgeRecipe(recipeId, result, ingredients, shardRequirements, requiredResearch);
        }
        
        @Override
        public void toNetwork(FriendlyByteBuf buffer, RealityForgeRecipe recipe) {
            buffer.writeItem(recipe.result);
            
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }
            
            buffer.writeInt(recipe.shardRequirements.size());
            for (Map.Entry<String, Integer> entry : recipe.shardRequirements.entrySet()) {
                buffer.writeUtf(entry.getKey());
                buffer.writeInt(entry.getValue());
            }
            
            // Write research requirement
            if (recipe.requiredResearch != null) {
                buffer.writeBoolean(true);
                buffer.writeUtf(recipe.requiredResearch);
            } else {
                buffer.writeBoolean(false);
            }
        }
    }
}
