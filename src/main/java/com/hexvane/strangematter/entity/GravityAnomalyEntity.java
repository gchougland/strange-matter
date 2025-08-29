package com.hexvane.strangematter.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GravityAnomalyEntity extends Entity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(GravityAnomalyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PULSE_INTENSITY = SynchedEntityData.defineId(GravityAnomalyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CONTAINED = SynchedEntityData.defineId(GravityAnomalyEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Constants for the gravity anomaly
    private static final float LEVITATION_RADIUS = 4.0f;
    private static final float AURA_RADIUS = 2.0f;
    private static final float LEVITATION_FORCE = 0.1f;
    private static final float ROTATION_SPEED = 0.5f;
    private static final int PARTICLE_SPAWN_RATE = 20; // particles per second
    
    // Animation timers
    private int tickCount = 0;
    private float lastPulseTime = 0;
    
    // Sound system
    private static final float MAX_SOUND_DISTANCE = 10.0f; // Maximum distance to hear the sound (matches attenuation)
    private static final int SOUND_DURATION_TICKS = 120; // 6 seconds = 120 ticks
    private static final int VOLUME_UPDATE_INTERVAL = 20; // Update volume every second (20 ticks)
    
    // Sound event reference - will be set from the main mod class
    private static SoundEvent gravityAnomalyLoopSound;
    
    // Sound tracking
    private boolean isSoundActive = false;
    private int soundStartTick = 0;
    private int lastVolumeUpdateTick = 0;
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
        
        for (Entity entity : entitiesInRange) {
            if (entity instanceof Player && ((Player) entity).isCreative()) {
                continue; // Skip creative players
            }
            
            // Calculate distance from anomaly center
            double distance = this.distanceTo(entity);
            if (distance <= LEVITATION_RADIUS) {
                // Apply upward force based on distance
                double forceMultiplier = 1.0 - (distance / LEVITATION_RADIUS);
                Vec3 upwardForce = new Vec3(0, LEVITATION_FORCE * forceMultiplier, 0);
                
                // Apply the force
                entity.setDeltaMovement(entity.getDeltaMovement().add(upwardForce));
                
                // Prevent entities from falling too fast
                if (entity.getDeltaMovement().y < -0.5) {
                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.5, 1.0));
                }
            }
        }
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
    
    private void updateSoundEffects() {
        // Find the nearest player
        Player nearestPlayer = this.level().getNearestPlayer(this, MAX_SOUND_DISTANCE);
        
        if (nearestPlayer == null) {
            // No player nearby, stop sound
            if (isSoundActive) {
                isSoundActive = false;
            }
            return;
        }
        
        // Calculate distance to player
        double distance = this.distanceTo(nearestPlayer);
        
        if (distance > MAX_SOUND_DISTANCE) {
            // Player too far, stop sound
            if (isSoundActive) {
                isSoundActive = false;
            }
            return;
        }
        
        // Player is in range, manage continuous sound loop
        if (!isSoundActive) {
            // Start the sound loop
            isSoundActive = true;
            soundStartTick = tickCount;
            
            // Play the first sound immediately
            this.level().playLocalSound(
                this.getX(), this.getY(), this.getZ(),
                getAmbientSound(),
                SoundSource.AMBIENT,
                calculateSoundVolume(distance),
                1.0f, // Pitch
                false // Don't broadcast to other players
            );
        } else {
            // Continue the loop - play next sound when current one should end
            int timeSinceStart = tickCount - soundStartTick;
            int soundCycle = timeSinceStart / SOUND_DURATION_TICKS;
            int currentCycleStart = soundStartTick + (soundCycle * SOUND_DURATION_TICKS);
            
            // If we're at the start of a new cycle, play the sound
            if (tickCount == currentCycleStart) {
                this.level().playLocalSound(
                    this.getX(), this.getY(), this.getZ(),
                    getAmbientSound(),
                    SoundSource.AMBIENT,
                    calculateSoundVolume(distance),
                    1.0f, // Pitch
                    false // Don't broadcast to other players
                );
            }
        }
    }
    
    private float calculateSoundVolume(double distance) {
        // Linear interpolation from max volume at 0 distance to min volume at max distance
        float volumeRatio = 1.0f - (float)(distance / MAX_SOUND_DISTANCE);
        volumeRatio = Math.max(0.0f, Math.min(1.0f, volumeRatio)); // Clamp between 0 and 1
        
        return 0.1f + (volumeRatio * 0.5f); // Range from 0.1 to 0.6
    }
    
    // Method to get the ambient sound (not an override since Entity doesn't have this)
    public SoundEvent getAmbientSound() {
        // Return the gravity anomaly loop sound
        return gravityAnomalyLoopSound != null ? gravityAnomalyLoopSound : SoundEvents.AMBIENT_CAVE.get();
    }
    
    // Method to set the sound event from the main mod class
    public static void setGravityAnomalyLoopSound(SoundEvent sound) {
        gravityAnomalyLoopSound = sound;
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
        return false;
    }
    
    @Override
    public boolean isPushable() {
        return false;
    }
}
