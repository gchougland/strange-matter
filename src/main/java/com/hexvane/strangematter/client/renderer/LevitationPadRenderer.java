package com.hexvane.strangematter.client.renderer;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.Config;
import com.hexvane.strangematter.block.LevitationPadBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LevitationPadRenderer implements BlockEntityRenderer<LevitationPadBlockEntity> {
    
    private static final ResourceLocation LEVITATION_FIELD_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/block/levitation_field.png");
    
    public LevitationPadRenderer(BlockEntityRendererProvider.Context context) {
        // Constructor for block entity renderer
    }
    
    @Override
    public int getViewDistance() {
        // Increase the view distance for this renderer to ensure beams are visible
        return 256; // Much larger than default BlockEntityRenderer distance
    }
    
    @Override
    public boolean shouldRender(LevitationPadBlockEntity blockEntity, Vec3 cameraPos) {
        // Always render - let the render method handle the actual beam visibility
        // This prevents premature culling based on the small block hitbox
        return true;
    }
    
    @Override
    public boolean shouldRenderOffScreen(LevitationPadBlockEntity blockEntity) {
        // Always render even when off-screen to ensure beam visibility
        return true;
    }
    
    @Override
    public AABB getRenderBoundingBox(LevitationPadBlockEntity blockEntity) {
        // Extend the render bounding box to include the full beam height
        // This prevents frustum culling from hiding the beam when the block is out of view
        int maxHeight = blockEntity.getMaxHeight();
        return new AABB(
            blockEntity.getBlockPos().getX() - 0.5, blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ() - 0.5,
            blockEntity.getBlockPos().getX() + 1.5, blockEntity.getBlockPos().getY() + maxHeight, blockEntity.getBlockPos().getZ() + 1.5
        );
    }
    
    @Override
    public void render(LevitationPadBlockEntity blockEntity, float partialTicks, PoseStack poseStack, 
                      MultiBufferSource buffer, int packedLight, int packedOverlay) {
        
        if (blockEntity == null || blockEntity.getLevel() == null) return;
        
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        BlockState state = level.getBlockState(pos);
        
        // Only render if the block is a levitation pad
        if (!(state.getBlock() instanceof com.hexvane.strangematter.block.LevitationPadBlock)) return;
        
        // Render the custom levitation beam effect
        boolean levitateUp = state.getValue(com.hexvane.strangematter.block.LevitationPadBlock.LEVITATE_UP);
        int maxHeight = calculateMaxHeight(level, pos);
        
        if (maxHeight > 0) {
            renderLevitationBeam(poseStack, buffer, maxHeight, levitateUp, partialTicks, packedLight);
        }
    }
    
    private int calculateMaxHeight(Level level, BlockPos pos) {
        BlockPos currentPos = pos.above();
        int height = 0;
        int maxRange = Config.levitationPadMaxHeight; // Use configurable value
        
        // Check up to max range blocks or until we hit a blocking block
        while (height < maxRange) {
            BlockState blockState = level.getBlockState(currentPos);
            
            // Check if this position has air or void - always allow through
            if (blockState.getBlock() == net.minecraft.world.level.block.Blocks.AIR ||
                blockState.getBlock() == net.minecraft.world.level.block.Blocks.CAVE_AIR ||
                blockState.getBlock() == net.minecraft.world.level.block.Blocks.VOID_AIR ||
                blockState.isAir()) {
                height++;
                currentPos = currentPos.above();
                continue;
            }
            
            // Check if it's a trapdoor - allow through if open, block if closed
            if (blockState.getBlock() instanceof net.minecraft.world.level.block.TrapDoorBlock) {
                if (blockState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN)) {
                    // Open trapdoor - allow beam to pass through
                    height++;
                    currentPos = currentPos.above();
                    continue;
                } else {
                    // Closed trapdoor - block the beam
                    break;
                }
            }
            
            // Any other block - block the beam
            break;
        }
        
        return ++height;
    }
    
    private void renderLevitationBeam(PoseStack poseStack, MultiBufferSource buffer, 
                                    int height, boolean goingUp, 
                                    float partialTicks, int packedLight) {
        
        poseStack.pushPose();
        
        // Get the render type for the texture (same method as ResonantConduitRenderer)
        RenderType renderType = RenderType.entityTranslucentEmissive(LEVITATION_FIELD_TEXTURE);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        
        // The beam is 12x12 pixels centered in a 16x16 texture, so we use 12x16 UV coordinates
        float textureHeight = 1.0f; // Full height of texture
        
        // Animation time for UV scrolling
        long gameTime = net.minecraft.client.Minecraft.getInstance().level.getGameTime();
        float animationTime = (gameTime + partialTicks) * 0.01f; // Slow animation
        
        // Animation offset for UV scrolling
        float uvOffset = (animationTime % 1.0f);
        if (goingUp) {
            uvOffset = -uvOffset; // Reverse direction for up mode
        }
        
        // Calculate UV coordinates for the 12x16 portion of the texture
        float uMin = 0.0f;
        float uMax = 12.0f / 16.0f; // End at pixel 12
        float vMin = 0.0f;
        float vMax = 1.0f;
        
        // Beam dimensions (12x12 blocks centered on the block)
        float beamSize = 0.75f; // 12/16 of a block
        float beamOffset = (1.0f - beamSize) / 2.0f; // Center the beam
        
        // Render the beam as a single tall quad
        float y1 = 0.0f;
        float y2 = height;
        
        // Calculate UV coordinates for the entire beam
        float beamVMin = (vMin*height) + uvOffset;
        float beamVMax = (vMax*height) + uvOffset;
        
        // Render the four faces of the beam as one tall quad
        renderBeamSegment(poseStack, vertexConsumer, 
                        beamOffset, 1.0f - beamOffset, // x bounds
                        y1, y2, // y bounds (full height)
                        beamOffset, 1.0f - beamOffset, // z bounds
                        uMin, uMax, beamVMin, beamVMax, // UV coordinates
                        packedLight, goingUp);
        
        poseStack.popPose();
    }
    
    private void renderBeamSegment(PoseStack poseStack, VertexConsumer vertexConsumer,
                                 float x1, float x2, float y1, float y2, float z1, float z2,
                                 float u1, float u2, float v1, float v2, int packedLight, boolean goingUp) {
        
        // Flip V coordinates because Minecraft UV (0,0) is bottom-left, but textures are designed with (0,0) top-left
        float v1Flipped = goingUp ? v1 : 1.0f - v1;
        float v2Flipped = goingUp ? v2 : 1.0f - v2;
        
        // North face (z = z1)
        addVertex(vertexConsumer, poseStack, x1, y1, z1, u1, v1Flipped, packedLight, 0);
        addVertex(vertexConsumer, poseStack, x1, y2, z1, u1, v2Flipped, packedLight, 0);
        addVertex(vertexConsumer, poseStack, x2, y2, z1, u2, v2Flipped, packedLight, 0);
        addVertex(vertexConsumer, poseStack, x2, y1, z1, u2, v1Flipped, packedLight, 0);
        
        // South face (z = z2)
        addVertex(vertexConsumer, poseStack, x2, y1, z2, u1, v1Flipped, packedLight, 0);
        addVertex(vertexConsumer, poseStack, x2, y2, z2, u1, v2Flipped, packedLight, 0);
        addVertex(vertexConsumer, poseStack, x1, y2, z2, u2, v2Flipped, packedLight, 0);
        addVertex(vertexConsumer, poseStack, x1, y1, z2, u2, v1Flipped, packedLight, 0);
        
        // West face (x = x1)
        addVertex(vertexConsumer, poseStack, x1, y1, z2, u1, v1Flipped, packedLight, 0);
        addVertex(vertexConsumer, poseStack, x1, y2, z2, u1, v2Flipped, packedLight, 0);
        addVertex(vertexConsumer, poseStack, x1, y2, z1, u2, v2Flipped, packedLight, 0);
        addVertex(vertexConsumer, poseStack, x1, y1, z1, u2, v1Flipped, packedLight, 0);
        
        // East face (x = x2)
        addVertex(vertexConsumer, poseStack, x2, y1, z1, u1, v1Flipped, packedLight, 0);
        addVertex(vertexConsumer, poseStack, x2, y2, z1, u1, v2Flipped, packedLight, 0);
        addVertex(vertexConsumer, poseStack, x2, y2, z2, u2, v2Flipped, packedLight, 0);
        addVertex(vertexConsumer, poseStack, x2, y1, z2, u2, v1Flipped, packedLight, 0);
    }
    
    /**
     * Add a vertex to the buffer (same method as ResonantConduitRenderer)
     */
    private void addVertex(VertexConsumer buffer, PoseStack poseStack, float x, float y, float z,
                          float u, float v, int combinedLight, int combinedOverlay) {
        buffer.addVertex(poseStack.last().pose(), x, y, z)
                .setColor(255, 255, 255, 128)
                .setUv(u, v)
                .setOverlay(combinedOverlay)
                .setLight(combinedLight)
                .setNormal(0.0f, 1.0f, 0.0f);
    }
}
