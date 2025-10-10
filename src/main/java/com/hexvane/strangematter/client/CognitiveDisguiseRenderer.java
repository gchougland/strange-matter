package com.hexvane.strangematter.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.Registries;

/**
 * Handles rendering mobs with cognitive disguises applied by the Thoughtwell anomaly.
 * This system allows mobs to appear as different random mob types when within the Thoughtwell's radius.
 */
public class CognitiveDisguiseRenderer {
    
    // Cache for persistent disguise entities to avoid recreation every frame
    private static final java.util.Map<java.util.UUID, Entity> disguiseEntityCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    // List of common mob types that can be used as disguises
    private static final String[] DISGUISE_TYPES = {
        "minecraft:chicken",
        "minecraft:cow", 
        "minecraft:pig",
        "minecraft:sheep",
        "minecraft:villager",
        "minecraft:zombie",
        "minecraft:skeleton",
        "minecraft:spider",
        "minecraft:creeper",
        "minecraft:enderman",
        "minecraft:witch",
        "minecraft:slime",
        "minecraft:husk",
        "minecraft:stray",
        "minecraft:cave_spider",
        "minecraft:silverfish",
        "minecraft:endermite",
        "minecraft:magma_cube",
        "minecraft:blaze"
    };
    
    /**
     * Renders a mob with its cognitive disguise if it has one.
     * This should be called from the main entity renderer.
     */
    public static boolean renderWithDisguise(Mob mob, EntityRenderDispatcher dispatcher, 
                                           PoseStack poseStack, MultiBufferSource buffer, int packedLight, float partialTicks) {
        
        // Check if the mob has a cognitive disguise
        if (!com.hexvane.strangematter.entity.ThoughtwellEntity.hasDisguise(mob.getUUID())) {
            return false;
        }
        
        String disguiseTypeString = com.hexvane.strangematter.entity.ThoughtwellEntity.getDisguiseType(mob.getUUID());
        int disguiseDuration = com.hexvane.strangematter.entity.ThoughtwellEntity.getDisguiseDuration(mob.getUUID());
        
        if (disguiseDuration <= 0) {
            com.hexvane.strangematter.entity.ThoughtwellEntity.removeDisguise(mob.getUUID());
            // Clean up cached entity
            Entity cachedEntity = disguiseEntityCache.remove(mob.getUUID());
            if (cachedEntity != null) {
                cachedEntity.remove(Entity.RemovalReason.DISCARDED);
            }
            return false;
        }
        
        // Get or create persistent disguise entity
        Entity disguiseEntity = disguiseEntityCache.get(mob.getUUID());
        if (disguiseEntity == null) {
            EntityType<?> disguiseType = mob.level().registryAccess().registryOrThrow(Registries.ENTITY_TYPE).get(ResourceLocation.parse(disguiseTypeString));
            if (disguiseType == null) {
                com.hexvane.strangematter.entity.ThoughtwellEntity.removeDisguise(mob.getUUID());
                return false;
            }
            
            disguiseEntity = disguiseType.create(mob.level());
            if (disguiseEntity == null) {
                com.hexvane.strangematter.entity.ThoughtwellEntity.removeDisguise(mob.getUUID());
                return false;
            }
            
            // Add to world so it gets properly ticked
            mob.level().addFreshEntity(disguiseEntity);
            disguiseEntityCache.put(mob.getUUID(), disguiseEntity);
        }
        
        // Update the persistent entity's state to match the original mob
        updateDisguiseEntity(disguiseEntity, mob);
        
        // Get the renderer for the cached entity
        try {
            @SuppressWarnings("unchecked")
            EntityRenderer<Entity> disguiseRenderer = (EntityRenderer<Entity>) dispatcher.getRenderer(disguiseEntity);
            
            if (disguiseRenderer != null) {
                disguiseRenderer.render(
                    disguiseEntity, 
                    mob.getYRot(), 
                    partialTicks, 
                    poseStack, 
                    buffer, 
                    packedLight
                );
                return true;
            }
        } catch (Exception e) {
            com.hexvane.strangematter.entity.ThoughtwellEntity.removeDisguise(mob.getUUID());
            Entity cachedEntity = disguiseEntityCache.remove(mob.getUUID());
            if (cachedEntity != null) {
                cachedEntity.remove(Entity.RemovalReason.DISCARDED);
            }
            return false;
        }
        
        return false;
    }
    
    /**
     * Updates a persistent disguise entity to match the original mob's state.
     */
    private static void updateDisguiseEntity(Entity disguiseEntity, Mob originalMob) {
        // Copy position from the original mob
        disguiseEntity.setPos(originalMob.getX(), originalMob.getY(), originalMob.getZ());
        
        // Copy old position for smooth interpolation
        disguiseEntity.xo = originalMob.xo;
        disguiseEntity.yo = originalMob.yo;
        disguiseEntity.zo = originalMob.zo;
        
        // Copy rotation
        float targetYRot = originalMob.getYRot();
        float targetXRot = originalMob.getXRot();
        disguiseEntity.setYRot(targetYRot);
        disguiseEntity.setXRot(targetXRot);
        disguiseEntity.yRotO = originalMob.yRotO;
        disguiseEntity.xRotO = originalMob.xRotO;
        
        // Copy movement
        disguiseEntity.setDeltaMovement(originalMob.getDeltaMovement());
        
        // Copy essential movement states and manually handle animation
        if (disguiseEntity instanceof LivingEntity disguiseLiving) {
            // Copy essential movement states
            disguiseLiving.setSpeed(originalMob.getSpeed());
            disguiseLiving.setShiftKeyDown(originalMob.isShiftKeyDown());
            disguiseLiving.setSprinting(originalMob.isSprinting());
            disguiseLiving.setOnGround(originalMob.onGround());
            disguiseLiving.setPose(originalMob.getPose());
            
            // Copy body and head rotation
            disguiseLiving.yBodyRot = originalMob.yBodyRot;
            disguiseLiving.yBodyRotO = originalMob.yBodyRotO;
            disguiseLiving.yHeadRot = originalMob.yHeadRot;
            disguiseLiving.yHeadRotO = originalMob.yHeadRotO;
            
            // Copy movement input for natural movement
            disguiseLiving.xxa = originalMob.xxa;
            disguiseLiving.yya = originalMob.yya;
            disguiseLiving.zza = originalMob.zza;
            
            // Copy the mob's walk animation state directly (same approach as player morphs)
            disguiseLiving.walkAnimation.setSpeed(originalMob.walkAnimation.speed());
            
            // Manually set the position (there's no direct setter, so we update with 0 acceleration)
            float currentPos = disguiseLiving.walkAnimation.position();
            float targetPos = originalMob.walkAnimation.position();
            float posDiff = targetPos - currentPos;
            
            // Smoothly interpolate to the target position
            if (Math.abs(posDiff) > 0.001f) {
                disguiseLiving.walkAnimation.update(disguiseLiving.walkAnimation.speed(), 0.4f);
            }
        }
    }
    
    /**
     * Gets a random disguise type from the available types.
     */
    public static String getRandomDisguiseType() {
        return DISGUISE_TYPES[(int) (Math.random() * DISGUISE_TYPES.length)];
    }
    
    /**
     * Cleans up cached entities when disguises are removed.
     */
    public static void cleanupDisguise(java.util.UUID mobUUID) {
        Entity cachedEntity = disguiseEntityCache.remove(mobUUID);
        if (cachedEntity != null) {
            cachedEntity.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}