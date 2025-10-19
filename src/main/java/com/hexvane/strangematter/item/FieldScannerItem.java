package com.hexvane.strangematter.item;

import com.hexvane.strangematter.research.ResearchData;
import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.research.ScannableObject;
import com.hexvane.strangematter.research.ScannableObjectRegistry;
import com.hexvane.strangematter.network.NetworkHandler;
import com.hexvane.strangematter.network.ResearchGainPacket;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;

public class FieldScannerItem extends Item {
    private static final int SCAN_DURATION = 40; // 2 seconds at 20 TPS
    private static final String SCANNING_TAG = "scanning";
    private static final String SCAN_TARGET_TAG = "scan_target";
    private static final String SCAN_PROGRESS_TAG = "scan_progress";
    private static final String SCAN_ENTITY_TAG = "scan_entity";
    
    public FieldScannerItem() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new com.hexvane.strangematter.client.FieldScannerRenderer());
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // Check if already scanning
        if (isScanning(stack)) {
            return InteractionResultHolder.pass(stack);
        }
        
        // For general use (not targeting specific entities/blocks), do nothing
        
        return InteractionResultHolder.pass(stack);
    }
    
    // Called by the event handler when interacting with entities
    public void onInteractWithEntity(ItemStack stack, Player player, Entity entity, InteractionHand hand) {
        if (player.level().isClientSide) {
            return;
        }
        
        if (ScannableObjectRegistry.isEntityScannable(entity)) {
            // Check if this is an anomaly spawned from a capsule
            if (entity instanceof com.hexvane.strangematter.entity.BaseAnomalyEntity anomaly) {
                if (anomaly.isSpawnedFromCapsule()) {
                    player.sendSystemMessage(Component.literal("§cThis anomaly was spawned from a containment capsule and cannot be scanned for research."));
                    return;
                }
            }
            
            Optional<ScannableObject> scannable = ScannableObjectRegistry.getScannableForEntity(entity);
            if (scannable.isPresent()) {
                String objectId = scannable.get().generateObjectId(entity);
                ResearchData researchData = ResearchData.get(player);
                
                if (!researchData.hasScanned(objectId)) {
                    // Only show message if not already scanning
                    if (!isScanning(stack)) {
                        startScanning(stack, objectId, scannable.get(), entity);
                        player.sendSystemMessage(Component.literal("§eScanning " + entity.getDisplayName().getString() + "..."));
                        
                        // Start the use animation
                        player.startUsingItem(hand);
                    }
                } else {
                    // Only show message if not already scanning and not on cooldown to prevent spam
                    if (!isScanning(stack) && !isOnCooldown(stack)) {
                        player.sendSystemMessage(Component.literal("§cThis anomaly has already been scanned."));
                        // Set a short cooldown to prevent message spam
                        setCooldown(stack, 1000); // 1 second cooldown
                    }
                }
            }
        } else {
            player.sendSystemMessage(Component.literal("§cNo research data available from this entity."));
        }
    }
    
    // Called by the event handler when interacting with blocks
    public void onInteractWithBlock(ItemStack stack, Player player, BlockPos pos, BlockState blockState, ScannableObject scannable, InteractionHand hand) {
        if (player.level().isClientSide) {
            return;
        }
        
        String objectId = scannable.generateObjectId(pos);
        ResearchData researchData = ResearchData.get(player);
        
        if (!researchData.hasScanned(objectId)) {
            // Only show message if not already scanning
            if (!isScanning(stack)) {
                startScanning(stack, objectId, scannable);
                player.sendSystemMessage(Component.literal("§eScanning " + blockState.getBlock().getName().getString() + "..."));
                
                // Start the use animation
                player.startUsingItem(hand);
            }
        } else {
            // Only show message if not already scanning and not on cooldown to prevent spam
            if (!isScanning(stack) && !isOnCooldown(stack)) {
                player.sendSystemMessage(Component.literal("§cThis anomaly has already been scanned."));
                // Set a short cooldown to prevent message spam
                setCooldown(stack, 1000); // 1 second cooldown
            }
        }
    }
    
    
    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) {
            return;
        }
        
        if (!isScanning(stack)) {
            return;
        }
        
        int progress = getScanProgress(stack);
        progress++;
        setScanProgress(stack, progress);
        
        // Play scanning sound
        if (progress % 10 == 0) { // Every 0.5 seconds
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                com.hexvane.strangematter.sound.StrangeMatterSounds.FIELD_SCANNER_SCAN.get(), SoundSource.PLAYERS, 0.3f, 1.2f);
        }
        
        if (progress >= SCAN_DURATION) {
            completeScan(level, player, stack);
            // Stop the use animation
            player.stopUsingItem();
        }
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) {
            return;
        }
        
        if (isScanning(stack)) {
            // Cancel scan if player releases early
            stopScanning(stack);
            player.sendSystemMessage(Component.literal("§cScan cancelled."));
        }
    }
    
    @Override
    public int getUseDuration(ItemStack stack) {
        return isScanning(stack) ? SCAN_DURATION : 0;
    }
    
    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return isScanning(oldStack) && isScanning(newStack);
    }
    
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isScanning(stack) ? UseAnim.SPYGLASS : UseAnim.NONE;
    }
    
    private void startScanning(ItemStack stack, String objectId, ScannableObject scannable) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(SCANNING_TAG, true);
        tag.putString(SCAN_TARGET_TAG, objectId);
        tag.putString("scan_type", scannable.getResearchType().getName());
        tag.putInt("scan_amount", scannable.getResearchAmount());
        tag.putInt(SCAN_PROGRESS_TAG, 0);
        
        // Start the use animation by setting the use duration
        // This will trigger the onUseTick method
    }
    
    private void startScanning(ItemStack stack, String objectId, ScannableObject scannable, Entity entity) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(SCANNING_TAG, true);
        tag.putString(SCAN_TARGET_TAG, objectId);
        tag.putString("scan_type", scannable.getResearchType().getName());
        tag.putInt("scan_amount", scannable.getResearchAmount());
        tag.putInt(SCAN_PROGRESS_TAG, 0);
        tag.putString(SCAN_ENTITY_TAG, entity.getType().toString());
        
        // Start the use animation by setting the use duration
        // This will trigger the onUseTick method
    }
    
    private void stopScanning(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(SCANNING_TAG, false);
        tag.remove(SCAN_TARGET_TAG);
        tag.remove("scan_type");
        tag.remove("scan_amount");
        tag.remove(SCAN_PROGRESS_TAG);
        tag.remove(SCAN_ENTITY_TAG);
        // Add a cooldown to prevent immediate re-scanning
        tag.putLong("scan_cooldown", System.currentTimeMillis() + 1000); // 1 second cooldown
    }
    
    private void completeScan(Level level, Player player, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        
        String objectId = tag.getString(SCAN_TARGET_TAG);
        ResearchType researchType = ResearchType.fromName(tag.getString("scan_type"));
        int amount = tag.getInt("scan_amount");
        
        if (researchType != null) {
            // Add research points
            ResearchData researchData = ResearchData.get(player);
            researchData.addResearchPoints(researchType, amount);
            researchData.markAsScanned(objectId);
            
            // Sync to client
            if (player instanceof ServerPlayer serverPlayer) {
                researchData.syncToClient(serverPlayer);
                NetworkHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer), 
                    new ResearchGainPacket(researchType, amount)
                );
                
                // Trigger advancement for scanning anomaly
                String entityType = tag.getString(SCAN_ENTITY_TAG);
                if (!entityType.isEmpty()) {
                    // Find the entity by type to trigger the advancement
                    level.getEntitiesOfClass(Entity.class, player.getBoundingBox().inflate(10.0))
                        .stream()
                        .filter(entity -> entity.getType().toString().equals(entityType))
                        .findFirst()
                        .ifPresent(entity -> {
                            StrangeMatterMod.SCAN_ANOMALY_TRIGGER.trigger(serverPlayer, entity);
                        });
                }
            }
            
            // Play success sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.5f);
            
            // Research points are displayed via the overlay, not chat
        }
        
        stopScanning(stack);
    }
    
    public boolean isScanning(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(SCANNING_TAG);
    }
    
    public boolean isOnCooldown(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return false;
        long cooldown = tag.getLong("scan_cooldown");
        return System.currentTimeMillis() < cooldown;
    }
    
    private void setCooldown(ItemStack stack, long durationMs) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong("scan_cooldown", System.currentTimeMillis() + durationMs);
    }
    
    private int getScanProgress(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(SCAN_PROGRESS_TAG) : 0;
    }
    
    private void setScanProgress(ItemStack stack, int progress) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(SCAN_PROGRESS_TAG, progress);
    }
}
