package com.hexvane.strangematter.event;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.WarpProjectileEntity;
import com.hexvane.strangematter.item.WarpGunItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StrangeMatterMod.MODID)
public class WarpGunEventHandler {
    
    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        
        // Check if the player is holding a warp gun
        if (stack.getItem() instanceof WarpGunItem) {
            System.out.println("Left click empty detected with warp gun!");
            // Send packet to server to handle left-click
            com.hexvane.strangematter.network.NetworkHandler.INSTANCE.sendToServer(
                new com.hexvane.strangematter.network.WarpGunShootPacket(false)
            );
        }
    }
    
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        
        // Check if the player is holding a warp gun
        if (stack.getItem() instanceof WarpGunItem) {
            System.out.println("Left click block detected with warp gun!");
            // Send packet to server to handle left-click
            com.hexvane.strangematter.network.NetworkHandler.INSTANCE.sendToServer(
                new com.hexvane.strangematter.network.WarpGunShootPacket(false)
            );
        }
    }
}
