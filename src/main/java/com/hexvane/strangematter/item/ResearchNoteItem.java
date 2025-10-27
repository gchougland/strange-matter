package com.hexvane.strangematter.item;

import com.hexvane.strangematter.research.ResearchType;
import com.hexvane.strangematter.StrangeMatterMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ResearchNoteItem extends Item {
    
    public ResearchNoteItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        
        // Show which research this note is for
        if (!customData.isEmpty() && customData.contains("research_id")) {
            String researchId = customData.copyTag().getString("research_id");
            // Try to get the research node to show its name
            com.hexvane.strangematter.research.ResearchNode node = com.hexvane.strangematter.research.ResearchNodeRegistry.getNode(researchId);
            if (node != null) {
                // Show the research display name in green color
                tooltip.add(node.getDisplayName().copy().withStyle(style -> style.withColor(0x00FF00)));
            } else {
                tooltip.add(Component.translatable("item.strangematter.research_note.for_research", researchId));
            }
        }
        
        super.appendHoverText(stack, context, tooltip, flag);
    }
    
    /**
     * Creates a research note with the specified research types and costs
     */
    public static ItemStack createResearchNote(Map<ResearchType, Integer> researchCosts, String researchId) {
        ItemStack stack = new ItemStack(StrangeMatterMod.RESEARCH_NOTES.get());
        
        // Store research data using DataComponents
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
            // Store research types
            ListTag typesList = new ListTag();
            for (ResearchType type : researchCosts.keySet()) {
                typesList.add(StringTag.valueOf(type.name()));
            }
            tag.put("research_types", typesList);
            
            // Store research costs
            CompoundTag costsTag = new CompoundTag();
            for (Map.Entry<ResearchType, Integer> entry : researchCosts.entrySet()) {
                costsTag.putInt(entry.getKey().name(), entry.getValue());
            }
            tag.put("research_costs", costsTag);
            
            // Store the research ID this note is for
            tag.putString("research_id", researchId);
        });
        
        return stack;
    }
    
    /**
     * Gets the research types required for this note
     */
    public static Set<ResearchType> getResearchTypes(ItemStack stack) {
        Set<ResearchType> types = new HashSet<>();
        net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        if (!customData.isEmpty() && customData.contains("research_types")) {
            ListTag typesList = customData.copyTag().getList("research_types", Tag.TAG_STRING);
            for (Tag typeTag : typesList) {
                types.add(ResearchType.valueOf(typeTag.getAsString()));
            }
        }
        return types;
    }
    
    /**
     * Gets the research ID this note is for
     */
    public static String getResearchId(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        if (!customData.isEmpty() && customData.contains("research_id")) {
            return customData.copyTag().getString("research_id");
        }
        return "";
    }
    
    /**
     * Checks if this is a valid research note
     */
    public static boolean isValidResearchNote(ItemStack stack) {
        if (!(stack.getItem() instanceof ResearchNoteItem)) {
            return false;
        }
        
        net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        return !customData.isEmpty() && 
               customData.contains("research_types") && 
               customData.contains("research_costs") && 
               customData.contains("research_id");
    }
}
