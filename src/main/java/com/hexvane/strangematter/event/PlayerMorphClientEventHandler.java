package com.hexvane.strangematter.event;

import com.hexvane.strangematter.client.PlayerMorphRenderer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Handles client-side player morph rendering cleanup
 */
@EventBusSubscriber(modid = "strangematter", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class PlayerMorphClientEventHandler {
    
    /**
     * Clear client-side cached morph entities when player changes dimension/respawns
     */
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            PlayerMorphRenderer.cleanupMorphEntity(player.getUUID());
        }
    }
    
    /**
     * Clear client-side cached morph entities on respawn
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            PlayerMorphRenderer.cleanupMorphEntity(player.getUUID());
        }
    }
}


