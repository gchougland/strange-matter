package com.hexvane.strangematter.kubejs;

import com.hexvane.strangematter.research.CustomResearchTypeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Builder for custom research point types (KubeJS).
 */
public class ResearchPointTypeBuilder {
    private final String id;
    private Component displayName;
    private ResourceLocation iconTexture = null;
    private ItemStack iconItem = ItemStack.EMPTY;

    public ResearchPointTypeBuilder(String id) {
        this.id = id;
    }

    public ResearchPointTypeBuilder name(String name) {
        if (name != null && name.contains(".")) {
            this.displayName = Component.translatable(name);
        } else {
            this.displayName = name != null ? Component.literal(name) : Component.literal(id);
        }
        return this;
    }

    public ResearchPointTypeBuilder iconTexture(String texturePath) {
        this.iconTexture = texturePath != null ? ResourceLocation.parse(texturePath) : null;
        return this;
    }

    public ResearchPointTypeBuilder iconItem(String itemId) {
        if (itemId != null && !itemId.isEmpty()) {
            try {
                ResourceLocation itemLocation = ResourceLocation.parse(itemId);
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(itemLocation)
                    .ifPresent(item -> this.iconItem = new ItemStack(item));
            } catch (Exception ignored) {}
        }
        return this;
    }

    /**
     * Register this custom research point type. Call after configuring.
     */
    public void build() {
        if (displayName == null) {
            displayName = Component.translatable("research.strangematter." + id);
        }
        if (iconTexture == null && (iconItem == null || iconItem.isEmpty())) {
            iconItem = new ItemStack(com.hexvane.strangematter.StrangeMatterMod.RESEARCH_NOTES.get());
        }
        CustomResearchTypeRegistry.register(id, displayName, iconTexture, iconItem != null ? iconItem.copy() : ItemStack.EMPTY);
    }

    public String getId() {
        return id;
    }
}
