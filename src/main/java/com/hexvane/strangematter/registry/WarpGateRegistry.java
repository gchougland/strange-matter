package com.hexvane.strangematter.registry;

import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WarpGateRegistry {
    private static final Map<UUID, WarpGateEntry> warpGates = new ConcurrentHashMap<>();
    
    public static class WarpGateEntry {
        public final UUID uuid;
        public final Level level;
        public final Vec3 position;
        public final boolean isPaired;
        public final long spawnTime;
        
        public WarpGateEntry(UUID uuid, Level level, Vec3 position, boolean isPaired) {
            this.uuid = uuid;
            this.level = level;
            this.position = position;
            this.isPaired = isPaired;
            this.spawnTime = System.currentTimeMillis();
        }
        
        public void markAsPaired() {
            // Update the entry to mark as paired
            warpGates.put(uuid, new WarpGateEntry(uuid, level, position, true));
        }
    }
    
    /**
     * Register a new warp gate when it spawns
     */
    public static void registerWarpGate(WarpGateAnomalyEntity warpGate) {
        UUID uuid = warpGate.getUUID();
        Level level = warpGate.level();
        Vec3 position = warpGate.position();
        
        WarpGateEntry entry = new WarpGateEntry(uuid, level, position, false);
        warpGates.put(uuid, entry);
        
        System.out.println("WarpGateRegistry: Registered warp gate " + uuid + " at " + position + " in level " + level.dimension().location());
    }
    
    /**
     * Unregister a warp gate when it's removed
     */
    public static void unregisterWarpGate(UUID uuid) {
        WarpGateEntry removed = warpGates.remove(uuid);
        if (removed != null) {
            System.out.println("WarpGateRegistry: Unregistered warp gate " + uuid + " at " + removed.position);
        }
    }
    
    /**
     * Find an unpaired warp gate that's far enough away from the given position
     */
    public static WarpGateEntry findUnpairedWarpGate(Level level, Vec3 fromPosition, double minDistance) {
        List<WarpGateEntry> candidates = new ArrayList<>();
        
        for (WarpGateEntry entry : warpGates.values()) {
            // Must be in the same level
            if (!entry.level.equals(level)) continue;
            
            // Must be unpaired
            if (entry.isPaired) continue;
            
            // Must be far enough away
            double distance = entry.position.distanceTo(fromPosition);
            if (distance < minDistance) continue;
            
            // Skip the same gate (shouldn't happen, but safety check)
            if (entry.position.equals(fromPosition)) continue;
            
            candidates.add(entry);
        }
        
        if (candidates.isEmpty()) {
            System.out.println("WarpGateRegistry: No unpaired warp gates found in level " + level.dimension().location());
            return null;
        }
        
        // Sort by distance and pick a random one from the top candidates
        candidates.sort((a, b) -> Double.compare(
            a.position.distanceTo(fromPosition),
            b.position.distanceTo(fromPosition)
        ));
        
        // Pick randomly from the top 3 candidates (if there are 3 or more)
        int maxCandidates = Math.min(3, candidates.size());
        WarpGateEntry selected = candidates.get(new Random().nextInt(maxCandidates));
        
        System.out.println("WarpGateRegistry: Found " + candidates.size() + " unpaired candidates, selected gate at " + selected.position + " (distance: " + selected.position.distanceTo(fromPosition) + ")");
        
        return selected;
    }
    
    /**
     * Mark a warp gate as paired
     */
    public static void markWarpGateAsPaired(UUID uuid) {
        WarpGateEntry entry = warpGates.get(uuid);
        if (entry != null) {
            entry.markAsPaired();
            System.out.println("WarpGateRegistry: Marked warp gate " + uuid + " as paired");
        }
    }
    
    /**
     * Get all warp gates in a specific level
     */
    public static List<WarpGateEntry> getWarpGatesInLevel(Level level) {
        return warpGates.values().stream()
            .filter(entry -> entry.level.equals(level))
            .toList();
    }
    
    /**
     * Get debug information about the registry
     */
    public static String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("WarpGateRegistry Debug Info:\n");
        sb.append("Total registered gates: ").append(warpGates.size()).append("\n");
        
        for (WarpGateEntry entry : warpGates.values()) {
            sb.append("- UUID: ").append(entry.uuid)
              .append(", Level: ").append(entry.level.dimension().location())
              .append(", Position: ").append(entry.position)
              .append(", Paired: ").append(entry.isPaired)
              .append(", Age: ").append(System.currentTimeMillis() - entry.spawnTime).append("ms\n");
        }
        
        return sb.toString();
    }
}
