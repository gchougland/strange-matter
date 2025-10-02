package com.hexvane.strangematter.menu.slots;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ShardSlot extends Slot {
    
    public ShardSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }
    
    @Override
    public boolean mayPlace(ItemStack stack) {
        return isShard(stack);
    }
    
    @Override
    public void setByPlayer(ItemStack stack) {
        super.setByPlayer(stack);
        if (!stack.isEmpty() && isShard(stack)) {
            // Insert as many shards as possible (up to 6 total limit)
            if (container instanceof com.hexvane.strangematter.block.RealityForgeBlockEntity realityForge) {
                int shardsInserted = realityForge.insertShard(stack);
                
                if (shardsInserted > 0) {
                    // Reduce the stack by the number of shards inserted
                    stack.shrink(shardsInserted);
                    
                    if (stack.getCount() > 0) {
                        set(stack); // Put the remaining stack back in the slot
                    } else {
                        set(ItemStack.EMPTY); // Remove the shard from the slot if all were consumed
                    }
                }
                // If no shards could be inserted (already at 6), leave the stack unchanged
            }
        }
    }
    
    private boolean isShard(ItemStack stack) {
        return stack.getItem() == StrangeMatterMod.ENERGETIC_SHARD.get() ||
               stack.getItem() == StrangeMatterMod.GRAVITIC_SHARD.get() ||
               stack.getItem() == StrangeMatterMod.CHRONO_SHARD.get() ||
               stack.getItem() == StrangeMatterMod.SPATIAL_SHARD.get() ||
               stack.getItem() == StrangeMatterMod.SHADE_SHARD.get() ||
               stack.getItem() == StrangeMatterMod.INSIGHT_SHARD.get();
    }
}
