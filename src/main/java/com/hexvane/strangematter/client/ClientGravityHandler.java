package com.hexvane.strangematter.client;

import com.hexvane.strangematter.GravityData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "strangematter", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGravityHandler {
    
    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Player player && player == Minecraft.getInstance().player) {
            // Check if player has gravity modification data using static data
            if (GravityData.hasPlayerGravityForce(player.getUUID())) {
                double forceMultiplier = GravityData.getPlayerGravityForce(player.getUUID());
                
                // Apply gravity modification on CLIENT SIDE
                Vec3 currentVelocity = player.getDeltaMovement();
                
                // Completely override gravity when falling
                if (currentVelocity.y < 0) {
                    // Reduce gravity dramatically
                    double gravityReduction = 1.0 - (forceMultiplier * 0.9); // 0.1 to 1.0 (90% reduction at max)
                    Vec3 newVelocity = new Vec3(currentVelocity.x, currentVelocity.y * gravityReduction, currentVelocity.z);
                    
                    // Add strong upward force
                    double upwardForce = 0.15 * forceMultiplier;
                    newVelocity = newVelocity.add(0, upwardForce, 0);
                    
                    // FORCE the new velocity on CLIENT
                    player.setDeltaMovement(newVelocity);
                    
                }
                
                // Reduce fall distance to prevent fall damage
                if (player.fallDistance > 0) {
                    player.fallDistance *= (1.0f - (float)(forceMultiplier * 0.8));
                }
            }
        }
    }
}
