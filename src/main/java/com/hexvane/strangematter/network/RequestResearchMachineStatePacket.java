package com.hexvane.strangematter.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestResearchMachineStatePacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "request_research_machine_state");
    public static final Type<RequestResearchMachineStatePacket> TYPE = new Type<>(ID);
    
    private final BlockPos pos;
    
    public RequestResearchMachineStatePacket(BlockPos pos) {
        this.pos = pos;
    }
    
    public RequestResearchMachineStatePacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }
    
    public static void handle(RequestResearchMachineStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                // Server side - send current state back to client
                var player = context.player();
                var level = player.level();
                var blockEntity = level.getBlockEntity(packet.pos);
                
                if (blockEntity instanceof com.hexvane.strangematter.block.ResearchMachineBlockEntity researchMachine) {
                    // Trigger a sync packet to be sent to all players tracking this chunk
                    // This will update the client-side BlockEntity with the latest server state
                    researchMachine.sendSyncPacket();
                }
            }
        });
    }
    
    public BlockPos getPos() {
        return pos;
    }
}
