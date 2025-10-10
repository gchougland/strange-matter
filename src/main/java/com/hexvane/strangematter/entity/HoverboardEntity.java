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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class HoverboardEntity extends Entity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Float> BOARD_ROTATION = SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.FLOAT);
    
    // Add rotation tracking to entity data
    private static final EntityDataAccessor<Float> BOARD_YAW = SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.FLOAT);
    
    // Add movement constants
    private static final float ACCELERATION = 0.02f;
    private static final float MAX_SPEED = 0.3f;
    private static final float FRICTION = 0.9f;
    
    // Add to constants
    private static final float ROTATION_SPEED = 2.0f; // Degrees per tick when turning
    
    // Add hover constants
    private static final float TARGET_HOVER_HEIGHT = 0.5f; // Half block above ground
    private static final float HOVER_ADJUSTMENT_SPEED = 0.15f; // Increased from 0.05 for faster climbing
    private static final float MAX_HOVER_SCAN_DISTANCE = 10.0f;
    private static final float CLIMB_BOOST = 0.2f; // Extra boost when detecting obstacle ahead
    
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
        this.entityData.define(BOARD_YAW, 0.0f); // Track hoverboard's facing direction
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        // REQUIRED: Abstract method in Entity - saves entity data to NBT
        compound.putFloat("BoardRotation", this.entityData.get(BOARD_ROTATION));
        compound.putFloat("BoardYaw", this.entityData.get(BOARD_YAW));
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        // REQUIRED: Abstract method in Entity - loads entity data from NBT
        if (compound.contains("BoardRotation")) {
            this.entityData.set(BOARD_ROTATION, compound.getFloat("BoardRotation"));
        }
        if (compound.contains("BoardYaw")) {
            this.entityData.set(BOARD_YAW, compound.getFloat("BoardYaw"));
            this.setYRot(compound.getFloat("BoardYaw")); // Set entity rotation
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
        super.tick();
        if (this.level().isClientSide) return;
        
        // Add basic debug to see if tick is running
        if (this.tickCount % 60 == 0) {
            System.out.println("HoverboardEntity tick - isVehicle: " + this.isVehicle() + ", passengers: " + this.getPassengers().size());
        }
         
        // Handle dismount when player sneaks
        handleDismount();
        
        // Only hover when someone is riding
        if (this.isVehicle()) {
            // Maintain hover height
            maintainHoverHeight();
            
            // Handle movement input
            handleMovement();
        } else {
            // No rider - apply gravity and friction, don't hover
            applyFriction();
            
            // Apply gravity
            Vec3 currentVelocity = this.getDeltaMovement();
            this.setDeltaMovement(currentVelocity.x, currentVelocity.y - 0.08, currentVelocity.z);
            
            // Move with gravity
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
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
     * Handle movement input from the rider - Tank controls version
     */
    private void handleMovement() {
        if (this.isVehicle()) {
            Entity rider = this.getControllingPassenger();
            if (rider == null && !this.getPassengers().isEmpty()) {
                rider = this.getPassengers().get(0);
            }
            
            if (rider instanceof Player player) {
                float forward = 0.0f;
                float turn = 0.0f;
                
                // Get player's movement input
                Vec3 playerMovement = player.getDeltaMovement();
                Vec3 lookDirection = player.getLookAngle().normalize();
                
                if (playerMovement.horizontalDistance() > 0.01) {
                    Vec3 horizontalMovement = new Vec3(playerMovement.x, 0, playerMovement.z).normalize();
                    
                    // Get the forward/backward component (W/S)
                    double forwardComponent = horizontalMovement.dot(lookDirection);
                    
                    // Get the left/right component (A/D)
                    // Cross product to get the "right" vector, then dot product for strafe
                    Vec3 rightDirection = new Vec3(lookDirection.z, 0, -lookDirection.x).normalize();
                    double strafeComponent = horizontalMovement.dot(rightDirection);
                    
                    // Forward/backward movement
                    if (Math.abs(forwardComponent) > 0.3) {
                        forward = (float)forwardComponent;
                    }
                    
                    // Left/right rotation
                    if (Math.abs(strafeComponent) > 0.3) {
                        turn = (float)strafeComponent;
                    }
                }
                
                if (this.tickCount % 60 == 0) {
                    System.out.println("Forward: " + forward + ", Turn: " + turn);
                }
                
                // Apply rotation
                if (Math.abs(turn) > 0.001f) {
                    float currentYaw = this.getYRot();
                    float newYaw = currentYaw - (turn * ROTATION_SPEED);
                    this.setYRot(newYaw);
                    this.entityData.set(BOARD_YAW, newYaw);
                }
                
                // Apply forward/backward movement in the direction the hoverboard is facing
                if (Math.abs(forward) > 0.001f) {
                    applyMovementTankStyle(forward);
                } else {
                    applyFriction();
                }
                
                this.move(MoverType.SELF, this.getDeltaMovement());
            }
        } else {
            applyFriction();
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
    }
    
    /**
     * Apply movement based on hoverboard's facing direction (not player's look direction)
     */
    private void applyMovementTankStyle(float forwardInput) {
        // Use the hoverboard's rotation, not the player's look direction
        double yaw = Math.toRadians(this.getYRot());
        
        // Calculate movement direction based on hoverboard's facing
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
     * Maintain hover height above ground
     */
    private void maintainHoverHeight() {
        // Find ground below
        BlockPos groundPos = findGroundBelow();
        
        if (groundPos != null) {
            // Calculate target Y position
            double targetY = groundPos.getY() + 1.0 + TARGET_HOVER_HEIGHT;
            double currentY = this.getY();
            double heightDifference = targetY - currentY;
            
            // Check if there's a block ahead that we need to climb
            boolean needsClimb = checkForObstacleAhead();
            
            // Apply gradual adjustment to Y velocity
            Vec3 currentVelocity = this.getDeltaMovement();
            double verticalAdjustment = heightDifference * HOVER_ADJUSTMENT_SPEED;
            
            // If moving and there's an obstacle, add extra upward boost
            if (needsClimb && currentVelocity.horizontalDistance() > 0.01) {
                verticalAdjustment += CLIMB_BOOST;
            }
            
            // Clamp the adjustment
            verticalAdjustment = Math.max(-0.2, Math.min(0.3, verticalAdjustment));
            
            // Set new velocity with hover adjustment
            this.setDeltaMovement(currentVelocity.x, verticalAdjustment, currentVelocity.z);
            
            if (this.tickCount % 60 == 0) {
                System.out.println("Hover: ground=" + groundPos.getY() + ", current=" + String.format("%.2f", currentY) + 
                                 ", target=" + String.format("%.2f", targetY) + ", climb=" + needsClimb);
            }
        } else {
            // No ground found, apply gravity slowly
            Vec3 currentVelocity = this.getDeltaMovement();
            this.setDeltaMovement(currentVelocity.x, currentVelocity.y - 0.02, currentVelocity.z);
        }
    }

    /**
     * Find solid ground or fluid surface below the hoverboard
     */
    private BlockPos findGroundBelow() {
        BlockPos currentPos = this.blockPosition();
        
        // Scan downward for solid ground or fluid surface
        for (int i = 0; i < MAX_HOVER_SCAN_DISTANCE; i++) {
            BlockPos checkPos = currentPos.below(i);
            BlockState blockState = this.level().getBlockState(checkPos);
            FluidState fluidState = this.level().getFluidState(checkPos);
            
            // Check if this is a fluid surface (water or lava)
            if (!fluidState.isEmpty()) {
                // Check if this is the top of the fluid
                if (fluidState.isSource() || isTopOfFluid(checkPos, fluidState)) {
                    return checkPos;
                }
            }
            
            // Check if this block is solid and can support the hoverboard
            if (!blockState.isAir() && blockState.isSolid()) {
                return checkPos;
            }
        }
        
        return null;
    }

    /**
     * Check if this position is the top surface of a fluid
     */
    private boolean isTopOfFluid(BlockPos pos, FluidState fluidState) {
        // Check if the block above is air or not fluid
        BlockPos above = pos.above();
        FluidState aboveFluid = this.level().getFluidState(above);
        
        // It's the top if there's no fluid above
        return aboveFluid.isEmpty();
    }

    /**
     * Check if there's a block directly ahead that we need to climb over
     */
    private boolean checkForObstacleAhead() {
        Vec3 velocity = this.getDeltaMovement();
        
        // Only check if we're moving
        if (velocity.horizontalDistance() < 0.01) {
            return false;
        }
        
        // Get movement direction
        Vec3 direction = new Vec3(velocity.x, 0, velocity.z).normalize();
        
        // Check 1-2 blocks ahead at ground level
        BlockPos currentPos = this.blockPosition();
        BlockPos aheadPos = currentPos.offset(
            (int)Math.round(direction.x),
            0,
            (int)Math.round(direction.z)
        );
        
        // Check if there's a solid block at or above current level
        BlockState aheadBlock = this.level().getBlockState(aheadPos);
        BlockState aboveAheadBlock = this.level().getBlockState(aheadPos.above());
        FluidState aheadFluid = this.level().getFluidState(aheadPos);
        
        // Return true if there's a solid block we need to climb
        // Don't count fluids as obstacles
        boolean hasObstacle = ((!aheadBlock.isAir() && aheadBlock.isSolid()) || 
                              (!aboveAheadBlock.isAir() && aboveAheadBlock.isSolid())) &&
                              aheadFluid.isEmpty();
        
        return hasObstacle;
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