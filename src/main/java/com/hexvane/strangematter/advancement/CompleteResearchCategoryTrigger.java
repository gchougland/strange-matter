package com.hexvane.strangematter.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class CompleteResearchCategoryTrigger extends SimpleCriterionTrigger<CompleteResearchCategoryTrigger.Instance> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("strangematter", "complete_research_category");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
        String researchCategory = json.has("research_category") ? json.get("research_category").getAsString() : null;
        return new Instance(predicate, researchCategory);
    }

    public void trigger(ServerPlayer player, String researchCategory) {
        this.trigger(player, instance -> instance.matches(researchCategory));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final String researchCategory;

        public Instance(ContextAwarePredicate predicate, String researchCategory) {
            super(ID, predicate);
            this.researchCategory = researchCategory;
        }

        public boolean matches(String researchCategory) {
            if (this.researchCategory == null) {
                return true; // No specific category required
            }
            
            return this.researchCategory.equals(researchCategory);
        }
    }
}
