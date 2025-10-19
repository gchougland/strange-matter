package com.hexvane.strangematter.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import com.hexvane.strangematter.registry.WarpGateRegistry;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.data.WarpGateLocationData;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Warp Gate Anomaly - A spatial anomaly that creates a teleportation link between two points.
 * When entities enter one warp gate, they are teleported to its paired gate.
 */
public class WarpGateAnomalyEntity extends BaseAnomalyEntity {
    
    // Entity data for syncing between client and server
    private static final EntityDataAccessor<String> PAIRED_GATE_ID = SynchedEntityData.defineId(WarpGateAnomalyEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(WarpGateAnomalyEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Config-driven getters for warp gate parameters
    private float getTeleportRadius() {
        return (float) com.hexvane.strangematter.Config.warpTeleportRadius;
    }
    
    @Override
    protected float getEffectRadius() {
        return getTeleportRadius();
    }
    
    
    private int getTeleportCooldownMax() {
        return com.hexvane.strangematter.Config.warpTeleportCooldown;
    }
    
    private static final float AURA_RADIUS = 4.0f;
    
    // Sound system - using StrangeMatterSounds for consistency
    
    // Teleportation tracking
    private int teleportCooldown = 0;
    private UUID pairedGateUUID;
    private BlockPos pairedGateStructureLocation;
    private java.util.Map<UUID, Integer> playerCooldowns = new java.util.HashMap<>();
    
    public WarpGateAnomalyEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.entityData.set(IS_ACTIVE, true);
        // Set a proper bounding box for the warp gate
        this.setBoundingBox(new AABB(-1.5, 0, -1.5, 1.5, 3, 1.5));
    }
    
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        // Register this warp gate in the location data when added to world
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            WarpGateLocationData data = WarpGateLocationData.get(serverLevel);
            data.addWarpGate(this.getUUID(), this.blockPosition());
        }
    }
    
    @Override
    public void remove(RemovalReason reason) {
        // Unregister this warp gate when removed
        WarpGateRegistry.unregisterWarpGate(this.getUUID());
        
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            WarpGateLocationData data = WarpGateLocationData.get(serverLevel);
            data.removeWarpGate(this.getUUID());
        }
        super.remove(reason);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PAIRED_GATE_ID, "");
        this.entityData.define(IS_ACTIVE, true);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.literal("Warp Gate");
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // Register this warp gate in the registry when it first ticks
        if (this.tickCount == 1) {
            WarpGateRegistry.registerWarpGate(this);
        }
        
        if (teleportCooldown > 0) {
            teleportCooldown--;
        }
    }
    
    @Override
    protected void applyAnomalyEffects() {
        if (!this.isActive() || this.isContained() || !com.hexvane.strangematter.Config.enableWarpEffects) {
            return; // Don't apply teleportation if not active, contained, or effects disabled
        }
        
        float teleportRadius = getTeleportRadius();
        AABB teleportBox = this.getBoundingBox().inflate(teleportRadius);
        List<Entity> entitiesInRange = this.level().getEntities(this, teleportBox);
        
        
        for (Entity entity : entitiesInRange) {
            // Calculate distance from anomaly center
            double distance = this.distanceTo(entity);
            
            
            // Allow creative players for testing, but add a message
            
            if (distance <= teleportRadius && teleportCooldown <= 0) {
                teleportEntity(entity);
            } else {
                // Check per-player cooldown
                if (entity instanceof Player player) {
                    UUID playerUUID = player.getUUID();
                    Integer playerCooldown = playerCooldowns.get(playerUUID);
                    if (playerCooldown != null && playerCooldown > 0) {
                        continue;
                    }
                }
            }
        }
    }
    
    private void teleportEntity(Entity entity) {
        // Ensure we're on the server side
        if (this.level().isClientSide) {
            return;
        }
        
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        
        
        
        
        // If we have a paired structure location, teleport directly there
        if (pairedGateStructureLocation != null) {
            
            // Use the stored warp gate position but offset it to prevent immediate re-detection
            // Offset by 4 blocks to ensure we're outside the 2-block teleportation radius
            Vec3 teleportPos = new Vec3(pairedGateStructureLocation.getX() + 4.0, pairedGateStructureLocation.getY() + 2, pairedGateStructureLocation.getZ() + 0.5);
            
            performDirectTeleportation(entity, serverLevel, teleportPos);
            return;
        }
        
        // No paired gate or couldn't find structure location, try to find and pair with unpaired gate
        WarpGateAnomalyEntity pairedGate = findAndPairWithUnpairedGate(serverLevel);
        if (pairedGate == null) {
            // No suitable gate found for pairing
            return;
        }
        
        // Calculate teleport position (offset from the paired gate to prevent back-and-forth)
        Vec3 teleportPos = pairedGate.position().add(3, 1, 0); // 3 blocks away from the destination portal
        
        
        // Teleport the entity using server-side methods
        if (entity instanceof Player player) {
            // Use server-side teleportation for players
            player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
        } else if (entity instanceof LivingEntity livingEntity) {
            // Use server-side teleportation for living entities
            livingEntity.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
        } else {
            // For other entities, just set position
            entity.setPos(teleportPos.x, teleportPos.y, teleportPos.z);
        }
        
        // Set cooldown to prevent rapid teleportation (per-entity, not per-gate)
        if (entity instanceof Player) {
            int cooldownMax = getTeleportCooldownMax();
            teleportCooldown = cooldownMax;
            pairedGate.teleportCooldown = cooldownMax;
        }
        
        // Play teleportation sound
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), 
            SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.AMBIENT, 1.0f, 1.5f);
        this.level().playSound(null, teleportPos.x, teleportPos.y, teleportPos.z, 
            SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.AMBIENT, 1.0f, 1.5f);
        
        // Spawn teleportation particles
        spawnTeleportParticles(this.position());
        spawnTeleportParticles(teleportPos);
        
    }
    
    private WarpGateAnomalyEntity findAndPairWithUnpairedGate(ServerLevel serverLevel) {
        Vec3 currentPos = this.position();
        
        // Prioritize finding distant structures first to create distributed connections
        // This prevents all warp gates from connecting to the same hub areas
        
        // If no unpaired gates in registry, search for distant structures and load them
        try {
            // Create a TagKey for our warp gate structure
            var warpGateTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.STRUCTURE,
                new net.minecraft.resources.ResourceLocation("strangematter", "warp_gate_anomaly")
            );
            
            
            // Search in multiple random directions far away to find suitable locations for warp gates
            Random random = new Random();
            BlockPos currentPosBlock = this.blockPosition();
            
            for (int attempt = 0; attempt < 5; attempt++) {
                // Pick a random direction and distance (minimum 1000 blocks away)
                double angle = random.nextDouble() * 2 * Math.PI;
                int distance = 1000 + random.nextInt(4000); // 1000-5000 blocks away
                
                int searchX = currentPosBlock.getX() + (int)(Math.cos(angle) * distance);
                int searchZ = currentPosBlock.getZ() + (int)(Math.sin(angle) * distance);
                BlockPos candidatePos = new BlockPos(searchX, 64, searchZ);
                
                
                // Check if this location is far enough away
                double actualDistance = Math.sqrt(currentPosBlock.distSqr(candidatePos));
                if (actualDistance < 500) {
                    continue;
                }
                
                // Force load the chunk to check the area
                int chunkX = candidatePos.getX() >> 4;
                int chunkZ = candidatePos.getZ() >> 4;
                
                
                boolean chunkLoaded = ForgeChunkManager.forceChunk(
                    serverLevel,
                    "strangematter",
                    candidatePos,
                    chunkX,
                    chunkZ,
                    true, // add the chunk
                    true  // ticking
                );
                
                
                // Wait a moment for the chunk to load
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Check if this location already has a warp gate
                int surfaceY = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, candidatePos.getX(), candidatePos.getZ());
                BlockPos entitySpawnPos = new BlockPos(candidatePos.getX(), surfaceY + 2, candidatePos.getZ());
                
                
                AABB searchArea = new AABB(entitySpawnPos).inflate(20, 10, 20);
                List<WarpGateAnomalyEntity> nearbyGates = serverLevel.getEntitiesOfClass(WarpGateAnomalyEntity.class, searchArea);
                
                
                if (nearbyGates.isEmpty()) {
                    
                    // Place anomalous grass blocks in a patch around the warp gate
                    for (int x = -5; x <= 5; x++) {
                        for (int z = -5; z <= 5; z++) {
                            if (x*x + z*z <= 25) { // Circular pattern
                                BlockPos grassPos = entitySpawnPos.offset(x, -1, z);
                                var currentBlock = serverLevel.getBlockState(grassPos);
                                if (currentBlock.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || 
                                    currentBlock.is(net.minecraft.world.level.block.Blocks.DIRT) ||
                                    currentBlock.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT) ||
                                    currentBlock.is(net.minecraft.world.level.block.Blocks.PODZOL) ||
                                    currentBlock.is(net.minecraft.world.level.block.Blocks.STONE)) {
                                    serverLevel.setBlock(grassPos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 3);
                                }
                            }
                        }
                    }
                    
                    // Spawn the warp gate entity at the center
                    WarpGateAnomalyEntity newWarpGate = StrangeMatterMod.WARP_GATE_ANOMALY_ENTITY.get().create(serverLevel);
                    if (newWarpGate != null) {
                        newWarpGate.setPos(entitySpawnPos.getX() + 0.5, entitySpawnPos.getY() + 1, entitySpawnPos.getZ() + 0.5);
                        newWarpGate.setActive(true); // Ensure it's active
                        serverLevel.addFreshEntity(newWarpGate);
                        
                        return pairWithGate(newWarpGate);
                    } else {
                    }
                } else {
                }
            }
            
            
            // Fallback: check registry for any unpaired gates (including nearby ones)
            WarpGateRegistry.WarpGateEntry unpairedEntry = WarpGateRegistry.findUnpairedWarpGate(
                this.level(), 
                currentPos, 
                100.0 // Lower minimum distance for fallback
            );
            
            if (unpairedEntry != null) {
                
                // Find the actual entity from the UUID
                BlockPos searchPos = new BlockPos((int)unpairedEntry.position.x, (int)unpairedEntry.position.y, (int)unpairedEntry.position.z);
                List<WarpGateAnomalyEntity> allGates = serverLevel.getEntitiesOfClass(WarpGateAnomalyEntity.class, 
                    new AABB(searchPos).inflate(50, 50, 50));
                
                WarpGateAnomalyEntity foundGate = null;
                for (WarpGateAnomalyEntity gate : allGates) {
                    if (gate.getUUID().equals(unpairedEntry.uuid)) {
                        foundGate = gate;
                        break;
                    }
                }
                
                if (foundGate != null) {
                    return pairWithGate(foundGate);
                } else {
                }
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("WarpGate: Error during structure search: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private WarpGateAnomalyEntity pairWithGate(WarpGateAnomalyEntity foundGate) {
        
        // Pair the gates using their actual positions
        BlockPos thisPos = this.blockPosition();
        BlockPos otherPos = foundGate.blockPosition();
        
        // Store each other's positions for teleportation
        this.setPairedGateStructureLocation(otherPos);
        foundGate.setPairedGateStructureLocation(thisPos);
        
        // Mark both gates as paired in the registry
        WarpGateRegistry.markWarpGateAsPaired(this.getUUID());
        WarpGateRegistry.markWarpGateAsPaired(foundGate.getUUID());
        
        
        
        return foundGate;
    }
    
    
    private WarpGateAnomalyEntity findExistingWarpGateAtStructureLocation(ServerLevel serverLevel, BlockPos structurePos) {

        // Calculate chunk coordinates
        int chunkX = structurePos.getX() >> 4;
        int chunkZ = structurePos.getZ() >> 4;


        // Force load the chunk using ForgeChunkManager
        boolean success = ForgeChunkManager.forceChunk(
            serverLevel,
            "strangematter",
            structurePos,
            chunkX,
            chunkZ,
            true, // add the chunk
            true  // ticking
        );


        // Search in a reasonable radius around the structure location (expand Y range significantly)
        AABB searchBox = new AABB(structurePos).inflate(50, 400, 50);


        List<WarpGateAnomalyEntity> entities = serverLevel.getEntitiesOfClass(WarpGateAnomalyEntity.class, searchBox);

        if (!entities.isEmpty()) {

            // Unload the chunk after we're done
            ForgeChunkManager.forceChunk(
                serverLevel,
                "strangematter",
                structurePos,
                chunkX,
                chunkZ,
                false, // remove the chunk
                false  // not ticking
            );

            return entities.get(0); // Return the first one found
        }

        
        // Unload the chunk after we're done
        ForgeChunkManager.forceChunk(
            serverLevel,
            "strangematter",
            structurePos,
            chunkX,
            chunkZ,
            false, // remove the chunk
            false  // not ticking
        );

        return null; // Don't spawn new gates, just return null
    }
    
    private WarpGateAnomalyEntity findOrSpawnWarpGateAtStructureLocation(ServerLevel serverLevel, BlockPos structurePos) {

        // Calculate chunk coordinates
        int chunkX = structurePos.getX() >> 4;
        int chunkZ = structurePos.getZ() >> 4;


        // Force load the chunk using ForgeChunkManager
        boolean success = ForgeChunkManager.forceChunk(
            serverLevel,
            "strangematter",
            structurePos,
            chunkX,
            chunkZ,
            true, // add the chunk
            true  // ticking
        );


        // Search in a reasonable radius around the structure location (expand Y range significantly)
        AABB searchBox = new AABB(structurePos).inflate(50, 400, 50);


        List<WarpGateAnomalyEntity> entities = serverLevel.getEntitiesOfClass(WarpGateAnomalyEntity.class, searchBox);

        if (!entities.isEmpty()) {

            // Unload the chunk after we're done
            ForgeChunkManager.forceChunk(
                serverLevel,
                "strangematter",
                structurePos,
                chunkX,
                chunkZ,
                false, // remove the chunk
                false  // not ticking
            );

            return entities.get(0); // Return the first one found
        }

        // No existing warp gate found, spawn a new one
        
        // Find the actual surface height at this location
        int surfaceY = serverLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, structurePos.getX(), structurePos.getZ());
        BlockPos surfacePos = new BlockPos(structurePos.getX(), surfaceY, structurePos.getZ());
        
        
        WarpGateAnomalyEntity newWarpGate = new WarpGateAnomalyEntity(
            StrangeMatterMod.WARP_GATE_ANOMALY_ENTITY.get(),
            serverLevel
        );
        newWarpGate.setPos(surfacePos.getX() + 0.5, surfacePos.getY() + 2, surfacePos.getZ() + 0.5);
        newWarpGate.setActive(true); // Make sure it's active so it can detect entities
        
        serverLevel.addFreshEntity(newWarpGate);
        
        // Spawn anomalous grass and resonite ore at the surface position
        spawnAnomalousGrassAndOre(serverLevel, surfacePos);
        
        // Unload the chunk after we're done
        ForgeChunkManager.forceChunk(
            serverLevel,
            "strangematter",
            structurePos,
            chunkX,
            chunkZ,
            false, // remove the chunk
            false  // not ticking
        );

        return newWarpGate;
    }
    
    public void spawnAnomalousGrassAndOre(ServerLevel serverLevel, BlockPos centerPos) {
        // Only modify terrain if terrain modification is enabled
        if (this.terrainModificationEnabled) {
            // Use the base class method for consistent terrain generation
            // This will spawn both resonite ore and spatial shard ore
            this.modifyTerrain();
        }
    }
    
    private void spawnTeleportParticles(Vec3 pos) {
        if (this.level().isClientSide) return;
        
        // Spawn ender particles at teleport location
        for (int i = 0; i < 20; i++) {
            double x = pos.x + (this.level().random.nextDouble() - 0.5) * 2.0;
            double y = pos.y + this.level().random.nextDouble() * 2.0;
            double z = pos.z + (this.level().random.nextDouble() - 0.5) * 2.0;
            
            this.level().addParticle(
                ParticleTypes.PORTAL,
                x, y, z,
                (this.level().random.nextDouble() - 0.5) * 0.1,
                this.level().random.nextDouble() * 0.1,
                (this.level().random.nextDouble() - 0.5) * 0.1
            );
        }
    }
    
    private void performDirectTeleportation(Entity entity, ServerLevel serverLevel, Vec3 teleportPos) {
        
        
        // Teleport the entity using server-side methods
        if (entity instanceof Player player) {
            // Use server-side teleportation for players
            player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
        } else if (entity instanceof LivingEntity livingEntity) {
            // Use server-side teleportation for living entities
            livingEntity.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
        } else {
            // For other entities, just set position
            entity.setPos(teleportPos.x, teleportPos.y, teleportPos.z);
        }
        
        // Play teleportation sound
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), 
            SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.AMBIENT, 1.0f, 1.5f);
        this.level().playSound(null, teleportPos.x, teleportPos.y, teleportPos.z, 
            SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.AMBIENT, 1.0f, 1.5f);
        
        // Spawn teleportation particles
        spawnTeleportParticles(this.position());
        spawnTeleportParticles(teleportPos);
        
    }
    
    private BlockPos findStructureLocationForPairedGate(ServerLevel serverLevel, UUID pairedUUID) {
        // This method should find the specific structure location where the paired gate is located
        // For now, we'll try to find the actual entity and get its position
        
        // Try to find the actual entity first
        Entity pairedEntity = serverLevel.getEntity(pairedUUID);
        if (pairedEntity instanceof WarpGateAnomalyEntity pairedGate) {
            return pairedGate.blockPosition();
        }
        
        return null;
    }
    
    private WarpGateAnomalyEntity findPairedGate(ServerLevel level) {
        if (pairedGateUUID == null) {
            return null;
        }
        
        
        Entity entity = level.getEntity(pairedGateUUID);
        if (entity instanceof WarpGateAnomalyEntity warpGate) {
            return warpGate;
        } else if (entity != null) {
        } else {
        }
        
        return null;
    }
    
    @Override
    protected void spawnParticles() {
        if (this.level().isClientSide) return;
        
        // Spawn swirling portal particles
        if (tickCount % 2 == 0) {
            double radius = AURA_RADIUS;
            double angle = (tickCount * 0.1) % (2 * Math.PI);
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            double y = this.getY() + 0.5;
            
            this.level().addParticle(
                ParticleTypes.PORTAL,
                x, y, z,
                -Math.sin(angle) * 0.1,
                0.05,
                Math.cos(angle) * 0.1
            );
        }
        
        // Spawn center vortex particles
        if (tickCount % 5 == 0) {
            for (int i = 0; i < 3; i++) {
                double angle = (tickCount * 0.05 + i * Math.PI * 2 / 3) % (2 * Math.PI);
                double radius = 1.0 + Math.sin(tickCount * 0.1) * 0.5;
                double x = this.getX() + Math.cos(angle) * radius;
                double z = this.getZ() + Math.sin(angle) * radius;
                double y = this.getY() + 0.5;
                
                this.level().addParticle(
                    ParticleTypes.END_ROD,
                    x, y, z,
                    0, 0.1, 0
                );
            }
        }
        
        // Spawn space tear effect particles
        if (tickCount % 10 == 0) {
            double x = this.getX() + (this.level().random.nextDouble() - 0.5) * 2.0;
            double y = this.getY() + this.level().random.nextDouble() * 2.0;
            double z = this.getZ() + (this.level().random.nextDouble() - 0.5) * 2.0;
            
            this.level().addParticle(
                ParticleTypes.REVERSE_PORTAL,
                x, y, z,
                (this.level().random.nextDouble() - 0.5) * 0.2,
                this.level().random.nextDouble() * 0.2,
                (this.level().random.nextDouble() - 0.5) * 0.2
            );
        }
    }
    
    @Override
    protected void updateClientEffects() {
        // Client-side visual effects
        // This will be handled by the renderer
    }
    
    @Override
    public ResourceLocation getAnomalySound() {
        return com.hexvane.strangematter.sound.StrangeMatterSounds.WARP_GATE_LOOP.get().getLocation();
    }
    
    @Override
    protected ResearchType getResearchType() {
        return ResearchType.SPACE;
    }
    
    @Override
    protected String getAnomalyName() {
        return "WarpGate";
    }
    
    @Override
    protected RegistryObject<Block> getShardOreBlock() {
        return StrangeMatterMod.SPATIAL_SHARD_ORE_BLOCK;
    }
    
    // Getters and setters
    public String getPairedGateId() {
        return this.entityData.get(PAIRED_GATE_ID);
    }
    
    public void setPairedGateId(String gateId) {
        this.entityData.set(PAIRED_GATE_ID, gateId);
    }
    
    public void setPairedGateStructureLocation(BlockPos location) {
        this.pairedGateStructureLocation = location;
    }
    
    public boolean isActive() {
        return this.entityData.get(IS_ACTIVE);
    }
    
    public void setActive(boolean active) {
        this.entityData.set(IS_ACTIVE, active);
    }
    
    public UUID getPairedGateUUID() {
        return pairedGateUUID;
    }
    
    public void setPairedGateUUID(UUID uuid) {
        this.pairedGateUUID = uuid;
    }
    
    public float getAuraRadius() {
        return AURA_RADIUS;
    }
    
    // Method to get the ambient sound
    public SoundEvent getAmbientSound() {
        return SoundEvents.PORTAL_AMBIENT;
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("PairedGateId")) {
            this.entityData.set(PAIRED_GATE_ID, compound.getString("PairedGateId"));
        }
        if (compound.contains("IsActive")) {
            this.entityData.set(IS_ACTIVE, compound.getBoolean("IsActive"));
        }
        if (compound.contains("PairedGateUUID")) {
            this.pairedGateUUID = compound.getUUID("PairedGateUUID");
        }
        if (compound.contains("TeleportCooldown")) {
            this.teleportCooldown = compound.getInt("TeleportCooldown");
        }
        if (compound.contains("PairedGateStructureLocation")) {
            CompoundTag posTag = compound.getCompound("PairedGateStructureLocation");
            this.pairedGateStructureLocation = new BlockPos(
                posTag.getInt("x"),
                posTag.getInt("y"),
                posTag.getInt("z")
            );
        }
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("PairedGateId", this.entityData.get(PAIRED_GATE_ID));
        compound.putBoolean("IsActive", this.entityData.get(IS_ACTIVE));
        if (pairedGateUUID != null) {
            compound.putUUID("PairedGateUUID", pairedGateUUID);
        }
        compound.putInt("TeleportCooldown", teleportCooldown);
        if (pairedGateStructureLocation != null) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pairedGateStructureLocation.getX());
            posTag.putInt("y", pairedGateStructureLocation.getY());
            posTag.putInt("z", pairedGateStructureLocation.getZ());
            compound.put("PairedGateStructureLocation", posTag);
        }
    }
}
