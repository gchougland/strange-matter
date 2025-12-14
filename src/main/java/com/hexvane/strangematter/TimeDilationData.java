package com.hexvane.strangematter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimeDilationData {
    // Static map to store time dilation slowdown factor for each player
    // This will be accessible from both client and server
    private static final Map<UUID, Double> playerSlowdownFactors = new HashMap<>();
    
    public static void setPlayerSlowdownFactor(UUID playerId, double factor) {
        if (factor > 0) {
            playerSlowdownFactors.put(playerId, factor);
        } else {
            playerSlowdownFactors.remove(playerId);
        }
    }
    
    public static double getPlayerSlowdownFactor(UUID playerId) {
        return playerSlowdownFactors.getOrDefault(playerId, 1.0);
    }
    
    public static void removePlayerSlowdownFactor(UUID playerId) {
        playerSlowdownFactors.remove(playerId);
    }
    
    public static boolean hasPlayerSlowdownFactor(UUID playerId) {
        return playerSlowdownFactors.containsKey(playerId);
    }
}

