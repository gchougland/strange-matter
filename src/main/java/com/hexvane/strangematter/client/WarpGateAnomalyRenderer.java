package com.hexvane.strangematter.client;

import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
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
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Renderer for the Warp Gate Anomaly Entity.
 * Creates a swirling vortex effect with space-tear visuals.
 */
public class WarpGateAnomalyRenderer extends EntityRenderer<WarpGateAnomalyEntity> {
    
    private static final ResourceLocation WARP_GATE_TEXTURE = ResourceLocation.fromNamespaceAndPath("strangematter", "textures/entity/warp_gate_anomaly.png");
    private static final ResourceLocation VORTEX_TEXTURE = ResourceLocation.fromNamespaceAndPath("strangematter", "textures/entity/warp_gate_vortex.png");
    
    public WarpGateAnomalyRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(WarpGateAnomalyEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight) {
        
        
        poseStack.pushPose();
        
        // Get entity data
        float rotation = entity.getRotation();
        boolean isActive = entity.isActive();
        
        // Fixed size - no more pulsing
        float baseSize = 2.0f;
        float size = baseSize;
        
        // Position the render and make it billboard face the player
        poseStack.translate(0.0, 1.0, 0.0);
        
        // Billboard to face the camera using Minecraft's proper method
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        
        // Render the main warp gate portal
        renderWarpGatePortal(entity, poseStack, bufferSource, packedLight, size, rotation, isActive);
        
        // Render the swirling vortex effect (offset slightly to prevent z-fighting)
        renderVortexEffect(poseStack, bufferSource, packedLight, size, rotation);
        
        // Render particle cylinder effect
        renderParticleCylinder(poseStack, bufferSource, packedLight, size);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
    
    private void renderWarpGatePortal(WarpGateAnomalyEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, 
                                    float size, float rotation, boolean isActive) {
        
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        
        // Main portal ring - always visible with base alpha
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation * 0.5f));
        poseStack.translate(0.0, 0.0, -0.01f); // Inverted Z offset so it's behind other layers
        float baseAlpha = 0.8f; // Base visibility
        renderQuadWithWarbling(poseStack, vertexConsumer, size, size, 0.0f, 0.0f, 1.0f, 1.0f, packedLight, baseAlpha);
        poseStack.popPose();
        
        // Inner portal (if active) - more visible when active
        if (isActive) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(-rotation * 0.3f));
            poseStack.translate(0.0, 0.0, -0.05f); // Inverted Z offset so inner portal is in front
            float innerAlpha = 0.6f;
            renderQuadWithWarbling(poseStack, vertexConsumer, size * 0.6f, size * 0.6f, 0.0f, 0.0f, 1.0f, 1.0f, packedLight, innerAlpha);
            poseStack.popPose();
        }
    }
    
    private void renderVortexEffect(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, 
                                  float size, float rotation) {
        
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(VORTEX_TEXTURE));
        
        // Multiple swirling layers - offset slightly to prevent z-fighting
        for (int i = 0; i < 3; i++) {
            poseStack.pushPose();
            float layerRotation = rotation + (i * 120.0f);
            float layerSize = size * (0.8f - i * 0.2f);
            float baseLayerAlpha = 0.7f - (i * 0.15f); // Much more visible vortex layers
            
            // Inverted Z offset for vortex layers so they appear in front
            poseStack.translate(0.0, 0.0, -0.02f - (i * 0.01f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(layerRotation));
            renderQuadWithWarbling(poseStack, vertexConsumer, layerSize, layerSize, 0.0f, 0.0f, 1.0f, 1.0f, packedLight, baseLayerAlpha);
            poseStack.popPose();
        }
    }
    
    private void renderParticleCylinder(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float size) {
        
        // Number of particles in the cylinder
        int particleCount = 24; // Increased from 16
        float cylinderRadius = size * 0.8f;
        float cylinderHeight = size * 2.0f;
        
        // Time-based animation
        float time = (System.currentTimeMillis() % 360000) / 1000.0f;
        
        for (int i = 0; i < particleCount; i++) {
            poseStack.pushPose();
            
            // Distribute particles in a cylinder around the warp gate
            float angle = (i * 360.0f / particleCount) + (time * 10.0f); // Slow rotation
            float x = (float) (Math.cos(Math.toRadians(angle)) * cylinderRadius);
            float z = (float) (Math.sin(Math.toRadians(angle)) * cylinderRadius);
            
            // Height animation - particles rise up and fade out
            float heightPhase = (time * 2.0f + i * 0.3f) % (cylinderHeight * 2.0f);
            float y = (heightPhase - cylinderHeight) * 0.5f; // Start below and rise up
            
            // Calculate fade based on height (fade in at bottom, fade out at top)
            float fadeProgress = heightPhase / (cylinderHeight * 2.0f);
            float particleAlpha = (float) (Math.sin(fadeProgress * Math.PI)) * 0.6f;
            
            // Skip particles that are too faded or below ground
            if (particleAlpha > 0.1f && y > -cylinderHeight * 0.5f) {
                poseStack.translate(x, y, z);
                
                // Small random rotation for each particle
                float rotation = time * 20.0f + i * 15.0f;
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
                
                // Vary particle sizes (smaller max size)
                float particleSize = 0.08f + (i % 4) * 0.03f;
                poseStack.scale(particleSize, particleSize, particleSize);
                
                // Render colored particle (purple/blue theme for warp gate)
                renderColoredParticle(poseStack, bufferSource, packedLight, 0x8A2BE2, particleAlpha);
            }
            
            poseStack.popPose();
        }
    }
    
    private void renderColoredParticle(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int color, float alpha) {
        // Extract RGB components from hex color
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(
            new ResourceLocation("minecraft", "textures/block/white_wool.png")));
        
        // Render double-sided quad particle
        float halfSize = 0.5f;
        
        // Front face
        vertexConsumer.vertex(matrix, -halfSize, -halfSize, 0.0f)
            .color(r, g, b, alpha)
            .uv(0.0f, 1.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, halfSize, -halfSize, 0.0f)
            .color(r, g, b, alpha)
            .uv(1.0f, 1.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, halfSize, halfSize, 0.0f)
            .color(r, g, b, alpha)
            .uv(1.0f, 0.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, -halfSize, halfSize, 0.0f)
            .color(r, g, b, alpha)
            .uv(0.0f, 0.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
        
        // Back face (flipped winding order)
        vertexConsumer.vertex(matrix, -halfSize, halfSize, 0.0f)
            .color(r, g, b, alpha)
            .uv(0.0f, 0.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, -1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, halfSize, halfSize, 0.0f)
            .color(r, g, b, alpha)
            .uv(1.0f, 0.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, -1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, halfSize, -halfSize, 0.0f)
            .color(r, g, b, alpha)
            .uv(1.0f, 1.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, -1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, -halfSize, -halfSize, 0.0f)
            .color(r, g, b, alpha)
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
        float warblingStrength = 0.15f; // Toned down slightly
        
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
    public ResourceLocation getTextureLocation(WarpGateAnomalyEntity entity) {
        return WARP_GATE_TEXTURE;
    }
}
