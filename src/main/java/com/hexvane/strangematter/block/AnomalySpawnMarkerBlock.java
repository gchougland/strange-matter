package com.hexvane.strangematter.block;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Invisible marker block used to defer anomaly entity spawning until the main thread.
 * This block is used during world generation to place a block entity that spawns
 * the actual anomaly entity on the next server tick.
 */
public class AnomalySpawnMarkerBlock extends Block implements EntityBlock {
    
    public AnomalySpawnMarkerBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE)
            .air()
            .noOcclusion()
            .replaceable()
            .strength(-1.0F, 3600000.0F)
            .noLootTable());
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return StrangeMatterMod.ANOMALY_SPAWN_MARKER_BLOCK_ENTITY.get().create(pos, state);
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : 
            blockEntityType == StrangeMatterMod.ANOMALY_SPAWN_MARKER_BLOCK_ENTITY.get() ? 
            (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof AnomalySpawnMarkerBlockEntity marker) {
                    AnomalySpawnMarkerBlockEntity.tick(level1, pos, state1, marker);
                }
            } : null;
    }
    
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }
}

