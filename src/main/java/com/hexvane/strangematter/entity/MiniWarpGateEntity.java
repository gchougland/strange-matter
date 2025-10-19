package com.hexvane.strangematter.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.hexvane.strangematter.StrangeMatterMod;

import java.util.List;
import java.util.UUID;

public class MiniWarpGateEntity extends Entity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(MiniWarpGateEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(MiniWarpGateEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> PAIRED_GATE_ID = SynchedEntityData.defineId(MiniWarpGateEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_PURPLE_PORTAL = SynchedEntityData.defineId(MiniWarpGateEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Constants for the mini warp gate
    private static final float TELEPORT_RADIUS = 1.0f; // 1x1 area
    private static final float AURA_RADIUS = 2.0f;
    private static final int TELEPORT_COOLDOWN = 40; // 2 seconds at 20 TPS (reduced from 5 seconds)
    private static final int MAX_LIFETIME = 12000; // 10 minutes at 20 TPS
    
    // Animation and behavior
    private float rotationSpeed = 0.5f;
    private int tickCount = 0;
    private int teleportCooldown = 0;
    private UUID ownerUUID;
    private UUID pairedGateUUID;
    private boolean hasBeenManaged = false;
    private boolean isPurplePortal = false; // false = cyan, true = purple
    
    public MiniWarpGateEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setBoundingBox(new AABB(-0.5, 0, -0.5, 0.5, 2, 0.5)); // 1x1x2 hitbox
    }
    
    @Override
    protected void defineSynchedData() {
        this.entityData.define(ROTATION, 0.0f);
        this.entityData.define(IS_ACTIVE, true);
        this.entityData.define(PAIRED_GATE_ID, "");
        this.entityData.define(IS_PURPLE_PORTAL, false);
    }
    
    @Override
    public void tick() {
        super.tick();
        tickCount++;
        
        if (!this.level().isClientSide) {
            // Server-side logic
            updateRotation();
            updateTeleportation();
            manageWarpGates();
            
            // Check lifetime
            if (tickCount > MAX_LIFETIME) {
                this.discard();
                return;
            }
            
            // Decrease teleport cooldown
            if (teleportCooldown > 0) {
                teleportCooldown--;
            }
        }
    }
    
    private void updateRotation() {
        float currentRotation = this.entityData.get(ROTATION);
        float newRotation = currentRotation + rotationSpeed;
        if (newRotation >= 360.0f) {
            newRotation -= 360.0f;
        }
        this.entityData.set(ROTATION, newRotation);
    }
    
    private void updateTeleportation() {
        if (!this.isActive() || teleportCooldown > 0) {
            return;
        }
        
        AABB teleportBox = this.getBoundingBox().inflate(TELEPORT_RADIUS);
        List<Entity> entitiesInRange = this.level().getEntities(this, teleportBox);
        
        for (Entity entity : entitiesInRange) {
            if (entity instanceof LivingEntity livingEntity) {
                // Check if entity is close enough to the center
                double distance = this.distanceTo(entity);
                if (distance <= TELEPORT_RADIUS) {
                    teleportEntity(livingEntity);
                    break; // Only teleport one entity per tick
                }
            }
        }
    }
    
    private void teleportEntity(LivingEntity entity) {
        MiniWarpGateEntity pairedGate = getPairedGate();
        if (pairedGate != null && pairedGate.isActive()) {
            // Only teleport if the paired gate is a different color
            if (pairedGate.isPurplePortal != this.isPurplePortal) {
                // Calculate teleport position (slightly offset to prevent immediate re-teleportation)
                Vec3 teleportPos = pairedGate.position().add(0, 0.5, 0);
                
                // Preserve velocity and direction
                Vec3 velocity = entity.getDeltaMovement();
                float yRot = entity.getYRot();
                float xRot = entity.getXRot();
                
                // Teleport the entity
                entity.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                entity.setDeltaMovement(velocity);
                entity.setYRot(yRot);
                entity.setXRot(xRot);
                
                // Set cooldown to prevent rapid teleportation
                this.teleportCooldown = TELEPORT_COOLDOWN;
                pairedGate.teleportCooldown = TELEPORT_COOLDOWN;
                
                // Play teleport sound
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), 
                    net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, 
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
    }
    
    private MiniWarpGateEntity getPairedGate() {
        String pairedId = this.entityData.get(PAIRED_GATE_ID);
        if (pairedId.isEmpty()) {
            return null;
        }
        
        try {
            UUID pairedUUID = UUID.fromString(pairedId);
            // Find the entity by UUID in the level
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(pairedUUID);
                if (entity instanceof MiniWarpGateEntity miniGate) {
                    return miniGate;
                }
            }
        } catch (IllegalArgumentException e) {
            // Invalid UUID, clear the paired gate
            this.entityData.set(PAIRED_GATE_ID, "");
        }
        
        return null;
    }
    
    private void manageWarpGates() {
        if (this.ownerUUID == null || this.hasBeenManaged) {
            return;
        }
        
        // Find the owner player
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.world.entity.player.Player player = serverLevel.getPlayerByUUID(this.ownerUUID);
            if (player != null) {
                // Remove any existing portal of the same color for this player
                removeExistingPortalOfSameColor();
                
                // Try to pair with the opposite color portal
                pairWithOppositeColor();
                
                this.hasBeenManaged = true;
            }
        }
    }
    
    private void pairWithOppositeColor() {
        // Find all mini warp gates owned by the same player
        List<MiniWarpGateEntity> playerGates = this.level().getEntitiesOfClass(MiniWarpGateEntity.class, 
            new AABB(this.position().add(-100, -100, -100), this.position().add(100, 100, 100)))
            .stream()
            .filter(gate -> gate != this) // Don't pair with ourselves
            .filter(gate -> gate.ownerUUID != null && gate.ownerUUID.equals(this.ownerUUID))
            .filter(gate -> gate.isPurplePortal != this.isPurplePortal) // Different color
            .toList();
        
        // If we find an opposite color portal, pair with it
        if (!playerGates.isEmpty()) {
            MiniWarpGateEntity oppositeGate = playerGates.get(0);
            
            // Pair the gates
            this.entityData.set(PAIRED_GATE_ID, oppositeGate.getUUID().toString());
            oppositeGate.entityData.set(PAIRED_GATE_ID, this.getUUID().toString());
            this.entityData.set(IS_ACTIVE, true);
            oppositeGate.entityData.set(IS_ACTIVE, true);
        }
    }
    
    private void removeExistingPortalOfSameColor() {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Find all mini warp gates owned by the same player
            List<MiniWarpGateEntity> playerGates = this.level().getEntitiesOfClass(MiniWarpGateEntity.class, 
                new AABB(this.position().add(-100, -100, -100), this.position().add(100, 100, 100)))
                .stream()
                .filter(gate -> gate != this) // Don't remove ourselves
                .filter(gate -> gate.ownerUUID != null && gate.ownerUUID.equals(this.ownerUUID))
                .filter(gate -> gate.isPurplePortal == this.isPurplePortal) // Same color
                .toList();
            
            // Remove any existing portal of the same color
            for (MiniWarpGateEntity existingGate : playerGates) {
                existingGate.discard();
            }
        }
    }
    
    public void setOwner(Player player) {
        this.ownerUUID = player.getUUID();
    }
    
    public boolean isActive() {
        return this.entityData.get(IS_ACTIVE);
    }
    
    public float getRotation() {
        return this.entityData.get(ROTATION);
    }
    
    public boolean isOnCooldown() {
        return teleportCooldown > 0;
    }
    
    public float getCooldownProgress() {
        return teleportCooldown / (float) TELEPORT_COOLDOWN;
    }
    
    // Accessor methods for WarpGateManager
    public void setPairedGateId(String pairedId) {
        this.entityData.set(PAIRED_GATE_ID, pairedId);
    }
    
    public void setActive(boolean active) {
        this.entityData.set(IS_ACTIVE, active);
    }
    
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }
    
    public void clearPairing() {
        this.entityData.set(PAIRED_GATE_ID, "");
        this.entityData.set(IS_ACTIVE, false);
    }
    
    public void setPortalType(boolean isPurple) {
        this.isPurplePortal = isPurple;
        this.entityData.set(IS_PURPLE_PORTAL, isPurple);
    }
    
    public boolean isPurplePortal() {
        return this.entityData.get(IS_PURPLE_PORTAL);
    }
    
    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
    }
    
    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        if (compound.contains("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        if (compound.contains("PairedGateUUID")) {
            this.pairedGateUUID = compound.getUUID("PairedGateUUID");
            this.entityData.set(PAIRED_GATE_ID, this.pairedGateUUID.toString());
        }
        this.tickCount = compound.getInt("TickCount");
    }
    
    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        if (this.pairedGateUUID != null) {
            compound.putUUID("PairedGateUUID", this.pairedGateUUID);
        }
        compound.putInt("TickCount", this.tickCount);
    }
}
