package com.hexvane.strangematter.research;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public abstract class AnomalousEvent {
    
    protected final String eventId;
    protected final int severity; // 1-10 scale
    
    public AnomalousEvent(String eventId, int severity) {
        this.eventId = eventId;
        this.severity = Math.max(1, Math.min(10, severity));
    }
    
    /**
     * Triggers this anomalous event
     */
    public abstract void trigger(ServerLevel level, BlockPos machinePos, ServerPlayer player);
    
    /**
     * Gets the display name of this event
     */
    public abstract String getDisplayName();
    
    /**
     * Gets the description of what this event does
     */
    public abstract String getDescription();
    
    /**
     * Checks if this event can occur given the current conditions
     */
    public boolean canOccur(Level level, BlockPos machinePos, ServerPlayer player) {
        return true; // Default implementation allows all events
    }
    
    // Getters
    
    public String getEventId() {
        return eventId;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    /**
     * Get a random anomalous event based on severity and available events
     */
    public static AnomalousEvent getRandomEvent(Level level, BlockPos machinePos, ServerPlayer player, int maxSeverity) {
        // Placeholder - will implement specific events later
        return new TemporalDisruptionEvent();
    }
    
    // Placeholder event implementations
    public static class TemporalDisruptionEvent extends AnomalousEvent {
        public TemporalDisruptionEvent() {
            super("temporal_disruption", 3);
        }
        
        @Override
        public void trigger(ServerLevel level, BlockPos machinePos, ServerPlayer player) {
            // Placeholder - cause some temporal effect
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("research.strangematter.event.temporal_disruption.message"));
        }
        
        @Override
        public String getDisplayName() {
            return "Temporal Disruption";
        }
        
        @Override
        public String getDescription() {
            return "Time itself becomes unstable around the research machine.";
        }
    }
    
    public static class RealityFractureEvent extends AnomalousEvent {
        public RealityFractureEvent() {
            super("reality_fracture", 7);
        }
        
        @Override
        public void trigger(ServerLevel level, BlockPos machinePos, ServerPlayer player) {
            // Placeholder - cause some reality-bending effect
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("research.strangematter.event.reality_fracture.message"));
        }
        
        @Override
        public String getDisplayName() {
            return "Reality Fracture";
        }
        
        @Override
        public String getDescription() {
            return "The fabric of reality tears near the research machine.";
        }
    }
    
    public static class QuantumEntanglementEvent extends AnomalousEvent {
        public QuantumEntanglementEvent() {
            super("quantum_entanglement", 5);
        }
        
        @Override
        public void trigger(ServerLevel level, BlockPos machinePos, ServerPlayer player) {
            // Placeholder - cause some quantum effect
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("research.strangematter.event.quantum_entanglement.message"));
        }
        
        @Override
        public String getDisplayName() {
            return "Quantum Entanglement";
        }
        
        @Override
        public String getDescription() {
            return "Quantum particles become entangled, causing unpredictable effects.";
        }
    }
}
