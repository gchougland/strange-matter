package com.hexvane.strangematter.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackPush;

public class CustomSoundManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomSoundManager.class);
    private static CustomSoundManager instance;
    private final Map<ResourceLocation, Integer> soundBuffers = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, AmbientSoundInstance> activeSounds = new ConcurrentHashMap<>();
    private boolean initialized = false;
    
    private CustomSoundManager() {}
    
    public static CustomSoundManager getInstance() {
        if (instance == null) {
            instance = new CustomSoundManager();
        }
        return instance;
    }
    
    public void initialize() {
        if (initialized) return;
        
        try {
            // Always use Minecraft's existing OpenAL context - don't create our own
            long currentContext = alcGetCurrentContext();
            if (currentContext == 0L) {
                LOGGER.warn("No OpenAL context available - Minecraft's audio system not initialized yet");
                return;
            }
            
            // Just mark as initialized - we'll use Minecraft's OpenAL context
            initialized = true;
            LOGGER.info("Custom Sound Manager initialized using Minecraft's OpenAL context");
            
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Custom Sound Manager", e);
        }
    }
    
    public void loadSound(ResourceLocation soundLocation) {
        if (!initialized) return;
        
        try {
            // Load the sound file from resources
            String path = "assets/" + soundLocation.getNamespace() + "/sounds/" + soundLocation.getPath() + ".ogg";
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
            
            if (inputStream == null) {
                LOGGER.warn("Could not find sound file: " + path);
                return;
            }
            
            // Convert to temporary file for STB Vorbis
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("sound", ".ogg");
            java.nio.file.Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            try (MemoryStack stack = stackPush()) {
                IntBuffer channels = stack.mallocInt(1);
                IntBuffer sampleRate = stack.mallocInt(1);
                
                ShortBuffer rawAudioBuffer = stb_vorbis_decode_filename(
                    tempFile.toString(), channels, sampleRate);
                
                if (rawAudioBuffer == null) {
                    LOGGER.error("Failed to decode sound file: " + path);
                    return;
                }
                
                // Create OpenAL buffer
                int buffer = alGenBuffers();
                alBufferData(buffer, AL_FORMAT_MONO16, rawAudioBuffer, sampleRate.get(0));
                
                soundBuffers.put(soundLocation, buffer);
                LOGGER.info("Loaded sound: " + soundLocation);
                
            } finally {
                java.nio.file.Files.deleteIfExists(tempFile);
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to load sound: " + soundLocation, e);
        }
    }
    
    public void playAmbientSound(ResourceLocation soundLocation, double x, double y, double z, float volume, boolean loop) {
        if (!initialized || !soundBuffers.containsKey(soundLocation)) {
            loadSound(soundLocation);
            if (!soundBuffers.containsKey(soundLocation)) {
                return;
            }
        }
        
        // Stop existing sound if playing
        stopAmbientSound(soundLocation);
        
        // Create new sound instance
        AmbientSoundInstance instance = new AmbientSoundInstance(soundLocation, x, y, z, volume, loop);
        activeSounds.put(soundLocation, instance);
        instance.start();
    }
    
    public void updateSoundVolume(ResourceLocation soundLocation, float newVolume) {
        AmbientSoundInstance instance = activeSounds.get(soundLocation);
        if (instance != null) {
            instance.setVolume(newVolume);
        }
    }
    
    public void updateSoundPosition(ResourceLocation soundLocation, double x, double y, double z) {
        AmbientSoundInstance instance = activeSounds.get(soundLocation);
        if (instance != null) {
            instance.setPosition(x, y, z);
        }
    }
    
    public void stopAmbientSound(ResourceLocation soundLocation) {
        AmbientSoundInstance instance = activeSounds.remove(soundLocation);
        if (instance != null) {
            instance.stop();
        }
    }
    
    public void shutdown() {
        LOGGER.info("Shutting down Custom Sound Manager");
        cleanup();
    }
    
    public void stopAllSounds() {
        for (AmbientSoundInstance instance : activeSounds.values()) {
            instance.stop();
        }
        activeSounds.clear();
    }
    
    public void cleanup() {
        stopAllSounds();
        
        // Delete buffers with error checking
        for (Map.Entry<ResourceLocation, Integer> entry : soundBuffers.entrySet()) {
            try {
                int buffer = entry.getValue();
                if (buffer != 0) {
                    alDeleteBuffers(buffer);
                }
            } catch (Exception e) {
                LOGGER.warn("Error deleting sound buffer for " + entry.getKey(), e);
            }
        }
        soundBuffers.clear();
        
        if (initialized) {
            // Don't destroy Minecraft's OpenAL context - just mark as uninitialized
            initialized = false;
            LOGGER.info("Custom Sound Manager cleaned up (preserving Minecraft's OpenAL context)");
        }
    }
    
    private static class AmbientSoundInstance {
        private final ResourceLocation soundLocation;
        private final int source;
        private final boolean loop;
        private double x, y, z;
        private float volume;
        private boolean playing = false;
        
        public AmbientSoundInstance(ResourceLocation soundLocation, double x, double y, double z, float volume, boolean loop) {
            this.soundLocation = soundLocation;
            this.x = x;
            this.y = y;
            this.z = z;
            this.volume = volume;
            this.loop = loop;
            
            // Create OpenAL source
            this.source = alGenSources();
            
            // Set source properties
            alSourcei(source, AL_BUFFER, CustomSoundManager.getInstance().soundBuffers.get(soundLocation));
            alSourcei(source, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
            alSource3f(source, AL_POSITION, (float) x, (float) y, (float) z);
            alSourcef(source, AL_GAIN, volume);
            alSourcef(source, AL_REFERENCE_DISTANCE, 1.0f);
            alSourcef(source, AL_MAX_DISTANCE, 10.0f);
            alSourcef(source, AL_ROLLOFF_FACTOR, 1.0f);
        }
        
        public void start() {
            if (!playing) {
                alSourcePlay(source);
                playing = true;
            }
        }
        
        public void stop() {
            if (playing) {
                try {
                    // Check if source is still valid before stopping
                    int state = alGetSourcei(source, AL_SOURCE_STATE);
                    if (state != AL_INVALID) {
                        alSourceStop(source);
                        alDeleteSources(source);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error stopping sound source", e);
                } finally {
                    playing = false;
                }
            }
        }
        
        public void setVolume(float newVolume) {
            this.volume = newVolume;
            if (playing) {
                try {
                    int state = alGetSourcei(source, AL_SOURCE_STATE);
                    if (state != AL_INVALID) {
                        alSourcef(source, AL_GAIN, volume);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error setting sound volume", e);
                }
            }
        }
        
        public void setPosition(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            if (playing) {
                try {
                    int state = alGetSourcei(source, AL_SOURCE_STATE);
                    if (state != AL_INVALID) {
                        alSource3f(source, AL_POSITION, (float) x, (float) y, (float) z);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error setting sound position", e);
                }
            }
        }
        
        public boolean isPlaying() {
            return playing && alGetSourcei(source, AL_SOURCE_STATE) == AL_PLAYING;
        }
    }
}
