package com.hexvane.strangematter.energy;

import net.minecraftforge.energy.EnergyStorage;

/**
 * Custom energy storage implementation for Resonance Energy.
 * Provides 1:1 compatibility with Forge Energy while maintaining the Resonance Energy theme.
 */
public class ResonanceEnergyStorage extends EnergyStorage {
    
    public ResonanceEnergyStorage(int capacity) {
        super(capacity);
    }
    
    public ResonanceEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }
    
    public ResonanceEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }
    
    public ResonanceEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }
    
    /**
     * Set the energy level directly (for creative blocks or special cases)
     */
    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(energy, capacity));
    }
    
    /**
     * Set the maximum capacity
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
        if (energy > capacity) {
            energy = capacity;
        }
    }
    
    /**
     * Set the maximum receive rate
     */
    public void setMaxReceive(int maxReceive) {
        this.maxReceive = maxReceive;
    }
    
    /**
     * Set the maximum extract rate
     */
    public void setMaxExtract(int maxExtract) {
        this.maxExtract = maxExtract;
    }
    
    /**
     * Check if the storage can receive energy
     */
    public boolean canReceive() {
        return maxReceive > 0;
    }
    
    /**
     * Check if the storage can extract energy
     */
    public boolean canExtract() {
        return maxExtract > 0;
    }
    
    /**
     * Get the energy level as a percentage (0.0 to 1.0)
     */
    public float getEnergyPercentage() {
        return capacity > 0 ? (float) energy / (float) capacity : 0.0f;
    }
    
    /**
     * Get the energy level as a percentage (0 to 100)
     */
    public int getEnergyPercentageInt() {
        return capacity > 0 ? (int) ((float) energy / (float) capacity * 100) : 0;
    }
    
    /**
     * Check if the storage is full
     */
    public boolean isFull() {
        return energy >= capacity;
    }
    
    /**
     * Check if the storage is empty
     */
    public boolean isEmpty() {
        return energy <= 0;
    }
}
