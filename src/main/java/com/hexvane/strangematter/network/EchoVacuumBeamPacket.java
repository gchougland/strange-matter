package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.hexvane.strangematter.StrangeMatterMod;

public class EchoVacuumBeamPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "echo_vacuum_beam");
    public static final Type<EchoVacuumBeamPacket> TYPE = new Type<>(ID);
    
    private final int playerId;
    private final boolean isActive;
    
    public EchoVacuumBeamPacket(int playerId, boolean isActive) {
        this.playerId = playerId;
        this.isActive = isActive;
    }
    
    public EchoVacuumBeamPacket(FriendlyByteBuf buffer) {
        this.playerId = buffer.readInt();
        this.isActive = buffer.readBoolean();
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.playerId);
        buffer.writeBoolean(this.isActive);
    }
    
    public static void handle(EchoVacuumBeamPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // For server-to-client packets, we need to get the client's level
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level != null) {
                net.minecraft.world.entity.Entity entity = minecraft.level.getEntity(packet.playerId);
                if (entity instanceof Player player) {
                    // Store the beam state for this player
                    StrangeMatterMod.setPlayerBeamState(player, packet.isActive);
                }
            }
        });
    }
    
    public static void sendToNearbyPlayers(Player sourcePlayer, boolean isActive) {
        Level level = sourcePlayer.level();
        if (!level.isClientSide) {
            EchoVacuumBeamPacket packet = new EchoVacuumBeamPacket(sourcePlayer.getId(), isActive);
            
            // Send packet to all players in the level
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                for (net.minecraft.server.level.ServerPlayer player : serverLevel.players()) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
                }
            }
        }
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public boolean isActive() {
        return isActive;
    }
}
