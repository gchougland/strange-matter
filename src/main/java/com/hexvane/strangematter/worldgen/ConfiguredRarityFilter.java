package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.Config;
import com.hexvane.strangematter.StrangeMatterMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

/**
 * A custom rarity filter that reads its rarity value from the config.
 * This allows anomaly spawn rates to be adjusted via configuration.
 */
public class ConfiguredRarityFilter extends PlacementModifier {
    public static final Codec<ConfiguredRarityFilter> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.STRING.fieldOf("feature_type").forGetter(filter -> filter.featureType)
        ).apply(instance, ConfiguredRarityFilter::new)
    );
    
    private final String featureType;
    
    public ConfiguredRarityFilter(String featureType) {
        this.featureType = featureType;
    }
    
    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        int rarity = getRarityFromConfig();
        
        // If feature is disabled, return empty stream (no spawns)
        if (!isFeatureEnabled()) {
            return Stream.empty();
        }
        
        // Apply rarity filter: 1/N chance
        if (rarity > 0 && random.nextInt(rarity) == 0) {
            return Stream.of(pos);
        }
        
        return Stream.empty();
    }
    
    @Override
    public PlacementModifierType<?> type() {
        return StrangeMatterMod.CONFIGURED_RARITY_FILTER.get();
    }
    
    /**
     * Get the rarity value from config based on feature type
     */
    private int getRarityFromConfig() {
        return switch (featureType) {
            case "gravity_anomaly" -> Config.gravityAnomalyRarity;
            case "temporal_bloom" -> Config.temporalBloomRarity;
            case "warp_gate_anomaly" -> Config.warpGateRarity;
            case "energetic_rift" -> Config.energeticRiftRarity;
            case "echoing_shadow" -> Config.echoingShadowRarity;
            case "thoughtwell" -> Config.thoughtwellRarity;
            default -> 500; // Default rarity if unknown
        };
    }
    
    /**
     * Check if the feature is enabled in config
     */
    private boolean isFeatureEnabled() {
        return switch (featureType) {
            case "gravity_anomaly" -> Config.enableGravityAnomaly;
            case "temporal_bloom" -> Config.enableTemporalBloom;
            case "warp_gate_anomaly" -> Config.enableWarpGate;
            case "energetic_rift" -> Config.enableEnergeticRift;
            case "echoing_shadow" -> Config.enableEchoingShadow;
            case "thoughtwell" -> Config.enableThoughtwell;
            default -> true; // Default to enabled if unknown
        };
    }
}

