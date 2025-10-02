package com.hexvane.strangematter.network;

import com.hexvane.strangematter.block.RealityForgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EjectShardsPacket {
    private final BlockPos pos;
    
    public EjectShardsPacket(BlockPos pos) {
        this.pos = pos;
    }
    
    public EjectShardsPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    BlockEntity blockEntity = player.level().getBlockEntity(pos);
                    if (blockEntity instanceof RealityForgeBlockEntity realityForge) {
                        realityForge.ejectShards(player);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
