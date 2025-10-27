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
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Item;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;
import java.util.ArrayList;

/**
 * Base class for all shard ore blocks in the Strange Matter mod.
 * Provides common functionality for ore blocks that drop shards.
 */
public abstract class ShardOreBlock extends Block {
    
    private final DeferredHolder<Item, Item> shardItem;
    
    public ShardOreBlock(DeferredHolder<Item, Item> shardItem) {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(3.0f, 3.0f)
            .pushReaction(PushReaction.DESTROY)
            .requiresCorrectToolForDrops()
        );
        this.shardItem = shardItem;
    }
    
    
    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.STONE;
    }
    
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        
        // Check if the tool has silk touch
        if (builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player) {
            ItemStack tool = player.getMainHandItem();
            if (tool.getEnchantmentLevel(builder.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH)) > 0) {
                // Drop the ore block itself with silk touch
                drops.add(new ItemStack(this));
            } else {
                // Check if tool is iron pickaxe or better
                if (tool.getItem() instanceof TieredItem tieredItem) {
                    if (tieredItem.getTier().getUses() >= Tiers.IRON.getUses()) {
                        // Drop 1-2 shards, affected by fortune
                        int fortuneLevel = tool.getEnchantmentLevel(builder.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE));
                        int dropCount = 1 + builder.getLevel().getRandom().nextInt(2); // 1-2 base drops
                        
                        // Apply fortune: each level adds 0-1 extra drops, max 4 total
                        for (int i = 0; i < fortuneLevel; i++) {
                            if (builder.getLevel().getRandom().nextFloat() < 0.33f) { // 33% chance per fortune level
                                dropCount++;
                            }
                        }
                        
                        // Cap at 4 drops maximum
                        dropCount = Math.min(dropCount, 4);
                        
                        drops.add(new ItemStack(shardItem.get(), dropCount));
                    }
                }
            }
        } else {
            // Default behavior: drop 1-2 shards
            drops.add(new ItemStack(shardItem.get(), 1 + builder.getLevel().getRandom().nextInt(2)));
        }
        
        return drops;
    }
    
    public int getExpDrop(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.util.RandomSource random, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
        // Drop experience like coal ore (0-2 experience)
        return silkTouchLevel == 0 ? random.nextInt(3) : 0;
    }
}
