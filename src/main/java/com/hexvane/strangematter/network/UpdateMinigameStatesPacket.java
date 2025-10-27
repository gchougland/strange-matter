package com.hexvane.strangematter.network;

import com.hexvane.strangematter.block.ResearchMachineBlockEntity;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public class UpdateMinigameStatesPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "update_minigame_states");
    public static final Type<UpdateMinigameStatesPacket> TYPE = new Type<>(ID);
    
    private final BlockPos pos;
    private final Map<ResearchType, Boolean> minigameStates;
    
    public UpdateMinigameStatesPacket(BlockPos pos, Map<ResearchType, Boolean> minigameStates) {
        this.pos = pos;
        this.minigameStates = minigameStates;
    }
    
    public UpdateMinigameStatesPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        int size = buffer.readInt();
        Map<ResearchType, Boolean> minigameStates = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResearchType type = buffer.readEnum(ResearchType.class);
            boolean isStable = buffer.readBoolean();
            minigameStates.put(type, isStable);
        }
        this.minigameStates = minigameStates;
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(minigameStates.size());
        for (Map.Entry<ResearchType, Boolean> entry : minigameStates.entrySet()) {
            buffer.writeEnum(entry.getKey());
            buffer.writeBoolean(entry.getValue());
        }
    }
    
    public static void handle(UpdateMinigameStatesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                // Server side - update the research machine
                var player = context.player();
                var level = player.level();
                var blockEntity = level.getBlockEntity(packet.pos);
                
                if (blockEntity instanceof ResearchMachineBlockEntity researchMachine) {
                    // This packet is no longer used since we moved to client-side instability calculation
                    // Keeping the packet for potential future use
                }
            }
        });
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    public Map<ResearchType, Boolean> getMinigameStates() {
        return minigameStates;
    }
}
