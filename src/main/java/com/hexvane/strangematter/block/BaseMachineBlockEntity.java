package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import com.hexvane.strangematter.energy.ResonanceEnergyStorage;

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
    
    // Energy system
    protected final ResonanceEnergyStorage energyStorage;
    protected final LazyOptional<IEnergyStorage> energyOptional;
    protected int energyPerTick = 1;
    protected int maxEnergyStorage = 1000;
    protected boolean[] energyInputSides = {true, true, true, true, true, true}; // All sides by default
    protected boolean[] energyOutputSides = {false, false, false, false, false, false}; // No output by default
    
    // Progress system for machines that produce items
    protected int progressLevel = 0;
    protected int maxProgressLevel = 100;
    
    // Inventory management
    protected final NonNullList<ItemStack> items;
    protected final int inventorySize;
    
    // Container data for GUI synchronization
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> isActive ? 1 : 0;
                case 3 -> energyPerTick;
                case 4 -> maxEnergyStorage;
                case 5 -> progressLevel;
                case 6 -> maxProgressLevel;
                default -> 0;
            };
        }
        
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energyStorage.setEnergy(value);
                case 1 -> energyStorage.setCapacity(value);
                case 2 -> isActive = value != 0;
                case 3 -> energyPerTick = value;
                case 4 -> maxEnergyStorage = value;
                case 5 -> progressLevel = value;
                case 6 -> maxProgressLevel = value;
            }
        }
        
        @Override
        public int getCount() {
            return 7;
        }
    };
    
    public BaseMachineBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, int inventorySize) {
        super(blockEntityType, pos, state);
        this.inventorySize = inventorySize;
        this.items = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
        this.energyStorage = new ResonanceEnergyStorage(maxEnergyStorage, 1000, 1000);
        this.energyOptional = LazyOptional.of(() -> this.energyStorage);
    }
    
    /**
     * Main tick method - called every tick for processing
     */
    public static void tick(Level level, BlockPos pos, BlockState state, BaseMachineBlockEntity blockEntity) {
        blockEntity.tickCounter++;
        
        // Try to receive energy from adjacent blocks every tick
        blockEntity.tryReceiveEnergy();
        
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
        return energyStorage.getEnergyStored() > 0;
    }
    
    /**
     * Consume energy from the machine
     */
    protected boolean consumeEnergy(int amount) {
        if (energyStorage.getEnergyStored() >= amount) {
            energyStorage.extractEnergy(amount, false);
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
        energyStorage.receiveEnergy(amount, false);
        setChanged();
        syncToClient();
    }
    
    /**
     * Try to receive energy from adjacent blocks
     */
    protected void tryReceiveEnergy() {
        if (level == null || level.isClientSide) return;
        
        for (Direction direction : Direction.values()) {
            if (energyInputSides[direction.ordinal()]) {
                BlockPos adjacentPos = worldPosition.relative(direction);
                BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);
                
                if (adjacentEntity != null) {
                    adjacentEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(adjacentStorage -> {
                        if (adjacentStorage.canExtract() && energyStorage.canReceive()) {
                            int energyToReceive = Math.min(energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored(), 1000);
                            if (energyToReceive > 0) {
                                int energyReceived = adjacentStorage.extractEnergy(energyToReceive, false);
                                if (energyReceived > 0) {
                                    energyStorage.receiveEnergy(energyReceived, false);
                                    setChanged();
                                    syncToClient();
                                }
                            }
                        }
                    });
                }
            }
        }
    }
    
    /**
     * Try to send energy to adjacent blocks
     */
    protected void trySendEnergy() {
        if (level == null || level.isClientSide) return;
        
        for (Direction direction : Direction.values()) {
            if (energyOutputSides[direction.ordinal()]) {
                BlockPos adjacentPos = worldPosition.relative(direction);
                BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);
                
                if (adjacentEntity != null) {
                    adjacentEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(adjacentStorage -> {
                        if (adjacentStorage.canReceive() && energyStorage.canExtract()) {
                            int energyToSend = Math.min(energyStorage.getEnergyStored(), 1000);
                            if (energyToSend > 0) {
                                int energySent = adjacentStorage.receiveEnergy(energyToSend, false);
                                if (energySent > 0) {
                                    energyStorage.extractEnergy(energySent, false);
                                    setChanged();
                                    syncToClient();
                                }
                            }
                        }
                    });
                }
            }
        }
    }
    
    /**
     * Configure which sides can accept energy input
     */
    protected void setEnergyInputSides(boolean[] sides) {
        this.energyInputSides = sides.clone();
    }
    
    /**
     * Configure which sides can output energy
     */
    protected void setEnergyOutputSides(boolean[] sides) {
        this.energyOutputSides = sides.clone();
    }
    
    /**
     * Set energy input for a specific side
     */
    protected void setEnergyInputSide(Direction side, boolean canInput) {
        this.energyInputSides[side.ordinal()] = canInput;
    }
    
    /**
     * Set energy output for a specific side
     */
    protected void setEnergyOutputSide(Direction side, boolean canOutput) {
        this.energyOutputSides[side.ordinal()] = canOutput;
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
        tag.putInt("energy_stored", energyStorage.getEnergyStored());
        tag.putInt("max_energy_stored", energyStorage.getMaxEnergyStored());
        tag.putInt("energy_per_tick", energyPerTick);
        tag.putInt("max_energy_storage", maxEnergyStorage);
        tag.putBoolean("is_active", isActive);
        tag.putInt("tick_counter", tickCounter);
        tag.putInt("progress_level", progressLevel);
        tag.putInt("max_progress_level", maxProgressLevel);
        
        // Save energy input/output sides
        CompoundTag energySidesTag = new CompoundTag();
        for (int i = 0; i < 6; i++) {
            energySidesTag.putBoolean("input_" + i, energyInputSides[i]);
            energySidesTag.putBoolean("output_" + i, energyOutputSides[i]);
        }
        tag.put("energy_sides", energySidesTag);
        
        ContainerHelper.saveAllItems(tag, this.items);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energyStorage.setEnergy(tag.getInt("energy_stored"));
        energyStorage.setCapacity(tag.getInt("max_energy_stored"));
        energyPerTick = tag.getInt("energy_per_tick");
        maxEnergyStorage = tag.getInt("max_energy_storage");
        isActive = tag.getBoolean("is_active");
        tickCounter = tag.getInt("tick_counter");
        progressLevel = tag.getInt("progress_level");
        maxProgressLevel = tag.getInt("max_progress_level");
        
        // Load energy input/output sides
        if (tag.contains("energy_sides")) {
            CompoundTag energySidesTag = tag.getCompound("energy_sides");
            for (int i = 0; i < 6; i++) {
                energyInputSides[i] = energySidesTag.getBoolean("input_" + i);
                energyOutputSides[i] = energySidesTag.getBoolean("output_" + i);
            }
        }
        
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
    
    // Capability support
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
    
    // Energy system getters
    public ResonanceEnergyStorage getEnergyStorage() {
        return energyStorage;
    }
    
    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }
    
    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }
    
    public float getEnergyPercentage() {
        return energyStorage.getEnergyPercentage();
    }
    
    public int getEnergyPercentageInt() {
        return energyStorage.getEnergyPercentageInt();
    }
}
