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
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
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
    
    // Jump constants
    private static final float JUMP_VELOCITY = 0.4f; // Upward velocity when jumping (reduced for smoother jumps)
    private int jumpCooldown = 0; // Cooldown between jumps
    private static final int JUMP_COOLDOWN_TICKS = 10; // Minimum ticks between jumps (increased to prevent spam)
    private boolean isJumping = false; // Track if currently jumping
    private int jumpTicks = 0; // Track how many ticks since jump started
    
    // Sound tracking
    private int loopSoundCooldown = 0; // Cooldown to prevent sound spam
    private static final int LOOP_SOUND_INTERVAL = 40; // Play loop sound every 2 seconds (40 ticks)
    
    public HoverboardEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true); // Hoverboards don't fall
        this.setMaxUpStep(1.0f); // Allow stepping up 1 block like a horse
    }
    
    @Override
    protected void defineSynchedData() {
        this.entityData.define(BOARD_ROTATION, 0.0f);
        this.entityData.define(FORWARD_MOMENTUM, 0.0f);
        this.entityData.define(IS_BOOSTING, false);
        this.entityData.define(IS_JUMPING, false);
    }
    
    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int lerpSteps, boolean teleport) {
        // Store old positions for interpolation
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        
        if (teleport || lerpSteps <= 0) {
            // Immediate positioning (teleport)
            this.setPos(x, y, z);
            this.setRot(yaw, pitch);
        } else {
            // Enhanced smoothing for jumps - use even stronger Y-axis smoothing during jumps
            double smoothingFactor = 1.0 / Math.max(lerpSteps, 1.0);
            
            // Detect if we're likely jumping (large Y difference)
            double yDiff = Math.abs(y - this.getY());
            boolean isLikelyJumping = yDiff > 0.3 || this.entityData.get(IS_JUMPING);
            
            // Use much stronger smoothing for Y axis during jumps to prevent jerking
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
            float deltaYaw = (float)(Mth.wrapDegrees(yaw - this.getYRot()) * smoothingFactor);
            float deltaPitch = (float)((pitch - this.getXRot()) * smoothingFactor);
            this.setRot(this.getYRot() + deltaYaw, this.getXRot() + deltaPitch);
        }
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("BoardRotation", this.entityData.get(BOARD_ROTATION));
        compound.putFloat("ForwardMomentum", this.entityData.get(FORWARD_MOMENTUM));
        compound.putBoolean("IsBoosting", this.entityData.get(IS_BOOSTING));
        compound.putInt("JumpCooldown", jumpCooldown);
        compound.putBoolean("IsJumping", isJumping);
        compound.putInt("JumpTicks", jumpTicks);
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
        if (compound.contains("JumpCooldown")) {
            jumpCooldown = compound.getInt("JumpCooldown");
        }
        if (compound.contains("IsJumping")) {
            isJumping = compound.getBoolean("IsJumping");
        }
        if (compound.contains("JumpTicks")) {
            jumpTicks = compound.getInt("JumpTicks");
        }
    }
    
    // ========== OPTIONAL OVERRIDES (FOR FUNCTIONALITY) ==========
    // These have default implementations but we override for specific behavior
    
    
    @Override
    public void tick() {
        super.tick();
        
        // Handle dismount when player sneaks
        handleDismount();
        
        // Update jump cooldown
        if (jumpCooldown > 0) {
            jumpCooldown--;
        }
        
        // Update jump tick counter
        if (isJumping) {
            jumpTicks++;
        }
        
        // Reset jump state if upward velocity is low/negative OR if cooldown expired
        if (this.getDeltaMovement().y < 0.05 || jumpTicks > 15) {
            if (isJumping) {
                isJumping = false;
                this.entityData.set(IS_JUMPING, false);
            }
            jumpTicks = 0;
        }
        
        // Update loop sound cooldown
        if (loopSoundCooldown > 0) {
            loopSoundCooldown--;
        }
        
        if (this.isVehicle()) {
            // Prevent fall damage for riders
            preventFallDamageForRiders();
            
            // Handle movement input (includes jump detection)
            handleMovement();
            
            // Play loop sound if moving
            handleLoopSound();
            
            // Maintain hover height (will skip if jumping)
            maintainHoverHeight();
            
            // Ensure smooth following of the player
            ensureSmoothFollowing();
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

                // Reset fall distance before dismounting to prevent accumulated fall damage
                player.fallDistance = 0.0f;
                if (passenger instanceof net.minecraft.world.entity.LivingEntity living) {
                    living.fallDistance = 0.0f;
                }
                
                passenger.stopRiding();
        }
    }
    
    /**
     * Prevent fall damage for players riding the hoverboard
     */
    private void preventFallDamageForRiders() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof Player player) {
                // Reset fall distance to prevent fall damage
                player.fallDistance = 0.0f;
                
                // Also reset for any LivingEntity to be safe
                if (passenger instanceof net.minecraft.world.entity.LivingEntity living) {
                    living.fallDistance = 0.0f;
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
            
            // Note: Jump handling is now done via network packet, not here
            
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
        
        // Add acceleration
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
     * Handle jump input from the player
     * Called when a jump packet is received from the client
     */
    public void handleJumpRequest() {
        // Check cooldown and that we're near hover height
        if (jumpCooldown <= 0 && !isJumping) {
            // Check if we're close to our target hover height
            BlockPos groundPos = findGroundBelow();
            if (groundPos != null) {
                double baseTargetY = groundPos.getY() + 1.0 + TARGET_HOVER_HEIGHT + 0.5;
                double maxHeightAdjustment = findMaxHeightInRadius();
                double targetY = baseTargetY + maxHeightAdjustment;
                double currentY = this.getY();
                double heightDifference = targetY - currentY;
                
                // Only allow jump if we're within 1.5 blocks of target height and not moving upward too fast
                boolean nearHoverHeight = Math.abs(heightDifference) < 1.5 && this.getDeltaMovement().y < 0.1;
                
                if (nearHoverHeight) {
                    // Apply jump velocity
                    Vec3 currentVelocity = this.getDeltaMovement();
                    Vec3 jumpVelocity = new Vec3(
                        currentVelocity.x,
                        currentVelocity.y + JUMP_VELOCITY,
                        currentVelocity.z
                    );
                    this.setDeltaMovement(jumpVelocity);
                    
                    // Set jump state and cooldown
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
            } else {
                // No ground found - allow jump but with slightly stricter requirements
                if (this.getDeltaMovement().y < 0.1) {
                    // Apply jump velocity
                    Vec3 currentVelocity = this.getDeltaMovement();
                    Vec3 jumpVelocity = new Vec3(
                        currentVelocity.x,
                        currentVelocity.y + JUMP_VELOCITY,
                        currentVelocity.z
                    );
                    this.setDeltaMovement(jumpVelocity);
                    
                    // Set jump state and cooldown
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
            }
        }
    }
    
    /**
     * Sets the jump pressed state (called from packet handler on server)
     */
    public void setJumpPressed(boolean pressed) {
        if (pressed && !this.level().isClientSide) {
            handleJumpRequest();
        }
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
            
            // Smooth jump handling - allow jump to complete, then smoothly transition back to hover
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
                // Blend current velocity with target adjustment for smoother movement
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
            
            // Check if this block can support the hoverboard
            if (!blockState.isAir() && canBlockSupportHoverboard(blockState, checkPos)) {
                return checkPos;
            }
        }
        
        return null;
    }
    
    /**
     * Check if a block can support the hoverboard, including partial blocks like slabs and snow
     */
    private boolean canBlockSupportHoverboard(BlockState blockState, BlockPos pos) {
        // Check if the block has a collision shape that can support the hoverboard
        if (blockState.isSolidRender(this.level(), pos)) {
            return true;
        }
        
        // Check for partial blocks that can still support the hoverboard
        // This includes slabs, snow layers, stairs, etc.
        var shape = blockState.getCollisionShape(this.level(), pos);
        if (!shape.isEmpty()) {
            // Check if the collision shape has any solid parts at the top
            // We consider it supportive if there's collision at Y=1.0 (top of block)
            var topBox = shape.bounds().maxY;
            return topBox >= 0.5; // At least half a block high
        }
        
        return false;
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
                    
                    // If there's a solid block at this height, we need to account for it
                    if (!blockState.isAir() && blockState.isSolidRender(this.level(), heightCheckPos)) {
                        // Calculate how much we need to raise to clear this block
                        double requiredHeight = y + 1.0; // +1 to clear the block
                        maxHeightAdjustment = Math.max(maxHeightAdjustment, requiredHeight);
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
    public double getPassengersRidingOffset() {
        return 0.4; // Height offset for rider - position player feet on top of board
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