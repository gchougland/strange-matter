package com.hexvane.strangematter.menu.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Output slot for machines - allows items to be taken but not placed by players.
 * Used for processed items, generated shards, etc.
 */
public class OutputSlot extends BaseMachineSlot {
    
    public OutputSlot(net.minecraft.world.Container container, int index, int x, int y) {
        super(container, index, x, y);
    }
    
    @Override
    protected boolean canPlaceItem(ItemStack stack) {
        return false; // Players cannot place items in output slots
    }
    
    @Override
    protected boolean canTakeItem(Player player) {
        return true; // Players can take items from output slots
    }
}


