package com.hexvane.strangematter.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.util.Mth;

public class HoverboardEntity extends Entity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<Float> BOARD_ROTATION = SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FORWARD_MOMENTUM = SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_BOOSTING = SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_JUMPING = SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Movement constants (configurable via Config class)
    private static final float FRICTION = 0.85f;
    private static final float ROTATION_SPEED = 3.0f; // Degrees per tick when turning
    private static final float BRAKE_FORCE = 0.95f; // How much S key slows down
    
    // Speed boost constants (Control key) - multipliers for config values
    private static final float BOOST_ACCELERATION_MULTIPLIER = 2.0f; // Double acceleration when boosting
    private static final float BOOST_MAX_SPEED_MULTIPLIER = 1.5f; // 1.5x max speed when boosting
    
    // Hover constants
    private static final float TARGET_HOVER_HEIGHT = 0.5f; // Half block above ground
    private static final float HOVER_ADJUSTMENT_SPEED = 0.2f;
    private static final float MAX_HOVER_SCAN_DISTANCE = 10.0f;
    private static final float CLIMB_BOOST = 0.25f; // Extra boost when detecting obstacle ahead
    
    // Jump constants (per guide)
    private static final float JUMP_VELOCITY = 0.4f;
    private static final int JUMP_COOLDOWN_TICKS = 10;
    
    // Jump state tracking (per guide)
    private boolean isJumping = false;
    private int jumpTicks = 0;
    private int jumpCooldown = 0;
    
    // Sound tracking
    private int loopSoundCooldown = 0;
    private static final int LOOP_SOUND_INTERVAL = 40; // Play loop sound every 2 seconds (40 ticks)
    
    public HoverboardEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true); // Hoverboards don't fall
        // this.setMaxUpStep(1.0f); // Allow stepping up 1 block like a horse - removed in NeoForge 1.21.1
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BOARD_ROTATION, 0.0f);
        builder.define(FORWARD_MOMENTUM, 0.0f);
        builder.define(IS_BOOSTING, false);
        builder.define(IS_JUMPING, false);
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("BoardRotation", this.entityData.get(BOARD_ROTATION));
        compound.putFloat("ForwardMomentum", this.entityData.get(FORWARD_MOMENTUM));
        compound.putBoolean("IsBoosting", this.entityData.get(IS_BOOSTING));
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("BoardRotation")) {
            this.entityData.set(BOARD_ROTATION, compound.getFloat("BoardRotation"));
        }
        if (compound.contains("ForwardMomentum")) {
            this.entityData.set(FORWARD_MOMENTUM, compound.getFloat("ForwardMomentum"));
        }
        if (compound.contains("IsBoosting")) {
            this.entityData.set(IS_BOOSTING, compound.getBoolean("IsBoosting"));
        }
    }
    
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        // Store old positions for interpolation (like 1.20.1 version)
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        
        if (steps <= 0) {
            // Immediate positioning
            this.setPos(x, y, z);
            this.setRot(yRot, xRot);
        } else {
            // Enhanced smoothing for jumps - use much stronger Y-axis smoothing during jumps
            double smoothingFactor = 1.0 / Math.max(steps, 1.0);
            
            // Detect if we're likely jumping (large Y difference)
            double yDiff = Math.abs(y - this.getY());
            boolean isLikelyJumping = yDiff > 0.3 || this.entityData.get(IS_JUMPING);
            
            // Use stronger smoothing for Y axis during jumps (matches 1.20.1)
            double ySmoothingFactor = isLikelyJumping ? smoothingFactor * 0.5 : smoothingFactor * 0.8;
            
            double deltaX = (x - this.getX()) * smoothingFactor;
            double deltaY = (y - this.getY()) * ySmoothingFactor;
            double deltaZ = (z - this.getZ()) * smoothingFactor;
            
            // Apply smooth exponential interpolation
            this.setPos(
                this.getX() + deltaX,
                this.getY() + deltaY,
                this.getZ() + deltaZ
            );
            
            // Smooth rotation too
            float deltaYaw = (float)(Mth.wrapDegrees(yRot - this.getYRot()) * smoothingFactor);
            float deltaPitch = (float)((xRot - this.getXRot()) * smoothingFactor);
            this.setRot(this.getYRot() + deltaYaw, this.getXRot() + deltaPitch);
        }
    }
    
    
    // ========== OPTIONAL OVERRIDES (FOR FUNCTIONALITY) ==========
    // These have default implementations but we override for specific behavior
    
    
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        // If player is sneaking, pick up the hoverboard
        if (player.isShiftKeyDown()) {
            // Drop hoverboard item
            ItemStack hoverboardItem = new ItemStack(com.hexvane.strangematter.StrangeMatterMod.HOVERBOARD.get());
            ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), hoverboardItem);
            this.level().addFreshEntity(itemEntity);
            
            // Play pickup sound
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), 
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8f, 1.0f);
            
            // Remove the entity
            this.discard();
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
        player.startRiding(this);
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
     * Prevent fall damage for players riding the hoverboard
     */
    private void preventFallDamageForRiders() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof Player player) {
                // Aggressively reset fall distance to prevent fall damage
                player.fallDistance = 0.0f;
                
                // During jump, don't interfere with player velocity
                if (!isJumping) {
                    // Only slow down very fast falls when not jumping
                    Vec3 playerVelocity = player.getDeltaMovement();
                    if (playerVelocity.y < -0.5) { // If falling faster than 0.5 blocks/tick
                        player.setDeltaMovement(playerVelocity.x, -0.1, playerVelocity.z); // Slow down the fall
                    }
                }
            }
        }
    }
    
    /**
     * Handle movement input from the rider using keyboard input
     */
    private void handleMovement() {
        if (!this.isVehicle()) return;
        
        Entity rider = this.getControllingPassenger();
        if (rider == null && !this.getPassengers().isEmpty()) {
            rider = this.getPassengers().get(0);
        }
        
        if (rider instanceof Player player) {
            // Get input from player's movement keys
            // Note: zza > 0 means forward (W key), zza < 0 means backward (S key)
            boolean forwardPressed = player.zza > 0; // Moving forward (W key)
            boolean backwardPressed = player.zza < 0; // Moving backward (S key)
            
            // Try different methods to detect Control key
            boolean boostPressed = player.isSprinting();
            
            // Sync boost state to entity data
            this.entityData.set(IS_BOOSTING, boostPressed);
            
            // Handle forward/backward movement (W/S keys) with boost
            // Rotation is now handled by ensureSmoothFollowing() which follows look direction
            if (forwardPressed) {
                applyForwardMovement(boostPressed);
            } else if (backwardPressed) {
                applyBackwardMovement(boostPressed);
            } else {
                // No input - apply friction
                applyFriction();
            }
        }
    }
    
    /**
     * Handle jump request from server (called by packet handler)
     * Per guide: validates jump conditions and applies jump velocity
     */
    public void handleJumpRequest() {
        if (!this.isVehicle()) return;
        
        // Validation per guide
        if (jumpCooldown > 0) return;
        if (isJumping) return;
        
        Vec3 currentVelocity = this.getDeltaMovement();
        if (currentVelocity.y >= 0.1) return; // Already moving up fast
        
        // Check height proximity (within 1.5 blocks of target hover height)
        BlockPos groundPos = findGroundBelow();
        if (groundPos != null) {
            double targetY = groundPos.getY() + 1.0 + TARGET_HOVER_HEIGHT + 0.5;
            double currentY = this.getY();
            if (Math.abs(currentY - targetY) > 1.5) return;
        }
        
        // Apply jump velocity (per guide)
        Vec3 newVelocity = currentVelocity.add(0, JUMP_VELOCITY, 0);
        this.setDeltaMovement(newVelocity.x, newVelocity.y, newVelocity.z);
        
        // Set jump state (per guide)
        isJumping = true;
        jumpTicks = 0;
        jumpCooldown = JUMP_COOLDOWN_TICKS;
        this.entityData.set(IS_JUMPING, true);
        
        // Play jump sound
        if (!this.level().isClientSide) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                com.hexvane.strangematter.sound.StrangeMatterSounds.HOVERBOARD_JUMP.get(), SoundSource.PLAYERS, 0.7f, 1.2f);
        }
    }
    
    
    /**
     * Handle loop sound playback while player is riding
     */
    private void handleLoopSound() {
        if (this.level().isClientSide) return;
        
        // Check if player is riding (not just moving)
        if (this.isVehicle() && loopSoundCooldown <= 0) {
            // Play loop sound
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                com.hexvane.strangematter.sound.StrangeMatterSounds.HOVERBOARD_LOOP.get(), SoundSource.PLAYERS, 0.4f, 1.0f);
            
            // Reset cooldown - play sound again after interval
            loopSoundCooldown = LOOP_SOUND_INTERVAL;
        }
    }
    
    /**
     * Ensure the hoverboard follows the player's look direction smoothly
     */
    private void ensureSmoothFollowing() {
        if (!this.isVehicle()) return;
        
        Entity rider = this.getControllingPassenger();
        if (rider == null && !this.getPassengers().isEmpty()) {
            rider = this.getPassengers().get(0);
        }
        
        if (rider != null) {
            // Make the hoverboard follow the player's look direction smoothly
            float targetYaw = rider.getYRot();
            float currentYaw = this.getYRot();
            
            // Calculate the shortest rotation path to avoid spinning
            float yawDiff = targetYaw - currentYaw;
            while (yawDiff > 180.0f) yawDiff -= 360.0f;
            while (yawDiff < -180.0f) yawDiff += 360.0f;
            
            // Apply smooth rotation with a damping factor
            float rotationSpeed = 0.3f; // Adjust this value for smoother/faster rotation
            float newYaw = currentYaw + (yawDiff * rotationSpeed);
            
            this.setYRot(newYaw);
            this.yRotO = this.getYRot();
        }
    }
    
    /**
     * Apply forward movement with momentum and optional boost
     */
    private void applyForwardMovement(boolean boost) {
        double yaw = Math.toRadians(this.getYRot());
        
        // Use config values with boost multipliers if Control key is pressed
        float acceleration = boost ? 
            (float)(com.hexvane.strangematter.Config.hoverboardAcceleration * BOOST_ACCELERATION_MULTIPLIER) : 
            (float)com.hexvane.strangematter.Config.hoverboardAcceleration;
        float maxSpeed = boost ? 
            (float)(com.hexvane.strangematter.Config.hoverboardMaxSpeed * BOOST_MAX_SPEED_MULTIPLIER) : 
            (float)com.hexvane.strangematter.Config.hoverboardMaxSpeed;
        
        // Calculate movement direction based on hoverboard's facing
        double moveX = -Math.sin(yaw) * acceleration;
        double moveZ = Math.cos(yaw) * acceleration;
        
        // Get current velocity
        Vec3 currentVelocity = this.getDeltaMovement();
        
        // Add acceleration (preserve Y velocity - don't touch vertical movement during jumps)
        Vec3 newVelocity = currentVelocity.add(moveX, 0, moveZ);
        
        // Limit maximum speed
        double horizontalSpeed = Math.sqrt(newVelocity.x * newVelocity.x + newVelocity.z * newVelocity.z);
        if (horizontalSpeed > maxSpeed) {
            double scale = maxSpeed / horizontalSpeed;
            newVelocity = new Vec3(newVelocity.x * scale, newVelocity.y, newVelocity.z * scale);
        }
        
        // Update momentum
        this.entityData.set(FORWARD_MOMENTUM, (float)horizontalSpeed);
        
        // Set the new velocity
        this.setDeltaMovement(newVelocity);
    }
    
    /**
     * Apply backward movement (braking) with optional boost
     */
    private void applyBackwardMovement(boolean boost) {
        Vec3 currentVelocity = this.getDeltaMovement();
        
        // Use stronger braking when boosting (more responsive)
        float brakeForce = boost ? BRAKE_FORCE * 0.9f : BRAKE_FORCE;
        
        // Apply braking force
        Vec3 newVelocity = new Vec3(
            currentVelocity.x * brakeForce,
            currentVelocity.y,
            currentVelocity.z * brakeForce
        );
        
        // Update momentum
        double horizontalSpeed = Math.sqrt(newVelocity.x * newVelocity.x + newVelocity.z * newVelocity.z);
        this.entityData.set(FORWARD_MOMENTUM, (float)horizontalSpeed);
        
        this.setDeltaMovement(newVelocity);
    }
    
    /**
     * Apply friction to slow down the hoverboard
     */
    private void applyFriction() {
        Vec3 velocity = this.getDeltaMovement();
        
        // Apply horizontal friction
        Vec3 newVelocity = new Vec3(
            velocity.x * FRICTION,
            velocity.y, // Don't apply friction to Y (vertical movement)
            velocity.z * FRICTION
        );
        
        // Update momentum
        double horizontalSpeed = Math.sqrt(newVelocity.x * newVelocity.x + newVelocity.z * newVelocity.z);
        this.entityData.set(FORWARD_MOMENTUM, (float)horizontalSpeed);
        
        this.setDeltaMovement(newVelocity);
    }
    
    /**
     * Maintain hover height above ground with smart height detection
     */
    private void maintainHoverHeight() {
        // Find ground below
        BlockPos groundPos = findGroundBelow();
        
        if (groundPos != null) {
            // Calculate base target Y position (add half block to base height)
            double baseTargetY = groundPos.getY() + 1.0 + TARGET_HOVER_HEIGHT + 0.5;
            
            // Find the highest block in a radius around the hoverboard
            double maxHeightAdjustment = findMaxHeightInRadius();
            
            // Calculate final target Y with height adjustment
            double targetY = baseTargetY + maxHeightAdjustment;
            double currentY = this.getY();
            double heightDifference = targetY - currentY;
            
            // Apply gradual adjustment to Y velocity
            Vec3 currentVelocity = this.getDeltaMovement();
            double verticalAdjustment = heightDifference * HOVER_ADJUSTMENT_SPEED;
            
            // Clamp the adjustment to prevent excessive movement - reduce aggressiveness for smoother motion
            verticalAdjustment = Math.max(-0.25, Math.min(0.3, verticalAdjustment));
            
            // Smooth jump handling - allow jump to complete, then smoothly transition back to hover (matches 1.20.1)
            if (isJumping && currentVelocity.y > 0.05 && jumpTicks < 12) {
                // During first part of jump, let it go up naturally with NO interference
                // Don't modify velocity at all - let jump complete naturally for smoothness
                return; // Don't apply hover adjustment during active jump
            } else if (isJumping && jumpTicks < 18) {
                // Later in jump - start applying gentle downward force if above target
                if (heightDifference < -0.3) {
                    // Above target height - apply very gentle downward adjustment
                    double newY = Math.max(currentVelocity.y - 0.015, -0.10); // Even gentler deceleration
                    this.setDeltaMovement(currentVelocity.x, newY, currentVelocity.z);
                } else {
                    // Let it continue with minimal damping for smoother motion
                    this.setDeltaMovement(currentVelocity.x, currentVelocity.y * 0.98, currentVelocity.z);
                }
            } else {
                // Normal hover adjustment - use smoother interpolation
                // Blend current velocity with target adjustment for smoother movement (matches 1.20.1)
                double smoothedY = currentVelocity.y * 0.8 + verticalAdjustment * 0.2;
                this.setDeltaMovement(currentVelocity.x, smoothedY, currentVelocity.z);
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
            
            // Check if this block has a collision shape that can support the hoverboard
            if (!blockState.isAir()) {
                VoxelShape collisionShape = blockState.getCollisionShape(this.level(), checkPos);
                if (!collisionShape.isEmpty() && !collisionShape.equals(Shapes.empty())) {
                    return checkPos;
                }
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
     * Find the maximum height adjustment needed in a radius around the hoverboard
     */
    private double findMaxHeightInRadius() {
        BlockPos currentPos = this.blockPosition();
        double maxHeightAdjustment = 0.0;
        int radius = 2; // Check 2 blocks in each direction
        int maxHeightCap = 1; // Cap at 1 block higher than current height
        
        // Scan a radius around the hoverboard
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Skip the center position (where we are)
                if (x == 0 && z == 0) continue;
                
                BlockPos checkPos = currentPos.offset(x, 0, z);
                
                // Check from ground level up to the height cap
                for (int y = 0; y <= maxHeightCap; y++) {
                    BlockPos heightCheckPos = checkPos.above(y);
                    BlockState blockState = this.level().getBlockState(heightCheckPos);
                    
                    // If there's a block with collision at this height, we need to account for it
                    if (!blockState.isAir()) {
                        VoxelShape collisionShape = blockState.getCollisionShape(this.level(), heightCheckPos);
                        if (!collisionShape.isEmpty() && !collisionShape.equals(Shapes.empty())) {
                            // Calculate how much we need to raise to clear this block
                            double requiredHeight = y + 1.0; // +1 to clear the block
                            maxHeightAdjustment = Math.max(maxHeightAdjustment, requiredHeight);
                        }
                    }
                }
            }
        }
        
        // Cap the height adjustment to prevent excessive changes
        return Math.min(maxHeightAdjustment, maxHeightCap);
    }
    
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        if (this.isVehicle()) {
            // If someone is riding, don't take damage from most sources
            return false;
        }
        return super.hurt(damageSource, amount);
    }
    
    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, net.minecraft.world.damagesource.DamageSource damageSource) {
        // Prevent fall damage for the hoverboard itself
        return false;
    }
    

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        // Position passenger slightly above the board to prevent clipping during jumps
        // 0.5 blocks above board center (board is ~0.25 blocks tall, player needs ~0.25 above)
        return new Vec3(0.0, 0.5, 0.0);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // Update jump cooldown
        if (jumpCooldown > 0) jumpCooldown--;
        
        // Update jump state tracking (per guide)
        if (isJumping) {
            jumpTicks++;
            Vec3 velocity = this.getDeltaMovement();
            // Reset jump state when falling or after max ticks (per guide)
            if (velocity.y < 0.05 || jumpTicks > 15) {
                isJumping = false;
                jumpTicks = 0;
                this.entityData.set(IS_JUMPING, false);
            }
        }
        
        // Update loop sound cooldown
        if (loopSoundCooldown > 0) {
            loopSoundCooldown--;
        }
        
        // Handle dismount when player sneaks
        handleDismount();
        
        if (this.isVehicle()) {
            // Play loop sound if riding
            handleLoopSound();
            
            // Handle movement input
            handleMovement();
            
            // Maintain hover height (with jump handling per guide)
            maintainHoverHeight();
            
            // Ensure smooth following
            ensureSmoothFollowing();
            
            // Prevent fall damage for riders
            preventFallDamageForRiders();
        } else {
            // No rider - apply friction and fall slowly
            applyFriction();
            Vec3 currentVelocity = this.getDeltaMovement();
            this.setDeltaMovement(currentVelocity.x, currentVelocity.y - 0.02, currentVelocity.z);
        }
        
        // Move the entity
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    public boolean shouldRiderSit() {
        return false; // Rider should stand, not sit
    }

    @Override
    protected boolean canRide(Entity entity) {
        return entity instanceof Player;
    }
    
    @Override
    public boolean isPickable() {
        return true;
    }
    
    @Override
    public boolean isPushable() {
        return false; // Prevent other entities from pushing the hoverboard
    }
}