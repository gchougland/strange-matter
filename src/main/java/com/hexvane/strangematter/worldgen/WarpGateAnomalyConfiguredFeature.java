package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.RegistryObject;
import javax.annotation.Nonnull;

public class WarpGateAnomalyConfiguredFeature extends BaseAnomalyConfiguredFeature {
    
    @Override
    public boolean place(@Nonnull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        
        // Use WorldGenUtils to efficiently find surface and ground
        WorldGenUtils.SurfaceInfo surfaceInfo = WorldGenUtils.findSurfaceAndGround(level, origin.getX(), origin.getZ());
        if (surfaceInfo == null) {
            return false; // No valid ground found
        }
        
        // Spawn the warp gate a few blocks above the surface
        int anomalyY = surfaceInfo.surfacePos.getY() + 2;
        BlockPos anomalyPos = new BlockPos(origin.getX(), anomalyY, origin.getZ());
        
        // Place a marker block that will spawn the entity on the next server tick
        // This defers entity spawning from the world generation thread to the main server thread
        var markerBlock = (com.hexvane.strangematter.block.AnomalySpawnerMarkerBlock) StrangeMatterMod.ANOMALY_SPAWNER_MARKER_BLOCK.get();
        BlockState markerState = markerBlock.defaultBlockState()
            .setValue(com.hexvane.strangematter.block.AnomalySpawnerMarkerBlock.TYPE, com.hexvane.strangematter.block.AnomalySpawnerMarkerBlock.SpawnType.WARP_GATE)
            .setValue(com.hexvane.strangematter.block.AnomalySpawnerMarkerBlock.ATTEMPTS, 0);
        if (!level.setBlock(anomalyPos, markerState, 3)) {
            return false;
        }
        level.scheduleTick(anomalyPos, markerBlock, 1);
        
        // Place terrain modification (grass and ores) using base class
        placeAnomalousGrass(level, origin, random);
        placeOres(level, origin, random);
        
        return true;
    }
    
    @Override
    protected RegistryObject<Block> getShardOreBlock() {
        return StrangeMatterMod.SPATIAL_SHARD_ORE_BLOCK;
    }
}
