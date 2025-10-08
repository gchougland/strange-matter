package com.hexvane.strangematter.block;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ResoniteBlock extends Block {
    
    public ResoniteBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(5.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
        );
    }
    
    @Override
    public java.util.List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        java.util.List<ItemStack> drops = new java.util.ArrayList<>();
        
        // Check if the tool has silk touch
        if (builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player) {
            ItemStack tool = player.getMainHandItem();
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
                // Drop the block itself with silk touch
                drops.add(new ItemStack(this));
            } else {
                // Drop the block itself normally (since it's a storage block)
                drops.add(new ItemStack(this));
            }
        } else {
            // Default behavior: drop the block itself
            drops.add(new ItemStack(this));
        }
        
        return drops;
    }
}

