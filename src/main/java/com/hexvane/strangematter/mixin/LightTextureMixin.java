package com.hexvane.strangematter.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.dimension.DimensionType;
import com.hexvane.strangematter.entity.EchoingShadowEntity;
import com.hexvane.strangematter.StrangeMatterMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightTexture.class)
public class LightTextureMixin {
    
    @Inject(method = "getBrightness", at = @At("RETURN"), cancellable = true)
    private static void onGetBrightness(DimensionType dimensionType, int lightLevel, CallbackInfoReturnable<Float> cir) {
        // This method is called during rendering for light calculations
        float originalResult = cir.getReturnValue();
        
        // Check if we're near an Echoing Shadow anomaly
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.level != null) {
            BlockPos playerPos = minecraft.player.blockPosition();
            
            // Check if there's an Echoing Shadow nearby and calculate shadow strength
            float maxShadowStrength = 0.0f;
            for (EchoingShadowEntity shadow : minecraft.level.getEntitiesOfClass(EchoingShadowEntity.class, 
                    minecraft.player.getBoundingBox().inflate(20.0))) { // Check within 20 blocks
                
                double distance = shadow.position().distanceTo(minecraft.player.position());
                double maxRadius = shadow.getLightAbsorptionRadius();
                
                if (distance <= maxRadius) {
                    // Inner core (0-5 blocks): maximum shadow strength
                    if (distance <= 5.0) {
                        maxShadowStrength = Math.max(maxShadowStrength, 1.0f);
                    } else {
                        // Outer ring (6-10 blocks): gradual transition from full to no shadow
                        double transitionStart = 5.0;
                        double transitionEnd = maxRadius;
                        double transitionDistance = distance - transitionStart;
                        double transitionRange = transitionEnd - transitionStart;
                        
                        float shadowStrength = (float) (1.0 - (transitionDistance / transitionRange));
                        maxShadowStrength = Math.max(maxShadowStrength, shadowStrength);
                    }
                }
            }
            
            // Apply gradual shadow effect based on distance
            if (maxShadowStrength > 0.0f && lightLevel > 0) {
                // Calculate light reduction based on shadow strength
                // At full strength (center), reduce by 12 levels
                // At edge, reduce by 0 levels
                int lightReduction = (int) (12 * maxShadowStrength);
                int modifiedLight = Math.max(0, lightLevel - lightReduction);
                float modifiedBrightness = modifiedLight / 15.0f;
                
                cir.setReturnValue(modifiedBrightness);
            }
        }
    }
}
