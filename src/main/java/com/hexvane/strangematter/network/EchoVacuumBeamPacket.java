package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import com.hexvane.strangematter.StrangeMatterMod;

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
            if (!context.get().getDirection().getReceptionSide().isClient()) return;

            int id = this.playerId;
            boolean active = this.isActive;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hexvane.strangematter.client.network.ClientPacketHandlers.handleEchoVacuumBeam(id, active)
            );
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
