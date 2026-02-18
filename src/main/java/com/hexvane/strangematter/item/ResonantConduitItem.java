package com.hexvane.strangematter.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import com.hexvane.strangematter.block.ResonantConduitBlock;
import com.hexvane.strangematter.Config;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Item for Resonant Conduits with tooltip information about energy transfer.
 */
public class ResonantConduitItem extends BlockItem {
    
    public ResonantConduitItem(ResonantConduitBlock block) {
        super(block, new Item.Properties());
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        tooltip.add(Component.translatable("item.strangematter.resonant_conduit.tooltip1")
            .withStyle(ChatFormatting.GRAY));
        
        tooltip.add(Component.translatable("item.strangematter.resonant_conduit.transfer_rate", Config.resonantConduitTransferRate, Config.energyUnitDisplay)
            .withStyle(ChatFormatting.GREEN));
    }
}

