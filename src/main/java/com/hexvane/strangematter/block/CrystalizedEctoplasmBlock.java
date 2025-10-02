package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.hexvane.strangematter.StrangeMatterMod;

public class CrystalizedEctoplasmBlock extends Block implements EntityBlock {
    
    // Direction property for crystal orientation
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    
    // Use standard cube shape for now
    private static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    
    public CrystalizedEctoplasmBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_GREEN)
            .strength(2.0f, 6.0f) // Harder than stone, requires iron pickaxe
            .pushReaction(PushReaction.DESTROY)
            .noOcclusion()
            .lightLevel(state -> 8) // Emit light level 8 (color #41b280)
            .requiresCorrectToolForDrops() // Requires proper tool to drop items
            .isRedstoneConductor((state, level, pos) -> false) // Allow redstone through
            .isSuffocating((state, level, pos) -> false) // Not suffocating
            .isViewBlocking((state, level, pos) -> false) // Not view blocking
        );
        // Register the default state with UP facing
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Get the direction the player is looking
        Direction facing = context.getClickedFace();
        
        // If placing on the top or bottom face, use the clicked face
        // If placing on a side face, use the opposite direction (so crystal points toward player)
        if (facing == Direction.UP || facing == Direction.DOWN) {
            return this.defaultBlockState().setValue(FACING, facing);
        } else {
            // For side faces, make the crystal point toward the player
            return this.defaultBlockState().setValue(FACING, facing);
        }
    }
    
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }
    
    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.GLASS; // Glass breaking sound
    }
    
    @Override
    public float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        ItemStack tool = player.getMainHandItem();
        
        // Check if using a pickaxe
        if (tool.getItem() instanceof TieredItem) {
            // Faster breaking with pickaxes
            return super.getDestroyProgress(state, player, level, pos) * 2.0f;
        }
        
        return super.getDestroyProgress(state, player, level, pos);
    }
    
    @Override
    public boolean canHarvestBlock(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Player player) {
        ItemStack tool = player.getMainHandItem();
        
        // Require at least iron pickaxe level
        if (tool.getItem() instanceof TieredItem tieredItem) {
            return tieredItem.getTier().getLevel() >= Tiers.IRON.getLevel();
        }
        
        return false;
    }
    
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false; // No growth mechanics - single block only
    }
    
    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.ENTITYBLOCK_ANIMATED;
    }
    
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }
    
    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }
    
    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }
    
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.animateTick(state, level, pos, random);
        
        // Spawn particles occasionally
        if (random.nextInt(15) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            
            // Spawn green particles that match the light color
            level.addParticle(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, x, y, z, 0.0, 0.05, 0.0);
        }
        
        // Occasionally spawn a small sparkle
        if (random.nextInt(100) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            
            level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT, x, y, z, 
                (random.nextDouble() - 0.5) * 0.05, 
                random.nextDouble() * 0.02, 
                (random.nextDouble() - 0.5) * 0.05);
        }
    }
    
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalizedEctoplasmBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return null; // No ticking needed
    }
}
