package com.hexvane.strangematter.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

public class ContainmentCapsuleItem extends Item {
    
    private final AnomalyType anomalyType;
    
    public ContainmentCapsuleItem(AnomalyType anomalyType) {
        super(new Item.Properties().stacksTo(64));
        this.anomalyType = anomalyType;
    }
    
    public boolean hasAnomaly() {
        return anomalyType != AnomalyType.NONE;
    }
    
    public AnomalyType getAnomalyType() {
        return anomalyType;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (hasAnomaly()) {
            tooltip.add(Component.translatable("item.strangematter.containment_capsule.filled.tooltip")
                .withStyle(ChatFormatting.GREEN));
            
            // Get the specific tooltip for this anomaly type
            String tooltipKey = "item.strangematter.containment_capsule_" + getAnomalyTypeKey() + ".tooltip";
            tooltip.add(Component.translatable(tooltipKey)
                .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.strangematter.containment_capsule.empty.tooltip")
                .withStyle(ChatFormatting.GRAY));
        }
    }
    
    private String getAnomalyTypeKey() {
        switch (anomalyType) {
            case GRAVITY: return "gravity";
            case ENERGETIC: return "energetic";
            case ECHOING_SHADOW: return "echoing_shadow";
            case TEMPORAL_BLOOM: return "temporal_bloom";
            case THOUGHTWELL: return "thoughtwell";
            case WARP_GATE: return "warp_gate";
            default: return "empty";
        }
    }
    
    public enum AnomalyType {
        NONE,
        GRAVITY,
        ENERGETIC,
        ECHOING_SHADOW,
        TEMPORAL_BLOOM,
        THOUGHTWELL,
        WARP_GATE
    }
    
    // Registry objects for all capsule types - these will be set by StrangeMatterMod
    public static net.minecraftforge.registries.RegistryObject<Item> EMPTY_CAPSULE;
    public static net.minecraftforge.registries.RegistryObject<Item> GRAVITY_CAPSULE;
    public static net.minecraftforge.registries.RegistryObject<Item> ENERGETIC_CAPSULE;
    public static net.minecraftforge.registries.RegistryObject<Item> ECHOING_SHADOW_CAPSULE;
    public static net.minecraftforge.registries.RegistryObject<Item> TEMPORAL_BLOOM_CAPSULE;
    public static net.minecraftforge.registries.RegistryObject<Item> THOUGHTWELL_CAPSULE;
    public static net.minecraftforge.registries.RegistryObject<Item> WARP_GATE_CAPSULE;
}
