package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.network.HoverboardJumpPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Client-side handler for detecting jump input while riding the hoverboard
 * Sends jump packet to server when space bar is pressed (rising edge detection)
 */
@EventBusSubscriber(modid = StrangeMatterMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class HoverboardJumpHandler {
    
    private static boolean wasJumpKeyPressed = false;
    
    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player) || player != Minecraft.getInstance().player) {
            return;
        }
        
        // Check if player is riding a hoverboard
        if (player.getVehicle() instanceof com.hexvane.strangematter.entity.HoverboardEntity hoverboard) {
            // Check if jump key is currently pressed
            net.minecraft.client.KeyMapping jumpKey = Minecraft.getInstance().options.keyJump;
            boolean jumpKeyPressed = jumpKey != null && jumpKey.isDown();
            
            // Detect rising edge (key just pressed, not while held)
            if (jumpKeyPressed && !wasJumpKeyPressed) {
                // Send jump packet to server
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new HoverboardJumpPacket(hoverboard.getId()));
            }
            
            wasJumpKeyPressed = jumpKeyPressed;
        } else {
            wasJumpKeyPressed = false;
        }
    }
}

