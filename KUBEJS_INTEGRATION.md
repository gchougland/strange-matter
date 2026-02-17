# Strange Matter - KubeJS Integration Guide

This guide explains how to use KubeJS with Strange Matter to create custom recipes, research nodes, and info pages.

## Table of Contents
- [Requirements](#requirements)
- [Recipe Integration](#recipe-integration)
- [Research Category Integration](#research-category-integration)
- [Research Node Integration](#research-node-integration)
- [Info Pages Integration](#info-pages-integration)
- [Translation Keys](#translation-keys)
- [Complete Examples](#complete-examples)

## Requirements

To use Strange Matter with KubeJS, you need:
- **Strange Matter** (this mod)
- **KubeJS Forge**
- **Rhino** (KubeJS dependency)
- **Architectury API** (KubeJS dependency)

Strange Matter works fine without KubeJS installed - the integration is completely optional.

## Recipe Integration

### Adding Reality Forge Recipes

Reality Forge recipes are added in `server_scripts` using the standard KubeJS recipe system.

**Location:** `kubejs/server_scripts/recipes.js`

```javascript
ServerEvents.recipes(event => {
  event.custom({
    type: 'strangematter:reality_forge',
    pattern: [
      'DDD',
      'DED',
      'DDD'
    ],
    key: {
      'D': { item: 'minecraft:diamond' },
      'E': { item: 'strangematter:resonite_ingot' }
    },
    result: { 
      item: 'minecraft:nether_star',
      count: 1
    },
    shards: {
      'gravitic': 5,
      'energetic': 5,
      'chrono': 5,
      'insight': 5,
      'shade': 5,
      'spatial': 5
    },
    required_research: 'reality_forge'
  }).id('modpack:custom_nether_star');
});
```

### Recipe Parameters

- **type**: Always `'strangematter:reality_forge'`
- **pattern**: Array of 3 strings, each 3 characters long (3x3 grid)
- **key**: Maps pattern characters to items
- **result**: Output item and optional count
- **shards** *(optional)*: Map of shard types to amounts required, cannot exceed 6 total shards
  - Shard types: `gravitic`, `energetic`, `chrono`, `insight`, `shade`, `spatial`
- **required_research** *(optional)*: Research node ID that must be unlocked

### Removing Recipes

```javascript
ServerEvents.recipes(event => {
  // Remove a specific recipe
  event.remove({ id: 'strangematter:reality_forge/some_recipe' });
  
  // Remove all Reality Forge recipes
  event.remove({ type: 'strangematter:reality_forge' });
  
  // Remove recipes by output
  event.remove({ type: 'strangematter:reality_forge', output: 'strangematter:specific_item' });
});
```

## Research Category Integration

### Creating Custom Research Categories

Research categories allow you to organize research nodes into separate tabs in the research screen. Categories can be hidden until certain research nodes are unlocked.

Categories MUST be created in `startup_scripts` (not server_scripts) because they need to be registered before the game fully loads.

**Location:** `kubejs/startup_scripts/research.js`

```javascript
// Create a custom category
StrangeMatter.registerCategory(
  StrangeMatter.createCategory('my_custom_category')
    .name('My Custom Category')
    .iconItem('minecraft:diamond')
    .unlockRequirement('reality_forge')  // Hidden until reality_forge unlocks
    .order(10)  // Display order (lower = earlier)
);
```

### Research Category Builder API

#### Methods

**`.name(string)`** - Set the display name
- Can be a plain string: `'My Category'`
- Or a translation key: `'research.category.mymod.my_category'`

**`.iconTexture(string)`** - Set a custom texture icon for the tab
- Example: `'mymod:textures/ui/category_icon.png'`
- Texture should be 11x11 pixels (will be centered in 13x11 tab)

**`.iconItem(string)`** - Set an item icon for the tab
- Example: `'minecraft:diamond'`
- Item icon will be rendered in the tab

**`.unlockRequirement(string)`** - Set the research node ID that must unlock before category becomes visible
- If not set, category is always visible
- Example: `'.unlockRequirement('reality_forge')'` - category hidden until reality_forge research unlocks

**`.order(int)`** - Set display order priority
- Lower numbers appear first (left side)
- Default is 100
- Example: `.order(5)` - appears before categories with higher order

**`.build()`** - Create the category (called automatically by `registerCategory`)

### Category Examples

```javascript
// Basic category (always visible)
StrangeMatter.registerCategory(
  StrangeMatter.createCategory('basic_research')
    .name('Basic Research')
    .iconItem('minecraft:book')
);

// Category with unlock requirement
StrangeMatter.registerCategory(
  StrangeMatter.createCategory('advanced_research')
    .name('Advanced Research')
    .iconItem('minecraft:nether_star')
    .unlockRequirement('reality_forge')  // Hidden until reality_forge unlocks
    .order(20)
);

// Category with custom texture icon
StrangeMatter.registerCategory(
  StrangeMatter.createCategory('custom_category')
    .name('Custom Category')
    .iconTexture('mymod:textures/ui/custom_tab_icon.png')
    .unlockRequirement('some_research_node')
    .order(15)
);
```

### Category Translation Keys

Category names can use translation keys. The default translation key format is:
- `research.category.strangematter.{category_id}`

You can also use custom translation keys by passing them to `.name()`:

```javascript
// In startup_scripts/research.js
StrangeMatter.registerCategory(
  StrangeMatter.createCategory('my_category')
    .name('research.category.mymod.my_category')  // Translation key
    .iconItem('minecraft:diamond')
);

// In assets/mymod/lang/en_us.json
{
  "research.category.mymod.my_category": "My Custom Category"
}
```

## Research Node Integration

### Creating Custom Research Nodes

Research nodes MUST be created in `startup_scripts` (not server_scripts) because they need to be registered before the game fully loads.

**Location:** `kubejs/startup_scripts/research.js`

```javascript
// If StrangeMatter binding doesn't work, use this fallback:
if (typeof StrangeMatter === 'undefined') {
  global.StrangeMatter = Java.loadClass('com.hexvane.strangematter.kubejs.StrangeMatterHelper');
}

// Simple research node
StrangeMatter.registerNode(
  StrangeMatter.createResearchNode('my_custom_research')
    .category('custom')
    .position(150, 250)  // X and Y coordinates in research GUI
    .cost('gravity', 15)
    .cost('energy', 10)
    .iconItem('minecraft:diamond')
    .prerequisite('reality_forge')
);
```

> **Note:** The `StrangeMatter` binding is automatically available when KubeJS loads the plugin. The fallback using `Java.loadClass()` is only needed if there are binding issues.

### Research Node Builder API

#### Methods

**`.category(string)`** - Set the category
- Use the category ID you created with `createCategory()`
- Default categories: `'general'`, `'reality_forge'`
- Custom categories: Use your own category IDs

**`.position(x, y)`** or **`.x(x).y(y)`** - Set position in research tree
- Use coordinates relative to other nodes
- Vanilla nodes range from about -240 to 280 in each direction

**`.cost(type, amount)`** - Add research point cost
- Types: `'gravity'`, `'time'`, `'space'`, `'energy'`, `'shadow'`, `'cognition'`
- Amount: Positive integer

**`.costs(map)`** - Set multiple costs at once
- Example: `.costs({ 'gravity': 10, 'energy': 5 })`

**`.iconItem(itemId)`** - Set item icon
- Example: `.iconItem('minecraft:diamond')`

**`.iconTexture(path)`** - Set custom texture icon
- Example: `.iconTexture('strangematter:textures/ui/custom_icon.png')`

**`.prerequisite(nodeId)`** - Add prerequisite research
- Node must be unlocked before this one
- Can be called multiple times for multiple prerequisites

**`.requiresMultipleAspects(boolean)`** - Set aspect requirement
- `true`: ALL cost types must be met
- `false` (default): ANY single cost type unlocks it

### Advanced Research Example

```javascript
StrangeMatter.registerNode(
  StrangeMatter.createResearchNode('advanced_reality_manipulation')
    .category('advanced')
    .position(200, 300)
    .cost('space', 30)
    .cost('time', 25)
    .cost('gravity', 20)
    .iconItem('minecraft:nether_star')
    .requiresMultipleAspects(true)  // Need ALL three aspects
    .prerequisite('warp_gun')
    .prerequisite('stasis_projector')
    .prerequisite('rift_stabilizer')
);
```

## Info Pages Integration

### Adding Info Pages

Info pages are displayed when viewing a research node in the Research Tablet.

```javascript
// Add pages to your custom research
StrangeMatter.addInfoPages('my_custom_research', builder => {
  // Page 1: Introduction
  builder.page()
    .title('Introduction to Custom Research')
    .content('This custom research unlocks amazing new possibilities!');
  
  // Page 2: Recipe
  builder.page()
    .title('Crafting Recipe')
    .content('Here is how to craft the result:')
    .recipe('my_custom_item');
  
  // Page 3: Reality Forge Recipe
  builder.page()
    .title('Advanced Crafting')
    .content('Use the Reality Forge for advanced crafting:')
    .realityForgeRecipe('my_advanced_item');
  
  // Page 4: Screenshot
  builder.page()
    .title('Visual Guide')
    .content('Here is how it looks in-game:')
    .screenshot('modpack:textures/ui/my_screenshot.png');
});
```

### Info Page Builder API

**`.title(string)`** - Page title (required)

**`.content(string)`** - Page description text
- Use `\n` for line breaks
- Can be plain text or a translation key

**`.recipe(recipeId)`** - Show a crafting recipe
- Recipe ID without namespace: `'diamond'` for vanilla recipes
- Full ID: `'strangematter:research_tablet'`

**`.realityForgeRecipe(recipeId)`** - Show a Reality Forge recipe with special rendering

**`.screenshot(path)`** - Display an image
- Path to texture: `'modpack:textures/ui/screenshot.png'`

### Adding Pages to Vanilla Research

You can also add custom pages to vanilla research nodes:

```javascript
StrangeMatter.addInfoPages('reality_forge', builder => {
  builder.page()
    .title('Modpack Custom Information')
    .content('This is additional information added by the modpack!');
});
```

## Translation Keys

### Research Node Names

Create translation files for your custom research:

**Location:** `kubejs/assets/kubejs/lang/en_us.json`

```json
{
  "research.strangematter.my_custom_research.name": "Custom Research",
  "research.strangematter.my_custom_research.description": "Unlocks custom features",
  
  "research.strangematter.advanced_reality_manipulation.name": "Advanced Reality Manipulation",
  "research.strangematter.advanced_reality_manipulation.description": "Master the fabric of reality"
}
```

### Naming Convention

- Node name: `research.strangematter.<node_id>.name`
- Node description: `research.strangematter.<node_id>.description`
- Page titles/content: Use plain text or create custom keys

## Complete Examples

### Example 1: Custom Category with Research Nodes

```javascript
// In startup_scripts/custom_research.js

// First, create a custom category
StrangeMatter.registerCategory(
  StrangeMatter.createCategory('gregtech_research')
    .name('GregTech Integration')
    .iconItem('gtceu:basic_circuit')
    .unlockRequirement('resonant_energy')  // Hidden until resonant_energy unlocks
    .order(10)
);

// Tier 1: Basic Circuit Research (in custom category)
StrangeMatter.registerNode(
  StrangeMatter.createResearchNode('gt_basic_circuits')
    .category('gregtech_research')  // Use custom category
    .position(-100, 100)
    .cost('energy', 20)
    .iconItem('gtceu:basic_circuit')
    .prerequisite('resonant_energy')
);

StrangeMatter.addInfoPages('gt_basic_circuits', builder => {
  builder.page()
    .title('Basic Circuit Integration')
    .content('Integrate resonant energy with basic circuits for advanced automation.');
  builder.page()
    .title('Circuit Assembly')
    .realityForgeRecipe('gt_resonant_circuit_board');
});

// Tier 2: Advanced Circuits (requires Tier 1)
StrangeMatter.registerNode(
  StrangeMatter.createResearchNode('gt_advanced_circuits')
    .category('gregtech_research')  // Same category
    .position(-100, 200)
    .cost('energy', 40)
    .cost('space', 30)
    .iconItem('gtceu:advanced_circuit')
    .prerequisite('gt_basic_circuits')
    .requiresMultipleAspects(true)
);
```

### Example 2: Custom Anomaly Research

```javascript
// In startup_scripts/custom_anomalies.js

// New anomaly type research
StrangeMatter.registerNode(
  StrangeMatter.createResearchNode('quantum_anomalies')
    .category('anomalies')
    .position(0, -300)
    .cost('space', 25)
    .cost('time', 25)
    .cost('energy', 25)
    .iconItem('modpack:quantum_core')
    .prerequisite('spatial_anomalies')
    .prerequisite('temporal_anomalies')
    .prerequisite('energy_anomalies')
    .requiresMultipleAspects(true)
);

StrangeMatter.addInfoPages('quantum_anomalies', builder => {
  builder.page()
    .title('Quantum Entanglement')
    .content('By combining spatial, temporal, and energetic research, you have discovered quantum anomalies.');
  builder.page()
    .title('Quantum Detector')
    .realityForgeRecipe('quantum_detector');
  builder.page()
    .title('Properties')
    .content('Quantum anomalies exist in superposition until observed...')
    .screenshot('modpack:textures/research/quantum_anomaly.png');
});
```

### Example 3: Recipe-Locked Progression

```javascript
// In startup_scripts/research.js
StrangeMatter.registerNode(
  StrangeMatter.createResearchNode('ultimate_crafting')
    .position(250, 0)
    .cost('gravity', 50)
    .cost('time', 50)
    .cost('space', 50)
    .cost('energy', 50)
    .cost('shadow', 50)
    .cost('cognition', 50)
    .iconItem('modpack:creative_core')
    .requiresMultipleAspects(true)
    .prerequisite('reality_forge')
);

// In server_scripts/recipes.js
ServerEvents.recipes(event => {
  event.custom({
    type: 'strangematter:reality_forge',
    pattern: [
      'NSN',
      'DCD',
      'NSN'
    ],
    key: {
      'N': { item: 'minecraft:nether_star' },
      'S': { item: 'strangematter:stabilized_core' },
      'D': { item: 'minecraft:dragon_egg' },
      'C': { item: 'modpack:creative_core_frame' }
    },
    result: { item: 'modpack:creative_core' },
    shards: {
      'gravitic': 100,
      'energetic': 100,
      'chrono': 100,
      'insight': 100,
      'shade': 100,
      'spatial': 100
    },
    required_research: 'ultimate_crafting'  // Locked behind research
  });
});
```

## Tips and Best Practices

1. **Use startup_scripts for research** - Research nodes and categories MUST be in startup_scripts, not server_scripts
2. **Use server_scripts for recipes** - Recipes go in server_scripts
3. **Create categories before nodes** - Register categories before creating nodes that use them
4. **Plan your coordinates** - Sketch out your research tree positions before implementing
5. **Test prerequisite chains** - Make sure research unlocks in the right order
6. **Use category unlock requirements** - Hide advanced categories until players unlock prerequisite research
7. **Add translations** - Always add translation keys for category names and research nodes
8. **Document custom research** - Add detailed info pages explaining your custom content
9. **Order categories logically** - Use `.order()` to control category tab display order

## Debugging

### "StrangeMatter is not defined" Error

If you get this error in your scripts:

**Solution 1: Add fallback at top of script**
```javascript
if (typeof StrangeMatter === 'undefined') {
  global.StrangeMatter = Java.loadClass('com.hexvane.strangematter.kubejs.StrangeMatterHelper');
}
```

**Solution 2: Verify KubeJS is installed**
- Check that KubeJS, Rhino, and Architectury are in your mods folder
- Look for `[Strange Matter] StrangeMatter binding registered!` in console

### Other Issues

If something else isn't working:

1. Check the console/logs for errors
2. Verify KubeJS is installed and loaded
3. Ensure research scripts are in `startup_scripts`, not `server_scripts`
4. Verify research node IDs are unique
5. Check that prerequisite node IDs exist
6. Test with `/reload` command (only works for recipes, not research)
7. Restart the game after changing startup scripts

## Support

For issues specific to:
- **KubeJS**: Visit the KubeJS Discord or GitHub
- **Strange Matter**: Visit the Strange Matter Discord or GitHub
- **Integration**: Check both mod pages for known compatibility issues
