package com.hexvane.strangematter.event;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

public class PlayerSpawnEventHandler {
    
    private static final String RECEIVED_TABLET_TAG = "strangematter.received_research_tablet";
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // Only handle on server side
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        
        // Check if player is in survival mode
        if (serverPlayer.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) {
            return;
        }
        
        // Check if player has already received the research tablet
        if (serverPlayer.getPersistentData().getBoolean(RECEIVED_TABLET_TAG)) {
            return;
        }
        
        // Give the player a research tablet
        ItemStack researchTablet = new ItemStack(StrangeMatterMod.RESEARCH_TABLET.get());
        
        // Try to add to inventory, or drop if full
        if (!serverPlayer.getInventory().add(researchTablet)) {
            serverPlayer.drop(researchTablet, false);
        }
        
        // Mark that the player has received the research tablet
        serverPlayer.getPersistentData().putBoolean(RECEIVED_TABLET_TAG, true);
    }
}

