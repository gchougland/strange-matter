package com.hexvane.strangematter.event;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles entity interaction events for the Echoform Imprinter to override mob right-click actions
 */
@Mod.EventBusSubscriber(modid = "strangematter")
public class EchoformImprinterEventHandler {
    
    /**
     * Intercept entity interactions when holding the Echoform Imprinter
     * Set to HIGHEST priority to override villager trading and other default interactions
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(event.getHand());
        
        // Check if holding the Echoform Imprinter
        if (heldItem.getItem() == StrangeMatterMod.ECHOFORM_IMPRINTER.get()) {
            // Cancel the default interaction so we can scan instead
            event.setCanceled(true);
            
            // The scanning will be handled by the item's use() method
            if (event.getHand() == InteractionHand.MAIN_HAND) {
                player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }
}

