package com.hexvane.strangematter.entity;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Thoughtwell anomaly entity that affects player cognition and confuses nearby mobs
 * with floating glowing boulders and cyan/runic particle effects.
 */
public class ThoughtwellEntity extends BaseAnomalyEntity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(ThoughtwellEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Config-driven getters for thoughtwell parameters
    private float getThoughtwellEffectRadius() {
        return (float) com.hexvane.strangematter.Config.thoughtwellEffectRadius;
    }
    
    @Override
    protected float getEffectRadius() {
        return getThoughtwellEffectRadius();
    }
    
    
    private int getConfusionDuration() {
        return com.hexvane.strangematter.Config.thoughtwellConfusionDuration;
    }
    
    private static final int NAUSEA_COOLDOWN = 100; // 5 seconds
    private static final int CONFUSION_COOLDOWN = 200; // 10 seconds
    private static final int PARTICLE_BURST_COOLDOWN = 40; // 2 seconds
    private static final int RUNIC_PARTICLE_COOLDOWN = 60; // 3 seconds
    
    // Simple disguise tracking system - maps entity UUIDs to disguise data
    private static final java.util.Map<java.util.UUID, String> DISGUISE_MAP = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<java.util.UUID, Integer> DISGUISE_DURATION_MAP = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Tracking for cooldowns
    private int nauseaCooldown = 0;
    private int confusionCooldown = 0;
    private int particleBurstCooldown = 0;
    private int runicParticleCooldown = 0;
    
    public ThoughtwellEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        // Set a proper bounding box for the thoughtwell (similar to warp gate)
        this.setBoundingBox(new AABB(-1.0, 0, -1.0, 1.0, 2.0, 1.0));
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ACTIVE, true);
    }
    
    
    @Override
    protected void updatePulseAnimation() {
        // Create a gentle pulsing effect with cognitive energy waves
        float time = (this.tickCount % 100) / 100.0f; // 5 second cycle (100 ticks)
        float pulseIntensity = (float) (0.4 + 0.6 * Math.sin(time * 2 * Math.PI));
        this.setPulseIntensity(pulseIntensity);
    }
    
    @Override
    protected void applyAnomalyEffects() {
        if (!this.isActive() || this.isContained() || !com.hexvane.strangematter.Config.enableThoughtwellEffects) {
            return; // Don't apply effects if not active, contained, or effects disabled
        }
        
        // Update cooldowns
        if (nauseaCooldown > 0) nauseaCooldown--;
        if (confusionCooldown > 0) confusionCooldown--;
        if (particleBurstCooldown > 0) particleBurstCooldown--;
        if (runicParticleCooldown > 0) runicParticleCooldown--;
        
        // Update disguise durations
        updateDisguiseDurations();
        
        // Apply nausea effect to nearby players
        if (nauseaCooldown <= 0 && com.hexvane.strangematter.Config.enableThoughtwellNausea) {
            affectNearbyPlayers();
        }
        
        // Confuse nearby mobs
        if (confusionCooldown <= 0 && com.hexvane.strangematter.Config.enableThoughtwellMobDisguise) {
            confuseNearbyMobs();
        }
        
        // Create cyan particle bursts
        if (particleBurstCooldown <= 0) {
            createCyanBurst();
        }
        
        // Create runic particle effects
        if (runicParticleCooldown <= 0) {
            createRunicParticles();
        }
    }
    
    private void affectNearbyPlayers() {
        // Create a large bounding box centered on the entity's position for effect detection
        float effectRadius = getThoughtwellEffectRadius();
        AABB effectBox = new AABB(
            this.getX() - effectRadius, this.getY() - effectRadius, this.getZ() - effectRadius,
            this.getX() + effectRadius, this.getY() + effectRadius, this.getZ() + effectRadius
        );
        List<Entity> entitiesInRange = this.level().getEntities(this, effectBox);
        
        boolean foundPlayer = false;
        for (Entity entity : entitiesInRange) {
            if (entity instanceof Player player) {
                double distance = this.distanceTo(player);
                if (distance <= effectRadius) {
                    // Apply slight nausea effect - intensity based on distance
                    float intensity = 1.0f - (float)(distance / effectRadius);
                    int duration = 100 + (int)(intensity * 100); // 5-10 seconds
                    int amplifier = intensity > 0.7f ? 1 : 0; // Level 1 nausea if very close
                    
                    player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, duration, amplifier, false, true));
                    
                    foundPlayer = true;
                }
            }
        }
        
        // Reset cooldown if we found a player
        if (foundPlayer) {
            nauseaCooldown = NAUSEA_COOLDOWN;
        }
    }
    
    private void confuseNearbyMobs() {
        // Create a large bounding box centered on the entity's position for effect detection
        float effectRadius = getThoughtwellEffectRadius();
        AABB confusionBox = new AABB(
            this.getX() - effectRadius, this.getY() - effectRadius, this.getZ() - effectRadius,
            this.getX() + effectRadius, this.getY() + effectRadius, this.getZ() + effectRadius
        );
        List<Entity> entitiesInRange = this.level().getEntities(this, confusionBox);
        
        boolean foundMob = false;
        for (Entity entity : entitiesInRange) {
            if (entity instanceof Mob mob && !(entity instanceof Player)) {
                double distance = this.distanceTo(mob);
                if (distance <= effectRadius) {
                    confuseMob(mob);
                    foundMob = true;
                }
            }
        }
        
        // Reset cooldown if we found a mob
        if (foundMob) {
            confusionCooldown = CONFUSION_COOLDOWN;
        }
    }
    
    private void confuseMob(Mob mob) {
        // Apply confusion effect to the mob
        int confusionDuration = getConfusionDuration();
        mob.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, confusionDuration, 0, false, true));
        
        // Apply cognitive disguise effect - make mobs appear as different random mobs
        applyCognitiveDisguise(mob);
        
        // Play cognitive sound effect
        this.level().playSound(null, mob.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 0.5f, 0.8f);
        
        // Randomly change the mob's target or make it wander
        if (mob.getTarget() != null && this.level().getRandom().nextFloat() < 0.3f) {
            mob.setTarget(null);
        }
    }
    
    private void createCyanBurst() {
        // Create a burst of cyan energy particles
        double radius = getThoughtwellEffectRadius() * 0.8;
        int particleCount = 12 + this.level().getRandom().nextInt(8); // 12-19 particles
        
        for (int i = 0; i < particleCount; i++) {
            double angle = this.level().getRandom().nextDouble() * 2 * Math.PI;
            double distance = this.level().getRandom().nextDouble() * radius;
            double height = (this.level().getRandom().nextDouble() - 0.5) * 2.0;
            
            double x = this.getX() + Math.cos(angle) * distance;
            double z = this.getZ() + Math.sin(angle) * distance;
            double y = this.getY() + height;
            
            // Spawn cyan energy particles
            this.level().addParticle(
                ParticleTypes.ELECTRIC_SPARK,
                x, y, z,
                (this.level().getRandom().nextDouble() - 0.5) * 0.2,
                (this.level().getRandom().nextDouble() - 0.5) * 0.2,
                (this.level().getRandom().nextDouble() - 0.5) * 0.2
            );
        }
        
        particleBurstCooldown = PARTICLE_BURST_COOLDOWN;
    }
    
    private void createRunicParticles() {
        // Create floating runic symbols around the thoughtwell
        int runeCount = 6 + this.level().getRandom().nextInt(4); // 6-9 runes
        
        for (int i = 0; i < runeCount; i++) {
            double angle = (i * Math.PI * 2) / runeCount + (this.tickCount * 0.02);
            double radius = 2.0 + this.level().getRandom().nextDouble() * 1.5;
            double height = (this.level().getRandom().nextDouble() - 0.5) * 2.0;
            
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            double y = this.getY() + height;
            
            // Spawn runic particles (using end rod for mystical effect)
            this.level().addParticle(
                ParticleTypes.END_ROD,
                x, y, z,
                Math.cos(angle + Math.PI/2) * 0.05, 0.1, Math.sin(angle + Math.PI/2) * 0.05
            );
        }
        
        runicParticleCooldown = RUNIC_PARTICLE_COOLDOWN;
    }
    
    @Override
    protected void spawnParticles() {
        if (this.level().isClientSide) return;
        
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        
        // Spawn enchant particles in a ring around the thoughtwell
        if (tickCount % 20 == 0) {
            spawnEnchantRingParticles(serverLevel);
        }
        
        // Spawn random enchant particles in a sphere around the thoughtwell (like echoing shadow)
        if (this.level().getRandom().nextFloat() < 0.1f) { // Less frequent than echoing shadow
            spawnRandomEnchantSphereParticles(serverLevel);
        }
    }
    
    private void spawnEnchantRingParticles(net.minecraft.server.level.ServerLevel serverLevel) {
        // Spawn enchant particles in a ring around the thoughtwell (only on the edge)
        int ringCount = 12; // More particles for a fuller ring
        
        for (int i = 0; i < ringCount; i++) {
            double angle = (i * Math.PI * 2) / ringCount + (this.tickCount * 0.02);
            double radius = 2.8; // Fixed radius for ring effect
            double height = Math.sin(tickCount * 0.05 + i) * 0.6; // Slight vertical movement
            
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            double y = this.getY() + height;
            
            // Spawn enchant particles in a ring (using enchant particle type)
            serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                x, y, z,
                1,
                Math.cos(angle + Math.PI/2) * 0.01, 0.02, Math.sin(angle + Math.PI/2) * 0.01,
                0.0
            );
        }
    }
    
    private void spawnRandomEnchantSphereParticles(net.minecraft.server.level.ServerLevel serverLevel) {
        // Spawn random enchant particles in a sphere around the thoughtwell (like echoing shadow)
        for (int i = 0; i < 2; i++) { // Fewer particles than echoing shadow
            double offsetX = (this.level().getRandom().nextDouble() - 0.5) * 6.0; // Larger sphere
            double offsetY = (this.level().getRandom().nextDouble() - 0.5) * 4.0; // Taller sphere
            double offsetZ = (this.level().getRandom().nextDouble() - 0.5) * 6.0; // Larger sphere
            
            // Spawn enchant particles randomly in a sphere around the thoughtwell
            serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                this.getX() + offsetX,
                this.getY() + offsetY,
                this.getZ() + offsetZ,
                1,
                0.0, 0.0, 0.0,
                0.0
            );
        }
    }
    
    @Override
    protected void updateClientEffects() {
        // Client-side visual effects handled by the renderer
    }
    
    @Override
    public ResourceLocation getAnomalySound() {
        return StrangeMatterSounds.THOUGHTWELL_LOOP.get().getLocation();
    }
    
    @Override
    protected ResearchType getResearchType() {
        return ResearchType.COGNITION;
    }
    
    @Override
    protected String getAnomalyName() {
        return "Thoughtwell";
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.strangematter.thoughtwell");
    }
    
    // Static methods to access disguise data from client side
    public static String getDisguiseType(java.util.UUID entityUUID) {
        return DISGUISE_MAP.get(entityUUID);
    }
    
    public static int getDisguiseDuration(java.util.UUID entityUUID) {
        return DISGUISE_DURATION_MAP.getOrDefault(entityUUID, 0);
    }
    
    public static boolean hasDisguise(java.util.UUID entityUUID) {
        return DISGUISE_MAP.containsKey(entityUUID);
    }
    
    public static void removeDisguise(java.util.UUID entityUUID) {
        DISGUISE_MAP.remove(entityUUID);
        DISGUISE_DURATION_MAP.remove(entityUUID);
    }
    
    public static void setDisguise(java.util.UUID entityUUID, String disguiseType, int duration) {
        DISGUISE_MAP.put(entityUUID, disguiseType);
        DISGUISE_DURATION_MAP.put(entityUUID, duration);
    }
    
    // Update disguise durations and remove expired ones
    private void updateDisguiseDurations() {
        java.util.Iterator<java.util.Map.Entry<java.util.UUID, Integer>> iterator = DISGUISE_DURATION_MAP.entrySet().iterator();
        while (iterator.hasNext()) {
            java.util.Map.Entry<java.util.UUID, Integer> entry = iterator.next();
            int duration = entry.getValue() - 1;
            if (duration <= 0) {
                // Remove expired disguise
                java.util.UUID uuid = entry.getKey();
                DISGUISE_MAP.remove(uuid);
                iterator.remove();
                
                // Sync removal to all clients
                if (this.level() instanceof net.minecraft.server.level.ServerLevel) {
                    com.hexvane.strangematter.network.MobDisguiseSyncPacket packet = 
                        new com.hexvane.strangematter.network.MobDisguiseSyncPacket(uuid);
                    com.hexvane.strangematter.network.NetworkHandler.INSTANCE.send(
                        net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
                }
            } else {
                entry.setValue(duration);
            }
        }
    }
    
    private void applyCognitiveDisguise(Mob mob) {
        // Apply a cognitive disguise effect that makes mobs appear as different random mob types
        // Only apply if the mob doesn't already have a disguise
        if (DISGUISE_MAP.containsKey(mob.getUUID())) {
            return; // Already has a disguise
        }
        
        // Get a random disguise type
        String disguiseType = com.hexvane.strangematter.client.CognitiveDisguiseRenderer.getRandomDisguiseType();
        
        // Store the disguise information in the static maps
        int duration = 2400; // 120 seconds at 20 TPS
        DISGUISE_MAP.put(mob.getUUID(), disguiseType);
        DISGUISE_DURATION_MAP.put(mob.getUUID(), duration);
        
        // Sync to all clients
        if (this.level() instanceof net.minecraft.server.level.ServerLevel) {
            com.hexvane.strangematter.network.MobDisguiseSyncPacket packet = 
                new com.hexvane.strangematter.network.MobDisguiseSyncPacket(mob.getUUID(), disguiseType, duration);
            com.hexvane.strangematter.network.NetworkHandler.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
        }
        
        // Add some visual particles to indicate the cognitive effect
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Spawn some cognitive distortion particles around the mob
            for (int i = 0; i < 8; i++) {
                double offsetX = (this.level().getRandom().nextDouble() - 0.5) * 3.0;
                double offsetY = this.level().getRandom().nextDouble() * 2.0;
                double offsetZ = (this.level().getRandom().nextDouble() - 0.5) * 3.0;
                
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    mob.getX() + offsetX,
                    mob.getY() + offsetY,
                    mob.getZ() + offsetZ,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
                );
            }
        }
    }
    
    public boolean isActive() {
        return this.entityData.get(IS_ACTIVE);
    }
    
    public void setActive(boolean active) {
        this.entityData.set(IS_ACTIVE, active);
    }
}
