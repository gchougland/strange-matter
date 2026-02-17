package com.hexvane.strangematter.network;

import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResearchGainPacket {
    private final String typeId;
    private final int amount;
    
    public ResearchGainPacket(String typeId, int amount) {
        this.typeId = typeId;
        this.amount = amount;
    }
    
    public ResearchGainPacket(ResearchType type, int amount) {
        this(type.getName(), amount);
    }
    
    public ResearchGainPacket(FriendlyByteBuf buf) {
        this.typeId = buf.readUtf();
        this.amount = buf.readInt();
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(typeId);
        buf.writeInt(amount);
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                String finalTypeId = typeId;
                int finalAmount = amount;
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> 
                    com.hexvane.strangematter.client.network.ClientPacketHandlers.handleResearchGain(finalTypeId, finalAmount));
            }
        });
        context.setPacketHandled(true);
    }
}
