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
- **Anomaly Core Block**: A mysterious purple block representing concentrated anomaly matter
- **Field Scanner**: A tool for detecting and analyzing anomalies
- **Strange Matter Creative Tab**: Organized collection of mod items

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

## Project Structure

```
src/main/java/com/hexvane/strangematter/
├── StrangeMatterMod.java    # Main mod class
└── Config.java              # Configuration system

src/main/resources/
├── assets/strangematter/    # Textures, models, language files
├── META-INF/mods.toml      # Mod metadata
└── pack.mcmeta             # Resource pack metadata
```

## Planned Features

### Tier 1 - The Unexplained
- Raw anomalies spawning naturally
- Observation tools (Field Scanner, Resonance Meter)
- Basic containment systems

### Tier 2 - Containment & Sampling
- Anomaly Jars and Sample Vials
- Anomaly Research Table
- Basic research progression

### Tier 3+ - Advanced Systems
- Gravity control systems
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
