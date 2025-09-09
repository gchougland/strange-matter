package com.hexvane.strangematter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class FieldScannerRenderer implements IClientItemExtensions {
    
    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, 
                                         ItemStack itemInHand, float partialTick, float equipProcess, 
                                         float swingProcess) {
        
        // Check if the item is being used (scanning)
        if (isScanning(itemInHand)) {
            // Custom scanning animation - hold scanner out in front
            float scanProgress = getScanProgress(itemInHand);
            float animationProgress = scanProgress / 40.0f; // SCAN_DURATION = 40
            
            // Move the scanner forward to look like it's being held out
            float forwardMovement = animationProgress * 0.3f; // Move forward to hold it out
            poseStack.translate(0.56f, -0.52f, -forwardMovement-0.7f); // Try positive Z for forward
            
            // Add subtle scanning vibration effect
            float vibration = (float) Math.sin(animationProgress * Math.PI * 8) * 0.01f;
            poseStack.translate(vibration, 0.0f, 0.0f);
            
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
            // Add slight downward rotation to point the scanner forward
            float rotation = animationProgress * -10.0f; // Point downward slightly
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation-45.0f));
            
            return true; // Apply custom transform only when scanning
        }
        
        return false; // Use default rendering when not scanning
    }
    
    private boolean isScanning(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getBoolean("scanning");
        }
        return false;
    }
    
    private int getScanProgress(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt("scan_progress");
        }
        return 0;
    }
}
