package com.hexvane.strangematter.command;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.GravityAnomalyEntity;
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
import net.minecraft.world.entity.EntityType;

public class AnomalyCommand {
    
    private static final SimpleCommandExceptionType INVALID_ANOMALY_TYPE = new SimpleCommandExceptionType(
        Component.literal("Invalid anomaly type. Available types: gravity")
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
            .then(Commands.literal("list")
                .executes(AnomalyCommand::listAnomalyTypes)
            )
        );
    }
    
    private static int spawnAnomalyAtPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String anomalyType = StringArgumentType.getString(context, "type");
        
        BlockPos spawnPos = source.getPlayerOrException().blockPosition().above();
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
            default:
                throw INVALID_ANOMALY_TYPE.create();
        }
    }
    
    private static int listAnomalyTypes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSuccess(() -> Component.literal("Available anomaly types:"), false);
        source.sendSuccess(() -> Component.literal("- gravity: Creates a floating icosahedron with levitation field"), false);
        
        return 1;
    }
}
