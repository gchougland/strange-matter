package com.hexvane.strangematter.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * Custom particle for energy absorption effect in the Resonance Condenser.
 * This particle slowly moves toward a target position, creating the energy absorption visual.
 */
public class EnergyAbsorptionParticle extends TextureSheetParticle {
    
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final double pullStrength;
    
    protected EnergyAbsorptionParticle(ClientLevel level, double x, double y, double z, 
                                     double targetX, double targetY, double targetZ, 
                                     double pullStrength) {
        super(level, x, y, z);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.pullStrength = pullStrength;
        
        // Set particle properties
        this.lifetime = 60 + level.random.nextInt(40); // 3-5 second lifetime
        this.scale(0.3f + level.random.nextFloat() * 0.4f); // Variable size
        this.alpha = 0.8f;
        
        // Set color to bright cyan/blue energy color
        this.rCol = 0.0f; // No red
        this.gCol = 0.8f + level.random.nextFloat() * 0.2f; // Bright green/cyan
        this.bCol = 1.0f; // Full blue
        
        // Initial random velocity
        this.xd = (level.random.nextDouble() - 0.5) * 0.1;
        this.yd = (level.random.nextDouble() - 0.5) * 0.1;
        this.zd = (level.random.nextDouble() - 0.5) * 0.1;
    }
    
    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        
        // Calculate direction to target
        double dx = this.targetX - this.x;
        double dy = this.targetY - this.y;
        double dz = this.targetZ - this.z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        if (distance > 0.1) { // Don't pull if very close
            // Normalize direction and apply pull strength
            double pullX = (dx / distance) * this.pullStrength;
            double pullY = (dy / distance) * this.pullStrength;
            double pullZ = (dz / distance) * this.pullStrength;
            
            // Apply pull force (stronger as particle gets closer to target)
            double distanceFactor = Math.min(1.0, distance / 5.0); // Stronger pull when closer
            this.xd += pullX * distanceFactor * 0.1;
            this.yd += pullY * distanceFactor * 0.1;
            this.zd += pullZ * distanceFactor * 0.1;
            
            // Apply damping to prevent overshooting
            this.xd *= 0.95;
            this.yd *= 0.95;
            this.zd *= 0.95;
        }
        
        // Move particle
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;
        
        // Fade out as particle gets closer to target
        double fadeDistance = Math.min(distance, 2.0);
        this.alpha = (float) (0.8f * (fadeDistance / 2.0));
        
        // Add some floating motion
        this.yd += Math.sin(this.age * 0.1) * 0.001;
    }
    
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
    
    
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;
        
        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }
        
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, 
                                     double x, double y, double z, 
                                     double xSpeed, double ySpeed, double zSpeed) {
            // Extract target position and pull strength from speed parameters
            // We'll use xSpeed, ySpeed, zSpeed to pass target position
            // and use a fixed pull strength for now
            System.out.println("EnergyAbsorptionParticle.Provider: Creating particle at (" + x + ", " + y + ", " + z + ") targeting (" + xSpeed + ", " + ySpeed + ", " + zSpeed + ")");
            EnergyAbsorptionParticle particle = new EnergyAbsorptionParticle(
                level, x, y, z, xSpeed, ySpeed, zSpeed, 0.05
            );
            // Use sprite set if available, otherwise skip
            if (this.spriteSet != null) {
                particle.pickSprite(this.spriteSet);
            }
            return particle;
        }
    }
}