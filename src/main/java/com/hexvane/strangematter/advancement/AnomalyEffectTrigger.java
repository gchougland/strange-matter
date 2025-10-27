package com.hexvane.strangematter.advancement;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class AnomalyEffectTrigger extends SimpleCriterionTrigger<AnomalyEffectTrigger.TriggerInstance> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "anomaly_effect_applied");

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> anomalyEffect() {
            return new Criterion<>(new AnomalyEffectTrigger(), new TriggerInstance(Optional.empty()));
        }
    }
}
