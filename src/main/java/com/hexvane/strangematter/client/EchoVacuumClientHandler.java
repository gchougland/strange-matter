package com.hexvane.strangematter.client;

import com.hexvane.strangematter.item.EchoVacuumItem;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.mojang.math.Axis;

@OnlyIn(Dist.CLIENT)
public class EchoVacuumClientHandler {
    
    private final EchoVacuumBeamRenderer beamRenderer;
    
    public EchoVacuumClientHandler() {
        this.beamRenderer = new EchoVacuumBeamRenderer();
    }
    
    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        
        if (localPlayer == null) {
            return;
        }
        
        // Render beams for all players using Echo Vacuum (including local player)
        for (net.minecraft.world.entity.player.Player player : com.hexvane.strangematter.StrangeMatterMod.getPlayersUsingBeam()) {
            renderBeamForPlayer(event, minecraft, player, player == localPlayer);
        }
    }
    
    private void renderBeamForPlayer(RenderLevelStageEvent event, Minecraft minecraft, 
                                   net.minecraft.world.entity.player.Player player, boolean isLocalPlayer) {
        
        // For other players, always use third-person perspective
        boolean isFirstPerson = isLocalPlayer && minecraft.options.getCameraType().isFirstPerson();
        
        // Calculate gun tip position based on view perspective
        Vec3 gunTipPos = calculateGunTipPosition(player, isFirstPerson);
        
        // Calculate beam direction and end position using raycasting
        Vec3 lookAngle = player.getLookAngle();
        float maxRange = 8.0f;
        
        // Try using player.pick() method for raycasting (only works for local player)
        net.minecraft.world.phys.HitResult hitResult;
        if (isLocalPlayer) {
            hitResult = ((net.minecraft.client.player.LocalPlayer) player).pick(maxRange, 1.0f, false);
        } else {
            // For other players, use simple distance calculation (can't raycast on client for other players)
            hitResult = new net.minecraft.world.phys.HitResult(Vec3.ZERO) {
                @Override
                public net.minecraft.world.phys.HitResult.Type getType() {
                    return net.minecraft.world.phys.HitResult.Type.MISS;
                }
            };
        }
        
        Vec3 beamEnd;
        if (hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            beamEnd = hitResult.getLocation();
        } else {
            beamEnd = gunTipPos.add(lookAngle.scale(maxRange));
        }
        
        // Translate to camera space for proper rendering
        event.getPoseStack().pushPose();
        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
        event.getPoseStack().translate(
            gunTipPos.x - cameraPos.x,
            gunTipPos.y - cameraPos.y,
            gunTipPos.z - cameraPos.z
        );
        
        // Rotate the pose stack to match the player's look direction
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(-yaw));
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(pitch));
        
        // Calculate beam direction in local space
        Vec3 localBeamEnd = beamEnd.subtract(gunTipPos);
        // Transform to local coordinates (rotate back to align with Z-axis)
        float cosYaw = (float) Math.cos(Math.toRadians(yaw));
        float sinYaw = (float) Math.sin(Math.toRadians(yaw));
        float cosPitch = (float) Math.cos(Math.toRadians(pitch));
        float sinPitch = (float) Math.sin(Math.toRadians(pitch));
        
        double localX = localBeamEnd.x * cosYaw + localBeamEnd.z * sinYaw;
        double localY = localBeamEnd.y * cosPitch + (localBeamEnd.x * sinYaw - localBeamEnd.z * cosYaw) * sinPitch;
        double localZ = -(localBeamEnd.x * sinYaw - localBeamEnd.z * cosYaw) * cosPitch + localBeamEnd.y * sinPitch;
        
        Vec3 localBeamDirection = new Vec3(localX, localY, localZ);
        Vec3 localBeamEndPos = new Vec3(localX, localY, localZ);
        
        beamRenderer.renderBeam(event.getPoseStack(), minecraft.renderBuffers().bufferSource(), 
                              LevelRenderer.getLightColor(minecraft.level, player.blockPosition()), 
                              Vec3.ZERO, localBeamDirection, new Vec3(0, 0, 1), isFirstPerson, 
                              hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK, localBeamEndPos);
        
        event.getPoseStack().popPose();
    }
    
    private Vec3 calculateGunTipPosition(net.minecraft.world.entity.player.Player player, boolean isFirstPerson) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookAngle = player.getLookAngle();
        
        if (isFirstPerson) {
            // In first-person, calculate actual gun tip position
            // Gun is held down and to the right in first-person
            Vec3 rightSide = Vec3.directionFromRotation(0, player.getYRot() + 90).scale(0.1); // Right side
            Vec3 downOffset = new Vec3(0, -0.2, 0); // Down offset
            Vec3 forwardOffset = lookAngle.scale(0.6); // Forward from camera
            
            return eyePos.add(rightSide).add(downOffset).add(forwardOffset);
        } else {
            // In third-person, gun tip is at the player's hand position
            // Approximate hand position (right side of player)
            Vec3 rightSide = Vec3.directionFromRotation(0, player.getYRot() + 90).scale(0.1);
            Vec3 handPos = eyePos.add(rightSide).add(new Vec3(1.0, 0, 0));
            return handPos.add(lookAngle.scale(0.5));
        }
    }
}
