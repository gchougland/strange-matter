package com.hexvane.strangematter.client.screen.components;

import net.minecraft.client.gui.GuiGraphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BubblingParticleSystem {
    
    private static class Particle {
        float x;
        float y;
        float size;
        int color;
        float life;
        float maxLife;
        float speed;
        
        Particle(float x, float y, float size, int color, float maxLife, float speed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.life = 0.0f;
            this.maxLife = maxLife;
            this.speed = speed;
        }
        
        boolean isAlive() {
            return life < maxLife;
        }
        
        void update() {
            life += 1.0f;
            y -= speed; // Particles rise up
        }
        
        void render(GuiGraphics guiGraphics) {
            float alpha = 1.0f - (life / maxLife); // Fade out over time
            int alphaInt = (int) (alpha * 255) << 24;
            int finalColor = (color & 0x00FFFFFF) | alphaInt;
            
            // Render particle as a small square
            int particleSize = Math.max(1, (int) size);
            guiGraphics.fill((int) x, (int) y, (int) x + particleSize, (int) y + particleSize, finalColor);
        }
    }
    
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private final int tubeX;
    private final int tubeY;
    private final int tubeWidth;
    private final int tubeHeight;
    private final int particleColor;
    private final float spawnRate; // Particles per tick
    private final float minSize;
    private final float maxSize;
    private final float minSpeed;
    private final float maxSpeed;
    private final float maxLife;
    
    private float spawnAccumulator = 0.0f;
    
    public BubblingParticleSystem(int tubeX, int tubeY, int tubeWidth, int tubeHeight, 
                                 int particleColor, float spawnRate, float minSize, float maxSize, 
                                 float minSpeed, float maxSpeed, float maxLife) {
        this.tubeX = tubeX;
        this.tubeY = tubeY;
        this.tubeWidth = tubeWidth;
        this.tubeHeight = tubeHeight;
        this.particleColor = particleColor;
        this.spawnRate = spawnRate;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.maxLife = maxLife;
    }
    
    public void update() {
        // Spawn new particles
        spawnAccumulator += spawnRate;
        while (spawnAccumulator >= 1.0f) {
            spawnParticle();
            spawnAccumulator -= 1.0f;
        }
        
        // Update existing particles
        particles.removeIf(particle -> {
            particle.update();
            return !particle.isAlive();
        });
    }
    
    public void render(GuiGraphics guiGraphics) {
        for (Particle particle : particles) {
            particle.render(guiGraphics);
        }
    }
    
    private void spawnParticle() {
        // Spawn particle at random position within tube bounds
        float x = tubeX + random.nextFloat() * tubeWidth;
        float y = tubeY + random.nextFloat() * (tubeHeight - 5);
        float size = minSize + random.nextFloat() * (maxSize - minSize);
        float speed = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
        
        particles.add(new Particle(x, y, size, particleColor, maxLife, speed));
    }
    
    // Factory methods for common configurations
    public static BubblingParticleSystem createInstabilityTubeEffect(int tubeX, int tubeY, int tubeWidth, int tubeHeight) {
        return new BubblingParticleSystem(
            tubeX, tubeY, tubeWidth, tubeHeight,
            0xFF9641BA, // Purple color
            0.3f, // Spawn rate (particles per tick)
            1.0f, 3.0f, // Size range
            0.5f, 1.5f, // Speed range
            20.0f // Max life (1 second at 20 TPS)
        );
    }
    
    public static BubblingParticleSystem createGravityTubeEffect(int tubeX, int tubeY, int tubeWidth, int tubeHeight) {
        return new BubblingParticleSystem(
            tubeX, tubeY, tubeWidth, tubeHeight,
            0xFF3dc7c7, // Cyan color
            0.3f, // Spawn rate (particles per tick)
            1.0f, 3.0f, // Size range
            0.5f, 1.5f, // Speed range
            20.0f // Max life (1 second at 20 TPS)
        );
    }
}
