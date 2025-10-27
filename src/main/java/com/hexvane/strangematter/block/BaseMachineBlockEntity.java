package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.network.FriendlyByteBuf;
import com.hexvane.strangematter.energy.EnergyAttachment;
import com.hexvane.strangematter.energy.MachineEnergyStorage;
import com.hexvane.strangematter.energy.EnergySideManager;
import com.hexvane.strangematter.energy.EnergyTransferManager;
import com.hexvane.strangematter.energy.ResonanceEnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Base class for machine block entities that provides common functionality.
 * This provides reusable machine logic for all Strange Matter machines.
 */
public abstract class BaseMachineBlockEntity extends BlockEntity implements Container, MenuProvider, IPacketHandlerTile {
    
    // Common machine properties
    protected boolean isActive = false;
    
    // Energy system - properly integrated with NeoForge
    protected final MachineEnergyStorage energyStorage;
    protected final EnergySideManager sideManager;
    protected EnergyTransferManager transferManager;
    protected int energyPerTick = 1;
    protected int maxEnergyStorage = 1000;
    
    // Compatibility energy storage for external access
    private ResonanceEnergyStorage compatibilityEnergyStorage;
    
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
                case 7 -> sideManager.getInputSideCount();
                case 8 -> sideManager.getOutputSideCount();
                default -> 0;
            };
        }
        
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energyStorage.setEnergy(value);
                case 2 -> isActive = value != 0;
                case 3 -> energyPerTick = value;
                case 4 -> maxEnergyStorage = value;
                case 5 -> progressLevel = value;
                case 6 -> maxProgressLevel = value;
                // Note: Energy storage capacity and side counts are read-only
            }
        }
        
        @Override
        public int getCount() {
            return 9;
        }
    };
    
    public BaseMachineBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, int inventorySize) {
        super(blockEntityType, pos, state);
        this.inventorySize = inventorySize;
        this.items = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
        
        // Initialize energy system
        this.energyStorage = new MachineEnergyStorage(maxEnergyStorage, 1000, 1000, this::onEnergyChanged);
        this.sideManager = new EnergySideManager();
        this.transferManager = new EnergyTransferManager(energyStorage, sideManager, pos, level);
        
        // Attach the energy storage to this block entity for NeoForge capabilities
        // Note: We need to use the old ResonanceEnergyStorage for compatibility
        this.compatibilityEnergyStorage = new ResonanceEnergyStorage(maxEnergyStorage, 1000, 1000);
        this.setData(EnergyAttachment.ENERGY_STORAGE, compatibilityEnergyStorage);
        
        // Initialize energy sides based on machine type
        initializeEnergySides();
    }
    
    /**
     * Initialize energy input/output sides based on machine type.
     * Override in subclasses to set appropriate sides.
     */
    protected abstract void initializeEnergySides();
    
    /**
     * Called when energy storage changes
     */
    protected void onEnergyChanged() {
        // Sync the compatibility energy storage with the internal storage
        if (compatibilityEnergyStorage != null) {
            compatibilityEnergyStorage.setEnergy(energyStorage.getEnergyStored());
        }
        setChanged();
    }
    
    /**
     * Sync the internal energy storage from the compatibility storage
     * This is called when external systems modify the compatibility storage
     */
    protected void syncFromCompatibilityStorage() {
        if (compatibilityEnergyStorage != null) {
            energyStorage.setEnergy(compatibilityEnergyStorage.getEnergyStored());
        }
    }
    
    /**
     * Main tick method - called every tick for processing
     */
    public static void tick(Level level, BlockPos pos, BlockState state, BaseMachineBlockEntity blockEntity) {
        // Only process energy and machine logic on server side
        if (!level.isClientSide) {
            // Role-based energy transfer - only call appropriate methods
            blockEntity.performRoleBasedEnergyTransfer();
            
            // Process machine logic every tick
            blockEntity.processMachine();
        }
        
        // Handle client-side effects
        if (level.isClientSide) {
            blockEntity.clientTick();
        }
    }
    
    /**
     * Called when a neighboring block changes - notify adjacent conduits
     */
    public void onNeighborChanged() {
        if (level == null) return;
        
        // Notify adjacent conduits that this machine has changed
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);
            
            if (adjacentEntity instanceof ResonantConduitBlockEntity adjacentConduit) {
                // Tell the adjacent conduit to update its connections
                adjacentConduit.notifyNeighborChanged();
            }
        }
    }
    
    /**
     * Override this to implement machine-specific processing logic
     */
    protected abstract void processMachine();
    
    /**
     * Perform role-based energy transfer based on machine type.
     * This prevents bidirectional energy flow that causes jumping energy bars.
     */
    protected void performRoleBasedEnergyTransfer() {
        MachineEnergyRole role = getEnergyRole();
        
        switch (role) {
            case GENERATOR:
                // Generators only send energy out, never receive
                trySendEnergy();
                break;
                
            case CONSUMER:
                // Consumers only receive energy, never send
                tryReceiveEnergy();
                break;
                
            case ENERGY_INDEPENDENT:
                // Energy-independent machines don't transfer energy at all
                break;
                
            case BOTH:
                // Machines that can both generate and consume (rare case)
                tryReceiveEnergy();
                trySendEnergy();
                break;
        }
    }
    
    /**
     * Determine the energy role of this machine.
     * Override this method in specific machine classes to define their role.
     */
    public MachineEnergyRole getEnergyRole() {
        boolean hasInputSides = sideManager.hasInputSides();
        boolean hasOutputSides = sideManager.hasOutputSides();
        
        if (hasInputSides && hasOutputSides) {
            return MachineEnergyRole.BOTH;
        } else if (hasInputSides) {
            return MachineEnergyRole.CONSUMER;
        } else if (hasOutputSides) {
            return MachineEnergyRole.GENERATOR;
        } else {
            return MachineEnergyRole.ENERGY_INDEPENDENT;
        }
    }
    
    /**
     * Check if this machine can receive energy from the given adjacent entity.
     * This prevents generators from receiving energy from other generators.
     */
    protected boolean canReceiveEnergyFrom(BlockEntity adjacentEntity) {
        // If the adjacent entity is a BaseMachineBlockEntity, check its role
        if (adjacentEntity instanceof BaseMachineBlockEntity adjacentMachine) {
            MachineEnergyRole adjacentRole = adjacentMachine.getEnergyRole();
            MachineEnergyRole ourRole = getEnergyRole();
            
            // Only allow receiving from generators or machines that can send energy
            return adjacentRole == MachineEnergyRole.GENERATOR || 
                   adjacentRole == MachineEnergyRole.BOTH;
        }
        
        // For non-BaseMachineBlockEntity (like conduits), allow receiving
        return true;
    }
    
    /**
     * Check if this machine can send energy to the given adjacent entity.
     * This prevents consumers from sending energy to other consumers.
     */
    protected boolean canSendEnergyTo(BlockEntity adjacentEntity) {
        // If the adjacent entity is a BaseMachineBlockEntity, check its role
        if (adjacentEntity instanceof BaseMachineBlockEntity adjacentMachine) {
            MachineEnergyRole adjacentRole = adjacentMachine.getEnergyRole();
            MachineEnergyRole ourRole = getEnergyRole();
            
            // Only allow sending to consumers or machines that can receive energy
            return adjacentRole == MachineEnergyRole.CONSUMER || 
                   adjacentRole == MachineEnergyRole.BOTH;
        }
        
        // For non-BaseMachineBlockEntity (like conduits), allow sending
        return true;
    }
    
    /**
     * Check if this machine can accept energy from a specific direction
     */
    public boolean canAcceptEnergyFrom(Direction direction) {
        return sideManager.canAcceptEnergyFrom(direction);
    }
    
    /**
     * Check if this machine can output energy to a specific direction
     */
    public boolean canOutputEnergyTo(Direction direction) {
        return sideManager.canOutputEnergyTo(direction);
    }
    
    /**
     * Enum defining the energy roles a machine can have
     */
    public enum MachineEnergyRole {
        GENERATOR,           // Only sends energy (ResonantBurner, RiftStabilizer)
        CONSUMER,           // Only receives energy (ResonanceCondenser)
        BOTH,               // Can both send and receive (rare)
        ENERGY_INDEPENDENT  // No energy transfer (RealityForge)
    }
    
    /**
     * Override this to implement client-side effects (particles, sounds, etc.)
     */
    protected void clientTick() {
        // Default: no client-side effects
    }
    
    /**
     * Check if the machine has enough energy to operate
     */
    public boolean hasEnergy() {
        return energyStorage.getEnergyStored() > 0;
    }
    
    /**
     * Consume energy from the machine
     */
    protected boolean consumeEnergy(int amount) {
        if (energyStorage.getEnergyStored() >= amount) {
            energyStorage.consumeEnergy(amount);
            return true;
        }
        return false;
    }
    
    /**
     * Add energy to the machine
     */
    protected void addEnergy(int amount) {
        energyStorage.addEnergy(amount);
    }
    
    /**
     * Get the energy transfer rate for this machine (can be overridden)
     */
    protected int getEnergyTransferRate() {
        return 1000; // Default 1000 RE/t
    }
    
    /**
     * Try to receive energy from adjacent blocks
     * Returns true if energy was received
     */
    protected boolean tryReceiveEnergy() {
        if (level == null || level.isClientSide) return false;
        
        // Update transfer manager with current level
        transferManager = new EnergyTransferManager(energyStorage, sideManager, worldPosition, level);
        
        int received = transferManager.tryReceiveEnergy(getEnergyTransferRate());
        return received > 0;
    }
    
    /**
     * Try to send energy to adjacent blocks
     * Returns true if energy was sent
     */
    protected boolean trySendEnergy() {
        if (level == null || level.isClientSide) return false;
        
        // Update transfer manager with current level
        transferManager = new EnergyTransferManager(energyStorage, sideManager, worldPosition, level);
        
        int sent = transferManager.trySendEnergy(getEnergyTransferRate());
        return sent > 0;
    }
    
    /**
     * Configure which sides can accept energy input
     */
    protected void setEnergyInputSides(boolean[] sides) {
        for (int i = 0; i < 6; i++) {
            sideManager.setInputSide(Direction.values()[i], sides[i]);
        }
    }
    
    /**
     * Configure which sides can output energy
     */
    protected void setEnergyOutputSides(boolean[] sides) {
        for (int i = 0; i < 6; i++) {
            sideManager.setOutputSide(Direction.values()[i], sides[i]);
        }
    }
    
    /**
     * Set energy input for a specific side
     */
    protected void setEnergyInputSide(Direction side, boolean canInput) {
        sideManager.setInputSide(side, canInput);
    }
    
    /**
     * Set energy output for a specific side
     */
    protected void setEnergyOutputSide(Direction side, boolean canOutput) {
        sideManager.setOutputSide(side, canOutput);
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
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("energy_stored", energyStorage.getEnergyStored());
        tag.putInt("max_energy_stored", energyStorage.getMaxEnergyStored());
        tag.putInt("energy_per_tick", energyPerTick);
        tag.putInt("max_energy_storage", maxEnergyStorage);
        tag.putBoolean("is_active", isActive);
        tag.putInt("progress_level", progressLevel);
        tag.putInt("max_progress_level", maxProgressLevel);
        
        // Save energy input/output sides
        CompoundTag energySidesTag = new CompoundTag();
        for (int i = 0; i < 6; i++) {
            energySidesTag.putBoolean("input_" + i, sideManager.canAcceptEnergyFrom(Direction.values()[i]));
            energySidesTag.putBoolean("output_" + i, sideManager.canOutputEnergyTo(Direction.values()[i]));
        }
        tag.put("energy_sides", energySidesTag);
        
        ContainerHelper.saveAllItems(tag, this.items, provider);
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        energyStorage.setEnergy(tag.getInt("energy_stored"));
        // Energy storage capacity is set in constructor, no need to change it
        energyPerTick = tag.getInt("energy_per_tick");
        maxEnergyStorage = tag.getInt("max_energy_storage");
        isActive = tag.getBoolean("is_active");
        progressLevel = tag.getInt("progress_level");
        maxProgressLevel = tag.getInt("max_progress_level");
        
        // Load energy input/output sides
        if (tag.contains("energy_sides")) {
            CompoundTag energySidesTag = tag.getCompound("energy_sides");
            for (int i = 0; i < 6; i++) {
                // Load energy sides - this is handled by initializeEnergySides() now
                // Load energy sides - this is handled by initializeEnergySides() now
            }
        }
        
        ContainerHelper.loadAllItems(tag, this.items, provider);
    }
    
    // Getters for GUI access
    public int getEnergyLevel() {
        return energyStorage.getEnergyStored();
    }
    
    public int getMaxEnergyLevel() {
        return energyStorage.getMaxEnergyStored();
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
        buffer.writeInt(energyStorage.getEnergyStored());
        buffer.writeInt(energyStorage.getMaxEnergyStored());
        buffer.writeBoolean(isActive);
        writeAdditionalStateData(buffer);
        return buffer;
    }
    
    @Override
    public void handleStatePacket(FriendlyByteBuf buffer) {
        int storedEnergy = buffer.readInt();
        int maxEnergy = buffer.readInt();
        isActive = buffer.readBoolean();
        
        // Update the actual energy storage
        energyStorage.setEnergy(storedEnergy);
        // Energy storage capacity is set in constructor, no need to change it
        
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
            com.hexvane.strangematter.network.MachineStatePacket.sendToClient(this);
        }
    }
    
    /**
     * Sync energy state to client - call this when energy changes
     */
    protected void syncEnergyToClient() {
        if (level != null && !level.isClientSide) {
            setChanged();
            // Trigger block entity data sync to client
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }
    
    
    // Energy system access methods
    public boolean canReceiveEnergy(Direction side) {
        return side == null || sideManager.canAcceptEnergyFrom(side);
    }
    
    public boolean canExtractEnergy(Direction side) {
        return side == null || sideManager.canOutputEnergyTo(side);
    }
    
    // Energy system getters
    public MachineEnergyStorage getEnergyStorage() {
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
