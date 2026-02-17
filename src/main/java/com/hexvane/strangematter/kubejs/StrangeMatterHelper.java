package com.hexvane.strangematter.kubejs;

import com.hexvane.strangematter.research.ResearchNode;
import com.hexvane.strangematter.research.ResearchType;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Helper class providing static methods for KubeJS scripts to interact with Strange Matter.
 * This class is registered as a global binding in KubeJS scripts.
 * 
 * Usage in scripts:
 * <pre>
 * // Add a custom research node
 * let builder = StrangeMatter.createResearchNode('my_custom_research');
 * builder.position(150, 300);
 * builder.cost('gravity', 20);
 * builder.iconItem('minecraft:diamond');
 * StrangeMatter.registerNode(builder);
 * 
 * // Or use method chaining:
 * StrangeMatter.registerNode(
 *   StrangeMatter.createResearchNode('my_research')
 *     .position(100, 200)
 *     .cost('energy', 15)
 *     .iconItem('minecraft:nether_star')
 * );
 * </pre>
 */
public class StrangeMatterHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Create a new research node builder.
     * 
     * @param id Unique identifier for the research node
     * @return A new ResearchNodeBuilder
     */
    public static ResearchNodeBuilder createResearchNode(String id) {
        return new ResearchNodeBuilder(id);
    }
    
    /**
     * Register a research node with the game.
     * This should be called during startup (not in server scripts).
     * 
     * @param builder The configured research node builder
     */
    public static void registerNode(ResearchNodeBuilder builder) {
        ResearchNode node = builder.build();
        CustomResearchRegistry.registerNode(node);
    }
    
    /**
     * Create a new research category builder.
     * 
     * @param id Unique identifier for the research category
     * @return A new ResearchCategoryBuilder
     */
    public static ResearchCategoryBuilder createCategory(String id) {
        return new ResearchCategoryBuilder(id);
    }
    
    /**
     * Register a research category with the game.
     * This should be called during startup (not in server scripts).
     * 
     * @param builder The configured research category builder
     */
    public static void registerCategory(ResearchCategoryBuilder builder) {
        com.hexvane.strangematter.research.ResearchCategory category = builder.build();
        CustomResearchRegistry.registerCategory(category);
    }
    
    /**
     * Create a new info page builder.
     * 
     * @return A new ResearchInfoPageBuilder
     */
    public static ResearchInfoPageBuilder createInfoPage() {
        return new ResearchInfoPageBuilder();
    }
    
    /**
     * Register info pages for a research node.
     * Pages will be displayed when viewing the research node in the Research Tablet.
     * 
     * @param nodeId The ID of the research node
     * @param pages List of configured info pages
     */
    public static void registerInfoPages(String nodeId, List<ResearchInfoPage> pages) {
        CustomResearchRegistry.registerInfoPages(nodeId, pages);
    }
    
    /**
     * Helper method to register a single info page.
     * 
     * @param nodeId The ID of the research node
     * @param page A single configured info page
     */
    public static void registerInfoPage(String nodeId, ResearchInfoPage page) {
        LOGGER.info("[Strange Matter KubeJS] registerInfoPage called for {}", nodeId);
        List<ResearchInfoPage> pages = new ArrayList<>();
        pages.add(page);
        CustomResearchRegistry.registerInfoPages(nodeId, pages);
    }
    
    /**
     * Simple method to add a text-only info page.
     * This is the most JavaScript-friendly approach.
     * 
     * @param nodeId The ID of the research node
     * @param title The page title
     * @param content The page content
     */
    public static void addSimplePage(String nodeId, String title, String content) {
        LOGGER.info("[Strange Matter KubeJS] addSimplePage called for {}: {}", nodeId, title);
        
        // Create page directly without using builder
        ResearchInfoPage page = new ResearchInfoPage();
        page.title = title;
        page.content = content;
        page.hasRecipes = false;
        page.hasScreenshots = false;
        page.recipeName = null;
        page.isRealityForgeRecipe = false;
        page.screenshotPath = null;
        
        LOGGER.info("[Strange Matter KubeJS] Created page object: {}", page.title);
        
        // Get existing pages or create new list
        List<ResearchInfoPage> existingPages = CustomResearchRegistry.getInfoPages(nodeId);
        List<ResearchInfoPage> allPages = existingPages != null ? new ArrayList<>(existingPages) : new ArrayList<>();
        allPages.add(page);
        
        LOGGER.info("[Strange Matter KubeJS] Registering {} total pages for {}", allPages.size(), nodeId);
        CustomResearchRegistry.registerInfoPages(nodeId, allPages);
    }
    
    /**
     * Add a page with a recipe.
     * 
     * @param nodeId The ID of the research node
     * @param title The page title
     * @param content The page content
     * @param recipeName The recipe to display
     */
    public static void addRecipePage(String nodeId, String title, String content, String recipeName) {
        LOGGER.info("[Strange Matter KubeJS] addRecipePage called for {}", nodeId);
        
        // Create page directly
        ResearchInfoPage page = new ResearchInfoPage();
        page.title = title;
        page.content = content;
        page.hasRecipes = true;
        page.hasScreenshots = false;
        page.recipeName = recipeName;
        page.isRealityForgeRecipe = false;
        page.screenshotPath = null;
        
        // Get existing pages or create new list
        List<ResearchInfoPage> existingPages = CustomResearchRegistry.getInfoPages(nodeId);
        List<ResearchInfoPage> allPages = existingPages != null ? new ArrayList<>(existingPages) : new ArrayList<>();
        allPages.add(page);
        
        LOGGER.info("[Strange Matter KubeJS] Registering {} total pages for {}", allPages.size(), nodeId);
        CustomResearchRegistry.registerInfoPages(nodeId, allPages);
    }
    
    /**
     * Create an info pages builder for a specific research node.
     * This returns the builder so you can chain page() calls.
     * 
     * Example:
     * <pre>
     * let builder = StrangeMatter.createInfoPagesBuilder('my_research');
     * builder.page().title('Page 1').content('Content 1').build();
     * builder.page().title('Page 2').recipe('my_item').build();
     * builder.finish(); // Registers all pages
     * </pre>
     * 
     * @param nodeId The ID of the research node
     * @return A new InfoPagesBuilder
     */
    public static InfoPagesBuilder createInfoPagesBuilder(String nodeId) {
        LOGGER.info("[Strange Matter KubeJS] Created info pages builder for: {}", nodeId);
        return new InfoPagesBuilder(nodeId);
    }
    
    /**
     * Legacy method - kept for compatibility but uses simpler API internally.
     * 
     * @param nodeId The ID of the research node
     * @param consumer Consumer that configures the pages (may not work in all KubeJS versions)
     */
    public static void addInfoPages(String nodeId, Consumer<InfoPagesBuilder> consumer) {
        LOGGER.info("[Strange Matter KubeJS] addInfoPages called for: {}", nodeId);
        InfoPagesBuilder builder = new InfoPagesBuilder(nodeId);
        consumer.accept(builder);
        builder.finish();
    }
    
    /**
     * Get the custom research registry for advanced usage.
     * Most scripts won't need this - use the helper methods instead.
     * 
     * @return The CustomResearchRegistry instance
     */
    public static Class<CustomResearchRegistry> getRegistry() {
        return CustomResearchRegistry.class;
    }
    
    /**
     * Helper class for building multiple info pages at once.
     */
    public static class InfoPagesBuilder {
        private final List<ResearchInfoPage> pages = new ArrayList<>();
        private final String nodeId;
        
        public InfoPagesBuilder(String nodeId) {
            this.nodeId = nodeId;
        }
        
        /**
         * Start building a new info page.
         * Auto-adds the page when the builder chain completes.
         * 
         * @return A custom ResearchInfoPageBuilder that auto-adds to this collection
         */
        public CustomResearchInfoPageBuilder page() {
            return new CustomResearchInfoPageBuilder(this);
        }
        
        void addPage(ResearchInfoPage page) {
            pages.add(page);
            LOGGER.info("[Strange Matter KubeJS] Added page: {}", page.title);
        }
        
        /**
         * Finish building and register all pages.
         * Call this after adding all pages.
         */
        public void finish() {
            LOGGER.info("[Strange Matter KubeJS] Finishing builder with {} pages", pages.size());
            CustomResearchRegistry.registerInfoPages(nodeId, pages);
        }
        
        List<ResearchInfoPage> build() {
            return pages;
        }
    }
    
    /**
     * Custom builder that auto-adds to the parent InfoPagesBuilder.
     */
    public static class CustomResearchInfoPageBuilder extends ResearchInfoPageBuilder {
        private final InfoPagesBuilder parent;
        
        CustomResearchInfoPageBuilder(InfoPagesBuilder parent) {
            this.parent = parent;
        }
        
        @Override
        public ResearchInfoPageBuilder title(String title) {
            super.title(title);
            return this;
        }
        
        @Override
        public ResearchInfoPageBuilder content(String content) {
            super.content(content);
            return this;
        }
        
        @Override
        public ResearchInfoPageBuilder recipe(String recipeName) {
            super.recipe(recipeName);
            return this;
        }
        
        @Override
        public ResearchInfoPageBuilder realityForgeRecipe(String recipeName) {
            super.realityForgeRecipe(recipeName);
            return this;
        }
        
        @Override
        public ResearchInfoPageBuilder screenshot(String screenshotPath) {
            super.screenshot(screenshotPath);
            return this;
        }
        
        @Override
        public ResearchInfoPage build() {
            ResearchInfoPage page = super.build();
            parent.addPage(page);
            return page;
        }
    }
}

