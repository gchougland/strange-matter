package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.Config;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

/**
 * A configurable biome modifier that respects config settings for feature placement.
 * This allows features to be enabled/disabled and their rarity to be adjusted via config.
 */
public record ConfiguredBiomeModifier(
        HolderSet<Biome> biomes,
        Holder<PlacedFeature> feature,
        GenerationStep.Decoration step,
        String featureType  // e.g., "gravity_anomaly", "resonite_ore"
) implements BiomeModifier {
    
    public static final Codec<ConfiguredBiomeModifier> CODEC = RecordCodecBuilder.create(
        builder -> builder.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ConfiguredBiomeModifier::biomes),
            PlacedFeature.CODEC.fieldOf("feature").forGetter(ConfiguredBiomeModifier::feature),
            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(ConfiguredBiomeModifier::step),
            Codec.STRING.fieldOf("feature_type").forGetter(ConfiguredBiomeModifier::featureType)
        ).apply(builder, ConfiguredBiomeModifier::new)
    );

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && this.biomes.contains(biome)) {
            // Check config to see if this feature should be added
            if (isFeatureEnabled()) {
                builder.getGenerationSettings().addFeature(this.step, this.feature);
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return MapCodec.assumeMapUnsafe(CODEC);
    }
    
    /**
     * Check config to determine if this feature should be enabled
     */
    private boolean isFeatureEnabled() {
        return switch (featureType) {
            case "gravity_anomaly" -> Config.enableGravityAnomaly;
            case "temporal_bloom" -> Config.enableTemporalBloom;
            case "warp_gate_anomaly" -> Config.enableWarpGate;
            case "energetic_rift" -> Config.enableEnergeticRift;
            case "echoing_shadow" -> Config.enableEchoingShadow;
            case "thoughtwell" -> Config.enableThoughtwell;
            case "resonite_ore" -> Config.enableResoniteOre;
            default -> true; // Default to enabled if unknown
        };
    }
}

