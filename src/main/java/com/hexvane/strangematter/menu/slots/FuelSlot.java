package com.hexvane.strangematter.menu.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;

/**
 * Fuel slot for machines - allows fuel items to be placed and taken.
 * Used for coal, fuel items, etc.
 */
public class FuelSlot extends BaseMachineSlot {
    
    public FuelSlot(net.minecraft.world.Container container, int index, int x, int y) {
        super(container, index, x, y);
    }
    
    @Override
    protected boolean canPlaceItem(ItemStack stack) {
        // Allow fuel items (coal, charcoal, etc.)
        return stack.is(Items.COAL) || 
               stack.is(Items.CHARCOAL) || 
               stack.is(Items.COAL_BLOCK) ||
               stack.is(ItemTags.COALS) ||
               stack.getBurnTime(net.minecraft.world.item.crafting.RecipeType.SMELTING) > 0; // Any item with burn time
    }
    
    @Override
    protected boolean canTakeItem(Player player) {
        return true; // Players can take fuel items
    }
}
