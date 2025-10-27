package com.hexvane.strangematter.event;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.item.WarpGunItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

public class WarpGunEventHandler {
    
    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        
        // Check if the player is holding a warp gun
        if (stack.getItem() instanceof WarpGunItem) {
            System.out.println("Left click empty detected with warp gun!");
            // Send packet to server to handle left-click
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
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
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new com.hexvane.strangematter.network.WarpGunShootPacket(false)
            );
        }
    }
}
