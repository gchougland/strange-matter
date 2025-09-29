package com.hexvane.strangematter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.hexvane.strangematter.entity.WarpProjectileEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class WarpProjectileRenderer extends EntityRenderer<WarpProjectileEntity> {
    
    private static final ResourceLocation WARP_PROJECTILE_TEXTURE = ResourceLocation.fromNamespaceAndPath("strangematter", "textures/entity/warp_gate_vortex.png");
    
    public WarpProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(WarpProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight) {
        
        poseStack.pushPose();
        
        // Make it face the camera
        poseStack.mulPose(net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        
        // Render a small quad
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        
        float size = 0.5f;
        float alpha = 0.8f;
        
        // Render the quad
        vertexConsumer.vertex(matrix, -size, -size, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(0.0f, 1.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, size, -size, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(1.0f, 1.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, size, size, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(1.0f, 0.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
            
        vertexConsumer.vertex(matrix, -size, size, 0.0f)
            .color(1.0f, 1.0f, 1.0f, alpha)
            .uv(0.0f, 0.0f)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, 0.0f, 0.0f, 1.0f)
            .endVertex();
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
    
    @Override
    public ResourceLocation getTextureLocation(WarpProjectileEntity entity) {
        return WARP_PROJECTILE_TEXTURE;
    }
}
