package com.hexvane.strangematter.energy;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import com.hexvane.strangematter.energy.EnergyAttachment;
import com.hexvane.strangematter.block.BaseMachineBlockEntity;

/**
 * Manages energy input/output sides for machines.
 * Provides robust side-based energy management.
 */
public class EnergySideManager {
    
    private final boolean[] inputSides = new boolean[6];
    private final boolean[] outputSides = new boolean[6];
    private final boolean[] enabledSides = new boolean[6];
    
    public EnergySideManager() {
        // Default: all sides disabled
        for (int i = 0; i < 6; i++) {
            inputSides[i] = false;
            outputSides[i] = false;
            enabledSides[i] = false;
        }
    }
    
    /**
     * Set energy input capability for a specific side
     */
    public void setInputSide(Direction side, boolean enabled) {
        if (side != null) {
            inputSides[side.ordinal()] = enabled;
            updateEnabledState(side);
        }
    }
    
    /**
     * Set energy output capability for a specific side
     */
    public void setOutputSide(Direction side, boolean enabled) {
        if (side != null) {
            outputSides[side.ordinal()] = enabled;
            updateEnabledState(side);
        }
    }
    
    /**
     * Set both input and output for a specific side
     */
    public void setSide(Direction side, boolean input, boolean output) {
        if (side != null) {
            inputSides[side.ordinal()] = input;
            outputSides[side.ordinal()] = output;
            updateEnabledState(side);
        }
    }
    
    /**
     * Enable all sides for input and output
     */
    public void enableAllSides() {
        for (int i = 0; i < 6; i++) {
            inputSides[i] = true;
            outputSides[i] = true;
            enabledSides[i] = true;
        }
    }
    
    /**
     * Disable all sides
     */
    public void disableAllSides() {
        for (int i = 0; i < 6; i++) {
            inputSides[i] = false;
            outputSides[i] = false;
            enabledSides[i] = false;
        }
    }
    
    /**
     * Check if a side can accept energy input
     */
    public boolean canAcceptEnergyFrom(Direction side) {
        return side != null && inputSides[side.ordinal()];
    }
    
    /**
     * Check if a side can output energy
     */
    public boolean canOutputEnergyTo(Direction side) {
        return side != null && outputSides[side.ordinal()];
    }
    
    /**
     * Check if a side is enabled for any energy operation
     */
    public boolean isSideEnabled(Direction side) {
        return side != null && enabledSides[side.ordinal()];
    }
    
    /**
     * Get the number of enabled input sides
     */
    public int getInputSideCount() {
        int count = 0;
        for (boolean input : inputSides) {
            if (input) count++;
        }
        return count;
    }
    
    /**
     * Get the number of enabled output sides
     */
    public int getOutputSideCount() {
        int count = 0;
        for (boolean output : outputSides) {
            if (output) count++;
        }
        return count;
    }
    
    /**
     * Check if there are any enabled input sides
     */
    public boolean hasInputSides() {
        return getInputSideCount() > 0;
    }
    
    /**
     * Check if there are any enabled output sides
     */
    public boolean hasOutputSides() {
        return getOutputSideCount() > 0;
    }
    
    /**
     * Get all enabled input sides
     */
    public Direction[] getInputSides() {
        return getSides(inputSides);
    }
    
    /**
     * Get all enabled output sides
     */
    public Direction[] getOutputSides() {
        return getSides(outputSides);
    }
    
    /**
     * Get all enabled sides (input or output)
     */
    public Direction[] getEnabledSides() {
        return getSides(enabledSides);
    }
    
    private Direction[] getSides(boolean[] sideArray) {
        int count = 0;
        for (boolean enabled : sideArray) {
            if (enabled) count++;
        }
        
        Direction[] sides = new Direction[count];
        int index = 0;
        for (int i = 0; i < 6; i++) {
            if (sideArray[i]) {
                sides[index++] = Direction.values()[i];
            }
        }
        return sides;
    }
    
    private void updateEnabledState(Direction side) {
        enabledSides[side.ordinal()] = inputSides[side.ordinal()] || outputSides[side.ordinal()];
    }
    
    /**
     * Check if an adjacent block can provide energy
     */
    public boolean canReceiveFromAdjacent(Level level, BlockPos pos, Direction side) {
        if (!canAcceptEnergyFrom(side)) {
            return false;
        }
        
        IEnergyStorage energyStorage = getAdjacentEnergyStorage(level, pos, side);
        return energyStorage != null && energyStorage.canExtract();
    }
    
    /**
     * Check if an adjacent block can accept energy
     */
    public boolean canSendToAdjacent(Level level, BlockPos pos, Direction side) {
        if (!canOutputEnergyTo(side)) {
            return false;
        }
        
        IEnergyStorage energyStorage = getAdjacentEnergyStorage(level, pos, side);
        return energyStorage != null && energyStorage.canReceive();
    }
    
    /**
     * Get energy storage from adjacent block
     */
    public IEnergyStorage getAdjacentEnergyStorage(Level level, BlockPos pos, Direction side) {
        if (!isSideEnabled(side)) {
            // System.out.println("EnergySideManager: Side " + side + " is not enabled");
            return null;
        }

        BlockPos adjacentPos = pos.relative(side);
        BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);

        if (adjacentEntity == null) {
            // System.out.println("EnergySideManager: No block entity at " + adjacentPos);
            return null;
        }

        // System.out.println("EnergySideManager: Checking energy storage for " + adjacentEntity.getClass().getSimpleName() + " on " + side);

        // For Strange Matter machines, get the internal energy storage directly
        if (adjacentEntity instanceof BaseMachineBlockEntity machineEntity) {
            // System.out.println("EnergySideManager: Found Strange Matter machine, using internal storage");
            // Only return the storage if the machine can actually provide energy
            if (machineEntity.getEnergyRole() == BaseMachineBlockEntity.MachineEnergyRole.GENERATOR || 
                machineEntity.getEnergyRole() == BaseMachineBlockEntity.MachineEnergyRole.BOTH) {
                return machineEntity.getEnergyStorage();
            } else {
                // System.out.println("EnergySideManager: Machine is not a generator, cannot provide energy");
                return null;
            }
        }

        // For other blocks, try the standard NeoForge capability
        IEnergyStorage standardStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, adjacentPos, side.getOpposite());
        if (standardStorage != null) {
            // System.out.println("EnergySideManager: Found standard NeoForge energy storage");
            return standardStorage;
        }

        // Fall back to our custom EnergyAttachment system for non-machine blocks
        if (EnergyAttachment.hasEnergyStorage(adjacentEntity)) {
            // System.out.println("EnergySideManager: Found custom EnergyAttachment storage");
            return EnergyAttachment.getEnergyStorage(adjacentEntity);
        }

        // System.out.println("EnergySideManager: No energy storage found");
        return null;
    }
}
