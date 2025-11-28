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
import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.Map;
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
    public Stream<BlockPos> getPositions(@Nonnull PlacementContext context, @Nonnull RandomSource random, @Nonnull BlockPos pos) {
        // If feature is disabled, return empty stream (no spawns)
        if (!isFeatureEnabled()) {
            return Stream.empty();
        }

        String currentDimension = getDimensionId(context);
        Integer overrideRarity = currentDimension != null ? getDimensionOverride(currentDimension) : null;

        // Check dimension restrictions, honoring overrides first
        if (!isDimensionAllowed(currentDimension, overrideRarity)) {
            return Stream.empty();
        }

        int rarity = overrideRarity != null ? overrideRarity : getBaseRarity();
        if (rarity <= 0) {
            return Stream.empty();
        }

        // Apply rarity filter: 1/N chance
        if (random.nextInt(rarity) == 0) {
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
    private int getBaseRarity() {
        try {
            return switch (featureType) {
                case "gravity_anomaly" -> Config.gravityAnomalyRarity;
                case "temporal_bloom" -> Config.temporalBloomRarity;
                case "warp_gate_anomaly" -> Config.warpGateRarity;
                case "energetic_rift" -> Config.energeticRiftRarity;
                case "echoing_shadow" -> Config.echoingShadowRarity;
                case "thoughtwell" -> Config.thoughtwellRarity;
                default -> 500; // Default rarity if unknown
            };
        } catch (Exception e) {
            return 500; // Safe default if config isn't loaded yet
        }
    }
    
    /**
     * Check if the feature is enabled in config
     */
    private boolean isFeatureEnabled() {
        try {
            return switch (featureType) {
                case "gravity_anomaly" -> Config.enableGravityAnomaly;
                case "temporal_bloom" -> Config.enableTemporalBloom;
                case "warp_gate_anomaly" -> Config.enableWarpGate;
                case "energetic_rift" -> Config.enableEnergeticRift;
                case "echoing_shadow" -> Config.enableEchoingShadow;
                case "thoughtwell" -> Config.enableThoughtwell;
                default -> true; // Default to enabled if unknown
            };
        } catch (Exception e) {
            return true; // Safe default if config isn't loaded yet
        }
    }
    
    private String getDimensionId(PlacementContext context) {
        try {
            return context.getLevel().getLevel().dimension().location().toString();
        } catch (Exception e) {
            return null; // Safe default if the level isn't fully initialized yet
        }
    }

    /**
     * Check if the current dimension is allowed for this feature type
     */
    private boolean isDimensionAllowed(String dimensionId, Integer overrideRarity) {
        if (overrideRarity != null) {
            return overrideRarity > 0;
        }

        if (dimensionId == null) {
            return true;
        }

        java.util.List<String> allowedDimensions = getDimensionsFromConfig();
        if (allowedDimensions == null || allowedDimensions.isEmpty()) {
            return true;
        }

        return allowedDimensions.contains(dimensionId);
    }

    private Integer getDimensionOverride(String dimensionId) {
        if (dimensionId == null || dimensionId.isEmpty()) {
            return null;
        }

        Map<String, Integer> overrides = getDimensionRarityOverrides();
        if (overrides == null || overrides.isEmpty()) {
            return null;
        }

        Integer value = overrides.get(dimensionId);
        if (value != null) {
            return value;
        }

        return overrides.get("*");
    }

    private Map<String, Integer> getDimensionRarityOverrides() {
        try {
            return switch (featureType) {
                case "gravity_anomaly" -> Config.gravityAnomalyDimensionRarities;
                case "temporal_bloom" -> Config.temporalBloomDimensionRarities;
                case "warp_gate_anomaly" -> Config.warpGateDimensionRarities;
                case "energetic_rift" -> Config.energeticRiftDimensionRarities;
                case "echoing_shadow" -> Config.echoingShadowDimensionRarities;
                case "thoughtwell" -> Config.thoughtwellDimensionRarities;
                default -> Collections.emptyMap();
            };
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
    
    /**
     * Get the allowed dimensions list from config based on feature type
     */
    private java.util.List<String> getDimensionsFromConfig() {
        try {
            return switch (featureType) {
                case "gravity_anomaly" -> Config.gravityAnomalyDimensions;
                case "temporal_bloom" -> Config.temporalBloomDimensions;
                case "warp_gate_anomaly" -> Config.warpGateDimensions;
                case "energetic_rift" -> Config.energeticRiftDimensions;
                case "echoing_shadow" -> Config.echoingShadowDimensions;
                case "thoughtwell" -> Config.thoughtwellDimensions;
                default -> null; // No restrictions if unknown
            };
        } catch (Exception e) {
            return null; // Safe default if config isn't loaded yet
        }
    }
}

