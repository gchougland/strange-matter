package com.hexvane.strangematter.advancement;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class CompleteResearchCategoryTrigger extends SimpleCriterionTrigger<CompleteResearchCategoryTrigger.TriggerInstance> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "complete_research_category");

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, String researchCategory) {
        this.trigger(player, instance -> instance.matches(researchCategory));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> researchCategory) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.optionalFieldOf("research_category").forGetter(TriggerInstance::researchCategory)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(String category) {
            return this.researchCategory.map(c -> c.equals(category)).orElse(true);
        }

        public static Criterion<TriggerInstance> completeResearchCategory(String category) {
            return new Criterion<>(new CompleteResearchCategoryTrigger(), new TriggerInstance(Optional.empty(), Optional.of(category)));
        }
    }
}
