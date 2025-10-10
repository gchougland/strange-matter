package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet to sync mob cognitive disguise data from server to client
 */
public class MobDisguiseSyncPacket {
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
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(mobUUID);
        buf.writeBoolean(clearDisguise);
        if (!clearDisguise) {
            buf.writeUtf(disguiseType);
            buf.writeInt(disguiseDuration);
        }
    }
    
    public static MobDisguiseSyncPacket decode(FriendlyByteBuf buf) {
        UUID mobUUID = buf.readUUID();
        boolean clearDisguise = buf.readBoolean();
        if (clearDisguise) {
            return new MobDisguiseSyncPacket(mobUUID);
        } else {
            String disguiseType = buf.readUtf();
            int disguiseDuration = buf.readInt();
            return new MobDisguiseSyncPacket(mobUUID, disguiseType, disguiseDuration);
        }
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // This runs on the client side
            if (clearDisguise) {
                com.hexvane.strangematter.entity.ThoughtwellEntity.removeDisguise(mobUUID);
                // Clear cached disguise entity on client
                com.hexvane.strangematter.client.CognitiveDisguiseRenderer.cleanupDisguise(mobUUID);
            } else {
                // Apply the disguise on the client
                com.hexvane.strangematter.entity.ThoughtwellEntity.setDisguise(mobUUID, disguiseType, disguiseDuration);
            }
        });
        context.setPacketHandled(true);
    }
}

