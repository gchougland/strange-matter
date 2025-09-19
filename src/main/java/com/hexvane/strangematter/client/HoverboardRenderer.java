package com.hexvane.strangematter.client;

import com.hexvane.strangematter.entity.HoverboardEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class HoverboardRenderer extends EntityRenderer<HoverboardEntity> {
    
    public HoverboardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f; // Stone slab shadow size
    }
    
    @Override
    public ResourceLocation getTextureLocation(HoverboardEntity entity) {
        // We'll use the stone slab's texture, but this method is required
        // The actual texture is handled by the block renderer
        return new ResourceLocation("minecraft", "textures/block/stone.png");
    }
    
    @Override
    public void render(HoverboardEntity entity, float entityYaw, float partialTicks, 
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        
        poseStack.pushPose();
        
        // Apply entity rotation first
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-entityYaw));
        
        // Then center the block model on the entity's position
        // If it's going to the left, try adjusting the X translation
        poseStack.translate(-0.5, 0.0, -0.5);
        
        // Get the stone slab block state
        BlockState stoneSlabState = Blocks.STONE_SLAB.defaultBlockState();
        
        // Get the block render dispatcher
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        
        // Render the stone slab model
        blockRenderer.renderSingleBlock(
            stoneSlabState, 
            poseStack, 
            buffer, 
            packedLight, 
            OverlayTexture.NO_OVERLAY
        );
        
        poseStack.popPose();
    }
}
