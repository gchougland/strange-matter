package com.hexvane.strangematter.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import com.hexvane.strangematter.entity.BaseAnomalyEntity;
import com.hexvane.strangematter.entity.GravityAnomalyEntity;
import com.hexvane.strangematter.entity.EnergeticRiftEntity;
import com.hexvane.strangematter.entity.EchoingShadowEntity;
import com.hexvane.strangematter.entity.TemporalBloomEntity;
import com.hexvane.strangematter.entity.ThoughtwellEntity;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import com.hexvane.strangematter.network.EchoVacuumBeamPacket;
import java.util.List;

public class EchoVacuumItem extends Item {
    
    private static final double BEAM_RANGE = 8.0;
    private static final double BEAM_WIDTH = 2.0;
    private static final int SUCTION_TIME = 40; // ticks to fully suck anomaly
    private static final double SUCTION_SPEED = 0.1;
    
    // Track anomaly states for proper return behavior
    private static final java.util.Map<BaseAnomalyEntity, Vec3> originalPositions = new java.util.HashMap<>();
    private static final java.util.Map<BaseAnomalyEntity, Float> originalScales = new java.util.HashMap<>();
    
    // Track which players are currently using the beam for packet sending
    private static final java.util.Set<Player> playersUsingBeam = new java.util.HashSet<>();
    
    // Track which anomalies are currently being targeted to prevent conflicts
    private static final java.util.Map<BaseAnomalyEntity, Player> targetedAnomalies = new java.util.HashMap<>();
    
    // Track which players have the fire loop sound playing
    private static final java.util.Set<Player> playersWithFireLoop = new java.util.HashSet<>();
    
    public EchoVacuumItem() {
        super(new Item.Properties().stacksTo(1).durability(200));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // Check if player has empty containment capsule
        if (!hasEmptyCapsule(player)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("item.strangematter.echo_vacuum.no_capsule"), true);
            return InteractionResultHolder.fail(stack);
        }
        
        // Play charge up sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
            com.hexvane.strangematter.sound.StrangeMatterSounds.ECHO_VACUUM_CHARGE_UP.get(), 
            SoundSource.PLAYERS, 0.7f, 1.0f);
        
        // Client-side rendering will handle the beam effects
        
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
    
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW; // Use bow animation for charging effect
    }
    
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // 1 hour duration (effectively infinite while held)
    }
    
    
    private static void returnAnomaliesToOriginalState(Level level) {
        // Return all tracked anomalies to their original positions and scales
        java.util.Iterator<java.util.Map.Entry<BaseAnomalyEntity, Vec3>> posIter = originalPositions.entrySet().iterator();
        while (posIter.hasNext()) {
            java.util.Map.Entry<BaseAnomalyEntity, Vec3> entry = posIter.next();
            BaseAnomalyEntity anomaly = entry.getKey();
            
            if (anomaly.isRemoved() || !anomaly.level().equals(level)) {
                // Anomaly was removed or is in different level, clean up tracking
                posIter.remove();
                originalScales.remove(anomaly);
                continue;
            }
            
            Vec3 originalPos = entry.getValue();
            Float originalScale = originalScales.get(anomaly);
            
            if (originalScale != null) {
                // Return to original position
                Vec3 currentPos = anomaly.position();
                Vec3 direction = originalPos.subtract(currentPos).normalize();
                double distance = currentPos.distanceTo(originalPos);
                
                if (distance > 0.1) {
                    // Move towards original position
                    Vec3 newPos = currentPos.add(direction.scale(SUCTION_SPEED));
                    anomaly.setPos(newPos.x, newPos.y, newPos.z);
                    
                    // Return to original scale
                    anomaly.setScale(originalScale);
                } else {
                    // Close enough, remove from tracking
                    posIter.remove();
                    originalScales.remove(anomaly);
                }
            }
        }
    }
    
    /**
     * Called from EchoVacuumEventHandler on server side
     */
    public static void handlePlayerTick(Player player, Level level) {
        boolean currentlyUsingBeam = player.isUsingItem() && player.getUseItem().getItem() instanceof EchoVacuumItem;
        boolean wasUsingBeam = playersUsingBeam.contains(player);
        
        if (currentlyUsingBeam) {
            if (!wasUsingBeam) {
                // Player just started using beam
                playersUsingBeam.add(player);
                EchoVacuumBeamPacket.sendToNearbyPlayers(player, true);
                
                // Play charge up sound on server (will replicate to clients)
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    com.hexvane.strangematter.sound.StrangeMatterSounds.ECHO_VACUUM_CHARGE_UP.get(), 
                    SoundSource.PLAYERS, 0.7f, 1.0f);
            }
            handleVacuumBeam(player, level);
        } else {
            if (wasUsingBeam) {
                // Player just stopped using beam
                playersUsingBeam.remove(player);
                EchoVacuumBeamPacket.sendToNearbyPlayers(player, false);
                
                // Play charge down sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    com.hexvane.strangematter.sound.StrangeMatterSounds.ECHO_VACUUM_CHARGE_DOWN.get(), 
                    SoundSource.PLAYERS, 0.6f, 1.0f);
                
                // Clean up any anomalies this player was targeting
                cleanupPlayerTargeting(player);
            }
        }
    }
    
    private boolean hasEmptyCapsule(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ContainmentCapsuleItem && 
                !((ContainmentCapsuleItem) stack.getItem()).hasAnomaly()) {
                return true;
            }
        }
        return false;
    }
    
    private void startVacuumBeam(Player player, Level level) {
        // Start using the item
        player.startUsingItem(player.getUsedItemHand());
        
        // Play chargeup sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
            com.hexvane.strangematter.sound.StrangeMatterSounds.ECHO_VACUUM_CHARGE_UP.get(), 
            SoundSource.PLAYERS, 0.7f, 1.0f);
    }
    
    private static void handleVacuumBeam(Player player, Level level) {
        Vec3 startPos = player.getEyePosition();
        Vec3 lookDirection = player.getLookAngle();
        Vec3 endPos = startPos.add(lookDirection.scale(BEAM_RANGE));
        
        // Create beam AABB with better collision detection
        AABB beamBox = new AABB(startPos, endPos).inflate(BEAM_WIDTH);
        
        // Find anomalies in beam path
        List<Entity> entities = level.getEntities(player, beamBox);
        BaseAnomalyEntity targetAnomaly = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (Entity entity : entities) {
            if (entity instanceof BaseAnomalyEntity anomaly && !anomaly.isContained()) {
                // Check if anomaly is already being targeted by someone else
                Player currentTargeter = targetedAnomalies.get(anomaly);
                if (currentTargeter != null && currentTargeter != player) {
                    continue; // Skip this anomaly, it's already being targeted
                }
                
                // Check if anomaly is actually in the beam path (not just in AABB)
                double distance = entity.distanceTo(player);
                Vec3 entityPos = entity.position();
                Vec3 toEntity = entityPos.subtract(startPos);
                double dotProduct = toEntity.normalize().dot(lookDirection);
                
                // Only target anomalies that are roughly in front of the player
                // Make targeting more forgiving in multiplayer (0.5 instead of 0.7)
                if (dotProduct > 0.5 && distance < closestDistance && distance < BEAM_RANGE) {
                    targetAnomaly = anomaly;
                    closestDistance = distance;
                }
            }
        }
        
        if (targetAnomaly != null) {
            // Mark this anomaly as being targeted by this player
            targetedAnomalies.put(targetAnomaly, player);
            // Suck the anomaly towards the player
            suckAnomaly(player, targetAnomaly, level);
        }
        
        // Update fire loop sound position if it's playing
        if (playersWithFireLoop.contains(player)) {
            com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().updateSoundPosition(
                com.hexvane.strangematter.sound.StrangeMatterSounds.ECHO_VACUUM_FIRE_LOOP.get().getLocation(),
                player.getX(), player.getY(), player.getZ()
            );
        }
        
        // Vacuum cone particles are now rendered client-side in EchoVacuumBeamRenderer
    }
    
    private static void suckAnomaly(Player player, BaseAnomalyEntity anomaly, Level level) {
        Vec3 playerPos = player.getEyePosition();
        Vec3 anomalyPos = anomaly.position();
        Vec3 direction = playerPos.subtract(anomalyPos).normalize();
        
        // Store original position and scale if not already stored
        if (!originalPositions.containsKey(anomaly)) {
            originalPositions.put(anomaly, anomalyPos);
            originalScales.put(anomaly, anomaly.getScale());
        }
        
        // Move anomaly towards player
        Vec3 newPos = anomalyPos.add(direction.scale(SUCTION_SPEED));
        anomaly.setPos(newPos.x, newPos.y, newPos.z);
        
        // Shrink the anomaly as it gets closer
        double distance = anomaly.distanceTo(player);
        double shrinkFactor = Math.max(0.1, distance / BEAM_RANGE);
        anomaly.setScale((float) shrinkFactor);
        
        // Extraction particles are now rendered client-side in EchoVacuumBeamRenderer
        
        // If close enough, contain the anomaly
        if (distance < 2.0) {
            containAnomaly(player, anomaly, level);
        }
    }
    
    private static void containAnomaly(Player player, BaseAnomalyEntity anomaly, Level level) {
        // Clean up targeting
        targetedAnomalies.remove(anomaly);
        
        // Find empty capsule in inventory
        ItemStack emptyCapsule = null;
        int capsuleSlot = -1;
        
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ContainmentCapsuleItem && 
                !((ContainmentCapsuleItem) stack.getItem()).hasAnomaly()) {
                emptyCapsule = stack;
                capsuleSlot = i;
                break;
            }
        }
        
        if (emptyCapsule != null) {
            // Create filled capsule
            ItemStack filledCapsule = createFilledCapsule(anomaly);
            
            // Replace empty capsule with filled one
            if (emptyCapsule.getCount() > 1) {
                emptyCapsule.shrink(1);
                player.getInventory().add(filledCapsule);
            } else {
                player.getInventory().setItem(capsuleSlot, filledCapsule);
            }
            
            // Remove anomaly
            anomaly.remove(Entity.RemovalReason.DISCARDED);
            
            // Play containment sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                com.hexvane.strangematter.sound.StrangeMatterSounds.ECHO_VACUUM_CONTAIN.get(), 
                SoundSource.PLAYERS, 1.0f, 1.0f);
            
            // Stop fire loop sound and play charge down
            if (playersWithFireLoop.contains(player)) {
                playersWithFireLoop.remove(player);
                // Stop the looping fire sound
                com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().stopAmbientSound(
                    com.hexvane.strangematter.sound.StrangeMatterSounds.ECHO_VACUUM_FIRE_LOOP.get().getLocation()
                );
                // Play charge down sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    com.hexvane.strangematter.sound.StrangeMatterSounds.ECHO_VACUUM_CHARGE_DOWN.get(), 
                    SoundSource.PLAYERS, 0.6f, 1.0f);
            }
            
            // Stop using the item
            player.stopUsingItem();
        }
    }
    
    private static ItemStack createFilledCapsule(BaseAnomalyEntity anomaly) {
        if (anomaly instanceof GravityAnomalyEntity) {
            return new ItemStack(ContainmentCapsuleItem.GRAVITY_CAPSULE.get());
        } else if (anomaly instanceof EnergeticRiftEntity) {
            return new ItemStack(ContainmentCapsuleItem.ENERGETIC_CAPSULE.get());
        } else if (anomaly instanceof EchoingShadowEntity) {
            return new ItemStack(ContainmentCapsuleItem.ECHOING_SHADOW_CAPSULE.get());
        } else if (anomaly instanceof TemporalBloomEntity) {
            return new ItemStack(ContainmentCapsuleItem.TEMPORAL_BLOOM_CAPSULE.get());
        } else if (anomaly instanceof ThoughtwellEntity) {
            return new ItemStack(ContainmentCapsuleItem.THOUGHTWELL_CAPSULE.get());
        } else if (anomaly instanceof WarpGateAnomalyEntity) {
            return new ItemStack(ContainmentCapsuleItem.WARP_GATE_CAPSULE.get());
        }
        
        return new ItemStack(ContainmentCapsuleItem.EMPTY_CAPSULE.get());
    }
    
    private static void cleanupPlayerTargeting(Player player) {
        // Remove this player from any anomalies they were targeting
        targetedAnomalies.entrySet().removeIf(entry -> entry.getValue() == player);
        
        // Clean up fire loop tracking and stop any playing sound
        if (playersWithFireLoop.contains(player)) {
            playersWithFireLoop.remove(player);
            // Stop the looping fire sound
            com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().stopAmbientSound(
                com.hexvane.strangematter.sound.StrangeMatterSounds.ECHO_VACUUM_FIRE_LOOP.get().getLocation()
            );
        }
        
        // Return any anomalies this player was targeting to their original state
        for (BaseAnomalyEntity anomaly : originalPositions.keySet()) {
            Player targeter = targetedAnomalies.get(anomaly);
            if (targeter == player) {
                Vec3 originalPos = originalPositions.get(anomaly);
                Float originalScale = originalScales.get(anomaly);
                
                if (originalPos != null && originalScale != null) {
                    anomaly.setPos(originalPos.x, originalPos.y, originalPos.z);
                    anomaly.setScale(originalScale);
                }
                
                // Clean up tracking
                originalPositions.remove(anomaly);
                originalScales.remove(anomaly);
                targetedAnomalies.remove(anomaly);
            }
        }
    }
    
}
