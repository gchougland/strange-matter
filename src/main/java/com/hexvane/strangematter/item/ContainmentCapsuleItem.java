package com.hexvane.strangematter.item;

import com.hexvane.strangematter.entity.ThrowableContainmentCapsuleEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        // Only allow throwing if the capsule contains an anomaly
        if (hasAnomaly()) {
            if (!level.isClientSide) {
                ThrowableContainmentCapsuleEntity throwableCapsule = new ThrowableContainmentCapsuleEntity(
                    level, player, itemStack);
                
                // Calculate throw direction and velocity
                Vec3 lookDirection = player.getLookAngle();
                Vec3 throwVelocity = lookDirection.multiply(1.5, 1.5, 1.5);
                throwableCapsule.shoot(throwVelocity.x, throwVelocity.y, throwVelocity.z, 1.0f, 1.0f);
                
                level.addFreshEntity(throwableCapsule);
                
                // Play potion throw sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5f, 0.4f);
            }
            
            // Consume the capsule when thrown
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            
            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
        }
        
        return InteractionResultHolder.pass(itemStack);
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
            
            // Add throwing instruction
            tooltip.add(Component.literal("Right click to throw and release anomaly")
                .withStyle(ChatFormatting.YELLOW));
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
