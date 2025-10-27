package com.hexvane.strangematter.block;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LevitationPadBlock extends Block implements EntityBlock {
    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LEVITATE_UP = BooleanProperty.create("levitate_up");
    
    public LevitationPadBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_CYAN)
            .strength(2.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .lightLevel(state -> 5) // Emit light level 5
        );
        // Register the default state with NORTH facing and levitate up mode
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(LEVITATE_UP, true));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LEVITATE_UP);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Get the horizontal direction the player is looking
        Direction facing = context.getHorizontalDirection();
        return this.defaultBlockState()
            .setValue(FACING, facing)
            .setValue(LEVITATE_UP, true); // Default to levitate up mode
    }
    
    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    
    @Override
    public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            // Toggle levitation mode
            boolean currentMode = state.getValue(LEVITATE_UP);
            boolean newMode = !currentMode;
            
            BlockState newState = state.setValue(LEVITATE_UP, newMode);
            level.setBlock(pos, newState, 3);
            
            // Update block entity if it exists
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof LevitationPadBlockEntity levitationPad) {
                levitationPad.setLevitateUp(newMode);
            }
            
            // Play sound and send message
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.5f, newMode ? 1.2f : 0.8f);
            
            String modeText = newMode ? "up" : "down";
            player.sendSystemMessage(Component.translatable("block.strangematter.levitation_pad.mode_changed", modeText));
            
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return StrangeMatterMod.LEVITATION_PAD_BLOCK_ENTITY.get().create(pos, state);
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == StrangeMatterMod.LEVITATION_PAD_BLOCK_ENTITY.get() && !level.isClientSide ? 
            (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof LevitationPadBlockEntity levitationPad) {
                    LevitationPadBlockEntity.serverTick(level1, pos, state1, levitationPad);
                }
            } : null;
    }
    
    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        // Make the visual shape match the JSON model: 16x2x16 (full width, 2 pixels high, full depth)
        return Shapes.box(0.0, 0.0, 0.0, 1.0, 2.0/16.0, 1.0);
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        // Make the collision shape match the JSON model: 16x2x16 (full width, 2 pixels high, full depth)
        return Shapes.box(0.0, 0.0, 0.0, 1.0, 2.0/16.0, 1.0);
    }
    
}
