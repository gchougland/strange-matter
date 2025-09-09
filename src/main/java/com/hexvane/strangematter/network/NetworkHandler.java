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
    }
}
