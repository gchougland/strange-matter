package com.hexvane.strangematter.network;

import com.hexvane.strangematter.research.ResearchData;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResearchSyncPacket {
    private final ResearchData researchData;
    
    public ResearchSyncPacket(ResearchData researchData) {
        this.researchData = researchData;
    }
    
    public ResearchSyncPacket(FriendlyByteBuf buf) {
        this.researchData = new ResearchData();
        this.researchData.deserializeNBT(buf.readNbt());
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(researchData.serializeNBT());
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // This packet is handled on the client side
            if (context.getDirection().getReceptionSide().isClient()) {
                ResearchData finalData = researchData;
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> 
                    com.hexvane.strangematter.client.network.ClientPacketHandlers.handleResearchSync(finalData));
            }
        });
        context.setPacketHandled(true);
    }
}
