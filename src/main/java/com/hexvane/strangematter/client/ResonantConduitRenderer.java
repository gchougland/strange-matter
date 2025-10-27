package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.block.ResonantConduitBlock;
import com.hexvane.strangematter.block.ResonantConduitBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Renderer for Resonant Conduits that handles proper texture and UV mapping.
 * Uses the block's getShape() method for collision but renders with proper textures.
 */
public class ResonantConduitRenderer implements BlockEntityRenderer<ResonantConduitBlockEntity> {

    private static final ResourceLocation CONDUIT_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/block/resonant_conduit.png");

    public ResonantConduitRenderer(BlockEntityRendererProvider.Context context) {
        // Constructor
    }

    @Override
    public void render(@Nonnull ResonantConduitBlockEntity blockEntity, float partialTicks, @Nonnull PoseStack poseStack,
                       @Nonnull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        if (blockEntity == null || blockEntity.getLevel() == null) {
            return;
        }

        BlockState blockState = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos());
        if (!(blockState.getBlock() instanceof ResonantConduitBlock conduitBlock)) {
            return;
        }

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutout(CONDUIT_TEXTURE));

        // Determine connection pattern
        List<Direction> connectedDirections = getConnectedDirections(blockEntity);
        boolean shouldRenderJoint = shouldRenderJoint(connectedDirections);

        // Render the joint only if needed
        if (shouldRenderJoint) {
            renderJoint(poseStack, buffer, combinedLight, combinedOverlay);
        }

        // Render connection tubes for each connected direction
        for (Direction direction : connectedDirections) {
            renderConnectionTube(poseStack, buffer, direction, 
                    shouldRenderJoint, combinedLight, combinedOverlay);
        }
    }

    /**
     * Get list of connected directions from the block entity
     */
    private List<Direction> getConnectedDirections(ResonantConduitBlockEntity blockEntity) {
        return blockEntity.getConnectedDirections();
    }

    /**
     * Determine if the joint should be rendered
     * Joint is rendered when:
     * - No connections (single block)
     * - More than 2 connections
     * - 2 connections that are not opposite (corner)
     */
    private boolean shouldRenderJoint(List<Direction> connectedDirections) {
        int connectionCount = connectedDirections.size();

        if (connectionCount == 0 || connectionCount == 1) {
            return true; // Single block or single connection, show joint
        }

        if (connectionCount == 2) {
            // Check if the two connections are opposite (straight line)
            Direction dir1 = connectedDirections.get(0);
            Direction dir2 = connectedDirections.get(1);
            return dir1 != dir2.getOpposite(); // Joint only if not opposite (corner)
        }

        // 3+ connections always need joint
        return true;
    }

    /**
     * Render the central 7x7 joint/node using joint UVs
     */
    private void renderJoint(PoseStack poseStack, VertexConsumer buffer, int combinedLight, int combinedOverlay) {
        poseStack.pushPose();

        // Center the joint in the block space (4x4 pixels centered, matches CORE_SHAPE: 6,6,6 to 10,10,10)
        float jointSize = 4.0f / 16.0f;
        float offset = (1.0f - jointSize) / 2.0f;
        poseStack.translate(offset, offset, offset);
        poseStack.scale(jointSize, jointSize, jointSize);

        // Render joint with proper UVs (joint is at 5,10 with size 7x7)
        renderCubeWithUVs(poseStack, buffer, 5.0f/32.0f, 10.0f/32.0f, 7.0f/32.0f, 7.0f/32.0f, combinedLight, combinedOverlay, null);

        poseStack.popPose();
    }

    /**
     * Render a connection tube extending in the given direction with proper UV mapping
     */
    private void renderConnectionTube(PoseStack poseStack, VertexConsumer buffer, Direction direction, 
                                     boolean hasJoint, int combinedLight, int combinedOverlay) {
        poseStack.pushPose();

        // Calculate the tube dimensions (2x2 pixels to match connection shapes: 7-9 range)
        float tubeWidth = 2.0f / 16.0f;
        float tubeLength = hasJoint ? 0.5f : 1.0f; // Half length when joint is present, full length for straight connections

        // Position tubes to match voxel shapes exactly (start at pixel 7, width 2 pixels)
        float tubeStart = 7.0f / 16.0f; // Start at pixel 7
        switch (direction) {
            case NORTH:
                poseStack.translate(tubeStart, tubeStart, 0.0f);
                poseStack.scale(tubeWidth, tubeWidth, tubeLength);
                break;
            case SOUTH:
                poseStack.translate(tubeStart, tubeStart, 1.0f - tubeLength);
                poseStack.scale(tubeWidth, tubeWidth, tubeLength);
                break;
            case EAST:
                poseStack.translate(1.0f - tubeLength, tubeStart, tubeStart);
                poseStack.scale(tubeLength, tubeWidth, tubeWidth);
                break;
            case WEST:
                poseStack.translate(0.0f, tubeStart, tubeStart);
                poseStack.scale(tubeLength, tubeWidth, tubeWidth);
                break;
            case UP:
                poseStack.translate(tubeStart, 1.0f - tubeLength, tubeStart);
                poseStack.scale(tubeWidth, tubeLength, tubeWidth);
                break;
            case DOWN:
                poseStack.translate(tubeStart, 0.0f, tubeStart);
                poseStack.scale(tubeWidth, tubeLength, tubeWidth);
                break;
        }

        // Render the tube with proper UVs based on direction and length
        float uStart, vStart, uSize, vSize;

        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            // Horizontal tube UVs (0,0) size 16x5
            uStart = 0.0f / 32.0f;
            vStart = 0.0f / 32.0f;
            uSize = 16.0f / 32.0f; // Full UV width - scaling handles the length
            vSize = 5.0f / 32.0f;
        } else if (direction == Direction.UP || direction == Direction.DOWN) {
            // Vertical tube UVs (0,5) size 5x16
            uStart = 0.0f / 32.0f;
            vStart = 5.0f / 32.0f;
            uSize = 5.0f / 32.0f;
            vSize = 16.0f / 32.0f; // Full UV height - scaling handles the length
        } else {
            // East/West use horizontal tube UVs but rotated
            uStart = 0.0f / 32.0f;
            vStart = 0.0f / 32.0f;
            uSize = 16.0f / 32.0f; // Full UV width - scaling handles the length
            vSize = 5.0f / 32.0f;
        }

        renderCubeWithUVs(poseStack, buffer, uStart, vStart, uSize, vSize, combinedLight, combinedOverlay, direction);

        poseStack.popPose();
    }

    /**
     * Render a cube with custom UV coordinates
     */
    private void renderCubeWithUVs(PoseStack poseStack, VertexConsumer buffer,
                                  float uStart, float vStart, float uSize, float vSize,
                                  int combinedLight, int combinedOverlay, Direction direction) {
        // UV coordinates are already in 0-1 range from the calling methods
        float minU = uStart;
        float maxU = uStart + uSize;
        float minV = vStart;
        float maxV = vStart + vSize;
        
        // Top and bottom face UVs for cardinal direction tubes (5,0) size 5x16
        float topBottomMinU = 0.0f / 32.0f;
        float topBottomMaxU = 5.0f / 32.0f;
        float topBottomMinV = 5.0f / 32.0f;
        float topBottomMaxV = 21.0f / 32.0f;

        // Front face
        addVertex(buffer, poseStack, 0, 0, 1, minU, minV, combinedLight, combinedOverlay);
        addVertex(buffer, poseStack, 1, 0, 1, maxU, minV, combinedLight, combinedOverlay);
        addVertex(buffer, poseStack, 1, 1, 1, maxU, maxV, combinedLight, combinedOverlay);
        addVertex(buffer, poseStack, 0, 1, 1, minU, maxV, combinedLight, combinedOverlay);

        // Back face
        addVertex(buffer, poseStack, 1, 0, 0, minU, minV, combinedLight, combinedOverlay);
        addVertex(buffer, poseStack, 0, 0, 0, maxU, minV, combinedLight, combinedOverlay);
        addVertex(buffer, poseStack, 0, 1, 0, maxU, maxV, combinedLight, combinedOverlay);
        addVertex(buffer, poseStack, 1, 1, 0, minU, maxV, combinedLight, combinedOverlay);

        // Left face
        addVertex(buffer, poseStack, 0, 0, 0, minU, minV, combinedLight, combinedOverlay);
        addVertex(buffer, poseStack, 0, 0, 1, maxU, minV, combinedLight, combinedOverlay);
        addVertex(buffer, poseStack, 0, 1, 1, maxU, maxV, combinedLight, combinedOverlay);
        addVertex(buffer, poseStack, 0, 1, 0, minU, maxV, combinedLight, combinedOverlay);

        // Right face
        addVertex(buffer, poseStack, 1, 0, 1, minU, minV, combinedLight, combinedOverlay);
        addVertex(buffer, poseStack, 1, 0, 0, maxU, minV, combinedLight, combinedOverlay);
        addVertex(buffer, poseStack, 1, 1, 0, maxU, maxV, combinedLight, combinedOverlay);
        addVertex(buffer, poseStack, 1, 1, 1, minU, maxV, combinedLight, combinedOverlay);

        // Top face - use special UVs for north/south direction tubes
        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            addVertex(buffer, poseStack, 0, 1, 1, topBottomMinU, topBottomMinV, combinedLight, combinedOverlay);
            addVertex(buffer, poseStack, 1, 1, 1, topBottomMaxU, topBottomMinV, combinedLight, combinedOverlay);
            addVertex(buffer, poseStack, 1, 1, 0, topBottomMaxU, topBottomMaxV, combinedLight, combinedOverlay);
            addVertex(buffer, poseStack, 0, 1, 0, topBottomMinU, topBottomMaxV, combinedLight, combinedOverlay);
        } else {
            addVertex(buffer, poseStack, 0, 1, 1, minU, minV, combinedLight, combinedOverlay);
            addVertex(buffer, poseStack, 1, 1, 1, maxU, minV, combinedLight, combinedOverlay);
            addVertex(buffer, poseStack, 1, 1, 0, maxU, maxV, combinedLight, combinedOverlay);
            addVertex(buffer, poseStack, 0, 1, 0, minU, maxV, combinedLight, combinedOverlay);
        }

        // Bottom face - use special UVs for north/south direction tubes
        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            addVertex(buffer, poseStack, 0, 0, 0, topBottomMinU, topBottomMinV, combinedLight, combinedOverlay);
            addVertex(buffer, poseStack, 1, 0, 0, topBottomMaxU, topBottomMinV, combinedLight, combinedOverlay);
            addVertex(buffer, poseStack, 1, 0, 1, topBottomMaxU, topBottomMaxV, combinedLight, combinedOverlay);
            addVertex(buffer, poseStack, 0, 0, 1, topBottomMinU, topBottomMaxV, combinedLight, combinedOverlay);
        } else {
            addVertex(buffer, poseStack, 0, 0, 0, minU, minV, combinedLight, combinedOverlay);
            addVertex(buffer, poseStack, 1, 0, 0, maxU, minV, combinedLight, combinedOverlay);
            addVertex(buffer, poseStack, 1, 0, 1, maxU, maxV, combinedLight, combinedOverlay);
            addVertex(buffer, poseStack, 0, 0, 1, minU, maxV, combinedLight, combinedOverlay);
        }
    }

    /**
     * Add a vertex to the buffer
     */
    private void addVertex(VertexConsumer buffer, PoseStack poseStack, float x, float y, float z,
                          float u, float v, int combinedLight, int combinedOverlay) {
        buffer.addVertex(poseStack.last().pose(), x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(combinedOverlay)
                .setLight(combinedLight)
                .setNormal(0.0f, 1.0f, 0.0f);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
