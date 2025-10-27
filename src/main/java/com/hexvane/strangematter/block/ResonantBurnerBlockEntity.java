package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.ContainerHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.menu.ResonantBurnerMenu;
import net.neoforged.neoforge.common.CommonHooks;

public class ResonantBurnerBlockEntity extends BaseMachineBlockEntity {

    // Fuel burning system
    private int burnTime = 0;
    private int burnDuration = 0;
    private int fuelSlot = 0; // Single fuel slot
    
    // Energy generation (now read from config)
    private int energyPerTick;
    private int maxEnergyStorage;
    
    // Machine inventory - fuel slot only
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    
    // Override ContainerData to provide burn time instead of progress
    private final net.minecraft.world.inventory.ContainerData burnDataAccess = new net.minecraft.world.inventory.ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> isActive ? 1 : 0;
                case 3 -> energyPerTick;
                case 4 -> maxEnergyStorage;
                case 5 -> burnTime;
                case 6 -> burnDuration;
                default -> 0;
            };
        }
        
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energyStorage.setEnergy(value);
                case 1 -> { /* Energy storage capacity is set in constructor, no need to change it */ }
                case 2 -> isActive = value != 0;
                case 3 -> energyPerTick = value;
                case 4 -> maxEnergyStorage = value;
                case 5 -> burnTime = value;
                case 6 -> burnDuration = value;
            }
        }
        
        @Override
        public int getCount() {
            return 7;
        }
    };
    
    public ResonantBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.RESONANT_BURNER_BLOCK_ENTITY.get(), pos, state, 1);
        
        // Configure energy system for Resonant Burner from config
        this.energyPerTick = com.hexvane.strangematter.Config.resonantBurnerEnergyPerTick;
        this.maxEnergyStorage = com.hexvane.strangematter.Config.resonantBurnerEnergyStorage;
    }
    
    @Override
    protected void initializeEnergySides() {
        // Burners only output energy - no input
        // Configure energy input sides (NO INPUT - this is a generator)
        boolean[] inputSides = {false, false, false, false, false, false};
        this.setEnergyInputSides(inputSides);
        
        // Configure energy output sides (all sides for power distribution)
        boolean[] outputSides = {true, true, true, true, true, true};
        this.setEnergyOutputSides(outputSides);
    }
    
    @Override
    protected int getEnergyTransferRate() {
        return com.hexvane.strangematter.Config.resonantBurnerTransferRate;
    }
    
    @Override
    public MachineEnergyRole getEnergyRole() {
        return MachineEnergyRole.GENERATOR; // Explicitly define as generator
    }
    
    @Override
    protected void processMachine() {
        // Process fuel burning and energy generation every tick
        processFuelBurning();
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, ResonantBurnerBlockEntity blockEntity) {
        // Use the base class tick method which calls processMachine() every tick
        BaseMachineBlockEntity.tick(level, pos, state, blockEntity);
    }
    
    private void processFuelBurning() {
        if (level != null && !level.isClientSide) {
            boolean wasBurning = burnTime > 0;
            boolean shouldUpdate = false;
            
            // If we're burning fuel, consume burn time and generate energy
            if (burnTime > 0) {
                burnTime--;
                setActive(true);
                
                // Generate energy while burning
                if (energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored()) {
                    int energyToAdd = Math.min(energyPerTick, energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
                    if (energyToAdd > 0) {
                        int oldEnergy = energyStorage.getEnergyStored();
                        energyStorage.receiveEnergy(energyToAdd, false);
                        shouldUpdate = true;
                        syncEnergyToClient();
                        
                    }
                }
            } else {
                setActive(false);
            }
            
            // If we're not burning and have fuel, start burning
            if (burnTime <= 0 && !items.get(fuelSlot).isEmpty()) {
                ItemStack fuelStack = items.get(fuelSlot);
                int burnTimeForFuel = fuelStack.getItem().getBurnTime(fuelStack, null);
                
                if (burnTimeForFuel > 0) {
                    burnDuration = burnTimeForFuel;
                    burnTime = burnTimeForFuel;
                    
                    // Consume fuel
                    if (fuelStack.hasCraftingRemainingItem()) {
                        // If fuel has a container item (like bucket), replace with container
                        items.set(fuelSlot, fuelStack.getCraftingRemainingItem());
                    } else {
                        // Otherwise, shrink the stack
                        fuelStack.shrink(1);
                        if (fuelStack.isEmpty()) {
                            items.set(fuelSlot, ItemStack.EMPTY);
                        }
                    }
                    
                    shouldUpdate = true;
                }
            }
            
            // Update if state changed
            if (wasBurning != (burnTime > 0) || shouldUpdate) {
                setChanged();
                syncToClient();
            }
        }
    }
    
    @Override
    protected void clientTick() {
        // Add client-side effects like particles when burning
        if (burnTime > 0 && level != null && level.getRandom().nextFloat() < 0.1f) {
            // Spawn flame particles occasionally
            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 0.5;
            
            level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, 
                x + (level.getRandom().nextDouble() - 0.5) * 0.5, 
                y + 0.1, 
                z + (level.getRandom().nextDouble() - 0.5) * 0.5, 
                0, 0.1, 0);
        }
    }
    
    // Container interface methods
    @Override
    public int getContainerSize() {
        return items.size();
    }
    
    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public ItemStack getItem(int index) {
        return this.items.get(index);
    }
    
    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(this.items, index, count);
    }
    
    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.items, index);
    }
    
    @Override
    public void setItem(int index, ItemStack stack) {
        this.items.set(index, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        setChanged();
        syncToClient();
    }
    
    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr((double)this.worldPosition.getX() + 0.5D,
                (double)this.worldPosition.getY() + 0.5D,
                (double)this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }
    
    @Override
    public void clearContent() {
        this.items.clear();
    }
    
    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        // Only allow fuel items in the fuel slot
        if (index == fuelSlot) {
            return stack.getItem().getBurnTime(stack, null) > 0;
        }
        return false;
    }
    
    // MenuProvider interface methods
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.strangematter.resonant_burner");
    }
    
    @Override
    protected AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory playerInventory) {
        return new ResonantBurnerMenu(id, playerInventory, this.getBlockPos());
    }
    
    // Get burn progress for GUI
    public int getBurnTime() {
        return burnTime;
    }
    
    public int getBurnDuration() {
        return burnDuration;
    }
    
    public float getBurnProgress() {
        if (burnDuration <= 0) return 0.0f;
        return (float) burnTime / (float) burnDuration;
    }
    
    @Override
    public net.minecraft.world.inventory.ContainerData getDataAccess() {
        return burnDataAccess;
    }
    
    // Override base packet methods to include burn-specific data
    @Override
    protected void writeAdditionalStateData(FriendlyByteBuf buffer) {
        buffer.writeInt(burnTime);
        buffer.writeInt(burnDuration);
    }
    
    @Override
    protected void readAdditionalStateData(FriendlyByteBuf buffer) {
        burnTime = buffer.readInt();
        burnDuration = buffer.readInt();
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("burn_time", burnTime);
        tag.putInt("burn_duration", burnDuration);
        tag.putInt("fuel_slot", fuelSlot);
        tag.putInt("energy_per_tick", energyPerTick);
        tag.putInt("max_energy_storage", maxEnergyStorage);
        ContainerHelper.saveAllItems(tag, this.items, provider);
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        burnTime = tag.getInt("burn_time");
        burnDuration = tag.getInt("burn_duration");
        fuelSlot = tag.getInt("fuel_slot");
        energyPerTick = tag.getInt("energy_per_tick");
        maxEnergyStorage = tag.getInt("max_energy_storage");
        ContainerHelper.loadAllItems(tag, this.items, provider);
    }
}
