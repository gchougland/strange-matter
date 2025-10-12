package com.hexvane.strangematter.event;

import com.hexvane.strangematter.client.PlayerMorphRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles client-side player morph rendering cleanup
 */
@Mod.EventBusSubscriber(modid = "strangematter", value = Dist.CLIENT)
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


