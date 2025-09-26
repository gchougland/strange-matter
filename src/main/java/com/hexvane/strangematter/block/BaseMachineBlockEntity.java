package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerData;
import com.hexvane.strangematter.api.block.entity.IPacketHandlerTile;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Base class for machine block entities that provides common functionality.
 * This provides reusable machine logic for all Strange Matter machines.
 */
public abstract class BaseMachineBlockEntity extends BlockEntity implements Container, MenuProvider, IPacketHandlerTile {
    
    // Common machine properties
    protected int energyLevel = 0;
    protected int maxEnergyLevel = 100;
    protected boolean isActive = false;
    protected int tickCounter = 0;
    
    // Inventory management
    protected final NonNullList<ItemStack> items;
    protected final int inventorySize;
    
    // Container data for GUI synchronization
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyLevel;
                case 1 -> maxEnergyLevel;
                case 2 -> isActive ? 1 : 0;
                default -> 0;
            };
        }
        
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energyLevel = value;
                case 1 -> maxEnergyLevel = value;
                case 2 -> isActive = value != 0;
            }
        }
        
        @Override
        public int getCount() {
            return 3;
        }
    };
    
    public BaseMachineBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, int inventorySize) {
        super(blockEntityType, pos, state);
        this.inventorySize = inventorySize;
        this.items = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
    }
    
    /**
     * Main tick method - called every tick for processing
     */
    public static void tick(Level level, BlockPos pos, BlockState state, BaseMachineBlockEntity blockEntity) {
        blockEntity.tickCounter++;
        
        // Process machine logic every 20 ticks (1 second)
        if (blockEntity.tickCounter >= 20) {
            blockEntity.tickCounter = 0;
            blockEntity.processMachine();
        }
        
        // Handle client-side effects
        if (level.isClientSide) {
            blockEntity.clientTick();
        }
    }
    
    /**
     * Override this to implement machine-specific processing logic
     */
    protected abstract void processMachine();
    
    /**
     * Override this to implement client-side effects (particles, sounds, etc.)
     */
    protected void clientTick() {
        // Default: no client-side effects
    }
    
    /**
     * Check if the machine has enough energy to operate
     */
    protected boolean hasEnergy() {
        return energyLevel > 0;
    }
    
    /**
     * Consume energy from the machine
     */
    protected boolean consumeEnergy(int amount) {
        if (energyLevel >= amount) {
            energyLevel -= amount;
            setChanged();
            syncToClient();
            return true;
        }
        return false;
    }
    
    /**
     * Add energy to the machine
     */
    protected void addEnergy(int amount) {
        energyLevel = Math.min(energyLevel + amount, maxEnergyLevel);
        setChanged();
        syncToClient();
    }
    
    /**
     * Set the machine's active state
     */
    protected void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            setChanged();
            syncToClient();
        }
    }
    
    /**
     * Sync data to client for GUI updates
     */
    protected void syncToClient() {
        if (level != null && !level.isClientSide) {
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
    
    // Container interface implementation
    @Override
    public int getContainerSize() {
        return inventorySize;
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
    }
    
    @Override
    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
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
        return true; // Override in subclasses for specific slot restrictions
    }
    
    // MenuProvider interface implementation
    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }
    
    @Override
    public AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory playerInventory, Player player) {
        return createMenu(id, playerInventory);
    }
    
    /**
     * Override this to create the specific menu for this machine
     */
    protected abstract AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory playerInventory);
    
    // NBT serialization
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energy_level", energyLevel);
        tag.putInt("max_energy_level", maxEnergyLevel);
        tag.putBoolean("is_active", isActive);
        tag.putInt("tick_counter", tickCounter);
        ContainerHelper.saveAllItems(tag, this.items);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energyLevel = tag.getInt("energy_level");
        maxEnergyLevel = tag.getInt("max_energy_level");
        isActive = tag.getBoolean("is_active");
        tickCounter = tag.getInt("tick_counter");
        ContainerHelper.loadAllItems(tag, this.items);
    }
    
    // Getters for GUI access
    public int getEnergyLevel() {
        return energyLevel;
    }
    
    public int getMaxEnergyLevel() {
        return maxEnergyLevel;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public ContainerData getDataAccess() {
        return dataAccess;
    }
    
    // IPacketHandlerTile implementation
    @Override
    public FriendlyByteBuf getStatePacket(FriendlyByteBuf buffer) {
        buffer.writeInt(energyLevel);
        buffer.writeInt(maxEnergyLevel);
        buffer.writeBoolean(isActive);
        writeAdditionalStateData(buffer);
        return buffer;
    }
    
    @Override
    public void handleStatePacket(FriendlyByteBuf buffer) {
        energyLevel = buffer.readInt();
        maxEnergyLevel = buffer.readInt();
        isActive = buffer.readBoolean();
        readAdditionalStateData(buffer);
    }
    
    /**
     * Override this method in subclasses to add additional data to state packets.
     */
    protected void writeAdditionalStateData(FriendlyByteBuf buffer) {
        // Override in subclasses
    }
    
    /**
     * Override this method in subclasses to read additional data from state packets.
     */
    protected void readAdditionalStateData(FriendlyByteBuf buffer) {
        // Override in subclasses
    }
    
    @Override
    public FriendlyByteBuf getGuiPacket(FriendlyByteBuf buffer) {
        return getStatePacket(buffer);
    }
    
    @Override
    public void handleGuiPacket(FriendlyByteBuf buffer) {
        handleStatePacket(buffer);
    }
    
    /**
     * Send state packet to client. Override in subclasses for additional data.
     */
    public void sendStatePacket() {
        if (level != null && !level.isClientSide) {
            // TODO: Implement packet sending through network system
        }
    }
}
