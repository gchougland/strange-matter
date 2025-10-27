package com.hexvane.strangematter.kubejs;

import com.mojang.logging.LogUtils;
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
 * 
 * NOTE: This plugin uses the services-based registration system
 * instead of the traditional KubeJSPlugin approach due to KubeJS 7.0 changes.
 * The integration is handled through the StrangeMatterHelper class which is
 * registered via the services file.
 */
public class StrangeMatterKubeJSPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Initialize the KubeJS integration.
     * This method is called when KubeJS loads the plugin.
     */
    public static void initialize() {
        LOGGER.info("[Strange Matter] KubeJS integration initialized!");
        LOGGER.info("[Strange Matter] StrangeMatter binding available in scripts!");
    }
    
    /**
     * Register bindings for KubeJS scripts.
     * This method is called by KubeJS when the plugin is loaded.
     * 
     * @param event The bindings event
     */
    public static void registerBindings(Object event) {
        LOGGER.info("[Strange Matter] Registering KubeJS bindings...");
        // The StrangeMatterHelper class is automatically available in scripts
        // through the services file registration
        LOGGER.info("[Strange Matter] StrangeMatter binding registered!");
    }
}

