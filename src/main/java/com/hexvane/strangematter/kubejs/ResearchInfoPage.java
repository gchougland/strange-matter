package com.hexvane.strangematter.kubejs;

/**
 * Data class representing a research info page.
 * This mirrors the InfoPage inner class from ResearchNodeInfoScreen
 * but is accessible from KubeJS.
 */
public class ResearchInfoPage {
    public String title;
    public String content;
    public boolean hasRecipes;
    public boolean hasScreenshots;
    public String recipeName;
    public boolean isRealityForgeRecipe;
    public String screenshotPath;
    
    public ResearchInfoPage() {
    }
}

