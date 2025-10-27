package com.hexvane.strangematter.command;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.GravityAnomalyEntity;
import com.hexvane.strangematter.entity.EnergeticRiftEntity;
import com.hexvane.strangematter.entity.EchoingShadowEntity;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
import com.hexvane.strangematter.entity.TemporalBloomEntity;
import com.hexvane.strangematter.entity.ThoughtwellEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class AnomalyCommand {
    
    private static final SimpleCommandExceptionType INVALID_ANOMALY_TYPE = new SimpleCommandExceptionType(
        Component.literal("Invalid anomaly type. Available types: gravity, warp_gate, energetic_rift, echoing_shadow, temporal_bloom, thoughtwell")
    );
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("anomaly")
            .requires(source -> source.hasPermission(2)) // Requires op level 2
            .then(Commands.literal("spawn")
                .then(Commands.argument("type", StringArgumentType.word())
                    .executes(AnomalyCommand::spawnAnomalyAtPlayer)
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(AnomalyCommand::spawnAnomalyAtPosition)
                    )
                )
            )
            .then(Commands.literal("locate")
                .then(Commands.argument("type", StringArgumentType.word())
                    .executes(AnomalyCommand::locateAnomaly)
                )
            )
            .then(Commands.literal("list")
                .executes(AnomalyCommand::listAnomalyTypes)
            )
            .then(Commands.literal("debug_chunk")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(AnomalyCommand::debugChunkLoad)
                )
            )
        );
    }
    
    private static int spawnAnomalyAtPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String anomalyType = StringArgumentType.getString(context, "type");
        
        ServerLevel level = source.getLevel();
        BlockPos playerPos = source.getPlayerOrException().blockPosition();
        
        // Find a good surface position for spawning
        // Find proper surface position
        BlockPos spawnPos = new BlockPos(playerPos.getX(), 
            level.getHeight(Heightmap.Types.WORLD_SURFACE, playerPos.getX(), playerPos.getZ()) + 2, 
            playerPos.getZ());
        return spawnAnomaly(source, anomalyType, spawnPos);
    }
    
    private static int spawnAnomalyAtPosition(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String anomalyType = StringArgumentType.getString(context, "type");
        BlockPos spawnPos = BlockPosArgument.getBlockPos(context, "pos");
        
        return spawnAnomaly(source, anomalyType, spawnPos);
    }
    
    private static int spawnAnomaly(CommandSourceStack source, String anomalyType, BlockPos spawnPos) throws CommandSyntaxException {
        ServerLevel level = source.getLevel();
        
        switch (anomalyType.toLowerCase()) {
            case "gravity":
                GravityAnomalyEntity gravityAnomaly = StrangeMatterMod.GRAVITY_ANOMALY.get().create(level);
                if (gravityAnomaly != null) {
                    gravityAnomaly.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
                    level.addFreshEntity(gravityAnomaly);
                    
                    source.sendSuccess(() -> Component.literal("Spawned gravity anomaly at " + 
                        spawnPos.getX() + ", " + spawnPos.getY() + ", " + spawnPos.getZ()), true);
                    return 1;
                } else {
                    throw new SimpleCommandExceptionType(Component.literal("Failed to create gravity anomaly entity")).create();
                }
            case "warp_gate":
                WarpGateAnomalyEntity warpGate = StrangeMatterMod.WARP_GATE_ANOMALY_ENTITY.get().create(level);
                if (warpGate != null) {
                    warpGate.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
                    
                    // Make the warp gate active so it can teleport immediately
                    warpGate.setActive(true);
                    
                    level.addFreshEntity(warpGate);
                    
                    // Spawn anomalous grass and resonite ore
                    spawnAnomalousGrassAndOre(level, spawnPos);
                    
                    source.sendSuccess(() -> Component.literal("Spawned active warp gate anomaly at " + 
                        spawnPos.getX() + ", " + spawnPos.getY() + ", " + spawnPos.getZ() + " (ready for teleportation)"), true);
                    return 1;
                } else {
                    throw new SimpleCommandExceptionType(Component.literal("Failed to create warp gate entity")).create();
                }
            case "energetic_rift":
                EnergeticRiftEntity energeticRift = StrangeMatterMod.ENERGETIC_RIFT.get().create(level);
                if (energeticRift != null) {
                    energeticRift.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
                    level.addFreshEntity(energeticRift);
                    
                    // Spawn anomalous grass and resonite ore
                    spawnAnomalousGrassAndOre(level, spawnPos);
                    
                    source.sendSuccess(() -> Component.literal("Spawned energetic rift at " + 
                        spawnPos.getX() + ", " + spawnPos.getY() + ", " + spawnPos.getZ()), true);
                    return 1;
                } else {
                    throw new SimpleCommandExceptionType(Component.literal("Failed to create energetic rift entity")).create();
                }
            case "echoing_shadow":
                EchoingShadowEntity echoingShadow = StrangeMatterMod.ECHOING_SHADOW.get().create(level);
                if (echoingShadow != null) {
                    echoingShadow.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
                    level.addFreshEntity(echoingShadow);
                    
                    // Spawn anomalous grass and resonite ore
                    spawnAnomalousGrassAndOre(level, spawnPos);
                    
                    source.sendSuccess(() -> Component.literal("Spawned echoing shadow at " + 
                        spawnPos.getX() + ", " + spawnPos.getY() + ", " + spawnPos.getZ()), true);
                    return 1;
                } else {
                    throw new SimpleCommandExceptionType(Component.literal("Failed to create echoing shadow entity")).create();
                }
            case "temporal_bloom":
                TemporalBloomEntity temporalBloom = StrangeMatterMod.TEMPORAL_BLOOM.get().create(level);
                if (temporalBloom != null) {
                    temporalBloom.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
                    level.addFreshEntity(temporalBloom);
                    
                    // Spawn anomalous grass and resonite ore
                    spawnAnomalousGrassAndOre(level, spawnPos);
                    
                    source.sendSuccess(() -> Component.literal("Spawned temporal bloom at " + 
                        spawnPos.getX() + ", " + spawnPos.getY() + ", " + spawnPos.getZ()), true);
                    return 1;
                } else {
                    throw new SimpleCommandExceptionType(Component.literal("Failed to create temporal bloom entity")).create();
                }
            case "thoughtwell":
                ThoughtwellEntity thoughtwell = StrangeMatterMod.THOUGHTWELL.get().create(level);
                if (thoughtwell != null) {
                    thoughtwell.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
                    level.addFreshEntity(thoughtwell);
                    
                    // Spawn anomalous grass and resonite ore
                    spawnAnomalousGrassAndOre(level, spawnPos);
                    
                    source.sendSuccess(() -> Component.literal("Spawned thoughtwell at " + 
                        spawnPos.getX() + ", " + spawnPos.getY() + ", " + spawnPos.getZ()), true);
                    return 1;
                } else {
                    throw new SimpleCommandExceptionType(Component.literal("Failed to create thoughtwell entity")).create();
                }
            default:
                throw INVALID_ANOMALY_TYPE.create();
        }
    }
    
    private static int locateAnomaly(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String anomalyType = StringArgumentType.getString(context, "type");
        ServerLevel serverLevel = source.getLevel();
        BlockPos playerPos = source.getPlayerOrException().blockPosition();
        
        switch (anomalyType.toLowerCase()) {
            case "gravity":
                // For gravity anomalies, search for entities
                var gravityEntities = serverLevel.getEntitiesOfClass(GravityAnomalyEntity.class, 
                    new net.minecraft.world.phys.AABB(playerPos).inflate(10000));
                
                if (gravityEntities.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("No gravity anomalies found within 10000 blocks"), false);
                    return 0;
                }
                
                // Find the nearest one
                GravityAnomalyEntity nearest = null;
                double nearestDistance = Double.MAX_VALUE;
                for (GravityAnomalyEntity entity : gravityEntities) {
                    double distance = playerPos.distSqr(entity.blockPosition());
                    if (distance < nearestDistance) {
                        nearest = entity;
                        nearestDistance = distance;
                    }
                }
                
                if (nearest != null) {
                    BlockPos nearestPos = nearest.blockPosition();
                    double distance = Math.sqrt(nearestDistance);
                    source.sendSuccess(() -> Component.literal("Nearest gravity anomaly at " + 
                        nearestPos.getX() + ", " + nearestPos.getY() + ", " + nearestPos.getZ() + 
                        " (distance: " + String.format("%.1f", distance) + " blocks)"), false);
                    return 1;
                }
                break;
                
        case "warp_gate":
            // Use the EXACT same method as warp gate pairing logic
            try {
                System.out.println("=== LOCATE COMMAND DEBUG ===");
                System.out.println("Locate Command: Searching for warp gate structures from player position: " + playerPos);
                System.out.println("Locate Command: Server level: " + serverLevel);
                
                // Create a TagKey for our warp gate structure using the correct 1.20.1 Forge API
                var warpGateTag = net.minecraft.tags.TagKey.create(
                    net.minecraft.core.registries.Registries.STRUCTURE,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("strangematter", "warp_gate_anomaly")
                );

                System.out.println("Locate Command: Created TagKey: " + warpGateTag);
                System.out.println("Locate Command: Searching within 10000 blocks...");

                // Use Minecraft's findNearestMapStructure method with the TagKey (EXACT same as pairing)
                var structurePos = serverLevel.findNearestMapStructure(warpGateTag, playerPos, 10000, false);

                System.out.println("Locate Command: findNearestMapStructure result: " + structurePos);

                if (structurePos == null) {
                    System.out.println("Locate Command: No structures found");
                    source.sendSuccess(() -> Component.literal("No warp gate structures found within 10000 blocks"), false);
                    return 0;
                }

                double distance = Math.sqrt(playerPos.distSqr(structurePos));
                System.out.println("Locate Command: Found structure at " + structurePos + " (distance: " + distance + ")");
                source.sendSuccess(() -> Component.literal("Nearest warp gate structure at " +
                    structurePos.getX() + ", " + structurePos.getY() + ", " + structurePos.getZ() +
                    " (distance: " + String.format("%.1f", distance) + " blocks)"), false);
                return 1;

            } catch (Exception e) {
                System.out.println("Locate Command: Exception occurred: " + e.getMessage());
                e.printStackTrace();
                source.sendSuccess(() -> Component.literal("Error locating warp gate structure: " + e.getMessage()), false);
                return 0;
            }
                
            case "energetic_rift":
                // For energetic rift anomalies, search for entities
                var energeticRiftEntities = serverLevel.getEntitiesOfClass(EnergeticRiftEntity.class, 
                    new net.minecraft.world.phys.AABB(playerPos).inflate(10000));
                
                if (energeticRiftEntities.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("No energetic rift anomalies found within 10000 blocks"), false);
                    return 0;
                }
                
                // Find the nearest one
                EnergeticRiftEntity nearestRift = null;
                double nearestRiftDistance = Double.MAX_VALUE;
                for (EnergeticRiftEntity entity : energeticRiftEntities) {
                    double distance = playerPos.distSqr(entity.blockPosition());
                    if (distance < nearestRiftDistance) {
                        nearestRift = entity;
                        nearestRiftDistance = distance;
                    }
                }
                
                if (nearestRift != null) {
                    BlockPos nearestPos = nearestRift.blockPosition();
                    double distance = Math.sqrt(nearestRiftDistance);
                    source.sendSuccess(() -> Component.literal("Nearest energetic rift at " + 
                        nearestPos.getX() + ", " + nearestPos.getY() + ", " + nearestPos.getZ() + 
                        " (distance: " + String.format("%.1f", distance) + " blocks)"), false);
                    return 1;
                }
                break;
                
            case "echoing_shadow":
                // For echoing shadow anomalies, search for entities
                var echoingShadowEntities = serverLevel.getEntitiesOfClass(EchoingShadowEntity.class, 
                    new net.minecraft.world.phys.AABB(playerPos).inflate(10000));
                
                if (echoingShadowEntities.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("No echoing shadow anomalies found within 10000 blocks"), false);
                    return 0;
                }
                
                // Find the nearest one
                EchoingShadowEntity nearestShadow = null;
                double nearestShadowDistance = Double.MAX_VALUE;
                for (EchoingShadowEntity entity : echoingShadowEntities) {
                    double distance = playerPos.distSqr(entity.blockPosition());
                    if (distance < nearestShadowDistance) {
                        nearestShadow = entity;
                        nearestShadowDistance = distance;
                    }
                }
                
                if (nearestShadow != null) {
                    BlockPos nearestPos = nearestShadow.blockPosition();
                    double distance = Math.sqrt(nearestShadowDistance);
                    source.sendSuccess(() -> Component.literal("Nearest echoing shadow at " + 
                        nearestPos.getX() + ", " + nearestPos.getY() + ", " + nearestPos.getZ() + 
                        " (distance: " + String.format("%.1f", distance) + " blocks)"), false);
                    return 1;
                }
                break;
                
            case "temporal_bloom":
                // For temporal bloom anomalies, search for entities
                var temporalBloomEntities = serverLevel.getEntitiesOfClass(TemporalBloomEntity.class, 
                    new net.minecraft.world.phys.AABB(playerPos).inflate(10000));
                
                if (temporalBloomEntities.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("No temporal bloom anomalies found within 10000 blocks"), false);
                    return 0;
                }
                
                // Find the nearest one
                TemporalBloomEntity nearestBloom = null;
                double nearestBloomDistance = Double.MAX_VALUE;
                for (TemporalBloomEntity entity : temporalBloomEntities) {
                    double distance = playerPos.distSqr(entity.blockPosition());
                    if (distance < nearestBloomDistance) {
                        nearestBloom = entity;
                        nearestBloomDistance = distance;
                    }
                }
                
                if (nearestBloom != null) {
                    BlockPos nearestPos = nearestBloom.blockPosition();
                    double distance = Math.sqrt(nearestBloomDistance);
                    source.sendSuccess(() -> Component.literal("Nearest temporal bloom at " + 
                        nearestPos.getX() + ", " + nearestPos.getY() + ", " + nearestPos.getZ() + 
                        " (distance: " + String.format("%.1f", distance) + " blocks)"), false);
                    return 1;
                }
                break;
                
            case "thoughtwell":
                // For thoughtwell anomalies, search for entities
                var thoughtwellEntities = serverLevel.getEntitiesOfClass(ThoughtwellEntity.class, 
                    new net.minecraft.world.phys.AABB(playerPos).inflate(10000));
                
                if (thoughtwellEntities.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("No thoughtwell anomalies found within 10000 blocks"), false);
                    return 0;
                }
                
                // Find the nearest one
                ThoughtwellEntity nearestThoughtwell = null;
                double nearestThoughtwellDistance = Double.MAX_VALUE;
                for (ThoughtwellEntity entity : thoughtwellEntities) {
                    double distance = playerPos.distSqr(entity.blockPosition());
                    if (distance < nearestThoughtwellDistance) {
                        nearestThoughtwell = entity;
                        nearestThoughtwellDistance = distance;
                    }
                }
                
                if (nearestThoughtwell != null) {
                    BlockPos nearestPos = nearestThoughtwell.blockPosition();
                    double distance = Math.sqrt(nearestThoughtwellDistance);
                    source.sendSuccess(() -> Component.literal("Nearest thoughtwell at " + 
                        nearestPos.getX() + ", " + nearestPos.getY() + ", " + nearestPos.getZ() + 
                        " (distance: " + String.format("%.1f", distance) + " blocks)"), false);
                    return 1;
                }
                break;
                
            default:
                throw INVALID_ANOMALY_TYPE.create();
        }
        
        source.sendSuccess(() -> Component.literal("No " + anomalyType + " anomalies found"), false);
        return 0;
    }
    
    private static int listAnomalyTypes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSuccess(() -> Component.literal("Available anomaly types:"), false);
        source.sendSuccess(() -> Component.literal("- gravity: Creates a floating icosahedron with levitation field"), false);
        source.sendSuccess(() -> Component.literal("- warp_gate: Creates a spatial anomaly for teleportation"), false);
        source.sendSuccess(() -> Component.literal("- energetic_rift: Creates an electric anomaly that zaps entities and strikes lightning rods"), false);
        source.sendSuccess(() -> Component.literal("- echoing_shadow: Creates a shadow anomaly that absorbs light and boosts mob spawning"), false);
        source.sendSuccess(() -> Component.literal("- temporal_bloom: Creates a temporal anomaly that affects crop growth and transforms mobs"), false);
        source.sendSuccess(() -> Component.literal("- thoughtwell: Creates a cognitive anomaly that affects player perception and confuses mobs"), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("Commands:"), false);
        source.sendSuccess(() -> Component.literal("- /anomaly spawn <type> [pos]: Spawn single anomaly"), false);
        source.sendSuccess(() -> Component.literal("- /anomaly locate <type>: Locate nearest anomaly of specified type"), false);
        
        return 1;
    }
    
    private static void spawnAnomalousGrassAndOre(ServerLevel level, BlockPos centerPos) {
        RandomSource random = level.getRandom();
        
        // Generate anomalous grass in a patch underneath
        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos grassPos = centerPos.offset(x, -1, z);
                
                // Check if we should place grass here (not always 100% chance)
                if (random.nextFloat() < 0.8f) {
                    // Use WorldGenUtils to find the proper position for anomalous grass
                    BlockPos properGrassPos = com.hexvane.strangematter.worldgen.WorldGenUtils.findAnomalousGrassPosition(level, 
                        grassPos.getX(), grassPos.getZ());
                    if (properGrassPos != null) {
                        level.setBlock(properGrassPos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 2);
                    }
                }
            }
        }
        
        // Generate resonite ore in a small area underneath the anomalous grass
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -3; y <= -1; y++) {
                    BlockPos orePos = centerPos.offset(x, y, z);
                    
                    // Small chance to place ore
                    if (random.nextFloat() < 0.15f) {
                        if (level.getBlockState(orePos).is(net.minecraft.world.level.block.Blocks.STONE)) {
                            level.setBlock(orePos, StrangeMatterMod.RESONITE_ORE_BLOCK.get().defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }
    
    private static int debugChunkLoad(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        
        source.sendSuccess(() -> Component.literal("Debugging chunk load at " + pos), false);
        
        // Manually trigger the chunk load event logic
        try {
            // Create a TagKey for our warp gate structure (now that we have a structure tag file)
            var warpGateTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.STRUCTURE,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("strangematter", "warp_gate_anomaly")
            );
            
            source.sendSuccess(() -> Component.literal("Searching for warp gate structure with TagKey: " + warpGateTag), false);
            
            // Check if there's a structure at this position
            var structurePos = level.findNearestMapStructure(warpGateTag, pos, 100, false);
            source.sendSuccess(() -> Component.literal("Structure search result: " + structurePos), false);
            
            if (structurePos != null) {
                // Check if there's already a warp gate entity at this location
                var existingEntities = level.getEntitiesOfClass(WarpGateAnomalyEntity.class, 
                    new net.minecraft.world.phys.AABB(structurePos).inflate(5));
                
                source.sendSuccess(() -> Component.literal("Existing entities found: " + existingEntities.size()), false);
                
                if (existingEntities.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("No existing warp gate entity found, spawning new one"), false);
                    
                    // Find the actual surface height at this location
                    int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, structurePos.getX(), structurePos.getZ());
                    BlockPos surfacePos = new BlockPos(structurePos.getX(), surfaceY, structurePos.getZ());
                    
                    source.sendSuccess(() -> Component.literal("Structure pos: " + structurePos + ", Surface pos: " + surfacePos), false);
                    
                    // Spawn the warp gate entity
                    WarpGateAnomalyEntity warpGate = new WarpGateAnomalyEntity(
                        StrangeMatterMod.WARP_GATE_ANOMALY_ENTITY.get(),
                        level
                    );
                    warpGate.setPos(surfacePos.getX() + 0.5, surfacePos.getY() + 2, surfacePos.getZ() + 0.5);
                    warpGate.setActive(true);
                    
                    level.addFreshEntity(warpGate);
                    
                    source.sendSuccess(() -> Component.literal("Successfully spawned warp gate entity at " + surfacePos), false);
                } else {
                    source.sendSuccess(() -> Component.literal("Warp gate entity already exists at " + structurePos), false);
                }
            } else {
                source.sendSuccess(() -> Component.literal("No warp gate structure found near position"), false);
            }
            
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error: " + e.getMessage()));
            e.printStackTrace();
        }
        
        return 1;
    }
}