package com.hexvane.strangematter.item;

import com.hexvane.strangematter.sound.StrangeMatterSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import javax.annotation.Nonnull;

import java.util.List;

public class GravitonHammerItem extends Item {
    private static final String CHARGING_TAG = "charging";
    private static final String CHARGE_LEVEL_TAG = "charge_level";
    private static final String CHARGE_TIME_TAG = "charge_time";
    
    // These will be loaded from config
    private static int getChargeLevel1Time() { return com.hexvane.strangematter.Config.gravitonHammerChargeLevel1Time; }
    private static int getChargeLevel2Time() { return com.hexvane.strangematter.Config.gravitonHammerChargeLevel2Time; }
    private static int getChargeLevel3Time() { return com.hexvane.strangematter.Config.gravitonHammerChargeLevel3Time; }
    private static int getTunnelDepthLevel1() { return com.hexvane.strangematter.Config.gravitonHammerTunnelDepthLevel1; }
    private static int getTunnelDepthLevel2() { return com.hexvane.strangematter.Config.gravitonHammerTunnelDepthLevel2; }
    private static int getTunnelDepthLevel3() { return com.hexvane.strangematter.Config.gravitonHammerTunnelDepthLevel3; }
    
    public GravitonHammerItem() {
        super(new Item.Properties()
            .stacksTo(1)
            .durability(0) // No durability
        );
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // Start charging on right click
        if (!isCharging(stack)) {
            startCharging(stack);
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        
        return InteractionResultHolder.pass(stack);
    }
    
    @Override
    public void onUseTick(@Nonnull Level level, @Nonnull LivingEntity livingEntity, @Nonnull ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) {
            return;
        }
        
        if (!isCharging(stack)) {
            return;
        }
        
        int chargeTime = getChargeTime(stack);
        chargeTime++;
        setChargeTime(stack, chargeTime);
        
        // Check for charge level upgrades
        int previousLevel = getChargeLevel(stack);
        int newLevel = calculateChargeLevel(chargeTime);
        
        if (newLevel > previousLevel) {
            setChargeLevel(stack, newLevel);
            playChargeLevelSound(level, player, newLevel);
        }
    }
    
    @Override
    public void releaseUsing(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull LivingEntity livingEntity, int timeCharged) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) {
            return;
        }
        
        if (isCharging(stack)) {
            int chargeLevel = getChargeLevel(stack);
            if (chargeLevel > 0) {
                // Perform tunnel mining
                performTunnelMining(level, player, stack, chargeLevel);
            }
            stopCharging(stack);
        }
    }
    
    @Override
    public int getUseDuration(@Nonnull ItemStack stack, @Nonnull LivingEntity entity) {
        return 72000; // Max duration for charging
    }
    
    @Override
    public UseAnim getUseAnimation(@Nonnull ItemStack stack) {
        return UseAnim.NONE;
    }
    
    @Override
    public boolean canAttackBlock(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player) {
        return !player.isCreative();
    }
    
    @Override
    public boolean mineBlock(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull LivingEntity miningEntity) {
        if (!level.isClientSide && miningEntity instanceof Player player) {
            // If crouching, only mine the single block
            if (player.isCrouching()) {
                if (canMineBlock(state, level, pos, player)) {
                    level.destroyBlock(pos, true, player);
                }
            } else {
                // Perform 3x3 mining on left click when not crouching
                performAreaMining(level, player, pos, state);
            }
        }
        return true;
    }
    
    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        // Iron pickaxe speed
        return 6.0f;
    }
    
    public boolean isCorrectToolForDrops(@Nonnull BlockState block) {
        // Can mine anything a diamond pickaxe can mine
        return block.requiresCorrectToolForDrops();
    }
    
    @Override
    public int getEnchantmentValue() {
        return 0; // Cannot be enchanted
    }
    
    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return false; // Cannot be enchanted
    }
    
    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext context, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("§7Left click: 3x3 area mining"));
        tooltipComponents.add(Component.literal("§7Left click + Crouch: Single block mining"));
        tooltipComponents.add(Component.literal("§7Right click hold: Charged tunnel mining"));
    }
    
    private void startCharging(ItemStack stack) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(CHARGING_TAG, true);
            tag.putInt(CHARGE_LEVEL_TAG, 0);
            tag.putInt(CHARGE_TIME_TAG, 0);
        });
    }
    
    private void stopCharging(ItemStack stack) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(CHARGING_TAG, false);
            tag.putInt(CHARGE_LEVEL_TAG, 0);
            tag.putInt(CHARGE_TIME_TAG, 0);
        });
    }
    
    private boolean isCharging(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        return !customData.isEmpty() && customData.copyTag().getBoolean(CHARGING_TAG);
    }
    
    private int getChargeLevel(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        return !customData.isEmpty() ? customData.copyTag().getInt(CHARGE_LEVEL_TAG) : 0;
    }
    
    private void setChargeLevel(ItemStack stack, int level) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(CHARGE_LEVEL_TAG, level);
        });
    }
    
    private int getChargeTime(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        return !customData.isEmpty() ? customData.copyTag().getInt(CHARGE_TIME_TAG) : 0;
    }
    
    private void setChargeTime(ItemStack stack, int time) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(CHARGE_TIME_TAG, time);
        });
    }
    
    
    private int calculateChargeLevel(int chargeTime) {
        if (chargeTime >= getChargeLevel3Time()) {
            return 3;
        } else if (chargeTime >= getChargeLevel2Time()) {
            return 2;
        } else if (chargeTime >= getChargeLevel1Time()) {
            return 1;
        }
        return 0;
    }
    
    private void playChargeLevelSound(Level level, Player player, int chargeLevel) {
        // Play the graviton chargeup sound with different pitches for each level
        switch (chargeLevel) {
            case 1:
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    StrangeMatterSounds.GRAVITON_CHARGEUP.get(), SoundSource.PLAYERS, 0.5f, 1.0f);
                break;
            case 2:
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    StrangeMatterSounds.GRAVITON_CHARGEUP.get(), SoundSource.PLAYERS, 0.7f, 1.2f);
                break;
            case 3:
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    StrangeMatterSounds.GRAVITON_CHARGEUP.get(), SoundSource.PLAYERS, 1.0f, 1.5f);
                break;
        }
    }
    
    private void performAreaMining(Level level, Player player, BlockPos centerPos, BlockState centerState) {
        if (level.isClientSide) return;
        
        // Get the face being mined to determine 3x3 orientation
        HitResult hitResult = player.pick(5.0, 0.0f, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) return;
        
        BlockHitResult blockHit = (BlockHitResult) hitResult;
        Direction face = blockHit.getDirection();
        
        // Calculate the 3x3 area based on the face
        List<BlockPos> blocksToMine = getAreaMiningPositions(centerPos, face);
        
        // Mine each block
        for (BlockPos pos : blocksToMine) {
            BlockState state = level.getBlockState(pos);
            if (canMineBlock(state, level, pos, player)) {
                level.destroyBlock(pos, true, player);
            }
        }
    }
    
    private void performTunnelMining(Level level, Player player, ItemStack stack, int chargeLevel) {
        if (level.isClientSide) return;
        
        // Get the direction the player is looking
        HitResult hitResult = player.pick(5.0, 0.0f, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            // If not looking at a block, use player's facing direction
            Direction face = player.getDirection();
            BlockPos startPos = player.blockPosition().relative(face);
            performSimpleTunnelMining(level, player, startPos, face, chargeLevel);
            return;
        }
        
        BlockHitResult blockHit = (BlockHitResult) hitResult;
        Direction face = blockHit.getDirection();
        BlockPos hitBlockPos = blockHit.getBlockPos();
        
        // Invert the direction so tunnel goes INTO the block, not behind it
        Direction tunnelDirection = face.getOpposite();
        
        // Start the tunnel from the block we're looking at (not behind it)
        BlockPos startPos = hitBlockPos;
        
        performSimpleTunnelMining(level, player, startPos, tunnelDirection, chargeLevel);
    }
    
    private void performSimpleTunnelMining(Level level, Player player, BlockPos startPos, Direction face, int chargeLevel) {
        int tunnelDepth = getTunnelDepth(chargeLevel);
        
        // Get all blocks in the tunnel
        List<BlockPos> allBlocks = new java.util.ArrayList<>();
        for (int i = 0; i < tunnelDepth; i++) {
            BlockPos currentPos = startPos.relative(face, i);
            
            // Add 3x3 cross-section for each depth level
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (face.getAxis() == Direction.Axis.X) {
                            // Tunnel along X axis, so 3x3 in YZ plane
                            allBlocks.add(currentPos.offset(0, y, z));
                        } else if (face.getAxis() == Direction.Axis.Y) {
                            // Tunnel along Y axis, so 3x3 in XZ plane
                            allBlocks.add(currentPos.offset(x, 0, z));
                        } else {
                            // Tunnel along Z axis, so 3x3 in XY plane
                            allBlocks.add(currentPos.offset(x, y, 0));
                        }
                    }
                }
            }
        }
        
        // Mine all blocks at once
        for (BlockPos pos : allBlocks) {
            BlockState state = level.getBlockState(pos);
            if (canMineBlock(state, level, pos, player)) {
                level.destroyBlock(pos, true, player);
            }
        }
        
        // Play mining sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
            StrangeMatterSounds.GRAVITON_CHARGEUP.get(), SoundSource.PLAYERS, 1.0f, 0.8f);
    }
    
    
    private List<BlockPos> getAreaMiningPositions(BlockPos center, Direction face) {
        List<BlockPos> positions = new java.util.ArrayList<>();
        
        // For top/bottom faces, mine in horizontal plane
        if (face == Direction.UP || face == Direction.DOWN) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    positions.add(center.offset(x, 0, z));
                }
            }
        }
        // For cardinal faces, mine in the plane perpendicular to the face
        else {
            Direction.Axis axis = face.getAxis();
            if (axis == Direction.Axis.X) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        positions.add(center.offset(0, y, z));
                    }
                }
            } else if (axis == Direction.Axis.Z) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        positions.add(center.offset(x, y, 0));
                    }
                }
            }
        }
        
        return positions;
    }
    
    private List<List<BlockPos>> getTunnelPositionsByLayer(BlockPos start, Direction direction, int depth) {
        List<List<BlockPos>> layers = new java.util.ArrayList<>();
        
        // Create a 3x3 tunnel extending in the direction, organized by layer
        for (int i = 0; i < depth; i++) {
            BlockPos currentPos = start.relative(direction, i);
            List<BlockPos> currentLayer = new java.util.ArrayList<>();
            
            // Add 3x3 cross-section at this depth
            Direction.Axis axis = direction.getAxis();
            if (axis == Direction.Axis.Y) {
                // Vertical tunnel - 3x3 horizontal cross-section
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        currentLayer.add(currentPos.offset(x, 0, z));
                    }
                }
            } else {
                // Horizontal tunnel - 3x3 vertical cross-section perpendicular to direction
                Direction up = Direction.UP;
                Direction right = direction.getClockWise();
                
                for (int upOffset = -1; upOffset <= 1; upOffset++) {
                    for (int rightOffset = -1; rightOffset <= 1; rightOffset++) {
                        currentLayer.add(currentPos.relative(up, upOffset).relative(right, rightOffset));
                    }
                }
            }
            
            layers.add(currentLayer);
        }
        
        return layers;
    }
    
    private int getTunnelDepth(int chargeLevel) {
        switch (chargeLevel) {
            case 1: return getTunnelDepthLevel1();
            case 2: return getTunnelDepthLevel2();
            case 3: return getTunnelDepthLevel3();
            default: return 0;
        }
    }
    
    private boolean canMineBlock(BlockState state, Level level, BlockPos pos, Player player) {
        // Check if the block can be mined with this tool
        if (!state.requiresCorrectToolForDrops()) {
            return true; // Can always mine blocks that don't require tools
        }
        
        // Check if this tool can mine the block (diamond pickaxe level)
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return false;
        }
        
        // Check if player can break the block (permissions, etc.)
        return level.mayInteract(player, pos);
    }
    
    @Override
    public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new com.hexvane.strangematter.client.GravitonHammerRenderer());
    }
}
