package com.hexvane.strangematter.client;

import com.hexvane.strangematter.entity.BaseAnomalyEntity;
import com.hexvane.strangematter.client.sound.CustomSoundManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "strangematter", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class AnomalySoundCleanupHandler {
    
    @SubscribeEvent
    public static void onEntityRemoved(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event) {
        // Check if the removed entity is an anomaly
        if (event.getEntity() instanceof BaseAnomalyEntity anomaly) {
            // Stop the anomaly's sound when it's removed
            try {
                CustomSoundManager.getInstance().stopAmbientSound(anomaly.getAnomalySound());
            } catch (Exception e) {
                // Ignore errors - the sound might already be stopped
            }
        }
    }
}
