package com.hexvane.strangematter.network;

import com.hexvane.strangematter.block.ResearchMachineBlockEntity;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class UpdateMinigameStatesPacket {
    private final BlockPos pos;
    private final Map<ResearchType, Boolean> minigameStates;
    
    public UpdateMinigameStatesPacket(BlockPos pos, Map<ResearchType, Boolean> minigameStates) {
        this.pos = pos;
        this.minigameStates = minigameStates;
    }
    
    public static void encode(UpdateMinigameStatesPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.minigameStates.size());
        for (Map.Entry<ResearchType, Boolean> entry : packet.minigameStates.entrySet()) {
            buffer.writeEnum(entry.getKey());
            buffer.writeBoolean(entry.getValue());
        }
    }
    
    public static UpdateMinigameStatesPacket new_(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int size = buffer.readInt();
        Map<ResearchType, Boolean> minigameStates = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResearchType type = buffer.readEnum(ResearchType.class);
            boolean isStable = buffer.readBoolean();
            minigameStates.put(type, isStable);
        }
        return new UpdateMinigameStatesPacket(pos, minigameStates);
    }
    
    public static void handle(UpdateMinigameStatesPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                // Server side - update the research machine
                var player = context.getSender();
                var level = player.level();
                var blockEntity = level.getBlockEntity(packet.pos);
                
                if (blockEntity instanceof ResearchMachineBlockEntity researchMachine) {
                    // This packet is no longer used since we moved to client-side instability calculation
                    // Keeping the packet for potential future use
                }
            }
        });
        context.setPacketHandled(true);
    }
}
