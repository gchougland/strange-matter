package com.hexvane.strangematter.block;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class StasisProjectorBlockEntity extends BlockEntity {
    
    private boolean powered = false;
    private UUID capturedEntityUUID = null;
    private UUID capturedItemEntityUUID = null;
    private Entity cachedEntity = null;
    private ItemEntity cachedItemEntity = null;
    
    // Height above the projector where items/entities float
    private static final double FLOAT_HEIGHT = 0.25;
    private static final double CAPTURE_RADIUS = 1.0;
    
    public StasisProjectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(StrangeMatterMod.STASIS_PROJECTOR_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    public void togglePower() {
        this.powered = !this.powered;
        if (!this.powered) {
            releaseAll();
        }
        setChanged();
        
        // Sync to client
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    
    public boolean isPowered() {
        return this.powered;
    }
    
    public void releaseAll() {
        // Release captured mob
        if (cachedEntity != null && level != null) {
            // Re-enable AI and physics
            if (cachedEntity instanceof Mob mob) {
                mob.setNoAi(false);
            }
            cachedEntity.setNoGravity(false);
            cachedEntity.setInvulnerable(false);
            cachedEntity = null;
        }
        
        // Release captured item entity
        if (cachedItemEntity != null && level != null) {
            cachedItemEntity.setNoGravity(false);
            cachedItemEntity.setPickUpDelay(10); // Short delay before pickup
            cachedItemEntity = null;
        }
        
        capturedEntityUUID = null;
        capturedItemEntityUUID = null;
        setChanged();
    }
    
    public ItemEntity getCapturedItemEntity() {
        return cachedItemEntity;
    }
    
    public Entity getCapturedEntity() {
        return cachedEntity;
    }
    
    @Override
    public Level getLevel() {
        return level;
    }
    
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }
    
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, StasisProjectorBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        
        if (!blockEntity.powered) {
            return;
        }
        
        Vec3 centerPos = Vec3.atCenterOf(pos).add(0, FLOAT_HEIGHT, 0);
        
        // Try to find and update cached entities
        if (blockEntity.capturedEntityUUID != null && blockEntity.cachedEntity == null && level instanceof ServerLevel serverLevel) {
            // Try to find the entity by UUID
            Entity entity = serverLevel.getEntity(blockEntity.capturedEntityUUID);
            if (entity != null) {
                blockEntity.cachedEntity = entity;
            }
        }
        
        if (blockEntity.capturedItemEntityUUID != null && blockEntity.cachedItemEntity == null && level instanceof ServerLevel serverLevel) {
            // Try to find the item entity by UUID
            Entity entity = serverLevel.getEntity(blockEntity.capturedItemEntityUUID);
            if (entity instanceof ItemEntity itemEntity) {
                blockEntity.cachedItemEntity = itemEntity;
            }
        }
        
        // Update captured entity position and state
        if (blockEntity.cachedEntity != null) {
            if (!blockEntity.cachedEntity.isAlive() || blockEntity.cachedEntity.isRemoved()) {
                blockEntity.cachedEntity = null;
                blockEntity.capturedEntityUUID = null;
                blockEntity.setChanged();
                return;
            }
            
            // Keep entity in stasis with rotation and bobbing like items
            // Add bobbing motion (like items do)
            long gameTime = level.getGameTime();
            double bobOffset = Math.sin((gameTime + 0) / 10.0) * 0.1; // Bob up and down by 0.1 blocks
            
            blockEntity.cachedEntity.setPos(centerPos.x, centerPos.y + bobOffset, centerPos.z);
            blockEntity.cachedEntity.setDeltaMovement(Vec3.ZERO);
            blockEntity.cachedEntity.setNoGravity(true);
            blockEntity.cachedEntity.setInvulnerable(true);
            
            // Rotate the entity slowly (like items do)
            float rotationSpeed = 4.0f; // Degrees per tick
            
            // For living entities, force synchronize all rotation values
            if (blockEntity.cachedEntity instanceof LivingEntity living) {
                float newYaw = (living.yBodyRot + rotationSpeed) % 360.0f;
                
                // Set all rotation components without touching previous values
                // Let Minecraft handle interpolation naturally
                living.yBodyRot = newYaw;
                living.setYRot(newYaw);
                living.yHeadRot = newYaw;
                
                living.setXRot(0);
            } else {
                // For non-living entities, just rotate normally
                float currentYaw = blockEntity.cachedEntity.getYRot();
                float newYaw = (currentYaw + rotationSpeed) % 360.0f;
                blockEntity.cachedEntity.setYRot(newYaw);
                blockEntity.cachedEntity.setXRot(0);
            }
            
            if (blockEntity.cachedEntity instanceof Mob mob) {
                mob.setNoAi(true);
                mob.setPersistenceRequired(); // Prevent despawning
            }
            if (blockEntity.cachedEntity instanceof LivingEntity living) {
                living.setDeltaMovement(Vec3.ZERO);
            }
            
            return; // Already have an entity, don't capture more
        }
        
        // Update captured item entity position
        if (blockEntity.cachedItemEntity != null) {
            if (!blockEntity.cachedItemEntity.isAlive() || blockEntity.cachedItemEntity.isRemoved()) {
                blockEntity.cachedItemEntity = null;
                blockEntity.capturedItemEntityUUID = null;
                blockEntity.setChanged();
                return;
            }
            
            // Keep item in stasis
            blockEntity.cachedItemEntity.setPos(centerPos.x, centerPos.y, centerPos.z);
            blockEntity.cachedItemEntity.setDeltaMovement(Vec3.ZERO);
            blockEntity.cachedItemEntity.setNoGravity(true);
            blockEntity.cachedItemEntity.setPickUpDelay(40); // Can't be picked up
            blockEntity.cachedItemEntity.setUnlimitedLifetime(); // Prevent despawning
            return; // Already have an item, don't capture more
        }
        
        // Try to capture nearby entities or items
        AABB captureBox = new AABB(
            centerPos.subtract(CAPTURE_RADIUS, CAPTURE_RADIUS, CAPTURE_RADIUS),
            centerPos.add(CAPTURE_RADIUS, CAPTURE_RADIUS, CAPTURE_RADIUS)
        );
        
        // First try to capture items
        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(ItemEntity.class, captureBox);
        if (!nearbyItems.isEmpty()) {
            ItemEntity itemEntity = nearbyItems.get(0);
            blockEntity.capturedItemEntityUUID = itemEntity.getUUID();
            blockEntity.cachedItemEntity = itemEntity;
            
            itemEntity.setPos(centerPos.x, centerPos.y, centerPos.z);
            itemEntity.setDeltaMovement(Vec3.ZERO);
            itemEntity.setNoGravity(true);
            itemEntity.setPickUpDelay(40);
            itemEntity.setUnlimitedLifetime(); // Prevent despawning
            
            blockEntity.setChanged();
            return;
        }
        
        // Then try to capture entities (mobs)
        List<Mob> nearbyEntities = level.getEntitiesOfClass(Mob.class, captureBox);
        
        if (!nearbyEntities.isEmpty()) {
            Mob entity = nearbyEntities.get(0);
            blockEntity.capturedEntityUUID = entity.getUUID();
            blockEntity.cachedEntity = entity;
            
            entity.setPos(centerPos.x, centerPos.y, centerPos.z);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);
            entity.setNoAi(true);
            entity.setPersistenceRequired(); // Prevent despawning
            
            blockEntity.setChanged();
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Powered", powered);
        
        if (capturedEntityUUID != null) {
            tag.putUUID("CapturedEntityUUID", capturedEntityUUID);
        }
        
        if (capturedItemEntityUUID != null) {
            tag.putUUID("CapturedItemEntityUUID", capturedItemEntityUUID);
        }
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        powered = tag.getBoolean("Powered");
        
        if (tag.hasUUID("CapturedEntityUUID")) {
            capturedEntityUUID = tag.getUUID("CapturedEntityUUID");
        } else {
            capturedEntityUUID = null;
        }
        
        if (tag.hasUUID("CapturedItemEntityUUID")) {
            capturedItemEntityUUID = tag.getUUID("CapturedItemEntityUUID");
        } else {
            capturedItemEntityUUID = null;
        }
    }
}

