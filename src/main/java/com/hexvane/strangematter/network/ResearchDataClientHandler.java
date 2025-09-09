package com.hexvane.strangematter.network;

import com.hexvane.strangematter.research.ResearchData;
import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.client.Minecraft;

public class ResearchDataClientHandler {
    private static ResearchData clientResearchData = new ResearchData();
    
    public static void handleResearchSync(ResearchData data) {
        clientResearchData = data;
    }
    
    public static void handleResearchGain(ResearchType type, int amount) {
        clientResearchData.addResearchPoints(type, amount);
        // Show the research gain overlay
        com.hexvane.strangematter.client.ResearchOverlay.showResearchGain(type, amount);
    }
    
    public static ResearchData getClientResearchData() {
        return clientResearchData;
    }
}
