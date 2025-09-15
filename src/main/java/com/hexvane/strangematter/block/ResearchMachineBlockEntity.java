package com.hexvane.strangematter.block;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.hexvane.strangematter.StrangeMatterMod;

import java.util.*;

public class ResearchMachineBlockEntity extends BlockEntity {
    
    public enum MachineState {
        IDLE,           // No research note inserted
        READY,          // Research note inserted, ready to begin
        RESEARCHING,    // Research in progress
        COMPLETED,      // Research completed successfully
        FAILED          // Research failed due to anomaly
    }
    
    private MachineState currentState = MachineState.IDLE;
    private String currentResearchId = "";
    private Set<ResearchType> activeResearchTypes = new HashSet<>();
    private float instabilityLevel = 0.5f; // 0.0 = success, 1.0 = failure
    private int researchTicks = 0;
    private java.util.UUID playerId = null; // Track who inserted the research note
    
    public ResearchMachineBlockEntity(BlockPos pos, BlockState state) {
        super(StrangeMatterMod.RESEARCH_MACHINE_BLOCK_ENTITY.get(), pos, state);
    }
    
    /**
     * Insert a research note into the machine
     */
    public boolean insertResearchNote(ItemStack researchNote, net.minecraft.world.entity.player.Player player) {
        if (currentState != MachineState.IDLE) {
            return false; // Machine already has a research note
        }
        
        if (com.hexvane.strangematter.item.ResearchNoteItem.isValidResearchNote(researchNote)) {
            currentResearchId = com.hexvane.strangematter.item.ResearchNoteItem.getResearchId(researchNote);
            activeResearchTypes = com.hexvane.strangematter.item.ResearchNoteItem.getResearchTypes(researchNote);
            currentState = MachineState.READY;
            instabilityLevel = 0.5f; // Start at halfway
            researchTicks = 0;
            playerId = player.getUUID();
            setChanged();
            
            // Play insert sound
            if (level != null) {
                level.playSound(null, worldPosition, StrangeMatterSounds.RESEARCH_MACHINE_NOTE_INSERT.get(), 
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 1.0f);
            }
            
            // Send packet to sync with client
            sendSyncPacket();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Begin the research process
     */
    public void beginResearch() {
        if (currentState == MachineState.READY) {
            currentState = MachineState.RESEARCHING;
            researchTicks = 0;
            setChanged();
        }
    }
    
    
    /**
     * Handle research completion - unlock research and reset machine
     */
    public void handleResearchCompletion() {
        if (playerId != null && !level.isClientSide) {
            net.minecraft.world.entity.player.Player player = level.getPlayerByUUID(playerId);
            if (player != null) {
                // Unlock the research for the player
                com.hexvane.strangematter.research.ResearchData researchData = 
                    com.hexvane.strangematter.research.ResearchData.get(player);
                researchData.unlockResearch(currentResearchId);
                researchData.syncToClient((net.minecraft.server.level.ServerPlayer) player);
                
                // Send success message
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "block.strangematter.research_machine.research_completed_success", 
                    com.hexvane.strangematter.research.ResearchNodeRegistry.getNode(currentResearchId).getName()));
                
                // Play success sound and particles at the machine location
                level.playSound(null, worldPosition, StrangeMatterSounds.RESEARCH_MACHINE_SUCCESS.get(), 
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 1.0f);
                
                // Spawn success particles
                for (int i = 0; i < 20; i++) {
                    double offsetX = (level.getRandom().nextDouble() - 0.5) * 2.0;
                    double offsetY = level.getRandom().nextDouble() * 1.5;
                    double offsetZ = (level.getRandom().nextDouble() - 0.5) * 2.0;
                    
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT, 
                        worldPosition.getX() + 0.5 + offsetX, 
                        worldPosition.getY() + 0.5 + offsetY, 
                        worldPosition.getZ() + 0.5 + offsetZ, 
                        0.0, 0.1, 0.0);
                }
            }
        }
        
        // Set state to completed and send immediate sync packet
        currentState = MachineState.COMPLETED;
        setChanged();
        sendSyncPacket();
        
        // Reset machine after a delay (5 seconds)
        level.getServer().tell(new net.minecraft.server.TickTask(100, () -> {
            if (!level.isClientSide) {
                clearResearch();
                sendSyncPacket();
            }
        }));
    }
    
    /**
     * Handle research failure - trigger anomalous event and reset machine
     */
    public void handleResearchFailure() {
        if (playerId != null && !level.isClientSide) {
            net.minecraft.world.entity.player.Player player = level.getPlayerByUUID(playerId);
            if (player != null) {
                // Send failure message
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "block.strangematter.research_machine.research_failed"));
            }
        }
        
        // Play failure sound
        if (level != null) {
            level.playSound(null, worldPosition, StrangeMatterSounds.RESEARCH_MACHINE_FAILURE.get(), 
                net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 1.0f);
        }
        
        // Set state to failed and send immediate sync packet
        currentState = MachineState.FAILED;
        setChanged();
        sendSyncPacket();
        
        // Reset machine after a delay (3 seconds)
        level.getServer().tell(new net.minecraft.server.TickTask(60, () -> {
            if (!level.isClientSide) {
                clearResearch();
                sendSyncPacket();
            }
        }));
    }
    
    /**
     * Clear the current research and return to idle state
     */
    public void clearResearch() {
        currentState = MachineState.IDLE;
        currentResearchId = "";
        activeResearchTypes.clear();
        instabilityLevel = 0.5f;
        researchTicks = 0;
        playerId = null;
        setChanged();
    }
    
    // Getters
    
    public MachineState getCurrentState() {
        return currentState;
    }
    
    public String getCurrentResearchId() {
        return currentResearchId;
    }
    
    public Set<ResearchType> getActiveResearchTypes() {
        return activeResearchTypes;
    }
    
    public float getInstabilityLevel() {
        return instabilityLevel;
    }
    
    public int getResearchTicks() {
        return researchTicks;
    }
    
    public boolean canInsertResearchNote() {
        return currentState == MachineState.IDLE;
    }
    
    public boolean canBeginResearch() {
        return currentState == MachineState.READY;
    }
    
    /**
     * Set the client-side state for GUI synchronization
     */
    public void setClientState(MachineState state, String researchId, Set<ResearchType> activeTypes, 
                              float instability, int ticks) {
        this.currentState = state;
        this.currentResearchId = researchId;
        this.activeResearchTypes = new HashSet<>(activeTypes);
        this.instabilityLevel = instability;
        this.researchTicks = ticks;
    }
    
    /**
     * Set the client-side instability level (for client-side updates)
     */
    public void setClientInstabilityLevel(float instability) {
        this.instabilityLevel = instability;
    }
    
    /**
     * Deactivate all minigames (for both client and server)
     */
    public void deactivateAllMinigames() {
        // This method can be called from both client and server
        // The actual minigame deactivation happens in the GUI on client side
        // This method is here for consistency and potential future use
    }
    
    /**
     * Send sync packet to clients
     */
    private void sendSyncPacket() {
        if (!level.isClientSide) {
            com.hexvane.strangematter.network.ResearchMachineSyncPacket packet = 
                new com.hexvane.strangematter.network.ResearchMachineSyncPacket(
                    worldPosition, currentState, currentResearchId, activeResearchTypes, 
                    instabilityLevel, researchTicks);
            
            // Send to all players tracking this chunk
            com.hexvane.strangematter.network.NetworkHandler.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.TRACKING_CHUNK.with(
                    () -> level.getChunkAt(worldPosition)
                ),
                packet
            );
        }
    }
    
    // NBT serialization
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("state", currentState.name());
        tag.putString("research_id", currentResearchId);
        tag.putFloat("instability", instabilityLevel);
        tag.putInt("research_ticks", researchTicks);
        
        // Save player ID
        if (playerId != null) {
            tag.putUUID("player_id", playerId);
        }
        
        // Save active research types
        CompoundTag typesTag = new CompoundTag();
        for (ResearchType type : activeResearchTypes) {
            typesTag.putBoolean(type.name(), true);
        }
        tag.put("active_types", typesTag);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        
        // Safely load state with fallback to IDLE
        try {
            String stateStr = tag.getString("state");
            if (!stateStr.isEmpty()) {
                currentState = MachineState.valueOf(stateStr);
            } else {
                currentState = MachineState.IDLE;
            }
        } catch (IllegalArgumentException e) {
            currentState = MachineState.IDLE;
        }
        
        // Load research ID with fallback to empty string
        currentResearchId = tag.getString("research_id");
        if (currentResearchId == null) {
            currentResearchId = "";
        }
        
        // Load instability level with fallback to 0.5f
        if (tag.contains("instability")) {
            instabilityLevel = tag.getFloat("instability");
        } else {
            instabilityLevel = 0.5f;
        }
        
        // Load research ticks with fallback to 0
        if (tag.contains("research_ticks")) {
            researchTicks = tag.getInt("research_ticks");
        } else {
            researchTicks = 0;
        }
        
        // Load player ID
        if (tag.contains("player_id")) {
            playerId = tag.getUUID("player_id");
        } else {
            playerId = null;
        }
        
        // Load active research types with error handling
        activeResearchTypes.clear();
        if (tag.contains("active_types")) {
            CompoundTag typesTag = tag.getCompound("active_types");
            for (String typeName : typesTag.getAllKeys()) {
                if (typesTag.getBoolean(typeName)) {
                    try {
                        activeResearchTypes.add(ResearchType.valueOf(typeName));
                    } catch (IllegalArgumentException e) {
                        // Skip invalid research types
                        // Skip invalid research types silently
                    }
                }
            }
        }
        
        // Ensure state consistency
        if (currentState == MachineState.IDLE && (!currentResearchId.isEmpty() || !activeResearchTypes.isEmpty())) {
            // If we have research data but state is IDLE, we should be READY
            currentState = MachineState.READY;
        } else if (currentState != MachineState.IDLE && currentResearchId.isEmpty()) {
            // If we don't have research data but state isn't IDLE, reset to IDLE
            currentState = MachineState.IDLE;
            activeResearchTypes.clear();
        }
    }
}
