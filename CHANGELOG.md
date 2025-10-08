# Changelog

All notable changes to Strange Matter will be documented in this file.

## [0.1.1] - 2025-10-08

### Added

#### New Blocks & Items
- **Stasis Projector** - A display pedestal that suspends items and entities in time
  - Holds one item or entity in suspended animation above the projector
  - Toggle on/off with shift right-click
  - Animated stasis field with pulsing transparency effect
  - Suspended items rotate and bounce smoothly
  - Suspended entities (mobs) rotate and bounce with disabled AI
  - Items cannot be picked up while in stasis
  - Entities don't despawn or take damage while in stasis
  - Sound effects for activation and deactivation
  - Research node with Reality Forge crafting recipe
- **Resonite Block** - Decorative storage block for resonite ingots

#### Quality of Life
- Players now spawn with a Research Tablet in survival mode

### Changed
- Tweaked research node requirements for better progression balance
- Updated several crafting recipes for improved game balance
  
### Fixed
- Fixed machines not dropping themselves when broken
  - Stasis Projector now drops itself
  - Resonant Burner now drops itself
  - Research Machine now drops itself
  - Reality Forge now drops itself
  - Resonance Condenser now drops itself
- Fixed Reality Forge not dropping stored shards when broken
- Fixed Reality Forge shard rendering not updating after world reload
- Fixed Research Tablet crash when viewing certain research nodes before world is fully loaded
- Fixed Anomalous Grass not turning into dirt when a block is placed on top of it
- Fixed Anomalous Grass not being convertible to farmland with a hoe

## [0.1.0] - Initial Alpha Release

### Added

#### Core Systems
- Complete anomaly system with 6 anomaly types
- Comprehensive research system with 22 research nodes
- Research Machine with interactive minigames
- Research Tablet for portable research access
- Research-locked recipe system

#### Anomaly Types
- **Gravity Anomalies** - Create levitation fields and modified gravity effects
- **Temporal Anomalies** - Accelerate crop growth and transform mobs over time
- **Spatial Anomalies** - Enable teleportation through warp gates
- **Energy Anomalies** - Generate electrical effects and interact with lightning rods
- **Shadow Anomalies** - Absorb light and increase mob spawning rates
- **Cognitive Anomalies** - Affect player perception and confuse nearby mobs

#### Research System
- Research Machine block with GUI interface
- 6 interactive minigames (Cognition, Energy, Gravity, Shadow, Space, Time)
- 22 research nodes covering all aspects of anomaly technology
- Research progression from basic detection to advanced reality manipulation
- Research-locked recipes requiring knowledge to craft
- Comprehensive documentation system for each research topic

#### Tools & Equipment
- **Field Scanner** - Detect and analyze anomalies in the world
- **Anomaly Resonator** - Compass-like device pointing to nearby anomalies
- **Echo Vacuum** - Extract anomalies from the world for containment
- **Warp Gun** - Create personal teleportation networks
- **Research Tablet** - Portable research interface

#### Advanced Crafting
- **Reality Forge** - Special crafting station requiring anomaly shards
- Recipe system with research requirements
- 6 different anomaly shard types for specialized crafting
- Visual recipe display showing required materials and shards
- Tooltip system for recipe ingredients

#### Power Systems
- **Resonant Energy** - New energy type for anomalous machines
- **Resonance Condenser** - Harvest energy directly from anomalies
- **Resonant Burner** - Generate power from resonant energy
- **Paradoxical Energy Cells** - High-capacity energy storage

#### Materials & Resources
- **Resonite** - New ore type found near anomalies
- **Resonite Ingot, Nugget, Raw Resonite** - Processing materials
- **Resonant Coil** - Basic resonant energy component
- **Resonant Circuit** - Advanced resonant energy component
- **Stabilized Core** - Core component for advanced machines
- **6 Anomaly Shard Types** - Gravitic, Chrono, Spatial, Shade, Insight, Energetic
- **6 Anomaly Shard Ore Types** - Underground ore generation

#### Containment System
- **Containment Capsules** - Store captured anomalies
- 7 capsule variants (Empty + 6 anomaly types)
- Anomaly extraction and storage mechanics

#### World Generation
- Natural anomaly spawning in various biomes
- Resonite ore generation underground near anomalies
- Six different anomaly shard ores throughout the world
- Anomalous grass blocks that form around anomaly sites
- Anomaly terrain modification and environmental effects

#### Blocks & Machines
- **Research Machine** - Main research station with GUI
- **Reality Forge** - Advanced crafting station
- **Resonance Condenser** - Energy harvesting machine
- **Resonant Burner** - Power generation machine
- **Paradoxical Energy Cell** - Energy storage block
- **Anomalous Grass Block** - Mysterious grass around anomalies

#### Commands
- `/anomaly spawn <type> [pos]` - Spawn anomalies for testing
- `/anomaly locate <type>` - Find nearest anomaly of specified type
- `/anomaly list` - List all available anomaly types
- `/anomaly debug_chunk <pos>` - Debug chunk loading issues
- `/research check [target]` - Check research progress
- `/research add <type> <amount> [target]` - Add research points
- `/research reset [target]` - Reset research progress
- `/research give <research_id> [target]` - Give research notes
- `/research unlock <research_id> [target]` - Unlock specific research
- `/research unlock_all [target]` - Unlock all research

#### Technical Features
- Custom entity system for all anomaly types
- Custom attributes for gravity modification
- Custom particle effects for energy absorption
- Custom sound system for ambient anomaly effects
- Custom recipe system for Reality Forge
- Chunk loading system for warp gate networks
- Data persistence for research progress and anomaly data
- Multiplayer synchronization for all systems

#### GUI Systems
- Research Machine interface with minigames
- Research Tablet interface for portable access
- Reality Forge crafting interface
- Resonance Condenser energy management
- Resonant Burner power generation interface
- Tooltip system for all items and blocks

#### Integration
- Full Forge compatibility
- No external dependencies
- Optimized performance for smooth gameplay
- Multiplayer server support
- Configurable settings for anomaly spawn rates

### Technical Details
- **Minecraft Version:** 1.20.1
- **Mod Loader:** Forge 47.4.0
- **Java Version:** 17+
- **Dependencies:** None
