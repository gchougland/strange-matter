package com.hexvane.strangematter.command;

import com.hexvane.strangematter.research.ResearchData;
import com.hexvane.strangematter.research.ResearchType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class ResearchCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("research")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            .then(Commands.literal("check")
                .executes(ResearchCommand::checkOwnResearch)
                .then(Commands.argument("target", EntityArgument.players())
                    .executes(ResearchCommand::checkPlayerResearch)))
            .then(Commands.literal("add")
                .then(Commands.argument("type", com.mojang.brigadier.arguments.StringArgumentType.string())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ResearchCommand::addResearch)
                        .then(Commands.argument("target", EntityArgument.players())
                            .executes(ResearchCommand::addResearchToPlayer)))))
            .then(Commands.literal("reset")
                .executes(ResearchCommand::resetOwnResearch)
                .then(Commands.argument("target", EntityArgument.players())
                    .executes(ResearchCommand::resetPlayerResearch))));
    }
    
    private static int checkOwnResearch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        return checkResearch(context, player);
    }
    
    private static int checkPlayerResearch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");
        int result = 0;
        for (ServerPlayer player : players) {
            result = checkResearch(context, player);
        }
        return result;
    }
    
    private static int checkResearch(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        ResearchData researchData = ResearchData.get(player);
        
        context.getSource().sendSuccess(() -> Component.literal("§6Research Points for " + player.getName().getString() + ":"), false);
        
        for (ResearchType type : ResearchType.values()) {
            int points = researchData.getResearchPoints(type);
            context.getSource().sendSuccess(() -> Component.literal("§e" + type.getName() + ": §f" + points), false);
        }
        
        int scannedCount = researchData.getScannedObjects().size();
        context.getSource().sendSuccess(() -> Component.literal("§eScanned Objects: §f" + scannedCount), false);
        
        return 1;
    }
    
    private static int addResearch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        return addResearchToPlayers(context, java.util.List.of(player));
    }
    
    private static int addResearchToPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");
        return addResearchToPlayers(context, players);
    }
    
    private static int addResearchToPlayers(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) throws CommandSyntaxException {
        String typeName = context.getArgument("type", String.class);
        int amount = context.getArgument("amount", Integer.class);
        
        ResearchType type = ResearchType.fromName(typeName);
        if (type == null) {
            context.getSource().sendFailure(Component.literal("§cInvalid research type: " + typeName));
            return 0;
        }
        
        int result = 0;
        for (ServerPlayer player : players) {
            ResearchData researchData = ResearchData.get(player);
            researchData.addResearchPoints(type, amount);
            researchData.syncToClient(player);
            
            context.getSource().sendSuccess(() -> Component.literal("§aAdded " + amount + " " + type.getName() + " research points to " + player.getName().getString()), true);
            result = 1;
        }
        
        return result;
    }
    
    private static int resetOwnResearch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        return resetResearch(context, player);
    }
    
    private static int resetPlayerResearch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");
        int result = 0;
        for (ServerPlayer player : players) {
            result = resetResearch(context, player);
        }
        return result;
    }
    
    private static int resetResearch(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        ResearchData researchData = ResearchData.get(player);
        
        // Reset all research points to 0
        for (ResearchType type : ResearchType.values()) {
            researchData.addResearchPoints(type, -researchData.getResearchPoints(type));
        }
        
        // Clear scanned objects
        researchData.setScannedObjects(java.util.Set.of());
        
        researchData.syncToClient(player);
        
        context.getSource().sendSuccess(() -> Component.literal("§aReset all research data for " + player.getName().getString()), true);
        return 1;
    }
}
