package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.block.ResonanceCondenserBlockEntity;
import com.hexvane.strangematter.client.screen.ResonanceCondenserScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = StrangeMatterMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StrangeMatterMenuScreens {

    @SubscribeEvent
    public static void registerMenuScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(StrangeMatterMod.RESONANCE_CONDENSER_MENU.get(), ResonanceCondenserScreen::new);
            BlockEntityRenderers.register(StrangeMatterMod.RESONANCE_CONDENSER_BLOCK_ENTITY.get(), ResonanceCondenserRenderer::new);
        });
    }
}

