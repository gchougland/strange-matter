package com.hexvane.strangematter.network;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final PayloadRegistrar INSTANCE = new PayloadRegistrar(PROTOCOL_VERSION);
    
    
    private static int packetId = 0;
    
    public static void register() {
        // Register all packets using NeoForge 1.21.1 network system
        
        // Research-related packets
        INSTANCE.playToClient(ResearchSyncPacket.TYPE, 
            CustomPacketPayload.codec(ResearchSyncPacket::write, ResearchSyncPacket::new), 
            ResearchSyncPacket::handle);
            
        INSTANCE.playToClient(ResearchGainPacket.TYPE, 
            CustomPacketPayload.codec(ResearchGainPacket::write, ResearchGainPacket::new), 
            ResearchGainPacket::handle);
            
        INSTANCE.playToServer(SpendResearchPointsPacket.TYPE, 
            CustomPacketPayload.codec(SpendResearchPointsPacket::write, SpendResearchPointsPacket::new), 
            SpendResearchPointsPacket::handle);
            
        INSTANCE.playToServer(RequestResearchMachineStatePacket.TYPE, 
            CustomPacketPayload.codec(RequestResearchMachineStatePacket::write, RequestResearchMachineStatePacket::new), 
            RequestResearchMachineStatePacket::handle);
            
        INSTANCE.playToClient(ResearchMachineSyncPacket.TYPE, 
            CustomPacketPayload.codec(ResearchMachineSyncPacket::write, ResearchMachineSyncPacket::new), 
            ResearchMachineSyncPacket::handle);
            
        INSTANCE.playToServer(MinigameStatePacket.TYPE, 
            CustomPacketPayload.codec(MinigameStatePacket::write, MinigameStatePacket::new), 
            MinigameStatePacket::handle);
            
        INSTANCE.playToServer(ResearchCompletionPacket.TYPE, 
            CustomPacketPayload.codec(ResearchCompletionPacket::write, ResearchCompletionPacket::new), 
            ResearchCompletionPacket::handle);
            
        INSTANCE.playToServer(UpdateMinigameStatesPacket.TYPE, 
            CustomPacketPayload.codec(UpdateMinigameStatesPacket::write, UpdateMinigameStatesPacket::new), 
            UpdateMinigameStatesPacket::handle);
        
        // Gravity and physics packets
        INSTANCE.playToClient(GravitySyncPacket.TYPE, 
            CustomPacketPayload.codec(GravitySyncPacket::write, GravitySyncPacket::new), 
            GravitySyncPacket::handle);
        
        // Player morphing packets
        INSTANCE.playToClient(PlayerMorphSyncPacket.TYPE, 
            CustomPacketPayload.codec(PlayerMorphSyncPacket::write, PlayerMorphSyncPacket::new), 
            PlayerMorphSyncPacket::handle);
            
        INSTANCE.playToClient(MobDisguiseSyncPacket.TYPE, 
            CustomPacketPayload.codec(MobDisguiseSyncPacket::write, MobDisguiseSyncPacket::new), 
            MobDisguiseSyncPacket::handle);
        
        // Item and tool packets
        INSTANCE.playToServer(WarpGunShootPacket.TYPE, 
            CustomPacketPayload.codec(WarpGunShootPacket::write, WarpGunShootPacket::new), 
            WarpGunShootPacket::handle);
            
        INSTANCE.playToClient(EchoVacuumBeamPacket.TYPE, 
            CustomPacketPayload.codec(EchoVacuumBeamPacket::write, EchoVacuumBeamPacket::new), 
            EchoVacuumBeamPacket::handle);
        
        // Machine and block entity packets
        INSTANCE.playToClient(MachineStatePacket.TYPE, 
            CustomPacketPayload.codec(MachineStatePacket::write, MachineStatePacket::new), 
            MachineStatePacket::handle);
            
        INSTANCE.playToServer(EjectShardsPacket.TYPE, 
            CustomPacketPayload.codec(EjectShardsPacket::write, EjectShardsPacket::new), 
            EjectShardsPacket::handle);
            
        // Hoverboard jump packet
        INSTANCE.playToServer(HoverboardJumpPacket.TYPE, 
            CustomPacketPayload.codec(HoverboardJumpPacket::write, HoverboardJumpPacket::new), 
            HoverboardJumpPacket::handle);
            
    }
}
