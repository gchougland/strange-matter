package com.hexvane.strangematter.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;

/**
 * KubeJS Plugin for Strange Matter mod integration.
 * This allows modpack creators to add/modify Reality Forge recipes using KubeJS scripts.
 * 
 * The Reality Forge recipe type is automatically available in KubeJS through the standard
 * recipe serializer system. No custom schema registration is needed.
 * 
 * Example usage in KubeJS:
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
 * You can also modify or remove existing Reality Forge recipes:
 * <pre>
 * ServerEvents.recipes(event => {
 *   // Remove a specific recipe
 *   event.remove({ type: 'strangematter:reality_forge', output: 'strangematter:some_item' })
 *   
 *   // Remove all Reality Forge recipes
 *   event.remove({ type: 'strangematter:reality_forge' })
 * })
 * </pre>
 */
public class StrangeMatterKubeJSPlugin extends KubeJSPlugin {
    // Plugin presence allows KubeJS to recognize Strange Matter mod
    // Custom recipe types work automatically through the serializer system
}

