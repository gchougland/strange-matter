package com.hexvane.strangematter.block;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.recipe.RealityForgeRecipe;
import com.hexvane.strangematter.recipe.RealityForgeRecipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class RealityForgeBlockEntity extends BaseMachineBlockEntity {
    
    // Note: BaseMachineBlockEntity provides inventory management through the 'items' field
    
    // Shard tracking
    private final Map<String, Integer> storedShards = new HashMap<>();
    private final List<String> shardOrder = new ArrayList<>();
    
    // Synchronized shard count (like burnTime in ResonantBurner)
    private int totalShardCount = 0;
    
    // Crafting state
    private boolean isCrafting = false;
    private int craftTicks = 0;
    private static final int CRAFT_TIME = 100; // 5 seconds at 20 TPS
    
    // Visual feedback for coalescing animation (happens during crafting)
    private boolean isCoalescing = false;
    
    // Store the recipe being crafted
    private RealityForgeRecipe currentRecipe = null;
    
    // Data synchronization - includes base class data + shard-specific data
    private final net.minecraft.world.inventory.ContainerData shardDataAccess = new net.minecraft.world.inventory.ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                // Base class data (indexes 0-6)
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> isActive ? 1 : 0;
                case 3 -> energyPerTick;
                case 4 -> maxEnergyStorage;
                case 5 -> progressLevel;
                case 6 -> maxProgressLevel;
                // Reality Forge specific data (indexes 7-11)
                case 7 -> storedShards.size(); // Number of shard types
                case 8 -> totalShardCount;     // Total number of shards (synchronized)
                case 9 -> isCrafting ? 1 : 0;  // Crafting state
                case 10 -> craftTicks;          // Craft progress
                case 11 -> isCoalescing ? 1 : 0; // Coalescing state (happens during crafting)
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                // Base class data
                case 0 -> energyStorage.setEnergy(value);
                case 1 -> energyStorage.setCapacity(value);
                case 2 -> isActive = value != 0;
                case 3 -> energyPerTick = value;
                case 4 -> maxEnergyStorage = value;
                case 5 -> progressLevel = value;
                case 6 -> maxProgressLevel = value;
                // Reality Forge specific data
                case 7 -> { /* storedShards.size() - sync handled by case 8 */ }
                case 8 -> {
                    totalShardCount = value; // Update synchronized shard count
                    // If totalShardCount is 0, clear all shards (client-side sync)
                    if (value == 0) {
                        storedShards.clear();
                        shardOrder.clear();
                    }
                }
                case 9 -> isCrafting = value != 0;
                case 10 -> craftTicks = value;
                case 11 -> isCoalescing = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 12;
        }
    };
    
    public RealityForgeBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.REALITY_FORGE_BLOCK_ENTITY.get(), pos, state, 11); // 11 slots: 9 crafting + 1 shard + 1 output
    }
    
    @Override
    protected void processMachine() {
        // Reality Forge specific processing logic
        if (isCrafting) {
            craftTicks++;
            if (craftTicks >= CRAFT_TIME) {
                completeCrafting();
            }
        }
    }
    
        // Static tick method for server-side ticking
    public static void tick(Level level, BlockPos pos, BlockState state, RealityForgeBlockEntity blockEntity) {
        if (blockEntity.isCrafting) {
            blockEntity.craftTicks++;

            // Start coalescing animation when crafting begins
            if (blockEntity.craftTicks == 1) {
                blockEntity.isCoalescing = true;
                blockEntity.setChanged();
            }

            if (blockEntity.craftTicks >= CRAFT_TIME) {
                // Double-check that we're still crafting and have a recipe before completing
                if (blockEntity.isCrafting && blockEntity.currentRecipe != null) {
                    blockEntity.completeCrafting();
                } else {
                    // Something went wrong, just stop crafting
                    blockEntity.stopCrafting();
                }
            }
        }
    }
    
    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        
        // Note: Removed aggressive safeguard that was causing crashes
        // The recipe is "locked in" once crafting starts
        
        // Trigger crafting attempt when items are placed in the crafting grid (slots 0-8)
        if (slot >= 0 && slot < 9) {
            attemptCraft();
        }
    }
    
    @Override
    protected AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory playerInventory) {
        return new com.hexvane.strangematter.menu.RealityForgeMenu(id, playerInventory, this.getBlockPos());
    }
    
    // Note: BaseMachineBlockEntity handles capability management
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); // BaseMachineBlockEntity handles inventory serialization
        tag.putInt("craftTicks", craftTicks);
        tag.putBoolean("isCrafting", isCrafting);
        
        // Save shard data
        CompoundTag shardTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : storedShards.entrySet()) {
            shardTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("storedShards", shardTag);
        
        // Save shard order
        net.minecraft.nbt.ListTag shardOrderTag = new net.minecraft.nbt.ListTag();
        for (String shard : shardOrder) {
            shardOrderTag.add(net.minecraft.nbt.StringTag.valueOf(shard));
        }
        tag.put("shardOrder", shardOrderTag);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag); // BaseMachineBlockEntity handles inventory deserialization
        craftTicks = tag.getInt("craftTicks");
        isCrafting = tag.getBoolean("isCrafting");
        
        // Load shard data
        if (tag.contains("storedShards")) {
            CompoundTag shardTag = tag.getCompound("storedShards");
            storedShards.clear();
            for (String key : shardTag.getAllKeys()) {
                storedShards.put(key, shardTag.getInt(key));
            }
        }
        
        // Load shard order
        if (tag.contains("shardOrder")) {
            shardOrder.clear();
            net.minecraft.nbt.ListTag shardOrderTag = tag.getList("shardOrder", net.minecraft.nbt.Tag.TAG_STRING);
            for (int i = 0; i < shardOrderTag.size(); i++) {
                shardOrder.add(shardOrderTag.getString(i));
            }
        }
    }
    
    // Note: BaseMachineBlockEntity handles capability management
    
    // Note: BaseMachineBlockEntity provides Container implementation
    
    // Shard management
    public int insertShard(ItemStack shardStack) {
        String shardType = getShardType(shardStack);
        if (shardType == null) return 0;
        
        // Check how many shards we can still accept
        int totalShards = storedShards.values().stream().mapToInt(Integer::intValue).sum();
        int maxShards = 6;
        int availableSlots = maxShards - totalShards;
        
        if (availableSlots <= 0) return 0;
        
        // Insert as many shards as possible (up to the stack size or available slots)
        int shardsToInsert = Math.min(shardStack.getCount(), availableSlots);
        
        storedShards.put(shardType, storedShards.getOrDefault(shardType, 0) + shardsToInsert);
        if (!shardOrder.contains(shardType)) {
            shardOrder.add(shardType);
        }
        
        // Update synchronized shard count
        totalShardCount = storedShards.values().stream().mapToInt(Integer::intValue).sum();
        
        // Play insertion sound
        if (level != null && !level.isClientSide && shardsToInsert > 0) {
            level.playSound(null, worldPosition, 
                com.hexvane.strangematter.sound.StrangeMatterSounds.REALITY_FORGE_INSERT.get(), 
                net.minecraft.sounds.SoundSource.BLOCKS, 0.7f, 1.0f);
        }
        
        // Attempt to craft after inserting shards
        if (shardsToInsert > 0 && !isCrafting) {
            attemptCraft();
        }
        
        setChanged();
        
        // Force packet update to ensure client gets the updated shard data
        if (level != null && !level.isClientSide) {
            // Additional setChanged to ensure packet is sent
            setChanged();
        }
        
        return shardsToInsert;
    }
    
    public void ejectShards(Player player) {
        // Stop crafting if currently crafting (safeguard)
        if (isCrafting) {
            stopCrafting();
        }
        
        for (String shardType : new ArrayList<>(shardOrder)) {
            int count = storedShards.getOrDefault(shardType, 0);
            if (count > 0) {
                ItemStack shardStack = createShardStack(shardType, count);
                if (!player.getInventory().add(shardStack)) {
                    // Drop on ground if inventory is full
                    player.drop(shardStack, false);
                }
                storedShards.remove(shardType);
            }
        }
        shardOrder.clear();
        
        // Reset synchronized shard count
        totalShardCount = 0;
        
        setChanged();
    }
    
    // Client-side method to immediately clear shards for visual feedback
    public void clearShardsForEject() {
        // Stop crafting if currently crafting (safeguard)
        if (isCrafting) {
            stopCrafting();
        }
        
        storedShards.clear();
        shardOrder.clear();
        totalShardCount = 0;
        setChanged();
    }
    
    private String getShardType(ItemStack stack) {
        if (stack.getItem() == StrangeMatterMod.ENERGETIC_SHARD.get()) return "energetic";
        if (stack.getItem() == StrangeMatterMod.GRAVITIC_SHARD.get()) return "gravitic";
        if (stack.getItem() == StrangeMatterMod.CHRONO_SHARD.get()) return "chrono";
        if (stack.getItem() == StrangeMatterMod.SPATIAL_SHARD.get()) return "spatial";
        if (stack.getItem() == StrangeMatterMod.SHADE_SHARD.get()) return "shade";
        if (stack.getItem() == StrangeMatterMod.INSIGHT_SHARD.get()) return "insight";
        return null;
    }
    
    private ItemStack createShardStack(String shardType, int count) {
        return switch (shardType) {
            case "energetic" -> new ItemStack(StrangeMatterMod.ENERGETIC_SHARD.get(), count);
            case "gravitic" -> new ItemStack(StrangeMatterMod.GRAVITIC_SHARD.get(), count);
            case "chrono" -> new ItemStack(StrangeMatterMod.CHRONO_SHARD.get(), count);
            case "spatial" -> new ItemStack(StrangeMatterMod.SPATIAL_SHARD.get(), count);
            case "shade" -> new ItemStack(StrangeMatterMod.SHADE_SHARD.get(), count);
            case "insight" -> new ItemStack(StrangeMatterMod.INSIGHT_SHARD.get(), count);
            default -> ItemStack.EMPTY;
        };
    }
    
    // Crafting logic
    public void attemptCraft() {
        if (isCrafting) return;
        
        // Check if we have a valid recipe
        RealityForgeRecipe recipe = RealityForgeRecipeRegistry.findMatchingRecipe(this);
        if (recipe != null) {
            startCrafting(recipe);
        }
    }
    
    private void startCrafting(RealityForgeRecipe recipe) {
        isCrafting = true;
        craftTicks = 0;
        currentRecipe = recipe; // Store the recipe for later use
        setChanged();
    }
    
    
    
    private void stopCrafting() {
        // Reset all crafting states without completing the craft
        isCrafting = false;
        isCoalescing = false;
        craftTicks = 0;
        currentRecipe = null;
        setChanged();
    }
    
    private void completeCrafting() {
        // Safety check - if currentRecipe is null, just reset states
        if (currentRecipe == null) {
            System.out.println("DEBUG: completeCrafting() called with null currentRecipe!");
            stopCrafting();
            return;
        }
        
        // Store recipe reference locally to prevent race conditions
        RealityForgeRecipe recipe = currentRecipe;
        
        // Consume ingredients (only one item from each stack)
        for (int i = 0; i < 9; i++) {
            ItemStack currentStack = getItem(i);
            if (!currentStack.isEmpty()) {
                // Shrink the stack by 1
                currentStack.shrink(1);
                if (currentStack.isEmpty()) {
                    setItem(i, ItemStack.EMPTY);
                } else {
                    setItem(i, currentStack);
                }
            }
        }

        // Consume shards
        for (Map.Entry<String, Integer> requirement : recipe.getShardRequirements().entrySet()) {
                String shardType = requirement.getKey();
                int required = requirement.getValue();
                int current = storedShards.getOrDefault(shardType, 0);
                if (current >= required) {
                    storedShards.put(shardType, current - required);
                    if (storedShards.get(shardType) == 0) {
                        storedShards.remove(shardType);
                        shardOrder.remove(shardType);
                    }
                }
            }

            // Set output
            setItem(10, currentRecipe.getResultItem(level.registryAccess()).copy());

            // Play sound
            if (level != null && !level.isClientSide) {
                level.playSound(null, worldPosition, 
                    com.hexvane.strangematter.sound.StrangeMatterSounds.REALITY_FORGE_CRAFT.get(), 
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
            }

        // Reset all states
        isCrafting = false;
        isCoalescing = false;
        craftTicks = 0;
        currentRecipe = null;
        
        // Clear all remaining shards after crafting
        storedShards.clear();
        shardOrder.clear();
        totalShardCount = 0;
        
        setChanged();
        
        // Force packet update to ensure client gets the cleared shard data
        if (level != null && !level.isClientSide) {
            // Additional setChanged to ensure packet is sent
            setChanged();
        }
    }
    
    // Data synchronization
    public net.minecraft.world.inventory.ContainerData getDataAccess() {
        return shardDataAccess;
    }
    
    // Override base packet methods to include shard data
    @Override
    protected void writeAdditionalStateData(net.minecraft.network.FriendlyByteBuf buffer) {
        // Write shard data
        buffer.writeInt(storedShards.size());
        for (Map.Entry<String, Integer> entry : storedShards.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeInt(entry.getValue());
        }
        buffer.writeInt(shardOrder.size());
        for (String shardType : shardOrder) {
            buffer.writeUtf(shardType);
        }
        buffer.writeBoolean(isCrafting);
        buffer.writeInt(craftTicks);
        buffer.writeBoolean(isCoalescing);
        
    }
    
    @Override
    protected void readAdditionalStateData(net.minecraft.network.FriendlyByteBuf buffer) {
        // Read shard data
        storedShards.clear();
        int shardTypeCount = buffer.readInt();
        for (int i = 0; i < shardTypeCount; i++) {
            String shardType = buffer.readUtf();
            int count = buffer.readInt();
            storedShards.put(shardType, count);
        }
        shardOrder.clear();
        int shardOrderSize = buffer.readInt();
        for (int i = 0; i < shardOrderSize; i++) {
            shardOrder.add(buffer.readUtf());
        }
        isCrafting = buffer.readBoolean();
        craftTicks = buffer.readInt();
        isCoalescing = buffer.readBoolean();
    }
    
    // Getters for GUI
    public Map<String, Integer> getStoredShards() {
        return new HashMap<>(storedShards);
    }
    
    public List<String> getShardOrder() {
        return new ArrayList<>(shardOrder);
    }
    
    public boolean isCrafting() {
        return isCrafting;
    }
    
    public int getCraftProgress() {
        return isCrafting ? (craftTicks * 100) / CRAFT_TIME : 0;
    }
    
    public boolean isCoalescing() {
        return isCoalescing;
    }
    
    public int getCoalesceProgress() {
        return isCoalescing ? (craftTicks * 100) / CRAFT_TIME : 0;
    }
    
    public void dropContents() {
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty() && level != null) {
                net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                    level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, stack);
                level.addFreshEntity(itemEntity);
            }
        }
    }
}
