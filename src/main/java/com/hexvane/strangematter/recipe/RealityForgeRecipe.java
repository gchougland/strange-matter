package com.hexvane.strangematter.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import javax.annotation.Nonnull;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

public class RealityForgeRecipe implements Recipe<RecipeInput> {
    
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
    public boolean matches(@Nonnull RecipeInput recipeInput, @Nonnull Level level) {
        // Check if the crafting grid matches
        for (int i = 0; i < 9; i++) {
            if (!ingredients.get(i).test(recipeInput.getItem(i))) {
                return false;
            }
        }
        
        // Check shard requirements
        // Note: RecipeInput doesn't provide access to block entity data
        // This will need to be handled by the crafting logic in RealityForgeBlockEntity
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
    public ItemStack assemble(@Nonnull RecipeInput recipeInput, @Nonnull HolderLookup.Provider provider) {
        return result.copy();
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }
    
    @Override
    public ItemStack getResultItem(@Nonnull HolderLookup.Provider provider) {
        return result;
    }
    
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
        public com.mojang.serialization.MapCodec<RealityForgeRecipe> codec() {
            return com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance -> 
                instance.group(
                    // Parse pattern array
                    com.mojang.serialization.Codec.STRING.listOf().fieldOf("pattern")
                        .forGetter(recipe -> {
                            // Reconstruct pattern from ingredients
                            String[] pattern = new String[3];
                            for (int i = 0; i < 3; i++) {
                                StringBuilder row = new StringBuilder();
                                for (int j = 0; j < 3; j++) {
                                    int idx = i * 3 + j;
                                    if (recipe.ingredients.get(idx) != net.minecraft.world.item.crafting.Ingredient.EMPTY) {
                                        row.append(getPatternChar(idx));
                                    } else {
                                        row.append(' ');
                                    }
                                }
                                pattern[i] = row.toString();
                            }
                            return java.util.Arrays.asList(pattern);
                        }),
                    // Parse key map
                    com.mojang.serialization.Codec.unboundedMap(com.mojang.serialization.Codec.STRING, 
                        net.minecraft.world.item.crafting.Ingredient.CODEC).fieldOf("key")
                        .forGetter(recipe -> {
                            // Reconstruct key from ingredients
                            java.util.Map<String, net.minecraft.world.item.crafting.Ingredient> keyMap = new java.util.HashMap<>();
                            for (int i = 0; i < 9; i++) {
                                if (recipe.ingredients.get(i) != net.minecraft.world.item.crafting.Ingredient.EMPTY) {
                                    keyMap.put(String.valueOf((char)('A' + i)), recipe.ingredients.get(i));
                                }
                            }
                            return keyMap;
                        }),
                    // Parse shard requirements (optional)
                    com.mojang.serialization.Codec.unboundedMap(com.mojang.serialization.Codec.STRING, com.mojang.serialization.Codec.INT)
                        .optionalFieldOf("shard_requirements")
                        .forGetter(recipe -> java.util.Optional.of(recipe.shardRequirements)),
                    // Parse shards (legacy, optional)
                    com.mojang.serialization.Codec.unboundedMap(com.mojang.serialization.Codec.STRING, com.mojang.serialization.Codec.INT)
                        .optionalFieldOf("shards")
                        .forGetter(recipe -> java.util.Optional.of(recipe.shardRequirements)),
                    // Parse required research (optional)
                    com.mojang.serialization.Codec.STRING.optionalFieldOf("required_research")
                        .forGetter(recipe -> java.util.Optional.of(recipe.requiredResearch)),
                    // Parse result - use ItemStack.CODEC which expects {"id": "...", "count": 1}
                    net.minecraft.world.item.ItemStack.CODEC.fieldOf("result")
                        .forGetter(recipe -> recipe.getResultItem(null))
                ).apply(instance, (pattern, key, shardReqs, shards, research, result) -> {
                    // Merge shard requirements from both fields
                    java.util.Map<String, Integer> shardRequirements = new java.util.HashMap<>();
                    if (shardReqs.isPresent()) {
                        shardRequirements.putAll(shardReqs.get());
                    }
                    if (shards.isPresent()) {
                        shardRequirements.putAll(shards.get());
                    }
                    
                    // Parse ingredients from pattern and key
                    net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> ingredients = 
                        net.minecraft.core.NonNullList.withSize(9, net.minecraft.world.item.crafting.Ingredient.EMPTY);
                    
                    for (int i = 0; i < pattern.size() && i < 3; i++) {
                        String row = pattern.get(i);
                        if (row.length() > 3) continue;
                        
                        for (int j = 0; j < row.length(); j++) {
                            char c = row.charAt(j);
                            if (c != ' ' && key.containsKey(String.valueOf(c))) {
                                ingredients.set(i * 3 + j, key.get(String.valueOf(c)));
                            }
                        }
                    }
                    
                    // The recipe ID will be provided by the recipe manager
                    // For now, use a temporary ID
                    net.minecraft.resources.ResourceLocation recipeId = 
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("strangematter", "temp");
                    
                    return new RealityForgeRecipe(
                        recipeId,
                        result,
                        ingredients,
                        shardRequirements,
                        research.orElse("")
                    );
                })
            );
        }
        
        // Helper to convert ingredient index to pattern character
        private static char getPatternChar(int idx) {
            return (char)('A' + idx);
        }
        
        @Override
        public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, RealityForgeRecipe> streamCodec() {
            return new net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, RealityForgeRecipe>() {
                @Override
                public void encode(net.minecraft.network.RegistryFriendlyByteBuf buffer, RealityForgeRecipe recipe) {
                    toNetwork(buffer, recipe);
                }
                
                @Override
                public RealityForgeRecipe decode(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
                    // First read the ID from the buffer
                    ResourceLocation id = net.minecraft.resources.ResourceLocation.STREAM_CODEC.decode(buffer);
                    // The rest of the recipe data is in the buffer, but fromNetwork expects it to continue from where it is
                    // So we need to create a wrapper that reads from the current position
                    RealityForgeRecipe recipe = fromNetwork(id, buffer);
                    return recipe;
                }
            };
        }
        
        public RealityForgeRecipe fromJson(ResourceLocation recipeId, JsonObject json, HolderLookup.Provider provider) {
            ItemStack result = ItemStack.CODEC.parse(provider.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE), GsonHelper.getAsJsonObject(json, "result")).getOrThrow();
            
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
                        ingredients.set(i * 3 + j, Ingredient.CODEC.parse(provider.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE), key.getAsJsonObject(String.valueOf(c))).getOrThrow());
                    }
                }
            }
            
            Map<String, Integer> shardRequirements = new HashMap<>();
            // Check for both "shards" and "shard_requirements" to support both old and new format
            if (json.has("shards")) {
                JsonObject shards = GsonHelper.getAsJsonObject(json, "shards");
                for (String key2 : shards.keySet()) {
                    shardRequirements.put(key2, GsonHelper.getAsInt(shards, key2));
                }
            } else if (json.has("shard_requirements")) {
                JsonObject shards = GsonHelper.getAsJsonObject(json, "shard_requirements");
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
        
        public RealityForgeRecipe fromNetwork(ResourceLocation recipeId, net.minecraft.network.RegistryFriendlyByteBuf buffer) {
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            
            NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
            for (int i = 0; i < 9; i++) {
                ingredients.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
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
        
        public void toNetwork(net.minecraft.network.RegistryFriendlyByteBuf buffer, RealityForgeRecipe recipe) {
            // Write the recipe ID first
            net.minecraft.resources.ResourceLocation.STREAM_CODEC.encode(buffer, recipe.id);
            
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            
            for (Ingredient ingredient : recipe.ingredients) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
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
