package com.hexvane.strangematter.entity;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.research.ScannableObject;
import com.hexvane.strangematter.research.ScannableObjectRegistry;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.hexvane.strangematter.client.sound.CustomSoundManager;

import java.util.Set;
import java.util.HashSet;

/**
 * Base class for all anomaly entities in the Strange Matter mod.
 * Provides common functionality like resonator detection, research scanning,
 * and terrain modification (anomalous grass and resonite ore spawning).
 */
public abstract class BaseAnomalyEntity extends Entity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(BaseAnomalyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PULSE_INTENSITY = SynchedEntityData.defineId(BaseAnomalyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CONTAINED = SynchedEntityData.defineId(BaseAnomalyEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Constants for anomaly behavior
    protected static final float ROTATION_SPEED = 0.5f;
    protected static final int PARTICLE_SPAWN_RATE = 20; // particles per second
    protected static final int TERRAIN_MODIFICATION_RADIUS = 5; // Radius for grass/ore spawning
    
    // Animation timers
    protected int tickCount = 0;
    protected float lastPulseTime = 0;
    
    // Sound system
    protected static final float MAX_SOUND_DISTANCE = 10.0f;
    protected boolean isSoundActive = false;
    protected float lastCalculatedVolume = 0.0f;
    
    // Terrain modification tracking
    private Set<BlockPos> modifiedBlocks = new HashSet<>();
    private boolean terrainModified = false;
    
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
            spawnParticles();
            
            // Modify terrain once when first spawned
            if (!terrainModified && tickCount > 20) { // Wait a bit for proper positioning
                modifyTerrain();
                terrainModified = true;
            }
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
     * Override this method to return the sound resource for this anomaly
     */
    protected abstract ResourceLocation getAnomalySound();
    
    /**
     * Override this method to return the research type this anomaly provides
     */
    protected abstract ResearchType getResearchType();
    
    /**
     * Override this method to return the research amount this anomaly provides
     */
    protected abstract int getResearchAmount();
    
    /**
     * Override this method to return the anomaly name for display
     */
    protected abstract String getAnomalyName();
    
    /**
     * Modify terrain around the anomaly - spawn anomalous grass and resonite ore
     */
    private void modifyTerrain() {
        BlockPos centerPos = this.blockPosition();
        
        // Create a patch of anomalous grass and resonite ore underneath
        for (int x = -TERRAIN_MODIFICATION_RADIUS; x <= TERRAIN_MODIFICATION_RADIUS; x++) {
            for (int z = -TERRAIN_MODIFICATION_RADIUS; z <= TERRAIN_MODIFICATION_RADIUS; z++) {
                // Only modify blocks within a circular radius
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= TERRAIN_MODIFICATION_RADIUS) {
                    BlockPos pos = centerPos.offset(x, 0, z);
                    
                    // Find the surface block
                    BlockPos surfacePos = findSurfaceBlock(pos);
                    if (surfacePos != null) {
                        // Place anomalous grass on the surface
                        BlockState currentState = this.level().getBlockState(surfacePos);
                        if (currentState.is(Blocks.GRASS_BLOCK) || currentState.is(Blocks.DIRT)) {
                            this.level().setBlock(surfacePos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 3);
                            modifiedBlocks.add(surfacePos);
                        }
                        
                        // Place resonite ore underground (1-3 blocks below surface)
                        int oreDepth = 1 + this.level().getRandom().nextInt(3);
                        BlockPos orePos = surfacePos.below(oreDepth);
                        BlockState oreState = this.level().getBlockState(orePos);
                        if (oreState.is(Blocks.STONE) || oreState.is(Blocks.DEEPSLATE)) {
                            this.level().setBlock(orePos, StrangeMatterMod.RESONITE_ORE_BLOCK.get().defaultBlockState(), 3);
                            modifiedBlocks.add(orePos);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Find the surface block at the given x,z coordinates
     */
    private BlockPos findSurfaceBlock(BlockPos pos) {
        // Start from a reasonable height and work down
        int startY = Math.min(this.getBlockY() + 10, this.level().getMaxBuildHeight() - 1);
        
        for (int y = startY; y >= this.level().getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState state = this.level().getBlockState(checkPos);
            
            // Found a solid block that's not air
            if (state.isSolid() && !state.isAir()) {
                // Check if the block above is air (making this the surface)
                BlockPos abovePos = checkPos.above();
                BlockState aboveState = this.level().getBlockState(abovePos);
                if (aboveState.isAir()) {
                    return checkPos;
                }
            }
        }
        
        return null;
    }
    
    protected void updateSoundEffects() {
        // Find the nearest player
        Player nearestPlayer = this.level().getNearestPlayer(this, MAX_SOUND_DISTANCE);
        
        if (nearestPlayer == null) {
            // No player nearby, stop sound
            if (isSoundActive) {
                CustomSoundManager.getInstance().stopAmbientSound(getAnomalySound());
                isSoundActive = false;
            }
            return;
        }
        
        // Calculate distance to player
        double distance = this.distanceTo(nearestPlayer);
        
        if (distance > MAX_SOUND_DISTANCE) {
            // Player too far, stop sound
            if (isSoundActive) {
                CustomSoundManager.getInstance().stopAmbientSound(getAnomalySound());
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
                CustomSoundManager.getInstance().updateSoundVolume(getAnomalySound(), volume);
                lastCalculatedVolume = volume;
            }
            
            // Update position
            CustomSoundManager.getInstance().updateSoundPosition(
                getAnomalySound(),
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
        if (compound.contains("TerrainModified")) {
            this.terrainModified = compound.getBoolean("TerrainModified");
        }
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("Rotation", this.entityData.get(ROTATION));
        compound.putFloat("PulseIntensity", this.entityData.get(PULSE_INTENSITY));
        compound.putBoolean("IsContained", this.entityData.get(IS_CONTAINED));
        compound.putBoolean("TerrainModified", this.terrainModified);
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
}
