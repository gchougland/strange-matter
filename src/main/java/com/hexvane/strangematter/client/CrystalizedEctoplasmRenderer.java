package com.hexvane.strangematter.client;

import com.hexvane.strangematter.block.CrystalizedEctoplasmBlockEntity;
import com.hexvane.strangematter.client.obj.OBJLoader;
import com.hexvane.strangematter.client.obj.OBJLoader.OBJModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.mojang.math.Axis;
import com.hexvane.strangematter.StrangeMatterMod;

public class CrystalizedEctoplasmRenderer implements BlockEntityRenderer<CrystalizedEctoplasmBlockEntity> {
    
    private static final ResourceLocation CRYSTAL_OBJ = new ResourceLocation(StrangeMatterMod.MODID, "models/block/crystalized_ectoplasm.obj");
    private static final ResourceLocation CRYSTAL_TEXTURE = new ResourceLocation(StrangeMatterMod.MODID, "textures/block/crystalized_ectoplasm.png");
    
    private OBJModel crystalModel;
    
    public CrystalizedEctoplasmRenderer(BlockEntityRendererProvider.Context context) {
        // Load the OBJ model
        try {
            this.crystalModel = OBJLoader.loadModel(CRYSTAL_OBJ);
            System.out.println("Crystalized Ectoplasm OBJ model loaded successfully with " + crystalModel.faces.size() + " faces");
        } catch (Exception e) {
            System.err.println("Failed to load crystalized ectoplasm OBJ model: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void render(CrystalizedEctoplasmBlockEntity blockEntity, float partialTicks, 
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        
        if (crystalModel == null) {
            System.err.println("Crystal model is null, cannot render");
            return;
        }
        
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(BlockStateProperties.FACING);
        
        poseStack.pushPose();
        
        // Move the model from bottom corner to center
        // Try moving it to the center of the block space
        poseStack.translate(0.5, 0.5, 0.5);
        
        // Apply rotation based on facing direction
        applyRotation(poseStack, facing);
        
        // Render the crystal model with translucent render type
        // Color #43ba82 with transparency
        float r = 0.263f; // 0x43 / 255
        float g = 0.729f; // 0xba / 255  
        float b = 0.510f; // 0x82 / 255
        float alpha = 0.8f; // 80% opacity for transparency
        
        crystalModel.render(poseStack, buffer, CRYSTAL_TEXTURE, packedLight, r, g, b, alpha);
        
        poseStack.popPose();
    }
    
    private void applyRotation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> {
                // Place on ceiling - crystal tip points down (upside down)
                poseStack.mulPose(Axis.XP.rotationDegrees(180));
            }
            case UP -> {
                // Place on floor - crystal tip points up (default orientation)
                // No rotation needed
            }
            case NORTH -> {
                // Place on north wall - crystal tip points south (away from wall, toward player)
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            }
            case SOUTH -> {
                // Place on south wall - crystal tip points north (away from wall, toward player)
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
            }
            case WEST -> {
                // Place on west wall - crystal tip points east (away from wall, toward player)
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            }
            case EAST -> {
                // Place on east wall - crystal tip points west (away from wall, toward player)
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
            }
        }
    }
}