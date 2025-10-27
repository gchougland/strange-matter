package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Packet to sync mob cognitive disguise data from server to client
 */
public class MobDisguiseSyncPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "mob_disguise_sync");
    public static final Type<MobDisguiseSyncPacket> TYPE = new Type<>(ID);
    
    private final UUID mobUUID;
    private final String disguiseType;
    private final int disguiseDuration;
    private final boolean clearDisguise;
    
    public MobDisguiseSyncPacket(UUID mobUUID, String disguiseType, int disguiseDuration) {
        this.mobUUID = mobUUID;
        this.disguiseType = disguiseType;
        this.disguiseDuration = disguiseDuration;
        this.clearDisguise = false;
    }
    
    public MobDisguiseSyncPacket(UUID mobUUID) {
        this.mobUUID = mobUUID;
        this.disguiseType = "";
        this.disguiseDuration = 0;
        this.clearDisguise = true;
    }
    
    public MobDisguiseSyncPacket(FriendlyByteBuf buf) {
        this.mobUUID = buf.readUUID();
        this.clearDisguise = buf.readBoolean();
        if (!clearDisguise) {
            this.disguiseType = buf.readUtf();
            this.disguiseDuration = buf.readInt();
        } else {
            this.disguiseType = "";
            this.disguiseDuration = 0;
        }
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(mobUUID);
        buf.writeBoolean(clearDisguise);
        if (!clearDisguise) {
            buf.writeUtf(disguiseType);
            buf.writeInt(disguiseDuration);
        }
    }
    
    public static void handle(MobDisguiseSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This runs on the client side
            if (packet.clearDisguise) {
                com.hexvane.strangematter.entity.ThoughtwellEntity.removeDisguise(packet.mobUUID);
                // Clear cached disguise entity on client
                com.hexvane.strangematter.client.CognitiveDisguiseRenderer.cleanupDisguise(packet.mobUUID);
            } else {
                // Apply the disguise on the client
                com.hexvane.strangematter.entity.ThoughtwellEntity.setDisguise(packet.mobUUID, packet.disguiseType, packet.disguiseDuration);
            }
        });
    }
    
    public UUID getMobUUID() {
        return mobUUID;
    }
    
    public String getDisguiseType() {
        return disguiseType;
    }
    
    public int getDisguiseDuration() {
        return disguiseDuration;
    }
    
    public boolean isClearDisguise() {
        return clearDisguise;
    }
}

