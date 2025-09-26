package com.hexvane.strangematter.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import com.hexvane.strangematter.block.ResonanceCondenserBlockEntity;

/**
 * Base class for machine menus that provides common functionality for inventory management.
 * This provides reusable machine GUI logic for all Strange Matter machines.
 */
public abstract class BaseMachineMenu extends AbstractContainerMenu {
    protected final Container machineInventory;
    protected final ContainerLevelAccess levelAccess;
    protected final int machineSlotCount;
    protected final Player player;
    
    public BaseMachineMenu(MenuType<?> menuType, int id, Inventory playerInventory, Container machineInventory, int machineSlotCount) {
        super(menuType, id);
        this.machineInventory = machineInventory;
        this.machineSlotCount = machineSlotCount;
        this.player = playerInventory.player;
        this.levelAccess = ContainerLevelAccess.create(
            machineInventory instanceof net.minecraft.world.level.block.entity.BlockEntity blockEntity ? blockEntity.getLevel() : null,
            machineInventory instanceof net.minecraft.world.level.block.entity.BlockEntity blockEntity ? blockEntity.getBlockPos() : BlockPos.ZERO
        );
        
        // Add machine slots (implemented by subclasses)
        addMachineSlots();
        
        // Add player inventory slots
        addPlayerSlots(playerInventory);
    }
    
    // Client-side constructor for when block entity is not available
    public BaseMachineMenu(MenuType<?> menuType, int id, Inventory playerInventory, int machineSlotCount) {
        super(menuType, id);
        this.machineInventory = null;
        this.machineSlotCount = machineSlotCount;
        this.player = playerInventory.player;
        this.levelAccess = ContainerLevelAccess.NULL;
        
        // Add machine slots (implemented by subclasses)
        addMachineSlots();
        
        // Add player inventory slots
        addPlayerSlots(playerInventory);
    }
    
    /**
     * Add machine-specific slots. Override this in subclasses to define machine inventory layout.
     */
    protected abstract void addMachineSlots();
    
    /**
     * Add player inventory slots in standard layout.
     */
    protected void addPlayerSlots(Inventory playerInventory) {
        // Add player inventory (slots after machine slots)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        
        // Add player hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            // Handle machine slots
            if (index < machineSlotCount) {
                if (!this.moveItemStackTo(itemstack1, machineSlotCount, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Handle player inventory to machine slots
                if (!this.moveItemStackTo(itemstack1, 0, machineSlotCount, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        
        return itemstack;
    }
    
    @Override
    public boolean stillValid(Player player) {
        if (this.machineInventory == null) {
            return false; // Client-side menu with null block entity is not valid
        }
        return this.levelAccess.evaluate((level, pos) ->
            level.getBlockEntity(pos) == this.machineInventory &&
            player.distanceToSqr((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D, true);
    }
    
    public Container getMachineInventory() {
        return this.machineInventory;
    }
    
    public int getMachineSlotCount() {
        return this.machineSlotCount;
    }
    
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.machineInventory != null && this.machineInventory instanceof com.hexvane.strangematter.api.block.entity.IPacketHandlerTile packetHandler) {
            // Use the base packet system for all machines
            if (packetHandler instanceof com.hexvane.strangematter.block.BaseMachineBlockEntity machineEntity) {
                machineEntity.sendStatePacket();
            }
        }
    }
}
