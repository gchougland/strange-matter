package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.client.obj.OBJLoader;
import com.hexvane.strangematter.client.obj.OBJLoader.OBJModel;
import com.hexvane.strangematter.entity.ThoughtwellEntity;
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

public class ThoughtwellRenderer extends EntityRenderer<ThoughtwellEntity> {
    
    private static final ResourceLocation THOUGHTWELL_OBJ = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "models/entity/thoughtwell.obj");
    private static final ResourceLocation AURA_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/entity/thoughtwell_aura.png");
    
    private OBJModel thoughtwellModel;
    
    public ThoughtwellRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        
        // Load the OBJ model
        try {
            this.thoughtwellModel = OBJLoader.loadModel(THOUGHTWELL_OBJ);
            System.out.println("Thoughtwell OBJ model loaded successfully with " + thoughtwellModel.faces.size() + " faces");
        } catch (Exception e) {
            System.err.println("Failed to load Thoughtwell OBJ model: " + e.getMessage());
            System.err.println("Make sure thoughtwell.obj and thoughtwell.mtl are in assets/strangematter/models/entity/");
            e.printStackTrace();
        }
    }
    
    @Override
    @Nonnull
    public ResourceLocation getTextureLocation(@Nonnull ThoughtwellEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/entity/thoughtwell.png");
    }
    
    @Override
    public void render(@Nonnull ThoughtwellEntity entity, float entityYaw, float partialTicks, 
                      @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight) {
        
        poseStack.pushPose();
        
        // Get animation data
        float rotation = entity.getRotation() + (partialTicks * 0.2f);
        float pulseIntensity = entity.getPulseIntensity();
        
        // Render in proper order to avoid transparency issues:
        // 1. First render opaque/solid elements
        renderThoughtwell(poseStack, buffer, packedLight, rotation, pulseIntensity, entity);
        
        // 2. Then render translucent elements with proper depth testing
        // Render cyan particle effects (translucent)
        renderCyanParticles(poseStack, buffer, packedLight, pulseIntensity, partialTicks, entity);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    
    private void renderThoughtwell(PoseStack poseStack, MultiBufferSource buffer, 
                                  int packedLight, float rotation, float pulseIntensity, ThoughtwellEntity entity) {
        
        if (thoughtwellModel == null) {
            // Fallback: render a simple placeholder if OBJ model failed to load
            renderFallbackModel(poseStack, buffer, packedLight, rotation, pulseIntensity, entity);
            return;
        }
        
        poseStack.pushPose();
        
        // Apply rotation (no camera orientation - keep it in world space)
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
        
        // Apply subtle pulsing scale effect and size adjustment - scaled up
        float scale = (2.2f + (pulseIntensity * 0.2f)); // Larger scale with gentle pulsing
        poseStack.scale(scale, scale, scale);
        
        // Render the model normally without distortions
        thoughtwellModel.render(poseStack, buffer, getTextureLocation(entity), packedLight, 1.0f, 1.0f, 1.0f, 1.0f);
        
        poseStack.popPose();
    }
    
    private void renderFallbackModel(PoseStack poseStack, MultiBufferSource buffer, 
                                   int packedLight, float rotation, float pulseIntensity, ThoughtwellEntity entity) {
        // Simple fallback rendering when OBJ model is not available
        poseStack.pushPose();
        
        // Apply rotation
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
        
        // Apply pulsing scale effect
        float scale = (0.7f + (pulseIntensity * 0.3f));
        poseStack.scale(scale, scale, scale);
        
        // Render a simple textured quad as placeholder
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        Matrix4f matrix4f = poseStack.last().pose();
        
        float size = 1.0f;
        float alpha = 0.8f;
        
        // Simple quad with cyan tint
        consumer.addVertex(matrix4f, -size, -size, 0).setColor((int)(0.6f * 255), (int)(1.0f * 255), (int)(1.0f * 255), (int)(alpha * 255))
               .setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix4f, size, -size, 0).setColor((int)(0.6f * 255), (int)(1.0f * 255), (int)(1.0f * 255), (int)(alpha * 255))
               .setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix4f, size, size, 0).setColor((int)(0.6f * 255), (int)(1.0f * 255), (int)(1.0f * 255), (int)(alpha * 255))
               .setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix4f, -size, size, 0).setColor((int)(0.6f * 255), (int)(1.0f * 255), (int)(1.0f * 255), (int)(alpha * 255))
               .setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
        
        poseStack.popPose();
    }
    
    
    private void renderCyanParticles(PoseStack poseStack, MultiBufferSource buffer, 
                                   int packedLight, float pulseIntensity, float partialTicks, ThoughtwellEntity entity) {
        
        // Render cyan particle effects around the thoughtwell in a ring
        float time = (System.currentTimeMillis() % 360000) / 1000.0f; // 6 minute cycle like gravity anomaly
        int particleCount = 8; // Start with fewer particles for debugging
        
        for (int i = 0; i < particleCount; i++) {
            poseStack.pushPose();
            
            // Calculate particle position in a slowly rotating ring
            float baseAngle = (i * 360.0f / particleCount); // Base angle for each particle
            float rotationOffset = time * 10.0f; // Slow rotation (10 degrees per second)
            float angle = (baseAngle + rotationOffset) % 360.0f; // Combine and normalize
            float radius = 1.5f; // Fixed radius for debugging
            float height = (float) Math.sin(time * 1.0f + i * 0.3f) * 0.4f; // Gentler height variation
            
            float x = (float) Math.cos(Math.toRadians(angle)) * radius;
            float z = (float) Math.sin(Math.toRadians(angle)) * radius;
            float y = height;
            
            poseStack.translate(x, y, z);
            
            // Fixed scale for debugging
            float particleScale = 1.0f;
            poseStack.scale(particleScale, particleScale, particleScale);
            
            // Face the camera by applying the camera rotation
            poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
            
            // Render cyan particle
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
            Matrix4f matrix4f = poseStack.last().pose();
            
            float size = 0.3f;
            float alpha = 1.0f; // Full alpha for debugging
            
            // Bright cyan color
            float r = 0.0f;
            float g = 1.0f;
            float b = 1.0f;
            
            // Render a quad facing the camera
            consumer.addVertex(matrix4f, -size, -size, 0).setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255)).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
            consumer.addVertex(matrix4f, size, -size, 0).setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255)).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
            consumer.addVertex(matrix4f, size, size, 0).setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255)).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
            consumer.addVertex(matrix4f, -size, size, 0).setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255)).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0f, 0.0f, 1.0f);
            
            poseStack.popPose();
        }
    }
    
    
}
