package com.hexvane.strangematter.network;

import com.hexvane.strangematter.block.RealityForgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EjectShardsPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "eject_shards");
    public static final Type<EjectShardsPacket> TYPE = new Type<>(ID);
    
    private final BlockPos pos;
    
    public EjectShardsPacket(BlockPos pos) {
        this.pos = pos;
    }
    
    public EjectShardsPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }
    
    public static void handle(EjectShardsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
                if (blockEntity instanceof RealityForgeBlockEntity realityForge) {
                    realityForge.ejectShards(player);
                }
            }
        });
    }
    
    public BlockPos getPos() {
        return pos;
    }
}
