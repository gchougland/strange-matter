package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.hexvane.strangematter.energy.EnergyAttachment;
import com.hexvane.strangematter.energy.ResonanceEnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import com.hexvane.strangematter.StrangeMatterMod;

/**
 * Block entity for the Paradoxical Energy Cell.
 * Provides infinite energy to connected blocks.
 */
public class ParadoxicalEnergyCellBlockEntity extends BlockEntity {
    
    private final ResonanceEnergyStorage energyStorage;
    
    public ParadoxicalEnergyCellBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.PARADOXICAL_ENERGY_CELL_BLOCK_ENTITY.get(), pos, state);
        // Create infinite energy storage
        this.energyStorage = new ResonanceEnergyStorage(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        
        // Attach energy storage to this block entity
        EnergyAttachment.attachEnergyStorage(this, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, ParadoxicalEnergyCellBlockEntity blockEntity) {
        if (level.isClientSide) return;
        
        // Distribute energy to adjacent blocks
        blockEntity.distributeEnergy();
    }
    
    /**
     * Distribute energy to adjacent blocks that can accept it
     */
    private void distributeEnergy() {
        if (level == null) return;
        
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);
            
            if (adjacentEntity != null) {
                if (EnergyAttachment.hasEnergyStorage(adjacentEntity)) {
                    IEnergyStorage adjacentStorage = EnergyAttachment.getEnergyStorage(adjacentEntity);
                    if (adjacentStorage.canReceive()) {
                        // Try to transfer energy
                        int transferRate = com.hexvane.strangematter.Config.paradoxicalCellTransferRate;
                        int energyToTransfer = Math.min(transferRate, adjacentStorage.getMaxEnergyStored() - adjacentStorage.getEnergyStored());
                        if (energyToTransfer > 0) {
                            int energyTransferred = adjacentStorage.receiveEnergy(energyToTransfer, false);
                            if (energyTransferred > 0) {
                                // Energy was transferred successfully
                                setChanged();
                            }
                        }
                        }
                    }
            }
        }
    }
    
    // Energy system access methods
    public ResonanceEnergyStorage getEnergyStorage() {
        return energyStorage;
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("energy", energyStorage.getEnergyStored());
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        energyStorage.setEnergy(tag.getInt("energy"));
    }
    
    // Note: getDisplayName() is not needed for block entities that don't have GUIs
    
}
