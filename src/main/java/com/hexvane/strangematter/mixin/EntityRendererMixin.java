package com.hexvane.strangematter.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept entity rendering and apply cognitive disguises when needed.
 * This allows any entity renderer to support cognitive disguises without modification.
 */
@Mixin(targets = "net.minecraft.client.renderer.entity.LivingEntityRenderer")
public class EntityRendererMixin<T extends net.minecraft.world.entity.LivingEntity> {
    
    @Inject(
        method = "render",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRender(T entity, float entityYaw, float partialTicks, 
                         PoseStack poseStack, MultiBufferSource buffer, int packedLight, 
                         CallbackInfo ci) {
        
        // Check if this is a mob with a cognitive disguise
        
        if (entity instanceof Mob mob && com.hexvane.strangematter.entity.ThoughtwellEntity.hasDisguise(mob.getUUID())) {
            
            // Get the entity render dispatcher directly from Minecraft instance
            EntityRenderDispatcher dispatcher = net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher();
            
            // Try to render with disguise
            boolean renderedWithDisguise = com.hexvane.strangematter.client.CognitiveDisguiseRenderer.renderWithDisguise(
                mob, 
                dispatcher, 
                poseStack, 
                buffer, 
                packedLight,
                partialTicks
            );
            
            if (renderedWithDisguise) {
                // Successfully rendered with disguise, cancel normal rendering
                ci.cancel();
            }
        }
    }
}
