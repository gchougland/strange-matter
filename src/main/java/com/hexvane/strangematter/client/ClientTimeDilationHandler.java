package com.hexvane.strangematter.client;

import com.hexvane.strangematter.TimeDilationData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientTimeDilationHandler {
    
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player && player == Minecraft.getInstance().player) {
            // Check if player has time dilation slowdown data using static data
            if (TimeDilationData.hasPlayerSlowdownFactor(player.getUUID())) {
                double slowdownFactor = TimeDilationData.getPlayerSlowdownFactor(player.getUUID());
                
                // Apply slowdown on CLIENT SIDE
                Vec3 currentVelocity = player.getDeltaMovement();
                
                // Only apply if player has movement
                if (currentVelocity.lengthSqr() > 0.0001) {
                    Vec3 slowedVelocity = currentVelocity.scale(slowdownFactor);
                    player.setDeltaMovement(slowedVelocity);
                }
            }
        }
    }
}

