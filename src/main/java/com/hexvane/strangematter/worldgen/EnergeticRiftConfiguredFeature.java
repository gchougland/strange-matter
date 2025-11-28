package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.level.block.Block;
import javax.annotation.Nonnull;

public class EnergeticRiftConfiguredFeature extends BaseAnomalyConfiguredFeature {
    
    @Override
    public boolean place(@Nonnull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        
        // Use optimized utility to find surface and ground
        WorldGenUtils.SurfaceInfo surfaceInfo = WorldGenUtils.findSurfaceAndGround(level, origin.getX(), origin.getZ());
        if (surfaceInfo == null) {
            return false; // No valid ground found
        }
        
        // Spawn the anomaly a few blocks above the surface
        int anomalyY = surfaceInfo.surfacePos.getY() + 2 + random.nextInt(3); // 2-4 blocks above surface
        BlockPos anomalyPos = new BlockPos(origin.getX(), anomalyY, origin.getZ());
        
        // Check if the area is clear of trees before placing the anomaly
        if (!WorldGenUtils.isAreaClearOfTrees(level, anomalyPos, 2)) {
            return false; // Skip placement if area is not clear of trees
        }
        
        // Place terrain modification (grass and ores) using base class
        placeAnomalousGrass(level, origin, random);
        placeOres(level, origin, random);
        
        // Place a marker block that will spawn the entity on the next server tick
        // This defers entity spawning from the world generation thread to the main server thread
        level.setBlock(anomalyPos, StrangeMatterMod.ANOMALY_SPAWN_MARKER_BLOCK.get().defaultBlockState(), 3);
        var blockEntity = level.getBlockEntity(anomalyPos);
        if (blockEntity instanceof com.hexvane.strangematter.block.AnomalySpawnMarkerBlockEntity marker) {
            marker.entityTypeLocation = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "energetic_rift");
            marker.spawnPosition = new net.minecraft.world.phys.Vec3(anomalyPos.getX() + 0.5, anomalyPos.getY(), anomalyPos.getZ() + 0.5);
            marker.setChanged();
        }
        
        return true;
    }
    
    @Override
    protected DeferredHolder<Block, ? extends Block> getShardOreBlock() {
        return StrangeMatterMod.ENERGETIC_SHARD_ORE_BLOCK;
    }
}
