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
        int rarity = getRarityFromConfig();
        
        // If feature is disabled, return empty stream (no spawns)
        if (!isFeatureEnabled()) {
            return Stream.empty();
        }
        
        // Check dimension restrictions
        if (!isDimensionAllowed(context)) {
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
    
    /**
     * Check if the current dimension is allowed for this feature type
     */
    private boolean isDimensionAllowed(PlacementContext context) {
        try {
            String currentDimension = context.getLevel().getLevel().dimension().location().toString();
            java.util.List<String> allowedDimensions = getDimensionsFromConfig();
            
            // If no dimensions are specified, allow all dimensions
            if (allowedDimensions == null || allowedDimensions.isEmpty()) {
                return true;
            }
            
            // Check if current dimension is in the allowed list
            return allowedDimensions.contains(currentDimension);
        } catch (Exception e) {
            return true; // Safe default if config isn't loaded yet
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

