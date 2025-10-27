package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.hexvane.strangematter.StrangeMatterMod;

import javax.annotation.Nullable;

/**
 * Resonant Conduit block that transfers resonant energy between machines.
 * Features dynamic connection states and optimized energy transfer.
 */
public class ResonantConduitBlock extends Block implements EntityBlock {
    
    // Connection properties for each direction
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    
    // Voxel shapes for different connection states
    private static final VoxelShape CORE_SHAPE = Block.box(6, 6, 6, 10, 10, 10);
    
    // Connection shapes (5x5 pixel tubes extending from 7x7 core)
    private static final VoxelShape NORTH_SHAPE = Block.box(7, 7, 0, 9, 9, 6);
    private static final VoxelShape SOUTH_SHAPE = Block.box(7, 7, 10, 9, 9, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(10, 7, 7, 16, 9, 9);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 7, 7, 6, 9, 9);
    private static final VoxelShape UP_SHAPE = Block.box(7, 10, 7, 9, 16, 9);
    private static final VoxelShape DOWN_SHAPE = Block.box(7, 0, 7, 9, 6, 9);
    
    public ResonantConduitBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(2.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .lightLevel(state -> 3) // Emit light level 3
            .noOcclusion() // Allow custom rendering
        );
        
        // Register default state with all connections false
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(NORTH, false)
            .setValue(SOUTH, false)
            .setValue(EAST, false)
            .setValue(WEST, false)
            .setValue(UP, false)
            .setValue(DOWN, false)
        );
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateConnections(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }
    
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, 
                                 LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // Update connections when neighbors change
        BlockState newState = updateConnections(state, level, pos);
        
        // Notify the block entity about the neighbor change
        if (level instanceof Level levelInstance) {
            BlockEntity blockEntity = levelInstance.getBlockEntity(pos);
            if (blockEntity instanceof ResonantConduitBlockEntity conduit) {
                conduit.onNeighborChanged();
            }
        }
        
        return newState;
    }
    
    /**
     * Update connection states based on adjacent blocks
     */
    private BlockState updateConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        BlockState newState = state;
        
        for (Direction direction : Direction.values()) {
            boolean connected = canConnectTo(level, pos.relative(direction), direction);
            newState = newState.setValue(getPropertyForDirection(direction), connected);
        }
        
        return newState;
    }
    
    /**
     * Check if this conduit can connect to the block at the given position
     */
    private boolean canConnectTo(LevelAccessor level, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        if (blockEntity == null) {
            return false;
        }
        
        // Connect to other conduits
        if (blockEntity instanceof ResonantConduitBlockEntity) {
            return true;
        }
        
        // Connect to any block that has energy capability
        if (level instanceof Level levelInstance) {
            var energyStorage = levelInstance.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, 
                                                            pos, direction.getOpposite());
            return energyStorage != null;
        }
        return false;
    }
    
    /**
     * Get the boolean property for a given direction
     */
    private BooleanProperty getPropertyForDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE_SHAPE;
        
        // Add connection shapes based on state
        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, NORTH_SHAPE);
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, SOUTH_SHAPE);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, EAST_SHAPE);
        }
        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, WEST_SHAPE);
        }
        if (state.getValue(UP)) {
            shape = Shapes.or(shape, UP_SHAPE);
        }
        if (state.getValue(DOWN)) {
            shape = Shapes.or(shape, DOWN_SHAPE);
        }
        
        return shape;
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }
    
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true; // Allow light to pass through
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F; // Full brightness for glowing effect
    }
    
    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false; // Don't occlude light
    }
    
    
    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantConduitBlockEntity(pos, state);
    }
    
    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, 
                                                                 BlockEntityType<T> blockEntityType) {
        return blockEntityType == StrangeMatterMod.RESONANT_CONDUIT_BLOCK_ENTITY.get() && !level.isClientSide ? 
            (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof ResonantConduitBlockEntity conduit) {
                    ResonantConduitBlockEntity.tick(level1, pos, state1, conduit);
                }
            } : null;
    }
    
    /**
     * Get the connection state for a specific direction
     */
    public boolean isConnected(BlockState state, Direction direction) {
        return switch (direction) {
            case NORTH -> state.getValue(NORTH);
            case SOUTH -> state.getValue(SOUTH);
            case EAST -> state.getValue(EAST);
            case WEST -> state.getValue(WEST);
            case UP -> state.getValue(UP);
            case DOWN -> state.getValue(DOWN);
        };
    }
    
    /**
     * Get the number of connections
     */
    public int getConnectionCount(BlockState state) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (isConnected(state, direction)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Check if this conduit has a straight connection (opposite directions)
     */
    public boolean hasStraightConnection(BlockState state) {
        for (Direction direction : Direction.values()) {
            if (isConnected(state, direction) && isConnected(state, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if this conduit has a corner connection (two adjacent connections)
     */
    public boolean hasCornerConnection(BlockState state) {
        int connectionCount = getConnectionCount(state);
        return connectionCount == 2 && !hasStraightConnection(state);
    }
    
    /**
     * Check if this conduit has an intersection connection (3+ connections)
     */
    public boolean hasIntersectionConnection(BlockState state) {
        return getConnectionCount(state) >= 3;
    }
}
