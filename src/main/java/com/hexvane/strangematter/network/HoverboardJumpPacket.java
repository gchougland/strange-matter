package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class HoverboardJumpPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "hoverboard_jump");
    public static final Type<HoverboardJumpPacket> TYPE = new Type<>(ID);
    
    private final int hoverboardId;
    
    public HoverboardJumpPacket(int hoverboardId) {
        this.hoverboardId = hoverboardId;
    }
    
    public HoverboardJumpPacket(FriendlyByteBuf buffer) {
        this.hoverboardId = buffer.readVarInt();
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.hoverboardId);
    }
    
    public static void handle(HoverboardJumpPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // Validate player is riding the hoverboard
                Entity vehicle = player.getVehicle();
                if (vehicle instanceof com.hexvane.strangematter.entity.HoverboardEntity hoverboard) {
                    if (vehicle.getId() == packet.hoverboardId) {
                        hoverboard.handleJumpRequest();
                    }
                }
            }
        });
    }
}

