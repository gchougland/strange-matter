package com.hexvane.strangematter.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = "strangematter", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GravityEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GravityEventHandler.class);
    
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Check if player has gravity modification data
            if (player.getPersistentData().contains("strangematter.gravity_force")) {
                double forceMultiplier = player.getPersistentData().getDouble("strangematter.gravity_force");
                
                // FORCE the velocity change - this is the key
                Vec3 currentVelocity = player.getDeltaMovement();
                
                if (forceMultiplier > 0) {
                    // UPWARD LEVITATION (positive force)
                    
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
                    } else {
                        // Even when not falling, add upward force
                        double upwardForce = 0.1 * forceMultiplier;
                        Vec3 newVelocity = currentVelocity.add(0, upwardForce, 0);
                        player.setDeltaMovement(newVelocity);
                    }
                    
                } else if (forceMultiplier < 0) {
                    // DOWNWARD LEVITATION (negative force) - gentle lowering
                    double downwardForce = Math.abs(forceMultiplier) * 0.02; // Much gentler downward force
                    
                    // Only apply downward force if player is falling too fast or not falling at all
                    if (currentVelocity.y > -0.1) { // If not falling much or rising
                        Vec3 newVelocity = currentVelocity.add(0, -downwardForce, 0);
                        player.setDeltaMovement(newVelocity);
                    } else if (currentVelocity.y < -0.5) { // If falling too fast, slow it down
                        Vec3 newVelocity = currentVelocity.multiply(1.0, 0.3, 1.0); // Slow down falling
                        player.setDeltaMovement(newVelocity);
                    }
                }
                
                // Reduce fall distance to prevent fall damage
                if (player.fallDistance > 0) {
                    player.fallDistance *= (1.0f - (float)(Math.abs(forceMultiplier) * 0.8));
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
                float newDistance = originalDistance * (1.0f - (float)(Math.abs(forceMultiplier) * 0.8));
                event.setDistance(newDistance);
            }
        }
    }
}
