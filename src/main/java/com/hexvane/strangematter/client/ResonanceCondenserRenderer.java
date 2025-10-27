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
import org.joml.Matrix3f;
import com.mojang.blaze3d.systems.RenderSystem;

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
        
        // Render the glass tube part as translucent
        renderGlassTube(blockEntity, poseStack, buffer, packedLight, packedOverlay);
        
        // Render the energy arcs
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) {
            renderEnergyArcs(blockEntity, poseStack, buffer, packedLight, partialTicks);
            renderEnergyAbsorptionParticles(blockEntity, poseStack, buffer, packedLight, partialTicks);
        }
    }
    
    private void renderGlassTube(ResonanceCondenserBlockEntity blockEntity, PoseStack poseStack, 
                                MultiBufferSource buffer, int packedLight, int packedOverlay) {
        
        poseStack.pushPose();
        
        // Use the correct render type with the texture
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        
        // Glass tube dimensions: from [5, 19, 5] to [11, 31, 11]
        // Convert from block coordinates (0-16) to world coordinates (0-1)
        double minX = 5.0 / 16.0;  // 0.3125
        double minY = 19.0 / 16.0; // 1.1875
        double minZ = 5.0 / 16.0;  // 0.3125
        double maxX = 11.0 / 16.0; // 0.6875
        double maxY = 31.0 / 16.0; // 1.9375
        double maxZ = 11.0 / 16.0; // 0.6875
        
        // Use white color with transparency to show texture properly
        float r = 1.0f;
        float g = 1.0f;
        float b = 1.0f;
        float a = 0.6f; // 60% opacity for glass effect
        
        Matrix4f matrix = poseStack.last().pose();
        
        // Render North face (facing negative Z)
        consumer.addVertex(matrix, (float)minX, (float)minY, (float)minZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(7.0f/16.0f, 8.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(0.0f, 0.0f, -1.0f);
        consumer.addVertex(matrix, (float)maxX, (float)minY, (float)minZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(8.5f/16.0f, 8.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(0.0f, 0.0f, -1.0f);
        consumer.addVertex(matrix, (float)maxX, (float)maxY, (float)minZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(8.5f/16.0f, 11.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(0.0f, 0.0f, -1.0f);
        consumer.addVertex(matrix, (float)minX, (float)maxY, (float)minZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(7.0f/16.0f, 11.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(0.0f, 0.0f, -1.0f);
        
        // Render East face (facing positive X)
        consumer.addVertex(matrix, (float)maxX, (float)minY, (float)minZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(8.5f/16.0f, 7.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix, (float)maxX, (float)minY, (float)maxZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(10.0f/16.0f, 7.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix, (float)maxX, (float)maxY, (float)maxZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(10.0f/16.0f, 10.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix, (float)maxX, (float)maxY, (float)minZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(8.5f/16.0f, 10.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(1.0f, 0.0f, 0.0f);
        
        // Render South face (facing positive Z)
        consumer.addVertex(matrix, (float)maxX, (float)minY, (float)maxZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(10.0f/16.0f, 7.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix, (float)minX, (float)minY, (float)maxZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(11.5f/16.0f, 7.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix, (float)minX, (float)maxY, (float)maxZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(11.5f/16.0f, 10.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix, (float)maxX, (float)maxY, (float)maxZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(10.0f/16.0f, 10.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
        
        // Render West face (facing negative X)
        consumer.addVertex(matrix, (float)minX, (float)minY, (float)maxZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(8.5f/16.0f, 10.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(-1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix, (float)minX, (float)minY, (float)minZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(10.0f/16.0f, 10.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(-1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix, (float)minX, (float)maxY, (float)minZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(10.0f/16.0f, 13.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(-1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix, (float)minX, (float)maxY, (float)maxZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(8.5f/16.0f, 13.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(-1.0f, 0.0f, 0.0f);
        
        // Render Up face (facing positive Y)
        consumer.addVertex(matrix, (float)minX, (float)maxY, (float)minZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(11.5f/16.0f, 11.5f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix, (float)maxX, (float)maxY, (float)minZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(10.0f/16.0f, 11.5f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix, (float)maxX, (float)maxY, (float)maxZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(10.0f/16.0f, 10.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix, (float)minX, (float)maxY, (float)maxZ)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(11.5f/16.0f, 10.0f/16.0f).setOverlay(packedOverlay).setLight(packedLight)
            .setNormal(0.0f, 1.0f, 0.0f);

        poseStack.popPose();
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
                    renderSineWaveArc(poseStack, buffer, packedLight, 
                        condenserX, condenserY, condenserZ,
                        anomalyX, anomalyY, anomalyZ,
                        partialTicks);
                }
            }
        }
    }
    
    private void renderSineWaveArc(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
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
        
        // Create sine wave arc with billboarded particles
        int particleCount = 12; // Number of particles along the arc
        double amplitude = 0.8; // Much more subtle wave amplitude
        double frequency = 1.5; // Wave frequency
        
        // Use time for animation
        float time = (System.currentTimeMillis() % 2000) / 2000.0f; // 2 second cycle
        
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
        
        for (int i = 0; i < particleCount; i++) {
            float t = i / (float) particleCount;
            
            // Calculate position along the direction
            double x = startX + dx * (t * totalDistance);
            double y = startY + dy * (t * totalDistance);
            double z = startZ + dz * (t * totalDistance);
            
            // Add sine wave offset, but reduce it near the start and end points
            // Use a curve that's 0 at t=0 and t=1, and peaks in the middle
            float waveIntensity = 4.0f * t * (1.0f - t); // Creates a bell curve from 0 to 1 and back to 0
            double sineOffset = Math.sin(t * frequency * Math.PI + time * 2 * Math.PI) * amplitude * waveIntensity;
            x += perpX * sineOffset;
            y += perpY * sineOffset;
            z += perpZ * sineOffset;
            
            // Render billboarded particle
            int color = 0x00FFFF; // Bright cyan color
            float alpha = 0.6f * (1.0f - t * 0.2f); // More transparent, fade towards the end
            float size = 0.2f * (1.0f - t * 0.3f); // Size decreases towards the end
            
            renderBillboardedParticle(poseStack, buffer, packedLight, x, y, z, color, alpha, size, time, i);
        }
    }
    
    private void renderBillboardedParticle(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                          double x, double y, double z, int color, float alpha, float size, 
                                          float time, int index) {
        
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        
        // Billboard to face the camera
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        
        // Add some rotation and pulsing
        float rotation = time * 360.0f + index * 30.0f;
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotation));
        
        // Pulsing size
        float pulseSize = size * (0.8f + 0.2f * (float)Math.sin(time * Math.PI * 4 + index));
        poseStack.scale(pulseSize, pulseSize, pulseSize);
        
        // Render colored particle
        renderColoredParticle(poseStack, buffer, packedLight, color, alpha);
        
        poseStack.popPose();
    }
    
    private void renderArcSegment(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                                 double x1, double y1, double z1, double x2, double y2, double z2,
                                 int color, float alpha, float thickness) {
        
        // Extract color components
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = alpha;
        
        // Apply minimal fading - make arcs more visible
        double distanceFromAnomaly = Math.sqrt(x2*x2 + y2*y2 + z2*z2);
        double maxDistance = 10.0; // Maximum distance from condenser to anomaly
        double fadeFactor = Math.max(0.7, 1.0 - (distanceFromAnomaly / maxDistance) * 0.3); // Less fading
        
        // Apply fade and shrink
        float finalAlpha = a * (float)fadeFactor;
        float finalThickness = thickness * (float)fadeFactor;
        
        // Calculate direction vector for the segment
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        if (length < 0.001) return; // Too short
        
        // Normalize direction
        dx /= length;
        dy /= length;
        dz /= length;
        
        // Calculate perpendicular vector (cross product with up vector)
        double perpX = -dz;
        double perpY = 0.0;
        double perpZ = dx;
        
        // Normalize perpendicular
        double perpLength = Math.sqrt(perpX*perpX + perpY*perpY + perpZ*perpZ);
        if (perpLength > 0) {
            perpX /= perpLength;
            perpY /= perpLength;
            perpZ /= perpLength;
        }
        
        // Scale perpendicular by thickness
        perpX *= finalThickness;
        perpY *= finalThickness;
        perpZ *= finalThickness;
        
        Matrix4f matrix = poseStack.last().pose();
        
        // Render a thick line segment as a quad
        // Calculate the four corners of the quad
        double x1a = x1 - perpX;
        double y1a = y1 - perpY;
        double z1a = z1 - perpZ;
        
        double x1b = x1 + perpX;
        double y1b = y1 + perpY;
        double z1b = z1 + perpZ;
        
        double x2a = x2 - perpX;
        double y2a = y2 - perpY;
        double z2a = z2 - perpZ;
        
        double x2b = x2 + perpX;
        double y2b = y2 + perpY;
        double z2b = z2 + perpZ;
        
        // Render the quad (two triangles) - simple approach
        // First triangle
        consumer.addVertex(matrix, (float)x1a, (float)y1a, (float)z1a)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(finalAlpha * 255))
            .setUv(0.0f, 0.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 1.0f, 0.0f);
            
        consumer.addVertex(matrix, (float)x1b, (float)y1b, (float)z1b)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(finalAlpha * 255))
            .setUv(1.0f, 0.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 1.0f, 0.0f);
            
        consumer.addVertex(matrix, (float)x2a, (float)y2a, (float)z2a)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(finalAlpha * 255))
            .setUv(0.0f, 1.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 1.0f, 0.0f);
        
        // Second triangle
        consumer.addVertex(matrix, (float)x1b, (float)y1b, (float)z1b)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(finalAlpha * 255))
            .setUv(1.0f, 0.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 1.0f, 0.0f);
            
        consumer.addVertex(matrix, (float)x2b, (float)y2b, (float)z2b)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(finalAlpha * 255))
            .setUv(1.0f, 1.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 1.0f, 0.0f);
            
        consumer.addVertex(matrix, (float)x2a, (float)y2a, (float)z2a)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(finalAlpha * 255))
            .setUv(0.0f, 1.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 1.0f, 0.0f);
    }
    
    private void renderEnergyAbsorptionParticles(ResonanceCondenserBlockEntity blockEntity, PoseStack poseStack, 
                                                MultiBufferSource buffer, int packedLight, float partialTicks) {
        
        // Only render if condenser is active and has energy
        if (!blockEntity.isActive() || !blockEntity.hasEnergy()) {
            return;
        }
        
        var pos = blockEntity.getBlockPos();
        double condenserX = 0.5; // Relative to block center
        double condenserY = 1.5; // Top of condenser
        double condenserZ = 0.5; // Relative to block center
        
        // Find nearby anomalies and render energy absorption particles
        AABB searchBox = new AABB(pos).inflate(16.0); // 16 block radius
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
                
                if (distance <= 16.0) {
                    // Render energy absorption particles around the anomaly
                    renderEnergyAbsorptionParticleField(poseStack, buffer, packedLight, partialTicks,
                        anomalyX, anomalyY, anomalyZ,
                        condenserX, condenserY, condenserZ);
                }
            }
        }
    }
    
    private void renderEnergyAbsorptionParticleField(PoseStack poseStack, MultiBufferSource buffer, int packedLight, 
                                                    float partialTicks, double anomalyX, double anomalyY, double anomalyZ,
                                                    double condenserX, double condenserY, double condenserZ) {
        
        // Use time for animation
        float time = (System.currentTimeMillis() % 3000) / 3000.0f; // 3 second cycle
        
        // Spawn particles in a sphere around the anomaly
        for (int i = 0; i < 8; i++) {
            // Random position in sphere around anomaly
            double angle = (i * Math.PI * 2 / 8) + time * Math.PI * 2; // Rotate around anomaly
            double height = Math.sin(i * 0.5) * 1.5; // Vary height
            double radius = 2.5 + Math.sin(time * Math.PI * 2 + i) * 0.5; // Pulsing radius
            
            double x = anomalyX + Math.cos(angle) * radius;
            double y = anomalyY + height;
            double z = anomalyZ + Math.sin(angle) * radius;
            
            // Calculate direction towards condenser
            double dx = condenserX - x;
            double dy = condenserY - y;
            double dz = condenserZ - z;
            double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
            
            if (distance > 0.1) {
                // Normalize direction
                dx /= distance;
                dy /= distance;
                dz /= distance;
                
                // Add some movement towards condenser
                double moveSpeed = 0.02;
                x += dx * moveSpeed * time;
                y += dy * moveSpeed * time;
                z += dz * moveSpeed * time;
                
                // Render the particle
                renderEnergyAbsorptionParticle(poseStack, buffer, packedLight, x, y, z, time, i);
            }
        }
    }
    
    private void renderEnergyAbsorptionParticle(PoseStack poseStack, MultiBufferSource buffer, int packedLight, 
                                               double x, double y, double z, float time, int index) {
        
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        
        // Billboard to face the camera
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        
        // Vary particle size and rotation
        float particleSize = 0.15f + (index % 3) * 0.05f;
        float rotation = time * 360.0f + index * 45.0f;
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotation));
        poseStack.scale(particleSize, particleSize, particleSize);
        
        // Calculate alpha based on time and index for pulsing effect
        float alpha = 0.8f * (0.5f + 0.5f * (float)Math.sin(time * Math.PI * 4 + index));
        
        // Render colored particle (bright cyan/blue energy color)
        renderColoredParticle(poseStack, buffer, packedLight, 0x00FFFF, alpha);
        
        poseStack.popPose();
    }
    
    private void renderColoredParticle(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int color, float alpha) {
        // Extract RGB components from hex color
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentCull(
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/white_concrete.png")));
        
        // Render double-sided quad particle
        float halfSize = 0.5f;
        
        // Front face
        vertexConsumer.addVertex(matrix, -halfSize, -halfSize, 0.0f)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255))
            .setUv(0.0f, 1.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
            
        vertexConsumer.addVertex(matrix, halfSize, -halfSize, 0.0f)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255))
            .setUv(1.0f, 1.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
            
        vertexConsumer.addVertex(matrix, halfSize, halfSize, 0.0f)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255))
            .setUv(1.0f, 0.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
            
        vertexConsumer.addVertex(matrix, -halfSize, halfSize, 0.0f)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255))
            .setUv(0.0f, 0.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, 1.0f);
        
        // Back face (flipped winding order)
        vertexConsumer.addVertex(matrix, -halfSize, halfSize, 0.0f)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255))
            .setUv(0.0f, 0.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, -1.0f);
            
        vertexConsumer.addVertex(matrix, halfSize, halfSize, 0.0f)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255))
            .setUv(1.0f, 0.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, -1.0f);
            
        vertexConsumer.addVertex(matrix, halfSize, -halfSize, 0.0f)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255))
            .setUv(1.0f, 1.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, -1.0f);
            
        vertexConsumer.addVertex(matrix, -halfSize, -halfSize, 0.0f)
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(alpha * 255))
            .setUv(0.0f, 1.0f)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0f, 0.0f, -1.0f);
    }
}
