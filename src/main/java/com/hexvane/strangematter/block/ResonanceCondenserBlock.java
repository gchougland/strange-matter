package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import com.hexvane.strangematter.StrangeMatterMod;

public class ResonanceCondenserBlock extends Block implements EntityBlock {
    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    
    public ResonanceCondenserBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLUE)
            .strength(3.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .lightLevel(state -> 5) // Emit light level 5
            .noOcclusion() // Allow light to pass through
            .isViewBlocking((state, level, pos) -> false) // Not view blocking for translucent effect
            .isSuffocating((state, level, pos) -> false) // Not suffocating
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof ResonanceCondenserBlockEntity condenser) {
                // Use NetworkHooks.openScreen like MaterialNexus
                net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player, condenser, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonanceCondenserBlockEntity(pos, state);
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == StrangeMatterMod.RESONANCE_CONDENSER_BLOCK_ENTITY.get() && !level.isClientSide ? 
            (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof ResonanceCondenserBlockEntity condenser) {
                    ResonanceCondenserBlockEntity.tick(level1, pos, state1, condenser);
                }
            } : null;
    }
}
