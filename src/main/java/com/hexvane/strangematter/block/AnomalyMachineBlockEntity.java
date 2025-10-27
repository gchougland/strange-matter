package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import com.hexvane.strangematter.entity.BaseAnomalyEntity;
import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.research.ScannableObjectRegistry;

/**
 * Base class for machines that interact with anomalies.
 * This provides common functionality for anomaly detection and interaction.
 */
public abstract class AnomalyMachineBlockEntity extends BaseMachineBlockEntity {
    
    // Anomaly detection
    protected int detectionRadius = 10;
    protected boolean hasNearbyAnomaly = false;
    protected ResearchType detectedAnomalyType = null;
    
    // Particle effects
    protected boolean spawnParticles = true;
    protected int particleSpawnRate = 5; // Every N ticks
    
    public AnomalyMachineBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, int inventorySize) {
        super(blockEntityType, pos, state, inventorySize);
    }
    
    @Override
    protected void processMachine() {
        // Check for nearby anomalies
        checkForNearbyAnomalies();
        
        // Process machine logic if anomaly is detected
        if (hasNearbyAnomaly) {
            processAnomalyLogic();
        } else {
            setActive(false);
        }
    }
    
    @Override
    protected void clientTick() {
        // Spawn particles if anomaly is detected
        if (spawnParticles && hasNearbyAnomaly && level.getGameTime() % particleSpawnRate == 0) {
            spawnAnomalyParticles();
        }
    }
    
    /**
     * Check for nearby anomalies within detection radius
     */
    protected void checkForNearbyAnomalies() {
        if (level == null) return;
        
        BlockPos pos = this.getBlockPos();
        boolean foundAnomaly = false;
        ResearchType anomalyType = null;
        
        for (Entity entity : level.getEntitiesOfClass(Entity.class,
            AABB.ofSize(pos.getCenter(), detectionRadius * 2, detectionRadius * 2, detectionRadius * 2))) {
            
            if (entity instanceof BaseAnomalyEntity) {
                foundAnomaly = true;
                
                // Get the research type of the anomaly
                var scannableOpt = ScannableObjectRegistry.getScannableForEntity(entity);
                if (scannableOpt.isPresent()) {
                    anomalyType = scannableOpt.get().getResearchType();
                }
                break;
            }
        }
        
        // Update state
        if (hasNearbyAnomaly != foundAnomaly) {
            hasNearbyAnomaly = foundAnomaly;
            detectedAnomalyType = anomalyType;
            setChanged();
            syncToClient();
        }
    }
    
    /**
     * Override this to implement machine-specific logic when anomaly is detected
     */
    protected abstract void processAnomalyLogic();
    
    /**
     * Override this to implement machine-specific particle effects
     */
    protected abstract void spawnAnomalyParticles();
    
    /**
     * Get the position of the nearest anomaly
     */
    protected net.minecraft.world.phys.Vec3 getNearestAnomalyPosition() {
        if (level == null || !hasNearbyAnomaly) return null;
        
        BlockPos pos = this.getBlockPos();
        for (Entity entity : level.getEntitiesOfClass(Entity.class,
            AABB.ofSize(pos.getCenter(), detectionRadius * 2, detectionRadius * 2, detectionRadius * 2))) {
            
            if (entity instanceof BaseAnomalyEntity) {
                return entity.position();
            }
        }
        return null;
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("detection_radius", detectionRadius);
        tag.putBoolean("has_nearby_anomaly", hasNearbyAnomaly);
        tag.putString("detected_anomaly_type", detectedAnomalyType != null ? detectedAnomalyType.name() : "");
        tag.putBoolean("spawn_particles", spawnParticles);
        tag.putInt("particle_spawn_rate", particleSpawnRate);
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        detectionRadius = tag.getInt("detection_radius");
        hasNearbyAnomaly = tag.getBoolean("has_nearby_anomaly");
        String anomalyTypeName = tag.getString("detected_anomaly_type");
        detectedAnomalyType = anomalyTypeName.isEmpty() ? null : ResearchType.valueOf(anomalyTypeName);
        spawnParticles = tag.getBoolean("spawn_particles");
        particleSpawnRate = tag.getInt("particle_spawn_rate");
    }
    
    // Getters for GUI access
    public boolean hasNearbyAnomaly() {
        return hasNearbyAnomaly;
    }
    
    public ResearchType getDetectedAnomalyType() {
        return detectedAnomalyType;
    }
    
    public int getDetectionRadius() {
        return detectionRadius;
    }
    
    public void setDetectionRadius(int radius) {
        this.detectionRadius = radius;
        setChanged();
        syncToClient();
    }
    
    public boolean isSpawnParticles() {
        return spawnParticles;
    }
    
    public void setSpawnParticles(boolean spawnParticles) {
        this.spawnParticles = spawnParticles;
        setChanged();
        syncToClient();
    }
}
