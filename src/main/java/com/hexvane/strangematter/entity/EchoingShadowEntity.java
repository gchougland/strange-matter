package com.hexvane.strangematter.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class EchoingShadowEntity extends BaseAnomalyEntity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(EchoingShadowEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Config-driven getters for echoing shadow parameters
    private float getShadowEffectRadius() {
        return (float) com.hexvane.strangematter.Config.shadowEffectRadius;
    }
    
    @Override
    protected float getEffectRadius() {
        return getShadowEffectRadius();
    }
    
    private double getMobSpawnBoost() {
        return com.hexvane.strangematter.Config.shadowMobSpawnBoost;
    }
    
    private static final int MOB_SPAWN_BOOST_TICKS = 20; // Boost mob spawning every 20 ticks
    private static final int MAX_SPAWNED_MOBS = 6;
    
    // Track mobs spawned by this anomaly
    private final Set<java.util.UUID> spawnedMobs = new HashSet<>();
    
    private int mobSpawnTimer = 0;
    
    public EchoingShadowEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ACTIVE, true);
    }
    
    @Override
    protected void applyAnomalyEffects() {
        if (!this.isActive() || this.isContained() || !com.hexvane.strangematter.Config.enableShadowEffects) {
            return; // Don't apply effects if not active, contained, or effects disabled
        }
        
        // Boost mob spawning in the shadow radius
        if (com.hexvane.strangematter.Config.enableShadowMobSpawnBoost) {
            if (mobSpawnTimer <= 0) {
                boostMobSpawning();
                mobSpawnTimer = MOB_SPAWN_BOOST_TICKS;
            } else {
                mobSpawnTimer--;
            }
        }
    }
    
    private void boostMobSpawning() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        
        float effectRadius = getShadowEffectRadius();
        AABB shadowBox = this.getBoundingBox().inflate(effectRadius);
        List<Entity> entitiesInRange = this.level().getEntities(this, shadowBox);
        
        // Count existing hostile mobs in the shadow area
        int hostileMobCount = 0;
        for (Entity entity : entitiesInRange) {
            if (entity instanceof LivingEntity livingEntity && !(entity instanceof Player)) {
                if (livingEntity.getType().getCategory().isFriendly() == false) {
                    BlockPos mobPos = livingEntity.blockPosition();
                    if (isInShadowRadius(mobPos)) {
                        hostileMobCount++;
                        
                        // Protect mobs from sunlight damage if they're in the shadow
                        protectMobFromSunlight(livingEntity);
                    }
                }
            }
        }
        
        // Clean up dead mobs from tracking
        cleanupDeadMobs();
        
        // Spawn mobs like a spawner - ignore light levels and time of day
        // Apply mob spawn boost from config
        double spawnChance = 0.25 * (getMobSpawnBoost() / 2.0); // Base 25% * (boost / 2.0) - at 2.0 boost = 25%
        if (spawnedMobs.size() < MAX_SPAWNED_MOBS && this.level().getRandom().nextFloat() < spawnChance) {
            attemptSpawnerLikeMobSpawn(serverLevel);
        }
    }
    
    private void cleanupDeadMobs() {
        // Remove dead or removed mobs from tracking
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            spawnedMobs.removeIf(uuid -> {
                Entity entity = serverLevel.getEntity(uuid);
                return entity == null || !entity.isAlive() || entity.isRemoved();
            });
        }
    }
    
    private void protectMobFromSunlight(LivingEntity mob) {
        if (mob == null || !this.level().isDay()) {
            return;
        }
        
        // Check if the mob is in the shadow radius
        BlockPos mobPos = mob.blockPosition();
        if (!isInShadowRadius(mobPos)) {
            return;
        }
        
        // Protect specific mob types from sunlight
        if (mob instanceof net.minecraft.world.entity.monster.Zombie || 
            mob instanceof net.minecraft.world.entity.monster.Skeleton ||
            mob instanceof net.minecraft.world.entity.monster.Stray ||
            mob instanceof net.minecraft.world.entity.monster.Husk ||
            mob instanceof net.minecraft.world.entity.monster.ZombieVillager ||
            mob instanceof net.minecraft.world.entity.monster.Drowned) {
            
            // Add a temporary effect to prevent burning
            mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 
                40, // 2 seconds
                0, 
                false, 
                false, 
                false
            ));
            
            // Also extinguish the mob if it's on fire
            if (mob.isOnFire()) {
                mob.clearFire();
            }
        }
    }
    
    private void attemptSpawnerLikeMobSpawn(net.minecraft.server.level.ServerLevel serverLevel) {
        // Try to spawn common hostile mobs in the shadow area
        BlockPos centerPos = this.blockPosition();
        
        // Try multiple spawn attempts
        for (int attempt = 0; attempt < 5; attempt++) {
            // Pick a random position within the shadow radius
            float shadowRadius = getShadowEffectRadius();
            int x = centerPos.getX() + this.level().getRandom().nextInt((int)(shadowRadius * 2)) - (int)shadowRadius;
            int z = centerPos.getZ() + this.level().getRandom().nextInt((int)(shadowRadius * 2)) - (int)shadowRadius;
            
            // Find a suitable Y position
            BlockPos spawnPos = new BlockPos(x, centerPos.getY(), z);
            BlockPos groundPos = serverLevel.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, spawnPos);
            
            // Check if this position is within the shadow radius
            if (isInShadowRadius(groundPos)) {
                // Try to spawn a random hostile mob
                EntityType<?>[] hostileMobs = {
                    EntityType.ZOMBIE,
                    EntityType.SKELETON,
                    EntityType.SPIDER,
                    EntityType.CREEPER,
                    EntityType.ENDERMAN
                };
                
                EntityType<?> mobType = hostileMobs[this.level().getRandom().nextInt(hostileMobs.length)];
                
                // Check if the spawn position is valid
                if (serverLevel.getBlockState(groundPos).isAir() && 
                    serverLevel.getBlockState(groundPos.above()).isAir()) {
                    
                    // Create and spawn the mob
                    Entity newMob = mobType.create(serverLevel);
                    if (newMob != null) {
                        newMob.moveTo(groundPos.getX() + 0.5, groundPos.getY(), groundPos.getZ() + 0.5, 
                                     this.level().getRandom().nextFloat() * 360.0f, 0.0f);
                        
                        if (serverLevel.addFreshEntity(newMob)) {
                            // Track the spawned mob
                            spawnedMobs.add(newMob.getUUID());
                            // Mob spawned successfully, break out of attempts
                            break;
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void updateClientEffects() {
        // No client-side effects for Echoing Shadow
    }
    
    @Override
    protected void spawnParticles() {
        if (this.level().isClientSide) {
            return;
        }
        
        // Spawn shadow particles around the anomaly (reduced from 3 to 1)
        for (int i = 0; i < 1; i++) {
            double offsetX = (this.level().getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = (this.level().getRandom().nextDouble() - 0.5) * 2.0;
            double offsetZ = (this.level().getRandom().nextDouble() - 0.5) * 2.0;
            
            // Spawn black particles (using smoke particles as shadow effect)
            this.level().addParticle(ParticleTypes.SMOKE, 
                this.getX() + offsetX, 
                this.getY() + offsetY, 
                this.getZ() + offsetZ, 
                0.0, 0.0, 0.0);
        }
        
        // Spawn occasional larger shadow bursts (reduced frequency and count)
        if (this.level().getRandom().nextFloat() < 0.05f) { // Reduced from 0.1f to 0.05f
            for (int i = 0; i < 4; i++) { // Reduced from 8 to 4
                double angle = (i / 4.0) * Math.PI * 2.0; // Fixed to match the reduced count
                double radius = 1.5 + this.level().getRandom().nextDouble() * 0.5;
                
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, 
                    this.getX() + offsetX, 
                    this.getY() + 0.5, 
                    this.getZ() + offsetZ, 
                    0.0, 0.1, 0.0);
            }
        }
    }
    
    @Override
    public ResourceLocation getAnomalySound() {
        return com.hexvane.strangematter.sound.StrangeMatterSounds.ECHOING_SHADOW_LOOP.get().getLocation();
    }
    
    // Override sound distance and volume for better audio experience
    private static final float ECHOING_SHADOW_SOUND_DISTANCE = 16.0f; // Range from 0 to 8 blocks
    private static final float ECHOING_SHADOW_MAX_VOLUME_DISTANCE = 8.0f; // Max volume up to 2 blocks
    
    @Override
    protected void updateSoundEffects() {
        // Sound effects only run on client side
        if (!this.level().isClientSide) {
            return;
        }

        com.hexvane.strangematter.platform.AnomalySoundClient soundClient =
            com.hexvane.strangematter.platform.ClientServices.anomalySound();
        if (soundClient == null) {
            return;
        }
        soundClient.initializeIfNeeded();

        Player nearestPlayer = this.level().getNearestPlayer(this, ECHOING_SHADOW_SOUND_DISTANCE);
        
        if (nearestPlayer == null) {
            // No player nearby, stop sound
            if (isSoundActive) {
                soundClient.stopAmbientSound(getAnomalySound());
                isSoundActive = false;
            }
            return;
        }
        
        double distance = this.distanceTo(nearestPlayer);
        
        if (distance > ECHOING_SHADOW_SOUND_DISTANCE) {
            // Player too far, stop sound
            if (isSoundActive) {
                soundClient.stopAmbientSound(getAnomalySound());
                isSoundActive = false;
            }
            return;
        }
        
        // Calculate volume based on distance (increased base volume)
        float volume = calculateEchoingShadowSoundVolume(distance);
        
        // Player is in range, manage continuous sound
        if (!isSoundActive) {
            // Start the sound
            soundClient.playAmbientSound(
                getAnomalySound(),
                this.position().x,
                this.position().y,
                this.position().z,
                volume,
                true // loop
            );
            isSoundActive = true;
            lastCalculatedVolume = volume;
        } else {
            // Update volume if it changed significantly
            if (Math.abs(volume - lastCalculatedVolume) > 0.01f) {
                soundClient.updateSoundVolume(getAnomalySound(), volume);
                lastCalculatedVolume = volume;
            }
            
            // Update position
            soundClient.updateSoundPosition(
                getAnomalySound(),
                this.position().x,
                this.position().y,
                this.position().z
            );
        }
    }
    
    private float calculateEchoingShadowSoundVolume(double distance) {
        if (distance <= ECHOING_SHADOW_MAX_VOLUME_DISTANCE) {
            // Within 2 blocks: maximum volume (1.0)
            return 1.0f;
        } else if (distance <= ECHOING_SHADOW_SOUND_DISTANCE) {
            // Between 2 and 8 blocks: linear decrease from 1.0 to 0.0
            float fadeRange = ECHOING_SHADOW_SOUND_DISTANCE - ECHOING_SHADOW_MAX_VOLUME_DISTANCE; // 6 blocks
            float fadeDistance = (float)(distance - ECHOING_SHADOW_MAX_VOLUME_DISTANCE); // Distance into fade zone
            float volumeRatio = 1.0f - (fadeDistance / fadeRange);
            return Math.max(0.0f, volumeRatio); // Clamp to 0.0 minimum
        } else {
            // Beyond 8 blocks: silent
            return 0.0f;
        }
    }
    
    @Override
    protected ResearchType getResearchType() {
        return ResearchType.SHADOW;
    }
    
    @Override
    protected String getAnomalyName() {
        return "Echoing Shadow";
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("MobSpawnTimer")) {
            this.mobSpawnTimer = compound.getInt("MobSpawnTimer");
        }
        
        // Load spawned mobs tracking
        spawnedMobs.clear();
        if (compound.contains("SpawnedMobs", 9)) { // 9 = TAG_LIST
            net.minecraft.nbt.ListTag mobList = compound.getList("SpawnedMobs", 8); // 8 = TAG_STRING
            for (int i = 0; i < mobList.size(); i++) {
                try {
                    spawnedMobs.add(java.util.UUID.fromString(mobList.getString(i)));
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                }
            }
        }
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("MobSpawnTimer", this.mobSpawnTimer);
        
        // Save spawned mobs tracking
        net.minecraft.nbt.ListTag mobList = new net.minecraft.nbt.ListTag();
        for (java.util.UUID uuid : spawnedMobs) {
            mobList.add(net.minecraft.nbt.StringTag.valueOf(uuid.toString()));
        }
        compound.put("SpawnedMobs", mobList);
    }
    
    /**
     * Check if a position is within the shadow radius
     */
    public boolean isInShadowRadius(BlockPos pos) {
        double distance = this.position().distanceTo(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        return distance <= getShadowEffectRadius();
    }
    
    public boolean isActive() {
        return this.entityData.get(IS_ACTIVE);
    }
    
    public void setActive(boolean active) {
        this.entityData.set(IS_ACTIVE, active);
    }
}
