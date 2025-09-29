package com.hexvane.strangematter.energy;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Capability provider for Resonance Energy.
 * Allows blocks to store and transfer Resonance Energy with Forge Energy compatibility.
 */
public class ResonanceEnergyCapability implements ICapabilityProvider {
    
    public static final Capability<IEnergyStorage> RESONANCE_ENERGY = CapabilityManager.get(new CapabilityToken<>(){});
    
    private final ResonanceEnergyStorage energyStorage;
    private final LazyOptional<IEnergyStorage> energyOptional;
    
    public ResonanceEnergyCapability(int capacity) {
        this.energyStorage = new ResonanceEnergyStorage(capacity);
        this.energyOptional = LazyOptional.of(() -> this.energyStorage);
    }
    
    public ResonanceEnergyCapability(int capacity, int maxReceive, int maxExtract) {
        this.energyStorage = new ResonanceEnergyStorage(capacity, maxReceive, maxExtract);
        this.energyOptional = LazyOptional.of(() -> this.energyStorage);
    }
    
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == RESONANCE_ENERGY || cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        return LazyOptional.empty();
    }
    
    /**
     * Get the energy storage instance
     */
    public ResonanceEnergyStorage getEnergyStorage() {
        return energyStorage;
    }
    
    /**
     * Invalidate the capability
     */
    public void invalidate() {
        energyOptional.invalidate();
    }
}
