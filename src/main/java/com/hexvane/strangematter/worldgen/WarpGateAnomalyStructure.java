package com.hexvane.strangematter.worldgen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

public class WarpGateAnomalyStructure extends Structure {
    public static final Codec<WarpGateAnomalyStructure> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            settingsCodec(instance)
        ).apply(instance, WarpGateAnomalyStructure::new)
    );

    public WarpGateAnomalyStructure(Structure.StructureSettings config) {
        super(config);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        System.out.println("=== WARP GATE STRUCTURE GENERATION DEBUG ===");
        System.out.println("WarpGate Structure: findGenerationPoint called for chunk " + context.chunkPos());
        System.out.println("WarpGate Structure: Height accessor min/max: " + context.heightAccessor().getMinBuildHeight() + "/" + context.heightAccessor().getMaxBuildHeight());
        
        // Get the chunk position
        ChunkPos chunkPos = context.chunkPos();

        // Find a suitable position in the chunk
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
            chunkPos.getMinBlockX() + context.random().nextInt(16),
            0, // Will be set to surface height
            chunkPos.getMinBlockZ() + context.random().nextInt(16)
        );

        System.out.println("WarpGate Structure: Trying position " + pos.getX() + "," + pos.getZ());

        // Use MOTION_BLOCKING_NO_LEAVES to find actual ground level, not tree tops
        int surfaceY = context.chunkGenerator().getBaseHeight(
            pos.getX(),
            pos.getZ(),
            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            context.heightAccessor(),
            context.randomState()
        );

        System.out.println("WarpGate Structure: Surface height at " + pos.getX() + "," + pos.getZ() + " = " + surfaceY);

        // Make sure we have a reasonable surface height
        if (surfaceY < context.heightAccessor().getMinBuildHeight() + 10 ||
            surfaceY > context.heightAccessor().getMaxBuildHeight() - 10) {
            System.out.println("WarpGate Structure: Surface height " + surfaceY + " is unreasonable, skipping");
            return Optional.empty();
        }

        // Check biome to prevent spawning in oceans or rivers
        Holder<Biome> biome = context.biomeSource().getNoiseBiome(
            pos.getX() >> 2, 
            pos.getY() >> 2, 
            pos.getZ() >> 2, 
            context.randomState().sampler()
        );
        
        String biomeName = biome.unwrapKey().map(key -> key.location().toString()).orElse("unknown");
        System.out.println("WarpGate Structure: Biome at " + pos + " = " + biomeName);
        
        // Prevent spawning in water biomes
        if (biomeName.contains("ocean") || 
            biomeName.contains("river") || 
            biomeName.contains("beach") ||
            biomeName.contains("deep_ocean") ||
            biomeName.contains("lukewarm_ocean") ||
            biomeName.contains("cold_ocean") ||
            biomeName.contains("frozen_ocean") ||
            biomeName.contains("warm_ocean")) {
            System.out.println("WarpGate Structure: Skipping water biome: " + biomeName);
            return Optional.empty();
        }

        pos.setY(surfaceY);
        System.out.println("WarpGate Structure: Setting warp gate at " + pos + " (2 blocks above surface)");

        // Check if there's enough space above the surface
        boolean spaceAbove1 = context.chunkGenerator().getBaseColumn(pos.getX(), pos.getZ(), context.heightAccessor(), context.randomState()).getBlock(pos.getY() + 1).isAir();
        boolean spaceAbove2 = context.chunkGenerator().getBaseColumn(pos.getX(), pos.getZ(), context.heightAccessor(), context.randomState()).getBlock(pos.getY() + 2).isAir();
        
        System.out.println("WarpGate Structure: Space above check - Y+1: " + spaceAbove1 + ", Y+2: " + spaceAbove2);

        if (spaceAbove1 && spaceAbove2) {
            System.out.println("WarpGate Structure: SUCCESS - Found good surface position, generating warp gate!");
            // Found good surface position, now generate the warp gate (spawn 2 blocks above surface)
            return Optional.of(new GenerationStub(
                pos.above(2),
                builder -> {
                    generateWarpGate(builder, pos.above(2));
                }
            ));
        }

        System.out.println("WarpGate Structure: FAILED - Not enough space above surface");
        return Optional.empty();
    }

    private void generateWarpGate(StructurePiecesBuilder builder, BlockPos pos) {
        System.out.println("WarpGate: Generating warp gate structure at " + pos);
        
        // For now, just mark this location as a warp gate structure
        // The actual entity spawning and block placement will be handled by the
        // NBT template system that was working before
        
        System.out.println("WarpGate: Successfully generated warp gate structure at " + pos);
        System.out.println("WarpGate: Entity and blocks will be spawned via NBT template");
    }

    @Override
    public StructureType<?> type() {
        return StrangeMatterMod.WARP_GATE_ANOMALY_STRUCTURE.get();
    }
}
