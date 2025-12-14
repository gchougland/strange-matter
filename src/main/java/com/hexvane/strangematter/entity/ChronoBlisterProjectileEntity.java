package com.hexvane.strangematter.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import com.hexvane.strangematter.StrangeMatterMod;
import org.jetbrains.annotations.NotNull;

public class ChronoBlisterProjectileEntity extends ThrowableProjectile {
    
    private static final EntityDataAccessor<Float> CHARGE_PROGRESS = SynchedEntityData.defineId(ChronoBlisterProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FIRED = SynchedEntityData.defineId(ChronoBlisterProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    
    private float chargeProgress = 0.0f;
    private boolean fired = false;
    
    public ChronoBlisterProjectileEntity(EntityType<? extends ChronoBlisterProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }
    
    public ChronoBlisterProjectileEntity(EntityType<? extends ChronoBlisterProjectileEntity> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
    }
    
    public void setChargeProgress(float progress) {
        this.chargeProgress = Math.max(0.0f, Math.min(1.0f, progress));
        this.entityData.set(CHARGE_PROGRESS, this.chargeProgress);
    }
    
    public float getChargeProgress() {
        return this.entityData.get(CHARGE_PROGRESS);
    }
    
    public void setFired(boolean fired) {
        this.fired = fired;
        this.entityData.set(FIRED, fired);
        if (fired) {
            this.setNoGravity(false); // Enable gravity when fired
        }
    }
    
    public boolean isFired() {
        return this.entityData.get(FIRED);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // Update charge progress from synched data
        this.chargeProgress = this.entityData.get(CHARGE_PROGRESS);
        this.fired = this.entityData.get(FIRED);
        
        // If not fired yet, follow the player's look direction
        if (!fired && this.getOwner() instanceof LivingEntity owner) {
            Vec3 lookDirection = owner.getLookAngle();
            Vec3 eyePos = owner.getEyePosition();
            // Keep it 0.5 blocks in front of the player so it's visible
            Vec3 startPos = eyePos.add(lookDirection.scale(0.5));
            this.setPos(startPos.x, startPos.y, startPos.z);
            this.setDeltaMovement(Vec3.ZERO); // Don't move while charging
        }
    }
    
    @Override
    protected void onHit(HitResult result) {
        // Only trigger hit if the projectile has been fired
        if (!this.fired) {
            return;
        }
        
        BlockPos hitPos;
        
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockResult = (BlockHitResult) result;
            hitPos = blockResult.getBlockPos().relative(blockResult.getDirection());
        } else if (result.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityResult = (EntityHitResult) result;
            hitPos = entityResult.getEntity().blockPosition();
        } else {
            this.discard();
            return;
        }
        
        // Spawn blob of time dilation blocks
        spawnTimeDilationBlob(hitPos);
        
        // Remove the projectile
        this.discard();
    }
    
    private void spawnTimeDilationBlob(BlockPos centerPos) {
        if (this.level().isClientSide) return;
        
        // Create a solid blob formation - fill a roughly spherical/rounded blob
        int radius = 2; // 2 block radius
        
        // Spawn blocks in a solid blob pattern
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Calculate distance from center
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    
                    // Create a solid blob - fill most positions within radius
                    // Use a slightly irregular shape for more natural blob appearance
                    if (distance <= radius + 0.5) {
                        // Add some randomness to make it less perfect sphere
                        // But keep it mostly solid
                        if (distance <= radius - 0.3 || this.level().getRandom().nextDouble() < 0.7) {
                            BlockPos spawnPos = centerPos.offset(x, y, z);
                            BlockState currentState = this.level().getBlockState(spawnPos);
                            
                            // Only spawn in air or replaceable blocks
                            if (currentState.isAir() || currentState.canBeReplaced()) {
                                this.level().setBlock(spawnPos, StrangeMatterMod.TIME_DILATION_BLOCK.get().defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    protected void defineSynchedData() {
        this.entityData.define(CHARGE_PROGRESS, 0.0f);
        this.entityData.define(FIRED, false);
    }
    
    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("ChargeProgress", this.chargeProgress);
        tag.putBoolean("Fired", this.fired);
    }
    
    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.chargeProgress = tag.getFloat("ChargeProgress");
        this.fired = tag.getBoolean("Fired");
        this.entityData.set(CHARGE_PROGRESS, this.chargeProgress);
        this.entityData.set(FIRED, this.fired);
    }
}

