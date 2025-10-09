package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import com.hexvane.strangematter.energy.ResonanceEnergyStorage;
import com.hexvane.strangematter.StrangeMatterMod;

/**
 * Block entity for the Paradoxical Energy Cell.
 * Provides infinite energy to connected blocks.
 */
public class ParadoxicalEnergyCellBlockEntity extends BlockEntity {
    
    private final ResonanceEnergyStorage energyStorage;
    private final LazyOptional<IEnergyStorage> energyOptional;
    
    public ParadoxicalEnergyCellBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.PARADOXICAL_ENERGY_CELL_BLOCK_ENTITY.get(), pos, state);
        // Create infinite energy storage
        this.energyStorage = new ResonanceEnergyStorage(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.energyOptional = LazyOptional.of(() -> this.energyStorage);
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
                adjacentEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(adjacentStorage -> {
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
                });
            }
        }
    }
    
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        return super.getCapability(cap, side);
    }
    
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energy", energyStorage.getEnergyStored());
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energyStorage.setEnergy(tag.getInt("energy"));
    }
    
    // Note: getDisplayName() is not needed for block entities that don't have GUIs
    
    /**
     * Get the energy storage for GUI access
     */
    public ResonanceEnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}
