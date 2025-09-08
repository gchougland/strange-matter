package com.hexvane.strangematter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GravityData {
    // Static map to store gravity force for each player
    // This will be accessible from both client and server
    private static final Map<UUID, Double> playerGravityForces = new HashMap<>();
    
    public static void setPlayerGravityForce(UUID playerId, double force) {
        playerGravityForces.put(playerId, force);
    }
    
    public static double getPlayerGravityForce(UUID playerId) {
        return playerGravityForces.getOrDefault(playerId, 0.0);
    }
    
    public static void removePlayerGravityForce(UUID playerId) {
        playerGravityForces.remove(playerId);
    }
    
    public static boolean hasPlayerGravityForce(UUID playerId) {
        return playerGravityForces.containsKey(playerId);
    }
}
