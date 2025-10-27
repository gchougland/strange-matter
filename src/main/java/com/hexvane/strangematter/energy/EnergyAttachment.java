package com.hexvane.strangematter.energy;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Energy attachment system for NeoForge 1.21.1
 * Replaces the old capability system with NeoForge's new data attachment system
 */
public class EnergyAttachment {
    
    // Create a deferred register for our energy attachment
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = 
        DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.ATTACHMENT_TYPES, "strangematter");
    
    // Register the energy storage attachment
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ResonanceEnergyStorage>> ENERGY_STORAGE = 
        ATTACHMENTS.register("energy_storage", () -> AttachmentType.builder(() -> new ResonanceEnergyStorage(10000)).serialize(new ResonanceEnergyStorage.Serializer()).build());
    
    /**
     * Get energy storage from a block entity
     */
    public static ResonanceEnergyStorage getEnergyStorage(BlockEntity blockEntity) {
        return blockEntity.getData(ENERGY_STORAGE);
    }
    
    /**
     * Check if a block entity has energy storage
     */
    public static boolean hasEnergyStorage(BlockEntity blockEntity) {
        return blockEntity.hasData(ENERGY_STORAGE);
    }
    
    /**
     * Create energy storage attachment for a block entity
     */
    public static void attachEnergyStorage(BlockEntity blockEntity, int capacity) {
        blockEntity.setData(ENERGY_STORAGE, new ResonanceEnergyStorage(capacity));
    }
    
    /**
     * Create energy storage attachment for a block entity with custom transfer rates
     */
    public static void attachEnergyStorage(BlockEntity blockEntity, int capacity, int maxReceive, int maxExtract) {
        blockEntity.setData(ENERGY_STORAGE, new ResonanceEnergyStorage(capacity, maxReceive, maxExtract));
    }
}
