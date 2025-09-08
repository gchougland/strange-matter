package com.hexvane.strangematter.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GravityEventHandler {
    
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Check if player has gravity modification data
            if (player.getPersistentData().contains("strangematter.gravity_force")) {
                double forceMultiplier = player.getPersistentData().getDouble("strangematter.gravity_force");
                
                // FORCE the velocity change - this is the key
                Vec3 currentVelocity = player.getDeltaMovement();
                
                // Completely override gravity when falling
                if (currentVelocity.y < 0) {
                    // Reduce gravity dramatically
                    double gravityReduction = 1.0 - (forceMultiplier * 0.9); // 0.1 to 1.0 (90% reduction at max)
                    Vec3 newVelocity = new Vec3(currentVelocity.x, currentVelocity.y * gravityReduction, currentVelocity.z);
                    
                    // Add strong upward force
                    double upwardForce = 0.15 * forceMultiplier;
                    newVelocity = newVelocity.add(0, upwardForce, 0);
                    
                    // FORCE the new velocity
                    player.setDeltaMovement(newVelocity);
                    
                }
                
                // Reduce fall distance to prevent fall damage
                if (player.fallDistance > 0) {
                    player.fallDistance *= (1.0f - (float)(forceMultiplier * 0.8));
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Check if player has gravity modification data
            if (player.getPersistentData().contains("strangematter.gravity_force")) {
                double forceMultiplier = player.getPersistentData().getDouble("strangematter.gravity_force");
                
                // Reduce fall damage based on gravity force
                float originalDistance = event.getDistance();
                float newDistance = originalDistance * (1.0f - (float)(forceMultiplier * 0.8));
                event.setDistance(newDistance);
                
            }
        }
    }
}
