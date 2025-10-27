package com.hexvane.strangematter.item;

import com.hexvane.strangematter.research.ResearchData;
import com.hexvane.strangematter.research.ResearchDataServerHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResearchTabletItem extends Item {
    public ResearchTabletItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            openResearchTablet(player);
        } else {
            // Server side: sync research data to client before opening GUI
            if (player instanceof ServerPlayer serverPlayer) {
                ResearchData researchData = ResearchData.get(serverPlayer);
                ResearchDataServerHandler.syncResearchDataToClient(serverPlayer, researchData);
            }
        }
        
        return InteractionResultHolder.success(itemStack);
    }

    @OnlyIn(Dist.CLIENT)
    private void openResearchTablet(Player player) {
        net.minecraft.client.Minecraft.getInstance().setScreen(new com.hexvane.strangematter.client.screen.ResearchTabletScreen());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.strangematter.research_tablet.tooltip"));
    }
}
