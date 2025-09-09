package com.hexvane.strangematter.research;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class ScannableObject {
    private final String id;
    private final ResearchType researchType;
    private final int researchAmount;
    private final boolean isEntity;
    private final ResourceLocation resourceLocation;
    
    public ScannableObject(String id, ResearchType researchType, int researchAmount, boolean isEntity, ResourceLocation resourceLocation) {
        this.id = id;
        this.researchType = researchType;
        this.researchAmount = researchAmount;
        this.isEntity = isEntity;
        this.resourceLocation = resourceLocation;
    }
    
    public String getId() {
        return id;
    }
    
    public ResearchType getResearchType() {
        return researchType;
    }
    
    public int getResearchAmount() {
        return researchAmount;
    }
    
    public boolean isEntity() {
        return isEntity;
    }
    
    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }
    
    public String generateObjectId(Entity entity) {
        return id + "_" + entity.getUUID().toString();
    }
    
    public String generateObjectId(BlockPos pos) {
        return id + "_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
    }
    
    public static ScannableObject forEntity(ResourceLocation entityType, ResearchType researchType, int amount) {
        return new ScannableObject(entityType.toString(), researchType, amount, true, entityType);
    }
    
    public static ScannableObject forBlock(ResourceLocation blockType, ResearchType researchType, int amount) {
        return new ScannableObject(blockType.toString(), researchType, amount, false, blockType);
    }
}
