package com.hexvane.strangematter.network;

import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResearchGainPacket {
    private final ResearchType researchType;
    private final int amount;
    
    public ResearchGainPacket(ResearchType researchType, int amount) {
        this.researchType = researchType;
        this.amount = amount;
    }
    
    public ResearchGainPacket(FriendlyByteBuf buf) {
        this.researchType = ResearchType.fromName(buf.readUtf());
        this.amount = buf.readInt();
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(researchType.getName());
        buf.writeInt(amount);
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // This packet is handled on the client side
            if (context.getDirection().getReceptionSide().isClient()) {
                com.hexvane.strangematter.network.ResearchDataClientHandler.handleResearchGain(researchType, amount);
            }
        });
        context.setPacketHandled(true);
    }
}
