package com.hexvane.strangematter.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;
import com.hexvane.strangematter.entity.ChronoBlisterProjectileEntity;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.client.ChronoBlisterRenderer;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import org.jetbrains.annotations.NotNull;

public class ChronoBlisterItem extends SwordItem {
    
    private static final String CHARGING_TAG = "charging";
    private static final String CHARGE_TIME_TAG = "charge_time";
    private static final String PROJECTILE_ID_TAG = "projectile_id";
    private static final int MAX_CHARGE_TIME = 20; // 1.5 seconds at 20 TPS
    
    public ChronoBlisterItem() {
        super(Tiers.WOOD, 3, -2.4f, new Item.Properties().stacksTo(1).durability(100));
    }
    
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new ChronoBlisterRenderer());
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // Start charging on right click
        if (!isCharging(stack)) {
            startCharging(level, player, stack);
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        
        return InteractionResultHolder.pass(stack);
    }
    
    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) {
            return;
        }
        
        if (!isCharging(stack)) {
            return;
        }
        
        int chargeTime = getChargeTime(stack);
        chargeTime++;
        setChargeTime(stack, chargeTime);
        
        // Update projectile charge progress
        int projectileId = getProjectileId(stack);
        if (projectileId != -1) {
            var projectile = level.getEntity(projectileId);
            if (projectile instanceof ChronoBlisterProjectileEntity chronoProjectile) {
                float chargeProgress = Math.min(1.0f, (float) chargeTime / MAX_CHARGE_TIME);
                chronoProjectile.setChargeProgress(chargeProgress);
            } else {
                // Projectile was removed somehow, stop charging
                stopCharging(stack);
            }
        }
    }
    
    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity, int timeCharged) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) {
            return;
        }
        
        if (isCharging(stack)) {
            int chargeTime = getChargeTime(stack);
            boolean fullyCharged = chargeTime >= MAX_CHARGE_TIME;
            
            int projectileId = getProjectileId(stack);
            if (projectileId != -1) {
                var projectile = level.getEntity(projectileId);
                if (projectile instanceof ChronoBlisterProjectileEntity chronoProjectile) {
                    if (fullyCharged) {
                        // Fire the projectile
                        Vec3 lookDirection = player.getLookAngle();
                        chronoProjectile.shoot(lookDirection.x, lookDirection.y, lookDirection.z, 1.5f, 1.0f);
                        chronoProjectile.setFired(true);
                        
                        // Play fire sound
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                            StrangeMatterSounds.CHRONO_BLISTER_FIRE.get(), 
                            SoundSource.PLAYERS, 1.0f, 1.0f);
                    } else {
                        // Despawn the projectile if not fully charged
                        chronoProjectile.discard();
                    }
                }
            }
            
            stopCharging(stack);
        }
    }
    
    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000; // Max duration for charging
    }
    
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.NONE; // No animation - projectile provides visual feedback
    }
    
    private void startCharging(Level level, Player player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(CHARGING_TAG, true);
        tag.putInt(CHARGE_TIME_TAG, 0);
        
        // Play charge sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
            StrangeMatterSounds.CHRONO_BLISTER_CHARGE.get(), 
            SoundSource.PLAYERS, 1.0f, 1.0f);
        
        // Spawn projectile immediately when charging starts
        if (!level.isClientSide) {
            Vec3 lookDirection = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();
            // Spawn 0.5 blocks in front of the player so it's visible
            Vec3 startPos = eyePos.add(lookDirection.scale(0.5));
            
            ChronoBlisterProjectileEntity projectile = new ChronoBlisterProjectileEntity(
                StrangeMatterMod.CHRONO_BLISTER_PROJECTILE_ENTITY.get(), 
                player, 
                level
            );
            projectile.setPos(startPos.x, startPos.y, startPos.z);
            projectile.setChargeProgress(0.0f);
            projectile.setFired(false);
            projectile.setNoGravity(true); // Don't fall while charging
            
            level.addFreshEntity(projectile);
            tag.putInt(PROJECTILE_ID_TAG, projectile.getId());
        }
    }
    
    private void stopCharging(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.putBoolean(CHARGING_TAG, false);
            tag.putInt(CHARGE_TIME_TAG, 0);
            tag.remove(PROJECTILE_ID_TAG);
        }
    }
    
    private boolean isCharging(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(CHARGING_TAG);
    }
    
    private int getChargeTime(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(CHARGE_TIME_TAG) : 0;
    }
    
    private void setChargeTime(ItemStack stack, int time) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(CHARGE_TIME_TAG, time);
    }
    
    private int getProjectileId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(PROJECTILE_ID_TAG) ? tag.getInt(PROJECTILE_ID_TAG) : -1;
    }
}

