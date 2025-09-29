package com.hexvane.strangematter.network;

import com.hexvane.strangematter.api.block.entity.IPacketHandlerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for synchronizing machine state between server and client.
 */
public class MachineStatePacket {
    private final BlockPos pos;
    private final FriendlyByteBuf data;

    public MachineStatePacket(BlockPos pos, FriendlyByteBuf data) {
        this.pos = pos;
        this.data = data;
    }

    public static void encode(MachineStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBytes(packet.data);
    }

    public static MachineStatePacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        FriendlyByteBuf data = new FriendlyByteBuf(buffer.readBytes(buffer.readableBytes()));
        return new MachineStatePacket(pos, data);
    }

    public static void handle(MachineStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Handle on client side
            Level world = context.getSender() != null ? context.getSender().level() : null;
            if (world != null) {
                BlockEntity blockEntity = world.getBlockEntity(packet.pos);
                if (blockEntity instanceof IPacketHandlerTile handlerTile) {
                    handlerTile.handleStatePacket(packet.data);
                }
            }
        });
        context.setPacketHandled(true);
    }

    public static void sendToClient(IPacketHandlerTile tile) {
        if (tile == null || !(tile instanceof BlockEntity blockEntity)) {
            return;
        }
        if (blockEntity.getLevel() == null || blockEntity.getLevel().isClientSide) {
            return;
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        MachineStatePacket packet = new MachineStatePacket(blockEntity.getBlockPos(), tile.getStatePacket(buffer));
        // TODO: Register and send packet through Forge network system
    }
}
