package com.hexvane.strangematter.event;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class PlayerSpawnEventHandler {
    
    private static final ResourceLocation ROOT_ADVANCEMENT = ResourceLocation.fromNamespaceAndPath("strangematter", "root");
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // Only handle on server side
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        
        // Check if player is in survival mode
        if (serverPlayer.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) {
            return;
        }
        
        // Find the root advancement holder
        AdvancementHolder rootAdvancementHolder = serverPlayer.server.getAdvancements().get(ROOT_ADVANCEMENT);
        
        if (rootAdvancementHolder == null) {
            return; // Advancement not found, skip
        }
        
        // Check if player already has the root advancement
        AdvancementProgress advancementProgress = serverPlayer.getAdvancements().getOrStartProgress(rootAdvancementHolder);
        if (advancementProgress.isDone()) {
            return; // Player already has the advancement, don't give tablet again
        }
        
        // Give the player a research tablet
        ItemStack researchTablet = new ItemStack(StrangeMatterMod.RESEARCH_TABLET.get());
        
        // Try to add to inventory, or drop if full
        if (!serverPlayer.getInventory().add(researchTablet)) {
            serverPlayer.drop(researchTablet, false);
        }
        
        // Grant the root advancement when giving the tablet
        // The root advancement uses an "impossible" trigger, so we need to grant it manually
        if (!advancementProgress.isDone()) {
            for (String criterion : advancementProgress.getRemainingCriteria()) {
                advancementProgress.grantProgress(criterion);
            }
            serverPlayer.getAdvancements().award(rootAdvancementHolder, "");
        }
    }
}

