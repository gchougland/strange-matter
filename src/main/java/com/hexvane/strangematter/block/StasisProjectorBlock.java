package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.network.chat.Component;

public class StasisProjectorBlock extends Block implements EntityBlock {
    
    // Small block shape to match the model - 6x2x6 pixels
    private static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 2.0D, 11.0D);
    
    public StasisProjectorBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(3.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .isViewBlocking((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
        );
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Handle dye application
        if (stack.getItem() instanceof DyeItem dyeItem) {
            if (!level.isClientSide) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof StasisProjectorBlockEntity stasisProjector) {
                    // Get the dye color
                    int dyeColor = dyeItem.getDyeColor().getFireworkColor();
                    stasisProjector.setFieldColor(dyeColor);
                    
                    // Consume the dye
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    
                    // Play sound and show message
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.DYE_USE, 
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                    
                    player.displayClientMessage(
                        Component.translatable("message.strangematter.stasis_projector.dyed", 
                            dyeItem.getDyeColor().getName()),
                        true
                    );
                }
            }
            return ItemInteractionResult.SUCCESS;
        }
        
        // Allow other item interactions to work normally
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof StasisProjectorBlockEntity stasisProjector) {
                    // Toggle on/off when shift right-clicking
                    stasisProjector.togglePower();
                    boolean isPowered = stasisProjector.isPowered();
                    
                    // Play appropriate sound
                    level.playSound(null, pos, 
                        isPowered ? com.hexvane.strangematter.sound.StrangeMatterSounds.STASIS_PROJECTOR_ON.get() 
                                  : com.hexvane.strangematter.sound.StrangeMatterSounds.STASIS_PROJECTOR_OFF.get(),
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 1.0f);
                    
                    player.displayClientMessage(
                        Component.translatable(isPowered ? "message.strangematter.stasis_projector.on" : "message.strangematter.stasis_projector.off"),
                        true
                    );
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StasisProjectorBlockEntity(pos, state);
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == StrangeMatterMod.STASIS_PROJECTOR_BLOCK_ENTITY.get() ? 
            (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof StasisProjectorBlockEntity stasisProjector) {
                    StasisProjectorBlockEntity.tick(level1, pos, state1, stasisProjector);
                }
            } : null;
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof StasisProjectorBlockEntity stasisProjector) {
                stasisProjector.releaseAll();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}

