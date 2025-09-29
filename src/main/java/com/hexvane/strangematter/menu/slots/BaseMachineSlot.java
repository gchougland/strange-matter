package com.hexvane.strangematter.menu.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;

/**
 * Base class for machine slots that provides common functionality.
 * This provides reusable slot logic for all Strange Matter machines.
 */
public abstract class BaseMachineSlot extends Slot {
    
    public BaseMachineSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }
    
    @Override
    public boolean mayPlace(ItemStack stack) {
        return canPlaceItem(stack);
    }
    
    @Override
    public boolean mayPickup(Player player) {
        return canTakeItem(player);
    }
    
    @Override
    public ItemStack getItem() {
        // Handle null container (client-side)
        if (this.container == null) {
            return ItemStack.EMPTY;
        }
        return super.getItem();
    }

    @Override
    public void setChanged() {
        // Handle null container (client-side)
        if (this.container != null) {
            super.setChanged();
        }
    }

    @Override
    public void set(ItemStack stack) {
        // Handle null container (client-side)
        if (this.container != null) {
            super.set(stack);
        }
    }
    
    @Override
    public boolean hasItem() {
        // Handle null container (client-side)
        if (this.container == null) {
            return false;
        }
        return super.hasItem();
    }
    
    @Override
    public int getMaxStackSize() {
        // Handle null container (client-side)
        if (this.container == null) {
            return 64;
        }
        return super.getMaxStackSize();
    }
    
    /**
     * Override this to define what items can be placed in this slot.
     */
    protected abstract boolean canPlaceItem(ItemStack stack);
    
    /**
     * Override this to define when items can be taken from this slot.
     */
    protected abstract boolean canTakeItem(Player player);
}
