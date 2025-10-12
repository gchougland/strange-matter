package com.hexvane.strangematter.event;

import com.hexvane.strangematter.item.EchoVacuumItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler for Echo Vacuum item - handles server-side tick logic
 */
@Mod.EventBusSubscriber(modid = "strangematter")
public class EchoVacuumEventHandler {
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Player player = event.player;
        Level level = player.level();
        
        // Only run on server side
        if (level.isClientSide) return;
        
        // Call the item's static tick handler
        EchoVacuumItem.handlePlayerTick(player, level);
    }
}


