package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.hexvane.strangematter.StrangeMatterMod;

public class CrystalizedEctoplasmBlockEntity extends BlockEntity {
    
    public CrystalizedEctoplasmBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.CRYSTALIZED_ECTOPLASM_BLOCK_ENTITY.get(), pos, state);
    }
}
