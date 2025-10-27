package com.hexvane.strangematter.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.lighting.LightEngine;
import com.hexvane.strangematter.world.ShadowLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightEngine.class)
public class LightEngineMixin {
    
    @Inject(method = "getLightValue(Lnet/minecraft/core/BlockPos;)I", at = @At("RETURN"), cancellable = true, remap = false)
    private void onGetLightValue(BlockPos levelPos, CallbackInfoReturnable<Integer> cir) {
        ServerLevel serverLevel = getServerLevel();
        // Debug: Log if we couldn't get the server level
        if (serverLevel == null) {
            // Only log once to avoid spam
            if (levelPos.getX() == 0 && levelPos.getZ() == 0) {
                System.out.println("LightEngineMixin: Could not get ServerLevel, reflect info:");
                // Try to list all fields
                try {
                    java.lang.reflect.Field[] fields = LightEngine.class.getDeclaredFields();
                    for (java.lang.reflect.Field field : fields) {
                        System.out.println("  Field: " + field.getName() + " type=" + field.getType().getName());
                    }
                } catch (Exception e) {
                    System.out.println("  Could not list fields: " + e.getMessage());
                }
            }
            return;
        }
        
        ShadowLightProvider provider = ShadowLightProvider.getInstance(serverLevel);
        
        // Check if position is in shadow radius
        boolean inShadow = provider.isPositionInShadow(levelPos);
        int originalLight = cir.getReturnValue();
        System.out.println("LightEngineMixin: pos=" + levelPos + " inShadow=" + inShadow + " light=" + originalLight);
        if (inShadow) {
            // Reduce light by 10 (don't go below 0)
            int modifiedLight = Math.max(0, originalLight - 10);
            cir.setReturnValue(modifiedLight);
        }
    }
    
    private ServerLevel getServerLevel() {
        // Use reflection to get the server level from the light engine
        try {
            // Try chunkSource field (it's declared directly in LightEngine)
            java.lang.reflect.Field chunkSourceField = LightEngine.class.getDeclaredField("chunkSource");
            chunkSourceField.setAccessible(true);
            Object chunkSource = chunkSourceField.get(this);
            
            // Check if it's a ServerChunkCache
            if (chunkSource instanceof net.minecraft.server.level.ServerChunkCache serverChunkCache) {
                Object level = serverChunkCache.getLevel();
                if (level instanceof ServerLevel serverLevel) {
                    return serverLevel;
                }
            }
        } catch (Exception e) {
            // Try to get from storage
            try {
                java.lang.reflect.Field storageField = LightEngine.class.getDeclaredField("storage");
                storageField.setAccessible(true);
                Object storage = storageField.get(this);
                // Try to get level from storage if possible
                // This is a fallback
            } catch (Exception ex) {
                // If all else fails, return null
            }
        }
        return null;
    }
}
