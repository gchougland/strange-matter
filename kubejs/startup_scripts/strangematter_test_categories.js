// Strange Matter - Test script for custom research categories
// Adds enough categories to overflow tabs to the right side of the Research Tablet.
//
// Copy this file to your game instance's kubejs/startup_scripts/ folder
// (e.g. run/kubejs/startup_scripts/ when developing).
// With default GUI height, ~6 tabs fit on the left; this adds 8 custom categories
// (plus General + Reality Forge = 10 total when Reality Forge is unlocked),
// so the extra tabs appear on the right.

const StrangeMatter = (typeof global.StrangeMatter !== 'undefined')
  ? global.StrangeMatter
  : Java.loadClass('com.hexvane.strangematter.kubejs.StrangeMatterHelper');

// Order values: General=0, Reality Forge=1. Custom use 10+ so they appear after.
// Lower order = left side first; overflow goes to the right.

StrangeMatter.registerCategory(
  StrangeMatter.createCategory('test_alpha')
    .name('Test Alpha')
    .iconItem('minecraft:paper')
    .order(10)
);

StrangeMatter.registerCategory(
  StrangeMatter.createCategory('test_beta')
    .name('Test Beta')
    .iconItem('minecraft:book')
    .order(20)
);

StrangeMatter.registerCategory(
  StrangeMatter.createCategory('test_gamma')
    .name('Test Gamma')
    .iconItem('minecraft:ender_pearl')
    .order(30)
);

StrangeMatter.registerCategory(
  StrangeMatter.createCategory('test_delta')
    .name('Test Delta')
    .iconItem('minecraft:blaze_powder')
    .order(40)
);

StrangeMatter.registerCategory(
  StrangeMatter.createCategory('test_epsilon')
    .name('Test Epsilon')
    .iconItem('minecraft:glowstone_dust')
    .order(50)
);

StrangeMatter.registerCategory(
  StrangeMatter.createCategory('test_zeta')
    .name('Test Zeta')
    .iconItem('minecraft:redstone')
    .order(60)
);

StrangeMatter.registerCategory(
  StrangeMatter.createCategory('test_eta')
    .name('Test Eta')
    .iconItem('minecraft:lapis_lazuli')
    .order(70)
);

StrangeMatter.registerCategory(
  StrangeMatter.createCategory('test_theta')
    .name('Test Theta')
    .iconItem('minecraft:amethyst_shard')
    .order(80)
);

console.info('[Strange Matter] Registered 8 test categories for tab overflow. Open the Research Tablet to see tabs on the right.');
