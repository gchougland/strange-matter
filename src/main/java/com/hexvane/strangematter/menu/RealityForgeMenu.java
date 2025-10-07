package com.hexvane.strangematter.menu;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.block.RealityForgeBlockEntity;
import com.hexvane.strangematter.menu.slots.OutputSlot;
import com.hexvane.strangematter.menu.slots.ShardSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import net.minecraft.world.inventory.ContainerData;

public class RealityForgeMenu extends BaseMachineMenu {
    
    private final RealityForgeBlockEntity blockEntity;
    private final ContainerLevelAccess levelAccess;
    private final ContainerData dataAccess;
    
    public RealityForgeMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        this(windowId, playerInventory, data.readBlockPos());
    }
    
    public RealityForgeMenu(int windowId, Inventory playerInventory, net.minecraft.core.BlockPos pos) {
        super(StrangeMatterMod.REALITY_FORGE_MENU.get(), windowId, playerInventory,
            playerInventory.player.level().getBlockEntity(pos) instanceof RealityForgeBlockEntity be ? be : null, 11);
        this.blockEntity = playerInventory.player.level().getBlockEntity(pos) instanceof RealityForgeBlockEntity be ? be : null;
        this.levelAccess = ContainerLevelAccess.create(playerInventory.player.level(), pos);
        this.dataAccess = this.blockEntity != null ? this.blockEntity.getDataAccess() : null;
        
        // Set the current player for research requirement checks
        if (this.blockEntity != null) {
            this.blockEntity.setCurrentPlayer(playerInventory.player);
        }
        
        // Add data synchronization for shard data
        if (this.dataAccess != null) {
            this.addDataSlots(this.dataAccess);
        }
    }
    
    // Client-side constructor for when block entity is not available
    public RealityForgeMenu(int windowId, Inventory playerInventory) {
        super(StrangeMatterMod.REALITY_FORGE_MENU.get(), windowId, playerInventory, 11);
        this.blockEntity = null;
        this.levelAccess = ContainerLevelAccess.NULL;
        this.dataAccess = null;
        
    }
    
    public RealityForgeMenu(int windowId, Inventory playerInventory, RealityForgeBlockEntity blockEntity) {
        super(StrangeMatterMod.REALITY_FORGE_MENU.get(), windowId, playerInventory, blockEntity, 11);
        this.blockEntity = blockEntity;
        this.levelAccess = ContainerLevelAccess.create(playerInventory.player.level(), 
            blockEntity != null ? blockEntity.getBlockPos() : net.minecraft.core.BlockPos.ZERO);
        this.dataAccess = this.blockEntity != null ? this.blockEntity.getDataAccess() : null;
        
        
        // Add data synchronization for shard data
        if (this.dataAccess != null) {
            this.addDataSlots(this.dataAccess);
        }
    }
    
    @Override
    protected void addMachineSlots() {
        
        // Add crafting grid slots (3x3) - positioned to match the GUI texture
        if (blockEntity != null) {
            // Add crafting grid slots (3x3) - slots 0-8
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int containerSlot = row * 3 + col;
                    int menuSlot = this.slots.size();
                    this.addSlot(new Slot(blockEntity, containerSlot, 64 + col * 16, 13 + row * 16));
                }
            }
            
            // Add shard input slot - slot 9
            int shardMenuSlot = this.slots.size();
            this.addSlot(new ShardSlot(blockEntity, 9, 37, 27));
            
            // Add output slot - slot 10
            int outputMenuSlot = this.slots.size();
            this.addSlot(new OutputSlot(blockEntity, 10, 122, 29));
        } else if (machineInventory != null) {
            // Add crafting grid slots (3x3) - slots 0-8
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int containerSlot = row * 3 + col;
                    int menuSlot = this.slots.size();
                    this.addSlot(new Slot(machineInventory, containerSlot, 64 + col * 16, 13 + row * 16));
                }
            }
            
            // Add shard input slot - slot 9
            int shardMenuSlot = this.slots.size();
            this.addSlot(new ShardSlot(machineInventory, 9, 37, 27));
            
            // Add output slot - slot 10
            int outputMenuSlot = this.slots.size();
            this.addSlot(new OutputSlot(machineInventory, 10, 122, 29));
        } else {
            // Fallback: create a dummy container for the slots
            net.minecraft.world.Container dummyContainer = new net.minecraft.world.Container() {
                private final net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> items = net.minecraft.core.NonNullList.withSize(11, net.minecraft.world.item.ItemStack.EMPTY);
                
                @Override
                public int getContainerSize() { return 11; }
                
                @Override
                public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
                
                @Override
                public net.minecraft.world.item.ItemStack getItem(int index) { return items.get(index); }
                
                @Override
                public net.minecraft.world.item.ItemStack removeItem(int index, int count) { return net.minecraft.world.ContainerHelper.removeItem(items, index, count); }
                
                @Override
                public net.minecraft.world.item.ItemStack removeItemNoUpdate(int index) { return net.minecraft.world.ContainerHelper.takeItem(items, index); }
                
                @Override
                public void setItem(int index, net.minecraft.world.item.ItemStack stack) { items.set(index, stack); }
                
                @Override
                public void setChanged() {}
                
                @Override
                public boolean stillValid(net.minecraft.world.entity.player.Player player) { return false; }
                
                @Override
                public void clearContent() { items.clear(); }
            };
            
            // Add crafting grid slots (3x3) - slots 0-8
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    this.addSlot(new Slot(dummyContainer, row * 3 + col, 64 + col * 16, 13 + row * 16));
                }
            }
            
            // Add shard input slot - slot 9
            this.addSlot(new ShardSlot(dummyContainer, 9, 37, 27));
            
            // Add output slot - slot 10
            this.addSlot(new OutputSlot(dummyContainer, 10, 122, 29));
        }
    }
    
    
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        
        // Check if crafting is active and this is a machine slot (0-10)
        if (isCrafting() && index >= 0 && index < 11) {
            // Lock machine slots during crafting
            return ItemStack.EMPTY;
        }
        
        ItemStack result = super.quickMoveStack(player, index);
        return result;
    }
    
    @Override
    public void clicked(int slotIndex, int button, net.minecraft.world.inventory.ClickType clickType, Player player) {
        
        // Check if crafting is active and this is a machine slot (0-10)
        if (isCrafting() && slotIndex >= 0 && slotIndex < 11) {
            // Lock machine slots during crafting
            return;
        }
        
        super.clicked(slotIndex, button, clickType, player);
    }
    
    public RealityForgeBlockEntity getBlockEntity() {
        return blockEntity;
    }
    
    public void ejectShards() {
        // Check if crafting is active - lock eject button during crafting
        if (isCrafting()) {
            return;
        }
        
        if (blockEntity != null) {
            // Send packet to server to handle eject
            com.hexvane.strangematter.network.NetworkHandler.INSTANCE.sendToServer(
                new com.hexvane.strangematter.network.EjectShardsPacket(blockEntity.getBlockPos())
            );
        }
    }
    
    public void attemptCraft() {
        if (blockEntity != null) {
            blockEntity.attemptCraft();
        }
    }
    
    // Methods to access synchronized data for the client
    public boolean isCrafting() {
        if (blockEntity != null) {
            return blockEntity.isCrafting();
        }
        return false;
    }
    
    public boolean isCoalescing() {
        if (blockEntity != null) {
            return blockEntity.isCoalescing();
        }
        return false;
    }
    
    public int getCraftProgress() {
        if (blockEntity != null) {
            return blockEntity.getCraftProgress();
        }
        return 0;
    }
    
    public int getCoalesceProgress() {
        if (blockEntity != null) {
            return blockEntity.getCoalesceProgress();
        }
        return 0;
    }
    
    public Map<String, Integer> getStoredShards() {
        if (blockEntity != null) {
            return blockEntity.getStoredShards();
        }
        return new HashMap<>();
    }
    
    public List<String> getShardOrder() {
        if (blockEntity != null) {
            return blockEntity.getShardOrder();
        }
        return new ArrayList<>();
    }
    
    public ContainerData getDataAccess() {
        return this.dataAccess;
    }
    
    
    
    
    @Override
    protected void addPlayerSlots(Inventory playerInventory) {
        
        // Add player inventory (slots after machine slots) - positioned to match the GUI texture
        // Player inventory slots are 9-35 in the player's inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int containerSlot = col + row * 9 + 9;
                int menuSlot = this.slots.size();
                this.addSlot(new Slot(playerInventory, containerSlot, 9 + col * 18, 68 + row * 17));
            }
        }
        
        // Add player hotbar - positioned to match the GUI texture
        // Player hotbar slots are 0-8 in the player's inventory
        for (int col = 0; col < 9; ++col) { // Add all 9 hotbar slots
            int containerSlot = col;
            this.addSlot(new Slot(playerInventory, containerSlot, 9 + col * 18, 124));
        }
        
    }
    
}
