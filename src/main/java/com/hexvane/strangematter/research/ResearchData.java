package com.hexvane.strangematter.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

public class ResearchData {
    private final Map<ResearchType, Integer> researchPoints;
    private final Set<String> scannedObjects;
    private final Set<String> unlockedResearch;
    private ResearchDataManager manager;
    private UUID playerId;
    
    public ResearchData() {
        this.researchPoints = new HashMap<>();
        this.scannedObjects = new HashSet<>();
        this.unlockedResearch = new HashSet<>();
        
        // Initialize all research types to 0
        for (ResearchType type : ResearchType.values()) {
            researchPoints.put(type, 0);
        }
        
        // Initialize default unlocked research
        initializeDefaultUnlockedResearch();
    }
    
    private void initializeDefaultUnlockedResearch() {
        // Unlock default research nodes by default
        unlockResearch("research");
        unlockResearch("field_scanner");
        unlockResearch("anomaly_shards");
        unlockResearch("anomaly_types");
        unlockResearch("resonite");
        unlockResearch("resonant_energy");
    }
    
    public void setManager(ResearchDataManager manager, UUID playerId) {
        this.manager = manager;
        this.playerId = playerId;
    }
    
    public int getResearchPoints(ResearchType type) {
        return researchPoints.getOrDefault(type, 0);
    }
    
    public void addResearchPoints(ResearchType type, int amount) {
        int current = researchPoints.getOrDefault(type, 0);
        researchPoints.put(type, current + amount);
        markDirty();
    }
    
    public void spendResearchPoints(ResearchType type, int amount) {
        int current = researchPoints.getOrDefault(type, 0);
        researchPoints.put(type, Math.max(0, current - amount));
        markDirty();
    }
    
    public boolean hasScanned(String objectId) {
        return scannedObjects.contains(objectId);
    }
    
    public void markAsScanned(String objectId) {
        scannedObjects.add(objectId);
        markDirty();
    }
    
    public Set<String> getScannedObjects() {
        return new HashSet<>(scannedObjects);
    }
    
    public void setScannedObjects(Set<String> scanned) {
        scannedObjects.clear();
        scannedObjects.addAll(scanned);
        markDirty();
    }
    
    public boolean hasUnlockedResearch(String researchId) {
        return unlockedResearch.contains(researchId);
    }
    
    public void unlockResearch(String researchId) {
        unlockedResearch.add(researchId);
        markDirty();
    }
    
    public Set<String> getUnlockedResearch() {
        return new HashSet<>(unlockedResearch);
    }
    
    public void setUnlockedResearch(Set<String> unlocked) {
        unlockedResearch.clear();
        unlockedResearch.addAll(unlocked);
        markDirty();
    }
    
    private void markDirty() {
        if (manager != null) {
            manager.markDirty();
        }
    }
    
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        
        // Serialize research points
        CompoundTag researchTag = new CompoundTag();
        for (Map.Entry<ResearchType, Integer> entry : researchPoints.entrySet()) {
            researchTag.putInt(entry.getKey().getName(), entry.getValue());
        }
        tag.put("research_points", researchTag);
        
        // Serialize scanned objects
        ListTag scannedTag = new ListTag();
        for (String objectId : scannedObjects) {
            CompoundTag objectTag = new CompoundTag();
            objectTag.putString("id", objectId);
            scannedTag.add(objectTag);
        }
        tag.put("scanned_objects", scannedTag);
        
        // Serialize unlocked research
        ListTag unlockedTag = new ListTag();
        for (String researchId : unlockedResearch) {
            CompoundTag unlockedResearchTag = new CompoundTag();
            unlockedResearchTag.putString("id", researchId);
            unlockedTag.add(unlockedResearchTag);
        }
        tag.put("unlocked_research", unlockedTag);
        
        return tag;
    }
    
    public void deserializeNBT(CompoundTag tag) {
        researchPoints.clear();
        scannedObjects.clear();
        unlockedResearch.clear();
        
        // Deserialize research points
        if (tag.contains("research_points")) {
            CompoundTag researchTag = tag.getCompound("research_points");
            for (ResearchType type : ResearchType.values()) {
                if (researchTag.contains(type.getName())) {
                    researchPoints.put(type, researchTag.getInt(type.getName()));
                } else {
                    researchPoints.put(type, 0);
                }
            }
        }
        
        // Deserialize scanned objects
        if (tag.contains("scanned_objects")) {
            ListTag scannedTag = tag.getList("scanned_objects", Tag.TAG_COMPOUND);
            for (Tag t : scannedTag) {
                if (t instanceof CompoundTag objectTag) {
                    scannedObjects.add(objectTag.getString("id"));
                }
            }
        }
        
        // Deserialize unlocked research
        if (tag.contains("unlocked_research")) {
            ListTag unlockedTag = tag.getList("unlocked_research", Tag.TAG_COMPOUND);
            for (Tag t : unlockedTag) {
                if (t instanceof CompoundTag researchTag) {
                    unlockedResearch.add(researchTag.getString("id"));
                }
            }
        }
        
        // Ensure default unlocked research is always present (for new and existing players)
        initializeDefaultUnlockedResearch();
    }
    
    public void syncToClient(ServerPlayer player) {
        ResearchDataServerHandler.syncResearchDataToClient(player, this);
    }
    
    public static ResearchData get(Player player) {
        if (player.level().isClientSide) {
            // Return empty data for client - real data comes from network sync
            return new ResearchData();
        }
        return ResearchDataManager.get(player);
    }
}
