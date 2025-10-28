package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collections;
import java.util.List;

public class ShardCrystalBlock extends Block {
    
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    
    // VoxelShapes for different orientations
    // The crystal model is roughly 8 pixels wide and 11 pixels tall (4-12 x/z, 0-11 y)
    // Approximate as a box: 0.25 to 0.75 in width/depth, 0 to 0.6875 in height
    private static final VoxelShape SHAPE_UP = Block.box(4, 5, 4, 12, 16, 12);
    private static final VoxelShape SHAPE_DOWN = Block.box(4, 0, 4, 12, 11, 12);
    private static final VoxelShape SHAPE_NORTH = Block.box(4, 4, 0, 12, 12, 11);
    private static final VoxelShape SHAPE_SOUTH = Block.box(4, 4, 5, 12, 12, 16);
    private static final VoxelShape SHAPE_WEST = Block.box(0, 4, 4, 11, 12, 12);
    private static final VoxelShape SHAPE_EAST = Block.box(5, 4, 4, 16, 12, 12);
    
    public ShardCrystalBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .strength(0.3f, 0.3f)
            .sound(SoundType.GLASS)
            .lightLevel(state -> 5)
            .noOcclusion() // Allow light to pass through
            .isViewBlocking((state, level, pos) -> false) // Not view blocking
            .isSuffocating((state, level, pos) -> false) // Not suffocating
            .pushReaction(PushReaction.DESTROY) // Breaks when pushed by pistons
            .requiresCorrectToolForDrops() // Requires pickaxe to drop
        );
        // Register the default state with DOWN facing (crystal pointing down when placed on floor)
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Get the direction of the face the player clicked on
        Direction clickedFace = context.getClickedFace();
        // For crystal blocks, we want them to point away from the face they're placed on
        // If placed on top of a block (clicking bottom face), crystal points up
        // If placed on bottom (clicking top face), crystal points down
        Direction crystalDirection = clickedFace.getOpposite();
        return this.defaultBlockState().setValue(FACING, crystalDirection);
    }
    
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        return state;
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return switch (facing) {
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Use the same shape for collision
        return getShape(state, level, pos, context);
    }
    
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Use the same shape for visual outline
        return getShape(state, level, pos, context);
    }
    
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // Always drop the block itself
        return Collections.singletonList(new ItemStack(this));
    }
}

