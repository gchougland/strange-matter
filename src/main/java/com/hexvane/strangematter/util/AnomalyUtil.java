package com.hexvane.strangematter.util;

import com.hexvane.strangematter.entity.BaseAnomalyEntity;
import com.hexvane.strangematter.entity.EnergeticRiftEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * Utility class for working with anomalies.
 * Provides helper functions for detecting and interacting with nearby anomalies.
 */
public class AnomalyUtil {
    
    /**
     * Find the nearest Energetic Rift anomaly within the specified radius.
     * @param level The world level
     * @param pos The center position to search from
     * @param radius The search radius
     * @return Optional containing the nearest Energetic Rift, or empty if none found
     */
    public static Optional<EnergeticRiftEntity> findNearestEnergeticRift(Level level, BlockPos pos, double radius) {
        Vec3 centerPos = Vec3.atCenterOf(pos);
        double diameter = radius * 2;
        AABB searchArea = AABB.ofSize(centerPos, diameter, diameter, diameter);
        
        List<EnergeticRiftEntity> rifts = level.getEntitiesOfClass(EnergeticRiftEntity.class, searchArea);
        
        EnergeticRiftEntity nearest = null;
        double nearestDistance = radius + 1; // Start with a distance greater than radius
        
        for (EnergeticRiftEntity rift : rifts) {
            double distance = centerPos.distanceTo(rift.position());
            if (distance <= radius && distance < nearestDistance) {
                nearest = rift;
                nearestDistance = distance;
            }
        }
        
        return Optional.ofNullable(nearest);
    }
    
    /**
     * Find the nearest anomaly of any type within the specified radius.
     * @param level The world level
     * @param pos The center position to search from
     * @param radius The search radius
     * @return Optional containing the nearest anomaly, or empty if none found
     */
    public static Optional<BaseAnomalyEntity> findNearestAnomaly(Level level, BlockPos pos, double radius) {
        Vec3 centerPos = Vec3.atCenterOf(pos);
        double diameter = radius * 2;
        AABB searchArea = AABB.ofSize(centerPos, diameter, diameter, diameter);
        
        List<BaseAnomalyEntity> anomalies = level.getEntitiesOfClass(BaseAnomalyEntity.class, searchArea);
        
        BaseAnomalyEntity nearest = null;
        double nearestDistance = radius + 1;
        
        for (BaseAnomalyEntity anomaly : anomalies) {
            double distance = centerPos.distanceTo(anomaly.position());
            if (distance <= radius && distance < nearestDistance) {
                nearest = anomaly;
                nearestDistance = distance;
            }
        }
        
        return Optional.ofNullable(nearest);
    }
    
    /**
     * Count how many Energetic Rift anomalies are within the specified radius.
     * @param level The world level
     * @param pos The center position to search from
     * @param radius The search radius
     * @return The number of Energetic Rifts within range
     */
    public static int countEnergeticRiftsInRange(Level level, BlockPos pos, double radius) {
        Vec3 centerPos = Vec3.atCenterOf(pos);
        double diameter = radius * 2;
        AABB searchArea = AABB.ofSize(centerPos, diameter, diameter, diameter);
        
        List<EnergeticRiftEntity> rifts = level.getEntitiesOfClass(EnergeticRiftEntity.class, searchArea);
        
        int count = 0;
        for (EnergeticRiftEntity rift : rifts) {
            double distance = centerPos.distanceTo(rift.position());
            if (distance <= radius) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Check if there is at least one Energetic Rift within the specified radius.
     * @param level The world level
     * @param pos The center position to search from
     * @param radius The search radius
     * @return true if at least one Energetic Rift is within range, false otherwise
     */
    public static boolean hasEnergeticRiftInRange(Level level, BlockPos pos, double radius) {
        return findNearestEnergeticRift(level, pos, radius).isPresent();
    }
}

