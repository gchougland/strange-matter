package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TimeDilationSyncPacket {
    private final double slowdownFactor;
    
    public TimeDilationSyncPacket(double slowdownFactor) {
        this.slowdownFactor = slowdownFactor;
    }
    
    public TimeDilationSyncPacket(FriendlyByteBuf buffer) {
        this.slowdownFactor = buffer.readDouble();
    }
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(this.slowdownFactor);
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        
        context.enqueueWork(() -> {
            // Client-side handling - get the local player
            if (context.getSender() == null) {
                net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                net.minecraft.world.entity.player.Player player = minecraft.player;
                if (player != null) {
                    player.getPersistentData().putDouble("strangematter.time_dilation_factor", this.slowdownFactor);
                    com.hexvane.strangematter.TimeDilationData.setPlayerSlowdownFactor(player.getUUID(), this.slowdownFactor);
                }
            }
        });
        context.setPacketHandled(true);
    }
    
    public double getSlowdownFactor() {
        return slowdownFactor;
    }
}

