package com.hexvane.strangematter.client;

import com.hexvane.strangematter.entity.HoverboardEntity;
import com.hexvane.strangematter.network.HoverboardJumpPacket;
import com.hexvane.strangematter.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class HoverboardInputHandler {
    private static boolean wasJumpPressed = false;
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player != null && player.isPassenger()) {
            // Check if player is riding a hoverboard
            if (player.getVehicle() instanceof HoverboardEntity hoverboard) {
                // Check if jump key is pressed
                boolean jumpPressed = mc.options.keyJump.isDown();
                
                // Only send packet on the rising edge (when key is first pressed)
                // This prevents continuous jumping while the key is held
                if (jumpPressed && !wasJumpPressed) {
                    // Key was just pressed - send jump packet to server
                    HoverboardJumpPacket packet = new HoverboardJumpPacket(hoverboard.getId());
                    NetworkHandler.INSTANCE.sendToServer(packet);
                }
                
                wasJumpPressed = jumpPressed;
            } else {
                wasJumpPressed = false;
            }
        } else {
            wasJumpPressed = false;
        }
    }
}

