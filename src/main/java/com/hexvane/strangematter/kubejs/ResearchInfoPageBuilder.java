package com.hexvane.strangematter.kubejs;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for creating research info pages through KubeJS.
 * Info pages are displayed when viewing a research node in the Research Tablet.
 */
public class ResearchInfoPageBuilder {
    private String title;
    private String content;
    private boolean hasRecipes = false;
    private boolean hasScreenshots = false;
    private String recipeName = null;
    private boolean isRealityForgeRecipe = false;
    private String screenshotPath = null;
    
    public ResearchInfoPageBuilder() {
    }
    
    /**
     * Set the title for this info page.
     * Can be either a translation key (e.g., "research.modid.page.title")
     * or plain text.
     */
    public ResearchInfoPageBuilder title(String title) {
        this.title = title;
        return this;
    }
    
    /**
     * Set the content/description for this info page.
     * Can be either a translation key (e.g., "research.modid.page.content")
     * or plain text. Supports multiple lines.
     */
    public ResearchInfoPageBuilder content(String content) {
        this.content = content;
        return this;
    }
    
    /**
     * Set a recipe to display on this page.
     * The recipe name should match a recipe ID without the namespace.
     * Example: "my_custom_item" for recipe "strangematter:my_custom_item"
     */
    public ResearchInfoPageBuilder recipe(String recipeName) {
        this.hasRecipes = true;
        this.recipeName = recipeName;
        return this;
    }
    
    /**
     * Set a Reality Forge recipe to display on this page.
     * Reality Forge recipes have special rendering.
     */
    public ResearchInfoPageBuilder realityForgeRecipe(String recipeName) {
        this.hasRecipes = true;
        this.recipeName = recipeName;
        this.isRealityForgeRecipe = true;
        return this;
    }
    
    /**
     * Set a screenshot/image to display on this page.
     * Path should be a resource location.
     * Example: "strangematter:textures/ui/my_screenshot.png"
     */
    public ResearchInfoPageBuilder screenshot(String screenshotPath) {
        this.hasScreenshots = true;
        this.screenshotPath = screenshotPath;
        return this;
    }
    
    /**
     * Build and return the info page data.
     */
    public ResearchInfoPage build() {
        if (title == null || title.isEmpty()) {
            throw new IllegalStateException("Info page must have a title");
        }
        
        ResearchInfoPage page = new ResearchInfoPage();
        page.title = title;
        page.content = content != null ? content : "";
        page.hasRecipes = hasRecipes;
        page.hasScreenshots = hasScreenshots;
        page.recipeName = recipeName;
        page.isRealityForgeRecipe = isRealityForgeRecipe;
        page.screenshotPath = screenshotPath;
        
        return page;
    }
}

