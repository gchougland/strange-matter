package com.hexvane.strangematter.entity;

import com.hexvane.strangematter.Config;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.research.ScannableObject;
import com.hexvane.strangematter.research.ScannableObjectRegistry;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
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
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(BaseAnomalyEntity.class, EntityDataSerializers.FLOAT);
    
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
    protected boolean terrainModificationEnabled = true;
    
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
            spawnParticles();
            
            // Modify terrain once when first spawned (if enabled)
            if (!terrainModified && terrainModificationEnabled && tickCount > 20) { // Wait a bit for proper positioning
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
     * Get the corresponding shard ore block for this anomaly type
     */
    protected abstract RegistryObject<Block> getShardOreBlock();
    
    /**
     * Check if a block can be replaced with ore
     */
    private boolean canReplaceWithOre(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || 
               state.is(Blocks.ANDESITE) || state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) ||
               state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE) ||
               state.is(Blocks.TERRACOTTA) || state.is(Blocks.WHITE_TERRACOTTA) ||
               state.is(Blocks.ORANGE_TERRACOTTA) || state.is(Blocks.MAGENTA_TERRACOTTA) ||
               state.is(Blocks.LIGHT_BLUE_TERRACOTTA) || state.is(Blocks.YELLOW_TERRACOTTA) ||
               state.is(Blocks.LIME_TERRACOTTA) || state.is(Blocks.PINK_TERRACOTTA) ||
               state.is(Blocks.GRAY_TERRACOTTA) || state.is(Blocks.LIGHT_GRAY_TERRACOTTA) ||
               state.is(Blocks.CYAN_TERRACOTTA) || state.is(Blocks.PURPLE_TERRACOTTA) ||
               state.is(Blocks.BLUE_TERRACOTTA) || state.is(Blocks.BROWN_TERRACOTTA) ||
               state.is(Blocks.GREEN_TERRACOTTA) || state.is(Blocks.RED_TERRACOTTA) ||
               state.is(Blocks.BLACK_TERRACOTTA);
    }
    
    /**
     * Set whether this anomaly should modify terrain when spawned
     */
    public void setTerrainModificationEnabled(boolean enabled) {
        this.terrainModificationEnabled = enabled;
    }
    
    /**
     * Get whether this anomaly should modify terrain when spawned
     */
    public boolean isTerrainModificationEnabled() {
        return this.terrainModificationEnabled;
    }
    
    /**
     * Modify terrain around the anomaly - spawn anomalous grass and resonite ore
     */
    public void modifyTerrain() {
        BlockPos centerPos = this.blockPosition();
        
        // Create a patch of anomalous grass and resonite ore underneath
        for (int x = -TERRAIN_MODIFICATION_RADIUS; x <= TERRAIN_MODIFICATION_RADIUS; x++) {
            for (int z = -TERRAIN_MODIFICATION_RADIUS; z <= TERRAIN_MODIFICATION_RADIUS; z++) {
                // Only modify blocks within a circular radius
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= TERRAIN_MODIFICATION_RADIUS) {
                    BlockPos pos = centerPos.offset(x, 0, z);
                    
                    // Find the surface block for grass placement
                    BlockPos surfacePos = findSurfaceBlock(pos);
                    if (surfacePos != null) {
                        // Check if we should place anomalous grass
                        BlockState currentState = this.level().getBlockState(surfacePos);
                        BlockPos targetPos = surfacePos;
                        
                        // If there's snow or vegetation on top, check the block underneath
                        if (currentState.is(Blocks.SNOW) || currentState.is(Blocks.SNOW_BLOCK) ||
                            currentState.is(Blocks.TALL_GRASS) || currentState.is(Blocks.FERN) ||
                            currentState.is(Blocks.LARGE_FERN) || currentState.is(Blocks.DEAD_BUSH) ||
                            currentState.is(Blocks.SWEET_BERRY_BUSH) || currentState.is(Blocks.GRASS)) {
                            // Check the block underneath
                            BlockPos belowPos = surfacePos.below();
                            BlockState belowState = this.level().getBlockState(belowPos);
                            if (belowState.is(Blocks.GRASS_BLOCK) || belowState.is(Blocks.DIRT) || 
                                belowState.is(Blocks.COARSE_DIRT) || belowState.is(Blocks.PODZOL)) {
                                targetPos = belowPos;
                                currentState = belowState;
                            }
                        }
                        
                        // Place anomalous grass on suitable surface blocks (if enabled in config)
                        if (Config.enableAnomalousGrass && 
                            (currentState.is(Blocks.GRASS_BLOCK) || currentState.is(Blocks.DIRT) || 
                            currentState.is(Blocks.COARSE_DIRT) || currentState.is(Blocks.PODZOL) ||
                            currentState.is(Blocks.SAND) || currentState.is(Blocks.RED_SAND) ||
                            currentState.is(Blocks.TERRACOTTA) || currentState.is(Blocks.WHITE_TERRACOTTA) ||
                            currentState.is(Blocks.ORANGE_TERRACOTTA) || currentState.is(Blocks.MAGENTA_TERRACOTTA) ||
                            currentState.is(Blocks.LIGHT_BLUE_TERRACOTTA) || currentState.is(Blocks.YELLOW_TERRACOTTA) ||
                            currentState.is(Blocks.LIME_TERRACOTTA) || currentState.is(Blocks.PINK_TERRACOTTA) ||
                            currentState.is(Blocks.GRAY_TERRACOTTA) || currentState.is(Blocks.LIGHT_GRAY_TERRACOTTA) ||
                            currentState.is(Blocks.CYAN_TERRACOTTA) || currentState.is(Blocks.PURPLE_TERRACOTTA) ||
                            currentState.is(Blocks.BLUE_TERRACOTTA) || currentState.is(Blocks.BROWN_TERRACOTTA) ||
                            currentState.is(Blocks.GREEN_TERRACOTTA) || currentState.is(Blocks.RED_TERRACOTTA) ||
                            currentState.is(Blocks.BLACK_TERRACOTTA) || currentState.is(Blocks.SNOW) ||
                            currentState.is(Blocks.SNOW_BLOCK))) {
                            this.level().setBlock(targetPos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 3);
                            modifiedBlocks.add(targetPos);
                        }
                    }
                    
                    // Place ore underground regardless of grass placement
                    // Use surface position as reference for ore placement
                    if (surfacePos != null) {
                        int surfaceY = surfacePos.getY();
                        int oreY = surfaceY - (1 + this.level().getRandom().nextInt(5)); // 1-5 blocks below surface
                        
                        // Place resonite ore (configurable chance)
                        if (this.level().getRandom().nextFloat() < Config.resoniteOreSpawnChanceNearAnomaly) {
                            BlockPos orePos = new BlockPos(pos.getX(), oreY, pos.getZ());
                            BlockState oreState = this.level().getBlockState(orePos);
                            if (canReplaceWithOre(oreState)) {
                                this.level().setBlock(orePos, StrangeMatterMod.RESONITE_ORE_BLOCK.get().defaultBlockState(), 3);
                                modifiedBlocks.add(orePos);
                            }
                        }
                        
                        // Place corresponding shard ore (configurable chance)
                        if (this.level().getRandom().nextFloat() < Config.shardOreSpawnChanceNearAnomaly) {
                            int shardOreY = surfaceY - (1 + this.level().getRandom().nextInt(5)); // 1-5 blocks below surface
                            BlockPos shardOrePos = new BlockPos(pos.getX(), shardOreY, pos.getZ());
                            BlockState shardOreState = this.level().getBlockState(shardOrePos);
                            if (canReplaceWithOre(shardOreState)) {
                                this.level().setBlock(shardOrePos, getShardOreBlock().get().defaultBlockState(), 3);
                                modifiedBlocks.add(shardOrePos);
                            }
                        }
                    } else {
                        // Fallback: use anomaly position - 5 blocks if no surface found
                        int anomalyY = centerPos.getY() - 5;
                        int oreY = anomalyY - (1 + this.level().getRandom().nextInt(5)); // 1-5 blocks below anomaly
                        
                        // Place resonite ore (configurable chance)
                        if (this.level().getRandom().nextFloat() < Config.resoniteOreSpawnChanceNearAnomaly) {
                            BlockPos orePos = new BlockPos(pos.getX(), oreY, pos.getZ());
                            BlockState oreState = this.level().getBlockState(orePos);
                            if (canReplaceWithOre(oreState)) {
                                this.level().setBlock(orePos, StrangeMatterMod.RESONITE_ORE_BLOCK.get().defaultBlockState(), 3);
                                modifiedBlocks.add(orePos);
                            }
                        }
                        
                        // Place corresponding shard ore (configurable chance)
                        if (this.level().getRandom().nextFloat() < Config.shardOreSpawnChanceNearAnomaly) {
                            int shardOreY = anomalyY - (1 + this.level().getRandom().nextInt(5)); // 1-5 blocks below anomaly
                            BlockPos shardOrePos = new BlockPos(pos.getX(), shardOreY, pos.getZ());
                            BlockState shardOreState = this.level().getBlockState(shardOrePos);
                            if (canReplaceWithOre(shardOreState)) {
                                this.level().setBlock(shardOrePos, getShardOreBlock().get().defaultBlockState(), 3);
                                modifiedBlocks.add(shardOrePos);
                            }
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
            
            // Look for ground-level blocks (not leaves, logs, etc.)
            if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || 
                state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL) ||
                state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) ||
                state.is(Blocks.STONE) || state.is(Blocks.SANDSTONE) ||
                state.is(Blocks.RED_SANDSTONE) || state.is(Blocks.TERRACOTTA) ||
                state.is(Blocks.WHITE_TERRACOTTA) || state.is(Blocks.ORANGE_TERRACOTTA) ||
                state.is(Blocks.MAGENTA_TERRACOTTA) || state.is(Blocks.LIGHT_BLUE_TERRACOTTA) ||
                state.is(Blocks.YELLOW_TERRACOTTA) || state.is(Blocks.LIME_TERRACOTTA) ||
                state.is(Blocks.PINK_TERRACOTTA) || state.is(Blocks.GRAY_TERRACOTTA) ||
                state.is(Blocks.LIGHT_GRAY_TERRACOTTA) || state.is(Blocks.CYAN_TERRACOTTA) ||
                state.is(Blocks.PURPLE_TERRACOTTA) || state.is(Blocks.BLUE_TERRACOTTA) ||
                state.is(Blocks.BROWN_TERRACOTTA) || state.is(Blocks.GREEN_TERRACOTTA) ||
                state.is(Blocks.RED_TERRACOTTA) || state.is(Blocks.BLACK_TERRACOTTA) ||
                state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) {
                return checkPos;
            }
        }
        
        return null;
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
        if (compound.contains("TerrainModified")) {
            this.terrainModified = compound.getBoolean("TerrainModified");
        }
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("Rotation", this.entityData.get(ROTATION));
        compound.putFloat("PulseIntensity", this.entityData.get(PULSE_INTENSITY));
        compound.putBoolean("IsContained", this.entityData.get(IS_CONTAINED));
        compound.putFloat("Scale", this.entityData.get(SCALE));
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
