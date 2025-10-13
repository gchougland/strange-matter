package com.hexvane.strangematter.block;

import com.hexvane.strangematter.sound.StrangeMatterSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import com.hexvane.strangematter.StrangeMatterMod;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResearchMachineBlock extends Block implements EntityBlock {
    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    
    public ResearchMachineBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLUE)
            .strength(3.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .lightLevel(state -> 7) // Emit light level 7
            .noOcclusion() // Make block invisible so only the entity renders
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
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    
    @Override
    public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        
        // Check if player is holding a research note
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof com.hexvane.strangematter.item.ResearchNoteItem) {
            if (!level.isClientSide) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof ResearchMachineBlockEntity researchMachine) {
                    if (researchMachine.insertResearchNote(heldItem, player)) {
                        // Remove the research note from player's hand
                        if (!player.getAbilities().instabuild) {
                            heldItem.shrink(1);
                        }
                        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("block.strangematter.research_machine.note_inserted"));
                        return InteractionResult.SUCCESS;
                    } else {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("block.strangematter.research_machine.already_has_note"));
                        // Play reject sound
                        level.playSound(null, pos, StrangeMatterSounds.RESEARCH_MACHINE_NOTE_REJECT.get(), 
                            net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 1.0f);
                        return InteractionResult.PASS;
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        
        // Default behavior: open GUI
        if (level.isClientSide) {
            // Open GUI on client side
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                BlockEntity finalBlockEntity = blockEntity;
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> 
                    com.hexvane.strangematter.client.ScreenHelper.openResearchMachineScreen(finalBlockEntity));
            }
        }
        
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.ENTITYBLOCK_ANIMATED;
    }
    
    
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return StrangeMatterMod.RESEARCH_MACHINE_BLOCK_ENTITY.get().create(pos, state);
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == StrangeMatterMod.RESEARCH_MACHINE_BLOCK_ENTITY.get() && !level.isClientSide ? 
            (level1, pos, state1, blockEntity) -> {
                // Add any server-side ticking logic here if needed
            } : null;
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Block is being removed, drop the research note if there is one
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ResearchMachineBlockEntity researchMachine) {
                // Check if there's a research note to drop
                if (researchMachine.getCurrentState() != ResearchMachineBlockEntity.MachineState.IDLE) {
                    // Create and drop the research note
                    if (!level.isClientSide) {
                        String researchId = researchMachine.getCurrentResearchId();
                        Set<com.hexvane.strangematter.research.ResearchType> activeTypes = researchMachine.getActiveResearchTypes();
                        
                        if (!researchId.isEmpty() && !activeTypes.isEmpty()) {
                            // Create a map of research types with default costs
                            Map<com.hexvane.strangematter.research.ResearchType, Integer> researchCosts = new HashMap<>();
                            for (com.hexvane.strangematter.research.ResearchType type : activeTypes) {
                                researchCosts.put(type, 1); // Default cost of 1
                            }
                            
                            ItemStack researchNote = com.hexvane.strangematter.item.ResearchNoteItem.createResearchNote(researchCosts, researchId);
                            
                            // Drop the research note at the block's position
                            net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                                level, 
                                pos.getX() + 0.5, 
                                pos.getY() + 1.0, 
                                pos.getZ() + 0.5, 
                                researchNote
                            );
                            itemEntity.setDefaultPickUpDelay();
                            level.addFreshEntity(itemEntity);
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
