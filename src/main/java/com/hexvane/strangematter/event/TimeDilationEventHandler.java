package com.hexvane.strangematter.event;

import com.hexvane.strangematter.TimeDilationData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "strangematter", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TimeDilationEventHandler {
    
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();
        
        if (entity instanceof Player player) {
            // Check if player has time dilation slowdown data
            // Works for both creative and survival players
            if (player.getPersistentData().contains("strangematter.time_dilation_factor")) {
                double slowdownFactor = player.getPersistentData().getDouble("strangematter.time_dilation_factor");
                
                // Only apply if factor is less than 1.0 (slowed)
                if (slowdownFactor < 1.0) {
                    // Apply slowdown to player movement on SERVER SIDE
                    Vec3 currentVelocity = player.getDeltaMovement();
                    
                    // Always apply slowdown if factor is set (even for small movements)
                    Vec3 slowedVelocity = currentVelocity.scale(slowdownFactor);
                    player.setDeltaMovement(slowedVelocity);
                }
            }
        }
        // Arrows and items are handled directly in TimeDilationBlockEntity.tick()
        // because they are not LivingEntity and don't trigger LivingEvent
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        
        // Clear time dilation data when player leaves the world
        if (player.getPersistentData().contains("strangematter.time_dilation_factor")) {
            TimeDilationData.removePlayerSlowdownFactor(player.getUUID());
            player.getPersistentData().remove("strangematter.time_dilation_factor");
        }
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        
        // Clear time dilation data when player changes dimensions
        if (player.getPersistentData().contains("strangematter.time_dilation_factor")) {
            TimeDilationData.removePlayerSlowdownFactor(player.getUUID());
            player.getPersistentData().remove("strangematter.time_dilation_factor");
        }
    }
}

