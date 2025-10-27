package com.hexvane.strangematter.world;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
public class WorldEventHandler {
    
    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            // Update shadow light provider every tick
            ShadowLightProvider.getInstance(serverLevel).updateShadowLightLevels();
        }
    }
}
