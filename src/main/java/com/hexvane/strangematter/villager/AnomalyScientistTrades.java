package com.hexvane.strangematter.villager;

import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StrangeMatterMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnomalyScientistTrades {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == StrangeMatterMod.ANOMALY_SCIENTIST.get()) {
            
            // Novice (Level 1) trades
            event.getTrades().get(1).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 8),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.RAW_RESONITE.get(), 4),
                12, 2, 0.05f
            ));
            
            event.getTrades().get(1).add((trader, random) -> new MerchantOffer(
                new ItemStack(StrangeMatterMod.RAW_RESONITE.get(), 6),
                ItemStack.EMPTY,
                new ItemStack(Items.EMERALD, 1),
                16, 2, 0.05f
            ));

            // Apprentice (Level 2) trades
            event.getTrades().get(2).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 12),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.RESONITE_INGOT.get(), 3),
                12, 5, 0.05f
            ));
            
            event.getTrades().get(2).add((trader, random) -> new MerchantOffer(
                new ItemStack(StrangeMatterMod.RESONITE_INGOT.get(), 2),
                ItemStack.EMPTY,
                new ItemStack(Items.EMERALD, 1),
                16, 5, 0.05f
            ));
            
            event.getTrades().get(2).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 5),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.RESONITE_NUGGET.get(), 9),
                16, 5, 0.05f
            ));

            // Journeyman (Level 3) trades
            event.getTrades().get(3).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 20),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.RESONANT_COIL.get(), 1),
                8, 10, 0.05f
            ));
            
            event.getTrades().get(3).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 16),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.GRAVITIC_SHARD.get(), 1),
                4, 10, 0.05f
            ));
            
            event.getTrades().get(3).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 16),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.SPATIAL_SHARD.get(), 1),
                4, 10, 0.05f
            ));

            // Expert (Level 4) trades
            event.getTrades().get(4).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 24),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.STABILIZED_CORE.get(), 1),
                6, 15, 0.05f
            ));
            
            event.getTrades().get(4).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 18),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.CHRONO_SHARD.get(), 1),
                4, 15, 0.05f
            ));
            
            event.getTrades().get(4).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 18),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.ENERGETIC_SHARD.get(), 1),
                4, 15, 0.05f
            ));

            // Master (Level 5) trades
            event.getTrades().get(5).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 30),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.RESONANT_CIRCUIT.get(), 1),
                4, 30, 0.05f
            ));
            
            event.getTrades().get(5).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 20),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.SHADE_SHARD.get(), 1),
                3, 30, 0.05f
            ));
            
            event.getTrades().get(5).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 20),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.INSIGHT_SHARD.get(), 1),
                3, 30, 0.05f
            ));
            
            event.getTrades().get(5).add((trader, random) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 48),
                ItemStack.EMPTY,
                new ItemStack(StrangeMatterMod.RESONITE_BLOCK_ITEM.get(), 1),
                2, 30, 0.05f
            ));
        }
    }
}

