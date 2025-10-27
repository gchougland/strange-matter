package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.client.obj.OBJLoader;
import com.hexvane.strangematter.client.obj.OBJLoader.OBJModel;
import com.hexvane.strangematter.entity.TemporalBloomEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import javax.annotation.Nonnull;

public class TemporalBloomRenderer extends EntityRenderer<TemporalBloomEntity> {
    
    private static final ResourceLocation TEMPORAL_BLOOM_OBJ = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "models/entity/temporal_bloom.obj");
    private static final ResourceLocation AURA_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/entity/temporal_bloom_aura.png");
    
    private OBJModel temporalBloomModel;
    
    public TemporalBloomRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        
        // Load the OBJ model
        try {
            this.temporalBloomModel = OBJLoader.loadModel(TEMPORAL_BLOOM_OBJ);
            System.out.println("Temporal Bloom OBJ model loaded successfully with " + temporalBloomModel.faces.size() + " faces");
        } catch (Exception e) {
            System.err.println("Failed to load Temporal Bloom OBJ model: " + e.getMessage());
            System.err.println("Make sure temporal_bloom.obj and temporal_bloom.mtl are in assets/strangematter/models/entity/");
            e.printStackTrace();
        }
    }
    
    @Override
    @Nonnull
    public ResourceLocation getTextureLocation(@Nonnull TemporalBloomEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/entity/temporal_bloom.png");
    }
    
    @Override
    public void render(@Nonnull TemporalBloomEntity entity, float entityYaw, float partialTicks, 
                      @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight) {
        
        poseStack.pushPose();
        
        // Get animation data
        float rotation = entity.getRotation() + (partialTicks * 0.3f);
        float pulseIntensity = entity.getPulseIntensity();
        
        // Render in proper order to avoid transparency issues:
        // 1. First render opaque/solid elements
        renderTemporalBloom(poseStack, buffer, packedLight, rotation, pulseIntensity, entity);
        
        // 2. Then render translucent elements with proper depth testing
        // Render floating light blue/purple particles around the bloom
        renderFloatingParticles(poseStack, buffer, packedLight, pulseIntensity, partialTicks, entity);
        
        // Render floating crystal particles (translucent)
        renderFloatingCrystals(poseStack, buffer, packedLight, pulseIntensity, partialTicks, entity);
        
        // Render energy ripples (translucent)
        renderEnergyRipples(poseStack, buffer, packedLight, partialTicks, entity);
        
        // Render emissive temporal glow effect (translucent)
        renderGlowEffect(poseStack, buffer, packedLight, rotation, pulseIntensity, entity);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    
    private void renderTemporalBloom(PoseStack poseStack, MultiBufferSource buffer, 
                                   int packedLight, float rotation, float pulseIntensity, TemporalBloomEntity entity) {
        
        if (temporalBloomModel == null) {
            // Fallback: render a simple placeholder if OBJ model failed to load
            renderFallbackModel(poseStack, buffer, packedLight, rotation, pulseIntensity, entity);
            return;
        }
        
        poseStack.pushPose();
        
        // Apply rotation (no camera orientation - keep it in world space)
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
        
        // Apply subtle pulsing scale effect and size adjustment
        float scale = (0.6f + (pulseIntensity * 0.1f)); // Reduced blooming effect
        poseStack.scale(scale, scale, scale);
        
        // Render the model with per-vertex distortion using the new method
        float time = (System.currentTimeMillis() % 10000) / 1000.0f; // 10 second cycle
        temporalBloomModel.renderWithDistortion(poseStack, buffer, getTextureLocation(entity), packedLight, 1.0f, 1.0f, 1.0f, 1.0f, time);
        
        poseStack.popPose();
    }
    
    private void renderFallbackModel(PoseStack poseStack, MultiBufferSource buffer, 
                                   int packedLight, float rotation, float pulseIntensity, TemporalBloomEntity entity) {
        // Simple fallback rendering when OBJ model is not available
        poseStack.pushPose();
        
        // Apply rotation
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
        
        // Apply pulsing scale effect
        float scale = (0.6f + (pulseIntensity * 0.3f));
        poseStack.scale(scale, scale, scale);
        
        // Render a simple textured quad as placeholder
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        Matrix4f matrix4f = poseStack.last().pose();
        
        float size = 1.0f;
        float alpha = 0.8f;
        
        // Simple quad
        consumer.addVertex(matrix4f, -size, -size, 0).setColor((int)(1.0f * 255), (int)(0.8f * 255), (int)(1.0f * 255), (int)(alpha * 255))
               .setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix4f, size, -size, 0).setColor((int)(1.0f * 255), (int)(0.8f * 255), (int)(1.0f * 255), (int)(alpha * 255))
               .setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix4f, size, size, 0).setColor((int)(1.0f * 255), (int)(0.8f * 255), (int)(1.0f * 255), (int)(alpha * 255))
               .setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix4f, -size, size, 0).setColor((int)(1.0f * 255), (int)(0.8f * 255), (int)(1.0f * 255), (int)(alpha * 255))
               .setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
        
        poseStack.popPose();
    }
    
    private void renderFloatingParticles(PoseStack poseStack, MultiBufferSource buffer, int packedLight, 
                                       float pulseIntensity, float partialTicks, TemporalBloomEntity entity) {
        // Render light blue/purple particles floating around the temporal bloom
        poseStack.pushPose();
        
        // Create time-based animation for particle movement
        float time = (System.currentTimeMillis() % 12000) / 1000.0f; // 12 second cycle
        
        // Render multiple floating particles in a sphere around the bloom
        for (int i = 0; i < 8; i++) {
            poseStack.pushPose();
            
            // Calculate particle position in a sphere
            float angle1 = (float) ((i * Math.PI * 2) / 8.0f + time * 0.5f);
            float angle2 = (i * 0.7f) + time * 0.3f;
            float radius = 2.0f + (float) Math.sin(time + i) * 0.5f;
            
            float x = (float) (Math.cos(angle1) * Math.cos(angle2) * radius);
            float y = (float) (Math.sin(angle2) * radius) + (float) Math.sin(time * 2 + i) * 0.3f;
            float z = (float) (Math.sin(angle1) * Math.cos(angle2) * radius);
            
            poseStack.translate(x, y, z);
            
            // Scale particles based on pulse intensity
            float particleScale = 0.3f + (pulseIntensity * 0.2f);
            poseStack.scale(particleScale, particleScale, particleScale);
            
            // Make the particle face the camera (billboard effect)
            net.minecraft.client.Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            float cameraYaw = camera.getYRot();
            float cameraPitch = camera.getXRot();
            
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-cameraYaw));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(cameraPitch));
            
            // Render particle as a translucent quad
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
            Matrix4f matrix4f = poseStack.last().pose();
            
            float size = 0.5f;
            float alpha = 0.6f + (float) Math.sin(time * 3 + i) * 0.3f;
            
            // Light blue/purple color
            float r = 0.6f + (float) Math.sin(time + i) * 0.2f;
            float g = 0.8f + (float) Math.cos(time + i) * 0.1f;
            float b = 1.0f;
            
            // Render a quad facing the camera
            consumer.addVertex(matrix4f, -size, -size, 0).setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255)).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
            consumer.addVertex(matrix4f, size, -size, 0).setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255)).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
            consumer.addVertex(matrix4f, size, size, 0).setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255)).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
            consumer.addVertex(matrix4f, -size, size, 0).setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255)).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
            
            poseStack.popPose();
        }
        
        poseStack.popPose();
    }
    
    private void renderFloatingCrystals(PoseStack poseStack, MultiBufferSource buffer, 
                                      int packedLight, float pulseIntensity, float partialTicks, TemporalBloomEntity entity) {
        
        poseStack.pushPose();
        
        // Render floating crystal particles around the bloom
        float time = (System.currentTimeMillis() + partialTicks * 50) * 0.001f;
        int crystalCount = 8;
        
        for (int i = 0; i < crystalCount; i++) {
            poseStack.pushPose();
            
            // Calculate crystal position in a circle
            float angle = (i * 2 * (float) Math.PI / crystalCount) + time * 0.5f;
            float radius = 2.0f + pulseIntensity * 0.5f;
            float height = (float) Math.sin(time * 2 + i) * 0.5f;
            
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            float y = height;
            
            poseStack.translate(x, y, z);
            
            // Scale crystals based on pulse
            float crystalScale = 0.1f + pulseIntensity * 0.05f;
            poseStack.scale(crystalScale, crystalScale, crystalScale);
            
            // Render small crystal using particles instead of geometry
            // The main OBJ model will handle the crystal geometry
            // This is just for additional floating particles
            
            poseStack.popPose();
        }
        
        poseStack.popPose();
    }
    
    private void renderEnergyRipples(PoseStack poseStack, MultiBufferSource buffer, 
                                   int packedLight, float partialTicks, TemporalBloomEntity entity) {
        
        poseStack.pushPose();
        
        // Render expanding energy ripples
        float time = (System.currentTimeMillis() + partialTicks * 50) * 0.002f;
        int rippleCount = 3;
        
        for (int i = 0; i < rippleCount; i++) {
            poseStack.pushPose();
            
            // Calculate ripple expansion
            float rippleTime = (time + i * 0.5f) % 2.0f; // 2 second cycle
            float rippleScale = rippleTime * 3.0f; // Expand to 3 blocks radius
            float alpha = (1.0f - rippleTime) * 0.4f; // Fade out as it expands
            
            if (alpha > 0) {
                poseStack.scale(rippleScale, 0.1f, rippleScale);
                
                // Energy ripples are handled by particle effects in the entity
                // The OBJ model provides the main visual structure
            }
            
            poseStack.popPose();
        }
        
        poseStack.popPose();
    }
    
    private void renderGlowEffect(PoseStack poseStack, MultiBufferSource buffer, 
                                 int packedLight, float rotation, float pulseIntensity, TemporalBloomEntity entity) {
        
        poseStack.pushPose();
        
        // Apply rotation for the glow effect
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation * 0.5f));
        
        // Create emissive glow effect
        float glowScale = 0.8f + pulseIntensity * 0.4f;
        poseStack.scale(glowScale, glowScale, glowScale);
        
        // The OBJ model should handle the emissive glow through its materials
        // This method can be used for additional glow effects if needed
        
        poseStack.popPose();
    }
    
}
