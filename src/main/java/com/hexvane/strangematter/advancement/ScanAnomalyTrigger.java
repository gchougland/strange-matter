package com.hexvane.strangematter.advancement;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class ScanAnomalyTrigger extends SimpleCriterionTrigger<ScanAnomalyTrigger.TriggerInstance> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "scan_anomaly");

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Entity anomaly) {
        this.trigger(player, instance -> instance.matches(anomaly));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> anomalyType) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.optionalFieldOf("anomaly_type").forGetter(TriggerInstance::anomalyType)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(Entity anomaly) {
            return this.anomalyType.map(type -> anomaly.getType().toString().equals(type)).orElse(true);
        }

        public static Criterion<TriggerInstance> scanAnomaly(String anomalyType) {
            return new Criterion<>(new ScanAnomalyTrigger(), new TriggerInstance(Optional.empty(), Optional.of(anomalyType)));
        }
    }
}
