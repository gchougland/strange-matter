package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.RegistryObject;
import javax.annotation.Nonnull;

public class EnergeticRiftConfiguredFeature extends BaseAnomalyConfiguredFeature {
    
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
        
        // Spawn the anomaly a few blocks above the surface
        int anomalyY = surfaceInfo.surfacePos.getY() + 2 + random.nextInt(3); // 2-4 blocks above surface
        BlockPos anomalyPos = new BlockPos(origin.getX(), anomalyY, origin.getZ());
        
        // Place a marker block that will spawn the entity on the next server tick
        // This defers entity spawning from the world generation thread to the main server thread
        if (!level.setBlock(anomalyPos, StrangeMatterMod.ANOMALY_SPAWNER_MARKER_BLOCK.get().defaultBlockState(), 3)) {
            return false;
        }
        var blockEntity = level.getBlockEntity(anomalyPos);
        if (!(blockEntity instanceof com.hexvane.strangematter.block.AnomalySpawnerMarkerBlockEntity marker)) {
            level.setBlock(anomalyPos, Blocks.AIR.defaultBlockState(), 3);
            return false;
        }
        marker.setEntityData("strangematter:energetic_rift", 
            anomalyPos.getX() + 0.5, anomalyPos.getY(), anomalyPos.getZ() + 0.5, 0.0f, 0.0f);
        
        // Place terrain modification (grass and ores) using base class
        placeAnomalousGrass(level, origin, random);
        placeOres(level, origin, random);
        
        return true;
    }
    
    @Override
    protected RegistryObject<Block> getShardOreBlock() {
        return StrangeMatterMod.ENERGETIC_SHARD_ORE_BLOCK;
    }
}
