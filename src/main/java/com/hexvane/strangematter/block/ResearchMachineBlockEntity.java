package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.hexvane.strangematter.StrangeMatterMod;

public class ResearchMachineBlockEntity extends BlockEntity {
    
    public ResearchMachineBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.RESEARCH_MACHINE_BLOCK_ENTITY.get(), pos, state);
    }
}
