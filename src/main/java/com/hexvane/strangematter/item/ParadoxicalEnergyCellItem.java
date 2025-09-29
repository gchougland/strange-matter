package com.hexvane.strangematter.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

/**
 * Item for the Paradoxical Energy Cell block.
 * Creative-only item with special rarity.
 */
public class ParadoxicalEnergyCellItem extends BlockItem {
    
    public ParadoxicalEnergyCellItem(Block block) {
        super(block, new Item.Properties()
            .rarity(Rarity.EPIC)
            .stacksTo(1)
        );
    }
}
