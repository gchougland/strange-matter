package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SpendResearchPointsPacket {
    private final Map<String, Integer> costs;
    private final String researchId;
    private final boolean instantUnlock;

    /** @param instantUnlock if true, unlock research immediately and do not create a research note (for nodes that use only custom point types). */
    public SpendResearchPointsPacket(Map<String, Integer> costs, String researchId, boolean instantUnlock) {
        this.costs = costs;
        this.researchId = researchId;
        this.instantUnlock = instantUnlock;
    }

    public SpendResearchPointsPacket(Map<String, Integer> costs, String researchId) {
        this(costs, researchId, false);
    }

    public static void encode(SpendResearchPointsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.costs.size());
        for (Map.Entry<String, Integer> entry : packet.costs.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeInt(entry.getValue());
        }
        buffer.writeUtf(packet.researchId);
        buffer.writeBoolean(packet.instantUnlock);
    }

    public static SpendResearchPointsPacket new_(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        Map<String, Integer> costs = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String typeId = buffer.readUtf();
            int amount = buffer.readInt();
            costs.put(typeId, amount);
        }
        String researchId = buffer.readUtf();
        boolean instantUnlock = buffer.readableBytes() >= 1 && buffer.readBoolean();
        return new SpendResearchPointsPacket(costs, researchId, instantUnlock);
    }
    
    public static void handle(SpendResearchPointsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                var player = context.getSender();
                com.hexvane.strangematter.research.ResearchData researchData = 
                    com.hexvane.strangematter.research.ResearchData.get(player);
                
                boolean canAfford = true;
                for (Map.Entry<String, Integer> entry : packet.costs.entrySet()) {
                    if (researchData.getResearchPoints(entry.getKey()) < entry.getValue()) {
                        canAfford = false;
                        break;
                    }
                }
                
                if (canAfford) {
                    for (Map.Entry<String, Integer> entry : packet.costs.entrySet()) {
                        researchData.spendResearchPoints(entry.getKey(), entry.getValue());
                    }

                    if (packet.instantUnlock) {
                        researchData.unlockResearch(packet.researchId);
                        researchData.syncToClient(player);
                        com.hexvane.strangematter.research.ResearchNode node =
                            com.hexvane.strangematter.research.ResearchNodeRegistry.getNode(packet.researchId);
                        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                            "research.strangematter.research_unlocked", node != null ? node.getDisplayName() : net.minecraft.network.chat.Component.literal(packet.researchId)));
                    } else {
                        net.minecraft.world.item.ItemStack researchNote =
                            com.hexvane.strangematter.item.ResearchNoteItem.createResearchNote(packet.costs, packet.researchId);

                        if (player.getInventory().add(researchNote)) {
                            researchData.syncToClient(player);
                            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                                "research.strangematter.note_received",
                                com.hexvane.strangematter.research.ResearchNodeRegistry.getNode(packet.researchId).getDisplayName()));
                        } else {
                            for (Map.Entry<String, Integer> entry : packet.costs.entrySet()) {
                                researchData.addResearchPoints(entry.getKey(), entry.getValue());
                            }
                            researchData.syncToClient(player);
                            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                                "research.strangematter.inventory_full"));
                        }
                    }
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                        "research.strangematter.cannot_afford"));
                }
            }
        });
        context.setPacketHandled(true);
    }
}
