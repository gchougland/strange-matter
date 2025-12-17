package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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
            if (!context.getDirection().getReceptionSide().isClient()) return;

            UUID id = mobUUID;
            boolean clear = clearDisguise;
            String type = disguiseType;
            int duration = disguiseDuration;

            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hexvane.strangematter.client.network.ClientPacketHandlers.handleMobDisguiseSync(id, clear, type, duration)
            );
        });
        context.setPacketHandled(true);
    }
}

