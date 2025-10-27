package com.hexvane.strangematter.event;

import com.hexvane.strangematter.item.EchoVacuumItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Event handler for Echo Vacuum item - handles server-side tick logic
 */
public class EchoVacuumEventHandler {
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // PlayerTickEvent.Post is already the END phase, so no need to check phase
        
        Player player = event.getEntity();
        Level level = player.level();
        
        // Only run on server side
        if (level.isClientSide) return;
        
        // Call the item's static tick handler
        EchoVacuumItem.handlePlayerTick(player, level);
    }
}


