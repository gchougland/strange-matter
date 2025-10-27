package com.hexvane.strangematter.client;

import com.hexvane.strangematter.block.RiftStabilizerBlockEntity;
import com.hexvane.strangematter.entity.EnergeticRiftEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;

/**
 * Renderer for the Rift Stabilizer block entity.
 * Renders an electricity beam from the connected Energetic Rift anomaly to the north face of the block.
 */
public class RiftStabilizerRenderer implements BlockEntityRenderer<RiftStabilizerBlockEntity> {
    
    public RiftStabilizerRenderer(BlockEntityRendererProvider.Context context) {
    }
    
    @Override
    public void render(RiftStabilizerBlockEntity blockEntity, float partialTicks, PoseStack poseStack, 
                      MultiBufferSource buffer, int packedLight, int packedOverlay) {
        
        // Only render if generating power
        if (!blockEntity.isGenerating()) {
            return;
        }
        
        // Find the connected rift on the client side (since entity references aren't synced)
        EnergeticRiftEntity rift = com.hexvane.strangematter.util.AnomalyUtil
            .findNearestEnergeticRift(
                blockEntity.getLevel(), 
                blockEntity.getBlockPos(), 
                com.hexvane.strangematter.Config.riftStabilizerRadius
            )
            .orElse(null);
        
        if (rift == null) {
            return;
        }
        
        // Get positions
        net.minecraft.world.phys.Vec3 riftPos = rift.position().add(0, 0.5, 0); // Center of rift
        net.minecraft.world.phys.Vec3 stabilizerPos = blockEntity.getBeamTargetPos(); // North face of stabilizer
        
        // Calculate relative positions (from stabilizer to rift)
        double startX = 0;
        double startY = 0;
        double startZ = 0;
        
        double endX = riftPos.x - stabilizerPos.x;
        double endY = riftPos.y - stabilizerPos.y;
        double endZ = riftPos.z - stabilizerPos.z;
        
        // Render the electricity beam
        poseStack.pushPose();
        
        // Translate to the beam start position (north face of block)
        poseStack.translate(
            stabilizerPos.x - blockEntity.getBlockPos().getX(),
            stabilizerPos.y - blockEntity.getBlockPos().getY(),
            stabilizerPos.z - blockEntity.getBlockPos().getZ()
        );
        
        // Use lightning render type for the beam
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        
        renderElectricityBeam(poseStack, consumer, packedLight, 
            startX, startY, startZ, 
            endX, endY, endZ);
        
        poseStack.popPose();
    }
    
    /**
     * Render an electricity beam from start to end position with zigzag effect.
     * This uses the same technique as the Energetic Rift renderer.
     */
    private void renderElectricityBeam(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                                      double startX, double startY, double startZ,
                                      double endX, double endY, double endZ) {
        
        // Create beam that goes from start to end with zigzag
        int segments = 16; // More segments for longer beams
        
        // Current position
        double x = startX;
        double y = startY;
        double z = startZ;
        
        // Calculate direction vector
        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double totalDistance = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        if (totalDistance < 0.1) return; // Too short to render
        
        dx /= totalDistance; 
        dy /= totalDistance; 
        dz /= totalDistance;
        
        // Use time for zigzag animation
        float time = (System.currentTimeMillis() % 2000) / 2000.0f; // 2 second cycle
        
        for (int i = 0; i < segments; i++) {
            float t = i / (float) segments;
            float nextT = (i + 1) / (float) segments;
            
            // Calculate next position along the direction
            double nextX = startX + dx * (nextT * totalDistance);
            double nextY = startY + dy * (nextT * totalDistance);
            double nextZ = startZ + dz * (nextT * totalDistance);
            
            // Add zigzag effect
            if (i > 0) {
                float zigzagIntensity = 0.15f; // Reduced intensity for a cleaner beam
                
                // Create perpendicular vectors for zigzag
                double perpX = -dz;
                double perpY = 1.0;
                double perpZ = dx;
                
                // Normalize perpendicular vector
                double perpLength = Math.sqrt(perpX*perpX + perpY*perpY + perpZ*perpZ);
                if (perpLength > 0) {
                    perpX /= perpLength; 
                    perpY /= perpLength; 
                    perpZ /= perpLength;
                }
                
                // Add zigzag using deterministic function
                float zigzag1 = (float) Math.sin(time * 30 + i * 2.5) * zigzagIntensity;
                float zigzag2 = (float) Math.sin(time * 35 + i * 3.0) * zigzagIntensity * 0.8f;
                
                nextX += perpX * zigzag1;
                nextY += perpY * zigzag2;
                nextZ += perpZ * zigzag1;
            }
            
            // Render segment
            if (i > 0) {
                int color = 0x00FFFF; // Cyan color (same as Energetic Rift)
                float alpha = 0.8f;
                float thickness = 0.08f; // Slightly thicker for visibility
                
                renderSparkQuad3D(poseStack, consumer, packedLight, 
                    x, y, z, nextX, nextY, nextZ, 
                    color, alpha, thickness);
            }
            
            // Move to next position
            x = nextX; 
            y = nextY; 
            z = nextZ;
        }
    }
    
    /**
     * Render a 3D quad for the spark beam.
     * This is the same method used in EnergeticRiftRenderer.
     */
    private void renderSparkQuad3D(PoseStack poseStack, VertexConsumer consumer, int packedLight, 
                                  double x1, double y1, double z1, double x2, double y2, double z2, 
                                  int color, float alpha, float thickness) {
        
        Matrix4f matrix = poseStack.last().pose();
        
        // Extract color components
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = alpha;
        
        // Calculate direction vector for perpendicular offset
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        if (length < 0.001) return; // Avoid division by zero
        
        // Normalize direction
        dx /= length;
        dy /= length;
        dz /= length;
        
        // Calculate perpendicular vectors for thickness
        double perpX1, perpY1, perpZ1;
        double perpX2, perpY2, perpZ2;
        
        // Create perpendicular vector (simple cross product with up vector)
        if (Math.abs(dy) > 0.9) {
            // If mostly vertical, use forward vector
            perpX1 = thickness;
            perpY1 = 0;
            perpZ1 = 0;
        } else {
            // Cross product with up vector (0,1,0)
            perpX1 = -dz * thickness;
            perpY1 = 0;
            perpZ1 = dx * thickness;
        }
        
        perpX2 = -perpX1;
        perpY2 = -perpY1;
        perpZ2 = -perpZ1;
        
        // Render as a proper quad using 4 vertices
        // Front face quad
        consumer.addVertex(matrix, (float)(x1 + perpX1), (float)(y1 + perpY1), (float)(z1 + perpZ1))
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
            .setNormal((float)dx, (float)dy, (float)dz);
        consumer.addVertex(matrix, (float)(x2 + perpX1), (float)(y2 + perpY1), (float)(z2 + perpZ1))
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
            .setNormal((float)dx, (float)dy, (float)dz);
        consumer.addVertex(matrix, (float)(x2 + perpX2), (float)(y2 + perpY2), (float)(z2 + perpZ2))
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
            .setNormal((float)dx, (float)dy, (float)dz);
        consumer.addVertex(matrix, (float)(x1 + perpX2), (float)(y1 + perpY2), (float)(z1 + perpZ2))
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
            .setNormal((float)dx, (float)dy, (float)dz);
        
        // Back face quad (reversed winding)
        consumer.addVertex(matrix, (float)(x1 + perpX2), (float)(y1 + perpY2), (float)(z1 + perpZ2))
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
            .setNormal((float)-dx, (float)-dy, (float)-dz);
        consumer.addVertex(matrix, (float)(x2 + perpX2), (float)(y2 + perpY2), (float)(z2 + perpZ2))
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
            .setNormal((float)-dx, (float)-dy, (float)-dz);
        consumer.addVertex(matrix, (float)(x2 + perpX1), (float)(y2 + perpY1), (float)(z2 + perpZ1))
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
            .setNormal((float)-dx, (float)-dy, (float)-dz);
        consumer.addVertex(matrix, (float)(x1 + perpX1), (float)(y1 + perpY1), (float)(z1 + perpZ1))
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
            .setNormal((float)-dx, (float)-dy, (float)-dz);
    }
}

