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
    private java.util.UUID currentPlayerId = null; // Track who is currently using the machine
    private Map<ResearchType, Map<String, Object>> minigameStates = new HashMap<>(); // Save minigame states
    
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
            playerId = player.getUUID();
            
            // Check if minigames are disabled in config
            if (!com.hexvane.strangematter.Config.enableMinigames) {
                // Instant unlock - skip minigames entirely
                if (!level.isClientSide) {
                    // Use the same completion flow as normal research
                    handleResearchCompletion();
                }
                return true;
            }
            
            // Normal mode - require minigames
            currentState = MachineState.READY;
            instabilityLevel = 0.5f; // Start at halfway
            researchTicks = 0;
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
                // Check if research is already unlocked
                com.hexvane.strangematter.research.ResearchData researchData = 
                    com.hexvane.strangematter.research.ResearchData.get(player);
                boolean alreadyUnlocked = researchData.hasUnlockedResearch(currentResearchId);
                
                if (alreadyUnlocked) {
                    // Send message that research was already unlocked (for testing purposes)
                    player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                        "block.strangematter.research_machine.research_already_unlocked", 
                        com.hexvane.strangematter.research.ResearchNodeRegistry.getNode(currentResearchId).getDisplayName()));
                    
                    // Still play success sound and particles for consistency
                    level.playSound(null, worldPosition, StrangeMatterSounds.RESEARCH_MACHINE_SUCCESS.get(), 
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 1.0f);
                } else {
                    // Unlock the research for the player
                    researchData.unlockResearch(currentResearchId);
                    researchData.syncToClient((net.minecraft.server.level.ServerPlayer) player);
                    
                    // Trigger advancement for each research category used in this research
                    com.hexvane.strangematter.research.ResearchNode researchNode = com.hexvane.strangematter.research.ResearchNodeRegistry.getNode(currentResearchId);
                    if (researchNode != null && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        for (com.hexvane.strangematter.research.ResearchType researchType : researchNode.getResearchCosts().keySet()) {
                            StrangeMatterMod.COMPLETE_RESEARCH_CATEGORY_TRIGGER.trigger(serverPlayer, researchType.getName());
                        }
                    }
                    
                    // Send success message
                    player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                        "block.strangematter.research_machine.research_completed_success", 
                        com.hexvane.strangematter.research.ResearchNodeRegistry.getNode(currentResearchId).getDisplayName()));
                    
                    // Play success sound and particles at the machine location
                    level.playSound(null, worldPosition, StrangeMatterSounds.RESEARCH_MACHINE_SUCCESS.get(), 
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 1.0f);
                }
                
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
     * Creates a research note ItemStack from the current stored research data
     */
    private ItemStack createResearchNoteFromStoredData() {
        if (currentResearchId.isEmpty() || activeResearchTypes.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        // Create a map of research types with default costs (since we don't store the original costs)
        Map<com.hexvane.strangematter.research.ResearchType, Integer> researchCosts = new HashMap<>();
        for (com.hexvane.strangematter.research.ResearchType type : activeResearchTypes) {
            researchCosts.put(type, 1); // Default cost of 1
        }
        
        return com.hexvane.strangematter.item.ResearchNoteItem.createResearchNote(researchCosts, currentResearchId);
    }
    
    /**
     * Drops the current research note at the machine's position
     */
    private void dropResearchNote() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        ItemStack researchNote = createResearchNoteFromStoredData();
        if (!researchNote.isEmpty()) {
            // Drop the research note at the machine's position
            net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                level, 
                worldPosition.getX() + 0.5, 
                worldPosition.getY() + 1.0, 
                worldPosition.getZ() + 0.5, 
                researchNote
            );
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
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
        
        // Drop the research note before clearing
        dropResearchNote();
        
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
    public void sendSyncPacket() {
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
        
        // Note: We don't need to save facing to NBT - the blockstate handles it
        // and the structure system will rotate it correctly when placing structures
        
        // Save player ID
        if (playerId != null) {
            tag.putUUID("player_id", playerId);
        }
        
        // Save current player ID
        if (currentPlayerId != null) {
            tag.putUUID("current_player_id", currentPlayerId);
        }
        
        // Save active research types
        CompoundTag typesTag = new CompoundTag();
        for (ResearchType type : activeResearchTypes) {
            typesTag.putBoolean(type.name(), true);
        }
        tag.put("active_types", typesTag);
        
        // Save minigame states
        CompoundTag minigameStatesTag = new CompoundTag();
        for (Map.Entry<ResearchType, Map<String, Object>> entry : minigameStates.entrySet()) {
            CompoundTag stateTag = new CompoundTag();
            Map<String, Object> state = entry.getValue();
            
            for (Map.Entry<String, Object> stateEntry : state.entrySet()) {
                Object value = stateEntry.getValue();
                if (value instanceof Boolean) {
                    stateTag.putBoolean(stateEntry.getKey(), (Boolean) value);
                } else if (value instanceof String) {
                    stateTag.putString(stateEntry.getKey(), (String) value);
                } else if (value instanceof Double) {
                    stateTag.putDouble(stateEntry.getKey(), (Double) value);
                } else if (value instanceof Integer) {
                    stateTag.putInt(stateEntry.getKey(), (Integer) value);
                } else {
                    stateTag.putString(stateEntry.getKey(), value.toString());
                }
            }
            
            minigameStatesTag.put(entry.getKey().name(), stateTag);
        }
        tag.put("minigame_states", minigameStatesTag);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        
        // Note: We DON'T restore facing from NBT here because the structure system
        // handles rotation automatically when placing structures. The blockstate will
        // already be correctly rotated by the time this loads.
        
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
        
        // Load current player ID
        if (tag.contains("current_player_id")) {
            currentPlayerId = tag.getUUID("current_player_id");
        } else {
            currentPlayerId = null;
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
        
        // Load minigame states
        minigameStates.clear();
        if (tag.contains("minigame_states")) {
            CompoundTag minigameStatesTag = tag.getCompound("minigame_states");
            for (String typeName : minigameStatesTag.getAllKeys()) {
                try {
                    ResearchType type = ResearchType.valueOf(typeName);
                    CompoundTag stateTag = minigameStatesTag.getCompound(typeName);
                    Map<String, Object> state = new HashMap<>();
                    
                    for (String key : stateTag.getAllKeys()) {
                        Object value;
                        if (stateTag.contains(key, net.minecraft.nbt.Tag.TAG_BYTE)) {
                            value = stateTag.getBoolean(key);
                        } else if (stateTag.contains(key, net.minecraft.nbt.Tag.TAG_STRING)) {
                            value = stateTag.getString(key);
                        } else if (stateTag.contains(key, net.minecraft.nbt.Tag.TAG_DOUBLE)) {
                            value = stateTag.getDouble(key);
                        } else if (stateTag.contains(key, net.minecraft.nbt.Tag.TAG_INT)) {
                            value = stateTag.getInt(key);
                        } else {
                            value = stateTag.getString(key);
                        }
                        
                        state.put(key, value);
                    }
                    
                    minigameStates.put(type, state);
                } catch (IllegalArgumentException e) {
                    // Skip invalid research types
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
    
    // Player locking methods
    public boolean isPlayerLocked(java.util.UUID playerId) {
        return currentPlayerId != null && !currentPlayerId.equals(playerId);
    }
    
    public void lockToPlayer(java.util.UUID playerId) {
        this.currentPlayerId = playerId;
        setChanged();
    }
    
    public void unlockPlayer() {
        this.currentPlayerId = null;
        setChanged();
    }
    
    public java.util.UUID getCurrentPlayerId() {
        return currentPlayerId;
    }
    
    // Minigame state persistence methods
    public Map<ResearchType, Map<String, Object>> getMinigameStates() {
        return minigameStates;
    }
    
    public void setMinigameStates(Map<ResearchType, Map<String, Object>> states) {
        this.minigameStates = new HashMap<>(states);
        setChanged();
    }
    
    // Setter methods for client-side updates
    public void setCurrentState(MachineState state) {
        this.currentState = state;
    }
    
    public void setCurrentResearchId(String researchId) {
        this.currentResearchId = researchId;
    }
    
    public void setActiveResearchTypes(Set<ResearchType> activeTypes) {
        this.activeResearchTypes = new HashSet<>(activeTypes);
    }
    
    public void setInstabilityLevel(float instabilityLevel) {
        this.instabilityLevel = instabilityLevel;
    }
    
    public void setResearchTicks(int researchTicks) {
        this.researchTicks = researchTicks;
    }
    
    // Client-side state update method
    public void setClientState(MachineState state, String researchId, Set<ResearchType> activeTypes, 
                              float instabilityLevel, int researchTicks) {
        this.currentState = state;
        this.currentResearchId = researchId;
        this.activeResearchTypes = new HashSet<>(activeTypes);
        this.instabilityLevel = instabilityLevel;
        this.researchTicks = researchTicks;
    }
    
}
