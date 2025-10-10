package com.hexvane.strangematter.morph;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player morph data - what entity type each player is morphed into
 */
public class PlayerMorphData {
    private static final Map<UUID, String> morphedEntities = new HashMap<>();
    private static final Map<UUID, UUID> morphedPlayerUUIDs = new HashMap<>(); // For player skins
    
    /**
     * Set a player's morph to a specific entity type
     */
    public static void setMorph(UUID playerUUID, String entityTypeId) {
        setMorph(playerUUID, entityTypeId, null);
    }
    
    /**
     * Set a player's morph to a specific entity type with optional player UUID for skin
     */
    public static void setMorph(UUID playerUUID, String entityTypeId, UUID targetPlayerUUID) {
        morphedEntities.put(playerUUID, entityTypeId);
        if (targetPlayerUUID != null) {
            morphedPlayerUUIDs.put(playerUUID, targetPlayerUUID);
        } else {
            morphedPlayerUUIDs.remove(playerUUID);
        }
    }
    
    /**
     * Clear a player's morph
     */
    public static void clearMorph(UUID playerUUID) {
        morphedEntities.remove(playerUUID);
        morphedPlayerUUIDs.remove(playerUUID);
    }
    
    /**
     * Get the target player UUID if morphed into a player
     */
    public static UUID getMorphedPlayerUUID(UUID playerUUID) {
        return morphedPlayerUUIDs.get(playerUUID);
    }
    
    /**
     * Check if a player is morphed
     */
    public static boolean isMorphed(UUID playerUUID) {
        return morphedEntities.containsKey(playerUUID);
    }
    
    /**
     * Get the entity type ID a player is morphed into
     */
    public static String getMorphEntityType(UUID playerUUID) {
        return morphedEntities.get(playerUUID);
    }
    
    /**
     * Get the EntityType a player is morphed into
     */
    public static EntityType<?> getMorphEntityTypeObj(UUID playerUUID) {
        String entityTypeId = morphedEntities.get(playerUUID);
        if (entityTypeId == null) return null;
        
        try {
            ResourceLocation resourceLocation = ResourceLocation.parse(entityTypeId);
            return net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(resourceLocation);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Save morph data to NBT
     */
    public static CompoundTag save(CompoundTag tag) {
        CompoundTag morphsTag = new CompoundTag();
        for (Map.Entry<UUID, String> entry : morphedEntities.entrySet()) {
            morphsTag.putString(entry.getKey().toString(), entry.getValue());
        }
        tag.put("morphs", morphsTag);
        return tag;
    }
    
    /**
     * Load morph data from NBT
     */
    public static void load(CompoundTag tag) {
        morphedEntities.clear();
        if (tag.contains("morphs")) {
            CompoundTag morphsTag = tag.getCompound("morphs");
            for (String key : morphsTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    morphedEntities.put(uuid, morphsTag.getString(key));
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                }
            }
        }
    }
}

