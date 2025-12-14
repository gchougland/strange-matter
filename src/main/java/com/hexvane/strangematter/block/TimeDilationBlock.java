package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.hexvane.strangematter.StrangeMatterMod;

public class TimeDilationBlock extends Block implements EntityBlock {
    
    public TimeDilationBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_ORANGE)
            .strength(0.3f)
            .pushReaction(PushReaction.DESTROY)
            .noOcclusion() // Allows light through
            .isViewBlocking((state, level, pos) -> false) // Not view blocking
            .isSuffocating((state, level, pos) -> false) // Not suffocating
            .sound(SoundType.GLASS)
            .lightLevel(state -> 3) // Emit light level 3
        );
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // No collision - walkable-through
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // Standard model rendering
    }
    
    // Decay is now handled by the block entity tick counter, not random ticks
    
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        // This is called when an entity steps on the block
        // The block entity tick will handle the slowdown, but this ensures detection
    }
    
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        // This is called every tick for entities inside the block
        // The block entity tick will handle the slowdown, but this ensures detection
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // Clean up player effects when block is removed
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof com.hexvane.strangematter.block.TimeDilationBlockEntity blockEntity) {
                blockEntity.cleanupAllAffectedPlayers();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return StrangeMatterMod.TIME_DILATION_BLOCK_ENTITY.get().create(pos, state);
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == StrangeMatterMod.TIME_DILATION_BLOCK_ENTITY.get() && !level.isClientSide ? 
            (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof TimeDilationBlockEntity timeDilationBlock) {
                    TimeDilationBlockEntity.serverTick(level1, pos, state1, timeDilationBlock);
                }
            } : null;
    }
}

