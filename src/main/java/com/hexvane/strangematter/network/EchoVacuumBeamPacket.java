package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.client.EchoVacuumClientHandler;

import java.util.function.Supplier;

public class EchoVacuumBeamPacket {
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
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.playerId);
        buffer.writeBoolean(this.isActive);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // For server-to-client packets, we need to get the client's level
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level != null) {
                net.minecraft.world.entity.Entity entity = minecraft.level.getEntity(this.playerId);
                if (entity instanceof Player player) {
                    // Store the beam state for this player
                    StrangeMatterMod.setPlayerBeamState(player, this.isActive);
                }
            }
        });
        return true;
    }
    
    public static void sendToNearbyPlayers(Player sourcePlayer, boolean isActive) {
        Level level = sourcePlayer.level();
        if (!level.isClientSide) {
            EchoVacuumBeamPacket packet = new EchoVacuumBeamPacket(sourcePlayer.getId(), isActive);
            NetworkHandler.INSTANCE.send(
                PacketDistributor.NEAR.with(
                    PacketDistributor.TargetPoint.p(
                        sourcePlayer.getX(), sourcePlayer.getY(), sourcePlayer.getZ(),
                        64.0, // 64 block radius
                        level.dimension()
                    )
                ),
                packet
            );
        }
    }
}
