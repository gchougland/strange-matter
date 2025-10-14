package com.hexvane.strangematter.block;

import com.hexvane.strangematter.Config;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.EnergeticRiftEntity;
import com.hexvane.strangematter.util.AnomalyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Block entity for the Rift Stabilizer.
 * Generates power by absorbing energy from nearby Energetic Rift anomalies.
 * Only outputs energy through its SOUTH face.
 */
public class RiftStabilizerBlockEntity extends BaseMachineBlockEntity {
    
    // Configurable beam offset (adjust to position beam correctly on block face)
    // These values are relative to the block center
    // X/Y offsets are added to center position, Z offset is the distance from center to face
    public static double BEAM_OFFSET_X = 0.0;
    public static double BEAM_OFFSET_Y = 0.5;
    public static double BEAM_OFFSET_Z = 0.0;
    
    private boolean isGenerating = false;
    private int currentPowerGeneration = 0;
    private EnergeticRiftEntity connectedRift = null;
    
    // Tick counter for rift checking
    private int riftCheckCounter = 0;
    private static final int RIFT_CHECK_INTERVAL = 20; // Check every 20 ticks (1 second)
    
    public RiftStabilizerBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.RIFT_STABILIZER_BLOCK_ENTITY.get(), pos, state, 0); // No inventory
        
        // Configure energy settings for Rift Stabilizer
        // Only output on SOUTH face, no input from any side
        boolean[] inputSides = {false, false, false, false, false, false}; // No input
        boolean[] outputSides = {false, false, false, false, false, false}; // Will be set to SOUTH only
        
        // Set energy storage configuration
        energyStorage.setCapacity(Config.riftStabilizerEnergyStorage);
        energyStorage.setMaxReceive(Config.riftStabilizerEnergyPerTick * 2); // Allow internal generation
        energyStorage.setMaxExtract(Config.riftStabilizerTransferRate);
        
        // Configure output sides - only SOUTH face
        Direction facing = state.getValue(RiftStabilizerBlock.FACING);
        Direction outputFace = facing.getOpposite(); // SOUTH is opposite of where block faces
        outputSides[outputFace.get3DDataValue()] = true;
        
        this.energyInputSides = inputSides;
        this.energyOutputSides = outputSides;
        
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, RiftStabilizerBlockEntity blockEntity) {
        if (level.isClientSide) return;
        
        // Call the base class tick method which handles energy distribution and calls processMachine()
        BaseMachineBlockEntity.tick(level, pos, state, blockEntity);
    }
    
    /**
     * Update power generation based on nearby Energetic Rift anomalies
     */
    private void updatePowerGeneration(Level level, BlockPos pos) {
        // Find nearest Energetic Rift within range
        Optional<EnergeticRiftEntity> riftOptional = AnomalyUtil.findNearestEnergeticRift(
            level, pos, Config.riftStabilizerRadius
        );
        
        boolean wasGenerating = isGenerating;
        
        if (riftOptional.isPresent()) {
            EnergeticRiftEntity rift = riftOptional.get();
            
            // Check if the rift is in front of the stabilizer's north face
            if (!isRiftInFrontOfNorthFace(rift, pos)) {
                isGenerating = false;
                connectedRift = null;
                currentPowerGeneration = 0;
            } else {
                // Check if this rift can support another stabilizer
                int stabilizersConnected = countStabilizersConnectedToRift(level, rift);
                
                if (stabilizersConnected < Config.riftStabilizerMaxPerRift) {
                    // We can generate power!
                    isGenerating = true;
                    connectedRift = rift;
                    currentPowerGeneration = Config.riftStabilizerEnergyPerTick;
                } else {
                    // Too many stabilizers connected to this rift
                    isGenerating = false;
                    connectedRift = null;
                    currentPowerGeneration = 0;
                }
            }
        } else {
            // No rift in range
            isGenerating = false;
            connectedRift = null;
            currentPowerGeneration = 0;
        }
        
        // Sync to client if state changed
        if (wasGenerating != isGenerating) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
            }
        }
    }
    
    /**
     * Check if the rift is in front of the stabilizer's north face
     */
    private boolean isRiftInFrontOfNorthFace(EnergeticRiftEntity rift, BlockPos pos) {
        Direction facing = getBlockState().getValue(RiftStabilizerBlock.FACING);
        
        // Calculate vector from stabilizer to rift
        net.minecraft.world.phys.Vec3 stabilizerPos = net.minecraft.world.phys.Vec3.atCenterOf(pos);
        net.minecraft.world.phys.Vec3 riftPos = rift.position();
        net.minecraft.world.phys.Vec3 toRift = riftPos.subtract(stabilizerPos).normalize();
        
        // Get the direction vector for the north face (same as facing direction)
        net.minecraft.world.phys.Vec3 northFaceDir = net.minecraft.world.phys.Vec3.atLowerCornerOf(facing.getNormal());
        
        // Check if the rift is in front (dot product > 0 means same general direction)
        double dotProduct = toRift.dot(northFaceDir);
        
        // Rift must be at least somewhat in front (within ~90 degrees)
        return dotProduct > 0.0;
    }
    
    /**
     * Count how many Rift Stabilizers are connected to the given rift
     */
    private int countStabilizersConnectedToRift(Level level, EnergeticRiftEntity rift) {
        BlockPos riftPos = rift.blockPosition();
        double radius = Config.riftStabilizerRadius;
        
        int count = 0;
        
        // Search in a cube around the rift
        for (int x = (int) -radius; x <= radius; x++) {
            for (int y = (int) -radius; y <= radius; y++) {
                for (int z = (int) -radius; z <= radius; z++) {
                    BlockPos checkPos = riftPos.offset(x, y, z);
                    
                    // Check if within sphere
                    if (checkPos.distSqr(riftPos) <= radius * radius) {
                        BlockEntity be = level.getBlockEntity(checkPos);
                        if (be instanceof RiftStabilizerBlockEntity stabilizer) {
                            // Check if this stabilizer is actually generating from this rift
                            if (stabilizer.connectedRift == rift || stabilizer.isWithinRangeOf(rift)) {
                                count++;
                            }
                        }
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * Check if this stabilizer is within range of the given rift
     */
    private boolean isWithinRangeOf(EnergeticRiftEntity rift) {
        double distance = Math.sqrt(worldPosition.distSqr(rift.blockPosition()));
        return distance <= Config.riftStabilizerRadius;
    }
    
    @Override
    protected MachineEnergyRole getEnergyRole() {
        return MachineEnergyRole.GENERATOR; // Rift Stabilizer only generates energy
    }
    
    @Override
    protected void processMachine() {
        if (level == null) return;
        
        // Check for nearby Energetic Rift periodically
        riftCheckCounter++;
        if (riftCheckCounter >= RIFT_CHECK_INTERVAL) {
            riftCheckCounter = 0;
            updatePowerGeneration(level, worldPosition);
        }
        
        // Generate energy every tick
        if (isGenerating && currentPowerGeneration > 0) {
            int generated = energyStorage.receiveEnergy(currentPowerGeneration, false);
            
            // Only mark changed and sync if we actually generated energy
            if (generated > 0) {
                setChanged();
                
                // Sync to client periodically to show updated energy
                if (riftCheckCounter == 0) { // Sync when we check for rifts
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2); // Use flag 2 for block entity data sync
                }
            }
        }
    }
    
    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory) {
        // Rift Stabilizer doesn't have a GUI
        return null;
    }
    
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putBoolean("isGenerating", isGenerating);
        tag.putInt("currentPowerGeneration", currentPowerGeneration);
        tag.putInt("riftCheckCounter", riftCheckCounter);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energyStorage.setEnergy(tag.getInt("energy"));
        isGenerating = tag.getBoolean("isGenerating");
        currentPowerGeneration = tag.getInt("currentPowerGeneration");
        riftCheckCounter = tag.getInt("riftCheckCounter");
    }
    
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }
    
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    
    // Public getters for display and rendering (energy getters inherited from BaseMachineBlockEntity)
    
    public boolean isGenerating() {
        return isGenerating;
    }
    
    public int getCurrentPowerGeneration() {
        return currentPowerGeneration;
    }
    
    public EnergeticRiftEntity getConnectedRift() {
        return connectedRift;
    }
    
    /**
     * Get the beam target position (on the north face of the block)
     * This is configurable via the static BEAM_OFFSET fields
     */
    public net.minecraft.world.phys.Vec3 getBeamTargetPos() {
        Direction facing = getBlockState().getValue(RiftStabilizerBlock.FACING);
        
        // Start at block center
        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + BEAM_OFFSET_Y;
        double z = worldPosition.getZ() + 0.5;
        
        // The north face is the same as the facing direction (intake face)
        // Apply offsets relative to the facing direction
        Direction northFace = facing;
        
        switch (northFace) {
            case NORTH:
                x += BEAM_OFFSET_X;
                z -= 0.25- BEAM_OFFSET_Z; // 0.5 to face, then offset inward/outward
                break;
            case SOUTH:
                x += BEAM_OFFSET_X;
                z += 0.25 + BEAM_OFFSET_Z; // 0.5 to face, then offset inward/outward
                break;
            case WEST:
                x -= 0.25 - BEAM_OFFSET_Z; // 0.5 to face, then offset inward/outward
                z += BEAM_OFFSET_X; // X offset becomes Z for west
                break;
            case EAST:
                x += 0.25 + BEAM_OFFSET_Z; // 0.5 to face, then offset inward/outward
                z += BEAM_OFFSET_X; // X offset becomes Z for east
                break;
            case UP:
            case DOWN:
                // Not used for horizontal-facing blocks
                break;
        }
        
        return new net.minecraft.world.phys.Vec3(x, y, z);
    }
}

