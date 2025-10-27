package com.hexvane.strangematter.client;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import com.hexvane.strangematter.StrangeMatterMod;

@EventBusSubscriber(modid = "strangematter", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ResearchOverlayEventHandler {
    
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiEvent.Post event) {
        GuiGraphics guiGraphics = event.getGuiGraphics();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        
        // Render our research gain overlay
        ResearchOverlay.render(guiGraphics, partialTick);
    }
    
}
