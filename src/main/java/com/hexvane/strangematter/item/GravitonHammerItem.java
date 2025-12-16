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
import net.minecraft.server.level.ServerPlayer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

public class GravitonHammerItem extends Item {
    private static final String CHARGING_TAG = "charging";
    private static final String CHARGE_LEVEL_TAG = "charge_level";
    private static final String CHARGE_TIME_TAG = "charge_time";

    /**
     * Reentrancy guard: when we break extra blocks via {@link ServerPlayer} game mode,
     * Minecraft will call {@link #mineBlock} for each additional block. We must not
     * trigger AoE/tunnel mining recursively.
     */
    private static final ThreadLocal<Boolean> INTERNAL_MINING = ThreadLocal.withInitial(() -> false);
    
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
    public int getUseDuration(@Nonnull ItemStack stack) {
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
            // Prevent recursion when breaking extra blocks through the real player break path
            if (Boolean.TRUE.equals(INTERNAL_MINING.get())) {
                return true;
            }

            // If crouching, only mine the single block
            if (player.isCrouching()) {
                // Vanilla already mined this single block; do not perform any extra breaking.
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
    
    @Override
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
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("§7Left click: 3x3 area mining"));
        tooltipComponents.add(Component.literal("§7Left click + Crouch: Single block mining"));
        tooltipComponents.add(Component.literal("§7Right click hold: Charged tunnel mining"));
    }
    
    private void startCharging(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(CHARGING_TAG, true);
        tag.putInt(CHARGE_LEVEL_TAG, 0);
        tag.putInt(CHARGE_TIME_TAG, 0);
    }
    
    private void stopCharging(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.putBoolean(CHARGING_TAG, false);
            tag.putInt(CHARGE_LEVEL_TAG, 0);
            tag.putInt(CHARGE_TIME_TAG, 0);
        }
    }
    
    private boolean isCharging(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(CHARGING_TAG);
    }
    
    private int getChargeLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(CHARGE_LEVEL_TAG) : 0;
    }
    
    private void setChargeLevel(ItemStack stack, int level) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(CHARGE_LEVEL_TAG, level);
    }
    
    private int getChargeTime(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(CHARGE_TIME_TAG) : 0;
    }
    
    private void setChargeTime(ItemStack stack, int time) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(CHARGE_TIME_TAG, time);
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
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        
        // Get the face being mined to determine 3x3 orientation
        HitResult hitResult = player.pick(5.0, 0.0f, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) return;
        
        BlockHitResult blockHit = (BlockHitResult) hitResult;
        Direction face = blockHit.getDirection();
        
        // Calculate the 3x3 area based on the face
        List<BlockPos> blocksToMine = getAreaMiningPositions(centerPos, face);
        
        boolean brokeAny = false;
        INTERNAL_MINING.set(true);
        try {
            // Mine each block. Stop on first denied/unbreakable block.
            for (BlockPos pos : blocksToMine) {
                BlockState state = level.getBlockState(pos);

                // Air should not stop the AoE; just skip it.
                if (state.isAir()) {
                    continue;
                }

                if (!canAttemptBreak(state, level, pos, player)) {
                    // Unbreakable (bedrock/etc) or basic permission denial -> stop immediately.
                    break;
                }

                boolean broke = serverPlayer.gameMode.destroyBlock(pos);
                if (!broke) {
                    // Protection mods (FTB Chunks) cancel here; stop immediately as requested.
                    break;
                }
                brokeAny = true;
            }
        } finally {
            INTERNAL_MINING.set(false);
        }

        if (brokeAny) {
            // Optional feedback; keep vanilla-ish and quiet.
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.35f, 1.0f);
        }
    }
    
    private void performTunnelMining(Level level, Player player, ItemStack stack, int chargeLevel) {
        if (level.isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        
        // Get the direction the player is looking
        HitResult hitResult = player.pick(5.0, 0.0f, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            // If not looking at a block, use player's facing direction
            Direction face = player.getDirection();
            BlockPos startPos = player.blockPosition().relative(face);
            performSimpleTunnelMining(level, serverPlayer, startPos, face, chargeLevel);
            return;
        }
        
        BlockHitResult blockHit = (BlockHitResult) hitResult;
        Direction face = blockHit.getDirection();
        BlockPos hitBlockPos = blockHit.getBlockPos();
        
        // Invert the direction so tunnel goes INTO the block, not behind it
        Direction tunnelDirection = face.getOpposite();
        
        // Start the tunnel from the block we're looking at (not behind it)
        BlockPos startPos = hitBlockPos;
        
        performSimpleTunnelMining(level, serverPlayer, startPos, tunnelDirection, chargeLevel);
    }
    
    private void performSimpleTunnelMining(Level level, ServerPlayer player, BlockPos startPos, Direction face, int chargeLevel) {
        int tunnelDepth = getTunnelDepth(chargeLevel);

        boolean brokeAny = false;
        INTERNAL_MINING.set(true);
        try {
            // Mine layer-by-layer; stop the tunnel on first denied/unbreakable block.
            for (int i = 0; i < tunnelDepth; i++) {
                BlockPos layerOrigin = startPos.relative(face, i);
                List<BlockPos> layerPositions = getTunnelLayerPositions(layerOrigin, face.getAxis());

                for (BlockPos pos : layerPositions) {
                    BlockState state = level.getBlockState(pos);

                    // Air should not stop the tunnel; just skip it.
                    if (state.isAir()) {
                        continue;
                    }

                    if (!canAttemptBreak(state, level, pos, player)) {
                        return; // stop tunnel immediately
                    }

                    boolean broke = player.gameMode.destroyBlock(pos);
                    if (!broke) {
                        return; // stop tunnel immediately (claims/protection/etc)
                    }
                    brokeAny = true;
                }
            }
        } finally {
            INTERNAL_MINING.set(false);
        }

        if (brokeAny) {
            // Play mining sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                StrangeMatterSounds.GRAVITON_CHARGEUP.get(), SoundSource.PLAYERS, 1.0f, 0.8f);
        }
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
    
    private int getTunnelDepth(int chargeLevel) {
        switch (chargeLevel) {
            case 1: return getTunnelDepthLevel1();
            case 2: return getTunnelDepthLevel2();
            case 3: return getTunnelDepthLevel3();
            default: return 0;
        }
    }
    
    private boolean canAttemptBreak(BlockState state, Level level, BlockPos pos, Player player) {
        // Never attempt to break air
        if (state.isAir()) {
            return false;
        }

        // Never attempt to break unbreakable blocks (bedrock, barriers, etc.)
        float destroySpeed = state.getDestroySpeed(level, pos);
        if (destroySpeed < 0.0f) {
            return false;
        }

        // Basic vanilla permission gate (spawn protection, etc.). Claims are handled by the real break path.
        if (!level.mayInteract(player, pos)) {
            return false;
        }

        // If the block requires the correct tool, only allow pickaxe-mineable blocks for this hammer.
        if (state.requiresCorrectToolForDrops() && !state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return false;
        }

        return true;
    }

    private List<BlockPos> getTunnelLayerPositions(BlockPos origin, Direction.Axis axis) {
        List<BlockPos> positions = new java.util.ArrayList<>(9);
        if (axis == Direction.Axis.X) {
            // 3x3 in YZ plane
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    positions.add(origin.offset(0, y, z));
                }
            }
        } else if (axis == Direction.Axis.Y) {
            // 3x3 in XZ plane
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    positions.add(origin.offset(x, 0, z));
                }
            }
        } else {
            // 3x3 in XY plane
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    positions.add(origin.offset(x, y, 0));
                }
            }
        }
        return positions;
    }
    
    @Override
    public void initializeClient(@Nonnull java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new com.hexvane.strangematter.client.GravitonHammerRenderer());
    }
}
