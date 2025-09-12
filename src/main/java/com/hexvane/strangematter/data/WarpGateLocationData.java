package com.hexvane.strangematter.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

public class WarpGateLocationData extends SavedData {
    private final Map<UUID, BlockPos> warpGateLocations = new HashMap<>();
    
    public static WarpGateLocationData create() {
        return new WarpGateLocationData();
    }
    
    public static WarpGateLocationData load(CompoundTag tag) {
        WarpGateLocationData data = new WarpGateLocationData();
        
        if (tag.contains("warpGates", Tag.TAG_LIST)) {
            ListTag warpGatesList = tag.getList("warpGates", Tag.TAG_COMPOUND);
            for (int i = 0; i < warpGatesList.size(); i++) {
                CompoundTag gateTag = warpGatesList.getCompound(i);
                UUID gateId = gateTag.getUUID("id");
                BlockPos pos = NbtUtils.readBlockPos(gateTag.getCompound("pos"));
                data.warpGateLocations.put(gateId, pos);
            }
        }
        
        return data;
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag warpGatesList = new ListTag();
        
        for (Map.Entry<UUID, BlockPos> entry : warpGateLocations.entrySet()) {
            CompoundTag gateTag = new CompoundTag();
            gateTag.putUUID("id", entry.getKey());
            gateTag.put("pos", NbtUtils.writeBlockPos(entry.getValue()));
            warpGatesList.add(gateTag);
        }
        
        tag.put("warpGates", warpGatesList);
        return tag;
    }
    
    public void addWarpGate(UUID gateId, BlockPos pos) {
        warpGateLocations.put(gateId, pos);
        setDirty();
        System.out.println("WarpGate: Registered warp gate " + gateId + " at " + pos);
    }
    
    public void removeWarpGate(UUID gateId) {
        warpGateLocations.remove(gateId);
        setDirty();
        System.out.println("WarpGate: Unregistered warp gate " + gateId);
    }
    
    public BlockPos getWarpGateLocation(UUID gateId) {
        return warpGateLocations.get(gateId);
    }
    
    public List<BlockPos> getAllWarpGateLocations() {
        return new ArrayList<>(warpGateLocations.values());
    }
    
    public List<BlockPos> getUnpairedWarpGateLocations(UUID excludeId) {
        List<BlockPos> locations = new ArrayList<>();
        for (Map.Entry<UUID, BlockPos> entry : warpGateLocations.entrySet()) {
            if (!entry.getKey().equals(excludeId)) {
                locations.add(entry.getValue());
            }
        }
        return locations;
    }
    
    public static WarpGateLocationData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(WarpGateLocationData::load, WarpGateLocationData::create, "warp_gate_locations");
    }
}
