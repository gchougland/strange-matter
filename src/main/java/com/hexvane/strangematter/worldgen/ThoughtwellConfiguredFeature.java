package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.ThoughtwellEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;

public class ThoughtwellConfiguredFeature extends Feature<NoneFeatureConfiguration> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("StrangeMatter:ThoughtwellFeature");
    private static int callCount = 0;
    
    public ThoughtwellConfiguredFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }
    
    public static int getCallCount() {
        return callCount;
    }
    
    @Override
    public boolean place(@Nonnull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        callCount++;
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        
        LOGGER.info("ThoughtwellFeature.place() called #{} at position: {} in dimension: {}", 
                   callCount, origin, level.getLevel().dimension().location());
        
        // Use WORLD_SURFACE heightmap to find the top surface
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, origin.getX(), origin.getZ());
        BlockPos surfacePos = new BlockPos(origin.getX(), surfaceY, origin.getZ());
        
        // Find the actual solid ground below the surface (in case it's leaves, air, etc.)
        BlockPos groundPos = surfacePos;
        while (groundPos.getY() > level.getMinBuildHeight() + 10) {
            var blockState = level.getBlockState(groundPos);
            if (blockState.isSolid() && !blockState.isAir() && 
                !blockState.getBlock().getDescriptionId().contains("leaves")) {
                break; // Found solid ground (grass, dirt, stone, sand, etc. are all fine)
            }
            groundPos = groundPos.below();
        }
        
        // Check if we found valid solid ground
        if (groundPos.getY() <= level.getMinBuildHeight() + 10) {
            LOGGER.info("No solid ground found below surface at {}, skipping placement", surfacePos);
            return false;
        }
        
        LOGGER.info("Found solid ground at {} (surface was at {})", groundPos, surfacePos);
        
        // Spawn the anomaly a few blocks above the surface
        int anomalyY = surfaceY + 2 + random.nextInt(3); // 2-4 blocks above surface
        BlockPos anomalyPos = new BlockPos(origin.getX(), anomalyY, origin.getZ());
        
        LOGGER.info("Surface Y: {}, Anomaly Y: {}, Final position: {}", surfaceY, anomalyY, anomalyPos);
        
        // Let the entity handle terrain generation using the base class method
        // This ensures consistent behavior and includes shard ore spawning
        
        // Spawn the thoughtwell entity above the surface
        ThoughtwellEntity anomaly = new ThoughtwellEntity(StrangeMatterMod.THOUGHTWELL.get(), level.getLevel());
        anomaly.moveTo(anomalyPos.getX() + 0.5, anomalyPos.getY(), anomalyPos.getZ() + 0.5, 0.0f, 0.0f);
        
        boolean success = level.getLevel().addFreshEntity(anomaly);
        LOGGER.info("Successfully placed thoughtwell at: {} - Success: {}", anomalyPos, success);
        
        return success;
    }
    
}
