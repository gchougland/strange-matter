package com.hexvane.strangematter.network;

import com.hexvane.strangematter.block.ResearchMachineBlockEntity;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ResearchMachineSyncPacket {
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
    
    public static void encode(ResearchMachineSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeEnum(packet.state);
        buffer.writeUtf(packet.researchId);
        buffer.writeInt(packet.activeTypes.size());
        for (ResearchType type : packet.activeTypes) {
            buffer.writeEnum(type);
        }
        buffer.writeFloat(packet.instabilityLevel);
        buffer.writeInt(packet.researchTicks);
    }
    
    public static ResearchMachineSyncPacket new_(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ResearchMachineBlockEntity.MachineState state = buffer.readEnum(ResearchMachineBlockEntity.MachineState.class);
        String researchId = buffer.readUtf();
        int typeCount = buffer.readInt();
        Set<ResearchType> activeTypes = new HashSet<>();
        for (int i = 0; i < typeCount; i++) {
            activeTypes.add(buffer.readEnum(ResearchType.class));
        }
        float instabilityLevel = buffer.readFloat();
        int researchTicks = buffer.readInt();
        return new ResearchMachineSyncPacket(pos, state, researchId, activeTypes, instabilityLevel, researchTicks);
    }
    
    public static void handle(ResearchMachineSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                // Server side - shouldn't happen
                return;
            }
            
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
        context.setPacketHandled(true);
    }
}
