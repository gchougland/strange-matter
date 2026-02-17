package com.hexvane.strangematter.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for custom research point types added via KubeJS.
 * Built-in types remain in ResearchType enum; custom types are stored here by string id.
 */
public class CustomResearchTypeRegistry {
    private static final Map<String, CustomResearchTypeInfo> customTypes = new LinkedHashMap<>();

    /**
     * Register a custom research point type. Rejects ids that match a built-in ResearchType name.
     */
    public static void register(String id, Component displayName, ResourceLocation iconTexture, ItemStack iconItem) {
        if (id == null || id.isEmpty()) {
            return;
        }
        if (ResearchType.fromName(id) != null) {
            return; // do not override built-in
        }
        customTypes.put(id, new CustomResearchTypeInfo(id, displayName, iconTexture, iconItem.copy()));
    }

    public static Optional<CustomResearchTypeInfo> get(String id) {
        return Optional.ofNullable(customTypes.get(id));
    }

    public static List<String> getAllIds() {
        return new ArrayList<>(customTypes.keySet());
    }

    public static boolean hasCustomType(String id) {
        return customTypes.containsKey(id);
    }

    public static class CustomResearchTypeInfo {
        private final String id;
        private final Component displayName;
        private final ResourceLocation iconTexture;
        private final ItemStack iconItem;

        public CustomResearchTypeInfo(String id, Component displayName, ResourceLocation iconTexture, ItemStack iconItem) {
            this.id = id;
            this.displayName = displayName;
            this.iconTexture = iconTexture;
            this.iconItem = iconItem;
        }

        public String getId() {
            return id;
        }

        public Component getDisplayName() {
            return displayName;
        }

        public ResourceLocation getIconTexture() {
            return iconTexture;
        }

        public ItemStack getIconItem() {
            return iconItem.copy();
        }

        public boolean hasIconTexture() {
            return iconTexture != null;
        }

        public boolean hasIconItem() {
            return iconItem != null && !iconItem.isEmpty();
        }
    }
}
