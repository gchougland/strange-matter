package com.hexvane.strangematter.network;

import com.hexvane.strangematter.research.ResearchData;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.ClientPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class ResearchSyncPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "research_sync");
    public static final Type<ResearchSyncPacket> TYPE = new Type<>(ID);
    
    private final ResearchData researchData;
    
    public ResearchSyncPacket(ResearchData researchData) {
        this.researchData = researchData;
    }
    
    public ResearchSyncPacket(FriendlyByteBuf buf) {
        this.researchData = new ResearchData();
        this.researchData.deserializeNBT(buf.readNbt());
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(researchData.serializeNBT());
    }
    
    
    public static void handle(ResearchSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Update client-side research data
            com.hexvane.strangematter.network.ResearchDataClientHandler.handleResearchSync(packet.researchData);
        });
    }
}
