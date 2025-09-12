package com.hexvane.strangematter.command;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.GravityAnomalyEntity;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
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
        Component.literal("Invalid anomaly type. Available types: gravity, warp_gate")
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
                    new net.minecraft.resources.ResourceLocation("strangematter", "warp_gate_anomaly")
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
                    if (level.getBlockState(grassPos).is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || 
                        level.getBlockState(grassPos).is(net.minecraft.world.level.block.Blocks.DIRT)) {
                        level.setBlock(grassPos, StrangeMatterMod.ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 2);
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
                new net.minecraft.resources.ResourceLocation("strangematter", "warp_gate_anomaly")
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