package com.hexvane.strangematter.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestResearchMachineStatePacket {
    private final BlockPos pos;
    
    public RequestResearchMachineStatePacket(BlockPos pos) {
        this.pos = pos;
    }
    
    public static void encode(RequestResearchMachineStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }
    
    public static RequestResearchMachineStatePacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        return new RequestResearchMachineStatePacket(pos);
    }
    
    public static void handle(RequestResearchMachineStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                // Server side - send current state back to client
                var player = context.getSender();
                var level = player.level();
                var blockEntity = level.getBlockEntity(packet.pos);
                
                if (blockEntity instanceof com.hexvane.strangematter.block.ResearchMachineBlockEntity researchMachine) {
                    // Trigger a sync packet to be sent to all players tracking this chunk
                    // This will update the client-side BlockEntity with the latest server state
                    researchMachine.sendSyncPacket();
                }
            }
        });
        context.setPacketHandled(true);
    }
}
