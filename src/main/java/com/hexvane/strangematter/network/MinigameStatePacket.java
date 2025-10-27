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

public class MinigameStatePacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "minigame_state");
    public static final Type<MinigameStatePacket> TYPE = new Type<>(ID);
    
    private final BlockPos pos;
    private final Map<ResearchType, Map<String, Object>> minigameStates;
    
    public MinigameStatePacket(BlockPos pos, Map<ResearchType, Map<String, Object>> minigameStates) {
        this.pos = pos;
        this.minigameStates = minigameStates;
    }
    
    public MinigameStatePacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        
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
        
        this.minigameStates = minigameStates;
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        
        // Write the number of research types
        buffer.writeInt(minigameStates.size());
        
        for (Map.Entry<ResearchType, Map<String, Object>> entry : minigameStates.entrySet()) {
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
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(MinigameStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                // Server side - save minigame states to block entity
                var player = context.player();
                var level = player.level();
                var blockEntity = level.getBlockEntity(packet.pos);
                
                if (blockEntity instanceof ResearchMachineBlockEntity researchMachine) {
                    researchMachine.setMinigameStates(packet.minigameStates);
                }
            }
        });
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    public Map<ResearchType, Map<String, Object>> getMinigameStates() {
        return minigameStates;
    }
}

