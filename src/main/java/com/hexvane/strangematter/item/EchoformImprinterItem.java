package com.hexvane.strangematter.item;

import com.hexvane.strangematter.morph.PlayerMorphData;
import com.hexvane.strangematter.client.PlayerMorphRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class EchoformImprinterItem extends Item {
    private static final int SCAN_DURATION = 20; // 1 second at 20 TPS
    private static final String SCANNING_TAG = "scanning";
    private static final String SCAN_TARGET_TAG = "scan_target";
    private static final String SCAN_PROGRESS_TAG = "scan_progress";
    private static final String MORPHED_ENTITY_TAG = "morphed_entity";
    private static final String MORPHED_TAG = "is_morphed";
    private static final double SCAN_RANGE = 5.0;
    
    public EchoformImprinterItem() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // Check if already scanning
        if (isScanning(stack)) {
            return InteractionResultHolder.pass(stack);
        }
        
        // Check if shift-clicking to morph back
        if (player.isShiftKeyDown()) {
            if (isMorphed(stack)) {
                morphBack(level, player, stack);
                return InteractionResultHolder.success(stack);
            } else {
                if (!level.isClientSide) {
                    player.sendSystemMessage(Component.literal("§cYou are not currently morphed."));
                }
                return InteractionResultHolder.fail(stack);
            }
        }
        
        // Try to find a nearby mob or player to scan
        if (!level.isClientSide) {
            Vec3 playerPos = player.position();
            AABB searchBox = new AABB(playerPos.x - SCAN_RANGE, playerPos.y - SCAN_RANGE, playerPos.z - SCAN_RANGE,
                                      playerPos.x + SCAN_RANGE, playerPos.y + SCAN_RANGE, playerPos.z + SCAN_RANGE);
            
            // First check for other players
            List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, searchBox, otherPlayer -> {
                return otherPlayer != player && otherPlayer.isAlive() && player.hasLineOfSight(otherPlayer);
            });
            
            if (!nearbyPlayers.isEmpty()) {
                Player targetPlayer = getTargetPlayer(player, nearbyPlayers);
                if (targetPlayer != null) {
                    startScanningPlayer(stack, targetPlayer, player);
                    player.startUsingItem(hand);
                    return InteractionResultHolder.success(stack); // Use SUCCESS to prevent other interactions
                }
            }
            
            // If no players, check for mobs
            List<Mob> nearbyMobs = level.getEntitiesOfClass(Mob.class, searchBox, mob -> {
                return mob.isAlive() && player.hasLineOfSight(mob);
            });
            
            if (!nearbyMobs.isEmpty()) {
                // Find the closest mob the player is looking at
                Mob targetMob = getTargetMob(player, nearbyMobs);
                
                if (targetMob != null) {
                    startScanning(stack, targetMob, player);
                    player.startUsingItem(hand);
                    return InteractionResultHolder.success(stack); // Use SUCCESS to prevent other interactions
                }
            }
            
            player.sendSystemMessage(Component.literal("§cNo valid target in range to scan."));
        }
        
        return InteractionResultHolder.pass(stack);
    }
    
    private Mob getTargetMob(Player player, List<Mob> mobs) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerEye = player.getEyePosition();
        
        Mob closestMob = null;
        double closestDot = 0.85; // Minimum dot product (angle threshold)
        
        for (Mob mob : mobs) {
            Vec3 toMob = mob.position().subtract(playerEye).normalize();
            double dot = lookVec.dot(toMob);
            
            if (dot > closestDot) {
                closestDot = dot;
                closestMob = mob;
            }
        }
        
        return closestMob;
    }
    
    private Player getTargetPlayer(Player player, List<Player> players) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerEye = player.getEyePosition();
        
        Player closestPlayer = null;
        double closestDot = 0.85; // Minimum dot product (angle threshold)
        
        for (Player targetPlayer : players) {
            Vec3 toPlayer = targetPlayer.position().subtract(playerEye).normalize();
            double dot = lookVec.dot(toPlayer);
            
            if (dot > closestDot) {
                closestDot = dot;
                closestPlayer = targetPlayer;
            }
        }
        
        return closestPlayer;
    }
    
    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        
        if (!isScanning(stack)) {
            return;
        }
        
        int progress = getScanProgress(stack);
        progress++;
        setScanProgress(stack, progress);
        
        // Play scanning sound
        if (progress % 5 == 0) { // Every 0.25 seconds
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                com.hexvane.strangematter.sound.StrangeMatterSounds.FIELD_SCANNER_SCAN.get(), SoundSource.PLAYERS, 0.4f, 1.4f);
        }
        
        // Spawn particles during scanning
        if (level.isClientSide) {
            UUID targetUUID = getTargetUUID(stack);
            if (targetUUID != null) {
                Entity target = findEntityByUUID(level, targetUUID);
                if (target != null) {
                    spawnScanParticles(level, player, target);
                }
            }
        }
        
        if (progress >= SCAN_DURATION) {
            if (!level.isClientSide) {
                completeScan(level, player, stack);
            }
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
    public UseAnim getUseAnimation(ItemStack stack) {
        return isScanning(stack) ? UseAnim.BOW : UseAnim.NONE;
    }
    
    private void startScanning(ItemStack stack, Mob target, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        
        // Clear any old scan data first
        tag.remove(MORPHED_ENTITY_TAG);
        tag.remove("target_type");
        
        // Get the proper resource location for the entity type
        net.minecraft.resources.ResourceLocation entityId = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        String entityTypeString = entityId != null ? entityId.toString() : target.getType().toString();
        
        // Set new scan data
        tag.putBoolean(SCANNING_TAG, true);
        tag.putUUID(SCAN_TARGET_TAG, target.getUUID());
        tag.putString("target_type", entityTypeString);
        tag.putInt(SCAN_PROGRESS_TAG, 0);
        
        player.sendSystemMessage(Component.literal("§eScanning " + target.getDisplayName().getString() + "..."));
    }
    
    private void startScanningPlayer(ItemStack stack, Player target, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        
        // Clear any old scan data first
        tag.remove(MORPHED_ENTITY_TAG);
        tag.remove("target_type");
        tag.remove("target_player_uuid");
        tag.remove("target_player_name");
        
        // Use minecraft:player as the entity type
        String entityTypeString = "minecraft:player";
        
        // Set new scan data including player-specific info
        tag.putBoolean(SCANNING_TAG, true);
        tag.putUUID(SCAN_TARGET_TAG, target.getUUID());
        tag.putString("target_type", entityTypeString);
        tag.putUUID("target_player_uuid", target.getUUID());
        tag.putString("target_player_name", target.getName().getString());
        tag.putInt(SCAN_PROGRESS_TAG, 0);
        
        player.sendSystemMessage(Component.literal("§eScanning " + target.getName().getString() + "..."));
    }
    
    private void stopScanning(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(SCANNING_TAG, false);
        tag.remove(SCAN_TARGET_TAG);
        tag.remove("target_type");
        tag.remove(SCAN_PROGRESS_TAG);
    }
    
    private void completeScan(Level level, Player player, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        
        String targetType = tag.getString("target_type");
        
        if (targetType != null && !targetType.isEmpty()) {
            // First clear any existing morph and cached entity
            String oldMorph = PlayerMorphData.getMorphEntityType(player.getUUID());
            if (oldMorph != null) {
                PlayerMorphData.clearMorph(player.getUUID());
                // Force cleanup of cached morph entity on both sides
                PlayerMorphRenderer.cleanupMorphEntity(player.getUUID());
            }
            
            // Store the NEW morphed entity type in item NBT
            tag.putString(MORPHED_ENTITY_TAG, targetType);
            tag.putBoolean(MORPHED_TAG, true);
            
            // If morphing into a player, store their UUID for skin rendering
            UUID targetPlayerUUID = null;
            if (targetType.equals("minecraft:player") && tag.hasUUID("target_player_uuid")) {
                targetPlayerUUID = tag.getUUID("target_player_uuid");
            }
            
            // Apply the NEW morph using the PlayerMorphData system
            PlayerMorphData.setMorph(player.getUUID(), targetType, targetPlayerUUID);
            
            // Sync to ALL clients (not just tracking, to ensure visibility)
            if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                com.hexvane.strangematter.network.PlayerMorphSyncPacket packet = 
                    new com.hexvane.strangematter.network.PlayerMorphSyncPacket(player.getUUID(), targetType, targetPlayerUUID, false);
                
                // Send to ALL players on the server for guaranteed visibility
                com.hexvane.strangematter.network.NetworkHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                    packet
                );
                
                System.out.println("DEBUG: Sent morph sync to ALL players");
            }
            
            // Debug logging
            if (!level.isClientSide) {
                System.out.println("DEBUG: Completed scan - Setting morph to: " + targetType);
                System.out.println("DEBUG: Old morph was: " + oldMorph);
                System.out.println("DEBUG: Target player UUID: " + targetPlayerUUID);
                System.out.println("DEBUG: Stored in item NBT: " + tag.getString(MORPHED_ENTITY_TAG));
                System.out.println("DEBUG: Actual morph data: " + PlayerMorphData.getMorphEntityType(player.getUUID()));
            }
            
            // Play success sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.5f);
            
            // Spawn success particles
            if (level instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 20; i++) {
                    double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
                    double offsetY = level.random.nextDouble() * 2.0;
                    double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        1, 0, 0, 0, 0.05);
                }
            }
            
            // Get the entity's display name (use player name if morphing into a player)
            String displayName;
            if (targetType.equals("minecraft:player") && tag.contains("target_player_name")) {
                displayName = tag.getString("target_player_name");
            } else {
                displayName = getEntityDisplayName(targetType);
            }
            player.sendSystemMessage(Component.literal("§aScan complete! You have morphed into " + displayName + "."));
        }
        
        stopScanning(stack);
    }
    
    private void morphBack(Level level, Player player, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        
        tag.putBoolean(MORPHED_TAG, false);
        tag.remove(MORPHED_ENTITY_TAG);
        
        // Clear the morph
        PlayerMorphData.clearMorph(player.getUUID());
        
        // Sync to ALL clients
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            com.hexvane.strangematter.network.PlayerMorphSyncPacket packet = 
                new com.hexvane.strangematter.network.PlayerMorphSyncPacket(player.getUUID(), null, null, true);
            
            // Send to ALL players for guaranteed visibility
            com.hexvane.strangematter.network.NetworkHandler.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                packet
            );
        }
        
        // Clean up the cached morph entity on client
        if (level.isClientSide) {
            PlayerMorphRenderer.cleanupMorphEntity(player.getUUID());
        }
        
        if (!level.isClientSide) {
            // Play sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0f, 1.0f);
            
            // Spawn particles
            if (level instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 15; i++) {
                    double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
                    double offsetY = level.random.nextDouble() * 2.0;
                    double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;
                    serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        1, 0, 0, 0, 0.05);
                }
            }
            
            player.sendSystemMessage(Component.literal("§aYou have morphed back to your normal form."));
        }
    }
    
    private void spawnScanParticles(Level level, Player player, Entity target) {
        // Create a beam effect from player to target
        Vec3 playerPos = player.getEyePosition();
        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
        Vec3 direction = targetPos.subtract(playerPos).normalize();
        
        double distance = playerPos.distanceTo(targetPos);
        int particleCount = (int) (distance * 3);
        
        for (int i = 0; i < particleCount; i++) {
            double t = i / (double) particleCount;
            Vec3 particlePos = playerPos.add(direction.scale(t * distance));
            
            level.addParticle(ParticleTypes.PORTAL,
                particlePos.x + (level.random.nextDouble() - 0.5) * 0.2,
                particlePos.y + (level.random.nextDouble() - 0.5) * 0.2,
                particlePos.z + (level.random.nextDouble() - 0.5) * 0.2,
                0, 0, 0);
        }
        
        // Add particles around the target
        for (int i = 0; i < 3; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * target.getBbWidth();
            double offsetY = level.random.nextDouble() * target.getBbHeight();
            double offsetZ = (level.random.nextDouble() - 0.5) * target.getBbWidth();
            
            level.addParticle(ParticleTypes.ENCHANT,
                target.getX() + offsetX,
                target.getY() + offsetY,
                target.getZ() + offsetZ,
                0, 0.1, 0);
        }
    }
    
    private Entity findEntityByUUID(Level level, UUID uuid) {
        // Search through all loaded entities using getEntities with a large bounding box
        AABB searchBox = new AABB(
            -30000000, -64, -30000000,
            30000000, 320, 30000000
        );
        List<Entity> entities = level.getEntities((Entity)null, searchBox, entity -> entity.getUUID().equals(uuid));
        return entities.isEmpty() ? null : entities.get(0);
    }
    
    /**
     * Get a user-friendly display name for an entity type
     */
    private String getEntityDisplayName(String entityTypeId) {
        try {
            net.minecraft.resources.ResourceLocation resourceLocation = net.minecraft.resources.ResourceLocation.parse(entityTypeId);
            EntityType<?> entityType = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(resourceLocation);
            if (entityType != null) {
                return entityType.getDescription().getString();
            }
        } catch (Exception e) {
            // Fall back to simple name
        }
        
        // Fallback: just remove the namespace and convert underscores to spaces
        String simpleName = entityTypeId.contains(":") ? entityTypeId.split(":")[1] : entityTypeId;
        return simpleName.replace("_", " ");
    }
    
    public boolean isScanning(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(SCANNING_TAG);
    }
    
    public boolean isMorphed(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(MORPHED_TAG);
    }
    
    private UUID getTargetUUID(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.hasUUID(SCAN_TARGET_TAG)) {
            return tag.getUUID(SCAN_TARGET_TAG);
        }
        return null;
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

