package com.hexvane.strangematter.event;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Cleans up stray anomaly marker block-entity NBT that can get persisted with an AIR block state, causing
 * vanilla to log "Tried to load a DUMMY block entity ... but found minecraft:air".
 *
 * We strip these entries on both SAVE (prevents new region files from accumulating spam) and LOAD (fixes
 * already-written chunks before they are fully deserialized).
 */
@Mod.EventBusSubscriber(modid = StrangeMatterMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkMarkerCleanupEventHandler {
    private static final String MARKER_BE_ID = StrangeMatterMod.MODID + ":anomaly_spawner_marker";

    @SubscribeEvent
    public static void onChunkDataLoad(ChunkDataEvent.Load event) {
        stripMarkerBlockEntities(event.getData());
    }

    @SubscribeEvent
    public static void onChunkDataSave(ChunkDataEvent.Save event) {
        stripMarkerBlockEntities(event.getData());
    }

    private static void stripMarkerBlockEntities(CompoundTag chunkNbt) {
        if (chunkNbt == null) return;

        // Vanilla 1.20+ uses "block_entities". Older mappings sometimes expose "blockEntities".
        stripFromList(chunkNbt, "block_entities");
        stripFromList(chunkNbt, "blockEntities");
    }

    private static void stripFromList(CompoundTag chunkNbt, String key) {
        if (!chunkNbt.contains(key, Tag.TAG_LIST)) return;

        ListTag list = chunkNbt.getList(key, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return;

        ListTag cleaned = new ListTag();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag beTag = list.getCompound(i);
            String id = beTag.getString("id");
            if (!MARKER_BE_ID.equals(id)) {
                cleaned.add(beTag);
            }
        }

        if (cleaned.size() != list.size()) {
            chunkNbt.put(key, cleaned);
        }
    }
}



