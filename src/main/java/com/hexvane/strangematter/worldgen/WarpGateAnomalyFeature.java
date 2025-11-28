package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class WarpGateAnomalyFeature extends Feature<NoneFeatureConfiguration> {
    public WarpGateAnomalyFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();

        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        // Find the actual surface height at this location
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        BlockPos surfacePos = new BlockPos(pos.getX(), surfaceY, pos.getZ());

        System.out.println("WarpGate Feature: Spawning warp gate at " + surfacePos);

        // Check if a WarpGateAnomalyEntity already exists at this location
        var existingGates = serverLevel.getEntitiesOfClass(WarpGateAnomalyEntity.class,
            new net.minecraft.world.phys.AABB(surfacePos).inflate(5)); // Search a small area

        if (existingGates.isEmpty()) {
            System.out.println("WarpGate Feature: No existing WarpGateAnomalyEntity found, spawning one at " + surfacePos);

            // Place spawn marker block instead of directly spawning entity
            int markerY = surfacePos.getY() + 2;
            BlockPos markerPos = new BlockPos(surfacePos.getX(), markerY, surfacePos.getZ());
            level.setBlock(markerPos, StrangeMatterMod.ANOMALY_SPAWN_MARKER_BLOCK.get().defaultBlockState(), 2);
            if (level.getBlockEntity(markerPos) instanceof com.hexvane.strangematter.block.AnomalySpawnMarkerBlockEntity marker) {
                marker.entityTypeLocation = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "warp_gate_anomaly");
                marker.spawnPosition = new net.minecraft.world.phys.Vec3(markerPos.getX() + 0.5, markerPos.getY(), markerPos.getZ() + 0.5);
                marker.setChanged();
            }

            System.out.println("WarpGate Feature: Successfully placed spawn marker at " + markerPos);
            return true;
        } else {
            System.out.println("WarpGate Feature: WarpGateAnomalyEntity already exists at " + surfacePos + ", skipping spawn.");
            return false;
        }
    }
}
