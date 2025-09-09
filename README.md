# Strange Matter Mod

A Minecraft 1.20.1 Forge mod that introduces strange anomalies into the world. Study, stabilize, and harness these reality-bending phenomena.

## Overview

The world is dotted with strange, unpredictable **anomalies** — rips in reality, weird physical effects, clusters of impossible matter. Players can **study, stabilize, and harness** these anomalies, gaining access to bizarre mechanics that feel both dangerous and rewarding.

## Features

### Core Concept
- **Procedural Anomalies** spawn randomly in the world
- Each anomaly has **unique rules of physics**
- **Research and Progression** system to unlock abilities
- **Anomaly Research Table** for studying and containment
- **Emergent Gameplay** with unique world generation

### Current Implementation

#### Blocks & Items
- **Anomalous Grass Block**: A mysterious grass block that can be placed and harvested
- **Crystalized Ectoplasm Block**: A crystalline block that generates naturally underground
- **Ectoplasm Item**: A mysterious substance with unique properties
- **Field Scanner**: A tool for detecting and analyzing anomalies
- **Anomaly Resonator**: A compass-like device that points toward nearby anomalies

#### Entities & World Generation
- **Gravity Anomaly Entity**: A floating icosahedron that creates levitation fields around players
- **Custom Gravity Attribute**: Allows entities to have modified gravity effects
- **World Generation**: Crystalized ectoplasm naturally generates in overworld biomes
- **Sound System**: Custom ambient sounds for gravity anomalies

#### Commands & Tools
- **Anomaly Commands**: `/anomaly spawn <type> [pos]` and `/anomaly list` for testing
- **Test Commands**: `/test_gravity_anomaly` for debugging world generation
- **Strange Matter Creative Tab**: Organized collection of all mod items

## Development Setup

### Prerequisites
- Java 17 or higher
- Minecraft 1.20.1
- Forge MDK 47.2.0

### Building
1. Clone the repository
2. Run `./gradlew build` to build the mod
3. The compiled mod will be in `build/libs/`

### Running in Development
1. Run `./gradlew runClient` to start Minecraft with the mod
2. Run `./gradlew runServer` to start a test server

### Testing Commands
The mod includes several commands for testing and debugging (requires OP level 2):

- `/anomaly spawn <type> [pos]` - Spawn an anomaly at the player's location or specified coordinates
  - Types: `gravity` (creates a floating icosahedron with levitation field)
- `/anomaly list` - List all available anomaly types
- `/test_gravity_anomaly` - Test the gravity anomaly world generation feature

## Project Structure

```
src/main/java/com/hexvane/strangematter/
├── StrangeMatterMod.java           # Main mod class
├── Config.java                     # Configuration system
├── block/
│   ├── AnomalousGrassBlock.java    # Anomalous grass block implementation
│   └── CrystalizedEctoplasmBlock.java # Crystalized ectoplasm block
├── item/
│   ├── AnomalousGrassItem.java     # Anomalous grass item
│   ├── AnomalyResonatorItem.java   # Anomaly detection compass
│   └── EctoplasmItem.java          # Ectoplasm item
├── entity/
│   └── GravityAnomalyEntity.java   # Gravity anomaly entity
├── client/
│   ├── GravityAnomalyRenderer.java # Entity renderer
│   └── sound/
│       └── CustomSoundManager.java # Sound management
├── command/
│   └── AnomalyCommand.java         # Admin commands for testing
├── worldgen/
│   └── GravityAnomalyConfiguredFeature.java # World generation
└── util/
    └── OBJParser.java              # 3D model parsing utilities

src/main/resources/
├── assets/strangematter/           # Textures, models, language files
│   ├── textures/                   # Block and item textures
│   ├── models/                     # Block and item models
│   ├── blockstates/                # Block state definitions
│   └── lang/en_us.json            # Localization
├── data/strangematter/             # Data-driven content
│   ├── worldgen/                   # World generation features
│   ├── forge/biome_modifier/       # Biome modifications
│   ├── loot_tables/                # Loot table definitions
│   └── recipes/                    # Crafting recipes
├── META-INF/mods.toml             # Mod metadata
└── pack.mcmeta                    # Resource pack metadata
```

## World Generation

The mod adds several world generation features:

- **Crystalized Ectoplasm**: Naturally generates underground in all overworld biomes during the underground decoration phase
- **Gravity Anomalies**: Can be spawned via commands and have world generation support (currently in development)

## Planned Features

### Tier 1 - The Unexplained ✅ (Partially Implemented)
- ✅ Raw anomalies spawning naturally (Gravity Anomaly entity)
- ✅ Observation tools (Field Scanner, Anomaly Resonator)
- ⏳ Basic containment systems

### Tier 2 - Containment & Sampling
- Anomaly Jars and Sample Vials
- Anomaly Research Table
- Basic research progression

### Tier 3+ - Advanced Systems
- ✅ Gravity control systems (Custom gravity attribute implemented)
- Temporal manipulation
- Spatial distortion devices
- Resonant energy systems

## License

MIT License - see LICENSE file for details.

## Author

**Hexvane** - Lead developer and concept creator

## Contributing

This is a development project. Feel free to contribute ideas, code, or resources!

---

*"Reality is just a suggestion when Strange Matter is involved..."*
