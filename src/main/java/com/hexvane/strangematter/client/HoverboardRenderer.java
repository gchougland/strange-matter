package com.hexvane.strangematter.client;

import com.hexvane.strangematter.entity.HoverboardEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class HoverboardRenderer extends EntityRenderer<HoverboardEntity> {
    
    private final ModelPart hoverboardModel;
    private static final ResourceLocation HOVERBOARD_TEXTURE = ResourceLocation.fromNamespaceAndPath("strangematter", "textures/entity/hoverboard.png");
    
    public HoverboardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.hoverboardModel = createHoverboardModel(context.bakeLayer(ModelLayers.PLAYER));
    }
    
    private static ModelPart createHoverboardModel(ModelPart root) {
        // Create a simple hoverboard model
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        
        // Main board - original coordinates
        partDefinition.addOrReplaceChild("board", 
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-5.5F, -1.0F, -8.0F, 11.0F, 2.0F, 16.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Front thruster
        partDefinition.addOrReplaceChild("front_thruster",
            CubeListBuilder.create()
                .texOffs(0, 35)
                .addBox(-5.5F, -2.0F, -13.0F, 11.0F, 5.0F, 5.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Back thruster
        partDefinition.addOrReplaceChild("back_thruster",
            CubeListBuilder.create()
                .texOffs(0, 35)
                .addBox(-5.5F, -2.0F, 8.0F, 11.0F, 5.0F, 5.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Top layer
        partDefinition.addOrReplaceChild("top_layer",
            CubeListBuilder.create()
                .texOffs(0, 18)
                .addBox(-4.5F, 1.0F, -8.0F, 9.0F, 1.0F, 16.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        return LayerDefinition.create(meshDefinition, 64, 64).bakeRoot();
    }
    
    @Override
    public ResourceLocation getTextureLocation(HoverboardEntity entity) {
        return HOVERBOARD_TEXTURE;
    }
    
    @Override
    public void render(HoverboardEntity entity, float entityYaw, float partialTicks, 
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        
        poseStack.pushPose();
        
        // Apply entity rotation
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-entityYaw));
        
        // Fix upside-down model by rotating 180 degrees around X-axis
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0F));
        
        // Get the texture
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        
        // Render the hoverboard model
        this.hoverboardModel.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        
        poseStack.popPose();
    }
}
