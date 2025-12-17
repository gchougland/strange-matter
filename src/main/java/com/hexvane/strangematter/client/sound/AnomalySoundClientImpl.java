package com.hexvane.strangematter.client.sound;

import com.hexvane.strangematter.platform.AnomalySoundClient;
import net.minecraft.resources.ResourceLocation;

public class AnomalySoundClientImpl implements AnomalySoundClient {
    @Override
    public void initializeIfNeeded() {
        CustomSoundManager.getInstance().initialize();
    }

    @Override
    public void playAmbientSound(ResourceLocation soundLocation, double x, double y, double z, float volume, boolean loop) {
        CustomSoundManager.getInstance().playAmbientSound(soundLocation, x, y, z, volume, loop);
    }

    @Override
    public void stopAmbientSound(ResourceLocation soundLocation) {
        CustomSoundManager.getInstance().stopAmbientSound(soundLocation);
    }

    @Override
    public void updateSoundVolume(ResourceLocation soundLocation, float volume) {
        CustomSoundManager.getInstance().updateSoundVolume(soundLocation, volume);
    }

    @Override
    public void updateSoundPosition(ResourceLocation soundLocation, double x, double y, double z) {
        CustomSoundManager.getInstance().updateSoundPosition(soundLocation, x, y, z);
    }
}

