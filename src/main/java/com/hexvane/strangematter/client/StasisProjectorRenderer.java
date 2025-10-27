package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.block.StasisProjectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class StasisProjectorRenderer implements BlockEntityRenderer<StasisProjectorBlockEntity> {
    
    private static final ResourceLocation FIELD_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/block/stasis_field.png");
    
    public StasisProjectorRenderer(BlockEntityRendererProvider.Context context) {
        // No special initialization needed
    }
    
    @Override
    public void render(StasisProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        if (!blockEntity.isPowered()) {
            return;
        }
        
        // Render the stasis field
        poseStack.pushPose();
        // Position so bottom of quad aligns with top of projector
        // Projector is 2 pixels (0.125 blocks) tall, quad is 2.0 blocks tall (size 1.0 means -1 to +1)
        // Center quad at 0.125 + 1.0 = 1.125 so bottom is at 0.125
        poseStack.translate(0.5, 1.125, 0.5);
        renderStasisField(poseStack, bufferSource, packedLight, partialTick, blockEntity);
        poseStack.popPose();
        
        // Items and entities are rendered by their own renderers, we just control their position in the BlockEntity
    }
    
    private void renderStasisField(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick, StasisProjectorBlockEntity blockEntity) {
        // Field size
        float size = 1.0f;
        
        // Animated pulsing transparency
        float time = (System.currentTimeMillis() % 3000) / 3000.0f;
        float pulse = (float) (0.5f + 0.5f * Math.sin(time * Math.PI * 2));
        float alpha = 0.5f + 0.3f * pulse; // Ranges from 0.5 to 0.8
        
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(FIELD_TEXTURE));
        
        // Billboard the quad to face the camera (Y-axis only)
        poseStack.pushPose();
        
        // Get camera position and calculate yaw angle to face it
        net.minecraft.client.Camera camera = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera();
        net.minecraft.world.phys.Vec3 cameraPos = camera.getPosition();
        net.minecraft.world.phys.Vec3 blockPos = blockEntity.getBlockPos().getCenter();
        
        double dx = cameraPos.x - blockPos.x;
        double dz = cameraPos.z - blockPos.z;
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        renderQuad(consumer, matrix, normal, size, packedLight, alpha, false, blockEntity); // Front face
        renderQuad(consumer, matrix, normal, size, packedLight, alpha, true, blockEntity);  // Back face
        poseStack.popPose();
    }
    
    private void renderQuad(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float size, int packedLight, float alpha, boolean reversed, StasisProjectorBlockEntity blockEntity) {
        // Render a vertical quad centered at origin
        // Extract RGB components from the color field
        int color = blockEntity.getFieldColor();
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        
        if (!reversed) {
            // Front face (normal winding order)
            // Bottom-left
            consumer.vertex(matrix, -size, -size, 0.0f)
                .color(red, green, blue, alpha)
                .uv(0.0f, 1.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0f, 0.0f, 1.0f)
                .endVertex();
            
            // Bottom-right
            consumer.vertex(matrix, size, -size, 0.0f)
                .color(red, green, blue, alpha)
                .uv(1.0f, 1.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0f, 0.0f, 1.0f)
                .endVertex();
            
            // Top-right
            consumer.vertex(matrix, size, size, 0.0f)
                .color(red, green, blue, alpha)
                .uv(1.0f, 0.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0f, 0.0f, 1.0f)
                .endVertex();
            
            // Top-left
            consumer.vertex(matrix, -size, size, 0.0f)
                .color(red, green, blue, alpha)
                .uv(0.0f, 0.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0f, 0.0f, 1.0f)
                .endVertex();
        } else {
            // Back face (reversed winding order)
            // Top-left
            consumer.vertex(matrix, -size, size, 0.0f)
                .color(red, green, blue, alpha)
                .uv(0.0f, 0.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0f, 0.0f, -1.0f)
                .endVertex();
            
            // Top-right
            consumer.vertex(matrix, size, size, 0.0f)
                .color(red, green, blue, alpha)
                .uv(1.0f, 0.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0f, 0.0f, -1.0f)
                .endVertex();
            
            // Bottom-right
            consumer.vertex(matrix, size, -size, 0.0f)
                .color(red, green, blue, alpha)
                .uv(1.0f, 1.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0f, 0.0f, -1.0f)
                .endVertex();
            
            // Bottom-left
            consumer.vertex(matrix, -size, -size, 0.0f)
                .color(red, green, blue, alpha)
                .uv(0.0f, 1.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0f, 0.0f, -1.0f)
                .endVertex();
        }
    }
}

