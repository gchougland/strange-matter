package com.hexvane.strangematter.advancement;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCriteriaTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> CRITERIA_TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, "strangematter");

    public static final DeferredHolder<CriterionTrigger<?>, AnomalyEffectTrigger> ANOMALY_EFFECT_TRIGGER =
            CRITERIA_TRIGGERS.register("anomaly_effect", AnomalyEffectTrigger::new);
    
    public static final DeferredHolder<CriterionTrigger<?>, ScanAnomalyTrigger> SCAN_ANOMALY_TRIGGER =
            CRITERIA_TRIGGERS.register("scan_anomaly", ScanAnomalyTrigger::new);
    
    public static final DeferredHolder<CriterionTrigger<?>, CompleteResearchCategoryTrigger> COMPLETE_RESEARCH_CATEGORY_TRIGGER =
            CRITERIA_TRIGGERS.register("complete_research_category", CompleteResearchCategoryTrigger::new);
}
