package com.hexvane.strangematter.menu.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Input slot for machines - allows items to be placed but not taken by players.
 * Used for fuel, ingredients, etc.
 */
public class InputSlot extends BaseMachineSlot {
    
    public InputSlot(net.minecraft.world.Container container, int index, int x, int y) {
        super(container, index, x, y);
    }
    
    @Override
    protected boolean canPlaceItem(ItemStack stack) {
        return true; // Allow any item to be placed
    }
    
    @Override
    protected boolean canTakeItem(Player player) {
        return false; // Players cannot take items from input slots
    }
}


