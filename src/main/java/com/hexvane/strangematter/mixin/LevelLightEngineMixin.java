package com.hexvane.strangematter.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import com.hexvane.strangematter.world.ShadowLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelLightEngine.class)
public class LevelLightEngineMixin {
    
    // Test if mixin is being applied at all
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        System.out.println("MIXIN APPLIED: LevelLightEngine constructor called!");
    }
    
    // Try targeting a method that's definitely called
    @Inject(method = "getLightValue", at = @At("RETURN"), cancellable = true)
    private void onGetLightValue(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        ServerLevel serverLevel = getServerLevel();
        if (serverLevel != null) {
            ShadowLightProvider provider = ShadowLightProvider.getInstance(serverLevel);
            
            int originalLight = cir.getReturnValue();
            int modifiedLight = provider.getModifiedLightLevel(pos, LightLayer.BLOCK, originalLight);
            
            if (modifiedLight != originalLight) {
                cir.setReturnValue(modifiedLight);
                System.out.println("LIGHT VALUE MODIFIED: " + originalLight + " -> " + modifiedLight + " at " + pos);
            }
        }
    }
    
    @Inject(method = "getLayerLightValue", at = @At("RETURN"), cancellable = true)
    private void onGetLayerLightValue(LightLayer lightLayer, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        ServerLevel serverLevel = getServerLevel();
        if (serverLevel != null) {
            ShadowLightProvider provider = ShadowLightProvider.getInstance(serverLevel);
            
            int originalLight = cir.getReturnValue();
            int modifiedLight = provider.getModifiedLightLevel(pos, lightLayer, originalLight);
            
            if (modifiedLight != originalLight) {
                cir.setReturnValue(modifiedLight);
                System.out.println("LAYER LIGHT MODIFIED: " + originalLight + " -> " + modifiedLight + " (" + lightLayer + ") at " + pos);
            }
        }
    }
    
    // Try targeting a different method that might be called more frequently
    @Inject(method = "getRawBrightness", at = @At("RETURN"), cancellable = true)
    private void onGetRawBrightness(BlockPos pos, int defaultValue, CallbackInfoReturnable<Integer> cir) {
        ServerLevel serverLevel = getServerLevel();
        if (serverLevel != null) {
            ShadowLightProvider provider = ShadowLightProvider.getInstance(serverLevel);
            
            int originalLight = cir.getReturnValue();
            int modifiedLight = provider.getModifiedLightLevel(pos, LightLayer.BLOCK, originalLight);
            
            if (modifiedLight != originalLight) {
                cir.setReturnValue(modifiedLight);
                System.out.println("RAW BRIGHTNESS MODIFIED: " + originalLight + " -> " + modifiedLight + " at " + pos);
            }
        }
    }
    
    private ServerLevel getServerLevel() {
        // Use reflection to get the server level from the light engine
        try {
            java.lang.reflect.Field levelField = LevelLightEngine.class.getDeclaredField("level");
            levelField.setAccessible(true);
            Object level = levelField.get(this);
            if (level instanceof ServerLevel serverLevel) {
                return serverLevel;
            }
        } catch (Exception e) {
            // Fallback: try to get from chunk source
            try {
                java.lang.reflect.Field chunkSourceField = LevelLightEngine.class.getDeclaredField("chunkSource");
                chunkSourceField.setAccessible(true);
                Object chunkSource = chunkSourceField.get(this);
                if (chunkSource instanceof net.minecraft.server.level.ServerChunkCache serverChunkCache) {
                    Object level = serverChunkCache.getLevel();
                    if (level instanceof ServerLevel serverLevel) {
                        return serverLevel;
                    }
                }
            } catch (Exception ex) {
                // If all else fails, return null
            }
        }
        return null;
    }
}
