package com.hexvane.strangematter.item;

import com.hexvane.strangematter.block.AnomalousGrassBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class AnomalousGrassItem extends BlockItem {
    
    public AnomalousGrassItem(AnomalousGrassBlock block) {
        super(block, new Item.Properties());
    }
}
