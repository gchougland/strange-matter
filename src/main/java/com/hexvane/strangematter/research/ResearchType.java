package com.hexvane.strangematter.research;

import net.minecraft.resources.ResourceLocation;

public enum ResearchType {
    COGNITION("cognition", "strangematter:textures/ui/cognition_icon.png"),
    ENERGY("energy", "strangematter:textures/ui/energy_icon.png"),
    GRAVITY("gravity", "strangematter:textures/ui/gravity_icon.png"),
    SHADOW("shadow", "strangematter:textures/ui/shadow_icon.png"),
    SPACE("space", "strangematter:textures/ui/space_icon.png"),
    TIME("time", "strangematter:textures/ui/time_icon.png");
    
    private final String name;
    private final String iconPath;
    
    ResearchType(String name, String iconPath) {
        this.name = name;
        this.iconPath = iconPath;
    }
    
    public String getName() {
        return name;
    }
    
    public String getIconPath() {
        return iconPath;
    }
    
    public ResourceLocation getIconResourceLocation() {
        return ResourceLocation.parse(iconPath);
    }
    
    public String getTranslationKey() {
        return "research.strangematter." + name;
    }
    
    public static ResearchType fromName(String name) {
        for (ResearchType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
