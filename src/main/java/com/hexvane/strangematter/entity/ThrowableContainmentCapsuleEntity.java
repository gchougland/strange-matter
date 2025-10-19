package com.hexvane.strangematter.entity;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.item.ContainmentCapsuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

public class ThrowableContainmentCapsuleEntity extends ThrowableItemProjectile {
    
    private static final EntityDataAccessor<ItemStack> CAPSULE_ITEM = SynchedEntityData.defineId(ThrowableContainmentCapsuleEntity.class, EntityDataSerializers.ITEM_STACK);
    
    public ThrowableContainmentCapsuleEntity(EntityType<? extends ThrowableContainmentCapsuleEntity> entityType, Level level) {
        super(entityType, level);
    }
    
    public ThrowableContainmentCapsuleEntity(Level level, LivingEntity shooter, ItemStack capsuleItem) {
        super(StrangeMatterMod.THROWABLE_CONTAINMENT_CAPSULE.get(), shooter, level);
        this.entityData.set(CAPSULE_ITEM, capsuleItem.copy());
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CAPSULE_ITEM, ItemStack.EMPTY);
    }
    
    @Override
    public Item getDefaultItem() {
        return StrangeMatterMod.CONTAINMENT_CAPSULE.get();
    }
    
    public ItemStack getCapsuleItem() {
        return this.entityData.get(CAPSULE_ITEM);
    }
    
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide && result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockResult = (BlockHitResult) result;
            BlockPos hitPos = blockResult.getBlockPos();
            
            ItemStack capsuleItem = getCapsuleItem();
            if (capsuleItem.getItem() instanceof ContainmentCapsuleItem containmentCapsule) {
                ContainmentCapsuleItem.AnomalyType anomalyType = containmentCapsule.getAnomalyType();
                
                if (anomalyType != ContainmentCapsuleItem.AnomalyType.NONE) {
                    // Spawn the anomaly at the hit location
                    spawnAnomalyAtLocation((ServerLevel) this.level(), hitPos, anomalyType);
                    
                    // Play glass breaking sound
                    this.level().playSound(null, hitPos.getX(), hitPos.getY(), hitPos.getZ(), 
                        SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
        }
        
        this.discard();
    }
    
    private void spawnAnomalyAtLocation(ServerLevel level, BlockPos pos, ContainmentCapsuleItem.AnomalyType anomalyType) {
        BlockPos spawnPos = pos.above(); // Spawn one block above the hit position
        
        switch (anomalyType) {
            case GRAVITY:
                GravityAnomalyEntity gravityAnomaly = StrangeMatterMod.GRAVITY_ANOMALY.get().create(level);
                if (gravityAnomaly != null) {
                    gravityAnomaly.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
                    gravityAnomaly.setTerrainModificationEnabled(false);
                    gravityAnomaly.setSpawnedFromCapsule(true);
                    level.addFreshEntity(gravityAnomaly);
                }
                break;
                
            case ENERGETIC:
                EnergeticRiftEntity energeticRift = StrangeMatterMod.ENERGETIC_RIFT.get().create(level);
                if (energeticRift != null) {
                    energeticRift.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
                    energeticRift.setTerrainModificationEnabled(false);
                    energeticRift.setSpawnedFromCapsule(true);
                    level.addFreshEntity(energeticRift);
                }
                break;
                
            case ECHOING_SHADOW:
                EchoingShadowEntity echoingShadow = StrangeMatterMod.ECHOING_SHADOW.get().create(level);
                if (echoingShadow != null) {
                    echoingShadow.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
                    echoingShadow.setTerrainModificationEnabled(false);
                    echoingShadow.setSpawnedFromCapsule(true);
                    level.addFreshEntity(echoingShadow);
                }
                break;
                
            case TEMPORAL_BLOOM:
                TemporalBloomEntity temporalBloom = StrangeMatterMod.TEMPORAL_BLOOM.get().create(level);
                if (temporalBloom != null) {
                    temporalBloom.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
                    temporalBloom.setTerrainModificationEnabled(false);
                    temporalBloom.setSpawnedFromCapsule(true);
                    level.addFreshEntity(temporalBloom);
                }
                break;
                
            case THOUGHTWELL:
                ThoughtwellEntity thoughtwell = StrangeMatterMod.THOUGHTWELL.get().create(level);
                if (thoughtwell != null) {
                    thoughtwell.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
                    thoughtwell.setTerrainModificationEnabled(false);
                    thoughtwell.setSpawnedFromCapsule(true);
                    level.addFreshEntity(thoughtwell);
                }
                break;
                
            case WARP_GATE:
                WarpGateAnomalyEntity warpGate = StrangeMatterMod.WARP_GATE_ANOMALY_ENTITY.get().create(level);
                if (warpGate != null) {
                    warpGate.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 2.0, spawnPos.getZ() + 0.5);
                    warpGate.setActive(true);
                    warpGate.setTerrainModificationEnabled(false);
                    warpGate.setSpawnedFromCapsule(true);
                    level.addFreshEntity(warpGate);
                }
                break;
                
            case NONE:
                // Do nothing for empty capsules
                break;
        }
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ItemStack capsuleItem = getCapsuleItem();
        if (!capsuleItem.isEmpty()) {
            tag.put("CapsuleItem", capsuleItem.save(new CompoundTag()));
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CapsuleItem")) {
            ItemStack capsuleItem = ItemStack.of(tag.getCompound("CapsuleItem"));
            this.entityData.set(CAPSULE_ITEM, capsuleItem);
        }
    }
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
