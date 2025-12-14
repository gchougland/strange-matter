package com.hexvane.strangematter.block;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.TimeDilationData;
import com.hexvane.strangematter.network.NetworkHandler;
import com.hexvane.strangematter.network.TimeDilationSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.HashSet;
import java.util.Set;

public class TimeDilationBlockEntity extends BlockEntity {
    
    private static final double SLOWDOWN_FACTOR = 0.3; // Slow down to 30% of normal speed (like molasses)
    private static final int MIN_DECAY_TICKS = 200; // 10 seconds at 20 ticks/second
    private static final int MAX_DECAY_TICKS = 600; // 30 seconds at 20 ticks/second
    
    private int ageTicks = 0; // Track age for decay
    private int decayTimeTicks = -1; // Random decay time, -1 means not initialized
    
    // Track affected players to clean up when they leave
    private Set<Player> affectedPlayers = new HashSet<>();
    
    public TimeDilationBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.TIME_DILATION_BLOCK_ENTITY.get(), pos, state);
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, TimeDilationBlockEntity blockEntity) {
        blockEntity.tick();
    }
    
    private void tick() {
        Level level = this.level;
        if (level == null || level.isClientSide) return;
        
        // Initialize random decay time on first tick if not set
        if (decayTimeTicks < 0) {
            decayTimeTicks = MIN_DECAY_TICKS + level.getRandom().nextInt(MAX_DECAY_TICKS - MIN_DECAY_TICKS + 1);
        }
        
        // Increment age and check for decay
        ageTicks++;
        if (ageTicks >= decayTimeTicks) {
            // Clean up all affected players before decaying
            cleanupAllAffectedPlayers();
            level.setBlock(worldPosition, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        
        // Create AABB for this block - slightly expanded to catch entities at edges
        AABB blockAABB = new AABB(
            worldPosition.getX() - 0.25,
            worldPosition.getY() - 0.25,
            worldPosition.getZ() - 0.25,
            worldPosition.getX() + 1.25,
            worldPosition.getY() + 1.25,
            worldPosition.getZ() + 1.25
        );
        
        // Get all entities - use getEntities to catch ALL entity types including arrows, players, etc.
        var entities = level.getEntities((Entity) null, blockAABB, entity -> {
            // Skip the projectile that spawned this block if it's still around
            if (entity instanceof com.hexvane.strangematter.entity.ChronoBlisterProjectileEntity) {
                return false;
            }
            
            // Check if entity's bounding box actually intersects with the block's space
            AABB entityAABB = entity.getBoundingBox();
            AABB exactBlockAABB = new AABB(
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ(),
                worldPosition.getX() + 1.0,
                worldPosition.getY() + 1.0,
                worldPosition.getZ() + 1.0
            );
            return entityAABB.intersects(exactBlockAABB);
        });
        
        // Track currently affected players
        Set<Player> currentAffectedPlayers = new HashSet<>();
        
        // Apply uniform slowdown to all entities
        for (Entity entity : entities) {
            Vec3 currentVelocity = entity.getDeltaMovement();
            
            // Handle players specifically - use the same system as gravity anomaly
            if (entity instanceof Player player) {
                if (!affectedPlayers.contains(player)) {
                    affectedPlayers.add(player);
                }
                currentAffectedPlayers.add(player);
                
                // Store slowdown factor for event-based modification (similar to gravity anomaly)
                TimeDilationData.setPlayerSlowdownFactor(player.getUUID(), SLOWDOWN_FACTOR);
                player.getPersistentData().putDouble("strangematter.time_dilation_factor", SLOWDOWN_FACTOR);
                
                // Send packet to client for synchronization
                if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    NetworkHandler.INSTANCE.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new TimeDilationSyncPacket(SLOWDOWN_FACTOR)
                    );
                }
                
                // Apply slowdown on server side
                Vec3 slowedVelocity = currentVelocity.scale(SLOWDOWN_FACTOR);
                player.setDeltaMovement(slowedVelocity);
            } else {
                // For all other entities (mobs, etc.), apply uniform slowdown
                Vec3 slowedVelocity = currentVelocity.scale(SLOWDOWN_FACTOR);
                entity.setDeltaMovement(slowedVelocity);
            }
        }
        
        // Clean up players who are no longer in range
        Set<Player> playersToRemove = new HashSet<>(affectedPlayers);
        playersToRemove.removeAll(currentAffectedPlayers);
        
        for (Player player : playersToRemove) {
            cleanupPlayerEffect(player);
        }
        
        affectedPlayers = currentAffectedPlayers;
        
        // Also check all currently affected players to make sure they're still actually in a time dilation block
        // This handles cases where blocks decayed but players weren't cleaned up
        for (Player player : new HashSet<>(affectedPlayers)) {
            BlockPos playerBlockPos = player.blockPosition();
            BlockPos playerFeetPos = player.blockPosition().below();
            boolean isInTimeDilation = level.getBlockState(playerBlockPos).getBlock() == StrangeMatterMod.TIME_DILATION_BLOCK.get() ||
                                      level.getBlockState(playerFeetPos).getBlock() == StrangeMatterMod.TIME_DILATION_BLOCK.get();
            
            if (!isInTimeDilation) {
                cleanupPlayerEffect(player);
                affectedPlayers.remove(player);
            }
        }
        
        // Arrows and items are not handled - only players and mobs are affected
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Age", ageTicks);
        tag.putInt("DecayTime", decayTimeTicks);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ageTicks = tag.getInt("Age");
        decayTimeTicks = tag.getInt("DecayTime");
        // If decayTime wasn't saved (old blocks), initialize it
        if (decayTimeTicks < 0) {
            decayTimeTicks = -1; // Will be initialized on next tick
        }
    }
    
    private void cleanupPlayerEffect(Player player) {
        if (player == null || !player.isAlive() || level == null) return;
        
        // Before cleaning up, check if player is still in ANY time dilation block
        // This prevents cleaning up when player moves from one block to another
        BlockPos playerBlockPos = player.blockPosition();
        BlockPos playerFeetPos = player.blockPosition().below();
        boolean isInAnyTimeDilation = level.getBlockState(playerBlockPos).getBlock() == StrangeMatterMod.TIME_DILATION_BLOCK.get() ||
                                      level.getBlockState(playerFeetPos).getBlock() == StrangeMatterMod.TIME_DILATION_BLOCK.get();
        
        // Only clean up if player is not in any time dilation block
        if (!isInAnyTimeDilation) {
            TimeDilationData.removePlayerSlowdownFactor(player.getUUID());
            player.getPersistentData().remove("strangematter.time_dilation_factor");
            
            // Send packet to clear slowdown on client
            if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                NetworkHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new TimeDilationSyncPacket(1.0) // Send 1.0 to clear the effect (normal speed)
                );
            }
        }
    }
    
    public void cleanupAllAffectedPlayers() {
        if (level == null) return;
        
        // Clean up all players that were affected by this block
        // But check if they're still in any time dilation block first
        for (Player player : new HashSet<>(affectedPlayers)) {
            cleanupPlayerEffect(player);
        }
        affectedPlayers.clear();
    }
}

