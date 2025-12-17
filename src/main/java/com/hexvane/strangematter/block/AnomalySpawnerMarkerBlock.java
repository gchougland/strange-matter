package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.hexvane.strangematter.StrangeMatterMod;

import javax.annotation.Nullable;

/**
 * An invisible marker block used during world generation to defer entity spawning
 * to the main server thread. This block is placed during world gen and its tile entity
 * spawns the actual anomaly entity on the next server tick.
 */
public class AnomalySpawnerMarkerBlock extends Block implements EntityBlock {
    
    public AnomalySpawnerMarkerBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE)
            // NOTE: Do NOT mark this block as "air".
            // During worldgen we place this into proto-chunks. If the block is considered air, the blockstate can be
            // optimized away as vanilla air while the block-entity NBT is still written, causing:
            // "Tried to load a DUMMY block entity ... but found ... minecraft:air"
            .noCollission() // Behave like air for collision/pathing
            .strength(-1.0F, 3600000.0F) // Unbreakable
            .noLootTable() // No drops
            .noOcclusion() // No occlusion culling
        );
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // No shape - completely invisible
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // No collision
    }
    
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // No visual shape
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true; // Allow light to pass through
    }
    
    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false; // Don't occlude light
    }
    
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F; // Full brightness - makes it blend with air
    }
    
    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnomalySpawnerMarkerBlockEntity(pos, state);
    }
    
    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, 
                                                                 BlockEntityType<T> blockEntityType) {
        return blockEntityType == StrangeMatterMod.ANOMALY_SPAWNER_MARKER_BLOCK_ENTITY.get() && !level.isClientSide ? 
            (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof AnomalySpawnerMarkerBlockEntity marker) {
                    AnomalySpawnerMarkerBlockEntity.serverTick(level1, pos, state1, marker);
                }
            } : null;
    }
}

