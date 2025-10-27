package com.hexvane.strangematter.network;

import com.hexvane.strangematter.morph.PlayerMorphData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Packet to sync player morph data from server to client
 */
public class PlayerMorphSyncPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "player_morph_sync");
    public static final Type<PlayerMorphSyncPacket> TYPE = new Type<>(ID);
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
    
    public PlayerMorphSyncPacket(FriendlyByteBuf buffer) {
        this.playerUUID = buffer.readUUID();
        this.clearMorph = buffer.readBoolean();
        if (!clearMorph) {
            boolean hasMorphType = buffer.readBoolean();
            if (hasMorphType) {
                this.morphEntityType = buffer.readUtf();
            } else {
                this.morphEntityType = null;
            }
            boolean hasTargetPlayer = buffer.readBoolean();
            if (hasTargetPlayer) {
                this.targetPlayerUUID = buffer.readUUID();
            } else {
                this.targetPlayerUUID = null;
            }
        } else {
            this.morphEntityType = null;
            this.targetPlayerUUID = null;
        }
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeBoolean(clearMorph);
        if (!clearMorph) {
            buffer.writeBoolean(morphEntityType != null);
            if (morphEntityType != null) {
                buffer.writeUtf(morphEntityType);
            }
            buffer.writeBoolean(targetPlayerUUID != null);
            if (targetPlayerUUID != null) {
                buffer.writeUUID(targetPlayerUUID);
            }
        }
    }
    
    public static void handle(PlayerMorphSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This runs on the client thread
            try {
                if (packet.clearMorph) {
                    System.out.println("CLIENT: Received clear morph for player " + packet.playerUUID);
                    PlayerMorphData.clearMorph(packet.playerUUID);
                    com.hexvane.strangematter.client.PlayerMorphRenderer.cleanupMorphEntity(packet.playerUUID);
                } else if (packet.morphEntityType != null) {
                    System.out.println("CLIENT: Received morph sync - " + packet.playerUUID + " -> " + packet.morphEntityType);
                    System.out.println("CLIENT: Current morph data before update: " + PlayerMorphData.getMorphEntityType(packet.playerUUID));
                    
                    // Clear old morph first
                    String oldMorph = PlayerMorphData.getMorphEntityType(packet.playerUUID);
                    if (oldMorph != null && !oldMorph.equals(packet.morphEntityType)) {
                        System.out.println("CLIENT: Clearing old morph entity cache");
                        UUID finalPlayerUUID = packet.playerUUID;
                        if (FMLEnvironment.dist == Dist.CLIENT) {
                            com.hexvane.strangematter.client.PlayerMorphRenderer.cleanupMorphEntity(finalPlayerUUID);
                        }
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
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public String getMorphEntityType() {
        return morphEntityType;
    }
    
    public UUID getTargetPlayerUUID() {
        return targetPlayerUUID;
    }
    
    public boolean isClearMorph() {
        return clearMorph;
    }
}

