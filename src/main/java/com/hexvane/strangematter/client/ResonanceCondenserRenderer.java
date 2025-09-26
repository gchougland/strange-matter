package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.block.ResonanceCondenserBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public class ResonanceCondenserRenderer implements BlockEntityRenderer<ResonanceCondenserBlockEntity> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/block/resonance_condenser.png");
    
    public ResonanceCondenserRenderer(BlockEntityRendererProvider.Context context) {
    }
    
    public ResourceLocation getTextureLocation(ResonanceCondenserBlockEntity blockEntity) {
        return TEXTURE;
    }
    
    @Override
    public void render(ResonanceCondenserBlockEntity blockEntity, float partialTicks, 
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        
        // Don't render the block model - Minecraft handles that automatically
        // Only render the energy arcs
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) {
            renderEnergyArcs(blockEntity, poseStack, buffer, packedLight, partialTicks);
        }
    }
    
    
    private void renderEnergyArcs(ResonanceCondenserBlockEntity blockEntity, PoseStack poseStack, 
                                 MultiBufferSource buffer, int packedLight, float partialTicks) {
        
        // Use lightning render type for energy arcs
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        
        // Get block position (relative to the block entity)
        var pos = blockEntity.getBlockPos();
        double condenserX = 0.5; // Relative to block center
        double condenserY = 1.5; // Top of condenser
        double condenserZ = 0.5; // Relative to block center
        
        // Find nearby anomalies and render sine wave arcs to them
        AABB searchBox = new AABB(pos).inflate(10.0); // 10 block radius
        List<Entity> entitiesInRange = blockEntity.getLevel().getEntities(null, searchBox);
        
        for (Entity anomaly : entitiesInRange) {
            if (anomaly instanceof com.hexvane.strangematter.entity.BaseAnomalyEntity) {
                // Calculate relative positions
                double anomalyX = anomaly.getX() - pos.getX();
                double anomalyY = anomaly.getY() + anomaly.getBbHeight() / 2.0 - pos.getY() - 1.0;
                double anomalyZ = anomaly.getZ() - pos.getZ();
                
                double distance = Math.sqrt(
                    Math.pow(anomalyX - condenserX, 2) + 
                    Math.pow(anomalyY - condenserY, 2) + 
                    Math.pow(anomalyZ - condenserZ, 2)
                );
                
                if (distance <= 10.0) {
                    // Render sine wave energy arc from condenser to anomaly
                    renderSineWaveArc(poseStack, consumer, packedLight, 
                        condenserX, condenserY, condenserZ,
                        anomalyX, anomalyY, anomalyZ,
                        partialTicks);
                }
            }
        }
    }
    
    private void renderSineWaveArc(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                                   double startX, double startY, double startZ,
                                   double endX, double endY, double endZ,
                                   float partialTicks) {
        
        // Calculate direction vector
        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double totalDistance = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        if (totalDistance < 0.1) return; // Too close
        
        // Normalize direction
        dx /= totalDistance;
        dy /= totalDistance;
        dz /= totalDistance;
        
        // Calculate perpendicular vector for sine wave
        double perpX, perpY, perpZ;
        if (Math.abs(dy) > 0.9) {
            // If mostly vertical, use forward vector
            perpX = 1.0;
            perpY = 0.0;
            perpZ = 0.0;
        } else {
            // Cross product with up vector (0,1,0)
            perpX = -dz;
            perpY = 0.0;
            perpZ = dx;
        }
        
        // Normalize perpendicular vector
        double perpLength = Math.sqrt(perpX*perpX + perpY*perpY + perpZ*perpZ);
        if (perpLength > 0) {
            perpX /= perpLength;
            perpY /= perpLength;
            perpZ /= perpLength;
        }
        
        // Create sine wave arc
        int segments = 16; // More segments for smoother sine wave
        double amplitude = 1.0; // Increased wave amplitude for visibility
        double frequency = 2.0; // Wave frequency
        
        // Use time for animation
        float time = (System.currentTimeMillis() % 2000) / 2000.0f; // 2 second cycle
        
        // Starting position
        double x = startX;
        double y = startY;
        double z = startZ;
        
        for (int i = 0; i < segments; i++) {
            float t = i / (float) segments;
            float nextT = (i + 1) / (float) segments;
            
            // Calculate next position along the direction
            double nextX = startX + dx * (nextT * totalDistance);
            double nextY = startY + dy * (nextT * totalDistance);
            double nextZ = startZ + dz * (nextT * totalDistance);
            
            // Add sine wave offset
            if (i > 0) {
                double sineOffset = Math.sin(t * frequency * Math.PI + time * 2 * Math.PI) * amplitude * (1.0 - t * 0.5);
                
                nextX += perpX * sineOffset;
                nextY += perpY * sineOffset;
                nextZ += perpZ * sineOffset;
            }
            
            // Render segment
            if (i > 0) {
                int color = 0x00FFFF; // Bright cyan color
                float alpha = 1.0f * (1.0f - t * 0.2f); // Less fade for better visibility
                float thickness = 0.15f * (1.0f - t * 0.3f); // Thicker arcs for visibility
                
                renderArcSegment(poseStack, consumer, packedLight, x, y, z, nextX, nextY, nextZ, color, alpha, thickness);
            }
            
            // Move to next position
            x = nextX;
            y = nextY;
            z = nextZ;
        }
    }
    
    private void renderArcSegment(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                                 double x1, double y1, double z1, double x2, double y2, double z2,
                                 int color, float alpha, float thickness) {
        
        // Extract color components
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = alpha;
        
        // Apply fading and shrinking based on distance from anomaly
        // Calculate distance from anomaly (assuming anomaly is at the end of the arc)
        double distanceFromAnomaly = Math.sqrt(x2*x2 + y2*y2 + z2*z2);
        double maxDistance = 10.0; // Maximum distance from condenser to anomaly
        double fadeFactor = Math.max(0.1, 1.0 - (distanceFromAnomaly / maxDistance)); // Fade as we get closer to anomaly
        
        // Apply fade and shrink
        float finalAlpha = a * (float)fadeFactor;
        float finalThickness = thickness * (float)fadeFactor;
        
        // Calculate segment center
        double centerX = (x1 + x2) / 2.0;
        double centerY = (y1 + y2) / 2.0;
        double centerZ = (z1 + z2) / 2.0;
        
        // Push pose stack and rotate towards camera
        poseStack.pushPose();
        poseStack.translate(centerX, centerY, centerZ);
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
        
        Matrix4f matrix = poseStack.last().pose();
        
        // Calculate billboard vertices (simple quad centered at origin)
        double halfThickness = finalThickness / 2.0;
        
        // Render billboarded quad (always facing camera)
        consumer.vertex(matrix, (float)(-halfThickness), (float)(-halfThickness), 0.0f)
            .color(r, g, b, finalAlpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, (float)(halfThickness), (float)(-halfThickness), 0.0f)
            .color(r, g, b, finalAlpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, (float)(halfThickness), (float)(halfThickness), 0.0f)
            .color(r, g, b, finalAlpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, (float)(-halfThickness), (float)(halfThickness), 0.0f)
            .color(r, g, b, finalAlpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(0, 0, 1).endVertex();
        
        // Pop pose stack
        poseStack.popPose();
    }
}
