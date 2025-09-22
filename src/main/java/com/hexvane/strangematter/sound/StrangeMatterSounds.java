package com.hexvane.strangematter.sound;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class StrangeMatterSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, StrangeMatterMod.MODID);

    // Research Tablet Sounds
    public static final RegistryObject<SoundEvent> RESEARCH_TABLET_OPEN = registerSoundEvent("research_tablet_open");
    public static final RegistryObject<SoundEvent> RESEARCH_TABLET_NODE_HOVER = registerSoundEvent("research_tablet_node_hover");
    public static final RegistryObject<SoundEvent> RESEARCH_TABLET_NODE_CLICK = registerSoundEvent("research_tablet_node_click");
    public static final RegistryObject<SoundEvent> RESEARCH_TABLET_PAGE_TURN = registerSoundEvent("research_tablet_page_turn");
    
    // Research Node Sounds
    public static final RegistryObject<SoundEvent> RESEARCH_NODE_LOCKED_CLICK = registerSoundEvent("research_node_locked_click");
    public static final RegistryObject<SoundEvent> RESEARCH_NOTE_CREATE = registerSoundEvent("research_note_create");
    
    // Research Machine Sounds
    public static final RegistryObject<SoundEvent> RESEARCH_MACHINE_OPEN = registerSoundEvent("research_machine_open");
    public static final RegistryObject<SoundEvent> RESEARCH_MACHINE_NOTE_INSERT = registerSoundEvent("research_machine_note_insert");
    public static final RegistryObject<SoundEvent> RESEARCH_MACHINE_NOTE_REJECT = registerSoundEvent("research_machine_note_reject");
    public static final RegistryObject<SoundEvent> RESEARCH_MACHINE_BEGIN_RESEARCH = registerSoundEvent("research_machine_begin_research");
    public static final RegistryObject<SoundEvent> RESEARCH_MACHINE_IDLE_LOOP = registerSoundEvent("research_machine_idle_loop");
    public static final RegistryObject<SoundEvent> RESEARCH_MACHINE_SUCCESS = registerSoundEvent("research_machine_success");
    public static final RegistryObject<SoundEvent> RESEARCH_MACHINE_FAILURE = registerSoundEvent("research_machine_failure");
    public static final RegistryObject<SoundEvent> RESEARCH_MACHINE_INSTABILITY_HIGH = registerSoundEvent("research_machine_instability_high");
    public static final RegistryObject<SoundEvent> RESEARCH_MACHINE_INSTABILITY_CRITICAL = registerSoundEvent("research_machine_instability_critical");
    
    // Energy Minigame Sounds
    public static final RegistryObject<SoundEvent> ENERGY_DIAL_TOGGLE = registerSoundEvent("energy_dial_toggle");
    public static final RegistryObject<SoundEvent> ENERGY_BUTTON_CLICK = registerSoundEvent("energy_button_click");
    public static final RegistryObject<SoundEvent> ENERGY_WAVE_ALIGN = registerSoundEvent("energy_wave_align");
    public static final RegistryObject<SoundEvent> ENERGY_WAVE_MISALIGN = registerSoundEvent("energy_wave_misalign");
    
    // General Minigame Sounds
    public static final RegistryObject<SoundEvent> MINIGAME_STABLE = registerSoundEvent("minigame_stable");
    public static final RegistryObject<SoundEvent> MINIGAME_UNSTABLE = registerSoundEvent("minigame_unstable");
    
    // Environmental Sounds
    public static final RegistryObject<SoundEvent> FIELD_SCANNER_SCAN = registerSoundEvent("field_scanner_scan");
    public static final RegistryObject<SoundEvent> GRAVITY_ANOMALY_LOOP = registerSoundEvent("gravity_anomaly_loop");
    public static final RegistryObject<SoundEvent> ENERGETIC_RIFT_LOOP = registerSoundEvent("energetic_rift_loop");
    public static final RegistryObject<SoundEvent> WARP_GATE_LOOP = registerSoundEvent("warp_gate_loop");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(StrangeMatterMod.MODID, name)));
    }
}
