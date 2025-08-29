⚡ Gravity Anomaly – In-Game Design Spec
🌌 Overall Presentation

Category: Natural anomaly (Euclid-class).

Form: Floating, unstable geometric object surrounded by warped visual effects.

Scale: About the size of a 1-block structure, but appears larger due to orbiting/glitch effects.

Player Interaction: Pulls nearby items/mobs upward in a soft levitation field.

🧱 Core Components
1. Central Icosahedron (The Heart of the Anomaly)

Shape: 20-sided polyhedron (icosahedron), floating about 1 block above ground.

Texture:

Base: Dark obsidian-like stone (deep purple/black pixel base).

Edges: Bright cyan-green glowing outlines (16×16 animated texture cycling brightness).

Faces: Subtle shimmering noise, faint “cracks” glowing from inside.

Animation:

Slow rotation (like an End Crystal, but geometric and deliberate).

Occasional flicker/pulse of brightness.

2. Resonant Aura (Outer Shell)

Appearance: Transparent spherical field surrounding the core (radius ~2 blocks).

Texture:

Semi-transparent particle sheet with warped gridlines (space distortion).

Shaders simulate slight refraction/distortion of blocks behind it.

Animation:

Slow pulsing waves outward, like ripples in glass.

Particle “shards” orbit core (small fragments shaped like distorted triangles).

3. Levitation Field (Gameplay Effect Layer)

Effect Zone: Radius 4–5 blocks around anomaly.

Visuals:

Floating pixel particles drifting upward (like falling sand particles, but reversed).

Random nearby blocks “tremble” slightly (minor sub-pixel offset animation).

Interaction:

Items & mobs within field gently float upward.

Player experiences mild levitation effect if too close.

🎨 Texture Breakdown (Minecraft 16×16 Style)

Icosahedron Faces:

Base texture: dark purple/black stone.

Overlay: cyan glowing cracks (animated 2–3 frame cycle).

Icosahedron Edges:

Neon cyan-green outline pixels (like Nether portal color shifting).

Aura Particles:

Translucent cyan shards, 16×16 soft glow textures.

Faint warped grid (like an End portal texture fragment).

Levitation Particles:

White → cyan dots rising slowly, similar to Ender particles but drifting straight upward.

🔊 Sound Design

Constant low rumble/hum (like End Crystal, but bassier).

Occasional whoosh when levitation spikes.

Subtle “glass creak” when close (gravity tension).

🕹️ Behavior

Idle State: Slowly rotates, hums, pulls items upward.

Player Interaction:

Scannable → grants Gravity Research Points.

Breaking attempt without containment → spawns “Paradox Slimes” or causes instability burst.

Containment:

Placing a Containment Anchor nearby stabilizes it, halting its pull.

Contained anomaly can be harvested for Gravitic Shards (crafting).