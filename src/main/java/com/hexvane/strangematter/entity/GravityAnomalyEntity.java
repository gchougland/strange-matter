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
import com.hexvane.strangematter.client.sound.CustomSoundManager;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.GravityData;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class GravityAnomalyEntity extends Entity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(GravityAnomalyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PULSE_INTENSITY = SynchedEntityData.defineId(GravityAnomalyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CONTAINED = SynchedEntityData.defineId(GravityAnomalyEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Constants for the gravity anomaly
    private static final float LEVITATION_RADIUS = 8.0f;
    private static final float AURA_RADIUS = 2.0f;
    private static final float LEVITATION_FORCE = 0.1f;
    private static final float ROTATION_SPEED = 0.5f;
    private static final int PARTICLE_SPAWN_RATE = 20; // particles per second
    
    // Animation timers
    private int tickCount = 0;
    private float lastPulseTime = 0;
    
    // Track affected players and their modifiers
    private Set<Player> affectedPlayers = new HashSet<>();
    private AttributeModifier lowGravityModifier = new AttributeModifier("Low Gravity", 0.0, AttributeModifier.Operation.ADDITION);
    
    // Sound system
    private static final float MAX_SOUND_DISTANCE = 10.0f; // Maximum distance to hear the sound
    private static final ResourceLocation GRAVITY_ANOMALY_SOUND = new ResourceLocation("strangematter", "gravity_anomaly_loop");
    
    // Sound tracking
    private boolean isSoundActive = false;
    private float lastCalculatedVolume = 0.0f;
    
    public GravityAnomalyEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }
    
    @Override
    protected void defineSynchedData() {
        this.entityData.define(ROTATION, 0.0f);
        this.entityData.define(PULSE_INTENSITY, 0.0f);
        this.entityData.define(IS_CONTAINED, false);
    }
    
    
    
    @Override
    public void tick() {
        super.tick();
        tickCount++;
        
        if (!this.level().isClientSide) {
            // Server-side logic
            updateRotation();
            updatePulseAnimation();
            applyLevitationField();
            spawnParticles();
        } else {
            // Client-side logic
            updateClientEffects();
            updateSoundEffects();
            // Apply client-side effects for smoother visuals
            applyClientSideEffects();
        }
    }
    
    private void updateRotation() {
        float currentRotation = this.entityData.get(ROTATION);
        float newRotation = currentRotation + ROTATION_SPEED;
        if (newRotation >= 360.0f) {
            newRotation -= 360.0f;
        }
        this.entityData.set(ROTATION, newRotation);
    }
    
    private void updatePulseAnimation() {
        // Create a pulsing effect every few seconds
        if (tickCount % 60 == 0) { // Every 3 seconds (60 ticks)
            this.entityData.set(PULSE_INTENSITY, 1.0f);
        }
        
        // Gradually decrease pulse intensity
        float currentPulse = this.entityData.get(PULSE_INTENSITY);
        if (currentPulse > 0) {
            this.entityData.set(PULSE_INTENSITY, Math.max(0, currentPulse - 0.05f));
        }
    }
    
    private void applyLevitationField() {
        if (this.entityData.get(IS_CONTAINED)) {
            return; // Don't apply levitation if contained
        }
        
        AABB levitationBox = this.getBoundingBox().inflate(LEVITATION_RADIUS);
        List<Entity> entitiesInRange = this.level().getEntities(this, levitationBox);
        
        // Clean up players who are no longer in range
        affectedPlayers.removeIf(player -> {
            double distance = this.distanceTo(player);
            if (distance > LEVITATION_RADIUS) {
                // Player left the area, remove gravity data
                GravityData.removePlayerGravityForce(player.getUUID());
                player.getPersistentData().remove("strangematter.gravity_force");
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
            if (distance <= LEVITATION_RADIUS) {
                applyLowGravityEffect(entity, distance);
            }
        }
    }
    
    private void applyLowGravityEffect(Entity entity, double distance) {
        // Calculate force multiplier based on distance (stronger closer to center)
        double forceMultiplier = 1.0 - (distance / LEVITATION_RADIUS);
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
        Vec3 upwardForce = new Vec3(0, LEVITATION_FORCE * forceMultiplier * 0.6, 0);
        Vec3 newVelocity = currentVelocity.add(upwardForce);
        
        // Reduce downward velocity
        if (newVelocity.y < 0) {
            newVelocity = new Vec3(newVelocity.x, newVelocity.y * 0.3, newVelocity.z);
        }
        
        entity.setDeltaMovement(newVelocity);
    }
    
    private void spawnParticles() {
        if (this.level().isClientSide) return;
        
        // Spawn levitation particles
        if (tickCount % (20 / PARTICLE_SPAWN_RATE) == 0) {
            double radius = LEVITATION_RADIUS * 0.8;
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
    
    private void updateClientEffects() {
        // Client-side visual effects
        // This will be handled by the renderer
    }
    
    private void applyClientSideEffects() {
        // Apply client-side effects for smoother visuals
        AABB levitationBox = this.getBoundingBox().inflate(LEVITATION_RADIUS);
        List<Entity> entitiesInRange = this.level().getEntities(this, levitationBox);
        
        for (Entity entity : entitiesInRange) {
            if (entity instanceof Player && ((Player) entity).isCreative()) {
                continue; // Skip creative players
            }
            
            // Calculate distance from anomaly center
            double distance = this.distanceTo(entity);
            if (distance <= LEVITATION_RADIUS) {
                applyClientSideLowGravity(entity, distance);
            }
        }
    }
    
    private void applyClientSideLowGravity(Entity entity, double distance) {
        // Calculate force multiplier based on distance
        double forceMultiplier = 1.0 - (distance / LEVITATION_RADIUS);
        forceMultiplier = Math.max(0.1, forceMultiplier);
        
        Vec3 currentVelocity = entity.getDeltaMovement();
        
        // Apply client-side effects for items and mobs only
        if (entity instanceof ItemEntity) {
            applyClientSideItemEffect((ItemEntity) entity, currentVelocity, forceMultiplier);
        } else if (entity instanceof LivingEntity && !(entity instanceof Player)) {
            applyClientSideMobEffect((LivingEntity) entity, currentVelocity, forceMultiplier);
        }
    }
    
    private void applyClientSideItemEffect(ItemEntity item, Vec3 currentVelocity, double forceMultiplier) {
        // Apply the same floating motion on client side for smooth visuals
        double time = (System.currentTimeMillis() % 360000) / 1000.0;
        double itemId = item.getId() * 0.1;
        
        double floatHeight = Math.sin(time * 0.5 + itemId) * 0.1 * forceMultiplier;
        double driftX = Math.cos(time * 0.3 + itemId) * 0.02 * forceMultiplier;
        double driftZ = Math.sin(time * 0.4 + itemId) * 0.02 * forceMultiplier;
        
        Vec3 newVelocity = new Vec3(
            currentVelocity.x * 0.8 + driftX,
            currentVelocity.y * 0.3 + floatHeight,
            currentVelocity.z * 0.8 + driftZ
        );
        
        if (newVelocity.length() > 0.5) {
            newVelocity = newVelocity.normalize().multiply(0.5, 0.5, 0.5);
        }
        
        item.setDeltaMovement(newVelocity);
    }
    
    private void applyClientSideMobEffect(LivingEntity mob, Vec3 currentVelocity, double forceMultiplier) {
        // Apply the same floating motion on client side for smooth visuals
        double time = (System.currentTimeMillis() % 360000) / 1000.0;
        double mobId = mob.getId() * 0.1;
        
        double floatHeight = Math.sin(time * 0.8 + mobId) * 0.15 * forceMultiplier;
        double driftX = Math.cos(time * 0.6 + mobId) * 0.03 * forceMultiplier;
        double driftZ = Math.sin(time * 0.7 + mobId) * 0.03 * forceMultiplier;
        
        double secondaryFloat = Math.sin(time * 1.2 + mobId * 1.5) * 0.05 * forceMultiplier;
        double secondaryDriftX = Math.cos(time * 0.9 + mobId * 1.3) * 0.01 * forceMultiplier;
        double secondaryDriftZ = Math.sin(time * 1.1 + mobId * 1.7) * 0.01 * forceMultiplier;
        
        Vec3 newVelocity = new Vec3(
            (currentVelocity.x * 0.7) + driftX + secondaryDriftX,
            (currentVelocity.y * 0.2) + floatHeight + secondaryFloat,
            (currentVelocity.z * 0.7) + driftZ + secondaryDriftZ
        );
        
        if (forceMultiplier > 0.3) {
            float rotationSpeed = (float)(forceMultiplier * 1.5);
            mob.setYRot(mob.getYRot() + rotationSpeed);
        }
        
        mob.setDeltaMovement(newVelocity);
    }
    
    private void updateSoundEffects() {
        // Find the nearest player
        Player nearestPlayer = this.level().getNearestPlayer(this, MAX_SOUND_DISTANCE);
        
        if (nearestPlayer == null) {
            // No player nearby, stop sound
            if (isSoundActive) {
                CustomSoundManager.getInstance().stopAmbientSound(GRAVITY_ANOMALY_SOUND);
                isSoundActive = false;
            }
            return;
        }
        
        // Calculate distance to player
        double distance = this.distanceTo(nearestPlayer);
        
        if (distance > MAX_SOUND_DISTANCE) {
            // Player too far, stop sound
            if (isSoundActive) {
                CustomSoundManager.getInstance().stopAmbientSound(GRAVITY_ANOMALY_SOUND);
                isSoundActive = false;
            }
            return;
        }
        
        // Calculate volume based on distance
        float volume = calculateSoundVolume(distance);
        
        // Player is in range, manage continuous sound
        if (!isSoundActive) {
            // Start the sound
            CustomSoundManager.getInstance().playAmbientSound(
                GRAVITY_ANOMALY_SOUND,
                this.getX(), this.getY(), this.getZ(),
                volume,
                true // Loop continuously
            );
            isSoundActive = true;
            lastCalculatedVolume = volume;
        } else {
            // Update volume if it changed significantly
            if (Math.abs(volume - lastCalculatedVolume) > 0.01f) {
                CustomSoundManager.getInstance().updateSoundVolume(GRAVITY_ANOMALY_SOUND, volume);
                lastCalculatedVolume = volume;
            }
            
            // Update position
            CustomSoundManager.getInstance().updateSoundPosition(
                GRAVITY_ANOMALY_SOUND,
                this.getX(), this.getY(), this.getZ()
            );
        }
    }
    
    private float calculateSoundVolume(double distance) {
        // Linear interpolation from max volume at 0 distance to min volume at max distance
        float volumeRatio = 1.0f - (float)(distance / MAX_SOUND_DISTANCE);
        volumeRatio = Math.max(0.0f, Math.min(1.0f, volumeRatio)); // Clamp between 0 and 1
        
        return 0.3f + (volumeRatio * 0.7f); // Range from 0.3 to 1.0
    }
    
    // Method to get the ambient sound (not an override since Entity doesn't have this)
    public SoundEvent getAmbientSound() {
        // Return the gravity anomaly loop sound
        return SoundEvents.AMBIENT_CAVE.get();
    }
    
    public float getRotation() {
        return this.entityData.get(ROTATION);
    }
    
    public float getPulseIntensity() {
        return this.entityData.get(PULSE_INTENSITY);
    }
    
    public boolean isContained() {
        return this.entityData.get(IS_CONTAINED);
    }
    
    public void setContained(boolean contained) {
        this.entityData.set(IS_CONTAINED, contained);
    }
    
    public float getLevitationRadius() {
        return LEVITATION_RADIUS;
    }
    
    public float getAuraRadius() {
        return AURA_RADIUS;
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("Rotation")) {
            this.entityData.set(ROTATION, compound.getFloat("Rotation"));
        }
        if (compound.contains("PulseIntensity")) {
            this.entityData.set(PULSE_INTENSITY, compound.getFloat("PulseIntensity"));
        }
        if (compound.contains("IsContained")) {
            this.entityData.set(IS_CONTAINED, compound.getBoolean("IsContained"));
        }
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("Rotation", this.entityData.get(ROTATION));
        compound.putFloat("PulseIntensity", this.entityData.get(PULSE_INTENSITY));
        compound.putBoolean("IsContained", this.entityData.get(IS_CONTAINED));
    }
    
    @Override
    public boolean isPickable() {
        return true; // Allow the entity to be interacted with
    }
    
    @Override
    public boolean isPushable() {
        return false;
    }
}
