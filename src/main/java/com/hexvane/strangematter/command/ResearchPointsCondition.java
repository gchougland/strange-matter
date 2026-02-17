package com.hexvane.strangematter.command;

import com.hexvane.strangematter.research.ResearchData;
import com.hexvane.strangematter.research.ResearchTypeHelper;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ResearchPointsCondition {
    private static final DynamicCommandExceptionType INVALID_RESEARCH_TYPE = 
        new DynamicCommandExceptionType(type -> Component.literal("Invalid research type: " + type));

    public static class ResearchTypeArgument implements ArgumentType<String> {
        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            return reader.readUnquotedString();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return SharedSuggestionProvider.suggest(ResearchTypeHelper.getAllTypeIds(), builder);
        }
    }

    public static boolean checkResearchPoints(CommandSourceStack source, Collection<ServerPlayer> targets, 
                                            String researchTypeStr, int minPoints, int maxPoints) throws CommandSyntaxException {
        if (!ResearchTypeHelper.isKnownType(researchTypeStr)) {
            throw INVALID_RESEARCH_TYPE.create(researchTypeStr);
        }

        for (ServerPlayer player : targets) {
            ResearchData researchData = ResearchData.get(player);
            int points = researchData.getResearchPoints(researchTypeStr);
            
            if (points >= minPoints && points <= maxPoints) {
                return true;
            }
        }
        
        return false;
    }

    public static boolean checkResearchPoints(CommandSourceStack source, Collection<ServerPlayer> targets, 
                                            String researchTypeStr, int minPoints) throws CommandSyntaxException {
        return checkResearchPoints(source, targets, researchTypeStr, minPoints, Integer.MAX_VALUE);
    }
}
