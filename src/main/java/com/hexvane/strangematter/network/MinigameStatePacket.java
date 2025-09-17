package com.hexvane.strangematter.network;

import com.hexvane.strangematter.block.ResearchMachineBlockEntity;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MinigameStatePacket {
    private final BlockPos pos;
    private final Map<ResearchType, Map<String, Object>> minigameStates;
    
    public MinigameStatePacket(BlockPos pos, Map<ResearchType, Map<String, Object>> minigameStates) {
        this.pos = pos;
        this.minigameStates = minigameStates;
    }
    
    public static void encode(MinigameStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        
        // Write the number of research types
        buffer.writeInt(packet.minigameStates.size());
        
        for (Map.Entry<ResearchType, Map<String, Object>> entry : packet.minigameStates.entrySet()) {
            // Write research type name
            buffer.writeUtf(entry.getKey().name());
            
            // Write the state map
            Map<String, Object> state = entry.getValue();
            buffer.writeInt(state.size());
            
            for (Map.Entry<String, Object> stateEntry : state.entrySet()) {
                buffer.writeUtf(stateEntry.getKey());
                Object value = stateEntry.getValue();
                
                // Write value type and data
                if (value instanceof Boolean) {
                    buffer.writeUtf("boolean");
                    buffer.writeBoolean((Boolean) value);
                } else if (value instanceof String) {
                    buffer.writeUtf("string");
                    buffer.writeUtf((String) value);
                } else if (value instanceof Double) {
                    buffer.writeUtf("double");
                    buffer.writeDouble((Double) value);
                } else if (value instanceof Integer) {
                    buffer.writeUtf("int");
                    buffer.writeInt((Integer) value);
                } else {
                    buffer.writeUtf("string");
                    buffer.writeUtf(value.toString());
                }
            }
        }
    }
    
    public static MinigameStatePacket new_(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        
        Map<ResearchType, Map<String, Object>> minigameStates = new HashMap<>();
        
        int typeCount = buffer.readInt();
        for (int i = 0; i < typeCount; i++) {
            String typeName = buffer.readUtf();
            ResearchType type = ResearchType.valueOf(typeName);
            
            Map<String, Object> state = new HashMap<>();
            int stateCount = buffer.readInt();
            
            for (int j = 0; j < stateCount; j++) {
                String key = buffer.readUtf();
                String valueType = buffer.readUtf();
                
                Object value;
                switch (valueType) {
                    case "boolean":
                        value = buffer.readBoolean();
                        break;
                    case "string":
                        value = buffer.readUtf();
                        break;
                    case "double":
                        value = buffer.readDouble();
                        break;
                    case "int":
                        value = buffer.readInt();
                        break;
                    default:
                        value = buffer.readUtf();
                        break;
                }
                
                state.put(key, value);
            }
            
            minigameStates.put(type, state);
        }
        
        return new MinigameStatePacket(pos, minigameStates);
    }
    
    public static void handle(MinigameStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                // Server side - save minigame states to block entity
                var player = context.getSender();
                var level = player.level();
                var blockEntity = level.getBlockEntity(packet.pos);
                
                if (blockEntity instanceof ResearchMachineBlockEntity researchMachine) {
                    researchMachine.setMinigameStates(packet.minigameStates);
                }
            }
        });
        context.setPacketHandled(true);
    }
}

