package com.hexvane.strangematter.sound;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class StrangeMatterSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, StrangeMatterMod.MODID);

    // Research Tablet Sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_TABLET_OPEN = registerSoundEvent("research_tablet_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_TABLET_NODE_HOVER = registerSoundEvent("research_tablet_node_hover");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_TABLET_NODE_CLICK = registerSoundEvent("research_tablet_node_click");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_TABLET_PAGE_TURN = registerSoundEvent("research_tablet_page_turn");
    
    // Research Node Sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_NODE_LOCKED_CLICK = registerSoundEvent("research_node_locked_click");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_NOTE_CREATE = registerSoundEvent("research_note_create");
    
    // Research Machine Sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_MACHINE_OPEN = registerSoundEvent("research_machine_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_MACHINE_NOTE_INSERT = registerSoundEvent("research_machine_note_insert");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_MACHINE_NOTE_REJECT = registerSoundEvent("research_machine_note_reject");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_MACHINE_BEGIN_RESEARCH = registerSoundEvent("research_machine_begin_research");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_MACHINE_IDLE_LOOP = registerSoundEvent("research_machine_idle_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_MACHINE_SUCCESS = registerSoundEvent("research_machine_success");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_MACHINE_FAILURE = registerSoundEvent("research_machine_failure");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_MACHINE_INSTABILITY_HIGH = registerSoundEvent("research_machine_instability_high");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_MACHINE_INSTABILITY_CRITICAL = registerSoundEvent("research_machine_instability_critical");
    
    // Energy Minigame Sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> ENERGY_DIAL_TOGGLE = registerSoundEvent("energy_dial_toggle");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENERGY_BUTTON_CLICK = registerSoundEvent("energy_button_click");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENERGY_WAVE_ALIGN = registerSoundEvent("energy_wave_align");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENERGY_WAVE_MISALIGN = registerSoundEvent("energy_wave_misalign");
    
    // General Minigame Sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> MINIGAME_STABLE = registerSoundEvent("minigame_stable");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINIGAME_UNSTABLE = registerSoundEvent("minigame_unstable");
    
    // Environmental Sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> FIELD_SCANNER_SCAN = registerSoundEvent("field_scanner_scan");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_ANOMALY_LOOP = registerSoundEvent("gravity_anomaly_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENERGETIC_RIFT_LOOP = registerSoundEvent("energetic_rift_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> ECHOING_SHADOW_LOOP = registerSoundEvent("echoing_shadow_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARP_GATE_LOOP = registerSoundEvent("warp_gate_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> TEMPORAL_BLOOM_LOOP = registerSoundEvent("temporal_bloom_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> THOUGHTWELL_LOOP = registerSoundEvent("thoughtwell_loop");
    
    // Warp Gun sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> WARP_GUN_SHOOT = registerSoundEvent("warp_gun_shoot");
    
    // Echo Vacuum sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> ECHO_VACUUM_CHARGE_UP = registerSoundEvent("echo_vacuum_charge_up");
    public static final DeferredHolder<SoundEvent, SoundEvent> ECHO_VACUUM_FIRE_LOOP = registerSoundEvent("echo_vacuum_fire_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> ECHO_VACUUM_CHARGE_DOWN = registerSoundEvent("echo_vacuum_charge_down");
    public static final DeferredHolder<SoundEvent, SoundEvent> ECHO_VACUUM_CONTAIN = registerSoundEvent("echo_vacuum_contain");
    
    // Reality Forge sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> REALITY_FORGE_CRAFT = registerSoundEvent("reality_forge_craft");
    public static final DeferredHolder<SoundEvent, SoundEvent> REALITY_FORGE_INSERT = registerSoundEvent("reality_forge_insert");

    // Stasis Projector sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> STASIS_PROJECTOR_ON = registerSoundEvent("stasis_projector_on");
    public static final DeferredHolder<SoundEvent, SoundEvent> STASIS_PROJECTOR_OFF = registerSoundEvent("stasis_projector_off");

    // Graviton Hammer sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITON_CHARGEUP = registerSoundEvent("graviton_chargeup");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, name)));
    }
}
