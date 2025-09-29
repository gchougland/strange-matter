package com.hexvane.strangematter.network;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    
    private static int packetId = 0;
    
    public static void register() {
        INSTANCE.registerMessage(packetId++, ResearchSyncPacket.class,
            ResearchSyncPacket::encode,
            ResearchSyncPacket::new,
            ResearchSyncPacket::handle);
            
        INSTANCE.registerMessage(packetId++, ResearchGainPacket.class,
            ResearchGainPacket::encode,
            ResearchGainPacket::new,
            ResearchGainPacket::handle);
            
        INSTANCE.registerMessage(packetId++, ResearchMachineSyncPacket.class,
            ResearchMachineSyncPacket::encode,
            ResearchMachineSyncPacket::new_,
            ResearchMachineSyncPacket::handle);
            
        INSTANCE.registerMessage(packetId++, SpendResearchPointsPacket.class,
            SpendResearchPointsPacket::encode,
            SpendResearchPointsPacket::new_,
            SpendResearchPointsPacket::handle);
            
        INSTANCE.registerMessage(packetId++, UpdateMinigameStatesPacket.class,
            UpdateMinigameStatesPacket::encode,
            UpdateMinigameStatesPacket::new_,
            UpdateMinigameStatesPacket::handle);
            
        INSTANCE.registerMessage(packetId++, ResearchCompletionPacket.class,
            ResearchCompletionPacket::encode,
            ResearchCompletionPacket::new_,
            ResearchCompletionPacket::handle);
            
        INSTANCE.registerMessage(packetId++, MinigameStatePacket.class,
            MinigameStatePacket::encode,
            MinigameStatePacket::new_,
            MinigameStatePacket::handle);
            
        INSTANCE.registerMessage(packetId++, RequestResearchMachineStatePacket.class,
            RequestResearchMachineStatePacket::encode,
            RequestResearchMachineStatePacket::decode,
            RequestResearchMachineStatePacket::handle);
            
        INSTANCE.registerMessage(packetId++, WarpGunShootPacket.class,
            WarpGunShootPacket::encode,
            WarpGunShootPacket::new,
            WarpGunShootPacket::handle);
    }
}
