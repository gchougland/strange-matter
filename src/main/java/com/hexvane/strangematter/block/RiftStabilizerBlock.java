package com.hexvane.strangematter.block;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;

public class RiftStabilizerBlock extends Block implements EntityBlock {
    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    
    public RiftStabilizerBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(5.0f, 10.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .noOcclusion()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Get the horizontal direction the player is looking
        Direction facing = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, facing);
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof RiftStabilizerBlockEntity stabilizer) {
                // Display power generation info
                int powerGeneration = stabilizer.getCurrentPowerGeneration();
                int energyStored = stabilizer.getEnergyStored();
                int maxEnergy = stabilizer.getMaxEnergyStored();
                boolean isGenerating = stabilizer.isGenerating();
                
                if (isGenerating) {
                    player.displayClientMessage(
                        Component.translatable("block.strangematter.rift_stabilizer.generating",
                            powerGeneration, com.hexvane.strangematter.Config.energyUnitDisplay, energyStored, maxEnergy, com.hexvane.strangematter.Config.energyUnitDisplay),
                        false
                    );
                } else {
                    player.displayClientMessage(
                        Component.translatable("block.strangematter.rift_stabilizer.not_generating",
                            energyStored, maxEnergy, com.hexvane.strangematter.Config.energyUnitDisplay),
                        false
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RiftStabilizerBlockEntity(pos, state);
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == StrangeMatterMod.RIFT_STABILIZER_BLOCK_ENTITY.get() ? 
            (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof RiftStabilizerBlockEntity stabilizer) {
                    RiftStabilizerBlockEntity.tick(level1, pos, state1, stabilizer);
                }
            } : null;
    }
}

