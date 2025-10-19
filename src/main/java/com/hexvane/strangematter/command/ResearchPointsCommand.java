package com.hexvane.strangematter.command;

import com.hexvane.strangematter.research.ResearchData;
import com.hexvane.strangematter.research.ResearchType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class ResearchPointsCommand {
    private static final DynamicCommandExceptionType INVALID_RESEARCH_TYPE = 
        new DynamicCommandExceptionType(type -> Component.literal("Invalid research type: " + type));
    
    private static final SuggestionProvider<CommandSourceStack> RESEARCH_TYPE_SUGGESTIONS = 
        (context, builder) -> SharedSuggestionProvider.suggest(
            new String[]{"gravity", "time", "space", "energy", "shadow", "cognition"}, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("researchpoints")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            .then(Commands.literal("check")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("researchType", StringArgumentType.string())
                        .suggests(RESEARCH_TYPE_SUGGESTIONS)
                        .then(Commands.argument("minPoints", IntegerArgumentType.integer(0))
                            .executes(ResearchPointsCommand::checkResearchPoints)
                            .then(Commands.argument("maxPoints", IntegerArgumentType.integer(0))
                                .executes(ResearchPointsCommand::checkResearchPointsRange)))))));
    }

    private static int checkResearchPoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        String researchTypeStr = StringArgumentType.getString(context, "researchType");
        ResearchType researchType = parseResearchType(researchTypeStr);
        
        if (researchType == null) {
            throw INVALID_RESEARCH_TYPE.create(researchTypeStr);
        }

        int minPoints = IntegerArgumentType.getInteger(context, "minPoints");
        int maxPoints = Integer.MAX_VALUE;
        try {
            maxPoints = IntegerArgumentType.getInteger(context, "maxPoints");
        } catch (Exception e) {
            // maxPoints not provided, use MAX_VALUE
        }

        int successCount = 0;
        for (ServerPlayer player : targets) {
            ResearchData researchData = ResearchData.get(player);
            int points = researchData.getResearchPoints(researchType);
            
            if (points >= minPoints && points <= maxPoints) {
                successCount++;
            }
        }

        // Return the number of players that meet the criteria
        // This allows command blocks to use the result for conditional execution
        return successCount;
    }

    private static int checkResearchPointsRange(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return checkResearchPoints(context);
    }

    private static ResearchType parseResearchType(String researchTypeStr) {
        return switch (researchTypeStr.toLowerCase()) {
            case "gravity" -> ResearchType.GRAVITY;
            case "time" -> ResearchType.TIME;
            case "space" -> ResearchType.SPACE;
            case "energy" -> ResearchType.ENERGY;
            case "shadow" -> ResearchType.SHADOW;
            case "cognition" -> ResearchType.COGNITION;
            default -> null;
        };
    }
}
