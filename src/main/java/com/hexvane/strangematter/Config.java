package com.hexvane.strangematter;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;

/**
 * Configuration file for Strange Matter mod.
 * All configuration options are defined here and synced on load.
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ========================================
    // WORLD GENERATION - ANOMALY SPAWN RATES
    // ========================================
    
    private static final ModConfigSpec.BooleanValue ENABLE_GRAVITY_ANOMALY;
    private static final ModConfigSpec.IntValue GRAVITY_ANOMALY_RARITY;
    
    private static final ModConfigSpec.BooleanValue ENABLE_TEMPORAL_BLOOM;
    private static final ModConfigSpec.IntValue TEMPORAL_BLOOM_RARITY;
    
    private static final ModConfigSpec.BooleanValue ENABLE_WARP_GATE;
    private static final ModConfigSpec.IntValue WARP_GATE_RARITY;
    
    private static final ModConfigSpec.BooleanValue ENABLE_ENERGETIC_RIFT;
    private static final ModConfigSpec.IntValue ENERGETIC_RIFT_RARITY;
    
    private static final ModConfigSpec.BooleanValue ENABLE_ECHOING_SHADOW;
    private static final ModConfigSpec.IntValue ECHOING_SHADOW_RARITY;
    
    private static final ModConfigSpec.BooleanValue ENABLE_THOUGHTWELL;
    private static final ModConfigSpec.IntValue THOUGHTWELL_RARITY;

    // ========================================
    // WORLD GENERATION - ORE GENERATION
    // ========================================
    
    private static final ModConfigSpec.BooleanValue ENABLE_RESONITE_ORE;
    private static final ModConfigSpec.IntValue RESONITE_ORE_VEINS_PER_CHUNK;

    // ========================================
    // WORLD GENERATION - TERRAIN MODIFICATION
    // ========================================
    
    private static final ModConfigSpec.BooleanValue ENABLE_ANOMALOUS_GRASS;
    private static final ModConfigSpec.DoubleValue RESONITE_ORE_SPAWN_CHANCE_NEAR_ANOMALY;
    private static final ModConfigSpec.DoubleValue SHARD_ORE_SPAWN_CHANCE_NEAR_ANOMALY;


    // ========================================
    // WORLD GENERATION - VILLAGER STRUCTURES
    // ========================================
    
    private static final ModConfigSpec.BooleanValue ENABLE_ANOMALY_SCIENTIST_HOUSE;
    private static final ModConfigSpec.IntValue ANOMALY_SCIENTIST_HOUSE_WEIGHT;

    // ========================================
    // ANOMALY EFFECTS CONFIG VALUES
    // ========================================
    
    // Gravity Anomaly
    private static final ModConfigSpec.BooleanValue ENABLE_GRAVITY_EFFECTS;
    private static final ModConfigSpec.DoubleValue GRAVITY_LEVITATION_RADIUS;
    private static final ModConfigSpec.DoubleValue GRAVITY_LEVITATION_FORCE;
    private static final ModConfigSpec.IntValue GRAVITY_RESEARCH_POINTS;
    
    // Temporal Bloom
    private static final ModConfigSpec.BooleanValue ENABLE_TEMPORAL_EFFECTS;
    private static final ModConfigSpec.DoubleValue TEMPORAL_EFFECT_RADIUS;
    private static final ModConfigSpec.IntValue TEMPORAL_CROP_GROWTH_STAGES;
    private static final ModConfigSpec.IntValue TEMPORAL_CROP_COOLDOWN;
    private static final ModConfigSpec.IntValue TEMPORAL_MOB_COOLDOWN;
    private static final ModConfigSpec.IntValue TEMPORAL_RESEARCH_POINTS;
    
    // Energetic Rift
    private static final ModConfigSpec.BooleanValue ENABLE_ENERGETIC_EFFECTS;
    private static final ModConfigSpec.DoubleValue ENERGETIC_ZAP_RADIUS;
    private static final ModConfigSpec.DoubleValue ENERGETIC_LIGHTNING_RADIUS;
    private static final ModConfigSpec.DoubleValue ENERGETIC_ZAP_DAMAGE;
    private static final ModConfigSpec.IntValue ENERGETIC_ZAP_COOLDOWN;
    private static final ModConfigSpec.IntValue ENERGETIC_LIGHTNING_COOLDOWN;
    private static final ModConfigSpec.IntValue ENERGETIC_RESEARCH_POINTS;
    
    // Warp Gate
    private static final ModConfigSpec.BooleanValue ENABLE_WARP_EFFECTS;
    private static final ModConfigSpec.DoubleValue WARP_TELEPORT_RADIUS;
    private static final ModConfigSpec.IntValue WARP_TELEPORT_COOLDOWN;
    private static final ModConfigSpec.IntValue WARP_RESEARCH_POINTS;
    
    // Echoing Shadow
    private static final ModConfigSpec.BooleanValue ENABLE_SHADOW_EFFECTS;
    private static final ModConfigSpec.DoubleValue SHADOW_EFFECT_RADIUS;
    private static final ModConfigSpec.DoubleValue SHADOW_LIGHT_ABSORPTION;
    private static final ModConfigSpec.DoubleValue SHADOW_MOB_SPAWN_BOOST;
    private static final ModConfigSpec.IntValue SHADOW_RESEARCH_POINTS;
    
    // Thoughtwell
    private static final ModConfigSpec.BooleanValue ENABLE_THOUGHTWELL_EFFECTS;
    private static final ModConfigSpec.DoubleValue THOUGHTWELL_EFFECT_RADIUS;
    private static final ModConfigSpec.IntValue THOUGHTWELL_CONFUSION_DURATION;
    private static final ModConfigSpec.IntValue THOUGHTWELL_RESEARCH_POINTS;

    // ========================================
    // ENERGY SYSTEM CONFIG VALUES
    // ========================================
    
    // Resonant Burner
    private static final ModConfigSpec.IntValue RESONANT_BURNER_ENERGY_PER_TICK;
    private static final ModConfigSpec.IntValue RESONANT_BURNER_ENERGY_STORAGE;
    private static final ModConfigSpec.IntValue RESONANT_BURNER_TRANSFER_RATE;
    
    // Resonance Condenser
    private static final ModConfigSpec.IntValue RESONANCE_CONDENSER_ENERGY_PER_TICK;
    private static final ModConfigSpec.IntValue RESONANCE_CONDENSER_ENERGY_STORAGE;
    private static final ModConfigSpec.IntValue RESONANCE_CONDENSER_PROGRESS_SPEED;
    
    // Paradoxical Energy Cell
    private static final ModConfigSpec.IntValue PARADOXICAL_CELL_TRANSFER_RATE;
    
    // Reality Forge
    private static final ModConfigSpec.IntValue REALITY_FORGE_CRAFT_TIME;
    
    // Rift Stabilizer
    private static final ModConfigSpec.IntValue RIFT_STABILIZER_ENERGY_PER_TICK;
    private static final ModConfigSpec.IntValue RIFT_STABILIZER_ENERGY_STORAGE;
    private static final ModConfigSpec.IntValue RIFT_STABILIZER_TRANSFER_RATE;
    private static final ModConfigSpec.DoubleValue RIFT_STABILIZER_RADIUS;
    private static final ModConfigSpec.IntValue RIFT_STABILIZER_MAX_PER_RIFT;
    
    // Resonant Conduit
    private static final ModConfigSpec.IntValue RESONANT_CONDUIT_TRANSFER_RATE;
    private static final ModConfigSpec.IntValue RESONANT_CONDUIT_NETWORK_UPDATE_INTERVAL;
    private static final ModConfigSpec.IntValue RESONANT_CONDUIT_MAX_NETWORK_SIZE;
    private static final ModConfigSpec.DoubleValue RESONANT_CONDUIT_DISTANCE_PENALTY;

    // ========================================
    // RESEARCH SYSTEM CONFIG VALUES
    // ========================================
    
    private static final ModConfigSpec.DoubleValue RESEARCH_COST_MULTIPLIER;
    
    // Individual research node costs
    private static final ModConfigSpec.IntValue ANOMALY_RESONATOR_COST;
    private static final ModConfigSpec.IntValue RESONANCE_CONDENSER_COST;
    private static final ModConfigSpec.IntValue CONTAINMENT_BASICS_COST;
    private static final ModConfigSpec.IntValue ECHOFORM_IMPRINTER_COST;
    private static final ModConfigSpec.IntValue REALITY_FORGE_COST;
    private static final ModConfigSpec.IntValue WARP_GUN_COST;
    private static final ModConfigSpec.IntValue GRAVITON_HAMMER_COST;
    private static final ModConfigSpec.IntValue STASIS_PROJECTOR_COST;
    private static final ModConfigSpec.IntValue RIFT_STABILIZER_COST;
    private static final ModConfigSpec.IntValue GRAVITY_ANOMALIES_COST;
    private static final ModConfigSpec.IntValue TEMPORAL_ANOMALIES_COST;
    private static final ModConfigSpec.IntValue SPATIAL_ANOMALIES_COST;
    private static final ModConfigSpec.IntValue ENERGY_ANOMALIES_COST;
    private static final ModConfigSpec.IntValue SHADOW_ANOMALIES_COST;
    private static final ModConfigSpec.IntValue COGNITIVE_ANOMALIES_COST;
    private static final ModConfigSpec.IntValue LEVITATION_PAD_COST;
    private static final ModConfigSpec.IntValue LEVITATION_PAD_MAX_HEIGHT;
    private static final ModConfigSpec.IntValue HOVERBOARD_COST;
    private static final ModConfigSpec.DoubleValue HOVERBOARD_MAX_SPEED;
    private static final ModConfigSpec.DoubleValue HOVERBOARD_ACCELERATION;

    // ========================================
    // GRAVITON HAMMER CONFIG VALUES
    // ========================================
    
    private static final ModConfigSpec.IntValue GRAVITON_HAMMER_CHARGE_LEVEL_1_TIME;
    private static final ModConfigSpec.IntValue GRAVITON_HAMMER_CHARGE_LEVEL_2_TIME;
    private static final ModConfigSpec.IntValue GRAVITON_HAMMER_CHARGE_LEVEL_3_TIME;
    private static final ModConfigSpec.IntValue GRAVITON_HAMMER_TUNNEL_DEPTH_LEVEL_1;
    private static final ModConfigSpec.IntValue GRAVITON_HAMMER_TUNNEL_DEPTH_LEVEL_2;
    private static final ModConfigSpec.IntValue GRAVITON_HAMMER_TUNNEL_DEPTH_LEVEL_3;

    // ========================================
    // RESEARCH MINIGAMES CONFIG VALUES
    // ========================================
    
    private static final ModConfigSpec.BooleanValue ENABLE_MINIGAMES;
    
    // Instability
    private static final ModConfigSpec.DoubleValue INSTABILITY_DECREASE_RATE;
    private static final ModConfigSpec.DoubleValue INSTABILITY_BASE_INCREASE_RATE;
    
    // Energy Minigame
    private static final ModConfigSpec.IntValue ENERGY_REQUIRED_ALIGNMENT_TICKS;
    private static final ModConfigSpec.IntValue ENERGY_DRIFT_DELAY_TICKS;
    private static final ModConfigSpec.DoubleValue ENERGY_AMPLITUDE_STEP;
    private static final ModConfigSpec.DoubleValue ENERGY_PERIOD_STEP;
    
    // Space Minigame
    private static final ModConfigSpec.DoubleValue SPACE_STABILITY_THRESHOLD;
    private static final ModConfigSpec.IntValue SPACE_DRIFT_DELAY_TICKS;
    private static final ModConfigSpec.DoubleValue SPACE_WARP_ADJUSTMENT;
    
    // Time Minigame
    private static final ModConfigSpec.DoubleValue TIME_SPEED_THRESHOLD;
    private static final ModConfigSpec.IntValue TIME_DRIFT_DELAY_TICKS;
    private static final ModConfigSpec.DoubleValue TIME_SPEED_ADJUSTMENT;
    private static final ModConfigSpec.DoubleValue TIME_SNAP_THRESHOLD;
    
    // Gravity Minigame
    private static final ModConfigSpec.DoubleValue GRAVITY_BALANCE_THRESHOLD;
    private static final ModConfigSpec.IntValue GRAVITY_DRIFT_DELAY_TICKS;
    
    // Shadow Minigame
    private static final ModConfigSpec.DoubleValue SHADOW_ALIGNMENT_THRESHOLD;
    private static final ModConfigSpec.IntValue SHADOW_DRIFT_DELAY_TICKS;
    private static final ModConfigSpec.DoubleValue SHADOW_ROTATION_STEP;
    
    // Cognition Minigame
    private static final ModConfigSpec.IntValue COGNITION_MATCH_DURATION;
    private static final ModConfigSpec.IntValue COGNITION_DIFFICULTY;

    static {
        BUILDER.comment("World Generation Settings").push("worldgen");
        
        // Anomaly Spawn Rates
        BUILDER.comment("Anomaly Spawn Configuration",
                "Enable/disable individual anomaly types and configure their spawn rarity.",
                "Rarity values represent 1/N chance per chunk (higher = rarer).",
                "For example, rarity of 500 means 1 in 500 chunks will spawn this anomaly.")
                .push("anomalies");
        
        ENABLE_GRAVITY_ANOMALY = BUILDER
                .comment("Enable Gravity Anomaly world generation")
                .define("enableGravityAnomaly", true);
        GRAVITY_ANOMALY_RARITY = BUILDER
                .comment("Gravity Anomaly spawn rarity (1/N chance per chunk)")
                .defineInRange("gravityAnomalyRarity", 500, 1, 100000);
        
        ENABLE_TEMPORAL_BLOOM = BUILDER
                .comment("Enable Temporal Bloom world generation")
                .define("enableTemporalBloom", true);
        TEMPORAL_BLOOM_RARITY = BUILDER
                .comment("Temporal Bloom spawn rarity (1/N chance per chunk)")
                .defineInRange("temporalBloomRarity", 500, 1, 100000);
        
        ENABLE_WARP_GATE = BUILDER
                .comment("Enable Warp Gate world generation")
                .define("enableWarpGate", true);
        WARP_GATE_RARITY = BUILDER
                .comment("Warp Gate spawn rarity (1/N chance per chunk)")
                .defineInRange("warpGateRarity", 500, 1, 100000);
        
        ENABLE_ENERGETIC_RIFT = BUILDER
                .comment("Enable Energetic Rift world generation")
                .define("enableEnergeticRift", true);
        ENERGETIC_RIFT_RARITY = BUILDER
                .comment("Energetic Rift spawn rarity (1/N chance per chunk)")
                .defineInRange("energeticRiftRarity", 500, 1, 100000);
        
        ENABLE_ECHOING_SHADOW = BUILDER
                .comment("Enable Echoing Shadow world generation")
                .define("enableEchoingShadow", true);
        ECHOING_SHADOW_RARITY = BUILDER
                .comment("Echoing Shadow spawn rarity (1/N chance per chunk)")
                .defineInRange("echoingShadowRarity", 500, 1, 100000);
        
        ENABLE_THOUGHTWELL = BUILDER
                .comment("Enable Thoughtwell world generation")
                .define("enableThoughtwell", true);
        THOUGHTWELL_RARITY = BUILDER
                .comment("Thoughtwell spawn rarity (1/N chance per chunk)")
                .defineInRange("thoughtwellRarity", 500, 1, 100000);
        
        BUILDER.pop(); // anomalies
        
        // Ore Generation
        BUILDER.comment("Ore Generation Configuration")
                .push("ores");
        
        ENABLE_RESONITE_ORE = BUILDER
                .comment("Enable Resonite Ore world generation")
                .define("enableResoniteOre", true);
        RESONITE_ORE_VEINS_PER_CHUNK = BUILDER
                .comment("Number of Resonite Ore veins to attempt to generate per chunk")
                .defineInRange("resoniteOreVeinsPerChunk", 3, 0, 64);
        
        BUILDER.pop(); // ores
        
        // Terrain Modification
        BUILDER.comment("Terrain Modification Configuration",
                "Settings for how anomalies modify the terrain around them")
                .push("terrain");
        
        ENABLE_ANOMALOUS_GRASS = BUILDER
                .comment("Enable Anomalous Grass generation around anomalies")
                .define("enableAnomalousGrass", true);
        RESONITE_ORE_SPAWN_CHANCE_NEAR_ANOMALY = BUILDER
                .comment("Chance (0.0-1.0) for Resonite Ore to spawn near anomalies")
                .defineInRange("resoniteOreSpawnChanceNearAnomaly", 0.5, 0.0, 1.0);
        SHARD_ORE_SPAWN_CHANCE_NEAR_ANOMALY = BUILDER
                .comment("Chance (0.0-1.0) for Anomaly Shard Ore to spawn near anomalies")
                .defineInRange("shardOreSpawnChanceNearAnomaly", 0.2, 0.0, 1.0);
        
        BUILDER.pop(); // terrain
        
        // Villager Structures
        BUILDER.comment("Villager Structure Configuration",
                "Control the generation of custom villager structures")
                .push("villager_structures");
        
        ENABLE_ANOMALY_SCIENTIST_HOUSE = BUILDER
                .comment("Enable Anomaly Scientist Lab generation in villages")
                .define("enableAnomalyScientistHouse", true);
        ANOMALY_SCIENTIST_HOUSE_WEIGHT = BUILDER
                .comment("Weight for Anomaly Scientist Lab in village generation (higher = more common)",
                        "Default village houses have weights around 1-10, with 5 being moderately common")
                .defineInRange("anomalyScientistHouseWeight", 6, 1, 100);
        
        BUILDER.pop(); // villager_structures
        BUILDER.pop(); // worldgen
        
        // ========================================
        // ANOMALY EFFECTS
        // ========================================
        
        BUILDER.comment("Anomaly Effects Configuration",
                "Control the behavior and strength of anomaly effects")
                .push("anomaly_effects");
        
        // Gravity Anomaly
        BUILDER.comment("Gravity Anomaly Effect Settings").push("gravity_anomaly");
        ENABLE_GRAVITY_EFFECTS = BUILDER
                .comment("Enable Gravity Anomaly effects (levitation)")
                .define("enableEffects", true);
        GRAVITY_LEVITATION_RADIUS = BUILDER
                .comment("Levitation effect radius in blocks")
                .defineInRange("levitationRadius", 8.0, 1.0, 32.0);
        GRAVITY_LEVITATION_FORCE = BUILDER
                .comment("Levitation force strength (higher = stronger)")
                .defineInRange("levitationForce", 0.1, 0.01, 1.0);
        GRAVITY_RESEARCH_POINTS = BUILDER
                .comment("Research points granted when scanned")
                .defineInRange("researchPoints", 10, 1, 1000);
        BUILDER.pop();
        
        // Temporal Bloom
        BUILDER.comment("Temporal Bloom Effect Settings").push("temporal_bloom");
        ENABLE_TEMPORAL_EFFECTS = BUILDER
                .comment("Enable Temporal Bloom effects (crop growth/mob transformation)")
                .define("enableEffects", true);
        TEMPORAL_EFFECT_RADIUS = BUILDER
                .comment("Effect radius in blocks")
                .defineInRange("effectRadius", 8.0, 1.0, 32.0);
        TEMPORAL_CROP_GROWTH_STAGES = BUILDER
                .comment("Maximum crop growth stages to add/remove (±N)")
                .defineInRange("cropGrowthStages", 2, 1, 10);
        TEMPORAL_CROP_COOLDOWN = BUILDER
                .comment("Cooldown between crop effects in ticks (20 ticks = 1 second)")
                .defineInRange("cropCooldown", 100, 1, 1200);
        TEMPORAL_MOB_COOLDOWN = BUILDER
                .comment("Cooldown between mob transformations in ticks (20 ticks = 1 second)")
                .defineInRange("mobCooldown", 200, 1, 1200);
        TEMPORAL_RESEARCH_POINTS = BUILDER
                .comment("Research points granted when scanned")
                .defineInRange("researchPoints", 10, 1, 1000);
        BUILDER.pop();
        
        // Energetic Rift
        BUILDER.comment("Energetic Rift Effect Settings").push("energetic_rift");
        ENABLE_ENERGETIC_EFFECTS = BUILDER
                .comment("Enable Energetic Rift effects (lightning/zapping)")
                .define("enableEffects", true);
        ENERGETIC_ZAP_RADIUS = BUILDER
                .comment("Entity zap radius in blocks")
                .defineInRange("zapRadius", 6.0, 1.0, 32.0);
        ENERGETIC_LIGHTNING_RADIUS = BUILDER
                .comment("Lightning rod detection radius in blocks")
                .defineInRange("lightningRadius", 8.0, 1.0, 32.0);
        ENERGETIC_ZAP_DAMAGE = BUILDER
                .comment("Zap damage in half-hearts (2.0 = 1 heart)")
                .defineInRange("zapDamage", 1.0, 0.5, 20.0);
        ENERGETIC_ZAP_COOLDOWN = BUILDER
                .comment("Cooldown between zaps in ticks (20 ticks = 1 second)")
                .defineInRange("zapCooldown", 40, 1, 1200);
        ENERGETIC_LIGHTNING_COOLDOWN = BUILDER
                .comment("Cooldown between lightning strikes in ticks (20 ticks = 1 second)")
                .defineInRange("lightningCooldown", 200, 1, 1200);
        ENERGETIC_RESEARCH_POINTS = BUILDER
                .comment("Research points granted when scanned")
                .defineInRange("researchPoints", 10, 1, 1000);
        BUILDER.pop();
        
        // Warp Gate
        BUILDER.comment("Warp Gate Effect Settings").push("warp_gate");
        ENABLE_WARP_EFFECTS = BUILDER
                .comment("Enable Warp Gate effects (teleportation)")
                .define("enableEffects", true);
        WARP_TELEPORT_RADIUS = BUILDER
                .comment("Teleport activation radius in blocks")
                .defineInRange("teleportRadius", 2.0, 0.5, 16.0);
        WARP_TELEPORT_COOLDOWN = BUILDER
                .comment("Cooldown between teleports in ticks (20 ticks = 1 second)")
                .defineInRange("teleportCooldown", 100, 1, 1200);
        WARP_RESEARCH_POINTS = BUILDER
                .comment("Research points granted when scanned")
                .defineInRange("researchPoints", 10, 1, 1000);
        BUILDER.pop();
        
        // Echoing Shadow
        BUILDER.comment("Echoing Shadow Effect Settings").push("echoing_shadow");
        ENABLE_SHADOW_EFFECTS = BUILDER
                .comment("Enable Echoing Shadow effects (light absorption/mob spawning)")
                .define("enableEffects", true);
        SHADOW_EFFECT_RADIUS = BUILDER
                .comment("Effect radius in blocks")
                .defineInRange("effectRadius", 8.0, 1.0, 32.0);
        SHADOW_LIGHT_ABSORPTION = BUILDER
                .comment("Light absorption strength (0.0 = none, 1.0 = full)")
                .defineInRange("lightAbsorption", 0.8, 0.0, 1.0);
        SHADOW_MOB_SPAWN_BOOST = BUILDER
                .comment("Mob spawn rate multiplier in shadow area")
                .defineInRange("mobSpawnBoost", 2.0, 1.0, 10.0);
        SHADOW_RESEARCH_POINTS = BUILDER
                .comment("Research points granted when scanned")
                .defineInRange("researchPoints", 10, 1, 1000);
        BUILDER.pop();
        
        // Thoughtwell
        BUILDER.comment("Thoughtwell Effect Settings").push("thoughtwell");
        ENABLE_THOUGHTWELL_EFFECTS = BUILDER
                .comment("Enable Thoughtwell effects (confusion)")
                .define("enableEffects", true);
        THOUGHTWELL_EFFECT_RADIUS = BUILDER
                .comment("Effect radius in blocks")
                .defineInRange("effectRadius", 6.0, 1.0, 32.0);
        THOUGHTWELL_CONFUSION_DURATION = BUILDER
                .comment("Confusion effect duration in ticks (20 ticks = 1 second)")
                .defineInRange("confusionDuration", 100, 20, 1200);
        THOUGHTWELL_RESEARCH_POINTS = BUILDER
                .comment("Research points granted when scanned")
                .defineInRange("researchPoints", 10, 1, 1000);
        BUILDER.pop();
        
        BUILDER.pop(); // anomaly_effects
        
        // ========================================
        // ENERGY SYSTEM
        // ========================================
        
        BUILDER.comment("Energy System Configuration",
                "Control energy generation, consumption, and storage for machines")
                .push("energy");
        
        // Resonant Burner
        BUILDER.comment("Resonant Burner Settings").push("resonant_burner");
        RESONANT_BURNER_ENERGY_PER_TICK = BUILDER
                .comment("Energy generated per tick (RE/t)")
                .defineInRange("energyPerTick", 50, 1, 10000);
        RESONANT_BURNER_ENERGY_STORAGE = BUILDER
                .comment("Internal energy storage capacity (RE)")
                .defineInRange("energyStorage", 50000, 100, 1000000);
        RESONANT_BURNER_TRANSFER_RATE = BUILDER
                .comment("Energy transfer rate to adjacent blocks per tick (RE/t)")
                .defineInRange("transferRate", 800, 1, 100000);
        BUILDER.pop();
        
        // Resonance Condenser
        BUILDER.comment("Resonance Condenser Settings").push("resonance_condenser");
        RESONANCE_CONDENSER_ENERGY_PER_TICK = BUILDER
                .comment("Energy consumed per tick (RE/t)")
                .defineInRange("energyPerTick", 10, 1, 1000);
        RESONANCE_CONDENSER_ENERGY_STORAGE = BUILDER
                .comment("Internal energy storage capacity (RE)")
                .defineInRange("energyStorage", 25000, 100, 100000);
        RESONANCE_CONDENSER_PROGRESS_SPEED = BUILDER
                .comment("Ticks required to progress shard generation (lower = faster)")
                .defineInRange("progressSpeed", 15, 1, 600);
        BUILDER.pop();
        
        // Paradoxical Energy Cell
        BUILDER.comment("Paradoxical Energy Cell Settings").push("paradoxical_energy_cell");
        PARADOXICAL_CELL_TRANSFER_RATE = BUILDER
                .comment("Energy transfer rate to adjacent blocks per tick (RE/t)")
                .defineInRange("transferRate", 1000, 1, 100000);
        BUILDER.pop();
        
        // Reality Forge
        BUILDER.comment("Reality Forge Settings").push("reality_forge");
        REALITY_FORGE_CRAFT_TIME = BUILDER
                .comment("Crafting time in ticks (20 ticks = 1 second)")
                .defineInRange("craftTime", 100, 1, 1200);
        BUILDER.pop();
        
        // Rift Stabilizer
        BUILDER.push("riftStabilizer");
        RIFT_STABILIZER_ENERGY_PER_TICK = BUILDER
                .comment("Energy generated per tick")
                .defineInRange("energyPerTick", 40, 1, 10000);
        RIFT_STABILIZER_ENERGY_STORAGE = BUILDER
                .comment("Maximum energy storage")
                .defineInRange("energyStorage", 40000, 1000, 1000000);
        RIFT_STABILIZER_TRANSFER_RATE = BUILDER
                .comment("Energy transfer rate per tick")
                .defineInRange("transferRate", 800, 1, 10000);
        RIFT_STABILIZER_RADIUS = BUILDER
                .comment("Radius to detect Energetic Rift anomalies")
                .defineInRange("radius", 16.0, 1.0, 64.0);
        RIFT_STABILIZER_MAX_PER_RIFT = BUILDER
                .comment("Maximum number of Rift Stabilizers that can be powered by a single Energetic Rift")
                .defineInRange("maxPerRift", 3, 1, 10);
        BUILDER.pop();
        
        // Resonant Conduit
        BUILDER.comment("Resonant Conduit Settings").push("resonant_conduit");
        RESONANT_CONDUIT_TRANSFER_RATE = BUILDER
                .comment("Energy transfer rate per tick through conduits (RE/t)")
                .defineInRange("transferRate", 500, 1, 100000);
        RESONANT_CONDUIT_NETWORK_UPDATE_INTERVAL = BUILDER
                .comment("Network cache update interval in ticks (20 ticks = 1 second)")
                .defineInRange("networkUpdateInterval", 20, 1, 600);
        RESONANT_CONDUIT_MAX_NETWORK_SIZE = BUILDER
                .comment("Maximum network size before performance optimization kicks in")
                .defineInRange("maxNetworkSize", 64, 4, 1000);
        RESONANT_CONDUIT_DISTANCE_PENALTY = BUILDER
                .comment("Energy transfer penalty per block distance (0.0 = no penalty, 0.1 = 10% loss per block)")
                .defineInRange("distancePenalty", 0.05, 0.0, 1.0);
        BUILDER.pop();
        
        BUILDER.pop(); // energy
        
        // ========================================
        // RESEARCH SYSTEM
        // ========================================
        
        BUILDER.comment("Research System Configuration",
                "Control research costs and progression")
                .push("research");
        
        RESEARCH_COST_MULTIPLIER = BUILDER
                .comment("Global multiplier for all research node costs (1.0 = default, 0.5 = half cost, 2.0 = double cost)")
                .defineInRange("costMultiplier", 1.0, 0.0, 10.0);
        
        // Individual research node cost overrides (optional)
        BUILDER.comment("Individual Research Node Costs",
                "Override costs for specific research nodes. Set to -1 to use default values.",
                "Each node can require multiple research types. Values are point costs.")
                .push("node_costs");
        
        // Only configure nodes that have actual costs (locked nodes)
        ANOMALY_RESONATOR_COST = BUILDER
                .comment("Anomaly Resonator total cost (-1 for default: Energy 10, Space 10)")
                .defineInRange("anomalyResonator", -1, -1, 10000);
        
        RESONANCE_CONDENSER_COST = BUILDER
                .comment("Resonance Condenser total cost (-1 for default: Energy 25, Space 15)")
                .defineInRange("resonanceCondenser", -1, -1, 10000);
        
        CONTAINMENT_BASICS_COST = BUILDER
                .comment("Containment Basics total cost (-1 for default: Energy 20, Shadow 15)")
                .defineInRange("containmentBasics", -1, -1, 10000);
        
        ECHOFORM_IMPRINTER_COST = BUILDER
                .comment("Echoform Imprinter total cost (-1 for default: Shadow 25, Cognition 20)")
                .defineInRange("echoformImprinter", -1, -1, 10000);
        
        REALITY_FORGE_COST = BUILDER
                .comment("Reality Forge total cost (-1 for default: Energy 5, Space 5, Time 5)")
                .defineInRange("realityForge", -1, -1, 10000);
        
        WARP_GUN_COST = BUILDER
                .comment("Warp Gun total cost (-1 for default: Space 15, Energy 10)")
                .defineInRange("warpGun", -1, -1, 10000);
        
        GRAVITON_HAMMER_COST = BUILDER
                .comment("Graviton Hammer total cost (-1 for default: Gravity 15, Energy 10)")
                .defineInRange("gravitonHammer", -1, -1, 10000);
        
        // Graviton Hammer configuration
        GRAVITON_HAMMER_CHARGE_LEVEL_1_TIME = BUILDER
                .comment("Charge time for level 1 in ticks (20 ticks = 1 second)")
                .defineInRange("gravitonHammerChargeLevel1Time", 20, 1, 200);
        
        GRAVITON_HAMMER_CHARGE_LEVEL_2_TIME = BUILDER
                .comment("Charge time for level 2 in ticks (40 ticks = 2 seconds)")
                .defineInRange("gravitonHammerChargeLevel2Time", 40, 1, 200);
        
        GRAVITON_HAMMER_CHARGE_LEVEL_3_TIME = BUILDER
                .comment("Charge time for level 3 in ticks (60 ticks = 3 seconds)")
                .defineInRange("gravitonHammerChargeLevel3Time", 60, 1, 200);
        
        GRAVITON_HAMMER_TUNNEL_DEPTH_LEVEL_1 = BUILDER
                .comment("Tunnel depth for charge level 1")
                .defineInRange("gravitonHammerTunnelDepthLevel1", 3, 1, 50);
        
        GRAVITON_HAMMER_TUNNEL_DEPTH_LEVEL_2 = BUILDER
                .comment("Tunnel depth for charge level 2")
                .defineInRange("gravitonHammerTunnelDepthLevel2", 6, 1, 50);
        
        GRAVITON_HAMMER_TUNNEL_DEPTH_LEVEL_3 = BUILDER
                .comment("Tunnel depth for charge level 3")
                .defineInRange("gravitonHammerTunnelDepthLevel3", 9, 1, 50);
        
        STASIS_PROJECTOR_COST = BUILDER
                .comment("Stasis Projector total cost (-1 for default: Gravity 5, Time 5)")
                .defineInRange("stasisProjector", -1, -1, 10000);
        
        RIFT_STABILIZER_COST = BUILDER
                .comment("Rift Stabilizer total cost (-1 for default: Energy 20, Space 10)")
                .defineInRange("riftStabilizer", -1, -1, 10000);
        
        GRAVITY_ANOMALIES_COST = BUILDER
                .comment("Gravity Anomalies research cost (-1 for default: Gravity 5)")
                .defineInRange("gravityAnomalies", -1, -1, 10000);
        
        TEMPORAL_ANOMALIES_COST = BUILDER
                .comment("Temporal Anomalies research cost (-1 for default: Time 5)")
                .defineInRange("temporalAnomalies", -1, -1, 10000);
        
        SPATIAL_ANOMALIES_COST = BUILDER
                .comment("Spatial Anomalies research cost (-1 for default: Space 5)")
                .defineInRange("spatialAnomalies", -1, -1, 10000);
        
        ENERGY_ANOMALIES_COST = BUILDER
                .comment("Energy Anomalies research cost (-1 for default: Energy 5)")
                .defineInRange("energyAnomalies", -1, -1, 10000);
        
        SHADOW_ANOMALIES_COST = BUILDER
                .comment("Shadow Anomalies research cost (-1 for default: Shadow 5)")
                .defineInRange("shadowAnomalies", -1, -1, 10000);
        
        COGNITIVE_ANOMALIES_COST = BUILDER
                .comment("Cognitive Anomalies research cost (-1 for default: Cognition 5)")
                .defineInRange("cognitiveAnomalies", -1, -1, 10000);
        
        LEVITATION_PAD_COST = BUILDER
                .comment("Levitation Pad total cost (-1 for default: Gravity 15, Energy 10)")
                .defineInRange("levitationPad", -1, -1, 10000);
        
        LEVITATION_PAD_MAX_HEIGHT = BUILDER
                .comment("Levitation Pad maximum beam height in blocks (default: 16)")
                .defineInRange("levitationPadMaxHeight", 16, 1, 64);
        
        HOVERBOARD_COST = BUILDER
                .comment("Hoverboard total cost (-1 for default: Gravity 10, Energy 15)")
                .defineInRange("hoverboard", -1, -1, 10000);
        
        HOVERBOARD_MAX_SPEED = BUILDER
                .comment("Hoverboard maximum speed (default: 0.8)")
                .defineInRange("hoverboardMaxSpeed", 0.8, 0.1, 2.0);
        
        HOVERBOARD_ACCELERATION = BUILDER
                .comment("Hoverboard acceleration (default: 0.03)")
                .defineInRange("hoverboardAcceleration", 0.03, 0.01, 0.2);
        
        BUILDER.pop(); // node_costs
        BUILDER.pop(); // research
        
        // ========================================
        // RESEARCH MINIGAMES
        // ========================================
        
        BUILDER.comment("Research Minigame Configuration",
                "Control difficulty and behavior of research minigames")
                .push("minigames");
        
        ENABLE_MINIGAMES = BUILDER
                .comment("Enable minigame requirement (false = instant unlock when inserting research note)")
                .define("enableMinigames", true);
        
        // Instability System
        BUILDER.comment("Instability System Settings",
                "More active minigames = SLOWER instability increase (more time to balance multiple aspects).")
                .push("instability");
        INSTABILITY_DECREASE_RATE = BUILDER
                .comment("Instability decrease rate per tick when all minigames are stable")
                .defineInRange("decreaseRate", 0.004, 0.0001, 0.1);
        INSTABILITY_BASE_INCREASE_RATE = BUILDER
                .comment("Base instability increase rate when minigames are unstable",
                        "Actual rate = baseRate / number_of_active_minigames",
                        "More active minigames = slower increase = more time to stabilize")
                .defineInRange("baseIncreaseRate", 0.001, 0.0001, 0.1);
        BUILDER.pop();
        
        // Energy Minigame
        BUILDER.comment("Energy Minigame Settings (Wave Alignment)").push("energy");
        ENERGY_REQUIRED_ALIGNMENT_TICKS = BUILDER
                .comment("Ticks required to maintain alignment for success (20 ticks = 1 second)")
                .defineInRange("requiredAlignmentTicks", 100, 20, 1200);
        ENERGY_DRIFT_DELAY_TICKS = BUILDER
                .comment("Ticks before parameters start drifting (increases difficulty over time)")
                .defineInRange("driftDelayTicks", 600, 60, 2400);
        ENERGY_AMPLITUDE_STEP = BUILDER
                .comment("Amplitude adjustment step size per button press")
                .defineInRange("amplitudeStep", 0.05, 0.01, 0.5);
        ENERGY_PERIOD_STEP = BUILDER
                .comment("Period adjustment step size per button press")
                .defineInRange("periodStep", 0.05, 0.01, 0.5);
        BUILDER.pop();
        
        // Space Minigame
        BUILDER.comment("Space Minigame Settings (Image Unwarp)").push("space");
        SPACE_STABILITY_THRESHOLD = BUILDER
                .comment("How close to 0 warp is considered stable (lower = harder)")
                .defineInRange("stabilityThreshold", 0.1, 0.01, 1.0);
        SPACE_DRIFT_DELAY_TICKS = BUILDER
                .comment("Ticks before warp starts drifting")
                .defineInRange("driftDelayTicks", 100, 20, 1200);
        SPACE_WARP_ADJUSTMENT = BUILDER
                .comment("Warp adjustment step size per button press")
                .defineInRange("warpAdjustment", 0.05, 0.01, 0.5);
        BUILDER.pop();
        
        // Time Minigame
        BUILDER.comment("Time Minigame Settings (Clock Speed Matching)").push("time");
        TIME_SPEED_THRESHOLD = BUILDER
                .comment("Speed difference threshold for stability (lower = harder)")
                .defineInRange("speedThreshold", 0.15, 0.01, 1.0);
        TIME_DRIFT_DELAY_TICKS = BUILDER
                .comment("Ticks before speed starts drifting")
                .defineInRange("driftDelayTicks", 200, 20, 1200);
        TIME_SPEED_ADJUSTMENT = BUILDER
                .comment("Speed adjustment step size per button press")
                .defineInRange("speedAdjustment", 0.1, 0.01, 1.0);
        TIME_SNAP_THRESHOLD = BUILDER
                .comment("Speed difference threshold for auto-snapping main hand to faded hand")
                .defineInRange("snapThreshold", 0.05, 0.01, 0.5);
        BUILDER.pop();
        
        // Gravity Minigame
        BUILDER.comment("Gravity Minigame Settings (Balance Scale)").push("gravity");
        GRAVITY_BALANCE_THRESHOLD = BUILDER
                .comment("Balance threshold for stability (lower = harder)")
                .defineInRange("balanceThreshold", 0.1, 0.01, 1.0);
        GRAVITY_DRIFT_DELAY_TICKS = BUILDER
                .comment("Ticks before balance starts drifting")
                .defineInRange("driftDelayTicks", 1000, 20, 1200);
        BUILDER.pop();
        
        // Shadow Minigame
        BUILDER.comment("Shadow Minigame Settings (Light Routing)").push("shadow");
        SHADOW_ALIGNMENT_THRESHOLD = BUILDER
                .comment("Alignment threshold for stability (lower = harder)")
                .defineInRange("alignmentThreshold", 10.0, 1.0, 50.0);
        SHADOW_DRIFT_DELAY_TICKS = BUILDER
                .comment("Ticks before mirrors start drifting")
                .defineInRange("driftDelayTicks", 200, 20, 1200);
        SHADOW_ROTATION_STEP = BUILDER
                .comment("Mirror rotation step size per button press (degrees)")
                .defineInRange("rotationStep", 15.0, 1.0, 90.0);
        BUILDER.pop();
        
        // Cognition Minigame
        BUILDER.comment("Cognition Minigame Settings (Symbol Matching)").push("cognition");
        COGNITION_MATCH_DURATION = BUILDER
                .comment("Ticks symbols stay visible before changing")
                .defineInRange("matchDuration", 50, 20, 600);
        COGNITION_DIFFICULTY = BUILDER
                .comment("Number of symbols to match (higher = harder)")
                .defineInRange("difficulty", 3, 2, 8);
        BUILDER.pop();
        
        
        BUILDER.pop(); // minigames
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    // Public static fields to access config values
    public static boolean enableGravityAnomaly;
    public static int gravityAnomalyRarity;
    
    public static boolean enableTemporalBloom;
    public static int temporalBloomRarity;
    
    public static boolean enableWarpGate;
    public static int warpGateRarity;
    
    public static boolean enableEnergeticRift;
    public static int energeticRiftRarity;
    
    public static boolean enableEchoingShadow;
    public static int echoingShadowRarity;
    
    public static boolean enableThoughtwell;
    public static int thoughtwellRarity;
    
    public static boolean enableResoniteOre;
    public static int resoniteOreVeinsPerChunk;
    
    public static boolean enableAnomalousGrass;
    public static double resoniteOreSpawnChanceNearAnomaly;
    public static double shardOreSpawnChanceNearAnomaly;
    
    // Villager structures
    public static boolean enableAnomalyScientistHouse;
    public static int anomalyScientistHouseWeight;
    
    // Anomaly effects
    // Gravity Anomaly
    public static boolean enableGravityEffects;
    public static double gravityLevitationRadius;
    public static double gravityLevitationForce;
    public static int gravityResearchPoints;
    
    // Temporal Bloom
    public static boolean enableTemporalEffects;
    public static double temporalEffectRadius;
    public static int temporalCropGrowthStages;
    public static int temporalCropCooldown;
    public static int temporalMobCooldown;
    public static int temporalResearchPoints;
    
    // Energetic Rift
    public static boolean enableEnergeticEffects;
    public static double energeticZapRadius;
    public static double energeticLightningRadius;
    public static double energeticZapDamage;
    public static int energeticZapCooldown;
    public static int energeticLightningCooldown;
    public static int energeticResearchPoints;
    
    // Warp Gate
    public static boolean enableWarpEffects;
    public static double warpTeleportRadius;
    public static int warpTeleportCooldown;
    public static int warpResearchPoints;
    
    // Echoing Shadow
    public static boolean enableShadowEffects;
    public static double shadowEffectRadius;
    public static double shadowLightAbsorption;
    public static double shadowMobSpawnBoost;
    public static int shadowResearchPoints;
    
    // Thoughtwell
    public static boolean enableThoughtwellEffects;
    public static double thoughtwellEffectRadius;
    public static int thoughtwellConfusionDuration;
    public static int thoughtwellResearchPoints;
    
    // Energy system
    // Resonant Burner
    public static int resonantBurnerEnergyPerTick;
    public static int resonantBurnerEnergyStorage;
    public static int resonantBurnerTransferRate;
    
    // Resonance Condenser
    public static int resonanceCondenserEnergyPerTick;
    public static int resonanceCondenserEnergyStorage;
    public static int resonanceCondenserProgressSpeed;
    
    // Paradoxical Energy Cell
    public static int paradoxicalCellTransferRate;
    
    // Reality Forge
    public static int realityForgeCraftTime;
    
    // Rift Stabilizer
    public static int riftStabilizerEnergyPerTick;
    public static int riftStabilizerEnergyStorage;
    public static int riftStabilizerTransferRate;
    public static double riftStabilizerRadius;
    public static int riftStabilizerMaxPerRift;
    
    // Resonant Conduit
    public static int resonantConduitTransferRate;
    public static int resonantConduitNetworkUpdateInterval;
    public static int resonantConduitMaxNetworkSize;
    public static double resonantConduitDistancePenalty;
    
    // Research system
    public static double researchCostMultiplier;
    
    // Individual research node costs
    public static int anomalyResonatorCost;
    public static int resonanceCondenserCost;
    public static int containmentBasicsCost;
    public static int echoformImprinterCost;
    public static int realityForgeCost;
    public static int warpGunCost;
    public static int gravitonHammerCost;
    public static int stasisProjectorCost;
    public static int riftStabilizerCost;
    public static int gravityAnomaliesCost;
    public static int temporalAnomaliesCost;
    public static int spatialAnomaliesCost;
    public static int energyAnomaliesCost;
    public static int shadowAnomaliesCost;
    public static int cognitiveAnomaliesCost;
    public static int levitationPadCost;
    public static int levitationPadMaxHeight;
    public static int hoverboardCost;
    public static double hoverboardMaxSpeed;
    public static double hoverboardAcceleration;
    
    // Graviton Hammer
    public static int gravitonHammerChargeLevel1Time;
    public static int gravitonHammerChargeLevel2Time;
    public static int gravitonHammerChargeLevel3Time;
    public static int gravitonHammerTunnelDepthLevel1;
    public static int gravitonHammerTunnelDepthLevel2;
    public static int gravitonHammerTunnelDepthLevel3;
    
    // Minigames
    public static boolean enableMinigames;
    
    // Instability
    public static double instabilityDecreaseRate;
    public static double instabilityBaseIncreaseRate;
    
    // Energy Minigame
    public static int energyRequiredAlignmentTicks;
    public static int energyDriftDelayTicks;
    public static double energyAmplitudeStep;
    public static double energyPeriodStep;
    
    // Space Minigame
    public static double spaceStabilityThreshold;
    public static int spaceDriftDelayTicks;
    public static double spaceWarpAdjustment;
    
    // Time Minigame
    public static double timeSpeedThreshold;
    public static int timeDriftDelayTicks;
    public static double timeSpeedAdjustment;
    public static double timeSnapThreshold;
    
    // Gravity Minigame
    public static double gravityBalanceThreshold;
    public static int gravityDriftDelayTicks;
    
    // Shadow Minigame
    public static double shadowAlignmentThreshold;
    public static int shadowDriftDelayTicks;
    public static double shadowRotationStep;
    
    
    // Cognition Minigame
    public static int cognitionMatchDuration;
    public static int cognitionDifficulty;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // Anomaly spawn rates
        enableGravityAnomaly = ENABLE_GRAVITY_ANOMALY.get();
        gravityAnomalyRarity = GRAVITY_ANOMALY_RARITY.get();
        
        enableTemporalBloom = ENABLE_TEMPORAL_BLOOM.get();
        temporalBloomRarity = TEMPORAL_BLOOM_RARITY.get();
        
        enableWarpGate = ENABLE_WARP_GATE.get();
        warpGateRarity = WARP_GATE_RARITY.get();
        
        enableEnergeticRift = ENABLE_ENERGETIC_RIFT.get();
        energeticRiftRarity = ENERGETIC_RIFT_RARITY.get();
        
        enableEchoingShadow = ENABLE_ECHOING_SHADOW.get();
        echoingShadowRarity = ECHOING_SHADOW_RARITY.get();
        
        enableThoughtwell = ENABLE_THOUGHTWELL.get();
        thoughtwellRarity = THOUGHTWELL_RARITY.get();
        
        // Ore generation
        enableResoniteOre = ENABLE_RESONITE_ORE.get();
        resoniteOreVeinsPerChunk = RESONITE_ORE_VEINS_PER_CHUNK.get();
        
        // Terrain modification
        enableAnomalousGrass = ENABLE_ANOMALOUS_GRASS.get();
        resoniteOreSpawnChanceNearAnomaly = RESONITE_ORE_SPAWN_CHANCE_NEAR_ANOMALY.get();
        shardOreSpawnChanceNearAnomaly = SHARD_ORE_SPAWN_CHANCE_NEAR_ANOMALY.get();
        
        // Villager structures
        enableAnomalyScientistHouse = ENABLE_ANOMALY_SCIENTIST_HOUSE.get();
        anomalyScientistHouseWeight = ANOMALY_SCIENTIST_HOUSE_WEIGHT.get();
        
        // Anomaly effects
        // Gravity Anomaly
        enableGravityEffects = ENABLE_GRAVITY_EFFECTS.get();
        gravityLevitationRadius = GRAVITY_LEVITATION_RADIUS.get();
        gravityLevitationForce = GRAVITY_LEVITATION_FORCE.get();
        gravityResearchPoints = GRAVITY_RESEARCH_POINTS.get();
        
        // Temporal Bloom
        enableTemporalEffects = ENABLE_TEMPORAL_EFFECTS.get();
        temporalEffectRadius = TEMPORAL_EFFECT_RADIUS.get();
        temporalCropGrowthStages = TEMPORAL_CROP_GROWTH_STAGES.get();
        temporalCropCooldown = TEMPORAL_CROP_COOLDOWN.get();
        temporalMobCooldown = TEMPORAL_MOB_COOLDOWN.get();
        temporalResearchPoints = TEMPORAL_RESEARCH_POINTS.get();
        
        // Energetic Rift
        enableEnergeticEffects = ENABLE_ENERGETIC_EFFECTS.get();
        energeticZapRadius = ENERGETIC_ZAP_RADIUS.get();
        energeticLightningRadius = ENERGETIC_LIGHTNING_RADIUS.get();
        energeticZapDamage = ENERGETIC_ZAP_DAMAGE.get();
        energeticZapCooldown = ENERGETIC_ZAP_COOLDOWN.get();
        energeticLightningCooldown = ENERGETIC_LIGHTNING_COOLDOWN.get();
        energeticResearchPoints = ENERGETIC_RESEARCH_POINTS.get();
        
        // Warp Gate
        enableWarpEffects = ENABLE_WARP_EFFECTS.get();
        warpTeleportRadius = WARP_TELEPORT_RADIUS.get();
        warpTeleportCooldown = WARP_TELEPORT_COOLDOWN.get();
        warpResearchPoints = WARP_RESEARCH_POINTS.get();
        
        // Echoing Shadow
        enableShadowEffects = ENABLE_SHADOW_EFFECTS.get();
        shadowEffectRadius = SHADOW_EFFECT_RADIUS.get();
        shadowLightAbsorption = SHADOW_LIGHT_ABSORPTION.get();
        shadowMobSpawnBoost = SHADOW_MOB_SPAWN_BOOST.get();
        shadowResearchPoints = SHADOW_RESEARCH_POINTS.get();
        
        // Thoughtwell
        enableThoughtwellEffects = ENABLE_THOUGHTWELL_EFFECTS.get();
        thoughtwellEffectRadius = THOUGHTWELL_EFFECT_RADIUS.get();
        thoughtwellConfusionDuration = THOUGHTWELL_CONFUSION_DURATION.get();
        thoughtwellResearchPoints = THOUGHTWELL_RESEARCH_POINTS.get();
        
        // Energy system
        // Resonant Burner
        resonantBurnerEnergyPerTick = RESONANT_BURNER_ENERGY_PER_TICK.get();
        resonantBurnerEnergyStorage = RESONANT_BURNER_ENERGY_STORAGE.get();
        resonantBurnerTransferRate = RESONANT_BURNER_TRANSFER_RATE.get();
        
        // Resonance Condenser
        resonanceCondenserEnergyPerTick = RESONANCE_CONDENSER_ENERGY_PER_TICK.get();
        resonanceCondenserEnergyStorage = RESONANCE_CONDENSER_ENERGY_STORAGE.get();
        resonanceCondenserProgressSpeed = RESONANCE_CONDENSER_PROGRESS_SPEED.get();
        
        // Paradoxical Energy Cell
        paradoxicalCellTransferRate = PARADOXICAL_CELL_TRANSFER_RATE.get();
        
        // Reality Forge
        realityForgeCraftTime = REALITY_FORGE_CRAFT_TIME.get();
        
        // Rift Stabilizer
        riftStabilizerEnergyPerTick = RIFT_STABILIZER_ENERGY_PER_TICK.get();
        riftStabilizerEnergyStorage = RIFT_STABILIZER_ENERGY_STORAGE.get();
        riftStabilizerTransferRate = RIFT_STABILIZER_TRANSFER_RATE.get();
        riftStabilizerRadius = RIFT_STABILIZER_RADIUS.get();
        riftStabilizerMaxPerRift = RIFT_STABILIZER_MAX_PER_RIFT.get();
        
        // Resonant Conduit
        resonantConduitTransferRate = RESONANT_CONDUIT_TRANSFER_RATE.get();
        resonantConduitNetworkUpdateInterval = RESONANT_CONDUIT_NETWORK_UPDATE_INTERVAL.get();
        resonantConduitMaxNetworkSize = RESONANT_CONDUIT_MAX_NETWORK_SIZE.get();
        resonantConduitDistancePenalty = RESONANT_CONDUIT_DISTANCE_PENALTY.get();
        
        // Research system
        researchCostMultiplier = RESEARCH_COST_MULTIPLIER.get();
        
        // Individual research node costs
        anomalyResonatorCost = ANOMALY_RESONATOR_COST.get();
        resonanceCondenserCost = RESONANCE_CONDENSER_COST.get();
        containmentBasicsCost = CONTAINMENT_BASICS_COST.get();
        echoformImprinterCost = ECHOFORM_IMPRINTER_COST.get();
        realityForgeCost = REALITY_FORGE_COST.get();
        warpGunCost = WARP_GUN_COST.get();
        gravitonHammerCost = GRAVITON_HAMMER_COST.get();
        stasisProjectorCost = STASIS_PROJECTOR_COST.get();
        riftStabilizerCost = RIFT_STABILIZER_COST.get();
        gravityAnomaliesCost = GRAVITY_ANOMALIES_COST.get();
        temporalAnomaliesCost = TEMPORAL_ANOMALIES_COST.get();
        spatialAnomaliesCost = SPATIAL_ANOMALIES_COST.get();
        energyAnomaliesCost = ENERGY_ANOMALIES_COST.get();
        shadowAnomaliesCost = SHADOW_ANOMALIES_COST.get();
        cognitiveAnomaliesCost = COGNITIVE_ANOMALIES_COST.get();
        levitationPadCost = LEVITATION_PAD_COST.get();
        levitationPadMaxHeight = LEVITATION_PAD_MAX_HEIGHT.get();
        hoverboardCost = HOVERBOARD_COST.get();
        hoverboardMaxSpeed = HOVERBOARD_MAX_SPEED.get();
        hoverboardAcceleration = HOVERBOARD_ACCELERATION.get();
        
        // Graviton Hammer
        gravitonHammerChargeLevel1Time = GRAVITON_HAMMER_CHARGE_LEVEL_1_TIME.get();
        gravitonHammerChargeLevel2Time = GRAVITON_HAMMER_CHARGE_LEVEL_2_TIME.get();
        gravitonHammerChargeLevel3Time = GRAVITON_HAMMER_CHARGE_LEVEL_3_TIME.get();
        gravitonHammerTunnelDepthLevel1 = GRAVITON_HAMMER_TUNNEL_DEPTH_LEVEL_1.get();
        gravitonHammerTunnelDepthLevel2 = GRAVITON_HAMMER_TUNNEL_DEPTH_LEVEL_2.get();
        gravitonHammerTunnelDepthLevel3 = GRAVITON_HAMMER_TUNNEL_DEPTH_LEVEL_3.get();
        
        // Minigames
        enableMinigames = ENABLE_MINIGAMES.get();
        
        // Instability
        instabilityDecreaseRate = INSTABILITY_DECREASE_RATE.get();
        instabilityBaseIncreaseRate = INSTABILITY_BASE_INCREASE_RATE.get();
        
        // Energy Minigame
        energyRequiredAlignmentTicks = ENERGY_REQUIRED_ALIGNMENT_TICKS.get();
        energyDriftDelayTicks = ENERGY_DRIFT_DELAY_TICKS.get();
        energyAmplitudeStep = ENERGY_AMPLITUDE_STEP.get();
        energyPeriodStep = ENERGY_PERIOD_STEP.get();
        
        // Space Minigame
        spaceStabilityThreshold = SPACE_STABILITY_THRESHOLD.get();
        spaceDriftDelayTicks = SPACE_DRIFT_DELAY_TICKS.get();
        spaceWarpAdjustment = SPACE_WARP_ADJUSTMENT.get();
        
        // Time Minigame
        timeSpeedThreshold = TIME_SPEED_THRESHOLD.get();
        timeDriftDelayTicks = TIME_DRIFT_DELAY_TICKS.get();
        timeSpeedAdjustment = TIME_SPEED_ADJUSTMENT.get();
        timeSnapThreshold = TIME_SNAP_THRESHOLD.get();
        
        // Gravity Minigame
        gravityBalanceThreshold = GRAVITY_BALANCE_THRESHOLD.get();
        gravityDriftDelayTicks = GRAVITY_DRIFT_DELAY_TICKS.get();
        
        // Shadow Minigame
        shadowAlignmentThreshold = SHADOW_ALIGNMENT_THRESHOLD.get();
        shadowDriftDelayTicks = SHADOW_DRIFT_DELAY_TICKS.get();
        shadowRotationStep = SHADOW_ROTATION_STEP.get();
        
        // Cognition Minigame
        cognitionMatchDuration = COGNITION_MATCH_DURATION.get();
        cognitionDifficulty = COGNITION_DIFFICULTY.get();
        
    }
}
