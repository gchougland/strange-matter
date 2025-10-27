package com.hexvane.strangematter.client;

import com.hexvane.strangematter.block.ResearchMachineBlockEntity;
import com.hexvane.strangematter.client.obj.OBJLoader;
import com.hexvane.strangematter.client.obj.OBJLoader.OBJModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.hexvane.strangematter.StrangeMatterMod;

public class ResearchMachineRenderer implements BlockEntityRenderer<ResearchMachineBlockEntity> {
    
    private static final ResourceLocation MACHINE_OBJ = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "models/block/research_machine.obj");
    private static final ResourceLocation MACHINE_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/block/research_machine.png");
    
    private OBJModel machineModel;
    
    public ResearchMachineRenderer(BlockEntityRendererProvider.Context context) {
        // Load the OBJ model with flipped UVs for proper texture mapping
        try {
            this.machineModel = OBJLoader.loadModel(MACHINE_OBJ, true); // Flip UVs for research machine
            System.out.println("Research Machine OBJ model loaded successfully with " + machineModel.faces.size() + " faces");
        } catch (Exception e) {
            System.err.println("Failed to load Research Machine OBJ model: " + e.getMessage());
            e.printStackTrace();
            this.machineModel = null;
        }
    }
    
    @Override
    public void render(ResearchMachineBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (machineModel == null) {
            return; // Don't render if model failed to load
        }
        
        poseStack.pushPose();
        
        // Center the model on the block (OBJ models are typically centered around origin)
        poseStack.translate(0.5, 0.0, 0.5);
        
        // Rotate the model based on the block's facing direction
        net.minecraft.core.Direction facing = blockEntity.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
        switch (facing) {
            case NORTH:
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
                break;
            case EAST:
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
                break;
            case SOUTH:
                // Default rotation (0 degrees)
                break;
            case WEST:
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(270));
                break;
            default:
                break;
        }
        
        // Scale the model to fit within a block
        poseStack.scale(1.0f, 1.0f, 1.0f);
        
        // Render the OBJ model with default texture as fallback
        machineModel.render(poseStack, bufferSource, MACHINE_TEXTURE, packedLight, 1.0f, 1.0f, 1.0f, 1.0f);
        
        poseStack.popPose();
    }
}
