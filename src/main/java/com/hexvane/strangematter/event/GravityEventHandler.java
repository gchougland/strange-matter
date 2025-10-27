package com.hexvane.strangematter.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
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
            // Check if player was riding a hoverboard and just got off
            boolean wasRidingHoverboard = player.getPersistentData().getBoolean("strangematter.was_riding_hoverboard");
            boolean currentlyRidingHoverboard = player.isPassenger() && 
                player.getVehicle() != null && 
                player.getVehicle().getClass().getSimpleName().equals("HoverboardEntity");
            
            if (!wasRidingHoverboard && currentlyRidingHoverboard) {
                // Just got on the hoverboard
                player.getPersistentData().putBoolean("strangematter.was_riding_hoverboard", true);
            } else if (wasRidingHoverboard && !currentlyRidingHoverboard && !player.level().isClientSide) {
                // Just got off the hoverboard - reset fall distance to prevent accumulated damage
                player.fallDistance = 0.0f;
                player.getPersistentData().putBoolean("strangematter.was_riding_hoverboard", false);
            }
            
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
            // Check if player is riding a hoverboard - completely prevent fall damage
            if (player.isPassenger() && player.getVehicle() != null) {
                String vehicleClassName = player.getVehicle().getClass().getSimpleName();
                if (vehicleClassName.equals("HoverboardEntity")) {
                    event.setCanceled(true);
                    return;
                }
            }
            
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
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        
        // Clear gravity data when player leaves the world
        if (player.getPersistentData().contains("strangematter.gravity_force")) {
            // Remove from static data
            com.hexvane.strangematter.GravityData.removePlayerGravityForce(player.getUUID());
            
            // Clear persistent data
            player.getPersistentData().remove("strangematter.gravity_force");
            
            LOGGER.info("[GRAVITY HANDLER] Cleared gravity effects for player {} who left the world", player.getName().getString());
        }
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        
        // Clear gravity data when player changes dimensions
        if (player.getPersistentData().contains("strangematter.gravity_force")) {
            // Remove from static data
            com.hexvane.strangematter.GravityData.removePlayerGravityForce(player.getUUID());
            
            // Clear persistent data
            player.getPersistentData().remove("strangematter.gravity_force");
            
            LOGGER.info("[GRAVITY HANDLER] Cleared gravity effects for player {} who changed dimension", player.getName().getString());
        }
    }
    
    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        // Check if the removed entity is a Gravity Anomaly
        if (event.getEntity().getType().toString().equals("strangematter:gravity_anomaly") ||
            event.getEntity().getClass().getSimpleName().equals("GravityAnomalyEntity")) {
            
            // Clear gravity effects for all players in the world
            if (event.getLevel() != null) {
                for (Player player : event.getLevel().players()) {
                    if (player.getPersistentData().contains("strangematter.gravity_force")) {
                        // Remove from static data
                        com.hexvane.strangematter.GravityData.removePlayerGravityForce(player.getUUID());
                        
                        // Clear persistent data
                        player.getPersistentData().remove("strangematter.gravity_force");
                        
                        // Send packet to clear gravity force on client
                        if (!event.getLevel().isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                            com.hexvane.strangematter.network.NetworkHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer), 
                                new com.hexvane.strangematter.network.GravitySyncPacket(0.0)
                            );
                        }
                        
                        LOGGER.info("[GRAVITY HANDLER] Cleared gravity effects for player {} due to Gravity Anomaly removal", player.getName().getString());
                    }
                }
            }
        }
    }
}
