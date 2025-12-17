package com.hexvane.strangematter.entity;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.ArrayList;

/**
 * Energetic Rift anomaly entity that zaps entities with electric damage
 * and strikes copper lightning rods in its radius.
 */
public class EnergeticRiftEntity extends BaseAnomalyEntity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(EnergeticRiftEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     * When a lightning rod is nearby, Energetic Rifts should not zap entities.
     * This small cooldown prevents re-scanning for rods every tick while grounded.
     */
    private static final int LIGHTNING_ROD_RECHECK_COOLDOWN_TICKS = 5;
    
    // Config-driven getters for energetic rift parameters
    private float getZapRadius() {
        return (float) com.hexvane.strangematter.Config.energeticZapRadius;
    }
    
    @Override
    protected float getEffectRadius() {
        return getZapRadius();
    }
    
    
    private float getLightningRodRadius() {
        return (float) com.hexvane.strangematter.Config.energeticLightningRadius;
    }
    
    private float getZapDamage() {
        return (float) com.hexvane.strangematter.Config.energeticZapDamage;
    }
    
    private int getZapCooldownMax() {
        return com.hexvane.strangematter.Config.energeticZapCooldown;
    }
    
    private int getLightningCooldownMax() {
        return com.hexvane.strangematter.Config.energeticLightningCooldown;
    }
    
    // Tracking for cooldowns
    private int zapCooldown = 0;
    private int lightningCooldown = 0;
    
    // Tracking for targeting sparks (client-side rendering)
    private final List<TargetingSpark> targetingSparks = new ArrayList<>();
    
    public EnergeticRiftEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ACTIVE, true);
    }
    
    
    @Override
    protected void updatePulseAnimation() {
        // Override pulse with breathing animation
        // Create smooth breathing effect using sine wave
        float time = (this.tickCount % 120) / 120.0f; // 6 second cycle (120 ticks)
        float breathingIntensity = (float) (0.5 + 0.5 * Math.sin(time * 2 * Math.PI));
        this.setPulseIntensity(breathingIntensity);
    }
    
    @Override
    protected void applyAnomalyEffects() {
        if (!this.isActive() || this.isContained() || !com.hexvane.strangematter.Config.enableEnergeticEffects) {
            return; // Don't apply effects if not active, contained, or effects disabled
        }
        
        // Update cooldowns
        if (zapCooldown > 0) zapCooldown--;
        if (lightningCooldown > 0) lightningCooldown--;
        
        // Zap entities in range
        if (zapCooldown <= 0 && com.hexvane.strangematter.Config.enableEnergeticZap) {
            // If a lightning rod is nearby, the rift is effectively grounded and should not zap entities.
            // This suppression is independent of enableEnergeticLightning (rod presence alone protects entities).
            if (hasLightningRodNearby()) {
                zapCooldown = LIGHTNING_ROD_RECHECK_COOLDOWN_TICKS;
            } else {
                zapEntitiesInRange();
            }
        }
        
        // Strike lightning rods in range
        if (lightningCooldown <= 0 && com.hexvane.strangematter.Config.enableEnergeticLightning) {
            strikeLightningRods();
        }
        
        // Update targeting sparks (remove expired ones)
        targetingSparks.removeIf(spark -> spark.age++ > spark.maxAge);
    }
    
    private void zapEntitiesInRange() {
        float zapRadius = getZapRadius();
        AABB zapBox = this.getBoundingBox().inflate(zapRadius);
        List<Entity> entitiesInRange = this.level().getEntities(this, zapBox);
        
        boolean foundTarget = false;
        for (Entity entity : entitiesInRange) {
            if (entity instanceof LivingEntity && !(entity instanceof Player && ((Player) entity).isCreative())) {
                double distance = this.distanceTo(entity);
                if (distance <= zapRadius) {
                    zapEntity((LivingEntity) entity);
                    foundTarget = true;
                }
            }
        }
        
        // Reset cooldown if we found a target
        if (foundTarget) {
            zapCooldown = getZapCooldownMax();
        }
    }
    
    private void zapEntity(LivingEntity entity) {
        // Check if entity is a player wearing tinfoil hat
        boolean isProtected = false;
        if (entity instanceof Player player) {
            if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() == StrangeMatterMod.TINFOIL_HAT.get()) {
                isProtected = true;
            }
        }
        
        // Deal electric damage (unless protected by tinfoil hat)
        if (!isProtected) {
            DamageSource electricDamage = this.damageSources().lightningBolt();
            entity.hurt(electricDamage, getZapDamage());
        }
        
        // Create targeting spark for visual effect
        createTargetingSpark(entity);
        
        // Play zap sound
        this.level().playSound(null, entity.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.AMBIENT, 0.3f, 1.5f);
        
        // Spawn electric particles around the entity
        spawnElectricParticles(entity.getX(), entity.getY() + 1.0, entity.getZ(), 8);
    }
    
    private void strikeLightningRods() {
        List<BlockPos> lightningRods = new java.util.ArrayList<>();
        
        // Find all lightning rods in range
        float lightningRadius = getLightningRodRadius();
        BlockPos center = this.blockPosition();
        for (int x = (int) -lightningRadius; x <= lightningRadius; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = (int) -lightningRadius; z <= lightningRadius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = this.level().getBlockState(pos);
                    
                    if (state.is(Blocks.LIGHTNING_ROD)) {
                        double distance = Math.sqrt(x * x + z * z);
                        if (distance <= lightningRadius) {
                            lightningRods.add(pos);
                        }
                    }
                }
            }
        }
        
        // Strike a random lightning rod
        if (!lightningRods.isEmpty()) {
            BlockPos targetRod = lightningRods.get(this.level().getRandom().nextInt(lightningRods.size()));
            strikeLightningRod(targetRod);
            lightningCooldown = getLightningCooldownMax();
        }
    }

    /**
     * Checks if any lightning rod is within the energetic lightning radius.
     * Uses the same search volume as {@link #strikeLightningRods()}, but exits early.
     */
    private boolean hasLightningRodNearby() {
        float lightningRadius = getLightningRodRadius();
        if (lightningRadius <= 0) {
            return false;
        }

        int r = (int) lightningRadius;
        double radiusSq = lightningRadius * lightningRadius;

        BlockPos center = this.blockPosition();
        for (int x = -r; x <= r; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -r; z <= r; z++) {
                    // Match existing behavior: radius check is horizontal (x/z), ignore y for the circle test
                    double horizSq = (double) x * (double) x + (double) z * (double) z;
                    if (horizSq > radiusSq) {
                        continue;
                    }

                    BlockPos pos = center.offset(x, y, z);
                    if (this.level().getBlockState(pos).is(Blocks.LIGHTNING_ROD)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    private void strikeLightningRod(BlockPos rodPos) {
        if (this.level() instanceof ServerLevel serverLevel) {
            // Create lightning at the lightning rod
            net.minecraft.world.entity.LightningBolt lightning = new net.minecraft.world.entity.LightningBolt(
                net.minecraft.world.entity.EntityType.LIGHTNING_BOLT, serverLevel
            );
            lightning.moveTo(Vec3.atBottomCenterOf(rodPos.above()));
            serverLevel.addFreshEntity(lightning);
            
            // Play lightning sound
            this.level().playSound(null, rodPos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.0f, 0.8f);
            
            // Spawn electric particles around the rod
            spawnElectricParticles(rodPos.getX() + 0.5, rodPos.getY() + 1.0, rodPos.getZ() + 0.5, 12);
        }
    }
    
    @Override
    protected void spawnParticles() {
        if (this.level().isClientSide) return;
        
        // Spawn swirling rift particles
        if (tickCount % (20 / PARTICLE_SPAWN_RATE) == 0) {
            spawnSwirlingParticles();
        }
        
        // Spawn electric sparks
        if (tickCount % 5 == 0) {
            spawnElectricSparks();
        }
        
        // Spawn aura particles around the core
        if (tickCount % 10 == 0) {
            spawnAuraParticles();
        }
    }
    
    private void spawnSwirlingParticles() {
        double radius = getZapRadius() * 0.6;
        double angle = (tickCount * 0.1) + (this.level().getRandom().nextDouble() * 0.5);
        double x = this.getX() + Math.cos(angle) * radius;
        double z = this.getZ() + Math.sin(angle) * radius;
        double y = this.getY() + (this.level().getRandom().nextDouble() - 0.5) * 2.0;
        
        // Spawn upward swirling particles
        this.level().addParticle(
            ParticleTypes.ELECTRIC_SPARK,
            x, y, z,
            Math.cos(angle + Math.PI/2) * 0.1, 0.2, Math.sin(angle + Math.PI/2) * 0.1
        );
    }
    
    private void spawnElectricSparks() {
        // Spawn random electric sparks around the rift
        net.minecraft.util.RandomSource random = this.level().getRandom();
        int sparkCount = 2 + random.nextInt(3); // 2-4 sparks
        
        for (int i = 0; i < sparkCount; i++) {
            double radius = getZapRadius() * (0.3 + random.nextDouble() * 0.7);
            double angle = random.nextDouble() * 2 * Math.PI;
            double height = random.nextDouble() * 2.0 - 1.0;
            
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            double y = this.getY() + height;
            
            // Spawn electric spark particles
            this.level().addParticle(
                ParticleTypes.ELECTRIC_SPARK,
                x, y, z,
                (random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.3
            );
        }
    }
    
    private void spawnAuraParticles() {
        double auraRadius = 1.5;
        for (int i = 0; i < 4; i++) {
            double angle = (tickCount * 0.05) + (i * Math.PI / 2);
            double x = this.getX() + Math.cos(angle) * auraRadius;
            double z = this.getZ() + Math.sin(angle) * auraRadius;
            double y = this.getY() + Math.sin(tickCount * 0.1) * 0.5;
            
            // Spawn electric aura particles
            this.level().addParticle(
                ParticleTypes.ELECTRIC_SPARK,
                x, y, z,
                0, 0.1, 0
            );
        }
    }
    
    private void spawnElectricParticles(double x, double y, double z, int count) {
        net.minecraft.util.RandomSource random = this.level().getRandom();
        for (int i = 0; i < count; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2.0;
            double offsetY = random.nextDouble() * 2.0;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0;
            
            this.level().addParticle(
                ParticleTypes.ELECTRIC_SPARK,
                x + offsetX, y + offsetY, z + offsetZ,
                (random.nextDouble() - 0.5) * 0.5, (random.nextDouble() - 0.5) * 0.5, (random.nextDouble() - 0.5) * 0.5
            );
        }
    }
    
    @Override
    protected void updateClientEffects() {
        // Client-side visual effects handled by the renderer
    }
    
    @Override
    public ResourceLocation getAnomalySound() {
        return StrangeMatterSounds.ENERGETIC_RIFT_LOOP.get().getLocation();
    }
    
    @Override
    protected ResearchType getResearchType() {
        return ResearchType.ENERGY;
    }
    
    @Override
    protected String getAnomalyName() {
        return "Energetic Rift";
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.strangematter.energetic_rift");
    }
    
    
    public List<TargetingSpark> getTargetingSparks() {
        return targetingSparks;
    }
    
    private void createTargetingSpark(Entity target) {
        // Get positions
        Vec3 riftPos = this.position().add(0, 0.5, 0); // Center of rift
        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0); // Center of target
        
        // Create targeting spark data for client-side rendering
        targetingSparks.add(new TargetingSpark(riftPos, targetPos, target.getId()));
    }
    
    // Inner class for targeting spark data
    public static class TargetingSpark {
        public final Vec3 startPos;
        public final Vec3 endPos;
        public final int targetId;
        public int age = 0;
        public final int maxAge = 20; // 1 second duration (20 ticks)
        
        public TargetingSpark(Vec3 start, Vec3 end, int targetId) {
            this.startPos = start;
            this.endPos = end;
            this.targetId = targetId;
        }
    }
    
    public boolean isActive() {
        return this.entityData.get(IS_ACTIVE);
    }
    
    public void setActive(boolean active) {
        this.entityData.set(IS_ACTIVE, active);
    }
}
