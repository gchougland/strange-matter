package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Base class for energy-based machines that consume fuel to operate.
 * This provides fuel-powered machine logic for Strange Matter machines.
 */
public abstract class EnergyMachineBlockEntity extends BaseMachineBlockEntity {
    
    // Fuel system
    protected int fuelTime = 0;
    protected int maxFuelTime = 0;
    protected int burnTime = 0;
    protected int maxBurnTime = 0;
    
    // Energy consumption
    protected int energyPerTick = 1;
    protected int maxEnergyStorage = 1000;
    
    public EnergyMachineBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, int inventorySize) {
        super(blockEntityType, pos, state, inventorySize);
        this.maxEnergyLevel = maxEnergyStorage;
    }
    
    @Override
    protected void processMachine() {
        // Handle fuel consumption
        if (burnTime > 0) {
            burnTime--;
            if (burnTime <= 0) {
                // Fuel exhausted, try to consume more
                consumeFuel();
            }
        }
        
        // Handle energy consumption
        if (isActive && hasEnergy()) {
            if (consumeEnergy(energyPerTick)) {
                // Machine is running
                processMachineLogic();
            } else {
                // Not enough energy
                setActive(false);
            }
        } else if (isActive && !hasEnergy()) {
            setActive(false);
        }
    }
    
    /**
     * Override this to implement machine-specific processing when powered
     */
    protected abstract void processMachineLogic();
    
    /**
     * Try to consume fuel from the fuel slot
     */
    protected void consumeFuel() {
        ItemStack fuelStack = getItem(getFuelSlotIndex());
        if (!fuelStack.isEmpty() && isFuel(fuelStack)) {
            // Consume fuel
            fuelStack.shrink(1);
            if (fuelStack.isEmpty()) {
                setItem(getFuelSlotIndex(), ItemStack.EMPTY);
            }
            
            // Set burn time
            burnTime = getBurnTime(fuelStack);
            maxBurnTime = burnTime;
            
            // Add energy
            addEnergy(burnTime);
            setChanged();
            syncToClient();
        }
    }
    
    /**
     * Check if an item is valid fuel
     */
    protected boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }
    
    /**
     * Get the burn time for a fuel item
     */
    protected int getBurnTime(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING);
    }
    
    /**
     * Get the index of the fuel slot (override in subclasses)
     */
    protected abstract int getFuelSlotIndex();
    
    /**
     * Get the index of the input slot (override in subclasses)
     */
    protected abstract int getInputSlotIndex();
    
    /**
     * Get the index of the output slot (override in subclasses)
     */
    protected abstract int getOutputSlotIndex();
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("fuel_time", fuelTime);
        tag.putInt("max_fuel_time", maxFuelTime);
        tag.putInt("burn_time", burnTime);
        tag.putInt("max_burn_time", maxBurnTime);
        tag.putInt("energy_per_tick", energyPerTick);
        tag.putInt("max_energy_storage", maxEnergyStorage);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuelTime = tag.getInt("fuel_time");
        maxFuelTime = tag.getInt("max_fuel_time");
        burnTime = tag.getInt("burn_time");
        maxBurnTime = tag.getInt("max_burn_time");
        energyPerTick = tag.getInt("energy_per_tick");
        maxEnergyStorage = tag.getInt("max_energy_storage");
    }
    
    // Getters for GUI access
    public int getBurnTime() {
        return burnTime;
    }
    
    public int getMaxBurnTime() {
        return maxBurnTime;
    }
    
    public int getFuelTime() {
        return fuelTime;
    }
    
    public int getMaxFuelTime() {
        return maxFuelTime;
    }
    
    public int getEnergyPerTick() {
        return energyPerTick;
    }
    
    public int getMaxEnergyStorage() {
        return maxEnergyStorage;
    }
}
