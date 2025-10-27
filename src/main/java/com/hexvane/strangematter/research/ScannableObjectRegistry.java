package com.hexvane.strangematter.research;

import com.hexvane.strangematter.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import com.hexvane.strangematter.StrangeMatterMod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ScannableObjectRegistry {
    private static final Map<ResourceLocation, ScannableObject> entityScannables = new HashMap<>();
    private static final Map<ResourceLocation, ScannableObject> blockScannables = new HashMap<>();
    
    /**
     * Initialize the registry. This is called during mod initialization.
     * Research points are retrieved from config instead of being hardcoded.
     */
    public static void init() {
        // Register gravity anomaly entity
        registerEntity(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "gravity_anomaly"), 
            ResearchType.GRAVITY, Config.gravityResearchPoints);
        
        // Register warp gate anomaly entity
        registerEntity(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "warp_gate_anomaly"), 
            ResearchType.SPACE, Config.warpResearchPoints);
        
        // Register energetic rift entity
        registerEntity(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "energetic_rift"), 
            ResearchType.ENERGY, Config.energeticResearchPoints);
        
        // Register echoing shadow entity
        registerEntity(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "echoing_shadow"), 
            ResearchType.SHADOW, Config.shadowResearchPoints);

        // Register temporal bloom entity
        registerEntity(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "temporal_bloom"), 
            ResearchType.TIME, Config.temporalResearchPoints);
        
        // Register thoughtwell entity
        registerEntity(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "thoughtwell"), 
            ResearchType.COGNITION, Config.thoughtwellResearchPoints);
    }
    
    public static void registerEntity(ResourceLocation entityType, ResearchType researchType, int amount) {
        ScannableObject scannable = ScannableObject.forEntity(entityType, researchType, amount);
        entityScannables.put(entityType, scannable);
    }
    
    public static void registerBlock(ResourceLocation blockType, ResearchType researchType, int amount) {
        ScannableObject scannable = ScannableObject.forBlock(blockType, researchType, amount);
        blockScannables.put(blockType, scannable);
    }
    
    public static Optional<ScannableObject> getScannableForEntity(Entity entity) {
        ResourceLocation entityType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return Optional.ofNullable(entityScannables.get(entityType));
    }
    
    public static Optional<ScannableObject> getScannableForBlock(BlockState blockState) {
        ResourceLocation blockType = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        return Optional.ofNullable(blockScannables.get(blockType));
    }
    
    public static boolean isEntityScannable(Entity entity) {
        return getScannableForEntity(entity).isPresent();
    }
    
    public static boolean isBlockScannable(BlockState blockState) {
        return getScannableForBlock(blockState).isPresent();
    }
}
