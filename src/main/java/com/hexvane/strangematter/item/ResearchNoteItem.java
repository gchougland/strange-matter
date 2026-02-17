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
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ResearchNoteItem extends Item {
    
    public ResearchNoteItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();
        
        // Show which research this note is for
        if (tag.contains("research_id")) {
            String researchId = tag.getString("research_id");
            // Try to get the research node to show its name
            com.hexvane.strangematter.research.ResearchNode node = com.hexvane.strangematter.research.ResearchNodeRegistry.getNode(researchId);
            if (node != null) {
                // Show the research display name in green color
                tooltip.add(node.getDisplayName().copy().withStyle(style -> style.withColor(0x00FF00)));
            } else {
                tooltip.add(Component.translatable("item.strangematter.research_note.for_research", researchId));
            }
        }
        
        super.appendHoverText(stack, level, tooltip, flag);
    }
    
    /**
     * Creates a research note with the specified research type ids and costs (string-keyed).
     */
    public static ItemStack createResearchNote(Map<String, Integer> researchCosts, String researchId) {
        ItemStack stack = new ItemStack(StrangeMatterMod.RESEARCH_NOTES.get());
        CompoundTag tag = stack.getOrCreateTag();
        
        ListTag typesList = new ListTag();
        for (String typeId : researchCosts.keySet()) {
            typesList.add(StringTag.valueOf(typeId));
        }
        tag.put("research_types", typesList);
        
        CompoundTag costsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : researchCosts.entrySet()) {
            costsTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("research_costs", costsTag);
        
        tag.putString("research_id", researchId);
        return stack;
    }
    
    /**
     * Gets the research types required for this note (built-in enum only; for Research Machine).
     * Custom type ids in the note are not included since the machine has no minigames for them.
     */
    public static Set<ResearchType> getResearchTypes(ItemStack stack) {
        Set<ResearchType> types = new HashSet<>();
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("research_types")) {
            ListTag typesList = tag.getList("research_types", Tag.TAG_STRING);
            for (Tag typeTag : typesList) {
                String typeId = typeTag.getAsString();
                ResearchType type = ResearchType.fromName(typeId);
                if (type != null) {
                    types.add(type);
                }
            }
        }
        return types;
    }
    
    /**
     * Gets the research ID this note is for
     */
    public static String getResearchId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("research_id")) {
            return tag.getString("research_id");
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
        
        CompoundTag tag = stack.getTag();
        return tag != null && 
               tag.contains("research_types") && 
               tag.contains("research_costs") && 
               tag.contains("research_id");
    }
}
