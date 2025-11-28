package com.hexvane.strangematter.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.core.SectionPos;
import com.hexvane.strangematter.entity.EchoingShadowEntity;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Custom light provider that modifies light levels around Echoing Shadow anomalies
 * This works by intercepting light level queries and applying shadow effects
 */
public class ShadowLightProvider {
    
    private static final Map<ServerLevel, ShadowLightProvider> INSTANCES = new ConcurrentHashMap<>();
    private final ServerLevel level;
    private final Set<EchoingShadowEntity> shadowAnomalies = new CopyOnWriteArraySet<>();
    private final Map<BlockPos, Integer> shadowLightLevels = new ConcurrentHashMap<>();
    
    private ShadowLightProvider(ServerLevel level) {
        this.level = level;
    }
    
    public static ShadowLightProvider getInstance(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level, ShadowLightProvider::new);
    }
    
    public void registerShadowAnomaly(EchoingShadowEntity anomaly) {
        shadowAnomalies.add(anomaly);
        updateShadowLightLevels();
    }
    
    public void unregisterShadowAnomaly(EchoingShadowEntity anomaly) {
        shadowAnomalies.remove(anomaly);
        updateShadowLightLevels();
    }
    
    public int getShadowAnomalyCount() {
        return shadowAnomalies.size();
    }
    
    /**
     * Check if a position is within the shadow radius of any anomaly
     */
    public boolean isPositionInShadow(BlockPos pos) {
        for (EchoingShadowEntity anomaly : shadowAnomalies) {
            if (anomaly == null || anomaly.isRemoved()) {
                continue;
            }
            
            float radius = anomaly.getLightAbsorptionRadius();
            BlockPos anomalyPos = anomaly.blockPosition();
            double distanceSq = pos.distSqr(anomalyPos);
            
            if (distanceSq <= radius * radius) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Update shadow light levels for all affected positions
     */
    public void updateShadowLightLevels() {
        // Create a snapshot of the current anomalies to avoid concurrent modification
        Set<EchoingShadowEntity> currentAnomalies = new HashSet<>(shadowAnomalies);
        
        shadowLightLevels.clear();
        
        if (currentAnomalies.isEmpty()) {
            return;
        }
        
        // Calculate shadow effects for all affected positions
        for (EchoingShadowEntity anomaly : currentAnomalies) {
            if (anomaly != null && !anomaly.isRemoved()) {
                calculateShadowEffect(anomaly);
            }
        }
    }
    
    /**
     * Calculate shadow effect from a specific anomaly
     */
    private void calculateShadowEffect(EchoingShadowEntity anomaly) {
        BlockPos centerPos = anomaly.blockPosition();
        int radius = (int) anomaly.getLightAbsorptionRadius();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    
                    if (distance <= anomaly.getLightAbsorptionRadius()) {
                        BlockPos pos = centerPos.offset(x, y, z);
                        
                        // Calculate light reduction based on distance
                        double reductionFactor = 1.0 - (distance / anomaly.getLightAbsorptionRadius());
                        int lightReduction = (int) (anomaly.getLightLevelReduction() * reductionFactor);
                        
                        if (lightReduction > 0) {
                            // Get original light level
                            int originalLight = level.getBrightness(LightLayer.BLOCK, pos);
                            int originalSkyLight = level.getBrightness(LightLayer.SKY, pos);
                            int maxOriginalLight = Math.max(originalLight, originalSkyLight);
                            
                            // Calculate shadow light level
                            int shadowLight = Math.max(0, maxOriginalLight - lightReduction);
                            shadowLightLevels.put(pos, shadowLight);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Get the modified light level at a position, accounting for shadow anomalies
     */
    public int getModifiedLightLevel(BlockPos pos, LightLayer lightLayer, int originalLight) {
        // Create a snapshot of the current anomalies to avoid concurrent modification
        Set<EchoingShadowEntity> currentAnomalies = new HashSet<>(shadowAnomalies);
        
        // Check if this position is affected by any shadow anomaly
        for (EchoingShadowEntity anomaly : currentAnomalies) {
            if (anomaly != null && !anomaly.isRemoved()) {
                double distance = anomaly.position().distanceTo(new net.minecraft.world.phys.Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                
                if (distance <= anomaly.getLightAbsorptionRadius()) {
                    // Calculate light reduction based on distance
                    double reductionFactor = 1.0 - (distance / anomaly.getLightAbsorptionRadius());
                    int lightReduction = (int) (anomaly.getLightLevelReduction() * reductionFactor);
                    
                    // Apply reduction to both block and sky light
                    int modifiedLight = Math.max(0, originalLight - lightReduction);
                    System.out.println("SHADOW EFFECT: " + originalLight + " -> " + modifiedLight + " (reduction: " + lightReduction + ") at " + pos);
                    return modifiedLight;
                }
            }
        }
        return originalLight;
    }
    
    /**
     * Force lighting updates for all affected areas
     */
    public void updateLighting() {
        LevelLightEngine lightEngine = level.getChunkSource().getLightEngine();
        
        for (BlockPos pos : shadowLightLevels.keySet()) {
            lightEngine.updateSectionStatus(SectionPos.of(pos), true);
        }
    }
    
    public static void cleanup() {
        INSTANCES.clear();
    }
}
