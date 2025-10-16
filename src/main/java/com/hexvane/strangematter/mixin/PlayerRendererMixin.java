package com.hexvane.strangematter.mixin;

import com.hexvane.strangematter.client.PlayerMorphRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept player rendering and render morphed entities instead
 */
@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @Inject(
        method = "render",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderPlayer(AbstractClientPlayer player, float entityYaw, float partialTicks,
                               PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                               CallbackInfo ci) {
        
        try {
            // Get the entity render dispatcher
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            
            // Try to render as morph
            boolean renderedAsMorph = PlayerMorphRenderer.renderPlayerAsMorph(
                player,
                dispatcher,
                poseStack,
                buffer,
                packedLight,
                partialTicks
            );
            
            if (renderedAsMorph) {
                // Successfully rendered as morph, cancel normal rendering
                ci.cancel();
            }
        } catch (NoClassDefFoundError e) {
            // If there's a class loading issue, just continue with normal rendering
            LOGGER.warn("Failed to load morph renderer classes, falling back to normal rendering: {}", e.getMessage());
        } catch (Exception e) {
            // Any other exception should also fall back to normal rendering
            LOGGER.error("Error in morph rendering, falling back to normal rendering", e);
        }
    }
}

