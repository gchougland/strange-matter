package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.block.ResonantBurnerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ResonantBurnerRenderer implements BlockEntityRenderer<ResonantBurnerBlockEntity> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/block/resonant_burner.png");
    
    public ResonantBurnerRenderer(BlockEntityRendererProvider.Context context) {
    }
    
    public ResourceLocation getTextureLocation(ResonantBurnerBlockEntity blockEntity) {
        return TEXTURE;
    }
    
    @Override
    public void render(ResonantBurnerBlockEntity blockEntity, float partialTicks, 
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        
        // Render flame particles when burning
        if (blockEntity.getBurnTime() > 0 && blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) {
            renderFlameEffects(blockEntity, poseStack, buffer, packedLight, partialTicks);
        }
    }
    
    private void renderFlameEffects(ResonantBurnerBlockEntity blockEntity, PoseStack poseStack, 
                                   MultiBufferSource buffer, int packedLight, float partialTicks) {
        
        // Use flame render type for fire effects
        VertexConsumer consumer = buffer.getBuffer(RenderType.cutout());
        
        poseStack.pushPose();
        
        // Position at the center of the block, slightly above the surface
        poseStack.translate(0.5, 0.1, 0.5);
        
        // Scale based on burn progress
        float burnProgress = blockEntity.getBurnProgress();
        float scale = 0.3f + (burnProgress * 0.4f); // Scale from 0.3 to 0.7 based on burn progress
        poseStack.scale(scale, scale, scale);
        
        // Animate the flame
        float time = (System.currentTimeMillis() % 1000) / 1000.0f; // 1 second cycle
        float animation = (float) Math.sin(time * Math.PI * 2) * 0.1f;
        poseStack.translate(0, animation, 0);
        
        // Billboard the flame to face the camera
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
        
        Matrix4f matrix = poseStack.last().pose();
        
        // Render flame quad
        float r = 1.0f;
        float g = 0.5f + (burnProgress * 0.3f); // More orange when burning well
        float b = 0.1f;
        float a = 0.8f;
        
        // Render flame quad (billboarded)
        consumer.vertex(matrix, -0.5f, -0.5f, 0.0f)
            .color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, 0.5f, -0.5f, 0.0f)
            .color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, 0.5f, 0.5f, 0.0f)
            .color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, -0.5f, 0.5f, 0.0f)
            .color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(0, 0, 1).endVertex();
        
        poseStack.popPose();
    }
}
