package com.hexvane.strangematter.block;

import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;

public class ResoniteDoorBlock extends DoorBlock {
    
    public ResoniteDoorBlock() {
        // Use OAK BlockSetType so it can be opened by hand (not requiring redstone like iron doors)
        super(BlockSetType.OAK, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(3.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .noOcclusion()
        );
    }
}

