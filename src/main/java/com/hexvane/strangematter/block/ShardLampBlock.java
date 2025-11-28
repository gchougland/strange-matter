package com.hexvane.strangematter.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.Collections;
import java.util.List;

public class ShardLampBlock extends Block {
    
    public ShardLampBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .strength(0.3f, 0.3f)
            .sound(SoundType.GLASS)
            .lightLevel(state -> 15) // Same light level as glowstone
        );
    }
    
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // Always drop itself when broken
        return Collections.singletonList(new ItemStack(this));
    }
}
