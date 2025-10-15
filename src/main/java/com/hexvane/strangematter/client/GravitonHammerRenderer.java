package com.hexvane.strangematter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class GravitonHammerRenderer implements IClientItemExtensions {
    
    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, 
                                         ItemStack itemInHand, float partialTick, float equipProcess, 
                                         float swingProcess) {
        
        // Check if the hammer is being charged
        if (isCharging(itemInHand)) {
            int chargeLevel = getChargeLevel(itemInHand);
            float chargeProgress = getChargeProgress(itemInHand);
            
            // Calculate pull-back distance based on charge level and progress
            float pullBackAmount = calculatePullBackAmount(chargeLevel, chargeProgress);
            
            // Apply the pull-back transform
            applyChargeTransform(poseStack, pullBackAmount, arm);
            
            return true; // Apply custom transform only when charging
        }
        
        return false; // Use default rendering when not charging
    }
    
    private boolean isCharging(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getBoolean("charging");
        }
        return false;
    }
    
    private int getChargeLevel(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt("charge_level");
        }
        return 0;
    }
    
    private float getChargeProgress(ItemStack stack) {
        if (stack.hasTag()) {
            int chargeTime = stack.getTag().getInt("charge_time");
            int currentLevel = getChargeLevel(stack);
            
            // Calculate progress within the current charge level
            int levelStartTime = getLevelStartTime(currentLevel);
            int levelEndTime = getLevelEndTime(currentLevel);
            
            if (levelEndTime > levelStartTime) {
                return Math.min(1.0f, (float) (chargeTime - levelStartTime) / (levelEndTime - levelStartTime));
            }
        }
        return 0.0f;
    }
    
    private int getLevelStartTime(int level) {
        switch (level) {
            case 1: return 0;
            case 2: return 20; // CHARGE_LEVEL_1_TIME
            case 3: return 40; // CHARGE_LEVEL_2_TIME
            default: return 0;
        }
    }
    
    private int getLevelEndTime(int level) {
        switch (level) {
            case 1: return 20; // CHARGE_LEVEL_1_TIME
            case 2: return 40; // CHARGE_LEVEL_2_TIME
            case 3: return 60; // CHARGE_LEVEL_3_TIME
            default: return 20;
        }
    }
    
    private float calculatePullBackAmount(int chargeLevel, float chargeProgress) {
        // Base pull-back for each charge level
        float basePullBack = 0.6f;
        
        switch (chargeLevel) {
            case 1:
                basePullBack = 0.5f; // Level 1: slight pull-back
                break;
            case 2:
                basePullBack = 0.4f; // Level 2: medium pull-back
                break;
            case 3:
                basePullBack = 0.3f; // Level 3: maximum pull-back
                break;
            default:
                basePullBack = 0.6f; // Default: minimal pull-back
                break;
        }
        
        // Apply progress within the current level
        return basePullBack * chargeProgress;
    }
    
    private void applyChargeTransform(PoseStack poseStack, float pullBackAmount, HumanoidArm arm) {
        // Pull the hammer back (negative Z translation)
        poseStack.translate(0.0f, 0.0f, -pullBackAmount);
        
        // Add slight upward rotation to make it look like pulling back for a swing
        float rotationAmount = pullBackAmount * 15.0f; // Rotate up as we pull back
        poseStack.mulPose(Axis.XP.rotationDegrees(rotationAmount));
        
        // Add slight outward rotation to make it look more natural
        float outwardRotation = pullBackAmount * 10.0f;
        if (arm == HumanoidArm.RIGHT) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(outwardRotation));
        } else {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-outwardRotation));
        }
    }
}
