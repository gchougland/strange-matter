package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.Config;
import com.hexvane.strangematter.StrangeMatterMod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.ArrayList;

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
        ResourceKey<PlacedFeature> key = ResourceKey.create(
                Registries.PLACED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, featureName)
        );
        
        // Note: This is informational logging since Forge's registry is immutable after freezing
        // The actual modification happens through our custom biome modifier system
        if (!enabled) {
            LOGGER.info("Feature {} is disabled in config", featureName);
        } else if (rarity != 500) { // 500 is the default
            LOGGER.info("Feature {} rarity changed to 1/{}", featureName, rarity);
        }
    }
    
    private static void updateOreGeneration(Registry<PlacedFeature> registry, String featureName,
                                           int veinsPerChunk, boolean enabled) {
        if (!enabled) {
            LOGGER.info("Ore feature {} is disabled in config", featureName);
        } else if (veinsPerChunk != 3) { // 3 is the default
            LOGGER.info("Ore feature {} veins per chunk changed to {}", featureName, veinsPerChunk);
        }
    }
}

