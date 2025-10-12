package com.hexvane.strangematter.client;

import com.hexvane.strangematter.morph.PlayerMorphData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles rendering players as morphed entities
 */
public class PlayerMorphRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Cache for persistent morph entities to avoid recreation every frame
    private static final Map<UUID, Entity> morphEntityCache = new ConcurrentHashMap<>();
    
    // Recursion guard to prevent infinite loops when rendering players as players
    private static final ThreadLocal<Boolean> isRendering = ThreadLocal.withInitial(() -> false);
    
    /**
     * Renders a player as their morphed entity if they have one
     * @return true if rendered as morph, false otherwise
     */
    public static boolean renderPlayerAsMorph(Player player, EntityRenderDispatcher dispatcher,
                                             PoseStack poseStack, MultiBufferSource buffer,
                                             int packedLight, float partialTicks) {
        
        // Prevent infinite recursion when rendering players as players
        if (isRendering.get()) {
            return false;
        }
        
        // Check if player has a morph
        if (!PlayerMorphData.isMorphed(player.getUUID())) {
            return false;
        }
        
        EntityType<?> morphType = PlayerMorphData.getMorphEntityTypeObj(player.getUUID());
        if (morphType == null) {
            return false;
        }
        
        // Get or create cached morph entity
        Entity morphEntity = morphEntityCache.get(player.getUUID());
        
        // Check if morphing into a player and if we have a target player UUID
        UUID targetPlayerUUID = PlayerMorphData.getMorphedPlayerUUID(player.getUUID());
        boolean isMorphingIntoPlayer = morphType.equals(net.minecraft.world.entity.EntityType.PLAYER);
        
        // Always check if the cached entity type matches the current morph type
        if (morphEntity == null || !morphEntity.getType().equals(morphType)) {
            // Clean up old entity if it exists or type changed
            if (morphEntity != null) {
                morphEntity.remove(Entity.RemovalReason.DISCARDED);
                morphEntityCache.remove(player.getUUID());
            }
            
            // Special handling for player morphs - temporarily disabled to fix server crash
            // TODO: Re-enable player morph skin rendering after fixing client-server separation
            morphEntity = morphType.create(player.level());
            
            if (morphEntity == null) {
                PlayerMorphData.clearMorph(player.getUUID());
                return false;
            }
            
            morphEntityCache.put(player.getUUID(), morphEntity);
        }
        
        // Update morph entity to match player state
        updateMorphEntity(morphEntity, player);
        
        // Render the morph entity
        try {
            // Set recursion guard
            isRendering.set(true);
            
            @SuppressWarnings("unchecked")
            EntityRenderer<Entity> morphRenderer = (EntityRenderer<Entity>) dispatcher.getRenderer(morphEntity);
            
            if (morphRenderer != null) {
                // Render with player's rotation
                morphRenderer.render(
                    morphEntity,
                    player.getYRot(),
                    partialTicks,
                    poseStack,
                    buffer,
                    packedLight
                );
                return true;
            }
        } catch (Exception e) {
            // If rendering fails, clear the morph
            LOGGER.error("Failed to render morph: {}", e.getMessage());
            PlayerMorphData.clearMorph(player.getUUID());
            Entity cachedEntity = morphEntityCache.remove(player.getUUID());
            if (cachedEntity != null) {
                cachedEntity.remove(Entity.RemovalReason.DISCARDED);
            }
        } finally {
            // Always clear recursion guard
            isRendering.set(false);
        }
        
        return false;
    }
    
    /**
     * Updates the morph entity to match the player's state for proper animations
     */
    private static void updateMorphEntity(Entity morphEntity, Player player) {
        // Copy position from player
        morphEntity.setPos(player.getX(), player.getY(), player.getZ());
        
        // Copy old positions for smooth interpolation between render frames
        morphEntity.xo = player.xo;
        morphEntity.yo = player.yo;
        morphEntity.zo = player.zo;
        
        // Copy rotation
        morphEntity.setYRot(player.getYRot());
        morphEntity.setXRot(player.getXRot());
        morphEntity.yRotO = player.yRotO;
        morphEntity.xRotO = player.xRotO;
        
        // Copy movement
        morphEntity.setDeltaMovement(player.getDeltaMovement());
        
        // Copy living entity specific data if applicable
        if (morphEntity instanceof LivingEntity morphLiving) {
            // Copy movement states
            morphLiving.setSpeed(player.getSpeed());
            morphLiving.setShiftKeyDown(player.isShiftKeyDown());
            morphLiving.setSprinting(player.isSprinting());
            morphLiving.setOnGround(player.onGround());
            morphLiving.setPose(player.getPose());
            
            // Copy body and head rotation
            morphLiving.yBodyRot = player.yBodyRot;
            morphLiving.yBodyRotO = player.yBodyRotO;
            morphLiving.yHeadRot = player.yHeadRot;
            morphLiving.yHeadRotO = player.yHeadRotO;
            
            // Copy movement input for animations
            morphLiving.xxa = player.xxa;
            morphLiving.yya = player.yya;
            morphLiving.zza = player.zza;
            
            // Copy if player is swimming/flying for proper animations
            morphLiving.setSwimming(player.isSwimming());
            
            // Copy the player's already-smoothed walk animation state directly
            // This is much smoother than calculating it ourselves each frame
            // Player is always a LivingEntity, so we can safely access walkAnimation
            morphLiving.walkAnimation.setSpeed(player.walkAnimation.speed());
            
            // Manually set the position (there's no direct setter, so we update with 0 acceleration)
            float currentPos = morphLiving.walkAnimation.position();
            float targetPos = player.walkAnimation.position();
            float posDiff = targetPos - currentPos;
            
            // Smoothly interpolate to the target position
            if (Math.abs(posDiff) > 0.001f) {
                morphLiving.walkAnimation.update(morphLiving.walkAnimation.speed(), 0.4f);
            }
        }
    }
    
    /**
     * Clean up cached morph entity for a player
     */
    public static void cleanupMorphEntity(UUID playerUUID) {
        Entity cachedEntity = morphEntityCache.remove(playerUUID);
        if (cachedEntity != null) {
            cachedEntity.remove(Entity.RemovalReason.DISCARDED);
        }
    }
    
    /**
     * Clean up all cached morph entities (call on logout/disconnect)
     */
    public static void cleanupAll() {
        for (Entity entity : morphEntityCache.values()) {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }
        morphEntityCache.clear();
    }
    
    /**
     * Find a player by UUID in the level
     */
    private static net.minecraft.client.player.AbstractClientPlayer findPlayerByUUID(net.minecraft.world.level.Level level, UUID uuid) {
        for (net.minecraft.world.entity.player.Player p : level.players()) {
            if (p.getUUID().equals(uuid) && p instanceof net.minecraft.client.player.AbstractClientPlayer clientPlayer) {
                return clientPlayer;
            }
        }
        return null;
    }
}

