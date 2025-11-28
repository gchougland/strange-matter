package com.hexvane.strangematter.event;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StrangeMatterMod.MODID)
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
        
        // Find the root advancement by iterating through all advancements
        Advancement rootAdvancement = null;
        for (Advancement advancement : serverPlayer.server.getAdvancements().getAllAdvancements()) {
            if (advancement.getId().equals(ROOT_ADVANCEMENT)) {
                rootAdvancement = advancement;
                break;
            }
        }
        
        if (rootAdvancement == null) {
            return; // Advancement not found, skip
        }
        
        // Check if player already has the root advancement
        AdvancementProgress advancementProgress = serverPlayer.getAdvancements().getOrStartProgress(rootAdvancement);
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
        }
    }
}

