package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.particle.EnergyAbsorptionParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StrangeMatterMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StrangeMatterParticleProviders {
    
    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        // Register the energy absorption particle provider
        event.registerSpriteSet(StrangeMatterMod.ENERGY_ABSORPTION_PARTICLE.get(), EnergyAbsorptionParticle.Provider::new);
    }
}