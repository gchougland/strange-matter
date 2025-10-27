package com.hexvane.strangematter.network;

import com.hexvane.strangematter.block.ResearchMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ResearchCompletionPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "research_completion");
    public static final Type<ResearchCompletionPacket> TYPE = new Type<>(ID);
    
    private final BlockPos pos;
    private final boolean success;
    
    public ResearchCompletionPacket(BlockPos pos, boolean success) {
        this.pos = pos;
        this.success = success;
    }
    
    public ResearchCompletionPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.success = buffer.readBoolean();
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(success);
    }
    
    public static void handle(ResearchCompletionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                // Server side - handle research completion/failure
                var player = context.player();
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
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    public boolean isSuccess() {
        return success;
    }
}
