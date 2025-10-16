package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GravitySyncPacket {
    private final double gravityForce;
    
    public GravitySyncPacket(double gravityForce) {
        this.gravityForce = gravityForce;
    }
    
    public GravitySyncPacket(FriendlyByteBuf buffer) {
        this.gravityForce = buffer.readDouble();
    }
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(this.gravityForce);
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        
        context.enqueueWork(() -> {
            // For client-side packets, getSender() returns null, so we need to get the local player
            if (context.getSender() != null) {
                // Server-side handling (shouldn't happen with our current setup)
                context.getSender().getPersistentData().putDouble("strangematter.gravity_force", this.gravityForce);
            } else {
                // Client-side handling - get the local player
                net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft.player != null) {
                    minecraft.player.getPersistentData().putDouble("strangematter.gravity_force", this.gravityForce);
                }
            }
        });
        context.setPacketHandled(true);
    }
    
    public double getGravityForce() {
        return gravityForce;
    }
}
