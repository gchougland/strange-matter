# Changelog

All notable changes to Strange Matter will be documented in this file.

## [0.2.7] - 2025-11-28

### Added
- **Per-Dimension Anomaly Rarity**: Added Per-Dimension Anomaly Rarity config options.
- **Shard Lamps**: Anomaly Shard colored lamps (6 types: Shade, Gravitic, Energetic, Insight, Chrono, Spatial).
- **Shard Lanterns**: Anomaly Shard colored lanterns (6 types: Shade, Gravitic, Energetic, Insight, Chrono, Spatial).
- **Shard Crystal Unpacking**: Recipe to craft crystals back into shards (1 crystal = 4 shards).

### Changed
- **Anomaly World Gen**: Modified ore/grass generation to spawn more consistently and performantly.
- **Improved FPS**: Optimized world generation and terrain modification to improve performance.
- **Default Ore Spawn Chances**: Reduced default resonite ore spawn chance from 0.5 to 0.25, and shard ore spawn chance from 0.2 to 0.1.

### Fixed
- **Echo Vacuum Duplication Bug**: Fixed issue where anomalies could appear to duplicate when collected with the echo vacuum, allowing them to be scanned for research points multiple times.
- **Research Tablet Adding**: Fixed issue where research tablets would be given every time on player login by using advancement system instead of persistent data tags.
- **Field Scanner Becoming Unusable**: Fixed issue where field scanner would become unusable due to corrupted NBT data by adding validation and cleanup logic.
- **Resonite Nugget Recipe**: Recipe was incorrectly giving only 1 nugget for an ingot, fixed to give 9.

### Removed
- **Echoing Shadow Light Absorption**: Removed light absorption feature from Echoing Shadow anomalies as it caused performance issues and lag.

## [0.2.6] - 2025-10-28

### Added
- **Dimension Config**: Added configurable list of dimensions for anomalies to spawn in.
- **Dyeable Stasis Projector**: Added the ability to right click the stasis projector with dyes to color the field.
- **Research Tablet Recipe Cycling**: Added recipe cycling in the tablet when there is more than one recipe for an item.
- **Option to hide recipes**: Added config option to hide recipes in recipe viewer mods.
- **Granular options for Anomaly Effects**: Added options to disable specific effects for anomalies.
- **Hoverboard Jump**: Added ability to jump on the hoverboard.
- **Placeable Shard Blocks**: Added shard crystal blocks for decoration.

### Fixed
- **Warp Gate Entity Language Entry**: Added missing language entry for Warp Gate Entity
- **Hoverboard**: Fixed issue with hoverboard sinking inside slabs and causing fall damage. Smoothed out turning as well.
- **Improved slider responsiveness**: Improved the responsiveness of the gravity minigames slider.
- **More Aggressive Stasis Projector**: Improved stasis projector to better counteract gravity from other mods.
- **Optimized World Gen**: Removed logging and improved peformance of anomaly world gen.
- **Organized Creative Menu**: Organized the items in the creative menu by type.
- **Research Tablet Recipe Lookup**: Changed the recipe view in the tablet to lookup recipes by item rather than recipe ID.

## [0.2.5] - 2025-10-20

### Added
- **Hoverboard Vehicle**: Revolutionary personal transportation device
  - **Anti-Gravity Technology**: Hovers smoothly above any surface with configurable height
  - **Momentum-Based Movement**: Realistic physics with acceleration, friction, and momentum
  - **Smart Terrain Navigation**: Automatically adjusts hover height based on surrounding blocks
  - **Speed Boost System**: Control key provides enhanced acceleration and top speed
  - **Smooth Controls**: W/S for forward/backward movement, follows player look direction for rotation
  - **Reality Forge Recipe**: Craftable using resonite ingots, resonant circuit, and containment capsule
  - **Research Integration**: Locked behind containment basics research with configurable costs
  - **Configurable Performance**: Adjustable max speed and acceleration via config file

## [0.2.4] - 2025-10-19

### Added
- **Advancement System**: Complete progression tracking system with 12 unique advancements
  - **Discovery & Foundation**: First Contact, The Researcher, Anomaly Collector
  - **Power & Energy**: Energy Harvester
  - **Tools & Equipment**: Field Researcher, Anomaly Hunter, Reality Extractor, Portal Master
  - **Advanced Crafting**: Reality Forge
  - **Research Mastery**: Knowledge Seeker, Research Master
  - **World Interaction**: Anomaly Tamer
  - **Custom Triggers**: Advanced tracking for anomaly effects, scanning, research completion, and containment
  - **Custom Tab**: Dedicated Strange Matter advancement tab with custom background
- **Research Points Conditional Commands**: Command system for conditional execution based on player research progress
  - **Execute If Integration**: Use research points as conditions in `/execute if` commands
  - **Scoreboard Integration**: Store research check results for complex conditional logic
  - **Command Block Ready**: Perfect for map makers and server administrators
  - **All Research Types**: Support for all six research categories with range checking
  - **Target Selectors**: Full support for Minecraft target selectors (@a, @p, @r, etc.)

### Fixed
- **Scanning anomalies from capsules**: You can no longer scan anomalies that were spawned from capsules
- **Summoned warp gates function**: Warp gates spawned through /summon were inactive by default not they are active
- **Resonite blasting recipe**: Fixed raw resonite blasting recipe
- **Configurable Anomaly Scientist Rarity**: Added configuration option for setting the rarity of the anomaly scientist villager house
- **Research Machine Texture Path**: Replaced MTL file texture paths with correct ones

### Improved
- **Time Research Minigame**: 
  - Added faded hand that moves at the goal speed, providing visual target for synchronization
  - Enhanced auto-snap feature for smoother speed adjustments
  - Improved visual feedback with semi-transparent faded hand
- **Gravity Research Minigame**:
  - Completely redesigned balance system with discrete integer values (-5 to 5)
  - Added 11-notch slider with snap-to-position functionality
  - Fixed stabilization logic - cube now properly centers when balanced
  - Improved drift system - randomly selects new target gravity instead of gradual drift
  - Increased drift delay to 30 seconds for more rewarding gameplay
  - Enhanced physics calculation for better balance detection

## [0.2.3] - 2025-10-16

### Fixed
- **Echo Vacuum Recipe**: Fixed typo causing recipe to not function
- **Gravity Issues**: Fixed gravity persisting between worlds and after containment
- **Reality Forge Energy Storage**: Removed energy storage that was causing confusion with Reality Forge
- **Warp Gun Server Error**: Fixed WarpGunEventHandler to only send packets client side

## [0.2.2] - 2025-10-15

### Added

- **Levitation Pad**
  - New advanced device that creates controlled levitation fields for safe transportation and anti-gravity applications
  - **Dual-mode operation**: Right-click to toggle between UP and DOWN modes
    - **UP Mode (Default)**: Lifts entities upward within 16 blocks, stops at first solid block encountered
    - **DOWN Mode**: Lowers entities to ground level with controlled descent
  - **Universal entity support**: Works on players, mobs, and items with specialized handling for each
  - **Fall damage prevention**: Eliminates fall damage during descent in both modes
  - **Smart beam detection**: 
    - Passes through open trapdoors (allows beam gates)
    - Blocked by closed trapdoors and all other solid blocks
    - Visual beam matches effect range exactly
  - **Advanced player levitation**: Uses gravity manipulation system for smooth, natural movement
  - **Item management**: Removes momentum, centers items in beam, applies gentle floating motion
  - **Visual effects**: Animated levitation beam with UV scrolling based on mode direction
  - **Research integration**: Unlocked via "Levitation Pad" research node requiring Reality Forge
  - **Comprehensive documentation**: Three-page info system with introduction, mechanics, and applications
  - **Configuration support**: Research cost configurable via config file
    - `levitationPad` (default: Gravity 15, Energy 10)

- **Throwable Containment Capsules**
  - **New deployment method**: Right-click any filled containment capsule to throw it like a potion
  - **Anomaly deployment**: When capsule hits the ground, it spawns the contained anomaly at the impact location

### Changed

- **Graviton Hammer Improvements**
  - **Precision mining mode**: Crouch + left-click now mines only a single block instead of 3x3 area

## [0.2.1] - 2025-10-14

### Added

- **Graviton Hammer**
  - New powerful mining tool that manipulates gravity for area-of-effect mining
  - **Left-click mining**: Mines a 3x3 area of blocks with orientation based on block face
    - Horizontal plane mining for top/bottom faces
    - Perpendicular plane mining for cardinal faces
  - **Right-click charging**: Hold to charge up the hammer with three distinct levels
    - **Level 1**: 3-block deep tunnel (configurable, default: 1 second charge)
    - **Level 2**: 6-block deep tunnel (configurable, default: 2 seconds charge)  
    - **Level 3**: 9-block deep tunnel (configurable, default: 3 seconds charge)
  - **Charge release**: Mines a 3x3 tunnel extending in the direction you're looking
  - **Tool properties**: Diamond pickaxe mining level, iron pickaxe speed, infinite durability, cannot be enchanted
  - **Research integration**: Unlocked via "Graviton Hammer" research node requiring Reality Forge
  - **Configuration support**: All charge times and tunnel depths configurable via config file
    - `gravitonHammerChargeLevel1Time` (default: 20 ticks)
    - `gravitonHammerChargeLevel2Time` (default: 40 ticks)
    - `gravitonHammerChargeLevel3Time` (default: 60 ticks)
    - `gravitonHammerTunnelDepthLevel1` (default: 3 blocks)
    - `gravitonHammerTunnelDepthLevel2` (default: 6 blocks)
    - `gravitonHammerTunnelDepthLevel3` (default: 9 blocks)

### Fixed

- **Research System Display**
  - Fixed Reality Forge recipe display in research info pages to show correct shard quantities (e.g., "6x" instead of just showing one of each shard)
  - Improved info page content formatting and reduced page lengths to fit properly within GUI constraints
  - Updated research documentation tone from "all caps exclamations" to scientific journal style

## [0.2.0] - 2025-10-13

### Added

- **Resonant Conduits**
  - Added Resonant Conduit block for transferring resonant energy between machines
  - Modular conduit system that connects from any side depending on adjacent machines or conduits
  - Dynamic rendering system with custom UV mapping:
    - Straight connections: 5x5 pixel tubes
    - Corners/intersections: 7x7 pixel joint in center with 5x5 tubes extending
    - Joint only renders when needed (not for straight across connections)
    - Single texture with different UV coordinates for tubes and joints
  - Custom block entity renderer with proper texture atlas integration
  - Crafting recipe: 4 Resonite Ingots + 4 Resonant Circuits → 8 Resonant Conduits
  - Conduits act as pure energy routers (no energy storage) with direct source-to-sink routing
  - Event-driven network updates with BFS pathfinding for energy transfer optimization
  - Compatible with all existing energy machines (Resonant Burner, Resonance Condenser, etc.)

- **Enhanced Energy System**
  - **Role-Based Energy Transfer**: Machines now have defined energy roles (GENERATOR, CONSUMER, BOTH, ENERGY_INDEPENDENT)
  - **Unidirectional Energy Flow**: 
    - Resonant Burner: Only supplies energy (GENERATOR role)
    - Resonance Condenser: Only accepts energy (CONSUMER role)
    - Reality Forge: Energy independent (ENERGY_INDEPENDENT role)
    - Rift Stabilizer: Only supplies energy (GENERATOR role)
  - **Improved Client/Server Synchronization**: Fixed energy bar jumping issues with proper block entity data sync
  - **Every-Tick Processing**: All machines now process every tick instead of every 20 ticks for better responsiveness
  - **Capability Exposure Control**: Energy capabilities only exposed on configured input/output sides

### Fixed

- **Energy Transfer Issues**
  - Fixed energy bars jumping up and down during transfer operations
  - Fixed client/server synchronization conflicts by ensuring energy logic runs only server-side
  - Fixed energy transfer between multiple machines of the same type (generators no longer transfer to each other)
  - Fixed conduit allowing cross-transfer between incompatible machine types
  - Improved energy transfer batching to prevent multiple sync packets from conflicting

- **Rift Stabilizer Integration**
  - Modified Rift Stabilizer to extend BaseMachineBlockEntity for full integration with role-based energy system
  - Fixed Rift Stabilizer energy generation by resolving internal energy storage configuration
  - Added proper tick counter system for rift detection (every 20 ticks) while maintaining every-tick energy generation
  - Fixed energy output configuration to match block facing direction

- **Machine Timing**
  - Fixed BaseMachineBlockEntity to call processMachine() every tick instead of every 20 ticks
  - Individual machines can now implement their own internal timing for specific operations
  - Improved responsiveness for all energy generation and consumption operations
  - Removed global tick synchronization in favor of distributed processing

- **Village Structure Integration**
  - Fixed `NoSuchFieldException: rawTemplates` error on dedicated servers
  - Enhanced field detection to handle both obfuscated (f_210559_, f_210560_) and deobfuscated (rawTemplates, templates) field names
  - Added robust error handling and debug logging for field discovery
  - Village structure injection now works reliably across all server environments (client, server, dedicated server)


## [0.1.7] - 2025-10-12

### Added
- **Decoration Blocks**
  - Added Resonite Tile - decorative building block crafted with 2 Resonite Ingots + 2 Stone → 4 Tiles
  - Added Resonite Tile Stairs - stairs variant crafted with 6 Resonite Tiles → 4 Stairs
  - Added Resonite Tile Slab - slab variant crafted with 3 Resonite Tiles → 6 Slabs
  - Added Fancy Resonite Tile - upgraded decorative variant crafted with 2x2 Resonite Tiles → 4 Fancy Tiles
  - Added Resonite Pillar - directional pillar block (placeable like logs) crafted with 2 Resonite Tiles → 2 Pillars
  - Added Resonite Door - functional door that opens like wooden doors, crafted with 6 Resonite Ingots → 3 Doors
  - Added Resonite Trapdoor - functional trapdoor with transparency, crafted with 6 Resonite Ingots → 2 Trapdoors
  - All decoration blocks are mineable with pickaxe and have appropriate loot tables
  - Villagers can now pathfind through and open Resonite Doors (added to `minecraft:wooden_doors` tag)

- **Villager Profession: Anomaly Scientist**
  - Added new villager profession that uses the Research Machine as their job site block
  - Villagers will claim Research Machines and convert to Anomaly Scientists naturally
  - Added to `acquirable_job_site` tag so unemployed villagers can acquire the profession
  - Anomaly Scientist Labs now generate naturally in all village types (plains, desert, savanna, snowy, taiga)
  - Structure generation uses Forge's event-based injection system (TagsUpdatedEvent + reflection)
  - Lab structures properly integrate with village jigsaw system using `minecraft:building_entrance` connection
  - **Novice trades**: Raw Resonite and basic resources
  - **Apprentice trades**: Resonite Ingots and Nuggets
  - **Journeyman trades**: Resonant Coils and basic shards (Gravitic, Spatial)
  - **Expert trades**: Stabilized Cores and advanced shards (Chrono, Energetic)
  - **Master trades**: Resonant Circuits, rare shards (Shade, Insight), and Resonite Blocks
  
### Fixed
- **Research Machine Structure Rotation**
  - Fixed Research Machine not rotating correctly when placed in village structures
  - Added `rotate()` and `mirror()` methods to Research Machine block for proper jigsaw structure integration
  - Research Machines now correctly orient themselves when village structures are rotated by the jigsaw system
  - Updated blockstate JSON to declare all facing variants for structure compatibility
  
- **Placement Modifier Registration**
  - Fixed world creation crash caused by incorrect placement modifier registration
  - Corrected ConfiguredRarityFilter and ConfiguredCountPlacement registration to use proper double-lambda syntax
  - Added exception handling in config access methods to prevent initialization order issues

- **Player Logout Crash**
  - Fixed occasional crash when logging out of worlds due to PlayerMorphData classloading race condition
  - Added try-catch block and null checks to prevent NoClassDefFoundError during logout

## [0.1.6] - 2025-10-12

### Fixed
- **Dedicated Server Compatibility**
  - Fixed critical crash when loading mod on dedicated servers (`ClientLevel` loading error)
  - Moved `ClientModEvents` from inner class to separate file to prevent client class loading on server
  - Removed all client-only imports from common code (entities, items, blocks, network packets)
  - Created `ScreenHelper` utility to isolate GUI class instantiation
  - Fixed JEI plugin to use `FMLEnvironment.dist` guards instead of `Minecraft.getInstance()`
  - Fixed all network packet handlers to use `DistExecutor` for client-only method calls
  - Fixed client mixin imports to use fully qualified names instead of direct imports
  - Separated player morph event handlers into client and server classes

- **Research System on Dedicated Servers**
  - Fixed research nodes not initializing on dedicated server
  - Research nodes now initialize during common setup (runs on both client and server)
  - Research Machine now works properly when inserting research notes on dedicated servers

- **Stasis Projector**
  - Fixed bug where adjacent stasis projectors would steal items/mobs from each other
  - Stasis projectors now only capture entities that aren't already being held by another projector

### Changed
- Reorganized client-only code for better maintainability and server compatibility
- Created new helper classes: `ScreenHelper`, `EchoVacuumEventHandler`, `PlayerMorphClientEventHandler`
- Particle and menu screen registration consolidated into `ClientModEvents`
- Sound manager initialization moved to client-only setup

## [0.1.5] - 2025-10-11

### Added
- **KubeJS Integration - Recipes**
  - Added full KubeJS plugin support for modpack creators
  - Modpack creators can now add, modify, and remove Reality Forge recipes using KubeJS scripts
- **KubeJS Integration - Research System**
  - Added support for creating custom research nodes through KubeJS
  - Can add custom info pages to any research node (vanilla or custom)
  - Full builder API with method chaining
  - Support for all research features: costs, prerequisites, icons, positioning
  - Custom research nodes appear in the research tree alongside vanilla nodes
  - **Recipe Features:**
    - 3x3 shaped crafting patterns
    - Anomaly shard requirements
    - Research requirements
    - Custom result items
  - **Research Features:**
    - Custom research nodes with costs, prerequisites, and icons
    - Custom info pages with text, recipes, and screenshots
    - Integration with vanilla research tree
    - Automatic translation key support
  - Example KubeJS recipe:
    ```javascript
    ServerEvents.recipes(event => {
      event.custom({
        type: 'strangematter:reality_forge',
        pattern: ['DDD', 'DED', 'DDD'],
        key: {
          'D': { item: 'minecraft:diamond' },
          'E': { item: 'strangematter:resonite_ingot' }
        },
        result: { item: 'strangematter:some_item' },
        shards: {
          'gravitic': 1,
          'energetic': 1,
          'chrono': 1,
          'insight': 1,
          'shade': 1,
          'spatial': 1
        },
        required_research: 'reality_forge'
      })
    })
    ```
  - Example KubeJS research node:
    ```javascript
    // In startup_scripts/research.js
    const StrangeMatter = Java.loadClass('com.hexvane.strangematter.kubejs.StrangeMatterHelper');

    // Example 1: Simple custom research node
    let simpleNode = StrangeMatter.createResearchNode('custom_simple_research');
    simpleNode.category('general');
    simpleNode.position(150, 250);  // X and Y coordinates in the research tree GUI
    simpleNode.cost('gravity', 15);
    simpleNode.cost('energy', 10);
    simpleNode.iconItem('minecraft:diamond');
    simpleNode.prerequisite('reality_forge');  // Must unlock Reality Forge first
    
    // Add info pages to custom research
    let pagesBuilder = StrangeMatter.createInfoPagesBuilder('custom_simple_research');

    // Page 1: Introduction (must call .build() to finalize)
    pagesBuilder.page()
        .title('Custom Research Introduction')
        .content('This is a custom research node added via KubeJS! You can add multiple pages to explain your custom content.')
        .build();

    // Page 2: With a recipe
    pagesBuilder.page()
        .title('Crafting Recipe')
        .content('Here is how to craft the result of this research:')
        .recipe('minecraft:chest')  // Shows vanilla diamond recipe as example
        .build();
    // Finish and register all pages
    pagesBuilder.finish();
    ```
  - KubeJS is an optional dependency - mod works without it

### Technical
- **KubeJS Integration Classes:**
  - `StrangeMatterKubeJSPlugin` - Main plugin registration
  - `StrangeMatterHelper` - Global bindings for scripts
  - `ResearchNodeBuilder` - Builder API for research nodes
  - `ResearchInfoPageBuilder` - Builder API for info pages
  - `CustomResearchRegistry` - Registry for custom research content
  - `ResearchInfoPage` - Data class for page information
- Reality Forge recipes work through KubeJS `event.custom()` with automatic JSON handling
- Research nodes registered through reflection (optional dependency safe)
- KubeJS plugin registered via META-INF/services
- KubeJS is a compile-only dependency (not bundled with mod)
- Recipes use standard JSON format compatible with datapacks
- Players must install KubeJS separately if they want scripting support
- Custom research integrates seamlessly with vanilla research system

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
