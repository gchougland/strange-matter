package com.hexvane.strangematter.platform;

import net.minecraft.resources.ResourceLocation;

/**
 * Client-only service interface exposed to common code without referencing client classes.
 * On dedicated servers this will be null/unset.
 */
public interface AnomalySoundClient {
    void initializeIfNeeded();

    void playAmbientSound(ResourceLocation soundLocation, double x, double y, double z, float volume, boolean loop);

    void stopAmbientSound(ResourceLocation soundLocation);

    void updateSoundVolume(ResourceLocation soundLocation, float volume);

    void updateSoundPosition(ResourceLocation soundLocation, double x, double y, double z);
}

