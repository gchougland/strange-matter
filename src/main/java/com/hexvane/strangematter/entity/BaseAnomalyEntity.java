package com.hexvane.strangematter.entity;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Base class for all anomaly entities in the Strange Matter mod.
 * Provides common functionality like resonator detection and research scanning.
 */
public abstract class BaseAnomalyEntity extends Entity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(BaseAnomalyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PULSE_INTENSITY = SynchedEntityData.defineId(BaseAnomalyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CONTAINED = SynchedEntityData.defineId(BaseAnomalyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(BaseAnomalyEntity.class, EntityDataSerializers.FLOAT);
    
    // Constants for anomaly behavior
    protected static final float ROTATION_SPEED = 0.5f;
    protected static final int PARTICLE_SPAWN_RATE = 20; // particles per second
    
    // Animation timers
    protected int tickCount = 0;
    protected float lastPulseTime = 0;
    
    // Sound system
    protected static final float MAX_SOUND_DISTANCE = 10.0f;
    protected boolean isSoundActive = false;
    protected float lastCalculatedVolume = 0.0f;
    
    // Research scanning tracking
    private boolean spawnedFromCapsule = false;
    
    public BaseAnomalyEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }
    
    @Override
    protected void defineSynchedData() {
        this.entityData.define(ROTATION, 0.0f);
        this.entityData.define(PULSE_INTENSITY, 0.0f);
        this.entityData.define(IS_CONTAINED, false);
        this.entityData.define(SCALE, 1.0f);
    }
    
    @Override
    public void tick() {
        super.tick();
        tickCount++;
        
        if (!this.level().isClientSide) {
            // Server-side logic
            updateRotation();
            updatePulseAnimation();
            applyAnomalyEffects();
            checkForFirstContact();
            spawnParticles();
        } else {
            // Client-side logic
            updateClientEffects();
            updateSoundEffects();
        }
    }
    
    protected void updateRotation() {
        float currentRotation = this.entityData.get(ROTATION);
        float newRotation = currentRotation + ROTATION_SPEED;
        if (newRotation >= 360.0f) {
            newRotation -= 360.0f;
        }
        this.entityData.set(ROTATION, newRotation);
    }
    
    protected void updatePulseAnimation() {
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
    
    /**
     * Override this method to implement specific anomaly effects
     */
    protected abstract void applyAnomalyEffects();
    
    /**
     * Override this method to implement specific particle effects
     */
    protected abstract void spawnParticles();
    
    /**
     * Override this method to implement specific client-side effects
     */
    protected abstract void updateClientEffects();
    
    /**
     * Check for players experiencing anomaly effects for the first time and trigger advancement
     */
    private void checkForFirstContact() {
        if (!this.isActive() || this.isContained()) {
            return; // Don't check if anomaly is not active or contained
        }
        
        // Only check every 20 ticks (1 second) to avoid performance issues
        if (tickCount % 20 != 0) return;
        
        // Find all players within effect range of this anomaly
        float effectRadius = getEffectRadius();
        AABB effectBox = this.getBoundingBox().inflate(effectRadius);
        List<Entity> entitiesInRange = this.level().getEntities(this, effectBox);
        
        for (Entity entity : entitiesInRange) {
            if (entity instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                double distance = this.distanceTo(entity);
                if (distance <= effectRadius) {
                    // Player is within effect range, trigger the advancement
                    StrangeMatterMod.ANOMALY_EFFECT_TRIGGER.trigger(serverPlayer);
                }
            }
        }
    }
    
    /**
     * Get the radius at which this anomaly's effects are applied
     * Override in subclasses to provide specific effect ranges
     */
    protected float getEffectRadius() {
        return 5.0f; // Default effect radius
    }
    
    
    /**
     * Override this method to return the sound resource for this anomaly
     */
    public abstract ResourceLocation getAnomalySound();
    
    /**
     * Override this method to return the research type this anomaly provides
     */
    protected abstract ResearchType getResearchType();
    
    /**
     * Get the research amount this anomaly provides by looking it up from the ScannableObjectRegistry
     */
    protected int getResearchAmount() {
        return com.hexvane.strangematter.research.ScannableObjectRegistry.getScannableForEntity(this)
            .map(com.hexvane.strangematter.research.ScannableObject::getResearchAmount)
            .orElse(0);
    }
    
    /**
     * Override this method to return the anomaly name for display
     */
    protected abstract String getAnomalyName();
    
    /**
     * Set whether this anomaly was spawned from a containment capsule
     */
    public void setSpawnedFromCapsule(boolean spawnedFromCapsule) {
        this.spawnedFromCapsule = spawnedFromCapsule;
    }
    
    /**
     * Get whether this anomaly was spawned from a containment capsule
     */
    public boolean isSpawnedFromCapsule() {
        return this.spawnedFromCapsule;
    }
    
    /**
     * Check if this anomaly is active (has effects enabled)
     * Override in subclasses to provide specific active state logic
     */
    public boolean isActive() {
        return true; // Default to active, subclasses can override
    }
    
    
    protected void updateSoundEffects() {
        // Sound effects only run on client side
        if (!this.level().isClientSide) {
            return;
        }
        
        // Ensure CustomSoundManager is initialized
        com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().initialize();
        
        // Find the nearest player
        Player nearestPlayer = this.level().getNearestPlayer(this, MAX_SOUND_DISTANCE);
        
        if (nearestPlayer == null) {
            // No player nearby, stop sound
            if (isSoundActive) {
                com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().stopAmbientSound(getAnomalySound());
                isSoundActive = false;
            }
            return;
        }
        
        // Calculate distance to player
        double distance = this.distanceTo(nearestPlayer);
        
        if (distance > MAX_SOUND_DISTANCE) {
            // Player too far, stop sound
            if (isSoundActive) {
                com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().stopAmbientSound(getAnomalySound());
                isSoundActive = false;
            }
            return;
        }
        
        // Calculate volume based on distance
        float volume = calculateSoundVolume(distance);
        
        // Player is in range, manage continuous sound
        if (!isSoundActive) {
            // Start the sound
            com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().playAmbientSound(
                getAnomalySound(),
                this.getX(), this.getY(), this.getZ(),
                volume,
                true // Loop continuously
            );
            isSoundActive = true;
            lastCalculatedVolume = volume;
        } else {
            // Update volume if it changed significantly
            if (Math.abs(volume - lastCalculatedVolume) > 0.01f) {
                com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().updateSoundVolume(getAnomalySound(), volume);
                lastCalculatedVolume = volume;
            }
            
            // Update position
            com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().updateSoundPosition(
                getAnomalySound(),
                this.getX(), this.getY(), this.getZ()
            );
        }
    }
    
    private float calculateSoundVolume(double distance) {
        // Linear interpolation from max volume at 0 distance to min volume at max distance
        float volumeRatio = 1.0f - (float)(distance / MAX_SOUND_DISTANCE);
        volumeRatio = Math.max(0.0f, Math.min(1.0f, volumeRatio)); // Clamp between 0 and 1
        
        return 0.5f + (volumeRatio * 0.5f); // Range from 0.5 to 1.0
    }
    
    // Getters for entity data
    public float getRotation() {
        return this.entityData.get(ROTATION);
    }
    
    public float getPulseIntensity() {
        return this.entityData.get(PULSE_INTENSITY);
    }
    
    protected void setPulseIntensity(float intensity) {
        this.entityData.set(PULSE_INTENSITY, intensity);
    }
    
    public boolean isContained() {
        return this.entityData.get(IS_CONTAINED);
    }
    
    public void setContained(boolean contained) {
        this.entityData.set(IS_CONTAINED, contained);
    }
    
    public float getScale() {
        return this.entityData.get(SCALE);
    }
    
    public void setScale(float scale) {
        this.entityData.set(SCALE, Math.max(0.1f, Math.min(2.0f, scale)));
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
        if (compound.contains("Scale")) {
            this.entityData.set(SCALE, compound.getFloat("Scale"));
        }
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("Rotation", this.entityData.get(ROTATION));
        compound.putFloat("PulseIntensity", this.entityData.get(PULSE_INTENSITY));
        compound.putBoolean("IsContained", this.entityData.get(IS_CONTAINED));
        compound.putFloat("Scale", this.entityData.get(SCALE));
    }
    
    @Override
    public boolean isPickable() {
        return true; // Allow the entity to be interacted with
    }
    
    @Override
    public boolean isPushable() {
        return false;
    }
    
    /**
     * Register this anomaly as scannable for the field scanner
     * This should be called in the constructor or during entity initialization
     */
    public void registerAsScannable() {
        // This method is kept for compatibility but actual registration
        // should be done in ScannableObjectRegistry static block
    }
    
    @Override
    public void remove(Entity.RemovalReason reason) {
        // Stop the anomaly's sound before removing the entity
        if (isSoundActive) {
            // This will be handled client-side, but we need to ensure cleanup
            stopSoundEffects();
        }
        
        // Call the parent remove method
        super.remove(reason);
    }
    
    /**
     * Stop sound effects for this anomaly
     * This should be called when the anomaly is removed or contained
     */
    protected void stopSoundEffects() {
        // This method will be overridden by client-side code if needed
        // For now, just mark that sound should be stopped
        isSoundActive = false;
    }
    
}
