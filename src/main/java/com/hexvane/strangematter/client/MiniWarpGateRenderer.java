package com.hexvane.strangematter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.hexvane.strangematter.entity.MiniWarpGateEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class MiniWarpGateRenderer extends EntityRenderer<MiniWarpGateEntity> {
    
    private static final ResourceLocation MINI_WARP_GATE_TEXTURE = ResourceLocation.fromNamespaceAndPath("strangematter", "textures/entity/warp_gate_anomaly.png");
    private static final ResourceLocation VORTEX_TEXTURE = ResourceLocation.fromNamespaceAndPath("strangematter", "textures/entity/warp_gate_vortex.png");
    
    public MiniWarpGateRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(MiniWarpGateEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight) {
        
        poseStack.pushPose();
        
        // Get entity data
        float rotation = entity.getRotation();
        boolean isActive = entity.isActive();
        boolean isOnCooldown = entity.isOnCooldown();
        float cooldownProgress = entity.getCooldownProgress();
        
        // Smaller size than the full warp gate anomaly
        float baseSize = 1.0f; // Half the size of the full warp gate
        float size = baseSize;
        
        // Shrink the gate when on cooldown
        if (isOnCooldown) {
            float shrinkFactor = 0.2f + (0.8f * cooldownProgress); // Shrink from 20% to 100% of original size
            size *= shrinkFactor;
        }
        
        // Position the render and make it billboard face the player
        poseStack.translate(0.0, 1.0, 0.0);
        
        // Billboard to face the camera using Minecraft's proper method
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        
        // Render the main mini warp gate portal
        renderMiniWarpGatePortal(entity, poseStack, bufferSource, packedLight, size, rotation, isActive, isOnCooldown, cooldownProgress);
        
        // Render the swirling vortex effect (offset slightly to prevent z-fighting)
        renderVortexEffect(poseStack, bufferSource, packedLight, size, rotation, isOnCooldown, cooldownProgress);
        
        // Render particle cylinder effect
        renderParticleCylinder(entity, poseStack, bufferSource, packedLight, size, isOnCooldown, cooldownProgress);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
    
    private void renderMiniWarpGatePortal(MiniWarpGateEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, 
                                        int packedLight, float size, float rotation, boolean isActive, boolean isOnCooldown, float cooldownProgress) {
        
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        
        // Main portal ring - always visible with base alpha
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation * 0.5f));
        poseStack.translate(0.0, 0.0, -0.01f); // Inverted Z offset so it's behind other layers
        float baseAlpha = 0.8f; // Base visibility
        
        // Reduce alpha when on cooldown
        if (isOnCooldown) {
            baseAlpha *= (0.1f + 0.9f * cooldownProgress); // Fade from 10% to 100% alpha
        }
        
        renderQuadWithWarbling(poseStack, vertexConsumer, size, size, 0.0f, 0.0f, 1.0f, 1.0f, packedLight, baseAlpha);
        poseStack.popPose();
        
        // Inner portal (if active) - more visible when active
        if (isActive) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(-rotation * 0.3f));
            poseStack.translate(0.0, 0.0, -0.05f); // Inverted Z offset so inner portal is in front
            float innerAlpha = 0.6f;
            
            // Reduce inner portal visibility when on cooldown
            if (isOnCooldown) {
                innerAlpha *= (0.05f + 0.95f * cooldownProgress); // Fade from 5% to 100% alpha
            }
            
            renderQuadWithWarbling(poseStack, vertexConsumer, size * 0.6f, size * 0.6f, 0.0f, 0.0f, 1.0f, 1.0f, packedLight, innerAlpha);
            poseStack.popPose();
        }
    }
    
    private void renderVortexEffect(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float size, float rotation, boolean isOnCooldown, float cooldownProgress) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(VORTEX_TEXTURE));
        
        // Vortex effect - rotating faster than the main portal
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation * 2.0f));
        poseStack.translate(0.0, 0.0, 0.01f); // Slightly in front of main portal
        float vortexAlpha = 0.4f;
        
        // Reduce vortex visibility when on cooldown
        if (isOnCooldown) {
            vortexAlpha *= (0.05f + 0.95f * cooldownProgress); // Fade from 5% to 100% alpha
        }
        
        renderQuad(poseStack, vertexConsumer, size * 0.8f, size * 0.8f, 0.0f, 0.0f, 1.0f, 1.0f, packedLight, vortexAlpha);
        poseStack.popPose();
    }
    
    private void renderParticleCylinder(MiniWarpGateEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float size, boolean isOnCooldown, float cooldownProgress) {
        // Render small particles around the mini warp gate
        float time = (System.currentTimeMillis() % 360000) / 1000.0f; // 6 minute cycle
        
        // Spawn particles in a cylinder pattern around the gate
        for (int i = 0; i < 8; i++) {
            float angle = (float) (i * Math.PI * 2 / 8 + time * 0.5);
            float radius = size * 0.6f;
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            float y = (float) Math.sin(time * 2 + i) * 0.3f; // Vertical oscillation
            
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            
            // Render particle with varying size and color based on portal type
            float alpha = 0.7f;
            float particleSize = 0.05f + (float) Math.sin(time * 3 + i) * 0.03f; // Vary size between 0.02 and 0.08
            
            // Reduce particle visibility when on cooldown
            if (isOnCooldown) {
                alpha *= (0.1f + 0.9f * cooldownProgress); // Fade from 10% to 100% alpha
                particleSize *= (0.3f + 0.7f * cooldownProgress); // Shrink particles from 30% to 100% size
            }
            
            renderColoredParticle(poseStack, bufferSource, packedLight, entity.isPurplePortal(), alpha, particleSize);
            poseStack.popPose();
        }
    }
    
    private void renderColoredParticle(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, boolean isPurple, float alpha, float size) {
        
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        ResourceLocation texture = isPurple ? 
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/purple_concrete.png") : 
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/cyan_concrete.png");
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(texture));
        
        // Render double-sided quad particle with variable size
        float halfSize = size / 2.0f;
        
        // Front face
        vertexConsumer.vertex(matrix, -halfSize, -halfSize, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(0.0f, 1.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, halfSize, -halfSize, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(1.0f, 1.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, halfSize, halfSize, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(1.0f, 0.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, -halfSize, halfSize, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(0.0f, 0.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
        
        // Back face (flipped winding order)
        vertexConsumer.vertex(matrix, -halfSize, halfSize, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(0.0f, 0.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, -1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, halfSize, halfSize, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(1.0f, 0.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, -1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, halfSize, -halfSize, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(1.0f, 1.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, -1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, -halfSize, -halfSize, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(0.0f, 1.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, -1.0f)
            .endVertex();
    }
    
    private void renderQuadWithWarbling(PoseStack poseStack, VertexConsumer vertexConsumer, float width, float height, 
                                       float u0, float v0, float u1, float v1, int packedLight, float alpha) {
        
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        
        // Simple time-based animation like gravity anomaly
        float time = (System.currentTimeMillis() % 360000) / 1000.0f; // 6 minute cycle like gravity anomaly
        
        // Calculate vertex positions
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        
        // Warbling effect - simple sine wave offsets for each vertex
        float warblingStrength = 0.1f; // Smaller warbling for mini gate
        
        // Calculate warbling offsets for each vertex corner
        float warbling1 = (float) Math.sin(time * 3.0f) * warblingStrength;
        float warbling2 = (float) Math.sin(time * 3.0f + 1.57f) * warblingStrength; // 90 degrees offset
        float warbling3 = (float) Math.sin(time * 3.0f + 3.14f) * warblingStrength; // 180 degrees offset
        float warbling4 = (float) Math.sin(time * 3.0f + 4.71f) * warblingStrength; // 270 degrees offset
        
        // Render the quad with warbled vertices
        vertexConsumer.vertex(matrix, -halfWidth + warbling1, -halfHeight + warbling1, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(u0, v1)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, halfWidth + warbling2, -halfHeight + warbling2, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(u1, v1)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, halfWidth + warbling3, halfHeight + warbling3, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(u1, v0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, -halfWidth + warbling4, halfHeight + warbling4, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(u0, v0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
    }
    
    private void renderQuad(PoseStack poseStack, VertexConsumer vertexConsumer, float width, float height, 
                          float u0, float v0, float u1, float v1, int packedLight, float alpha) {
        
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        
        // Calculate vertex positions
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        
        // Render the quad
        vertexConsumer.vertex(matrix, -halfWidth, -halfHeight, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(u0, v1)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, halfWidth, -halfHeight, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(u1, v1)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, halfWidth, halfHeight, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(u1, v0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, -halfWidth, halfHeight, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(u0, v0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
    }
    
    @Override
    public ResourceLocation getTextureLocation(MiniWarpGateEntity entity) {
        return MINI_WARP_GATE_TEXTURE;
    }
}
