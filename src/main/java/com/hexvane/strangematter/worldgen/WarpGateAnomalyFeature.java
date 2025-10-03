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

            WarpGateAnomalyEntity newWarpGate = new WarpGateAnomalyEntity(
                StrangeMatterMod.WARP_GATE_ANOMALY_ENTITY.get(),
                serverLevel
            );
            newWarpGate.setPos(surfacePos.getX() + 0.5, surfacePos.getY() + 2, surfacePos.getZ() + 0.5);
            newWarpGate.setActive(true);
            serverLevel.addFreshEntity(newWarpGate);

            // Terrain generation will be handled by WarpGateSpawnEventHandler

            System.out.println("WarpGate Feature: Successfully spawned WarpGateAnomalyEntity at " + surfacePos);
            return true;
        } else {
            System.out.println("WarpGate Feature: WarpGateAnomalyEntity already exists at " + surfacePos + ", skipping spawn.");
            return false;
        }
    }
}
