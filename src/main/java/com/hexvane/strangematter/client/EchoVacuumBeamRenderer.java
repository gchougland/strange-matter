package com.hexvane.strangematter.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Matrix3f;

public class EchoVacuumBeamRenderer {
    
    private static final ResourceLocation BEAM_TEXTURE = ResourceLocation.fromNamespaceAndPath("strangematter", "textures/particle/beam.png");
    
    public void renderBeam(PoseStack poseStack, MultiBufferSource buffer, int packedLight, 
                          Vec3 start, Vec3 end, Vec3 direction, boolean isFirstPerson, boolean hitTarget, Vec3 localBeamEnd) {
        
        // Render vacuum cone particles at the gun tip (local origin) - after rotation
        renderVacuumConeParticles(poseStack, buffer, packedLight, Vec3.ZERO, direction, isFirstPerson);
        
        // Render extraction particles at beam end only when hitting a target
        if (hitTarget) {
            renderAnomalyExtractionParticles(poseStack, buffer, packedLight, Vec3.ZERO, localBeamEnd, direction);
        }

        // Add fixed 45-degree rotation for better visibility (no spinning)
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0f));
        
        // Render the chaotic beam from local origin to end
        renderChaoticBeam(poseStack, buffer, packedLight, Vec3.ZERO, end, direction);
    }
    
    private void renderChaoticBeam(PoseStack poseStack, MultiBufferSource buffer, int packedLight, 
                                 Vec3 start, Vec3 end, Vec3 direction) {
        
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(BEAM_TEXTURE));
        
        // Create a chaotic beam by rendering multiple segments with zigzag
        int segments = 20; // More segments for smoother beam
        float maxThickness = 1.0f; // Maximum thickness at 1 block away
        float minThickness = 0.025f; // Minimum thickness at gun tip (much thinner)
        
        // Calculate direction and length
        double totalLength = end.subtract(start).length();
        
        // Render the beam as a series of connected segments with smooth zigzag
        double[] segmentX = new double[segments];
        double[] segmentY = new double[segments];
        double[] segmentZ = new double[segments];
        float[] segmentThickness = new float[segments];
        
        // Calculate all segment positions and thicknesses first
        for (int i = 0; i < segments; i++) {
            float t = i / (float) (segments - 1);
            
            // Base position along the beam
            double baseX = start.x + direction.x * (t * totalLength);
            double baseY = start.y + direction.y * (t * totalLength);
            double baseZ = start.z + direction.z * (t * totalLength);
            
            // Calculate thickness based on distance from gun tip
            // Max thickness at 1 block away, min at gun tip (distance = 0.0)
            double distanceFromTip = t * totalLength;
            float thicknessFactor = Math.max(0.0f, Math.min(1.0f, (float)(distanceFromTip / 1.0)));
            segmentThickness[i] = minThickness + (maxThickness - minThickness) * thicknessFactor;
            
            // Add chaotic zigzag effect based on distance from gun tip
            // No wobble at gun tip (distance = 0), max wobble at ~2 blocks away
            float wobbleDistance = 2.0f; // Distance at which wobble reaches maximum
            float wobbleFactor = Math.min(1.0f, (float)(distanceFromTip / wobbleDistance));
            float zigzagIntensity = 0.3f * wobbleFactor; // Wobble intensity based on distance
            
            // Create perpendicular vectors for zigzag
            double perpX = -direction.z;
            double perpY = 1.0;
            double perpZ = direction.x;
            
            // Normalize perpendicular vector
            double perpLength = Math.sqrt(perpX*perpX + perpY*perpY + perpZ*perpZ);
            if (perpLength > 0) {
                perpX /= perpLength;
                perpY /= perpLength;
                perpZ /= perpLength;
            }
            
            // Add zigzag using deterministic function
            float time = (System.currentTimeMillis() % 2000) / 1000.0f; // 2 second cycle
            float zigzag1 = (float) Math.sin(time * 5.0f + i * 0.5f) * zigzagIntensity;
            float zigzag2 = (float) Math.sin(time * 6.5f + i * 0.7f) * zigzagIntensity * 0.8f;
            
            segmentX[i] = baseX + perpX * zigzag1;
            segmentY[i] = baseY + perpY * zigzag2;
            segmentZ[i] = baseZ + perpZ * zigzag1;
        }
        
        // Render connected segments with individual thicknesses
        for (int i = 0; i < segments - 1; i++) {
            int color = 0x00FFFF; // Cyan
            float alpha = 0.8f;
            
            // Use average thickness of current and next segment
            float avgThickness = (segmentThickness[i] + segmentThickness[i + 1]) / 2.0f;
            
            renderBeamSegment(poseStack, consumer, packedLight, 
                segmentX[i], segmentY[i], segmentZ[i],
                segmentX[i + 1], segmentY[i + 1], segmentZ[i + 1],
                color, alpha, avgThickness);
        }
    }
    
    private void renderBeamSegment(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                                 double x1, double y1, double z1, double x2, double y2, double z2, 
                                 int color, float alpha, float thickness) {
        
        // Extract RGB components
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = alpha;
        
        // Calculate direction and perpendicular vectors
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        if (length < 0.001) return;
        
        dx /= length; dy /= length; dz /= length;
        
        // Calculate perpendicular vectors for quad
        double perpX1, perpY1, perpZ1;
        if (Math.abs(dy) < 0.9) {
            perpX1 = 0;
            perpY1 = thickness;
            perpZ1 = 0;
        } else {
            perpX1 = -dz * thickness;
            perpY1 = 0;
            perpZ1 = dx * thickness;
        }
        
        double perpX2 = -perpX1;
        double perpY2 = -perpY1;
        double perpZ2 = -perpZ1;
        
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        
        // Render as a proper quad with rotated UVs (90 degrees clockwise)
        consumer.addVertex(matrix, (float)(x1 + perpX1), (float)(y1 + perpY1), (float)(z1 + perpZ1)).setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal((float)dx, (float)dy, (float)dz);
        consumer.addVertex(matrix, (float)(x2 + perpX1), (float)(y2 + perpY1), (float)(z2 + perpZ1)).setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal((float)dx, (float)dy, (float)dz);
        consumer.addVertex(matrix, (float)(x2 + perpX2), (float)(y2 + perpY2), (float)(z2 + perpZ2)).setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal((float)dx, (float)dy, (float)dz);
        consumer.addVertex(matrix, (float)(x1 + perpX2), (float)(y1 + perpY2), (float)(z1 + perpZ2)).setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255)).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal((float)dx, (float)dy, (float)dz);
    }
    
    private void renderVacuumConeParticles(PoseStack poseStack, MultiBufferSource buffer, int packedLight, 
                                         Vec3 vacuumPos, Vec3 direction, boolean isFirstPerson) {
        
        // Use white wool texture like WarpGateAnomalyRenderer
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentCull(
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/white_concrete.png")));
        
        int cyanColor = 0x00FFFF;
        float alpha = 0.8f;
        
        // Adjust particle size and behavior based on view perspective
        float spiralRadius = isFirstPerson ? 2.0f : 2.5f; // Increased radius to spawn further out
        
        // Render 12 particles in a cone pattern being sucked into the vacuum
        for (int i = 0; i < 12; i++) {
            // Use proper time calculation like other renderers
            float time = (System.currentTimeMillis() % 3000) / 1000.0f; // 3 second cycle
            float baseAngle = i * (360f / 12f); // Base angle for each particle
            
            // Each particle has its own lifecycle
            float particleLife = (time + i * 0.25f) % 1.0f; // Offset each particle's lifecycle
            
            // Particles spawn on outer ring and move toward center
            float outerRadius = spiralRadius;
            float innerRadius = 0.1f;
            float currentRadius = outerRadius - (particleLife * (outerRadius - innerRadius));
            
            // Calculate position in a cone that points in the same direction as the beam
            // Create a cone that opens outward from the gun tip in the beam direction
            double coneDistance = 8.0 - (particleLife * 7.0); // Spawn far out, move towards gun tip
            double coneRadius = currentRadius * 0.8; // Smaller radius for cone effect
            
            // Position particles in a cone shape that follows the beam direction
            // The cone should be oriented along the Z-axis (beam direction)
            double x = Math.cos(Math.toRadians(baseAngle)) * coneRadius;
            double y = Math.sin(Math.toRadians(baseAngle)) * coneRadius; // Vertical component for cone
            double z = coneDistance; // Positive Z to point forward (try this instead)
            
            // Add slight random movement
            x += (Math.sin(time * 6.0f + i) * 0.05);
            z += (Math.cos(time * 5.0f + i) * 0.05);
            
            // Calculate particle size (start random, shrink over lifetime)
            // Use deterministic "random" based on particle index for consistent size
            float deterministicRandom = (float)(Math.sin(i * 1.618f) * 0.5f + 0.5f); // 0-1 range
            float randomStartSize = 0.05f + (deterministicRandom * 0.1f); // Consistent random between 0.05-0.1
            float minSize = 0.01f;
            float currentParticleSize = randomStartSize - (particleLife * (randomStartSize - minSize)); // Shrink from random start to 0.01
            
            // Calculate alpha (fade as they move toward center)
            float particleAlpha = alpha * (1.0f - particleLife * 0.5f);
            
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            
            // Billboard the particle to face the camera using proper camera rotation
            Minecraft minecraft = Minecraft.getInstance();
            //poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
            
            renderColoredParticle(poseStack, consumer, packedLight, cyanColor, particleAlpha, currentParticleSize);
            poseStack.popPose();
        }
    }
    
    private void renderAnomalyExtractionParticles(PoseStack poseStack, MultiBufferSource buffer, int packedLight, 
                                                Vec3 start, Vec3 end, Vec3 direction) {
        
        // Use white wool texture like WarpGateAnomalyRenderer
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentCull(
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/white_concrete.png")));
        
        int cyanColor = 0x00FFFF;
        float alpha = 0.8f;
        
        // Render particles shooting out from the beam end
        for (int i = 0; i < 6; i++) {
            // Use proper time calculation like other renderers
            float time = (System.currentTimeMillis() % 2000) / 1000.0f; // 2 second cycle
            
            // Calculate random direction for particles shooting out
            float angleX = (float) (Math.sin(time * 3.0f + i * 0.8) * Math.PI);
            float angleY = (float) (Math.cos(time * 2.5f + i * 1.2) * Math.PI * 0.5);
            float angleZ = (float) (Math.sin(time * 2.8f + i * 0.6) * Math.PI);
            
            // Calculate position at beam end
            Vec3 particlePos = end.add(
                Math.sin(angleX) * Math.cos(angleY) * 0.3,
                Math.sin(angleY) * 0.3,
                Math.cos(angleZ) * Math.sin(angleY) * 0.3
            );
            
            // Add some movement outward
            float outwardSpeed = (time % 1.0f) * 0.8f;
            particlePos = particlePos.add(
                Math.sin(angleX) * Math.cos(angleY) * outwardSpeed,
                Math.sin(angleY) * outwardSpeed * 0.5,
                Math.cos(angleZ) * Math.sin(angleY) * outwardSpeed
            );
            
            poseStack.pushPose();
            
            poseStack.translate(particlePos.x, particlePos.y, particlePos.z);
            
            renderColoredParticle(poseStack, consumer, packedLight, cyanColor, alpha, 0.2f);
            poseStack.popPose();
        }
    }
    
    private void renderColoredParticle(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, 
                                     int color, float alpha, float halfSize) {
        // Extract RGB components from hex color
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        
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
        
        // Back face (for double-sided rendering)
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
