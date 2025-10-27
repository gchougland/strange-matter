package com.hexvane.strangematter.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.GravityData;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hexvane.strangematter.network.NetworkHandler;
import com.hexvane.strangematter.network.GravitySyncPacket;

public class GravityAnomalyEntity extends BaseAnomalyEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(GravityAnomalyEntity.class);
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(GravityAnomalyEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Constants for the gravity anomaly (now read from config)
    private float getLevitationRadius() {
        return (float) com.hexvane.strangematter.Config.gravityLevitationRadius;
    }
    
    @Override
    protected float getEffectRadius() {
        return getLevitationRadius();
    }
    
    
    private float getLevitationForce() {
        return (float) com.hexvane.strangematter.Config.gravityLevitationForce;
    }
    
    private static final float AURA_RADIUS = 2.0f;
    
    // Track affected players and their modifiers
    private Set<Player> affectedPlayers = new HashSet<>();
    private AttributeModifier lowGravityModifier = new AttributeModifier(ResourceLocation.fromNamespaceAndPath("strangematter", "low_gravity"), 0.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
    
    // Sound system - using StrangeMatterSounds for consistency
    
    public GravityAnomalyEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ACTIVE, true);
    }
    
    @Override
    protected void applyAnomalyEffects() {
        if (!this.isActive() || this.isContained() || !com.hexvane.strangematter.Config.enableGravityEffects) {
            return; // Don't apply levitation if not active, contained, or effects disabled
        }
        
        float levitationRadius = getLevitationRadius();
        AABB levitationBox = this.getBoundingBox().inflate(levitationRadius);
        List<Entity> entitiesInRange = this.level().getEntities(this, levitationBox);
        
        // Clean up players who are no longer in range
        affectedPlayers.removeIf(player -> {
            double distance = this.distanceTo(player);
            if (distance > levitationRadius) {
                // Player left the area, remove gravity data
                GravityData.removePlayerGravityForce(player.getUUID());
                player.getPersistentData().remove("strangematter.gravity_force");
                
                // Send packet to clear gravity force on client
                if (!level().isClientSide) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer((net.minecraft.server.level.ServerPlayer) player, new GravitySyncPacket(0.0)); // Send 0 to clear the effect
                    LOGGER.info("[GRAVITY ANOMALY] Server: Player {} left range, sent clear packet", player.getName().getString());
                }
                return true;
            }
            return false;
        });
        
        for (Entity entity : entitiesInRange) {
            if (entity instanceof Player && ((Player) entity).isCreative()) {
                continue; // Skip creative players
            }
            
            // Calculate distance from anomaly center
            double distance = this.distanceTo(entity);
            if (distance <= levitationRadius) {
                applyLowGravityEffect(entity, distance);
            }
        }
    }
    
    private void applyLowGravityEffect(Entity entity, double distance) {
        // Calculate force multiplier based on distance (stronger closer to center)
        float levitationRadius = getLevitationRadius();
        double forceMultiplier = 1.0 - (distance / levitationRadius);
        forceMultiplier = Math.max(0.1, forceMultiplier); // Minimum effect even at edge
        
        Vec3 currentVelocity = entity.getDeltaMovement();
        
        // Apply different effects based on entity type
        if (entity instanceof ItemEntity) {
            applyItemLowGravity((ItemEntity) entity, currentVelocity, forceMultiplier);
        } else if (entity instanceof Player) {
            applyPlayerLowGravity((Player) entity, currentVelocity, forceMultiplier);
        } else if (entity instanceof LivingEntity) {
            applyMobLowGravity((LivingEntity) entity, currentVelocity, forceMultiplier);
        } else {
            applyGenericLowGravity(entity, currentVelocity, forceMultiplier);
        }
    }
    
    private void applyItemLowGravity(ItemEntity item, Vec3 currentVelocity, double forceMultiplier) {
        // Create gentle floating motion for items
        double time = (System.currentTimeMillis() % 360000) / 1000.0;
        double itemId = item.getId() * 0.1; // Unique offset per item
        
        // Gentle sine wave floating motion
        double floatHeight = Math.sin(time * 0.5 + itemId) * 0.1 * forceMultiplier;
        double driftX = Math.cos(time * 0.3 + itemId) * 0.02 * forceMultiplier;
        double driftZ = Math.sin(time * 0.4 + itemId) * 0.02 * forceMultiplier;
        
        Vec3 newVelocity = new Vec3(
            currentVelocity.x * 0.8 + driftX,  // Slow down horizontal movement
            currentVelocity.y * 0.3 + floatHeight,  // Reduce gravity, add floating
            currentVelocity.z * 0.8 + driftZ   // Slow down horizontal movement
        );
        
        // Prevent items from flying away
        newVelocity = newVelocity.multiply(1.0, 1.0, 1.0);
        if (newVelocity.length() > 0.5) {
            newVelocity = newVelocity.normalize().multiply(0.5, 0.5, 0.5);
        }
        
        item.setDeltaMovement(newVelocity);
    }
    
    private void applyPlayerLowGravity(Player player, Vec3 currentVelocity, double forceMultiplier) {
        // Store the player and force multiplier for event-based gravity modification
        if (!affectedPlayers.contains(player)) {
            affectedPlayers.add(player);
        }
        
        // Store the force multiplier in static data that both client and server can access
        GravityData.setPlayerGravityForce(player.getUUID(), forceMultiplier);
        
        // Also store in persistent data for fallback
        player.getPersistentData().putDouble("strangematter.gravity_force", forceMultiplier);
        
        // Send packet to client for synchronization
        if (!this.level().isClientSide) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer((net.minecraft.server.level.ServerPlayer) player, new GravitySyncPacket(forceMultiplier));
        } else {
            LOGGER.info("[GRAVITY ANOMALY] Client: Player {} in field, force: {}", player.getName().getString(), forceMultiplier);
        }
    }
    
    private void applyMobLowGravity(LivingEntity mob, Vec3 currentVelocity, double forceMultiplier) {
        // Create mesmerizing floating motion for mobs
        double time = (System.currentTimeMillis() % 360000) / 1000.0;
        double mobId = mob.getId() * 0.1; // Unique offset per mob
        
        // Create complex floating motion similar to dirt block particles
        double floatHeight = Math.sin(time * 0.8 + mobId) * 0.15 * forceMultiplier;
        double driftX = Math.cos(time * 0.6 + mobId) * 0.03 * forceMultiplier;
        double driftZ = Math.sin(time * 0.7 + mobId) * 0.03 * forceMultiplier;
        
        // Add secondary wave for more complex motion
        double secondaryFloat = Math.sin(time * 1.2 + mobId * 1.5) * 0.05 * forceMultiplier;
        double secondaryDriftX = Math.cos(time * 0.9 + mobId * 1.3) * 0.01 * forceMultiplier;
        double secondaryDriftZ = Math.sin(time * 1.1 + mobId * 1.7) * 0.01 * forceMultiplier;
        
        Vec3 newVelocity = new Vec3(
            (currentVelocity.x * 0.7) + driftX + secondaryDriftX,  // Slow down and add floating drift
            (currentVelocity.y * 0.2) + floatHeight + secondaryFloat,  // Strong gravity reduction + floating
            (currentVelocity.z * 0.7) + driftZ + secondaryDriftZ   // Slow down and add floating drift
        );
        
        // Add gentle rotation for floating mobs
        if (forceMultiplier > 0.3) {
            float rotationSpeed = (float)(forceMultiplier * 1.5);
            mob.setYRot(mob.getYRot() + rotationSpeed);
        }
        
        mob.setDeltaMovement(newVelocity);
    }
    
    private void applyGenericLowGravity(Entity entity, Vec3 currentVelocity, double forceMultiplier) {
        // Generic entities get basic low gravity effect
        float levitationForce = getLevitationForce();
        Vec3 upwardForce = new Vec3(0, levitationForce * forceMultiplier * 0.6, 0);
        Vec3 newVelocity = currentVelocity.add(upwardForce);
        
        // Reduce downward velocity
        if (newVelocity.y < 0) {
            newVelocity = new Vec3(newVelocity.x, newVelocity.y * 0.3, newVelocity.z);
        }
        
        entity.setDeltaMovement(newVelocity);
    }
    
    @Override
    protected void spawnParticles() {
        if (this.level().isClientSide) return;
        
        // Spawn levitation particles
        if (tickCount % (20 / PARTICLE_SPAWN_RATE) == 0) {
            double radius = getLevitationRadius() * 0.8;
            double angle = this.level().random.nextDouble() * 2 * Math.PI;
            double x = this.getX() + Math.cos(angle) * radius * this.level().random.nextDouble();
            double z = this.getZ() + Math.sin(angle) * radius * this.level().random.nextDouble();
            double y = this.getY() - 1;
            
            // Spawn upward drifting particles
            this.level().addParticle(
                ParticleTypes.END_ROD,
                x, y, z,
                0, 0.1, 0
            );
        }
        
        // Spawn aura particles around the core
        if (tickCount % 10 == 0) {
            double auraRadius = AURA_RADIUS;
            for (int i = 0; i < 3; i++) {
                double angle = (tickCount * 0.1) + (i * Math.PI * 2 / 3);
                double x = this.getX() + Math.cos(angle) * auraRadius;
                double z = this.getZ() + Math.sin(angle) * auraRadius;
                double y = this.getY() + 0.5;
                
                this.level().addParticle(
                    ParticleTypes.PORTAL,
                    x, y, z,
                    0, 0.05, 0
                );
            }
        }
    }
    
    @Override
    protected void updateClientEffects() {
        // Client-side visual effects
        // This will be handled by the renderer
    }
    
    @Override
    public ResourceLocation getAnomalySound() {
        return com.hexvane.strangematter.sound.StrangeMatterSounds.GRAVITY_ANOMALY_LOOP.get().getLocation();
    }
    
    @Override
    protected ResearchType getResearchType() {
        return ResearchType.GRAVITY;
    }
    
    @Override
    protected String getAnomalyName() {
        return "Gravity";
    }
    
    @Override
    protected DeferredHolder<Block, Block> getShardOreBlock() {
        return StrangeMatterMod.GRAVITIC_SHARD_ORE_BLOCK;
    }
    
    public float getAuraRadius() {
        return AURA_RADIUS;
    }
    
    // Method to get the ambient sound (not an override since Entity doesn't have this)
    public SoundEvent getAmbientSound() {
        // Return the gravity anomaly loop sound
        return SoundEvents.AMBIENT_CAVE.value();
    }
    
    public boolean isActive() {
        return this.entityData.get(IS_ACTIVE);
    }
    
    public void setActive(boolean active) {
        this.entityData.set(IS_ACTIVE, active);
    }
}