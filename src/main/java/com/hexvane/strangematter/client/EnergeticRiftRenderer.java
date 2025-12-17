package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.EnergeticRiftEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nonnull;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.world.phys.AABB;

public class EnergeticRiftRenderer extends EntityRenderer<EnergeticRiftEntity> {
    
    private static final ResourceLocation RIFT_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/entity/energetic_rift.png");

    // Cache lightning rod lookups so we don't scan blocks every frame.
    private static final int LIGHTNING_ROD_CACHE_TTL_TICKS = 5;
    private static final long LIGHTNING_ROD_CACHE_STALE_TICKS = 200;
    private static final Map<Integer, CachedLightningRod> LIGHTNING_ROD_CACHE = new HashMap<>();

    private static class CachedLightningRod {
        BlockPos rodPos;
        long nextRefreshTick;
        long lastSeenTick;
    }
    
    public EnergeticRiftRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
    }
    
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EnergeticRiftEntity entity) {
        return RIFT_TEXTURE;
    }
    
    @Override
    public void render(@Nonnull EnergeticRiftEntity entity, float entityYaw, float partialTicks, 
                      @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight) {
        
        poseStack.pushPose();
        
        // Position the render and make it billboard face the player
        poseStack.translate(0.0, 0.5, 0.0);
        
        // Billboard to face the camera - try different rotation to ensure visibility
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180)); // Rotate 180 degrees to face player
        
        // Get entity data
        float rotation = entity.getRotation() + partialTicks * 0.5f;
        float pulseIntensity = entity.getPulseIntensity();
        
        // Render the main swirling rift (billboarded)
        renderSwirlingRift(poseStack, buffer, packedLight, rotation, pulseIntensity);
        
        poseStack.popPose();
        
        // Render electric sparks (3D, rotating with the rift)
        poseStack.pushPose();
        poseStack.translate(0.0, 0.5, 0.0);
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90)); // Rotate to match rift orientation
        renderElectricSparks(poseStack, buffer, packedLight, (System.currentTimeMillis() % 360000) / 3000.0f + partialTicks * 0.03f);
        poseStack.popPose();
        
        
        // Render targeting sparks (from center to mobs) - in world space, not rotated
        renderTargetingSparks(poseStack, buffer, packedLight, entity);
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    
    private void renderSwirlingRift(PoseStack poseStack, MultiBufferSource buffer, int packedLight, 
                                   float rotation, float pulseIntensity) {
        
        // Texture is static; avoid calling getTextureLocation with null (Nonnull contract).
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(RIFT_TEXTURE));
        
        // Scale based on pulse intensity
        float scale = 1.0f + pulseIntensity * 0.3f;
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        
        // Render the main rift (already billboarded from parent) - no tinting
        renderBillboard(poseStack, consumer, packedLight, 2.0f, 2.0f, 0xFFFFFF, 0.8f);
        
        // Render inner swirl with rotation - no tinting
        // Move it slightly forward to prevent clipping with the main rift
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 0.01); // Move forward slightly (positive Z in billboard space)
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-rotation * 1.5f));
        renderBillboard(poseStack, consumer, packedLight, 1.2f, 1.2f, 0xFFFFFF, 0.6f);
        poseStack.popPose();
        
        poseStack.popPose();
    }
    
    
    private void renderElectricSparks(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float time) {
        
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        
        int sparkCount = 6;
        for (int i = 0; i < sparkCount; i++) {
            float sparkPhase = (time * 0.8f + i * 0.5f) % 2.5f; // 2.5 second cycle, much slower
            if (sparkPhase > 2.0f) continue; // Spark is "dead" after 2 seconds
            
            // Rotate sparks with the rift - base angle + rift rotation
            float baseAngle = i * (360f / sparkCount);
            float sparkAngle = baseAngle + time * 8f; // Rotate with rift (even faster rotation)
            float sparkRadius = 0.3f + sparkPhase * 2.2f; // Grow from 0.3 to 2.5
            
            // Render lightning-like spark from center outward (3D, coplanar with rift)
            renderLightningSpark(poseStack, consumer, packedLight, time + i, sparkAngle, sparkRadius, sparkPhase, baseAngle);
        }
    }
    private void renderLightningSpark(PoseStack poseStack, VertexConsumer consumer, int packedLight, 
                                   float time, float angle, float maxRadius, float sparkPhase, float baseAngle) {
        
        // Create lightning that goes from center outward in a straight line with zigzags
        int segments = 8; // Fewer segments for cleaner lightning
        
        // Starting position at center
        double x = 0;
        double y = 0;
        double z = 0;
        
        // Calculate the main direction vector (from center outward in the rift plane)
        double baseDirX = Math.cos(Math.toRadians(angle));
        double baseDirY = 0;
        double baseDirZ = Math.sin(Math.toRadians(angle));
        
        // No random needed - using deterministic sine wave patterns
        
        for (int i = 0; i < segments; i++) {
            float t = i / (float) segments;
            float nextT = (i + 1) / (float) segments;
            
            // Calculate next position along the main direction
            double nextX = baseDirX * (nextT * maxRadius);
            double nextY = baseDirY * (nextT * maxRadius);
            double nextZ = baseDirZ * (nextT * maxRadius);
            
            // Add lightning zigzag
            if (i > 0) {
                // Zigzag intensity decreases as we get further from center
                float zigzagIntensity = 0.5f * (1f - t * 0.3f);
                
                // Create perpendicular vectors for zigzag
                double perpX = -baseDirZ; // Perpendicular to main direction
                double perpY = 1.0; // Vertical component
                double perpZ = baseDirX;
                
                // Normalize perpendicular vector
                double perpLength = Math.sqrt(perpX*perpX + perpY*perpY + perpZ*perpZ);
                if (perpLength > 0) {
                    perpX /= perpLength;
                    perpY /= perpLength;
                    perpZ /= perpLength;
                }
                
                // Add zigzag using deterministic noise-like function
                float zigzag1 = (float) Math.sin(time * 1.0 + i * 1.2 + baseAngle * 0.1f) * zigzagIntensity;
                float zigzag2 = (float) Math.sin(time * 1.3 + i * 1.5 + baseAngle * 0.15f) * zigzagIntensity * 0.8f;
                
                // Apply zigzag perpendicular to main direction
                nextX += perpX * zigzag1;
                nextY += perpY * zigzag2;
                nextZ += perpZ * zigzag1;
            }
            
            // Render segment from current to next position
            if (i > 0) {
                // Fade out as spark ages
                float alpha = (1f - sparkPhase * 0.8f) * 0.9f;
                int color = 0x00FFFF; // cyan color
                
                // Calculate thickness - thinner as we get further from center
                float thickness = 0.06f * (1f - t * 0.7f); // Start at 0.06, reduce to 0.018 at the end
                
                renderSparkQuad3D(poseStack, consumer, packedLight, x, y, z, nextX, nextY, nextZ, color, alpha, thickness);
                
                // Create branches at certain points
                if (i > 2 && i < segments - 2 && t > 0.3f && t < 0.8f) {
                    // Branch probability based on position
                    float branchChance = (float) Math.sin(t * Math.PI + baseAngle * 0.1f);
                    if (branchChance > 0.7f) {
                        // Create a branch
                        float branchAngle = angle + 30f + (float) Math.sin(time * 2 + i * 0.5f) * 20f;
                        float branchLength = maxRadius * 0.4f * (1f - t);
                        
                        // Calculate branch direction
                        double branchDirX = Math.cos(Math.toRadians(branchAngle));
                        double branchDirY = 0;
                        double branchDirZ = Math.sin(Math.toRadians(branchAngle));
                        
                        double branchEndX = nextX + branchDirX * branchLength;
                        double branchEndY = nextY + branchDirY * branchLength;
                        double branchEndZ = nextZ + branchDirZ * branchLength;
                        
                        // Render branch
                        float branchAlpha = alpha * 0.7f; // Slightly more transparent
                        float branchThickness = thickness * 0.6f; // Thinner than main branch
                        
                        renderSparkQuad3D(poseStack, consumer, packedLight, nextX, nextY, nextZ, branchEndX, branchEndY, branchEndZ, color, branchAlpha, branchThickness);
                    }
                }
            }
            
            
            // Move to next position
            x = nextX;
            y = nextY;
            z = nextZ;
        }
    }
    
    private void renderSparkQuad3D(PoseStack poseStack, VertexConsumer consumer, int packedLight, 
                                 double x1, double y1, double z1, double x2, double y2, double z2, int color, float alpha, float thickness) {
        
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
        
        // Calculate perpendicular vectors for thickness (use passed parameter)
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
        
        // Render as a proper quad using 4 vertices (like other anomaly renderers)
        // Front face quad
        consumer.vertex(matrix, (float)(x1 + perpX1), (float)(y1 + perpY1), (float)(z1 + perpZ1)).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal((float)dx, (float)dy, (float)dz).endVertex();
        consumer.vertex(matrix, (float)(x2 + perpX1), (float)(y2 + perpY1), (float)(z2 + perpZ1)).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal((float)dx, (float)dy, (float)dz).endVertex();
        consumer.vertex(matrix, (float)(x2 + perpX2), (float)(y2 + perpY2), (float)(z2 + perpZ2)).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal((float)dx, (float)dy, (float)dz).endVertex();
        consumer.vertex(matrix, (float)(x1 + perpX2), (float)(y1 + perpY2), (float)(z1 + perpZ2)).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal((float)dx, (float)dy, (float)dz).endVertex();
        
        // Back face quad (reversed winding)
        consumer.vertex(matrix, (float)(x1 + perpX2), (float)(y1 + perpY2), (float)(z1 + perpZ2)).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal((float)-dx, (float)-dy, (float)-dz).endVertex();
        consumer.vertex(matrix, (float)(x2 + perpX2), (float)(y2 + perpY2), (float)(z2 + perpZ2)).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal((float)-dx, (float)-dy, (float)-dz).endVertex();
        consumer.vertex(matrix, (float)(x2 + perpX1), (float)(y2 + perpY1), (float)(z2 + perpZ1)).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal((float)-dx, (float)-dy, (float)-dz).endVertex();
        consumer.vertex(matrix, (float)(x1 + perpX1), (float)(y1 + perpY1), (float)(z1 + perpZ1)).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal((float)-dx, (float)-dy, (float)-dz).endVertex();
    }
    
    private void renderBillboard(PoseStack poseStack, VertexConsumer consumer, int packedLight, 
                               float width, float height, int color, float alpha) {
        
        Matrix4f matrix = poseStack.last().pose();
        
        // Extract color components
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = alpha;
        
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        
        // Render billboard quad
        consumer.vertex(matrix, -halfWidth, -halfHeight, 0).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, halfWidth, -halfHeight, 0).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, halfWidth, halfHeight, 0).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, -halfWidth, halfHeight, 0).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
    }
    
    private void renderTargetingSparks(PoseStack poseStack, MultiBufferSource buffer, int packedLight, EnergeticRiftEntity entity) {
        // Use lightning render type for targeting sparks
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

        // If a lightning rod is nearby, render targeting sparks ONLY to the rod (not to entities).
        BlockPos rodPos = getNearestLightningRodCached(entity);
        if (rodPos != null) {
            long currentTime = System.currentTimeMillis();
            int cycleTime = 2000; // 2 seconds in milliseconds
            int sparkDuration = 500; // 0.5 seconds visibility
            int sparkTimer = (int) (currentTime % cycleTime);

            if (sparkTimer < sparkDuration) {
                float progress = sparkTimer / (float) sparkDuration;
                float alpha = progress <= 0.2f ? 1.0f : (1.0f - (progress - 0.2f) / 0.8f);
                renderTargetingSparkToRod(poseStack, consumer, packedLight, entity, rodPos, alpha);
            }
            return;
        }
        
        // Create targeting sparks client-side based on entities in range
        // Show sparks that appear instantly and disappear quickly
        AABB zapBox = entity.getBoundingBox().inflate(6.0f); // ZAP_RADIUS
        List<net.minecraft.world.entity.Entity> entitiesInRange = entity.level().getEntities(entity, zapBox);
        
        for (net.minecraft.world.entity.Entity target : entitiesInRange) {
            if (target instanceof net.minecraft.world.entity.LivingEntity && 
                !(target instanceof net.minecraft.world.entity.player.Player && ((net.minecraft.world.entity.player.Player) target).isCreative())) {
                double distance = entity.distanceTo(target);
                if (distance <= 6.0f) {
                    // Create a visual targeting spark from center to entity
                    // Use a timing pattern that matches the server-side cooldown (40 ticks = 2 seconds)
                    // Show sparks for a brief moment every 2 seconds
                    long currentTime = System.currentTimeMillis();
                    int cycleTime = 2000; // 2 seconds in milliseconds
                    int sparkDuration = 500; // 0.5 seconds visibility
                    int sparkTimer = (int) (currentTime % cycleTime);
                    
                    if (sparkTimer < sparkDuration) {
                        // Spark is visible - calculate fade
                        float progress = sparkTimer / (float) sparkDuration;
                        float alpha = progress <= 0.2f ? 1.0f : (1.0f - (progress - 0.2f) / 0.8f); // Fade out after 0.1 seconds
                        
                        renderTargetingSparkToEntity(poseStack, consumer, packedLight, entity, target, alpha);
                    }
                }
            }
        }
    }
    
    private void renderTargetingSparkToEntity(PoseStack poseStack, VertexConsumer consumer, int packedLight, 
                                            EnergeticRiftEntity entity, net.minecraft.world.entity.Entity target, float alpha) {
        // Get positions relative to entity position
        double startX = 0.0; // Relative to entity position
        double startY = 0.5;
        double startZ = 0.0;
        double endX = target.getX() - entity.getX(); // Relative to entity
        double endY = target.getY() + target.getBbHeight() / 2 - entity.getY();
        double endZ = target.getZ() - entity.getZ();
        
        // Create targeting spark that goes straight from start to end with some zigzag
        int segments = 12; // More segments for smoother lightning
        
        // Starting position
        double x = startX;
        double y = startY;
        double z = startZ;
        
        // Calculate direction vector
        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double totalDistance = Math.sqrt(dx*dx + dy*dy + dz*dz);
        dx /= totalDistance; dy /= totalDistance; dz /= totalDistance;
        
        // Use time for zigzag animation (faster flicker)
        float time = (System.currentTimeMillis() % 1000) / 1000.0f; // 1 second cycle
        float sparkAngle = (float) Math.atan2(endZ - startZ, endX - startX) * 180.0f / (float) Math.PI;
        
        for (int i = 0; i < segments; i++) {
            float t = i / (float) segments;
            float nextT = (i + 1) / (float) segments;
            
            // Calculate next position along the direction (full distance instantly)
            double nextX = startX + dx * (nextT * totalDistance);
            double nextY = startY + dy * (nextT * totalDistance);
            double nextZ = startZ + dz * (nextT * totalDistance);
            
            // Add zigzag effect
            if (i > 0) {
                float zigzagIntensity = 0.4f * (1f - t * 0.6f);
                
                // Create perpendicular vectors for zigzag
                double perpX = -dz;
                double perpY = 1.0;
                double perpZ = dx;
                
                // Normalize perpendicular vector
                double perpLength = Math.sqrt(perpX*perpX + perpY*perpY + perpZ*perpZ);
                if (perpLength > 0) {
                    perpX /= perpLength; perpY /= perpLength; perpZ /= perpLength;
                }
                
                // Add zigzag using deterministic function
                float zigzag1 = (float) Math.sin(time * 20 + i * 2.0 + sparkAngle * 0.1f) * zigzagIntensity;
                float zigzag2 = (float) Math.sin(time * 25 + i * 2.5 + sparkAngle * 0.15f) * zigzagIntensity * 0.8f;
                
                nextX += perpX * zigzag1;
                nextY += perpY * zigzag2;
                nextZ += perpZ * zigzag1;
            }
            
            // Render segment
            if (i > 0) {
                int color = 0x00FFFF; // Cyan color
                float thickness = 0.1f * (1f - t * 0.7f); // Start thick, get thinner
                
                renderSparkQuad3D(poseStack, consumer, packedLight, x, y, z, nextX, nextY, nextZ, color, alpha, thickness);
            }
            
            // Move to next position
            x = nextX; y = nextY; z = nextZ;
        }
    }

    private void renderTargetingSparkToRod(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                                          EnergeticRiftEntity entity, BlockPos rodPos, float alpha) {
        // Target the top of the lightning rod for a nice visual
        double targetX = rodPos.getX() + 0.5;
        double targetY = rodPos.getY() + 1.0;
        double targetZ = rodPos.getZ() + 0.5;

        double startX = 0.0;
        double startY = 0.5;
        double startZ = 0.0;

        double endX = targetX - entity.getX();
        double endY = targetY - entity.getY();
        double endZ = targetZ - entity.getZ();

        renderTargetingSparkToPoint(poseStack, consumer, packedLight, startX, startY, startZ, endX, endY, endZ, alpha);
    }

    private void renderTargetingSparkToPoint(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                                            double startX, double startY, double startZ,
                                            double endX, double endY, double endZ,
                                            float alpha) {
        // Create targeting spark that goes straight from start to end with some zigzag
        int segments = 12;

        double x = startX;
        double y = startY;
        double z = startZ;

        // Calculate direction vector
        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double totalDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (totalDistance < 0.0001) {
            return;
        }
        dx /= totalDistance;
        dy /= totalDistance;
        dz /= totalDistance;

        // Use time for zigzag animation (faster flicker)
        float time = (System.currentTimeMillis() % 1000) / 1000.0f; // 1 second cycle

        for (int i = 0; i < segments; i++) {
            float t = i / (float) segments;
            float nextT = (i + 1) / (float) segments;

            // Calculate next position along the direction (full distance instantly)
            double nextX = startX + dx * (nextT * totalDistance);
            double nextY = startY + dy * (nextT * totalDistance);
            double nextZ = startZ + dz * (nextT * totalDistance);

            // Add zigzag effect (match entity targeting style)
            if (i > 0) {
                float zigzagIntensity = 0.4f * (1f - t * 0.6f);

                // Use multiple sine waves for more natural lightning
                float noise1 = (float) Math.sin((t * 10f + time * 8f) * Math.PI * 2) * zigzagIntensity;
                float noise2 = (float) Math.sin((t * 7f + time * 5f) * Math.PI * 2) * zigzagIntensity * 0.6f;
                float noise3 = (float) Math.sin((t * 13f + time * 9f) * Math.PI * 2) * zigzagIntensity * 0.4f;

                // Apply noise perpendicular-ish to direction (simple axis mix)
                nextX += (noise1 + noise3) * (1.0 - Math.abs(dx));
                nextY += (noise2) * (1.0 - Math.abs(dy));
                nextZ += (noise1 - noise3) * (1.0 - Math.abs(dz));
            }

            // Render segment
            renderSparkQuad3D(poseStack, consumer, packedLight,
                x, y, z,
                nextX, nextY, nextZ,
                0x7FDBFF, alpha, 0.06f
            );

            // Update current position
            x = nextX;
            y = nextY;
            z = nextZ;
        }
    }

    private BlockPos getNearestLightningRodCached(EnergeticRiftEntity entity) {
        long gameTime = entity.level().getGameTime();
        int id = entity.getId();

        // Periodic cleanup to prevent unbounded growth
        if (gameTime % 100L == 0L && !LIGHTNING_ROD_CACHE.isEmpty()) {
            LIGHTNING_ROD_CACHE.entrySet().removeIf(e -> (gameTime - e.getValue().lastSeenTick) > LIGHTNING_ROD_CACHE_STALE_TICKS);
        }

        CachedLightningRod cached = LIGHTNING_ROD_CACHE.get(id);
        if (cached != null) {
            cached.lastSeenTick = gameTime;
            if (gameTime < cached.nextRefreshTick) {
                return cached.rodPos;
            }
        } else {
            cached = new CachedLightningRod();
            cached.lastSeenTick = gameTime;
            LIGHTNING_ROD_CACHE.put(id, cached);
        }

        cached.rodPos = findNearestLightningRod(entity);
        cached.nextRefreshTick = gameTime + LIGHTNING_ROD_CACHE_TTL_TICKS;
        return cached.rodPos;
    }

    private BlockPos findNearestLightningRod(EnergeticRiftEntity entity) {
        float lightningRadius = (float) com.hexvane.strangematter.Config.energeticLightningRadius;
        if (lightningRadius <= 0) {
            return null;
        }

        int r = (int) lightningRadius;
        double radiusSq = lightningRadius * lightningRadius;
        BlockPos center = entity.blockPosition();

        BlockPos best = null;
        double bestHorizSq = Double.MAX_VALUE;

        for (int x = -r; x <= r; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -r; z <= r; z++) {
                    double horizSq = (double) x * (double) x + (double) z * (double) z;
                    if (horizSq > radiusSq) {
                        continue;
                    }

                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = entity.level().getBlockState(pos);
                    if (state.is(Blocks.LIGHTNING_ROD)) {
                        if (horizSq < bestHorizSq) {
                            bestHorizSq = horizSq;
                            best = pos;
                        }
                    }
                }
            }
        }

        return best;
    }
    
}
