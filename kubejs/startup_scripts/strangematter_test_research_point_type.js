// Strange Matter - Test script for custom research point types
// Registers a custom type "test_mana" and a research node that uses it as a cost.
// Copy to your game instance's kubejs/startup_scripts/ (e.g. run/kubejs/startup_scripts/).
// Grant custom points via commands when supported, or use for testing the UI.

const StrangeMatter = (typeof global.StrangeMatter !== 'undefined')
  ? global.StrangeMatter
  : Java.loadClass('com.hexvane.strangematter.kubejs.StrangeMatterHelper');

// Register custom research point type
StrangeMatter.registerResearchPointType(
  StrangeMatter.createResearchPointType('test_mana')
    .name('Mana')
    .iconItem('minecraft:experience_bottle')
);

// Node that mixes custom + built-in (gives research note, use Research Machine)
StrangeMatter.registerNode(
  StrangeMatter.createResearchNode('test_mana_research')
    .category('general')
    .position(160, 0)
    .cost('test_mana', 10)
    .cost('energy', 5)
    .iconItem('minecraft:experience_bottle')
    .prerequisite('research')
);

// Node that uses only test_mana (instant unlock, no research note)
StrangeMatter.registerNode(
  StrangeMatter.createResearchNode('test_mana_only')
    .category('general')
    .position(200, 0)
    .cost('test_mana', 15)
    .iconItem('minecraft:experience_bottle')
    .prerequisite('test_mana_research')
);

console.info('[Strange Matter] Registered custom research point type "test_mana" and nodes "test_mana_research", "test_mana_only". Open the Research Tablet to see them.');
