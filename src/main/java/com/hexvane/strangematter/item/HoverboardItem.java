package com.hexvane.strangematter.item;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.HoverboardEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class HoverboardItem extends Item {
    private static final float PARTIAL_TICK = 1.0F;
    private static final int REQUIRED_CLEAR_SPACE = 2; // Blocks of clearance needed above surface
    
    public HoverboardItem() {
        super(new Item.Properties());
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // Client-side - do nothing
        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }
        
        // Use the player's actual reach distance (accounts for creative/survival mode)
        double reachDistance = player.getBlockReach();
        
        // Ray trace to find what block the player is looking at
        HitResult hitResult = player.pick(reachDistance, PARTIAL_TICK, false);
        
        // Check if we hit a block within reasonable range
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            sendErrorMessage(player, "No suitable surface found within reach");
            return InteractionResultHolder.fail(stack);
        }
        
        // Validate the hit result is actually within reach (edge case protection)
        BlockHitResult blockHit = (BlockHitResult) hitResult;
        if (blockHit.getLocation().distanceTo(player.position()) > reachDistance) {
            sendErrorMessage(player, "Target surface is too far away");
            return InteractionResultHolder.fail(stack);
        }
        
        // Get the targeted block
        BlockPos targetPos = blockHit.getBlockPos();
        BlockState targetBlock = level.getBlockState(targetPos);
        
        // Check if the block is solid
        if (!isValidSupportBlock(level, targetPos, targetBlock)) {
            sendErrorMessage(player, "Cannot deploy hoverboard on " + getBlockName(targetBlock) + " - surface not solid enough");
            return InteractionResultHolder.fail(stack);
        }
        
        // Check if there's enough space above the block
        if (!hasClearSpaceAbove(level, targetPos)) {
            sendErrorMessage(player, "Not enough space above the surface to deploy hoverboard");
            return InteractionResultHolder.fail(stack);
        }
        
        // Calculate placement position on top of the block
        Vec3 placementPos = new Vec3(
            targetPos.getX() + 0.5, 
            targetPos.getY() + 1.0, 
            targetPos.getZ() + 0.5
        );
        
        // Spawn the hoverboard
        spawnHoverboard(level, placementPos, player);
        
        // Play deployment sound
        level.playSound(null, placementPos.x, placementPos.y, placementPos.z, 
               SoundEvents.DISPENSER_DISPENSE, SoundSource.PLAYERS, 0.8f, 1.4f);
        
        // Consume the item (unless in creative mode)
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        
        return InteractionResultHolder.success(stack);
    }
    
    private boolean isValidSupportBlock(Level level, BlockPos pos, BlockState blockState) {
        // Air is never valid
        if (blockState.isAir()) {
            return false;
        }
        
        // Check if the block has a solid collision shape
        VoxelShape shape = blockState.getCollisionShape(level, pos);
        return !shape.isEmpty() && !shape.equals(Shapes.empty());
    }
    
    private boolean hasClearSpaceAbove(Level level, BlockPos supportBlock) {
        // Check multiple blocks above for adequate clearance
        for (int i = 1; i <= REQUIRED_CLEAR_SPACE; i++) {
            BlockPos checkPos = supportBlock.above(i);
            BlockState checkState = level.getBlockState(checkPos);
            
            // If any block in the required space is not clear, fail
            if (!checkState.isAir() && !checkState.canBeReplaced()) {
                return false;
            }
        }
        return true;
    }
    
    private void spawnHoverboard(Level level, Vec3 position, Player player) {
        // Create hoverboard entity
        HoverboardEntity hoverboard = new HoverboardEntity(StrangeMatterMod.HOVERBOARD_ENTITY.get(), level);
        
        // Adjust spawn position to prevent glitching
        // The 'position' from raycast is the exact click point, but we need to spawn above the block
        double spawnX = position.x;
        double spawnY = position.y + 0.1; // Slightly above the surface to prevent collision glitching
        double spawnZ = position.z;
        
        // Set position and rotation
        hoverboard.moveTo(spawnX, spawnY, spawnZ, player.getYRot(), 0.0f);
        
        // Add the entity to the world
        level.addFreshEntity(hoverboard);
    }
    
    private void sendSuccessMessage(Player player, String message) {
        player.displayClientMessage(Component.literal("§a" + message), true);
    }
    
    private void sendErrorMessage(Player player, String message) {
        player.displayClientMessage(Component.literal("§c" + message), true);
    }
    
    private String getBlockName(BlockState blockState) {
        return blockState.getBlock().getName().getString();
    }
}