package com.hexvane.strangematter.network;

import com.hexvane.strangematter.block.ResearchMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResearchCompletionPacket {
    private final BlockPos pos;
    private final boolean success;
    
    public ResearchCompletionPacket(BlockPos pos, boolean success) {
        this.pos = pos;
        this.success = success;
    }
    
    public static void encode(ResearchCompletionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.success);
    }
    
    public static ResearchCompletionPacket new_(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        boolean success = buffer.readBoolean();
        return new ResearchCompletionPacket(pos, success);
    }
    
    public static void handle(ResearchCompletionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                // Server side - handle research completion/failure
                var player = context.getSender();
                var level = player.level();
                var blockEntity = level.getBlockEntity(packet.pos);
                
                if (blockEntity instanceof ResearchMachineBlockEntity researchMachine) {
                    if (packet.success) {
                        researchMachine.handleResearchCompletion();
                    } else {
                        researchMachine.handleResearchFailure();
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
