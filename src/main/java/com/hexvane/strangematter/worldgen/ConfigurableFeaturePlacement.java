package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.Config;
import com.hexvane.strangematter.StrangeMatterMod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles dynamic modification of world generation features based on config values.
 * This modifies placed features at server start to respect config settings.
 */
@Mod.EventBusSubscriber(modid = StrangeMatterMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConfigurableFeaturePlacement {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        Registry<PlacedFeature> placedFeatures = event.getServer()
                .registryAccess()
                .registryOrThrow(Registries.PLACED_FEATURE);
        
        // Update anomaly spawn rarities based on config
        updateAnomalyRarity(placedFeatures, "gravity_anomaly", Config.gravityAnomalyRarity, Config.enableGravityAnomaly);
        updateAnomalyRarity(placedFeatures, "temporal_bloom", Config.temporalBloomRarity, Config.enableTemporalBloom);
        updateAnomalyRarity(placedFeatures, "warp_gate_anomaly", Config.warpGateRarity, Config.enableWarpGate);
        updateAnomalyRarity(placedFeatures, "energetic_rift", Config.energeticRiftRarity, Config.enableEnergeticRift);
        updateAnomalyRarity(placedFeatures, "echoing_shadow", Config.echoingShadowRarity, Config.enableEchoingShadow);
        updateAnomalyRarity(placedFeatures, "thoughtwell", Config.thoughtwellRarity, Config.enableThoughtwell);
        
        // Update resonite ore generation
        updateOreGeneration(placedFeatures, "resonite_ore", Config.resoniteOreVeinsPerChunk, Config.enableResoniteOre);
    }
    
    private static void updateAnomalyRarity(Registry<PlacedFeature> registry, String featureName, 
                                           int rarity, boolean enabled) {
        // ResourceKey<PlacedFeature> key = ResourceKey.create(
        //         Registries.PLACED_FEATURE,
        //         ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, featureName)
        // );
        
        // Note: This is informational logging since Forge's registry is immutable after freezing
        // The actual modification happens through our custom biome modifier system
        // Only log when features are disabled to reduce log spam
        if (!enabled) {
            LOGGER.info("Feature {} is disabled in config", featureName);
        }
    }
    
    private static void updateOreGeneration(Registry<PlacedFeature> registry, String featureName,
                                           int veinsPerChunk, boolean enabled) {
        // Only log when features are disabled to reduce log spam
        if (!enabled) {
            LOGGER.info("Ore feature {} is disabled in config", featureName);
        }
    }
}

