package com.hexvane.strangematter.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Robust energy storage implementation for Strange Matter machines.
 * Integrates properly with NeoForge's IEnergyStorage system.
 */
public class MachineEnergyStorage implements IEnergyStorage {
    
    private final int maxEnergy;
    private final int maxReceive;
    private final int maxExtract;
    private int energy;
    
    // Callback for when energy changes
    private final Runnable onEnergyChanged;
    
    public MachineEnergyStorage(int maxEnergy, int maxReceive, int maxExtract, Runnable onEnergyChanged) {
        this.maxEnergy = maxEnergy;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.onEnergyChanged = onEnergyChanged;
    }
    
    public MachineEnergyStorage(int maxEnergy, int maxReceive, int maxExtract) {
        this(maxEnergy, maxReceive, maxExtract, null);
    }
    
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive <= 0) {
            return 0;
        }
        
        int energyReceived = Math.min(maxReceive, Math.min(this.maxReceive, maxEnergy - energy));
        if (!simulate) {
            energy += energyReceived;
            if (onEnergyChanged != null) {
                onEnergyChanged.run();
            }
        }
        
        return energyReceived;
    }
    
    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (maxExtract <= 0) {
            return 0;
        }
        
        int energyExtracted = Math.min(maxExtract, Math.min(this.maxExtract, energy));
        if (!simulate) {
            energy -= energyExtracted;
            if (onEnergyChanged != null) {
                onEnergyChanged.run();
            }
        }
        
        return energyExtracted;
    }
    
    @Override
    public int getEnergyStored() {
        return energy;
    }
    
    @Override
    public int getMaxEnergyStored() {
        return maxEnergy;
    }
    
    @Override
    public boolean canExtract() {
        return maxExtract > 0 && energy > 0;
    }
    
    @Override
    public boolean canReceive() {
        return maxReceive > 0 && energy < maxEnergy;
    }
    
    // Additional methods for internal use
    public int getMaxReceive() {
        return maxReceive;
    }
    
    public int getMaxExtract() {
        return maxExtract;
    }
    
    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(energy, maxEnergy));
        if (onEnergyChanged != null) {
            onEnergyChanged.run();
        }
    }
    
    public void addEnergy(int amount) {
        setEnergy(getEnergyStored() + amount);
    }
    
    public void consumeEnergy(int amount) {
        setEnergy(getEnergyStored() - amount);
    }
    
    public boolean hasEnergy() {
        return energy > 0;
    }
    
    public boolean isFull() {
        return energy >= maxEnergy;
    }
    
    public boolean isEmpty() {
        return energy <= 0;
    }
    
    public float getEnergyPercentage() {
        return maxEnergy > 0 ? (float) energy / maxEnergy : 0.0f;
    }
    
    public int getEnergyPercentageInt() {
        return Math.round(getEnergyPercentage() * 100);
    }
}
