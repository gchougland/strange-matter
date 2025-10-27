package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.hexvane.strangematter.item.WarpGunItem;

import java.util.function.Supplier;

public class WarpGunShootPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "warp_gun_shoot");
    public static final Type<WarpGunShootPacket> TYPE = new Type<>(ID);
    private final boolean isPurple;
    
    public WarpGunShootPacket(boolean isPurple) {
        this.isPurple = isPurple;
    }
    
    public WarpGunShootPacket(FriendlyByteBuf buffer) {
        this.isPurple = buffer.readBoolean();
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.isPurple);
    }
    
    public static void handle(WarpGunShootPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.getMainHandItem().getItem() instanceof WarpGunItem) {
                    WarpGunItem.shootProjectileStatic(player, packet.isPurple);
                }
            }
        });
    }
}
