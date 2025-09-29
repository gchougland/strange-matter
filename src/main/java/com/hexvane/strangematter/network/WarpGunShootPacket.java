package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import com.hexvane.strangematter.item.WarpGunItem;

import java.util.function.Supplier;

public class WarpGunShootPacket {
    private final boolean isPurple;
    
    public WarpGunShootPacket(boolean isPurple) {
        this.isPurple = isPurple;
    }
    
    public WarpGunShootPacket(FriendlyByteBuf buffer) {
        this.isPurple = buffer.readBoolean();
    }
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.isPurple);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && player.getMainHandItem().getItem() instanceof WarpGunItem) {
                WarpGunItem.shootProjectileStatic(player, this.isPurple);
            }
        });
        return true;
    }
}
