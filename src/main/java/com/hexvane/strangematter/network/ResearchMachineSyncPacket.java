package com.hexvane.strangematter.network;

import com.hexvane.strangematter.block.ResearchMachineBlockEntity;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public class ResearchMachineSyncPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "research_machine_sync");
    public static final Type<ResearchMachineSyncPacket> TYPE = new Type<>(ID);
    
    private final BlockPos pos;
    private final ResearchMachineBlockEntity.MachineState state;
    private final String researchId;
    private final Set<ResearchType> activeTypes;
    private final float instabilityLevel;
    private final int researchTicks;
    
    public ResearchMachineSyncPacket(BlockPos pos, ResearchMachineBlockEntity.MachineState state, 
                                   String researchId, Set<ResearchType> activeTypes, 
                                   float instabilityLevel, int researchTicks) {
        this.pos = pos;
        this.state = state;
        this.researchId = researchId;
        this.activeTypes = activeTypes;
        this.instabilityLevel = instabilityLevel;
        this.researchTicks = researchTicks;
    }
    
    public ResearchMachineSyncPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.state = buffer.readEnum(ResearchMachineBlockEntity.MachineState.class);
        this.researchId = buffer.readUtf();
        int typeCount = buffer.readInt();
        Set<ResearchType> activeTypes = new HashSet<>();
        for (int i = 0; i < typeCount; i++) {
            activeTypes.add(buffer.readEnum(ResearchType.class));
        }
        this.activeTypes = activeTypes;
        this.instabilityLevel = buffer.readFloat();
        this.researchTicks = buffer.readInt();
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeEnum(state);
        buffer.writeUtf(researchId);
        buffer.writeInt(activeTypes.size());
        for (ResearchType type : activeTypes) {
            buffer.writeEnum(type);
        }
        buffer.writeFloat(instabilityLevel);
        buffer.writeInt(researchTicks);
    }
    
    public static void handle(ResearchMachineSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Client side - update the block entity
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level != null) {
                var blockEntity = minecraft.level.getBlockEntity(packet.pos);
                if (blockEntity instanceof ResearchMachineBlockEntity researchMachine) {
                    researchMachine.setClientState(packet.state, packet.researchId, packet.activeTypes, 
                                                 packet.instabilityLevel, packet.researchTicks);
                }
            }
        });
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    public ResearchMachineBlockEntity.MachineState getState() {
        return state;
    }
    
    public String getResearchId() {
        return researchId;
    }
    
    public Set<ResearchType> getActiveTypes() {
        return activeTypes;
    }
    
    public float getInstabilityLevel() {
        return instabilityLevel;
    }
    
    public int getResearchTicks() {
        return researchTicks;
    }
}
