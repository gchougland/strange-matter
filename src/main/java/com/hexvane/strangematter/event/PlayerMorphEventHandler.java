package com.hexvane.strangematter.event;

import com.hexvane.strangematter.morph.PlayerMorphData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.UUID;

/**
 * Handles server-side player morph cleanup and syncing
 */
public class PlayerMorphEventHandler {
    
    /**
     * Sync all morphs to a player when they join
     */
    @SubscribeEvent
    public static void onPlayerJoinLevel(EntityJoinLevelEvent event) {
        // Only handle on server side
        if (event.getLevel().isClientSide()) {
            return;
        }
        
        // Only handle players
        if (!(event.getEntity() instanceof ServerPlayer joiningPlayer)) {
            return;
        }
        
        System.out.println("DEBUG: Player joined server, syncing morphs to: " + joiningPlayer.getName().getString());
        
        // Sync all existing morphs to the joining player
        for (Player otherPlayer : event.getLevel().players()) {
            String morphType = PlayerMorphData.getMorphEntityType(otherPlayer.getUUID());
            if (morphType != null) {
                UUID targetPlayerUUID = PlayerMorphData.getMorphedPlayerUUID(otherPlayer.getUUID());
                System.out.println("DEBUG: Syncing morph " + morphType + " for player " + otherPlayer.getName().getString() + " to joining player");
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(joiningPlayer,
                    new com.hexvane.strangematter.network.PlayerMorphSyncPacket(otherPlayer.getUUID(), morphType, targetPlayerUUID, false));
            }
        }
    }
    
    /**
     * Clear morph data when player logs out
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        try {
            Player player = event.getEntity();
            if (player != null) {
                PlayerMorphData.clearMorph(player.getUUID());
            }
        } catch (Exception e) {
            // Silently catch any errors during logout to prevent crashes
            // This can happen if PlayerMorphData hasn't been loaded yet
        }
    }
}
