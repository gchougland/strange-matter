package com.hexvane.strangematter.block;

import com.hexvane.strangematter.StrangeMatterMod;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Gravitic Shard Ore Block - drops gravitic shards when mined.
 * Spawns beneath gravity anomalies.
 */
public class GraviticShardOreBlock extends ShardOreBlock {
    
    public GraviticShardOreBlock() {
        super(StrangeMatterMod.GRAVITIC_SHARD);
    }
}
