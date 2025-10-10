# Changelog

All notable changes to Strange Matter will be documented in this file.

## [0.1.4] - 2025-10-10

### Added
- **Rift Stabilizer**
  - New power generation block that harnesses energy from Energetic Rift anomalies
  - Generates power passively when placed within range of an Energetic Rift
  - Front face must point toward the rift to generate power
  - Back face outputs energy to adjacent machines
  - Displays beautiful electricity beam from rift to block when active
  - Right-click to view power generation status and stored energy
  - Maximum 3 stabilizers can be powered by a single Energetic Rift
  - Configurable parameters: energy per tick, storage capacity, transfer rate, detection radius, max per rift
  - Crafted in Reality Forge using 2 Energetic Shards
  - Requires Reality Forge research to unlock
- **Rift Stabilizer Research Node**
  - New research node positioned at (140, 280) on the research tree
  - Costs 20 Energy + 10 Space research points
  - Requires Reality Forge research to unlock
  - Three info pages with mad scientist narration:
    - "Rift Stabilizer" (with Reality Forge recipe)
    - "Power Generation Theory" (mechanics and requirements)
    - "Installation & Operation" (usage guide)
  - Configurable research cost via config file
- **AnomalyUtil Utility Class**
  - New utility class for easy anomaly detection throughout the mod
  - `findNearestEnergeticRift()` - Find nearest Energetic Rift within radius
  - `findNearestAnomaly()` - Find nearest anomaly of any type
  - `countEnergeticRiftsInRange()` - Count rifts in range
  - `hasEnergeticRiftInRange()` - Check if any rift exists nearby
  - Reusable across the entire mod for consistent anomaly detection
- **Research Node Documentation Improvements**
  - Added "The Field Scanner" page to "research" node explaining how to scan anomalies
  - Added "The Research Tablet" page to "research" node explaining how to spend research points
  - Complete tutorial workflow: Scan → Spend Points → Research → Minigames

### Fixed
- Fixed stasis projector recipe not working

### Technical
- `RiftStabilizerBlock` - Directional block with horizontal facing support
- `RiftStabilizerBlockEntity` - Generates power, stores energy, outputs through specific face
  - Server-side power generation with client synchronization
  - Directional validation using dot product calculations
  - Configurable beam offset via static fields for easy positioning adjustments
  - Proper NBT serialization and update packet handling
- `RiftStabilizerRenderer` - Custom block entity renderer for electricity beam
  - Uses lightning render type for beam effect
  - Same spark rendering technique as Energetic Rift
  - Client-side anomaly detection for beam target
  - Zigzag animation with configurable parameters
- Energy system integration with Forge Energy capability
  - Only provides capability on output face (back)
  - Internal energy generation through custom maxReceive setting
  - Every-tick generation with periodic client sync
- All configurable values in Config.java with proper defaults
- Research cost system integration with override support

## [0.1.3] - 2025-10-09

### Added
- **Echoform Imprinter**
  - New item that allows players to scan and morph into **mobs and other players** with full animations
  - Right-click and hold on a mob or player for 1 second to scan them
  - Successfully scanning morphs you into that target's appearance
  - Full animation support - walk, run, swim, sneak animations all work
  - Proper player-to-mob state synchronization for realistic movement
  - **Player morphing**: Can morph into other players to look exactly like them
  - **Multiplayer support**: Morphs are visible to all nearby players on servers
  - Shift+right-click to morph back to normal form
  - Plays field scanner sound effects during scanning
  - Beautiful particle effects during scanning and morphing
  - Displays proper entity/player names in messages
  - Crafted in the Reality Forge using shade shards and insight shards
  - Requires Containment Basics research to unlock
- **Echoform Imprinter Research Node**
  - New research node positioned at (80, 160) on the research tree
  - Costs 25 Shadow + 20 Cognition research points
  - Requires Containment Basics research to unlock
  - Three info pages with mad scientist narration:
    - "Identity Transference" (with recipe)
    - "Operational Protocols" (usage guide)
    - "Shadow-Cognitive Theory" (lore and theory)
  - Configurable research cost via config file
- **Thoughtwell Cognitive Disguise Multiplayer Support**
  - Thoughtwell cognitive disguises now sync properly to all clients in multiplayer
  - Added `MobDisguiseSyncPacket` for server-to-client synchronization
  - Disguises are visible to all players on servers, not just the host
  - Disguises properly expire and sync removal across all clients

### Technical
- New `PlayerMorphData` system for managing player morphs
- New `PlayerMorphRenderer` for rendering players as mobs/players with proper animations
  - ThreadLocal recursion guard to prevent infinite loops when morphing into players
  - Direct walk animation synchronization via `walkAnimation.position()` and `speed()`
- `PlayerRendererMixin` intercepts player rendering to apply morphs
- Cached morph entities for performance
- Full animation synchronization between player and morphed entity (works in multiplayer)
- `PlayerMorphSyncPacket` for proper client-server synchronization
  - Broadcasts to ALL players using `PacketDistributor.ALL` for guaranteed visibility
  - Syncs player UUID for skin rendering when morphing into players
- `PlayerMorphEventHandler` for cleanup on logout/world change and syncing on join
- `EchoformImprinterEventHandler` overrides entity interactions (villagers, rideable mobs, etc.)
- Support for morphing into both mobs and other players with correct skins
- Proper entity type registration using `ForgeRegistries.ENTITY_TYPES.getKey()`
- Mixin refmap properly generated and included for production builds
- Fixed Java 17 compatibility with mixin annotation processor
- `MobDisguiseSyncPacket` for syncing Thoughtwell cognitive disguises to all clients
- `ThoughtwellEntity.setDisguise()` method for applying disguises on client side
- Improved `CognitiveDisguiseRenderer` animation using walk animation state copying

## [0.1.2] - 2025-10-09

### Added

#### Configuration
- **Comprehensive configuration system** for all major mod features
  - All configuration files are located in `config/strangematter-common.toml`
  - Hot-reloadable configuration with `/reload` command

##### World Generation
- **Anomaly Spawn Rates**
  - Enable/disable individual anomaly types
  - Configure spawn rarity for each anomaly (1 in X chunks)
  - Gravity Anomaly (default: 1/500)
  - Temporal Bloom (default: 1/500)
  - Warp Gate (default: 1/500)
  - Energetic Rift (default: 1/500)
  - Echoing Shadow (default: 1/500)
  - Thoughtwell (default: 1/500)
- **Ore Generation**
  - Enable/disable Resonite ore generation
  - Resonite ore veins per chunk (default: 3)
- **Terrain Modification**
  - Enable/disable Anomalous grass spawning
  - Resonite ore spawn chance near anomalies (default: 15%)
  - Anomaly shard ore spawn rates near anomalies

##### Anomaly Effects
- **Gravity Anomaly**
  - Enable/disable levitation effects
  - Levitation radius (default: 8 blocks)
  - Levitation force strength (default: 0.1)
  - Research points granted when scanned
- **Temporal Bloom**
  - Enable/disable temporal effects
  - Effect radius (default: 8 blocks)
  - Crop growth/de-growth stages (default: ±1-2)
  - Crop effect cooldown (default: 5 seconds)
  - Mob transformation cooldown (default: 10 seconds)
  - Research points granted
- **Energetic Rift**
  - Enable/disable energy effects
  - Zap radius (default: 6 blocks)
  - Lightning rod radius (default: 8 blocks)
  - Zap damage (default: 1.0 hearts)
  - Zap cooldown (default: 2 seconds)
  - Lightning rod cooldown (default: 10 seconds)
  - Research points granted
- **Warp Gate**
  - Enable/disable teleportation effects
  - Teleport radius (default: 2 blocks)
  - Teleport cooldown (default: 5 seconds)
  - Research points granted
- **Echoing Shadow**
  - Enable/disable shadow effects
  - Effect radius
  - Light absorption strength
  - Mob spawn boost multiplier
  - Research points granted
- **Thoughtwell**
  - Enable/disable cognitive effects
  - Effect radius
  - Confusion duration
  - Research points granted

##### Energy System
- **Resonant Burner**
  - Energy generation per tick (default: 20 RE/t)
  - Internal energy storage (default: 10,000 RE)
  - Energy transfer rate to adjacent blocks (default: 1000 RE/t)
- **Resonance Condenser**
  - Energy consumption per tick (default: 2 RE/t)
  - Internal energy storage (default: 1000 RE)
  - Shard generation progress speed (default: 15 ticks)
- **Paradoxical Energy Cell**
  - Energy transfer rate per tick (default: 1000 RE/t)
- **Reality Forge**
  - Crafting time (default: 100 ticks / 5 seconds)

##### Research System
- **Research Costs**
  - Global multiplier for all research node costs
  - Individual cost overrides for each of the 12 locked research nodes:
    - Anomaly Resonator
    - Resonance Condenser
    - Containment Basics
    - Reality Forge
    - Warp Gun
    - Stasis Projector
    - Gravity Anomalies
    - Temporal Anomalies
    - Spatial Anomalies
    - Energy Anomalies
    - Shadow Anomalies
    - Cognitive Anomalies

##### Research Minigames
- **Global Settings**
  - Enable/disable minigame requirement (instant unlock when disabled)
  - Instability mechanics:
    - Decrease rate when all minigames stable (default: 0.004/tick)
    - Base increase rate divided by number of active minigames (default: 0.001/tick)
    - More active minigames = slower instability increase
- **Energy Minigame (Wave Alignment)**
  - Required alignment time (default: 100 ticks / 5 seconds)
  - Drift delay before difficulty increases (default: 600 ticks / 30 seconds)
  - Amplitude adjustment step size (default: 0.05)
  - Period adjustment step size (default: 0.05)
- **Space Minigame (Image Unwarp)**
  - Stability threshold (default: 0.1, lower = harder)
  - Drift delay (default: 100 ticks)
  - Warp adjustment step size (default: 0.05)
- **Time Minigame (Clock Speed Matching)**
  - Speed difference threshold for stability (default: 0.15)
  - Drift delay (default: 200 ticks)
  - Speed adjustment step size (default: 0.1)
- **Gravity Minigame (Balance Scale)**
  - Balance threshold for stability (default: 0.1)
  - Drift delay (default: 200 ticks)
  - Uses continuous slider (no step adjustment)
- **Shadow Minigame (Light Routing)**
  - Alignment threshold for stability (default: 10.0)
  - Drift delay (default: 200 ticks)
  - Mirror rotation step size (default: 15 degrees)
- **Cognition Minigame (Symbol Matching)**
  - Symbol display duration (default: 100 ticks / 5 seconds)
  - Number of symbols to match (default: 4, range: 2-8)

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
