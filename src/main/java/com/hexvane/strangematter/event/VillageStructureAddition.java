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
        
        // Register Anomaly Scientist Lab for each biome type
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
            // Try to find the correct field names using reflection
            Field[] fields = StructureTemplatePool.class.getDeclaredFields();
            Field rawTemplatesField = null;
            Field templatesField = null;
            
            // Look for fields that contain List<Pair<...>> and List<StructurePoolElement>
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                String typeName = field.getType().getSimpleName();
                
                // Look for the rawTemplates field (List<Pair<StructurePoolElement, Integer>>)
                // Handle both deobfuscated names and obfuscated names
                if (typeName.equals("List") && 
                    (fieldName.toLowerCase().contains("raw") || fieldName.startsWith("f_"))) {
                    try {
                        Object fieldValue = field.get(pool);
                        if (fieldValue instanceof List<?>) {
                            List<?> list = (List<?>) fieldValue;
                            if (!list.isEmpty() && list.get(0) instanceof Pair<?, ?>) {
                                rawTemplatesField = field;
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                        // Continue searching
                    }
                }
            }
            
            // Look for the templates field (List<StructurePoolElement> or ObjectArrayList<StructurePoolElement>)
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                String typeName = field.getType().getSimpleName();
                
                if ((typeName.equals("List") || typeName.equals("ObjectArrayList")) && 
                    (fieldName.toLowerCase().contains("template") || fieldName.toLowerCase().contains("element") || fieldName.startsWith("f_"))) {
                    try {
                        Object fieldValue = field.get(pool);
                        if (fieldValue instanceof List<?>) {
                            List<?> list = (List<?>) fieldValue;
                            if (!list.isEmpty() && list.get(0) instanceof StructurePoolElement) {
                                templatesField = field;
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                        // Continue searching
                    }
                }
            }
            
            if (rawTemplatesField == null || templatesField == null) {
                for (Field field : fields) {
                    LOGGER.warn("  " + field.getName() + " (" + field.getType().getSimpleName() + ")");
                }
                return;
            }
            
            @SuppressWarnings("unchecked")
            List<Pair<StructurePoolElement, Integer>> rawTemplates = 
                (List<Pair<StructurePoolElement, Integer>>) rawTemplatesField.get(pool);
            
            // Ensure list is mutable
            if (!(rawTemplates instanceof ArrayList)) {
                rawTemplates = new ArrayList<>(rawTemplates);
                rawTemplatesField.set(pool, rawTemplates);
            }
            
            @SuppressWarnings("unchecked")
            List<StructurePoolElement> templates = (List<StructurePoolElement>) templatesField.get(pool);
            
            SinglePoolElement addedElement = SinglePoolElement.single(toAdd.toString())
                .apply(StructureTemplatePool.Projection.RIGID);
            
            // Add to both lists
            rawTemplates.add(Pair.of(addedElement, 5));
            templates.add(addedElement);
            
            LOGGER.info("Successfully added structure " + toAdd + " to pool " + poolId);
            
        } catch (Exception e) {
            LOGGER.error("Failed to add structure to pool " + poolId, e);
        }
    }
}

