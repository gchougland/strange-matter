package com.hexvane.strangematter.network;

import com.hexvane.strangematter.morph.PlayerMorphData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet to sync player morph data from server to client
 */
public class PlayerMorphSyncPacket {
    private final UUID playerUUID;
    private final String morphEntityType; // null means clear morph
    private final UUID targetPlayerUUID; // UUID of player being morphed into (for skin)
    private final boolean clearMorph;
    
    public PlayerMorphSyncPacket(UUID playerUUID, String morphEntityType, UUID targetPlayerUUID, boolean clearMorph) {
        this.playerUUID = playerUUID;
        this.morphEntityType = morphEntityType;
        this.targetPlayerUUID = targetPlayerUUID;
        this.clearMorph = clearMorph;
    }
    
    public static void encode(PlayerMorphSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.playerUUID);
        buffer.writeBoolean(packet.clearMorph);
        if (!packet.clearMorph) {
            if (packet.morphEntityType != null) {
                buffer.writeBoolean(true);
                buffer.writeUtf(packet.morphEntityType);
            } else {
                buffer.writeBoolean(false);
            }
            // Write target player UUID if present
            buffer.writeBoolean(packet.targetPlayerUUID != null);
            if (packet.targetPlayerUUID != null) {
                buffer.writeUUID(packet.targetPlayerUUID);
            }
        }
    }
    
    public static PlayerMorphSyncPacket decode(FriendlyByteBuf buffer) {
        UUID playerUUID = buffer.readUUID();
        boolean clearMorph = buffer.readBoolean();
        String morphEntityType = null;
        UUID targetPlayerUUID = null;
        
        if (!clearMorph) {
            boolean hasMorphType = buffer.readBoolean();
            if (hasMorphType) {
                morphEntityType = buffer.readUtf();
            }
            boolean hasTargetPlayer = buffer.readBoolean();
            if (hasTargetPlayer) {
                targetPlayerUUID = buffer.readUUID();
            }
        }
        return new PlayerMorphSyncPacket(playerUUID, morphEntityType, targetPlayerUUID, clearMorph);
    }
    
    public static void handle(PlayerMorphSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // This runs on the client thread
            try {
                if (packet.clearMorph) {
                    System.out.println("CLIENT: Received clear morph for player " + packet.playerUUID);
                    PlayerMorphData.clearMorph(packet.playerUUID);
                    UUID finalPlayerUUID = packet.playerUUID;
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> 
                        com.hexvane.strangematter.client.network.ClientPacketHandlers.cleanupMorphEntity(finalPlayerUUID));
                } else if (packet.morphEntityType != null) {
                    System.out.println("CLIENT: Received morph sync - " + packet.playerUUID + " -> " + packet.morphEntityType);
                    System.out.println("CLIENT: Current morph data before update: " + PlayerMorphData.getMorphEntityType(packet.playerUUID));
                    
                    // Clear old morph first
                    String oldMorph = PlayerMorphData.getMorphEntityType(packet.playerUUID);
                    if (oldMorph != null && !oldMorph.equals(packet.morphEntityType)) {
                        System.out.println("CLIENT: Clearing old morph entity cache");
                        UUID finalPlayerUUID = packet.playerUUID;
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> 
                            com.hexvane.strangematter.client.network.ClientPacketHandlers.cleanupMorphEntity(finalPlayerUUID));
                    }
                    
                    // Set new morph (with player UUID if morphing into a player)
                    PlayerMorphData.setMorph(packet.playerUUID, packet.morphEntityType, packet.targetPlayerUUID);
                    System.out.println("CLIENT: Morph data after update: " + PlayerMorphData.getMorphEntityType(packet.playerUUID));
                    if (packet.targetPlayerUUID != null) {
                        System.out.println("CLIENT: Target player UUID: " + packet.targetPlayerUUID);
                    }
                }
            } catch (Exception e) {
                System.err.println("ERROR handling morph sync packet: " + e.getMessage());
                e.printStackTrace();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

