package com.hexvane.strangematter.research;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import com.hexvane.strangematter.StrangeMatterMod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ScannableObjectRegistry {
    private static final Map<ResourceLocation, ScannableObject> entityScannables = new HashMap<>();
    private static final Map<ResourceLocation, ScannableObject> blockScannables = new HashMap<>();
    
    static {
        // Register gravity anomaly entity
        registerEntity(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "gravity_anomaly"), 
            ResearchType.GRAVITY, 10);
        
        // Register warp gate anomaly entity
        registerEntity(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "warp_gate_anomaly"), 
            ResearchType.SPACE, 10);
        
        // Register energetic rift entity
        registerEntity(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "energetic_rift"), 
            ResearchType.ENERGY, 10);
        
        // Register echoing shadow entity
        registerEntity(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "echoing_shadow"), 
            ResearchType.SHADOW, 10);

        // Register temporal bloom entity
        registerEntity(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "temporal_bloom"), 
        ResearchType.TIME, 10);
        
        // Register thoughtwell entity
        registerEntity(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "thoughtwell"), 
            ResearchType.COGNITION, 10);
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
        ResourceLocation entityType = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return Optional.ofNullable(entityScannables.get(entityType));
    }
    
    public static Optional<ScannableObject> getScannableForBlock(BlockState blockState) {
        ResourceLocation blockType = ForgeRegistries.BLOCKS.getKey(blockState.getBlock());
        return Optional.ofNullable(blockScannables.get(blockType));
    }
    
    public static boolean isEntityScannable(Entity entity) {
        return getScannableForEntity(entity).isPresent();
    }
    
    public static boolean isBlockScannable(BlockState blockState) {
        return getScannableForBlock(blockState).isPresent();
    }
}
