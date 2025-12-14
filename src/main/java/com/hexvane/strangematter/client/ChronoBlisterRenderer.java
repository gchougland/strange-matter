package com.hexvane.strangematter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class ChronoBlisterRenderer implements IClientItemExtensions {
    
    private static final String CHARGING_TAG = "charging";
    private static final String CHARGE_TIME_TAG = "charge_time";
    private static final int MAX_CHARGE_TIME = 20;
    
    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, 
                                         ItemStack itemInHand, float partialTick, float equipProcess, 
                                         float swingProcess) {
        
        // Check if the item is being charged
        if (isCharging(itemInHand)) {
            int chargeTime = getChargeTime(itemInHand);
            float chargeProgress = Math.min(1.0f, (float) chargeTime / MAX_CHARGE_TIME);
            
            // Custom charging animation - hold gun forward and slightly up
            // Position the gun in front of the player
            float forwardMovement = 0.3f + (chargeProgress * 0.2f); // Move forward as it charges
            float upMovement = -0.1f + (chargeProgress * 0.1f); // Slight upward movement
            
            // Adjust position based on arm
            float armOffset = arm == HumanoidArm.RIGHT ? 0.56f : -0.56f;
            poseStack.translate(armOffset, -0.52f + upMovement, -forwardMovement - 0.7f);
            
            // Rotate to point forward and to the right
            poseStack.mulPose(Axis.YP.rotationDegrees(15.0f)); // Point more to the right (was 90.0f)
            
            // Slight downward rotation to aim forward
            float rotation = -15.0f - (chargeProgress * 5.0f); // Point forward and slightly down
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
            
            // Add subtle charging vibration effect
            float vibration = (float) Math.sin(chargeProgress * Math.PI * 4) * 0.005f;
            poseStack.translate(vibration, 0.0f, 0.0f);
            
            return true; // Apply custom transform only when charging
        }
        
        return false; // Use default rendering when not charging
    }
    
    private boolean isCharging(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(CHARGING_TAG);
    }
    
    private int getChargeTime(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(CHARGE_TIME_TAG) : 0;
    }
}

