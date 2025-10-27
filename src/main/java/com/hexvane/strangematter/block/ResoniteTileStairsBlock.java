package com.hexvane.strangematter.block;

import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import com.hexvane.strangematter.StrangeMatterMod;

public class ResoniteTileStairsBlock extends StairBlock {
    
    public ResoniteTileStairsBlock() {
        super(StrangeMatterMod.RESONITE_TILE_BLOCK.get().defaultBlockState(),
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.0f, 6.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
        );
    }
}

