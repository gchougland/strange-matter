package com.hexvane.strangematter.network;

import com.hexvane.strangematter.api.block.entity.IPacketHandlerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet for synchronizing machine state between server and client.
 */
public class MachineStatePacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "machine_state");
    public static final Type<MachineStatePacket> TYPE = new Type<>(ID);
    
    private final BlockPos pos;
    private final FriendlyByteBuf data;

    public MachineStatePacket(BlockPos pos, FriendlyByteBuf data) {
        this.pos = pos;
        this.data = data;
    }
    
    public MachineStatePacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        int dataLength = buffer.readableBytes();
        this.data = new FriendlyByteBuf(buffer.readBytes(dataLength));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        int readerIndex = this.data.readerIndex();
        this.data.readerIndex(0);
        buffer.writeBytes(this.data, this.data.readableBytes());
        this.data.readerIndex(readerIndex);
    }

    public static void handle(MachineStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Handle on client side
            Level world = context.player() != null ? context.player().level() : null;
            if (world != null) {
                BlockEntity blockEntity = world.getBlockEntity(packet.pos);
                if (blockEntity instanceof IPacketHandlerTile handlerTile) {
                    handlerTile.handleStatePacket(packet.data);
                }
            }
        });
    }

    public static void sendToClient(IPacketHandlerTile tile) {
        if (tile == null || !(tile instanceof BlockEntity blockEntity)) {
            return;
        }
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        
        // Create buffer for the data
        FriendlyByteBuf dataBuffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        tile.getStatePacket(dataBuffer);
        
        // Create the packet with the position and data buffer
        MachineStatePacket packet = new MachineStatePacket(blockEntity.getBlockPos(), dataBuffer);
        
        // Send packet to all players in the level
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (net.minecraft.server.level.ServerPlayer player : serverLevel.players()) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    public FriendlyByteBuf getData() {
        return data;
    }
}
