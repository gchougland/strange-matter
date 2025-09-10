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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import com.hexvane.strangematter.StrangeMatterMod;

public class ResoniteOreBlock extends Block {
    
    public ResoniteOreBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(3.0f, 3.0f)
            .pushReaction(PushReaction.DESTROY)
            .requiresCorrectToolForDrops()
        );
    }
    
    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.STONE;
    }
    
    
    
    @Override
    public java.util.List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        java.util.List<ItemStack> drops = new java.util.ArrayList<>();
        
        // Check if the tool has silk touch
        if (builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player) {
            ItemStack tool = player.getMainHandItem();
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
                // Drop the ore block itself with silk touch
                drops.add(new ItemStack(this));
            } else {
                // Drop 1-2 raw resonite, affected by fortune
                int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
                int dropCount = 1 + builder.getLevel().getRandom().nextInt(2); // 1-2 base drops
                
                // Apply fortune: each level adds 0-1 extra drops, max 4 total
                for (int i = 0; i < fortuneLevel; i++) {
                    if (builder.getLevel().getRandom().nextFloat() < 0.33f) { // 33% chance per fortune level
                        dropCount++;
                    }
                }
                
                // Cap at 4 drops maximum
                dropCount = Math.min(dropCount, 4);
                
                drops.add(new ItemStack(StrangeMatterMod.RAW_RESONITE.get(), dropCount));
            }
        } else {
            // Default behavior: drop 1-2 raw resonite
            int dropCount = 1 + builder.getLevel().getRandom().nextInt(2);
            drops.add(new ItemStack(StrangeMatterMod.RAW_RESONITE.get(), dropCount));
        }
        
        return drops;
    }
}
