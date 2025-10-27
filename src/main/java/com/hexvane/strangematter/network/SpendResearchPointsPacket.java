package com.hexvane.strangematter.network;

import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public class SpendResearchPointsPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "spend_research_points");
    public static final Type<SpendResearchPointsPacket> TYPE = new Type<>(ID);
    
    private final Map<ResearchType, Integer> costs;
    private final String researchId;
    
    public SpendResearchPointsPacket(Map<ResearchType, Integer> costs, String researchId) {
        this.costs = costs;
        this.researchId = researchId;
    }
    
    public SpendResearchPointsPacket(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        Map<ResearchType, Integer> costs = new java.util.HashMap<>();
        for (int i = 0; i < size; i++) {
            ResearchType type = buffer.readEnum(ResearchType.class);
            int amount = buffer.readInt();
            costs.put(type, amount);
        }
        String researchId = buffer.readUtf();
        this.costs = costs;
        this.researchId = researchId;
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(costs.size());
        for (Map.Entry<ResearchType, Integer> entry : costs.entrySet()) {
            buffer.writeEnum(entry.getKey());
            buffer.writeInt(entry.getValue());
        }
        buffer.writeUtf(researchId);
    }
    
    public static void handle(SpendResearchPointsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                // Server side - spend the research points
                var player = context.player();
                com.hexvane.strangematter.research.ResearchData researchData = 
                    com.hexvane.strangematter.research.ResearchData.get(player);
                
                // Check if player has enough points
                boolean canAfford = true;
                for (Map.Entry<ResearchType, Integer> entry : packet.costs.entrySet()) {
                    if (researchData.getResearchPoints(entry.getKey()) < entry.getValue()) {
                        canAfford = false;
                        break;
                    }
                }
                
                if (canAfford) {
                    // Spend the points
                    for (Map.Entry<ResearchType, Integer> entry : packet.costs.entrySet()) {
                        researchData.spendResearchPoints(entry.getKey(), entry.getValue());
                    }
                    
                    // Create and give research note on server side
                    net.minecraft.world.item.ItemStack researchNote = 
                        com.hexvane.strangematter.item.ResearchNoteItem.createResearchNote(packet.costs, packet.researchId);
                    
                    if (player.getInventory().add(researchNote)) {
                        // Sync to client
                        researchData.syncToClient((ServerPlayer) player);
                        
                        // Send success message
                        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                            "research.strangematter.note_received", 
                            com.hexvane.strangematter.research.ResearchNodeRegistry.getNode(packet.researchId).getDisplayName()));
                    } else {
                        // Inventory full - refund the points
                        for (Map.Entry<ResearchType, Integer> entry : packet.costs.entrySet()) {
                            researchData.addResearchPoints(entry.getKey(), entry.getValue());
                        }
                        researchData.syncToClient((ServerPlayer) player);
                        
                        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                            "research.strangematter.inventory_full"));
                    }
                } else {
                    // Send failure message
                    player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                        "research.strangematter.cannot_afford"));
                }
            }
        });
    }
    
    public Map<ResearchType, Integer> getCosts() {
        return costs;
    }
    
    public String getResearchId() {
        return researchId;
    }
}
