# Strange Matter Mod

A Minecraft 1.20.1 Forge mod that introduces reality-bending anomalies into the world. Study, contain, and harness these mysterious phenomena through a comprehensive research system.

## Overview

The world is dotted with strange, unpredictable **anomalies** — rips in reality that bend the laws of physics. Players can **study, contain, and harness** these anomalies through an advanced research system, gaining access to bizarre technologies that feel both dangerous and rewarding.

## Core Features

### 🌌 **Anomaly System**
- **6 Types of Anomalies** with unique physics-defying effects:
  - **Gravity Anomalies** - Create levitation fields and modified gravity
  - **Temporal Anomalies** - Accelerate crop growth and transform mobs
  - **Spatial Anomalies** - Enable teleportation between warp gates
  - **Energy Anomalies** - Generate electrical effects and strike lightning rods
  - **Shadow Anomalies** - Absorb light and boost mob spawning
  - **Cognitive Anomalies** - Affect player perception and confuse mobs

### 🔬 **Research System**
- **Research Machine** - Advanced research station with interactive minigames
- **Research Tablet** - Portable research interface
- **Research Nodes** - 22 unlockable research topics with detailed documentation
- **Research Locking** - Reality Forge recipes locked behind research requirements
- **Minigames** - 6 different research challenges (Cognition, Energy, Gravity, Shadow, Space, Time)

### ⚡ **Energy & Power**
- **Resonant Energy** - New energy system for powering anomalous machines
- **Resonant Burner** - Early-game power generation
- **Resonance Condenser** - Advanced anomaly energy harvesting
- **Paradoxical Energy Cells** - High-capacity energy storage

### 🛠️ **Tools & Equipment**
- **Field Scanner** - Detect and analyze anomalies in the world
- **Anomaly Resonator** - Compass-like device pointing to nearby anomalies
- **Echo Vacuum** - Extract anomalies from the world
- **Warp Gun** - Create personal teleportation gates
- **Containment Capsules** - Store and transport captured anomalies

### 🏭 **Advanced Crafting**
- **Reality Forge** - Special crafting station requiring anomaly shards
- **Research-Locked Recipes** - Advanced items require research to craft
- **Anomaly Shard Integration** - 6 different shard types for specialized crafting

### 🌍 **World Generation**
- **Anomaly Spawning** - Natural anomaly generation in various biomes
- **Resonite Ore** - New ore type found near anomalies
- **Anomaly Shard Ores** - 6 different shard ores underground
- **Anomalous Grass** - Mysterious grass blocks around anomalies

## Research Tree

The mod features a comprehensive research system with 22 research nodes:

### **Default Unlocked Research**
- **Research** - Introduction to the research system
- **Field Scanner** - Basic anomaly detection tools
- **Anomaly Shards** - Understanding anomaly materials
- **Anomaly Types** - Learning about different anomaly categories
- **Resonite** - Basic anomalous materials
- **Resonant Energy** - Understanding power systems

### **Advanced Research**
- **Anomaly Resonator** - Advanced detection equipment
- **Reality Forge** - Gateway to advanced crafting
- **Resonance Condenser** - Advanced energy harvesting
- **Containment Basics** - Anomaly containment systems
- **Warp Gun** - Teleportation technology

### **Specialized Research**
- **Gravity Anomalies** - Deep dive into gravity manipulation
- **Temporal Anomalies** - Time manipulation research
- **Spatial Anomalies** - Space distortion studies
- **Energy Anomalies** - Electrical anomaly research
- **Shadow Anomalies** - Shadow manipulation studies
- **Cognitive Anomalies** - Mental effect research

## Items & Blocks

### **Research Equipment**
- Research Machine (Block + Block Entity)
- Research Tablet (Portable research interface)
- Research Notes (Basic research material)

### **Tools**
- Field Scanner (Anomaly detection)
- Anomaly Resonator (Anomaly location)
- Echo Vacuum (Anomaly extraction)
- Warp Gun (Teleportation device)

### **Materials**
- Resonite Ore, Ingot, Nugget, Raw Resonite
- Resonant Coil, Circuit, Stabilized Core
- 6 Anomaly Shard types (Gravitic, Chrono, Spatial, Shade, Insight, Energetic)
- 6 Anomaly Shard Ore types

### **Machines**
- Reality Forge (Advanced crafting)
- Resonance Condenser (Energy harvesting)
- Resonant Burner (Power generation)
- Paradoxical Energy Cell (Energy storage)

### **Containment**
- 7 Containment Capsule variants (Empty + 6 anomaly types)

### **World Generation**
- Anomalous Grass Block
- Resonite Ore (underground)
- 6 Anomaly Shard Ores (underground)

## Commands

### **Anomaly Commands** (OP Level 2)
- `/anomaly spawn <type> [pos]` - Spawn an anomaly
- `/anomaly locate <type>` - Find nearest anomaly of type
- `/anomaly list` - List all available anomaly types
- `/anomaly debug_chunk <pos>` - Debug chunk loading

### **Research Commands** (OP Level 2)
- `/research check [target]` - Check research progress
- `/research add <type> <amount> [target]` - Add research points
- `/research reset [target]` - Reset research progress
- `/research give <research_id> [target]` - Give research notes
- `/research unlock <research_id> [target]` - Unlock specific research
- `/research unlock_all [target]` - Unlock all research

## Development Setup

### **Prerequisites**
- Java 17 or higher
- Minecraft 1.20.1
- Forge MDK 47.4.0

### **Building**
1. Clone the repository
2. Run `./gradlew build` to build the mod
3. The compiled mod will be in `build/libs/`

### **Running in Development**
1. Run `./gradlew runClient` to start Minecraft with the mod
2. Run `./gradlew runServer` to start a test server

## Project Structure

```
src/main/java/com/hexvane/strangematter/
├── StrangeMatterMod.java           # Main mod class
├── Config.java                     # Configuration system
├── block/                          # Block implementations
│   ├── AnomalousGrassBlock.java
│   ├── ResearchMachineBlock.java
│   ├── RealityForgeBlock.java
│   ├── ResonanceCondenserBlock.java
│   └── ResonantBurnerBlock.java
├── item/                           # Item implementations
│   ├── FieldScannerItem.java
│   ├── AnomalyResonatorItem.java
│   ├── ResearchTabletItem.java
│   ├── WarpGunItem.java
│   └── EchoVacuumItem.java
├── entity/                         # Anomaly entities
│   ├── BaseAnomalyEntity.java
│   ├── GravityAnomalyEntity.java
│   ├── TemporalBloomEntity.java
│   ├── WarpGateAnomalyEntity.java
│   ├── EnergeticRiftEntity.java
│   ├── EchoingShadowEntity.java
│   └── ThoughtwellEntity.java
├── research/                       # Research system
│   ├── ResearchNode.java
│   ├── ResearchNodeRegistry.java
│   ├── ResearchData.java
│   └── ScannableObjectRegistry.java
├── client/                         # Client-side code
│   ├── screen/                     # GUI screens
│   ├── renderer/                   # Entity renderers
│   └── sound/                      # Sound management
├── command/                        # Commands
│   ├── AnomalyCommand.java
│   └── ResearchCommand.java
├── recipe/                         # Custom recipes
│   ├── RealityForgeRecipe.java
│   └── RealityForgeRecipeRegistry.java
├── worldgen/                       # World generation
└── util/                          # Utilities
```

## World Generation Features

- **Anomaly Spawning** - Natural generation in various biomes
- **Resonite Ore** - Underground ore deposits near anomalies
- **Anomaly Shard Ores** - 6 different shard ores underground
- **Anomalous Grass** - Grass blocks that spawn around anomalies

## Technical Features

- **Custom Attributes** - Entity gravity modification system
- **Custom Particles** - Energy absorption effects
- **Custom Sounds** - Ambient anomaly sounds
- **Custom Recipes** - Reality Forge with shard requirements
- **Research Locking** - Recipe access controlled by research
- **Chunk Loading** - Warp gates maintain loaded chunks
- **Data Persistence** - Research progress and anomaly data

## License

MIT License - see LICENSE file for details.

## Author

**Hexvane** - Lead developer and concept creator

## Contributing

This is an active development project. Feel free to contribute ideas, code, or resources!

---

*"Reality is just a suggestion when Strange Matter is involved..."*