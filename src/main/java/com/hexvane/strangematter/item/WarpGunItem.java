package com.hexvane.strangematter.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.hexvane.strangematter.entity.WarpProjectileEntity;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class WarpGunItem extends SwordItem {
    
    public WarpGunItem() {
        super(Tiers.WOOD, 3, -2.4f, new Item.Properties().stacksTo(1).durability(100));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            shootProjectile(player, true); // Right-click = purple portal
        }
        
        return InteractionResultHolder.success(stack);
    }
    
    
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, net.minecraft.world.entity.Entity entity) {
        // Always shoot on left-click, regardless of entity
        shootProjectile(player, false); // Left-click = cyan portal
        return true; // Prevent default attack behavior and swing animation
    }
    
    @Override
    public boolean canAttackBlock(net.minecraft.world.level.block.state.BlockState state, Level level, net.minecraft.core.BlockPos pos, Player player) {
        return false; // Allow attacking blocks
    }
    
    @Override
    public boolean isCorrectToolForDrops(net.minecraft.world.level.block.state.BlockState state) {
        return false; // Don't actually mine blocks
    }
    
    
    private void shootProjectile(Player player, boolean isPurple) {
        if (!player.level().isClientSide) {
            shootProjectileInternal(player, isPurple);
        }
    }
    
    public static void shootProjectileStatic(Player player, boolean isPurple) {
        shootProjectileInternal(player, isPurple);
    }
    
    private static void shootProjectileInternal(Player player, boolean isPurple) {
        // Calculate the direction the player is looking
        Vec3 lookDirection = player.getLookAngle();
        Vec3 startPos = player.getEyePosition();
        
        // Create and spawn the warp projectile
        WarpProjectileEntity projectile = new WarpProjectileEntity(StrangeMatterMod.WARP_PROJECTILE_ENTITY.get(), player.level());
        projectile.setPos(startPos.x, startPos.y, startPos.z);
        projectile.shoot(lookDirection.x, lookDirection.y, lookDirection.z, 1.5f, 1.0f);
        projectile.setOwner(player);
        projectile.setPortalType(isPurple);
        
        // Play firing sound
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            com.hexvane.strangematter.sound.StrangeMatterSounds.WARP_GUN_SHOOT.get(), 
            net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
        
        player.level().addFreshEntity(projectile);
        
        System.out.println((isPurple ? "Right-click" : "Left-click") + ": Created " + (isPurple ? "purple" : "cyan") + " projectile");
    }
    
}
