package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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
            if (context.getDirection().getReceptionSide().isClient()) {
                double factor = this.slowdownFactor;
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.hexvane.strangematter.client.network.ClientPacketHandlers.handleTimeDilationSync(factor));
            }
        });
        context.setPacketHandled(true);
    }
    
    public double getSlowdownFactor() {
        return slowdownFactor;
    }
}

