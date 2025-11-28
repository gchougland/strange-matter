package com.hexvane.strangematter.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Base class for all anomaly configured features.
 * Provides common terrain modification functionality (anomalous grass and ore placement).
 */
public abstract class BaseAnomalyConfiguredFeature extends Feature<NoneFeatureConfiguration> {
    
    public BaseAnomalyConfiguredFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }
    
    /**
     * Get the shard ore block type for this anomaly.
     * Each anomaly type should return its corresponding shard ore block.
     */
    protected abstract DeferredHolder<Block, ? extends Block> getShardOreBlock();
    
    /**
     * Get the terrain modification radius for this anomaly.
     * Can be overridden by subclasses to customize the radius.
     */
    protected int getTerrainModificationRadius() {
        return 5; // Default radius
    }
    
    /**
     * Get the grass placement chance for this anomaly.
     * Can be overridden by subclasses to customize the chance.
     */
    protected float getGrassPlacementChance() {
        return 0.8f; // Default 80% chance
    }
    
    /**
     * Places anomalous grass in a patchy circle around the origin position.
     * Uses config values for terrain modification.
     */
    protected void placeAnomalousGrass(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int radius = getTerrainModificationRadius();
        float chance = getGrassPlacementChance();
        WorldGenUtils.placeAnomalousGrassPatch(level, origin, radius, chance, random);
    }
    
    /**
     * Places resonite ore and shard ore underneath the anomaly.
     * Uses config values for ore spawn chances and replacement blocks.
     */
    protected void placeOres(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int radius = getTerrainModificationRadius();
        WorldGenUtils.placeAnomalyOres(level, origin, radius, random, getShardOreBlock().get());
    }
}

