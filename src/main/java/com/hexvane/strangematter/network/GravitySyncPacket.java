package com.hexvane.strangematter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.ClientPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class GravitySyncPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "gravity_sync");
    public static final Type<GravitySyncPacket> TYPE = new Type<>(ID);
    private final double gravityForce;
    
    public GravitySyncPacket(double gravityForce) {
        this.gravityForce = gravityForce;
    }
    
    public GravitySyncPacket(FriendlyByteBuf buffer) {
        this.gravityForce = buffer.readDouble();
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeDouble(this.gravityForce);
    }
    
    public static void handle(GravitySyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().getPersistentData().putDouble("strangematter.gravity_force", packet.gravityForce);
        });
    }
    
    public double getGravityForce() {
        return gravityForce;
    }
}
