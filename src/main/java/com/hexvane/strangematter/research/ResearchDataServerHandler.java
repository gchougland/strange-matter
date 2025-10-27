package com.hexvane.strangematter.research;

import com.hexvane.strangematter.network.NetworkHandler;
import com.hexvane.strangematter.network.ResearchSyncPacket;
import net.minecraft.server.level.ServerPlayer;

public class ResearchDataServerHandler {
    public static void syncResearchDataToClient(ServerPlayer player, ResearchData data) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new ResearchSyncPacket(data));
    }
}
