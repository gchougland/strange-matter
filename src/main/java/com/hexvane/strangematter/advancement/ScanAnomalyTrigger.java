package com.hexvane.strangematter.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ScanAnomalyTrigger extends SimpleCriterionTrigger<ScanAnomalyTrigger.Instance> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "scan_anomaly");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
        String anomalyType = json.has("anomaly_type") ? json.get("anomaly_type").getAsString() : null;
        return new Instance(predicate, anomalyType);
    }

    public void trigger(ServerPlayer player, Entity anomaly) {
        this.trigger(player, instance -> instance.matches(anomaly));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final String anomalyType;

        public Instance(ContextAwarePredicate predicate, String anomalyType) {
            super(ID, predicate);
            this.anomalyType = anomalyType;
        }

        public boolean matches(Entity anomaly) {
            if (anomalyType == null) {
                return true; // No specific type required
            }
            
            // Check if the anomaly matches the required type
            String entityType = anomaly.getType().toString();
            return entityType.equals(anomalyType);
        }
    }
}
