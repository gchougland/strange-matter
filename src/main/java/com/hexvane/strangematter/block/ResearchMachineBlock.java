package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import com.hexvane.strangematter.StrangeMatterMod;

public class ResearchMachineBlock extends Block implements EntityBlock {
    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    
    public ResearchMachineBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLUE)
            .strength(3.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .lightLevel(state -> 7) // Emit light level 7
            .noOcclusion() // Make block invisible so only the entity renders
        );
        // Register the default state with NORTH facing
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Get the horizontal direction the player is looking
        Direction facing = context.getHorizontalDirection();
        return this.defaultBlockState().setValue(FACING, facing);
    }
    
    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.ENTITYBLOCK_ANIMATED;
    }
    
    
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return StrangeMatterMod.RESEARCH_MACHINE_BLOCK_ENTITY.get().create(pos, state);
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == StrangeMatterMod.RESEARCH_MACHINE_BLOCK_ENTITY.get() && !level.isClientSide ? 
            (level1, pos, state1, blockEntity) -> {
                // Add any server-side ticking logic here if needed
            } : null;
    }
}
