package com.hexvane.strangematter.event;

import com.hexvane.strangematter.StrangeMatterMod;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Adds the Anomaly Scientist Lab to vanilla village generation
 * Uses reflection to inject into village template pools
 */
@Mod.EventBusSubscriber(modid = StrangeMatterMod.MODID)
public class VillageStructureAddition {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("StrangeMatter:VillageStructures");
    
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)
            return;
        
        // Register Anomaly Scientist Lab for each biome type (like IE does)
        for (String biome : new String[]{"plains", "snowy", "savanna", "desert", "taiga"}) {
            addToPool(
                ResourceLocation.fromNamespaceAndPath("minecraft", "village/" + biome + "/houses"),
                ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "anomaly_scientist_lab"),
                event.getRegistryAccess()
            );
        }
    }
    
    private static void addToPool(ResourceLocation poolId, ResourceLocation toAdd, 
                                  net.minecraft.core.RegistryAccess regAccess) {
        Registry<StructureTemplatePool> registry = regAccess.registryOrThrow(Registries.TEMPLATE_POOL);
        StructureTemplatePool pool = Objects.requireNonNull(registry.get(poolId), poolId.getPath());
        
        try {
            // Access private rawTemplates field using reflection
            Field rawTemplatesField = StructureTemplatePool.class.getDeclaredField("rawTemplates");
            rawTemplatesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Pair<StructurePoolElement, Integer>> rawTemplates = 
                (List<Pair<StructurePoolElement, Integer>>) rawTemplatesField.get(pool);
            
            // Ensure list is mutable (IE does this check)
            if (!(rawTemplates instanceof ArrayList)) {
                rawTemplates = new ArrayList<>(rawTemplates);
                rawTemplatesField.set(pool, rawTemplates);
            }
            
            // Access private templates field using reflection  
            Field templatesField = StructureTemplatePool.class.getDeclaredField("templates");
            templatesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<StructurePoolElement> templates = (List<StructurePoolElement>) templatesField.get(pool);
            
            // Use IE's exact approach - just the string, no processor
            SinglePoolElement addedElement = SinglePoolElement.single(toAdd.toString())
                .apply(StructureTemplatePool.Projection.RIGID);
            
            // Add to both lists
            rawTemplates.add(Pair.of(addedElement, 5));
            templates.add(addedElement);
            
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Failed to add structure to pool " + poolId, e);
        }
    }
}

