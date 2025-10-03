package com.hexvane.strangematter.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import com.hexvane.strangematter.world.ShadowLightProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class EchoingShadowEntity extends BaseAnomalyEntity {
    
    // Constants for the echoing shadow anomaly
    private static final float SHADOW_RADIUS = 12.0f;
    private static final float LIGHT_ABSORPTION_RADIUS = 8.0f;
    private static final int LIGHT_LEVEL_REDUCTION = 8; // Reduce light level by this amount
    private static final int MOB_SPAWN_BOOST_TICKS = 20; // Boost mob spawning every 20 ticks
    private static final int MAX_SPAWNED_MOBS = 6;
    
    // Track mobs spawned by this anomaly
    private final Set<java.util.UUID> spawnedMobs = new HashSet<>();
    
    // Track affected light positions for custom light modification
    private Set<BlockPos> affectedLightPositions = new HashSet<>();
    private int mobSpawnTimer = 0;
    
    public EchoingShadowEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        // Register with shadow light provider when added to world
        if (!this.level().isClientSide && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            System.out.println("EchoingShadowEntity: Registering with ShadowLightProvider at " + this.blockPosition());
            ShadowLightProvider.getInstance(serverLevel).registerShadowAnomaly(this);
        }
    }
    
    @Override
    protected void applyAnomalyEffects() {
        if (this.isContained()) {
            return; // Don't apply effects if contained
        }
        
        // Apply light absorption effect
        applyLightAbsorption();
        
        // Boost mob spawning in the shadow radius
        if (mobSpawnTimer <= 0) {
            boostMobSpawning();
            mobSpawnTimer = MOB_SPAWN_BOOST_TICKS;
        } else {
            mobSpawnTimer--;
        }
    }
    
    private void applyLightAbsorption() {
        if (this.level().isClientSide) {
            return; // Only run on server side
        }
        
        BlockPos centerPos = this.blockPosition();
        int radius = (int) LIGHT_ABSORPTION_RADIUS;
        
        // Clear previously affected positions
        affectedLightPositions.clear();
        
        // Create a sphere of reduced light around the anomaly
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    
                    if (distance <= LIGHT_ABSORPTION_RADIUS) {
                        BlockPos pos = centerPos.offset(x, y, z);
                        
                        // Calculate shadow intensity based on distance (stronger at center)
                        double shadowFactor = 1.0 - (distance / LIGHT_ABSORPTION_RADIUS);
                        int lightReduction = (int) (LIGHT_LEVEL_REDUCTION * shadowFactor);
                        
                        if (lightReduction > 0) {
                            // Mark this position as affected by shadow
                            affectedLightPositions.add(pos);
                            
                            // Create shadow particles to indicate the light absorption
                            if (this.level().getRandom().nextFloat() < 0.01f) { // Very low frequency
                                spawnShadowParticle(pos);
                            }
                        }
                    }
                }
            }
        }
        
        // Force a lighting update for the entire area
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Update lighting for multiple sections around the anomaly
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos sectionPos = centerPos.offset(x * 16, 0, z * 16);
                    serverLevel.getChunkSource().getLightEngine().updateSectionStatus(
                        net.minecraft.core.SectionPos.of(sectionPos), true
                    );
                }
            }
            
            // Try to directly modify light levels using the light engine
            net.minecraft.world.level.lighting.LevelLightEngine lightEngine = serverLevel.getChunkSource().getLightEngine();
            
            // Force lighting recalculation for affected positions
            for (BlockPos pos : affectedLightPositions) {
                // Mark the position for lighting update
                lightEngine.updateSectionStatus(net.minecraft.core.SectionPos.of(pos), true);
                
                // Try to set light level directly (this might not work in all cases)
                try {
                    // This is a more aggressive approach - force lighting recalculation
                    serverLevel.getChunkSource().getLightEngine().checkBlock(pos);
                } catch (Exception e) {
                    // Ignore exceptions - this is experimental
                }
            }
        }
        
        // Try a different approach - use custom light source
        tryCustomLightModification();
    }
    
    private void tryCustomLightModification() {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Try a more direct approach - modify light levels in the world
            BlockPos centerPos = this.blockPosition();
            
            // Test with a few positions around the anomaly
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos testPos = centerPos.offset(x, 0, z);
                    
                    // Check if this position is within the shadow radius
                    if (isInShadowRadius(testPos)) {
                        // Get current light levels
                        int currentBlockLight = serverLevel.getBrightness(LightLayer.BLOCK, testPos);
                        int currentSkyLight = serverLevel.getBrightness(LightLayer.SKY, testPos);
                        
                        // Calculate distance-based reduction - make it DRASTIC
                        double distance = this.position().distanceTo(new Vec3(testPos.getX() + 0.5, testPos.getY() + 0.5, testPos.getZ() + 0.5));
                        double reductionFactor = 1.0 - (distance / LIGHT_ABSORPTION_RADIUS);
                        
                        // Make the reduction much more dramatic - reduce to near 0
                        int newBlockLight = (int) (currentBlockLight * (1.0 - reductionFactor * 0.9)); // Reduce by 90% at center
                        int newSkyLight = (int) (currentSkyLight * (1.0 - reductionFactor * 0.8)); // Reduce by 80% at center
                        
                        // Ensure minimum values
                        newBlockLight = Math.max(0, newBlockLight);
                        newSkyLight = Math.max(0, newSkyLight);
                    }
                }
            }
        }
    }
    
    private void spawnShadowParticle(BlockPos pos) {
        // Spawn shadow particles to indicate light absorption
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Create a shadow particle effect
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.SMOKE,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                1, // count
                0.1, 0.1, 0.1, // spread
                0.0 // speed
            );
        }
    }
    
    private void boostMobSpawning() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        
        AABB shadowBox = this.getBoundingBox().inflate(SHADOW_RADIUS);
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
        if (spawnedMobs.size() < MAX_SPAWNED_MOBS && this.level().getRandom().nextFloat() < 0.25f) { // 25% chance per tick, max 6 spawned mobs
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
            int x = centerPos.getX() + this.level().getRandom().nextInt((int)(SHADOW_RADIUS * 2)) - (int)SHADOW_RADIUS;
            int z = centerPos.getZ() + this.level().getRandom().nextInt((int)(SHADOW_RADIUS * 2)) - (int)SHADOW_RADIUS;
            
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
    protected void updateClientEffects() {
        // Client-side effects for the echoing shadow
        // This could include visual effects, screen darkening, etc.
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
        Player nearestPlayer = this.level().getNearestPlayer(this, ECHOING_SHADOW_SOUND_DISTANCE);
        
        if (nearestPlayer == null) {
            // No player nearby, stop sound
            if (isSoundActive) {
                com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().stopAmbientSound(getAnomalySound());
                isSoundActive = false;
            }
            return;
        }
        
        double distance = this.distanceTo(nearestPlayer);
        
        if (distance > ECHOING_SHADOW_SOUND_DISTANCE) {
            // Player too far, stop sound
            if (isSoundActive) {
                com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().stopAmbientSound(getAnomalySound());
                isSoundActive = false;
            }
            return;
        }
        
        // Calculate volume based on distance (increased base volume)
        float volume = calculateEchoingShadowSoundVolume(distance);
        
        // Player is in range, manage continuous sound
        if (!isSoundActive) {
            // Start the sound
            com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().playAmbientSound(
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
                com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().updateSoundVolume(getAnomalySound(), volume);
                lastCalculatedVolume = volume;
            }
            
            // Update position
            com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().updateSoundPosition(
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
    protected int getResearchAmount() {
        return 15; // Shadow research points
    }
    
    @Override
    protected String getAnomalyName() {
        return "Echoing Shadow";
    }
    
    @Override
    protected RegistryObject<Block> getShardOreBlock() {
        return StrangeMatterMod.SHADE_SHARD_ORE_BLOCK;
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
    
    @Override
    public void remove(@org.jetbrains.annotations.NotNull RemovalReason reason) {
        // Unregister from shadow light provider when removed
        if (!this.level().isClientSide && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            ShadowLightProvider.getInstance(serverLevel).unregisterShadowAnomaly(this);
        }
        affectedLightPositions.clear();
        super.remove(reason);
    }
    
    /**
     * Get the current light level at a position, accounting for shadow absorption
     */
    public int getEffectiveLightLevel(BlockPos pos) {
        double distance = this.position().distanceTo(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        
        if (distance <= LIGHT_ABSORPTION_RADIUS) {
            double reductionFactor = 1.0 - (distance / LIGHT_ABSORPTION_RADIUS);
            int lightReduction = (int) (LIGHT_LEVEL_REDUCTION * reductionFactor);
            
            // Get both block and sky light levels
            int blockLight = this.level().getBrightness(LightLayer.BLOCK, pos);
            int skyLight = this.level().getBrightness(LightLayer.SKY, pos);
            
            // Apply shadow reduction to both light types
            int effectiveBlockLight = Math.max(0, blockLight - lightReduction);
            int effectiveSkyLight = Math.max(0, skyLight - lightReduction);
            
            // Return the maximum of the two (Minecraft uses the higher value)
            return Math.max(effectiveBlockLight, effectiveSkyLight);
        }
        
        return this.level().getBrightness(LightLayer.BLOCK, pos);
    }
    
    /**
     * Check if a position is in complete darkness due to shadow absorption
     */
    public boolean isInCompleteDarkness(BlockPos pos) {
        return getEffectiveLightLevel(pos) <= 0;
    }
    
    /**
     * Check if a position is within the shadow radius
     */
    public boolean isInShadowRadius(BlockPos pos) {
        double distance = this.position().distanceTo(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        return distance <= SHADOW_RADIUS;
    }
    
    /**
     * Get the light absorption radius for the custom light engine
     */
    public float getLightAbsorptionRadius() {
        return LIGHT_ABSORPTION_RADIUS;
    }
    
    /**
     * Get the light level reduction amount for the custom light engine
     */
    public int getLightLevelReduction() {
        return LIGHT_LEVEL_REDUCTION;
    }
}
