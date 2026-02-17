package com.hexvane.strangematter.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified lookup for research point types (built-in and custom) by string id.
 */
public final class ResearchTypeHelper {

    public static List<String> getAllTypeIds() {
        List<String> ids = new ArrayList<>();
        for (ResearchType type : ResearchType.values()) {
            ids.add(type.getName());
        }
        ids.addAll(CustomResearchTypeRegistry.getAllIds());
        return ids;
    }

    public static Component getDisplayName(String id) {
        ResearchType type = ResearchType.fromName(id);
        if (type != null) {
            return Component.translatable(type.getTranslationKey());
        }
        return CustomResearchTypeRegistry.get(id)
            .map(CustomResearchTypeRegistry.CustomResearchTypeInfo::getDisplayName)
            .orElse(Component.literal(id));
    }

    public static ResourceLocation getIconResourceLocation(String id) {
        ResearchType type = ResearchType.fromName(id);
        if (type != null) {
            return type.getIconResourceLocation();
        }
        return CustomResearchTypeRegistry.get(id)
            .filter(CustomResearchTypeRegistry.CustomResearchTypeInfo::hasIconTexture)
            .map(CustomResearchTypeRegistry.CustomResearchTypeInfo::getIconTexture)
            .orElse(null);
    }

    public static ItemStack getIconItem(String id) {
        ResearchType type = ResearchType.fromName(id);
        if (type != null) {
            return ItemStack.EMPTY; // built-in use texture
        }
        return CustomResearchTypeRegistry.get(id)
            .filter(CustomResearchTypeRegistry.CustomResearchTypeInfo::hasIconItem)
            .map(CustomResearchTypeRegistry.CustomResearchTypeInfo::getIconItem)
            .orElse(ItemStack.EMPTY);
    }

    public static boolean hasIconTexture(String id) {
        ResearchType type = ResearchType.fromName(id);
        if (type != null) {
            return true;
        }
        return CustomResearchTypeRegistry.get(id)
            .map(CustomResearchTypeRegistry.CustomResearchTypeInfo::hasIconTexture)
            .orElse(false);
    }

    public static boolean hasIconItem(String id) {
        ResearchType type = ResearchType.fromName(id);
        if (type != null) {
            return false; // built-in use texture
        }
        return CustomResearchTypeRegistry.get(id)
            .map(CustomResearchTypeRegistry.CustomResearchTypeInfo::hasIconItem)
            .orElse(false);
    }

    public static boolean isKnownType(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        return ResearchType.fromName(id) != null || CustomResearchTypeRegistry.hasCustomType(id);
    }
}
