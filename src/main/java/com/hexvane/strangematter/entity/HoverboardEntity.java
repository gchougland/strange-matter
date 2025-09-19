package com.hexvane.strangematter.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.MoverType;

public class HoverboardEntity extends Entity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Float> BOARD_ROTATION = SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.FLOAT);
    
    // Add movement constants
    private static final float ACCELERATION = 0.02f;
    private static final float MAX_SPEED = 0.3f;
    private static final float FRICTION = 0.9f;
    
    public HoverboardEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        System.out.println("HoverboardEntity constructor called!");
        // Remove this line - size should be set in registration
        // this.setBoundingBox(this.getBoundingBox().inflate(0.0, -0.4, 0.0));
    }
    
    // ========== REQUIRED METHODS (CANNOT BE REMOVED) ==========
    // These are abstract in Entity class and must be implemented
    
    @Override
    protected void defineSynchedData() {
        // REQUIRED: Abstract method in Entity - defines what data syncs between client/server
        this.entityData.define(BOARD_ROTATION, 0.0f);
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        // REQUIRED: Abstract method in Entity - saves entity data to NBT
        compound.putFloat("BoardRotation", this.entityData.get(BOARD_ROTATION));
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        // REQUIRED: Abstract method in Entity - loads entity data from NBT
        if (compound.contains("BoardRotation")) {
            this.entityData.set(BOARD_ROTATION, compound.getFloat("BoardRotation"));
        }
    }
    
    // ========== OPTIONAL OVERRIDES (FOR FUNCTIONALITY) ==========
    // These have default implementations but we override for specific behavior
    
    @Override
    public boolean isPickable() {
        // OPTIONAL: Override to allow player interaction with the hoverboard
        // Default: depends on entity type, usually true for most entities
        return true;
    }
    
    @Override
    public boolean isPushable() {
        // OPTIONAL: Override to prevent other entities from pushing the hoverboard around
        // Default: true for most entities
        return false;
    }
    
    @Override
    public void tick() {
        super.tick(); // Always call super for basic entity functionality
        if (this.level().isClientSide) return;
        
        // Add basic debug to see if tick is running
        if (this.tickCount % 60 == 0) { // Every 3 seconds
            System.out.println("HoverboardEntity tick - isVehicle: " + this.isVehicle() + ", passengers: " + this.getPassengers().size());
        }
            
        // Handle dismount when player sneaks
        handleDismount();
            
        // Handle movement input - uncomment this!
        handleMovement();
    }
    
    /**
     * Handle player interaction (right-click to mount or remove)
     */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        // If player is sneaking, remove the hoverboard
        if (player.isShiftKeyDown()) {
            System.out.println("Removing hoverboard");
            this.discard(); // Remove the entity
            return InteractionResult.SUCCESS;
        }
        
        // Check if player is already riding something
        if (player.isPassenger()) {
            return InteractionResult.PASS;
        }
        
        // Check if hoverboard already has a rider
        if (this.isVehicle()) {
            return InteractionResult.PASS;
        }
        
        // Mount the player on the hoverboard
        boolean success = player.startRiding(this);
        System.out.println("startRiding result: " + success);
        
        return InteractionResult.SUCCESS;
    }
    
    /**
     * Handle dismount when player sneaks
     */
    private void handleDismount() {
        if (!this.isVehicle()) return;

        // check if the player is sneaking
        for (Entity passenger : this.getPassengers()) {
                if (!(passenger instanceof Player)) continue;
                Player player = (Player) passenger;
                if (!player.isShiftKeyDown()) continue;

                passenger.stopRiding();
        }
    }
    
    /**
     * Handle movement input from the rider
     */
    private void handleMovement() {
        if (this.isVehicle()) {
            Entity rider = this.getControllingPassenger();
            if (rider == null && !this.getPassengers().isEmpty()) {
                rider = this.getPassengers().get(0);
            }
            
            if (rider instanceof Player player) {
                float forward = 0.0f;
                
                // Get player's movement and look direction
                Vec3 playerMovement = player.getDeltaMovement();
                Vec3 lookDirection = player.getLookAngle().normalize();
                
                if (playerMovement.horizontalDistance() > 0.01) {
                    // Calculate the forward component of player movement
                    Vec3 horizontalMovement = new Vec3(playerMovement.x, 0, playerMovement.z).normalize();
                    
                    // Dot product gives us how much the movement aligns with look direction
                    // 1.0 = moving forward, -1.0 = moving backward, 0.0 = moving sideways
                    double forwardComponent = horizontalMovement.dot(lookDirection);
                    
                    // Only use movement if it's mostly forward/backward (not sideways)
                    if (Math.abs(forwardComponent) > 0.7) { // 0.7 is about 45 degrees
                        forward = (float)forwardComponent;
                    }
                }
                
                if (this.tickCount % 60 == 0) {
                    System.out.println("Player movement: " + playerMovement.horizontalDistance() + ", forward: " + forward);
                }
                
                // Apply movement
                if (Math.abs(forward) > 0.001f) {
                    applyMovement(player, forward);
                } else {
                    applyFriction();
                }
                
                this.move(MoverType.SELF, this.getDeltaMovement());
            }
        }
    }
    
    /**
     * Apply movement based on player input
     */
    private void applyMovement(Player player, float forwardInput) {
        // Get the direction the player is looking
        double yaw = Math.toRadians(player.getYRot()); // Convert to radians using double
        
        // Calculate movement direction using double for precision
        double moveX = -Math.sin(yaw) * forwardInput * ACCELERATION;
        double moveZ = Math.cos(yaw) * forwardInput * ACCELERATION;
        
        // Get current velocity
        Vec3 currentVelocity = this.getDeltaMovement();
        
        // Add acceleration
        Vec3 newVelocity = currentVelocity.add(moveX, 0, moveZ);
        
        // Limit maximum speed
        double horizontalSpeed = Math.sqrt(newVelocity.x * newVelocity.x + newVelocity.z * newVelocity.z);
        if (horizontalSpeed > MAX_SPEED) {
            double scale = MAX_SPEED / horizontalSpeed;
            newVelocity = new Vec3(newVelocity.x * scale, newVelocity.y, newVelocity.z * scale);
        }
        
        // Set the new velocity
        this.setDeltaMovement(newVelocity);
    }
    
    /**
     * Apply friction to slow down the hoverboard
     */
    private void applyFriction() {
        Vec3 velocity = this.getDeltaMovement();
        
        // Apply horizontal friction - convert FRICTION to double
        Vec3 newVelocity = new Vec3(
            velocity.x * (double)FRICTION,
            velocity.y, // Don't apply friction to Y (vertical movement)
            velocity.z * (double)FRICTION
        );
        
        this.setDeltaMovement(newVelocity);
    }
    
    /**
     * Prevent the hoverboard from being destroyed when ridden
     */
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        if (this.isVehicle()) {
            // If someone is riding, don't take damage from most sources
            return false;
        }
        return super.hurt(damageSource, amount);
    }

    /**
     * Override to control passenger behavior - try to make it more like standing on a platform
     */
    @Override
    public double getPassengersRidingOffset() {
        return 0.8; // Keep the height offset
    }

    /**
     * Try to override the vehicle type behavior
     */
    @Override
    public boolean shouldRiderSit() {
        return false; // Tell Minecraft the rider shouldn't sit
    }

    /**
     * Override to prevent default sitting behavior
     */
    @Override
    protected boolean canRide(Entity entity) {
        return entity instanceof Player;
    }
}