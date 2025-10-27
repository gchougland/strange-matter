package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

public class AnomalousGrassBlock extends Block {
    
    public AnomalousGrassBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GREEN)
            .strength(0.6f)
            .pushReaction(PushReaction.DESTROY)
            .noOcclusion()
            .lightLevel(state -> 3) // Emit light level 3
        );
    }
    
    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.GRASS;
    }
    
    
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        
        // Spawn particles occasionally
        if (random.nextInt(10) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.1;
            double z = pos.getZ() + random.nextDouble();
            
            // Spawn green particles that float upward
            level.addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0.0, 0.1, 0.0);
        }
        
        // Occasionally spawn a small green sparkle
        if (random.nextInt(50) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.5;
            double z = pos.getZ() + random.nextDouble();
            
            level.addParticle(ParticleTypes.ENCHANT, x, y, z, 
                (random.nextDouble() - 0.5) * 0.1, 
                random.nextDouble() * 0.05, 
                (random.nextDouble() - 0.5) * 0.1);
        }
    }
    
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }
    
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        
        // Occasionally spread to nearby grass blocks
        if (random.nextInt(100) == 0) {
            BlockPos spreadPos = pos.offset(
                random.nextInt(3) - 1, 
                random.nextInt(3) - 1, 
                random.nextInt(3) - 1
            );
            
            BlockState spreadState = level.getBlockState(spreadPos);
            if (spreadState.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) {
                level.setBlock(spreadPos, this.defaultBlockState(), 3);
            }
        }
    }
    
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
        
        // Check if a block was placed on top of this anomalous grass
        if (neighborPos.equals(pos.above())) {
            BlockState aboveState = level.getBlockState(pos.above());
            // If there's a solid block above, convert to dirt
            if (aboveState.isSolidRender(level, pos.above())) {
                level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
            }
        }
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Check if the player is using a hoe
        if (itemStack.getItem() instanceof HoeItem) {
            // Convert anomalous grass to farmland
            level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 3);
            
            // Play the hoe sound
            level.playSound(player, pos, net.minecraft.sounds.SoundEvents.HOE_TILL, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            
            // Damage the hoe slightly
            if (!level.isClientSide) {
                itemStack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
            
            return ItemInteractionResult.SUCCESS;
        }
        
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    
    @Override
    public java.util.List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        java.util.List<ItemStack> drops = new java.util.ArrayList<>();
        
        // Check if the tool has silk touch
        if (builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player) {
            ItemStack tool = player.getMainHandItem();
            if (tool.getEnchantmentLevel(builder.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH)) > 0) {
                // Drop the block itself with silk touch
                drops.add(new ItemStack(this));
            } else {
                // Drop dirt without silk touch
                drops.add(new ItemStack(Blocks.DIRT));
            }
        } else {
            // Default behavior: drop dirt
            drops.add(new ItemStack(Blocks.DIRT));
        }
        
        return drops;
    }
}
