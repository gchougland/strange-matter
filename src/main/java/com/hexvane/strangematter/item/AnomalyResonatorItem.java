package com.hexvane.strangematter.item;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.GravityAnomalyEntity;
import com.hexvane.strangematter.entity.BaseAnomalyEntity;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class AnomalyResonatorItem extends CompassItem {
    
    private static final String SYNCED_ANOMALIES_TAG = "synced_anomalies";
    private static final String LAST_ANOMALY_TAG = "last_anomaly";
    
    public AnomalyResonatorItem() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        
        // Update target position for compass needle
        List<UUID> syncedAnomalies = getSyncedAnomalies(stack);
        BaseAnomalyEntity nearestAnomaly = findNearestUnsyncedAnomaly(level, player, stack);
        
        if (nearestAnomaly != null) {
            // Set the target position in NBT for the compass needle
            setTargetPosition(stack, nearestAnomaly.blockPosition());
            
            double distance = player.position().distanceTo(nearestAnomaly.position());
            player.sendSystemMessage(Component.literal("§eNearest unsynced anomaly: " + String.format("%.1f", distance) + " blocks away"));
        } else {
            // Clear target position if no anomalies found
            clearTargetPosition(stack);
            player.sendSystemMessage(Component.literal("§cNo unsynced anomalies detected."));
        }
        
        if (!syncedAnomalies.isEmpty()) {
            player.sendSystemMessage(Component.literal("§aSynced with " + syncedAnomalies.size() + " anomalies"));
        }
        
        return InteractionResultHolder.success(stack);
    }
    
    // This method will be called by the entity interaction event handler
    public void onInteractWithAnomaly(ItemStack stack, Player player, BaseAnomalyEntity anomaly) {
        if (player.level().isClientSide) {
            return;
        }
        
        // Check if this anomaly has already been synced
        List<UUID> syncedAnomalies = getSyncedAnomalies(stack);
        if (syncedAnomalies.contains(anomaly.getUUID())) {
            player.sendSystemMessage(Component.literal("§cThis anomaly has already been synced!"));
            return;
        }
        
        // Sync with this specific anomaly
        syncWithAnomaly(stack, anomaly);
        
        // Update target position to point to next nearest unsynced anomaly
        BaseAnomalyEntity nearestAnomaly = findNearestUnsyncedAnomaly(player.level(), player, stack);
        if (nearestAnomaly != null) {
            setTargetPosition(stack, nearestAnomaly.blockPosition());
        } else {
            clearTargetPosition(stack);
        }
        
        player.sendSystemMessage(Component.literal("§aResonator synced with this anomaly!"));
    }
    
    private BaseAnomalyEntity findNearestUnsyncedAnomaly(Level level, Player player, ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        
        Vec3 playerPos = player.position();
        List<UUID> syncedAnomalies = getSyncedAnomalies(stack);
        
        // Search in a large radius around the player
        AABB searchArea = new AABB(
            playerPos.x - 1000, playerPos.y - 100, playerPos.z - 1000,
            playerPos.x + 1000, playerPos.y + 100, playerPos.z + 1000
        );
        
        // Search for all types of anomalies
        List<BaseAnomalyEntity> anomalies = new java.util.ArrayList<>();
        anomalies.addAll(serverLevel.getEntitiesOfClass(GravityAnomalyEntity.class, searchArea));
        anomalies.addAll(serverLevel.getEntitiesOfClass(WarpGateAnomalyEntity.class, searchArea));
        anomalies.addAll(serverLevel.getEntitiesOfClass(com.hexvane.strangematter.entity.EnergeticRiftEntity.class, searchArea));
        
        BaseAnomalyEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (BaseAnomalyEntity anomaly : anomalies) {
            // Skip if already synced
            if (syncedAnomalies.contains(anomaly.getUUID())) {
                continue;
            }
            
            double distance = playerPos.distanceTo(anomaly.position());
            if (distance < nearestDistance) {
                nearest = anomaly;
                nearestDistance = distance;
            }
        }
        
        return nearest;
    }
    
    private void syncWithAnomaly(ItemStack stack, BaseAnomalyEntity anomaly) {
        CompoundTag tag = stack.getOrCreateTag();
        
        // Add this anomaly to the synced list
        List<UUID> syncedAnomalies = getSyncedAnomalies(stack);
        syncedAnomalies.add(anomaly.getUUID());
        setSyncedAnomalies(stack, syncedAnomalies);
        
        // Set as last synced anomaly
        tag.putUUID(LAST_ANOMALY_TAG, anomaly.getUUID());
    }
    
    private List<UUID> getSyncedAnomalies(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(SYNCED_ANOMALIES_TAG)) {
            return new java.util.ArrayList<>();
        }
        
        List<UUID> synced = new java.util.ArrayList<>();
        net.minecraft.nbt.ListTag listTag = tag.getList(SYNCED_ANOMALIES_TAG, net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < listTag.size(); i++) {
            try {
                synced.add(UUID.fromString(listTag.getString(i)));
            } catch (IllegalArgumentException e) {
                // Skip invalid UUIDs
            }
        }
        return synced;
    }
    
    private void setSyncedAnomalies(ItemStack stack, List<UUID> syncedAnomalies) {
        CompoundTag tag = stack.getOrCreateTag();
        net.minecraft.nbt.ListTag listTag = new net.minecraft.nbt.ListTag();
        for (UUID uuid : syncedAnomalies) {
            listTag.add(net.minecraft.nbt.StringTag.valueOf(uuid.toString()));
        }
        tag.put(SYNCED_ANOMALIES_TAG, listTag);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        tooltip.add(Component.literal("§7Points to the nearest unsynced anomaly"));
        tooltip.add(Component.literal("§7Right-click to check status"));
        tooltip.add(Component.literal("§7Right-click anomaly to sync with it"));
        tooltip.add(Component.literal("§7Needle spins when no anomalies found"));
        
        List<UUID> syncedAnomalies = getSyncedAnomalies(stack);
        if (!syncedAnomalies.isEmpty()) {
            tooltip.add(Component.literal("§aSynced with " + syncedAnomalies.size() + " anomalies"));
        }
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        // Make the resonator glow if it has synced anomalies
        return !getSyncedAnomalies(stack).isEmpty();
    }
    
    // Method to check if the compass should spin (no target found)
    public static boolean shouldSpin(ItemStack stack, Level level, Player player) {
        return getTargetPosition(stack, level, player) == null;
    }
    
    // Compass functionality - these methods are called by the compass template
    public static float getAngle(ItemStack stack, Level level, Player player) {
        if (level == null || player == null) {
            return 0.0f;
        }
        
        // Check if the compass should spin (no target found)
        if (shouldSpin(stack, level, player)) {
            // Spin the needle when no target is found
            return (float) (System.currentTimeMillis() * 0.1) % 360.0f;
        }
        
        // Get the target position
        BlockPos targetPos = getTargetPosition(stack, level, player);
        if (targetPos == null) {
            return 0.0f;
        }
        
        // Calculate the angle to the target
        double dx = targetPos.getX() - player.getX();
        double dz = targetPos.getZ() - player.getZ();
        
        // Calculate angle in degrees
        double angle = Math.atan2(dz, dx) * 180.0 / Math.PI;
        
        // Adjust for Minecraft's coordinate system (north is 0 degrees)
        angle = angle - 90.0;
        
        // Normalize to 0-360 range
        while (angle < 0) angle += 360;
        while (angle >= 360) angle -= 360;
        
        return (float) angle;
    }
    
    // Method to get the target position for the compass needle
    public static BlockPos getTargetPosition(ItemStack stack, Level level, Player player) {
        // Check if we have a cached target position in NBT
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("target_pos")) {
            CompoundTag posTag = tag.getCompound("target_pos");
            int x = posTag.getInt("x");
            int y = posTag.getInt("y");
            int z = posTag.getInt("z");
            return new BlockPos(x, y, z);
        }
        
        // If no cached position, return null (will cause spinning)
        return null;
    }
    
    // Method to set the target position in NBT (called from server side)
    public static void setTargetPosition(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", pos.getX());
        posTag.putInt("y", pos.getY());
        posTag.putInt("z", pos.getZ());
        tag.put("target_pos", posTag);
    }
    
    // Method to clear the target position
    public static void clearTargetPosition(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove("target_pos");
        }
    }
    
    
}
