package com.hexvane.strangematter.block;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.Config;
import com.hexvane.strangematter.GravityData;
import com.hexvane.strangematter.network.NetworkHandler;
import com.hexvane.strangematter.network.GravitySyncPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LevitationPadBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(LevitationPadBlockEntity.class);
    
    private static final int MAX_RANGE = 16;
    private static final double LEVITATION_SPEED = 0.2; // Increased for better player levitation
    private static final double LEVITATION_SPEED_DOWN = 0.05; // Gentle downward movement
    private static final int TICK_INTERVAL = 2; // Process every 2 ticks for performance
    
    private boolean levitateUp = true;
    private int tickCounter = 0;
    private Set<ItemEntity> affectedItems = new HashSet<>();
    
    // Track affected players to clean up gravity data when they leave
    private Set<Player> affectedPlayers = new HashSet<>();
    
    public LevitationPadBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.LEVITATION_PAD_BLOCK_ENTITY.get(), pos, state);
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, LevitationPadBlockEntity blockEntity) {
        blockEntity.tick();
    }
    
    private void tick() {
        if (level == null || level.isClientSide) return;
        
        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;
        
        if (level.getBlockState(worldPosition).getBlock() instanceof LevitationPadBlock) {
            LevitationPadBlock block = (LevitationPadBlock) level.getBlockState(worldPosition).getBlock();
            boolean levitateUp = level.getBlockState(worldPosition).getValue(LevitationPadBlock.LEVITATE_UP);
            
            if (levitateUp) {
                processLevitationUp();
            } else {
                processLevitationDown();
            }
        }
    }
    
    private void processLevitationUp() {
        if (level == null) return;
        
        // Calculate the range based on block collision detection
        int maxHeight = findMaxHeight();
        
        // Create AABB for entity detection (12x12 area centered on block)
        AABB detectionBox = new AABB(
            worldPosition.getX() - 0.5, worldPosition.getY() + 1, worldPosition.getZ() - 0.5,
            worldPosition.getX() + 1.5, worldPosition.getY() + maxHeight, worldPosition.getZ() + 1.5
        );
        
        // Get all entities in the detection area
        var entities = level.getEntitiesOfClass(Entity.class, detectionBox);
        
        // Track which items and players are currently being affected
        Set<ItemEntity> currentAffectedItems = new HashSet<>();
        Set<Player> currentAffectedPlayers = new HashSet<>();
        
        for (Entity entity : entities) {
            if (shouldAffectEntity(entity)) {
                levitateEntityUp(entity, maxHeight);
                if (entity instanceof ItemEntity itemEntity) {
                    currentAffectedItems.add(itemEntity);
                } else if (entity instanceof Player player) {
                    currentAffectedPlayers.add(player);
                }
            }
        }
        
        // Restore gravity for items that are no longer being affected
        affectedItems.removeAll(currentAffectedItems);
        for (ItemEntity item : affectedItems) {
            if (item.isAlive()) {
                item.setNoGravity(false);
            }
        }
        
        // Clean up gravity data for players that are no longer being affected
        affectedPlayers.removeAll(currentAffectedPlayers);
        for (Player player : affectedPlayers) {
            if (player.isAlive()) {
                GravityData.removePlayerGravityForce(player.getUUID());
                player.getPersistentData().remove("strangematter.gravity_force");
                
                // Send packet to clear gravity force on client
                if (!level.isClientSide) {
                    NetworkHandler.INSTANCE.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) player), 
                        new GravitySyncPacket(0.0) // Send 0 to clear the effect
                    );
                }
            }
        }
        
        // Update the sets of affected entities
        affectedItems = currentAffectedItems;
        affectedPlayers = currentAffectedPlayers;
    }
    
    private void processLevitationDown() {
        if (level == null) return;
        
        // Create AABB for entity detection (12x12 area centered on block, extending up to max range)
        AABB detectionBox = new AABB(
            worldPosition.getX() - 0.5, worldPosition.getY() + 1, worldPosition.getZ() - 0.5,
            worldPosition.getX() + 1.5, worldPosition.getY() + MAX_RANGE, worldPosition.getZ() + 1.5
        );
        
        // Get all entities in the detection area
        var entities = level.getEntitiesOfClass(Entity.class, detectionBox);
        
        // Track which items and players are currently being affected
        Set<ItemEntity> currentAffectedItems = new HashSet<>();
        Set<Player> currentAffectedPlayers = new HashSet<>();
        
        for (Entity entity : entities) {
            if (shouldAffectEntity(entity)) {
                levitateEntityDown(entity);
                if (entity instanceof ItemEntity itemEntity) {
                    currentAffectedItems.add(itemEntity);
                } else if (entity instanceof Player player) {
                    currentAffectedPlayers.add(player);
                }
            }
        }
        
        // Restore gravity for items that are no longer being affected
        affectedItems.removeAll(currentAffectedItems);
        for (ItemEntity item : affectedItems) {
            if (item.isAlive()) {
                item.setNoGravity(false);
            }
        }
        
        // Clean up gravity data for players that are no longer being affected
        affectedPlayers.removeAll(currentAffectedPlayers);
        for (Player player : affectedPlayers) {
            if (player.isAlive()) {
                GravityData.removePlayerGravityForce(player.getUUID());
                player.getPersistentData().remove("strangematter.gravity_force");
                
                // Send packet to clear gravity force on client
                if (!level.isClientSide) {
                    NetworkHandler.INSTANCE.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) player), 
                        new GravitySyncPacket(0.0) // Send 0 to clear the effect
                    );
                }
            }
        }
        
        // Update the sets of affected entities
        affectedItems = currentAffectedItems;
        affectedPlayers = currentAffectedPlayers;
    }
    
    private int findMaxHeight() {
        if (level == null) return Config.levitationPadMaxHeight;
        
        BlockPos currentPos = worldPosition.above();
        int height = 0;
        int maxRange = Config.levitationPadMaxHeight;
        
        // Check up to maxRange blocks or until we hit a blocking block
        while (height < maxRange) {
            // Get the block state and check if it's actually a block
            BlockState blockState = level.getBlockState(currentPos);
            
            // Check if this position has air or void - always allow through
            if (blockState.getBlock() == net.minecraft.world.level.block.Blocks.AIR ||
                blockState.getBlock() == net.minecraft.world.level.block.Blocks.CAVE_AIR ||
                blockState.getBlock() == net.minecraft.world.level.block.Blocks.VOID_AIR ||
                blockState.isAir()) {
                height++;
                currentPos = currentPos.above();
                continue;
            }
            
            // Check if it's a trapdoor - allow through if open, block if closed
            if (blockState.getBlock() instanceof net.minecraft.world.level.block.TrapDoorBlock) {
                if (blockState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN)) {
                    // Open trapdoor - allow beam to pass through
                    height++;
                    currentPos = currentPos.above();
                    continue;
                } else {
                    // Closed trapdoor - block the beam
                    break;
                }
            }
            
            // Any other block - block the beam
            break;
        }
        
        return height;
    }
    
    private boolean shouldAffectEntity(Entity entity) {
        // Don't affect block entities (this shouldn't happen but just in case)
        if (entity.getClass().getSimpleName().contains("BlockEntity")) return false;
        
        // Don't affect players in creative mode unless they want to be affected
        if (entity instanceof Player player) {
            return !player.getAbilities().flying; // Don't affect flying players
        }
        
        // Affect all other entities (mobs, items, etc.)
        return true;
    }
    
    private void levitateEntityUp(Entity entity, int maxHeight) {
        if (level == null) return;

        double entityY = entity.getY();
        double targetY = worldPosition.getY() + maxHeight + 1;

        // If entity is already at or above target, don't move it
        if (entityY >= targetY) return;

        // Calculate movement
        double distanceToTarget = targetY - entityY;
        double moveDistance = Math.min(LEVITATION_SPEED, distanceToTarget);

        // Apply levitation based on entity type
        if (entity instanceof Player player) {
            // Use the same system as Gravity Anomaly - store gravity force data
            double forceMultiplier = Math.min(1.0, moveDistance / LEVITATION_SPEED);
            GravityData.setPlayerGravityForce(player.getUUID(), forceMultiplier);
            player.getPersistentData().putDouble("strangematter.gravity_force", forceMultiplier);
            
            // Send packet to client for synchronization
            if (!level.isClientSide) {
                NetworkHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) player), 
                    new GravitySyncPacket(forceMultiplier)
                );
            }
            
        } else if (entity instanceof ItemEntity itemEntity) {
            // For items, remove all momentum and center them in the beam with gentle floating
            // Calculate center of the beam (block center)
            double centerX = worldPosition.getX() + 0.5;
            double centerZ = worldPosition.getZ() + 0.5;
            
            // Calculate distance from center
            double deltaX = centerX - entity.getX();
            double deltaZ = centerZ - entity.getZ();
            
            // Apply centering force (stronger when further from center)
            double centeringForce = 0.2; // Increased for better centering
            double newX = deltaX * centeringForce; // Remove all horizontal momentum
            double newZ = deltaZ * centeringForce; // Remove all horizontal momentum
            
            // Add gentle floating motion and upward force
            double time = (System.currentTimeMillis() % 360000) / 1000.0;
            double itemId = itemEntity.getId() * 0.1; // Unique offset per item
            double floatHeight = Math.sin(time * 0.5 + itemId) * 0.02; // Gentle floating
            
            // Apply the new motion (no previous momentum, just centering + floating + upward)
            itemEntity.setDeltaMovement(newX, moveDistance + floatHeight, newZ);
            itemEntity.setNoGravity(true);
        } else if (entity instanceof LivingEntity livingEntity) {
            // For other living entities (mobs)
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, moveDistance, motion.z);
            livingEntity.fallDistance = 0;
        } else {
            // For other entities
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, moveDistance, motion.z);
        }
    }
    
    private void levitateEntityDown(Entity entity) {
        if (level == null) return;
        
        double entityY = entity.getY();
        double targetY = worldPosition.getY() + 1;
        
        // If entity is already at or below target, don't move it
        if (entityY <= targetY) return;
        
        // Calculate movement
        double distanceToTarget = entityY - targetY;
        double moveDistance = Math.min(LEVITATION_SPEED_DOWN, distanceToTarget);
        
        // Apply downward levitation based on entity type
        if (entity instanceof Player player) {
            // Use the same system as Gravity Anomaly - store gravity force data for downward movement
            double forceMultiplier = Math.min(1.0, moveDistance / LEVITATION_SPEED_DOWN);
            // Store negative force for downward movement
            GravityData.setPlayerGravityForce(player.getUUID(), -forceMultiplier);
            player.getPersistentData().putDouble("strangematter.gravity_force", -forceMultiplier);
            
            // Send packet to client for synchronization
            if (!level.isClientSide) {
                NetworkHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) player), 
                    new GravitySyncPacket(-forceMultiplier)
                );
            }
        } else if (entity instanceof ItemEntity itemEntity) {
            // For items, remove all momentum and center them in the beam while lowering
            // Calculate center of the beam (block center)
            double centerX = worldPosition.getX() + 0.5;
            double centerZ = worldPosition.getZ() + 0.5;
            
            // Calculate distance from center
            double deltaX = centerX - entity.getX();
            double deltaZ = centerZ - entity.getZ();
            
            // Apply centering force (stronger when further from center)
            double centeringForce = 0.2; // Increased for better centering
            double newX = deltaX * centeringForce; // Remove all horizontal momentum
            double newZ = deltaZ * centeringForce; // Remove all horizontal momentum
            
            // Add gentle floating motion while lowering
            double time = (System.currentTimeMillis() % 360000) / 1000.0;
            double itemId = itemEntity.getId() * 0.1; // Unique offset per item
            double floatHeight = Math.sin(time * 0.5 + itemId) * 0.01; // Gentle floating (smaller for downward)
            
            // Apply the new motion (no previous momentum, just centering + floating + downward)
            itemEntity.setDeltaMovement(newX, -moveDistance + floatHeight, newZ);
            itemEntity.setNoGravity(true);
        } else if (entity instanceof LivingEntity livingEntity) {
            // For other living entities (mobs)
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, -moveDistance, motion.z);
            livingEntity.fallDistance = 0;
        } else {
            // For other entities
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, -moveDistance, motion.z);
        }
    }
    
    
    public void setLevitateUp(boolean levitateUp) {
        this.levitateUp = levitateUp;
        setChanged();
    }
    
    public boolean isLevitateUp() {
        return levitateUp;
    }
    
    public int getMaxHeight() {
        return findMaxHeight();
    }
    
    @Override
    public AABB getRenderBoundingBox() {
        // Extend the render bounding box to include the full beam height
        // This prevents frustum culling from hiding the beam when the block is out of view
        int maxHeight = findMaxHeight();
        return new AABB(
            worldPosition.getX() - 0.5, worldPosition.getY(), worldPosition.getZ() - 0.5,
            worldPosition.getX() + 1.5, worldPosition.getY() + maxHeight, worldPosition.getZ() + 1.5
        );
    }
    
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("levitate_up", levitateUp);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        levitateUp = tag.getBoolean("levitate_up");
    }
    
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("levitate_up", levitateUp);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        levitateUp = tag.getBoolean("levitate_up");
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
