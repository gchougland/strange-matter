package com.hexvane.strangematter.network;

import com.hexvane.strangematter.entity.HoverboardEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HoverboardJumpPacket {
    private final int entityId;
    
    public HoverboardJumpPacket(int entityId) {
        this.entityId = entityId;
    }
    
    public HoverboardJumpPacket(FriendlyByteBuf buffer) {
        this.entityId = buffer.readInt();
    }
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                // Server side - handle the jump
                var level = context.getSender().level();
                Entity entity = level.getEntity(this.entityId);
                
                if (entity instanceof HoverboardEntity hoverboard) {
                    // Check if the player is riding this hoverboard
                    if (hoverboard.isVehicle()) {
                        Entity controllingPassenger = hoverboard.getControllingPassenger();
                        if (controllingPassenger == null && !hoverboard.getPassengers().isEmpty()) {
                            controllingPassenger = hoverboard.getPassengers().get(0);
                        }
                        
                        if (controllingPassenger != null && controllingPassenger.getId() == context.getSender().getId()) {
                            // Player is riding, trigger jump
                            hoverboard.setJumpPressed(true);
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}

