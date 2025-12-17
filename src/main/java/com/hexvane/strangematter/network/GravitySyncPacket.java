package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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
            if (context.getSender() != null) {
                // Server-side handling (shouldn't happen with our current setup)
                context.getSender().getPersistentData().putDouble("strangematter.gravity_force", this.gravityForce);
                return;
            }
            if (context.getDirection().getReceptionSide().isClient()) {
                double force = this.gravityForce;
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.hexvane.strangematter.client.network.ClientPacketHandlers.handleGravitySync(force));
            }
        });
        context.setPacketHandled(true);
    }
    
    public double getGravityForce() {
        return gravityForce;
    }
}
