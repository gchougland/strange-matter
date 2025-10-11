package com.hexvane.strangematter.kubejs;

import com.mojang.logging.LogUtils;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import org.slf4j.Logger;

/**
 * KubeJS Plugin for Strange Matter mod integration.
 * Provides support for:
 * - Reality Forge recipes
 * - Custom research nodes
 * - Custom research info pages
 * 
 * RECIPE INTEGRATION:
 * Reality Forge recipes are available through the standard recipe system.
 * 
 * Example:
 * <pre>
 * ServerEvents.recipes(event => {
 *   event.custom({
 *     type: 'strangematter:reality_forge',
 *     pattern: ['DDD', 'DED', 'DDD'],
 *     key: {
 *       'D': { item: 'minecraft:diamond' },
 *       'E': { item: 'strangematter:resonite_ingot' }
 *     },
 *     result: { item: 'strangematter:some_item' },
 *     shards: {
 *       'gravitic': 10,
 *       'energetic': 5
 *     },
 *     required_research: 'reality_forge'
 *   })
 * })
 * </pre>
 * 
 * RESEARCH INTEGRATION:
 * Add custom research nodes and info pages.
 * 
 * Example:
 * <pre>
 * // In startup_scripts (runs once at game start)
 * StrangeMatterEvents.research(event => {
 *   // Create a custom research node
 *   event.addNode('my_research')
 *     .category('custom')
 *     .position(100, 200)
 *     .cost('gravity', 10)
 *     .cost('energy', 5)
 *     .iconItem('minecraft:diamond')
 *     .prerequisite('reality_forge')
 *   
 *   // Add info pages to any research node
 *   event.addPages('my_research', pages => {
 *     pages.page()
 *       .title('My Custom Research')
 *       .content('This is my custom research!')
 *       .recipe('my_custom_item')
 *   })
 * })
 * </pre>
 */
public class StrangeMatterKubeJSPlugin extends KubeJSPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @Override
    public void registerBindings(BindingsEvent event) {
        // Register the StrangeMatterHelper as a global binding for all script types
        // This allows scripts to use: StrangeMatter.createResearchNode(), etc.
        LOGGER.info("[Strange Matter] Registering KubeJS bindings...");
        event.add("StrangeMatter", StrangeMatterHelper.class);
        LOGGER.info("[Strange Matter] StrangeMatter binding registered!");
    }
}

