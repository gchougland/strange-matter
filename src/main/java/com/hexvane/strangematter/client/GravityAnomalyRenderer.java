package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.client.obj.OBJLoader;
import com.hexvane.strangematter.client.obj.OBJLoader.OBJModel;
import com.hexvane.strangematter.entity.GravityAnomalyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class GravityAnomalyRenderer extends EntityRenderer<GravityAnomalyEntity> {
    
    private static final ResourceLocation ICOSAHEDRON_OBJ = new ResourceLocation(StrangeMatterMod.MODID, "models/entity/icosahedron.obj");
    private static final ResourceLocation AURA_TEXTURE = new ResourceLocation(StrangeMatterMod.MODID, "textures/entity/gravity_anomaly_aura.png");
    
    private OBJModel icosahedronModel;
    
    public GravityAnomalyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        
        // Load the OBJ model
        try {
            this.icosahedronModel = OBJLoader.loadModel(ICOSAHEDRON_OBJ);
            System.out.println("Custom OBJ model loaded successfully with " + icosahedronModel.faces.size() + " faces");
        } catch (Exception e) {
            System.err.println("Failed to load custom OBJ model: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public ResourceLocation getTextureLocation(GravityAnomalyEntity entity) {
        return new ResourceLocation(StrangeMatterMod.MODID, "textures/entity/gravity_anomaly_icosahedron.png");
    }
    
    @Override
    public void render(GravityAnomalyEntity entity, float entityYaw, float partialTicks, 
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        
        poseStack.pushPose();
        
        // Get animation data
        float rotation = entity.getRotation() + (partialTicks * 0.5f);
        float pulseIntensity = entity.getPulseIntensity();
        
        // Render in proper order to avoid transparency issues:
        // 1. First render opaque/solid elements
        renderIcosahedron(poseStack, buffer, packedLight, rotation, pulseIntensity);
        
        // 2. Then render translucent elements with proper depth testing
        // Render the outer aura icosahedron (translucent)
        renderAuraIcosahedron(poseStack, buffer, packedLight, partialTicks);
        
        // Render floating dirt particles (translucent)
        renderFloatingParticles(poseStack, buffer, packedLight, pulseIntensity, partialTicks);
        
        // Render ground particle field (translucent)
        renderGroundParticleField(poseStack, buffer, packedLight, partialTicks);
        
        // Render emissive green glow effect (translucent)
        renderGlowEffect(poseStack, buffer, packedLight, rotation, pulseIntensity);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    
    private void renderIcosahedron(PoseStack poseStack, MultiBufferSource buffer, 
                                 int packedLight, float rotation, float pulseIntensity) {
        
        if (icosahedronModel == null) {
            System.err.println("Icosahedron model is null, cannot render");
            return;
        }
        
        poseStack.pushPose();
        
        // Apply rotation (no camera orientation - keep it in world space)
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
        
        // Apply pulsing scale effect and size adjustment
        float scale = (0.8f + (pulseIntensity * 0.2f)); // Increased scale for the icosahedron
        poseStack.scale(scale, scale, scale);
        
        // Render the model using our custom OBJ loader
        icosahedronModel.render(poseStack, buffer, getTextureLocation(null), packedLight, 1.0f, 1.0f, 1.0f, 1.0f);
        
        poseStack.popPose();
    }
    
    private void renderAuraIcosahedron(PoseStack poseStack, MultiBufferSource buffer, 
                                     int packedLight, float partialTicks) {
        
        if (icosahedronModel == null) {
            return;
        }
        
        poseStack.pushPose();
        
        // Apply slower rotation (opposite direction and much slower speed)
        float auraRotation = -(System.currentTimeMillis() % 8000) / 8000.0f * 360.0f; // 8 second cycle, opposite direction
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(auraRotation));
        
        // Make it larger than the main icosahedron so it completely surrounds it
        float auraScale = 1.5f; // 50% larger to ensure main icosahedron is inside
        poseStack.scale(auraScale, auraScale, auraScale);
        
        // Render the aura icosahedron with translucent white color (let texture handle colors)
        icosahedronModel.render(poseStack, buffer, AURA_TEXTURE, packedLight, 1.0f, 1.0f, 1.0f, 0.4f);
        
        poseStack.popPose();
    }
    
    private void renderGlowEffect(PoseStack poseStack, MultiBufferSource buffer, 
                                int packedLight, float rotation, float pulseIntensity) {
        
        // Face the camera by applying the camera rotation
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        
        // Add rotation around the perpendicular axis (Z-axis since quad is in XY plane)
        float glowRotation = (System.currentTimeMillis() % 6000) / 6000.0f * 360.0f; // 6 second cycle
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(glowRotation));
        
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(
            new ResourceLocation("minecraft", "textures/block/white_wool.png")));
        
        // Calculate glow intensity based on pulse
        float glowIntensity = 0.2f + (pulseIntensity * 0.3f);
        float glowSize = 1.5f + (pulseIntensity * 0.3f);
        
        // Render a circular glow with vertex color fading from center to edge
        int segments = 16;
        float centerAlpha = glowIntensity;
        float edgeAlpha = 0.0f;
        
        for (int i = 0; i < segments; i++) {
            float angle1 = (i / (float) segments) * 2.0f * (float) Math.PI;
            float angle2 = ((i + 1) / (float) segments) * 2.0f * (float) Math.PI;
            
            // Center vertex (bright)
            vertexConsumer.vertex(matrix4f, 0, 0, 0).color(0.2f, 1.0f, 0.3f, centerAlpha).uv(0.5f, 0.5f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
            
            // Edge vertices (fade to transparent)
            float x1 = (float) (Math.cos(angle1) * glowSize);
            float y1 = (float) (Math.sin(angle1) * glowSize);
            float x2 = (float) (Math.cos(angle2) * glowSize);
            float y2 = (float) (Math.sin(angle2) * glowSize);
            
            vertexConsumer.vertex(matrix4f, x1, y1, 0).color(0.2f, 1.0f, 0.3f, edgeAlpha).uv(0.5f + x1 * 0.1f, 0.5f + y1 * 0.1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix4f, x2, y2, 0).color(0.2f, 1.0f, 0.3f, edgeAlpha).uv(0.5f + x2 * 0.1f, 0.5f + y2 * 0.1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        }
    }
    
    private void renderFloatingParticles(PoseStack poseStack, MultiBufferSource buffer, 
                                      int packedLight, float pulseIntensity, float partialTicks) {
        
        // Number of particles to render
        int particleCount = 12;
        
        // Base distance from center
        float baseDistance = 2.0f;
        
        // Fixed distance - no pulsing
        float pulseDistance = baseDistance;
        
        for (int i = 0; i < particleCount; i++) {
            poseStack.pushPose();
            
            // Calculate spherical position around the icosahedron
            // Use golden ratio to distribute points evenly on a sphere
            float phi = (float) Math.acos(1.0f - 2.0f * (i + 0.5f) / particleCount);
            float theta = (float) (Math.PI * (1.0f + Math.sqrt(5.0f)) * (i + 0.5f));
            
            float x = (float) (Math.sin(phi) * Math.cos(theta) * pulseDistance);
            float y = (float) (Math.sin(phi) * Math.sin(theta) * pulseDistance);
            float z = (float) (Math.cos(phi) * pulseDistance);
            
            poseStack.translate(x, y, z);
            
            // Rotation for each particle - each has different speeds and directions
            float time = (System.currentTimeMillis() % 360000) / 1000.0f; // 6 minute cycle (360 seconds)
            float rotationX = (i * 37.0f + time * (10.0f + (i % 3) * 5.0f)) % 360.0f;
            float rotationY = (i * 73.0f + time * (8.0f + (i % 2) * 7.0f)) % 360.0f;
            float rotationZ = (i * 29.0f + time * (12.0f + (i % 4) * 3.0f)) % 360.0f;
            
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rotationX));
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationY));
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotationZ));
            
            // Varying sizes for particles
            float size = 0.1f + (i % 5) * 0.08f; // 0.1f, 0.18f, 0.26f, 0.34f, 0.42f
            poseStack.scale(size, size, size);
            
            // Render dirt block particle
            renderDirtBlock(poseStack, buffer, packedLight);
            
            poseStack.popPose();
        }
    }
    
    private void renderGroundParticleField(PoseStack poseStack, MultiBufferSource buffer, 
                                         int packedLight, float partialTicks) {
        
        // Number of ground particles
        int particleCount = 24;
        
        // Effect radius from the entity
        float effectRadius = 4.0f; // Same as LEVITATION_RADIUS
        
        // Time for animation
        float time = (System.currentTimeMillis() % 360000) / 1000.0f;
        
        for (int i = 0; i < particleCount; i++) {
            poseStack.pushPose();
            
            // Distribute particles in a circle around the anomaly
            float angle = (i * 360.0f / particleCount) + (time * 5.0f); // Slow rotation
            float distance = (i % 3 + 1) * (effectRadius / 3.0f); // Vary distance: 1.33, 2.67, 4.0
            
            float x = (float) (Math.cos(Math.toRadians(angle)) * distance);
            float z = (float) (Math.sin(Math.toRadians(angle)) * distance);
            
            // Height animation - particles rise and fall
            float heightOffset = (float) Math.sin(time * 2.0f + i * 0.5f) * 0.3f + 0.1f;
            float y = -1.0f + heightOffset; // Start below ground level
            
            poseStack.translate(x, y, z);
            
            // Small rotation for each particle
            float rotation = time * 20.0f + i * 15.0f;
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
            
            // Small size for ground particles
            float size = 0.05f + (i % 3) * 0.02f; // 0.05f, 0.07f, 0.09f
            poseStack.scale(size, size, size);
            
            // Render colored particle
            renderColoredParticle(poseStack, buffer, packedLight, 0x41b280);
            
            poseStack.popPose();
        }
    }
    
    private void renderColoredParticle(PoseStack poseStack, MultiBufferSource buffer, 
                                     int packedLight, int color) {
        // Extract RGB components from hex color #41b280
        float r = ((color >> 16) & 0xFF) / 255.0f; // 0x41 = 65/255 = 0.255
        float g = ((color >> 8) & 0xFF) / 255.0f;  // 0xb2 = 178/255 = 0.698
        float b = (color & 0xFF) / 255.0f;         // 0x80 = 128/255 = 0.502
        
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentCull(
            ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/entity/gravity_anomaly_aura.png")));
        
        // Render a simple cube with the specified color
        float halfSize = 0.5f;
        
        // Front face
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, halfSize).color(r, g, b, 0.8f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, halfSize).color(r, g, b, 0.8f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, halfSize).color(r, g, b, 0.8f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, halfSize).color(r, g, b, 0.8f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        
        // Back face
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, -halfSize).color(r, g, b, 0.8f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, -1).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, -halfSize).color(r, g, b, 0.8f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, -1).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, -halfSize).color(r, g, b, 0.8f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, -1).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, -halfSize).color(r, g, b, 0.8f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, -1).endVertex();
        
        // Top face
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, -halfSize).color(r, g, b, 0.8f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, halfSize).color(r, g, b, 0.8f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, halfSize).color(r, g, b, 0.8f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, -halfSize).color(r, g, b, 0.8f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
        
        // Bottom face
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, -halfSize).color(r, g, b, 0.8f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, -1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, -halfSize).color(r, g, b, 0.8f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, -1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, halfSize).color(r, g, b, 0.8f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, -1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, halfSize).color(r, g, b, 0.8f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, -1, 0).endVertex();
        
        // Right face
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, -halfSize).color(r, g, b, 0.8f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(1, 0, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, -halfSize).color(r, g, b, 0.8f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(1, 0, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, halfSize).color(r, g, b, 0.8f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(1, 0, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, halfSize).color(r, g, b, 0.8f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(1, 0, 0).endVertex();
        
        // Left face
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, -halfSize).color(r, g, b, 0.8f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(-1, 0, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, halfSize).color(r, g, b, 0.8f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(-1, 0, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, halfSize).color(r, g, b, 0.8f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(-1, 0, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, -halfSize).color(r, g, b, 0.8f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(-1, 0, 0).endVertex();
    }
    
    private void renderDirtBlock(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Create a simple colored cube for the dirt block (no texture for now)
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentCull(
            new ResourceLocation("minecraft", "textures/block/dirt.png")));
        
        // Render a simple cube (6 faces) with no color tinting
        float halfSize = 0.5f;
        
        // Front face
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        
        // Back face
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, -halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, -1).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, -halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, -1).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, -halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, -1).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, -halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, -1).endVertex();
        
        // Top face
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, -halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, -halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
        
        // Bottom face
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, -halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, -1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, -halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, -1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, -1, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, -1, 0).endVertex();
        
        // Right face
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, -halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(1, 0, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, -halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(1, 0, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(1, 0, 0).endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(1, 0, 0).endVertex();
        
        // Left face
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, -halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(-1, 0, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(-1, 0, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(-1, 0, 0).endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, -halfSize).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(-1, 0, 0).endVertex();
    }
}
