package com.hexvane.strangematter.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResearchDataManager extends SavedData {
    private static final String DATA_NAME = "strangematter_research_data";
    private final Map<UUID, ResearchData> playerResearchData;
    
    public ResearchDataManager() {
        this.playerResearchData = new HashMap<>();
    }
    
    public static ResearchDataManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            ResearchDataManager::load,
            ResearchDataManager::new,
            DATA_NAME
        );
    }
    
    public static ResearchData get(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            ResearchDataManager manager = get(serverLevel);
            return manager.getPlayerResearchData(player.getUUID());
        }
        return new ResearchData(); // Return empty data for client
    }
    
    public ResearchData getPlayerResearchData(UUID playerId) {
        return playerResearchData.computeIfAbsent(playerId, k -> {
            ResearchData data = new ResearchData();
            data.setManager(this, playerId);
            return data;
        });
    }
    
    public void markDirty() {
        setDirty();
    }
    
    public void setPlayerResearchData(UUID playerId, ResearchData data) {
        playerResearchData.put(playerId, data);
        setDirty();
    }
    
    @Override
    public CompoundTag save(@org.jetbrains.annotations.NotNull CompoundTag tag) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, ResearchData> entry : playerResearchData.entrySet()) {
            playersTag.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }
        tag.put("players", playersTag);
        return tag;
    }
    
    public static ResearchDataManager load(CompoundTag tag) {
        ResearchDataManager manager = new ResearchDataManager();
        
        if (tag.contains("players")) {
            CompoundTag playersTag = tag.getCompound("players");
            for (String key : playersTag.getAllKeys()) {
                try {
                    UUID playerId = UUID.fromString(key);
                    ResearchData data = new ResearchData();
                    data.deserializeNBT(playersTag.getCompound(key));
                    data.setManager(manager, playerId);
                    manager.playerResearchData.put(playerId, data);
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                }
            }
        }
        
        return manager;
    }
}
