package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.BaseAnomalyEntity;
import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.research.ScannableObjectRegistry;
import com.hexvane.strangematter.menu.ResonanceCondenserMenu;

public class ResonanceCondenserBlockEntity extends BaseMachineBlockEntity {

    private int tickCounter = 0;
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY); // Single output slot
    
    // Cached anomaly for efficiency
    private BaseAnomalyEntity cachedAnomaly = null;
    private int anomalyCheckCounter = 0;
    
    // Machine inventory - this is what the GUI actually uses
    private final net.neoforged.neoforge.items.IItemHandler machineInventory = new net.neoforged.neoforge.items.wrapper.InvWrapper(this);
    
    public ResonanceCondenserBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.RESONANCE_CONDENSER_BLOCK_ENTITY.get(), pos, state, 1);
        
        // Configure energy system for Resonance Condenser from config
        this.energyPerTick = com.hexvane.strangematter.Config.resonanceCondenserEnergyPerTick;
        this.maxEnergyStorage = com.hexvane.strangematter.Config.resonanceCondenserEnergyStorage;
    }
    
    @Override
    protected void initializeEnergySides() {
        // Configure energy input sides (all sides for receiving power)
        boolean[] inputSides = {true, true, true, true, true, true}; // All sides by default
        this.setEnergyInputSides(inputSides);
        
        // No energy output by default
        boolean[] outputSides = {false, false, false, false, false, false};
        this.setEnergyOutputSides(outputSides);
    }
    
    @Override
    protected int getEnergyTransferRate() {
        return 1000; // Use default transfer rate for condenser
    }
    
    @Override
    public MachineEnergyRole getEnergyRole() {
        return MachineEnergyRole.CONSUMER; // Explicitly define as consumer
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, ResonanceCondenserBlockEntity blockEntity) {
        blockEntity.tickCounter++;
        
        // Call parent tick method to handle energy processing
        BaseMachineBlockEntity.tick(level, pos, state, blockEntity);
        
        // Only process machine logic on server side
        if (!level.isClientSide) {
            blockEntity.processResonance();
        }
        
        // Particles are now handled by client-side event handler
    }
    
    private void processResonance() {
        if (!level.isClientSide) {
            // Only search for anomalies every 20 ticks (1 second) to improve performance
            anomalyCheckCounter++;
            if (anomalyCheckCounter >= 20 || cachedAnomaly == null || !cachedAnomaly.isAlive()) {
                anomalyCheckCounter = 0;
                findNearbyAnomaly();
            }
            
            // Only consume energy and operate if there's a valid cached anomaly
            if (cachedAnomaly != null && cachedAnomaly.isAlive()) {
                // Check if we have enough energy to operate
                if (!hasEnergy() || !consumeEnergy(energyPerTick)) {
                    setActive(false);
                    return;
                }
                
                setActive(true);
                
                int progressSpeed = com.hexvane.strangematter.Config.resonanceCondenserProgressSpeed;
                if (this.tickCounter >= progressSpeed) {
                    this.tickCounter = 0;
                    // Progress increases only if an anomaly is found AND we have energy
                    if (progressLevel < maxProgressLevel) {
                        progressLevel = Math.min(progressLevel + 1, maxProgressLevel);
                        setChanged();
                        syncToClient(); // Sync to client for GUI updates
                    }
                    
                    // Generate shard when progress level reaches maximum
                    if (progressLevel >= maxProgressLevel) {
                        generateShardFromNearbyAnomaly();
                        // Reset progress level after generating shard
                        progressLevel = 0;
                        setChanged();
                        syncToClient();
                    }
                }
            } else {
                // No valid anomaly nearby, stop operating
                setActive(false);
                cachedAnomaly = null; // Clear the cache
            }
        }
    }
    
    private void findNearbyAnomaly() {
        BlockPos pos = this.getBlockPos();
        cachedAnomaly = null;
        
        for (Entity entity : level.getEntitiesOfClass(Entity.class,
            net.minecraft.world.phys.AABB.ofSize(pos.getCenter(), 20, 20, 20))) {

            if (entity instanceof BaseAnomalyEntity anomaly && anomaly.isAlive()) {
                cachedAnomaly = anomaly;
                break;
            }
        }
    }
    
    private void generateShardFromNearbyAnomaly() {
        if (level == null) {
            System.out.println("generateShardFromNearbyAnomaly: level is null!");
            return;
        }
        
        if (cachedAnomaly == null || !cachedAnomaly.isAlive()) {
            return; // No valid cached anomaly
        }
        
        Item shardItem = null;
        
        // Use the cached anomaly to determine shard type based on research type
        var scannableOpt = ScannableObjectRegistry.getScannableForEntity(cachedAnomaly);
        if (scannableOpt.isPresent()) {
            ResearchType researchType = scannableOpt.get().getResearchType();
            shardItem = getShardItemForResearchType(researchType);
        }
        
        if (shardItem != null) {
            // Try to add shard to machine inventory
            ItemStack shardStack = new ItemStack(shardItem);
            ItemStack currentStack = items.get(0);
            
            if (currentStack.isEmpty()) {
                // Empty slot - place the shard
                setItem(0, shardStack);
                setChanged();
                syncToClient();
            } else if (currentStack.is(shardItem) && currentStack.getCount() < currentStack.getMaxStackSize()) {
                // Same item type - stack it
                currentStack.grow(1);
                setChanged();
                syncToClient();
            }
            // If slot is full with different item, don't generate shard
        }
    }
    
    private Item getShardItemForResearchType(ResearchType researchType) {
        switch (researchType) {
            case GRAVITY:
                return StrangeMatterMod.GRAVITIC_SHARD.get();
            case TIME:
                return StrangeMatterMod.CHRONO_SHARD.get();
            case SPACE:
                return StrangeMatterMod.SPATIAL_SHARD.get();
            case SHADOW:
                return StrangeMatterMod.SHADE_SHARD.get();
            case COGNITION:
                return StrangeMatterMod.INSIGHT_SHARD.get();
            case ENERGY:
                return StrangeMatterMod.ENERGETIC_SHARD.get();
            default:
                return StrangeMatterMod.GRAVITIC_SHARD.get(); // Fallback
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
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
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
        // Only allow placing in the output slot if it's empty or stackable
        if (index == 0) {
            ItemStack currentStack = items.get(0);
            return currentStack.isEmpty() || 
                   (currentStack.is(stack.getItem()) && currentStack.getCount() < currentStack.getMaxStackSize());
        }
        return false;
    }
    
    // Method to sync data to client
    public void syncToClient() {
        if (level != null && !level.isClientSide) {
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
    
    // Energy arcs are now rendered client-side by the block entity renderer
    // No server-side particle spawning needed
    
    
    // Get progress level for GUI display
    public int getProgressLevel() {
        return progressLevel;
    }
    
    public int getMaxProgressLevel() {
        return maxProgressLevel;
    }
    
    public ContainerData getDataAccess() {
        return dataAccess;
    }
    
    // Override base packet methods to include progress-specific data
    @Override
    protected void writeAdditionalStateData(FriendlyByteBuf buffer) {
        buffer.writeInt(progressLevel);
        buffer.writeInt(maxProgressLevel);
    }
    
    @Override
    protected void readAdditionalStateData(FriendlyByteBuf buffer) {
        progressLevel = buffer.readInt();
        maxProgressLevel = buffer.readInt();
    }
    
    public void sendGuiNetworkData(AbstractContainerMenu container, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            sendStatePacket(); // Use base packet system
        }
    }
    
    @Override
    protected AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory playerInventory) {
        return new ResonanceCondenserMenu(id, playerInventory, this.getBlockPos());
    }
    
    @Override
    protected void processMachine() {
        // Process resonance absorption and shard generation
        processResonance();
    }
    
    // MenuProvider interface methods
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.strangematter.resonance_condenser");
    }


    
    
    public void setProgressLevel(int level) {
        this.progressLevel = Math.max(0, Math.min(level, maxProgressLevel));
        setChanged();
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
        setChanged();
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("progress_level", progressLevel);
        tag.putBoolean("is_active", isActive);
        tag.putInt("tick_counter", tickCounter);
        tag.putInt("anomaly_check_counter", anomalyCheckCounter);
        ContainerHelper.saveAllItems(tag, this.items, provider);
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        progressLevel = tag.getInt("progress_level");
        isActive = tag.getBoolean("is_active");
        tickCounter = tag.getInt("tick_counter");
        anomalyCheckCounter = tag.getInt("anomaly_check_counter");
        ContainerHelper.loadAllItems(tag, this.items, provider);
        
        // Clear cached anomaly on load since entity references don't persist
        cachedAnomaly = null;
    }
}
