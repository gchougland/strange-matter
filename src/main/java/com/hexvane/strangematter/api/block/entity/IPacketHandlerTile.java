package com.hexvane.strangematter.api.block.entity;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Interface for block entities that can handle network packets.
 * Provides a standardized way for machines to send and receive data.
 */
public interface IPacketHandlerTile {

    // CONFIG
    default FriendlyByteBuf getConfigPacket(FriendlyByteBuf buffer) {
        return buffer;
    }

    default void handleConfigPacket(FriendlyByteBuf buffer) {
    }

    // CONTROL
    default FriendlyByteBuf getControlPacket(FriendlyByteBuf buffer) {
        return buffer;
    }

    default void handleControlPacket(FriendlyByteBuf buffer) {
    }

    // GUI
    default FriendlyByteBuf getGuiPacket(FriendlyByteBuf buffer) {
        return buffer;
    }

    default void handleGuiPacket(FriendlyByteBuf buffer) {
    }

    // REDSTONE
    default FriendlyByteBuf getRedstonePacket(FriendlyByteBuf buffer) {
        return buffer;
    }

    default void handleRedstonePacket(FriendlyByteBuf buffer) {
    }

    // STATE
    default FriendlyByteBuf getStatePacket(FriendlyByteBuf buffer) {
        return buffer;
    }

    default void handleStatePacket(FriendlyByteBuf buffer) {
    }

    // RENDER
    default FriendlyByteBuf getRenderPacket(FriendlyByteBuf buffer) {
        return buffer;
    }

    default void handleRenderPacket(FriendlyByteBuf buffer) {
    }
}

