package com.hexvane.strangematter.client;

import com.hexvane.strangematter.entity.BaseAnomalyEntity;
import com.hexvane.strangematter.client.sound.CustomSoundManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class AnomalySoundCleanupHandler {
    
    @SubscribeEvent
    public static void onEntityRemoved(net.minecraftforge.event.entity.EntityLeaveLevelEvent event) {
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
