package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.client.obj.OBJLoader;
import com.hexvane.strangematter.client.obj.OBJLoader.OBJModel;
import com.hexvane.strangematter.entity.EchoingShadowEntity;
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
import org.joml.Matrix3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EchoingShadowRenderer extends EntityRenderer<EchoingShadowEntity> {
    
    private static final ResourceLocation ICOSAHEDRON_OBJ = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "models/entity/icosahedron.obj");
    private static final ResourceLocation SHADOW_BILLBOARD_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/entity/shadow_billboard.png");
    private static final ResourceLocation ICOSAHEDRON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/entity/echoing_shadow_icosahedron.png");
    private static final ResourceLocation ECHO_RINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/entity/echo_rings.png");
    
    private OBJModel icosahedronModel;
    
    public EchoingShadowRenderer(EntityRendererProvider.Context context) {
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
    public ResourceLocation getTextureLocation(@org.jetbrains.annotations.NotNull EchoingShadowEntity entity) {
        return ICOSAHEDRON_TEXTURE;
    }
    
    @Override
    public void render(@org.jetbrains.annotations.NotNull EchoingShadowEntity entity, float entityYaw, float partialTicks, 
                      @org.jetbrains.annotations.NotNull PoseStack poseStack, @org.jetbrains.annotations.NotNull MultiBufferSource buffer, int packedLight) {
        
        poseStack.pushPose();
        
        // Get animation data
        float rotation = entity.getRotation() + (partialTicks * 0.5f);
        float pulseIntensity = entity.getPulseIntensity();
        
        // Render in proper order to avoid transparency issues:
        // 1. First render the shadow billboard (translucent)
        renderShadowBillboard(poseStack, buffer, packedLight, rotation, pulseIntensity);
        
        // 2. Then render the animated icosahedron (translucent, pitch black)
        renderAnimatedIcosahedron(poseStack, buffer, packedLight, rotation, pulseIntensity, partialTicks);
        
        // 3. Render rotating purple ring billboards
        renderRotatingRings(poseStack, buffer, packedLight, pulseIntensity, partialTicks);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    
    private void renderShadowBillboard(PoseStack poseStack, MultiBufferSource buffer, 
                                     int packedLight, float rotation, float pulseIntensity) {
        
        poseStack.pushPose();
        
        // Make the billboard face the camera properly using the same method as EnergeticRiftRenderer
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180)); // Rotate 180 degrees to face player
        
        // Apply fixed scale (no pulsation)
        float scale = 1.5f;
        poseStack.scale(scale, scale, scale);
        
        // Render the shadow billboard as a translucent quad (no culling to see through it)
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(SHADOW_BILLBOARD_TEXTURE));
        
        // Render a large translucent quad for the shadow billboard
        float halfSize = 3.0f; // Made bigger
        float alpha = 1.0f; // Low alpha to allow entities to show through
        
        // Front face of the billboard
        vertexConsumer.addVertex(matrix4f, -halfSize, -halfSize, 0.0f)
            .setColor(0, 0, 0, (int)(alpha * 255))
            .setUv(0.0f, 1.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
            
        vertexConsumer.addVertex(matrix4f, halfSize, -halfSize, 0.0f)
            .setColor(0, 0, 0, (int)(alpha * 255))
            .setUv(1.0f, 1.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
            
        vertexConsumer.addVertex(matrix4f, halfSize, halfSize, 0.0f)
            .setColor(0, 0, 0, (int)(alpha * 255))
            .setUv(1.0f, 0.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
            
        vertexConsumer.addVertex(matrix4f, -halfSize, halfSize, 0.0f)
            .setColor(0, 0, 0, (int)(alpha * 255))
            .setUv(0.0f, 0.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
        
        poseStack.popPose();
    }
    
    private void renderAnimatedIcosahedron(PoseStack poseStack, MultiBufferSource buffer, 
                                         int packedLight, float rotation, float pulseIntensity, float partialTicks) {
        
        if (icosahedronModel == null) {
            System.err.println("Icosahedron model is null, cannot render");
            return;
        }
        
        poseStack.pushPose();
        
        // Apply rotation (no camera orientation - keep it in world space)
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
        
        // Apply pulsing scale effect and size adjustment
        float baseScale = 1.2f + (pulseIntensity * 0.2f); // Made larger
        poseStack.scale(baseScale, baseScale, baseScale);
        
        // Render the model with per-vertex distortion using the new method
        float time = (System.currentTimeMillis() % 10000) / 1000.0f; // 10 second cycle
        icosahedronModel.renderWithDistortion(poseStack, buffer, ICOSAHEDRON_TEXTURE, packedLight, 0.0f, 0.0f, 0.0f, 1.0f, time);
        
        poseStack.popPose();
    }
    
    
    private void renderRotatingRings(PoseStack poseStack, MultiBufferSource buffer, 
                                   int packedLight, float pulseIntensity, float partialTicks) {
        
        // Create time-based rotation for the rings
        float time = (System.currentTimeMillis() % 12000) / 1000.0f; // 12 second cycle
        
        // Render the larger ring (rotates clockwise) - offset forward slightly
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 0.1); // Move forward 0.1 units
        renderRingBillboard(poseStack, buffer, packedLight, pulseIntensity, time, 1.0f, 1.0f);
        poseStack.popPose();
        
        // Render the smaller ring (rotates counter-clockwise) - offset forward more
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 0.2); // Move forward 0.2 units (more than the larger ring)
        renderRingBillboard(poseStack, buffer, packedLight, pulseIntensity, -time * 1.3f, 0.7f, 0.7f);
        poseStack.popPose();
    }
    
    private void renderRingBillboard(PoseStack poseStack, MultiBufferSource buffer, 
                                   int packedLight, float pulseIntensity, float rotation, 
                                   float size, float alpha) {
        
        poseStack.pushPose();
        
        // Face the camera by applying the camera rotation (same as shadow billboard)
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180)); // Rotate 180 degrees to face player
        
        // Apply rotation around the Z-axis (perpendicular to the billboard)
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotation * 30.0f)); // 30 degrees per second
        
        // Apply pulsing scale effect
        float scale = size * (0.8f + (pulseIntensity * 0.4f));
        poseStack.scale(scale, scale, scale);
        
        
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentCull(ECHO_RINGS_TEXTURE));
        
        // Render the purple ring billboard
        float halfSize = 1.2f; // Made smaller
        float finalAlpha = alpha * (0.8f + (pulseIntensity * 0.2f)); // Increased base alpha
        
        // Front face of the ring billboard
        vertexConsumer.addVertex(matrix4f, -halfSize, -halfSize, 0.0f)
            .setColor((int)(0.8f * 255), (int)(0.2f * 255), 255, (int)(finalAlpha * 255)) // Purple color
            .setUv(0.0f, 1.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
            
        vertexConsumer.addVertex(matrix4f, halfSize, -halfSize, 0.0f)
            .setColor((int)(0.8f * 255), (int)(0.2f * 255), 255, (int)(finalAlpha * 255))
            .setUv(1.0f, 1.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
            
        vertexConsumer.addVertex(matrix4f, halfSize, halfSize, 0.0f)
            .setColor((int)(0.8f * 255), (int)(0.2f * 255), 255, (int)(finalAlpha * 255))
            .setUv(1.0f, 0.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
            
        vertexConsumer.addVertex(matrix4f, -halfSize, halfSize, 0.0f)
            .setColor((int)(0.8f * 255), (int)(0.2f * 255), 255, (int)(finalAlpha * 255))
            .setUv(0.0f, 0.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
        
        poseStack.popPose();
    }
}
