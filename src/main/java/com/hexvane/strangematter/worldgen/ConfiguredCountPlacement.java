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
 * A custom count placement modifier that reads its count value from the config.
 * This allows ore vein counts to be adjusted via configuration.
 */
public class ConfiguredCountPlacement extends PlacementModifier {
    public static final Codec<ConfiguredCountPlacement> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.STRING.fieldOf("feature_type").forGetter(filter -> filter.featureType)
        ).apply(instance, ConfiguredCountPlacement::new)
    );
    
    private final String featureType;
    
    public ConfiguredCountPlacement(String featureType) {
        this.featureType = featureType;
    }
    
    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        int count = getCountFromConfig();
        
        // If feature is disabled, return empty stream
        if (!isFeatureEnabled()) {
            return Stream.empty();
        }
        
        // Generate 'count' number of positions
        return Stream.generate(() -> pos).limit(count);
    }
    
    @Override
    public PlacementModifierType<?> type() {
        return StrangeMatterMod.CONFIGURED_COUNT_PLACEMENT.get();
    }
    
    /**
     * Get the count value from config based on feature type
     */
    private int getCountFromConfig() {
        try {
            return switch (featureType) {
                case "resonite_ore" -> Config.resoniteOreVeinsPerChunk;
                default -> 3; // Default count if unknown
            };
        } catch (Exception e) {
            return 3; // Safe default if config isn't loaded yet
        }
    }
    
    /**
     * Check if the feature is enabled in config
     */
    private boolean isFeatureEnabled() {
        return switch (featureType) {
            case "resonite_ore" -> Config.enableResoniteOre;
            default -> true; // Default to enabled if unknown
        };
    }
}

