package com.hexvane.strangematter.client.renderer;

import com.hexvane.strangematter.entity.ThrowableContainmentCapsuleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ThrowableContainmentCapsuleRenderer extends EntityRenderer<ThrowableContainmentCapsuleEntity> {
    
    private final ItemRenderer itemRenderer;
    
    public ThrowableContainmentCapsuleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }
    
    @Override
    public void render(ThrowableContainmentCapsuleEntity entity, float entityYaw, float partialTicks, 
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        
        poseStack.pushPose();
        
        // Rotate the item to face the direction of movement
        poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        
        // Scale the item slightly
        poseStack.scale(0.5f, 0.5f, 0.5f);
        
        // Get the capsule item to render
        ItemStack capsuleItem = entity.getCapsuleItem();
        if (capsuleItem.isEmpty()) {
            capsuleItem = entity.getDefaultItem().getDefaultInstance();
        }
        
        // Render the item
        this.itemRenderer.renderStatic(capsuleItem, ItemDisplayContext.GROUND, 
            packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), 0);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    
    @Override
    public ResourceLocation getTextureLocation(ThrowableContainmentCapsuleEntity entity) {
        // This renderer uses the item's texture, so we don't need a separate texture
        return null;
    }
}
