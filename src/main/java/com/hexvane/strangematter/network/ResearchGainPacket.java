package com.hexvane.strangematter.network;

import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.ClientPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class ResearchGainPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "research_gain");
    public static final Type<ResearchGainPacket> TYPE = new Type<>(ID);
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
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(researchType.getName());
        buf.writeInt(amount);
    }
    
    
    public static void handle(ResearchGainPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            com.hexvane.strangematter.network.ResearchDataClientHandler.handleResearchGain(packet.researchType, packet.amount);
        });
    }
}
