package com.hexvane.strangematter.entity;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Temporal Bloom anomaly entity that randomly affects crop growth stages
 * and transforms mobs between baby and adult forms in its radius.
 */
public class TemporalBloomEntity extends BaseAnomalyEntity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(TemporalBloomEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Config-driven getters for temporal bloom parameters
    private float getTemporalEffectRadius() {
        return (float) com.hexvane.strangematter.Config.temporalEffectRadius;
    }
    
    @Override
    protected float getEffectRadius() {
        return getTemporalEffectRadius();
    }
    
    
    private int getCropCooldownMax() {
        return com.hexvane.strangematter.Config.temporalCropCooldown;
    }
    
    private int getMobCooldownMax() {
        return com.hexvane.strangematter.Config.temporalMobCooldown;
    }
    
    private int getCropGrowthStages() {
        return com.hexvane.strangematter.Config.temporalCropGrowthStages;
    }
    
    private static final int PARTICLE_BURST_COOLDOWN = 40; // 2 seconds
    
    // Tracking for cooldowns
    private int cropEffectCooldown = 0;
    private int mobTransformCooldown = 0;
    private int particleBurstCooldown = 0;
    
    public TemporalBloomEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ACTIVE, true);
    }
    
    @Override
    protected void updatePulseAnimation() {
        // Create a gentle pulsing effect with temporal energy waves
        float time = (this.tickCount % 80) / 80.0f; // 4 second cycle (80 ticks)
        float pulseIntensity = (float) (0.3 + 0.7 * Math.sin(time * 2 * Math.PI));
        this.setPulseIntensity(pulseIntensity);
    }
    
    @Override
    protected void applyAnomalyEffects() {
        if (!this.isActive() || this.isContained() || !com.hexvane.strangematter.Config.enableTemporalEffects) {
            return; // Don't apply effects if not active, contained, or effects disabled
        }
        
        // Update cooldowns
        if (cropEffectCooldown > 0) cropEffectCooldown--;
        if (mobTransformCooldown > 0) mobTransformCooldown--;
        if (particleBurstCooldown > 0) particleBurstCooldown--;
        
        // Apply crop growth effects
        if (cropEffectCooldown <= 0) {
            affectNearbyCrops();
        }
        
        // Transform mobs between baby and adult forms
        if (mobTransformCooldown <= 0) {
            transformNearbyMobs();
        }
        
        // Create temporal particle bursts
        if (particleBurstCooldown <= 0) {
            createTemporalBurst();
        }
    }
    
    private void affectNearbyCrops() {
        BlockPos center = this.blockPosition();
        
        // Find crops in a circular area around the anomaly
        float effectRadius = getTemporalEffectRadius();
        for (int x = (int) -effectRadius; x <= effectRadius; x++) {
            for (int z = (int) -effectRadius; z <= effectRadius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= effectRadius) {
                    BlockPos pos = center.offset(x, 0, z);
                    
                    // Check multiple Y levels for crops
                    for (int y = -2; y <= 2; y++) {
                        BlockPos cropPos = pos.offset(0, y, 0);
                        BlockState state = this.level().getBlockState(cropPos);
                        
                        if (state.getBlock() instanceof CropBlock cropBlock) {
                            affectCrop(cropPos, state, cropBlock);
                        }
                    }
                }
            }
        }
        
        cropEffectCooldown = getCropCooldownMax();
    }
    
    private void affectCrop(BlockPos pos, BlockState state, CropBlock cropBlock) {
        // Get the age property for this crop - use reflection to access the protected method
        IntegerProperty ageProperty = null;
        try {
            java.lang.reflect.Method getAgePropertyMethod = CropBlock.class.getDeclaredMethod("getAgeProperty");
            getAgePropertyMethod.setAccessible(true);
            ageProperty = (IntegerProperty) getAgePropertyMethod.invoke(cropBlock);
        } catch (Exception e) {
            // If reflection fails, try to find the age property in the block state
            for (net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties()) {
                if (property instanceof IntegerProperty && property.getName().equals("age")) {
                    ageProperty = (IntegerProperty) property;
                    break;
                }
            }
        }
        
        if (ageProperty == null) {
            return; // Can't find age property, skip this crop
        }
        
        int currentAge = state.getValue(ageProperty);
        int maxAge = cropBlock.getMaxAge();
        
        // Randomly add or remove growth stages
        net.minecraft.util.RandomSource random = this.level().getRandom();
        int ageChange = 0;
        int maxStages = getCropGrowthStages();
        
        if (random.nextBoolean()) {
            // Add growth (age up)
            ageChange = 1 + random.nextInt(maxStages);
        } else {
            // Remove growth (age down)
            ageChange = -(1 + random.nextInt(maxStages));
        }
        
        int newAge = Math.max(0, Math.min(maxAge, currentAge + ageChange));
        
        if (newAge != currentAge) {
            BlockState newState = state.setValue(ageProperty, newAge);
            this.level().setBlock(pos, newState, 3);
            
            // Spawn temporal particles at the crop
            spawnTemporalParticles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8);
            spawnBlockEffectParticles(pos);
            
            // Play temporal sound effect
            this.level().playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 0.6f, 1.2f);
        }
    }
    
    private void transformNearbyMobs() {
        float effectRadius = getTemporalEffectRadius();
        AABB transformBox = this.getBoundingBox().inflate(effectRadius);
        List<Entity> entitiesInRange = this.level().getEntities(this, transformBox);
        
        boolean foundTarget = false;
        for (Entity entity : entitiesInRange) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                double distance = this.distanceTo(entity);
                if (distance <= effectRadius) {
                    transformMob((LivingEntity) entity);
                    foundTarget = true;
                }
            }
        }
        
        // Reset cooldown if we found a target
        if (foundTarget) {
            mobTransformCooldown = getMobCooldownMax();
        }
    }
    
    private void transformMob(LivingEntity mob) {
        if (mob instanceof Animal animal) {
            // Transform animals between baby and adult
            if (animal.isBaby()) {
                // Make adult
                animal.setAge(0); // 0 = adult
                spawnTemporalParticles(mob.getX(), mob.getY() + 1.0, mob.getZ(), 10);
                spawnEntityEffectParticles(mob);
                this.level().playSound(null, mob.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 0.8f, 1.5f);
            } else {
                // Make baby
                animal.setAge(-24000); // -24000 = baby
                spawnTemporalParticles(mob.getX(), mob.getY() + 1.0, mob.getZ(), 10);
                spawnEntityEffectParticles(mob);
                this.level().playSound(null, mob.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 0.8f, 0.8f);
            }
        } else if (mob instanceof Monster) {
            // For monsters, we can use a different approach since they don't have age
            // We'll create a visual effect and maybe change their size slightly
            spawnTemporalParticles(mob.getX(), mob.getY() + 1.0, mob.getZ(), 8);
            spawnEntityEffectParticles(mob);
            this.level().playSound(null, mob.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 0.6f, 1.0f);
            
            // Create a temporal distortion effect on the mob
            createTemporalDistortion(mob);
        }
    }
    
    private void createTemporalDistortion(LivingEntity mob) {
        // Create a brief temporal distortion effect
        // This could be expanded to include more complex effects
        Vec3 mobPos = mob.position();
        
        // Spawn particles in a spiral pattern around the mob
        for (int i = 0; i < 12; i++) {
            double angle = (i * Math.PI * 2) / 12.0;
            double radius = 1.5;
            double x = mobPos.x + Math.cos(angle) * radius;
            double z = mobPos.z + Math.sin(angle) * radius;
            double y = mobPos.y + 1.0;
            
            this.level().addParticle(
                ParticleTypes.END_ROD,
                x, y, z,
                (Math.random() - 0.5) * 0.1, 0.1, (Math.random() - 0.5) * 0.1
            );
        }
    }
    
    private void createTemporalBurst() {
        // Create a burst of temporal energy particles
        double radius = getTemporalEffectRadius() * 0.8;
        int particleCount = 15 + this.level().getRandom().nextInt(10); // 15-24 particles
        
        for (int i = 0; i < particleCount; i++) {
            double angle = this.level().getRandom().nextDouble() * 2 * Math.PI;
            double distance = this.level().getRandom().nextDouble() * radius;
            double height = (this.level().getRandom().nextDouble() - 0.5) * 3.0;
            
            double x = this.getX() + Math.cos(angle) * distance;
            double z = this.getZ() + Math.sin(angle) * distance;
            double y = this.getY() + height;
            
            // Spawn temporal energy particles
            this.level().addParticle(
                ParticleTypes.END_ROD,
                x, y, z,
                (this.level().getRandom().nextDouble() - 0.5) * 0.2,
                (this.level().getRandom().nextDouble() - 0.5) * 0.2,
                (this.level().getRandom().nextDouble() - 0.5) * 0.2
            );
        }
        
        particleBurstCooldown = PARTICLE_BURST_COOLDOWN;
    }
    
    @Override
    protected void spawnParticles() {
        if (this.level().isClientSide) return;
        
        // Spawn floating temporal crystals
        if (tickCount % (20 / PARTICLE_SPAWN_RATE) == 0) {
            spawnFloatingCrystals();
        }
        
        // Spawn energy ripples
        if (tickCount % 15 == 0) {
            spawnEnergyRipples();
        }
        
        // Spawn aura particles around the core
        if (tickCount % 8 == 0) {
            spawnAuraParticles();
        }
    }
    
    private void spawnFloatingCrystals() {
        // Spawn floating crystal particles around the bloom
        double radius = 2.0 + this.level().getRandom().nextDouble() * 2.0;
        double angle = (tickCount * 0.05) + (this.level().getRandom().nextDouble() * 0.5);
        double x = this.getX() + Math.cos(angle) * radius;
        double z = this.getZ() + Math.sin(angle) * radius;
        double y = this.getY() + (this.level().getRandom().nextDouble() - 0.5) * 2.0;
        
        // Spawn floating crystal particles
        this.level().addParticle(
            ParticleTypes.END_ROD,
            x, y, z,
            Math.cos(angle + Math.PI/2) * 0.05, 0.1, Math.sin(angle + Math.PI/2) * 0.05
        );
    }
    
    private void spawnEnergyRipples() {
        // Create energy ripples that expand outward
        double rippleRadius = (tickCount % 60) * 0.2; // Expand over 3 seconds
        if (rippleRadius > getTemporalEffectRadius()) return;
        
        for (int i = 0; i < 8; i++) {
            double angle = (i * Math.PI * 2) / 8.0;
            double x = this.getX() + Math.cos(angle) * rippleRadius;
            double z = this.getZ() + Math.sin(angle) * rippleRadius;
            double y = this.getY() + Math.sin(tickCount * 0.1) * 0.5;
            
            // Spawn ripple particles
            this.level().addParticle(
                ParticleTypes.ELECTRIC_SPARK,
                x, y, z,
                0, 0.05, 0
            );
        }
    }
    
    private void spawnAuraParticles() {
        double auraRadius = 1.0;
        for (int i = 0; i < 6; i++) {
            double angle = (tickCount * 0.03) + (i * Math.PI / 3);
            double x = this.getX() + Math.cos(angle) * auraRadius;
            double z = this.getZ() + Math.sin(angle) * auraRadius;
            double y = this.getY() + Math.sin(tickCount * 0.08) * 0.3;
            
            // Spawn temporal aura particles
            this.level().addParticle(
                ParticleTypes.END_ROD,
                x, y, z,
                0, 0.02, 0
            );
        }
    }
    
    private void spawnTemporalParticles(double x, double y, double z, int count) {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Use server-side particle spawning that will be sent to clients
            for (int i = 0; i < count; i++) {
                double offsetX = (this.level().getRandom().nextDouble() - 0.5) * 1.0;
                double offsetY = this.level().getRandom().nextDouble() * 1.0;
                double offsetZ = (this.level().getRandom().nextDouble() - 0.5) * 1.0;
                
                // Use fewer white particles for less overwhelming effect
                if (i % 2 == 0) { // Only spawn every other particle
                    serverLevel.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        x + offsetX, y + offsetY, z + offsetZ,
                        1,
                        (this.level().getRandom().nextDouble() - 0.5) * 0.2,
                        (this.level().getRandom().nextDouble() - 0.5) * 0.2,
                        (this.level().getRandom().nextDouble() - 0.5) * 0.2,
                        0.0
                    );
                }
            }
        }
    }
    
    private void spawnBlockEffectParticles(BlockPos pos) {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Add some portal particles for extra visibility
            for (int i = 0; i < 4; i++) {
                double x = pos.getX() + 0.5 + (this.level().getRandom().nextDouble() - 0.5) * 1.0;
                double z = pos.getZ() + 0.5 + (this.level().getRandom().nextDouble() - 0.5) * 1.0;
                double y = pos.getY() + 0.5 + this.level().getRandom().nextDouble() * 0.5;
                
                serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    x, y, z,
                    1,
                    (this.level().getRandom().nextDouble() - 0.5) * 0.1,
                    0.1,
                    (this.level().getRandom().nextDouble() - 0.5) * 0.1,
                    0.0
                );
            }
        }
    }
    
    private void spawnEntityEffectParticles(LivingEntity entity) {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Add portal particles for extra visibility
            for (int i = 0; i < 6; i++) {
                double x = entity.getX() + (this.level().getRandom().nextDouble() - 0.5) * entity.getBbWidth();
                double z = entity.getZ() + (this.level().getRandom().nextDouble() - 0.5) * entity.getBbWidth();
                double y = entity.getY() + entity.getBbHeight() * 0.5 + (this.level().getRandom().nextDouble() - 0.5) * entity.getBbHeight();
                
                serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    x, y, z,
                    1,
                    (this.level().getRandom().nextDouble() - 0.5) * 0.1,
                    0.1,
                    (this.level().getRandom().nextDouble() - 0.5) * 0.1,
                    0.0
                );
            }
        }
    }
    
    @Override
    protected void updateClientEffects() {
        // Client-side visual effects handled by the renderer
    }
    
    @Override
    public ResourceLocation getAnomalySound() {
        return StrangeMatterSounds.TEMPORAL_BLOOM_LOOP.get().getLocation();
    }
    
    @Override
    protected ResearchType getResearchType() {
        return ResearchType.TIME;
    }
    
    @Override
    protected String getAnomalyName() {
        return "Temporal Bloom";
    }
    
    @Override
    protected DeferredHolder<Block, Block> getShardOreBlock() {
        return StrangeMatterMod.CHRONO_SHARD_ORE_BLOCK;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.strangematter.temporal_bloom");
    }
    
    public boolean isActive() {
        return this.entityData.get(IS_ACTIVE);
    }
    
    public void setActive(boolean active) {
        this.entityData.set(IS_ACTIVE, active);
    }
}
