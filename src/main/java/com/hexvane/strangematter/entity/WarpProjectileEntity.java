package com.hexvane.strangematter.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import com.hexvane.strangematter.entity.MiniWarpGateEntity;
import com.hexvane.strangematter.StrangeMatterMod;

public class WarpProjectileEntity extends ThrowableProjectile {
    
    private boolean isPurplePortal = false; // false = cyan, true = purple
    
    public WarpProjectileEntity(EntityType<? extends WarpProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }
    
    public WarpProjectileEntity(EntityType<? extends WarpProjectileEntity> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
    }
    
    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockResult = (BlockHitResult) result;
            BlockPos hitPos = blockResult.getBlockPos();
            
            // Check if there's a 2x2x2 area around the hit position
            if (canSpawnWarpGate(hitPos)) {
                // Find the center of the 2x2x2 area
                BlockPos centerPos = findCenterPosition(hitPos);
                
                // Spawn the mini warp gate
                spawnMiniWarpGate(centerPos);
            }
        }
        
        // Remove the projectile
        this.discard();
    }
    
    private boolean canSpawnWarpGate(BlockPos hitPos) {
        // Search in a 3x3x3 area around the hit position for a suitable 2x2x2 space
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    BlockPos searchPos = hitPos.offset(offsetX, offsetY, offsetZ);
                    if (hasSpaceForWarpGate(searchPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private boolean hasSpaceForWarpGate(BlockPos centerPos) {
        // Check if there's enough space in a 2x2x2 area starting from centerPos
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos checkPos = centerPos.offset(x, y, z);
                    BlockState state = this.level().getBlockState(checkPos);
                    
                    // Check if the block is solid and not air
                    if (!state.isAir() && state.isSolidRender(this.level(), checkPos)) {
                        return false; // Not enough space
                    }
                }
            }
        }
        return true;
    }
    
    private BlockPos findCenterPosition(BlockPos hitPos) {
        // Search in a 3x3x3 area around the hit position for the best 2x2x2 space
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    BlockPos searchPos = hitPos.offset(offsetX, offsetY, offsetZ);
                    if (hasSpaceForWarpGate(searchPos)) {
                        return searchPos; // Return the first suitable position found
                    }
                }
            }
        }
        // Fallback to original position if no suitable space found
        return hitPos;
    }
    
    public void setPortalType(boolean isPurple) {
        this.isPurplePortal = isPurple;
    }
    
    private void spawnMiniWarpGate(BlockPos centerPos) {
        if (!this.level().isClientSide) {
            MiniWarpGateEntity miniWarpGate = new MiniWarpGateEntity(StrangeMatterMod.MINI_WARP_GATE_ENTITY.get(), this.level());
            miniWarpGate.setPos(centerPos.getX() + 0.5, centerPos.getY() + 1.0, centerPos.getZ() + 0.5);
            miniWarpGate.setPortalType(this.isPurplePortal);
            
            System.out.println("Spawning mini warp gate with isPurple: " + this.isPurplePortal);
            
            // Set the owner to the player who shot the projectile
            if (this.getOwner() instanceof Player player) {
                miniWarpGate.setOwner(player);
            }
            
            this.level().addFreshEntity(miniWarpGate);
        }
    }
    
    @Override
    protected void defineSynchedData() {
        // No additional data needed for the projectile
    }
}
