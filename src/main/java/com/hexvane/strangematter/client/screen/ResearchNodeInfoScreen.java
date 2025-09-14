package com.hexvane.strangematter.client.screen;

import com.hexvane.strangematter.research.ResearchNode;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

public class ResearchNodeInfoScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("strangematter:textures/ui/research_tablet_background.png");
    
    private final ResearchNode node;
    private final Screen parentScreen;
    private int currentPage = 0;
    private List<InfoPage> pages;
    
    // UI dimensions
    private int guiX, guiY;
    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 240;
    
    // Navigation buttons
    private Button prevButton, nextButton, closeButton;
    
    public ResearchNodeInfoScreen(ResearchNode node, Screen parentScreen) {
        super(node.getDisplayName());
        this.node = node;
        this.parentScreen = parentScreen;
        this.pages = new ArrayList<>();
        
        // Initialize pages (will be populated with recipes and screenshots)
        initializePages();
    }
    
    private void initializePages() {
        if (node.getId().equals("foundation")) {
            initializeFoundationPages();
        } else if (node.getId().equals("basic_scanner")) {
            initializeResonatorPages();
        } else {
            // Default pages for other research nodes
            initializeDefaultPages();
        }
    }
    
    private void initializeFoundationPages() {
        // Page 1: Introduction to Anomaly Research
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.foundation.intro.title";
        intro.content = "research.strangematter.foundation.intro.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = false;
        pages.add(intro);
        
        // Page 2: Research Types
        InfoPage researchTypes = new InfoPage();
        researchTypes.title = "research.strangematter.foundation.types.title";
        researchTypes.content = "research.strangematter.foundation.types.content";
        researchTypes.hasRecipes = false;
        researchTypes.hasScreenshots = false;
        pages.add(researchTypes);
        
        // Page 3: Field Scanner Recipe
        InfoPage scannerRecipe = new InfoPage();
        scannerRecipe.title = "research.strangematter.foundation.scanner.title";
        scannerRecipe.content = "research.strangematter.foundation.scanner.content";
        scannerRecipe.hasRecipes = true;
        scannerRecipe.hasScreenshots = false;
        // recipeItems no longer needed - extracted programmatically from recipe registry
        scannerRecipe.recipeName = "field_scanner";
        pages.add(scannerRecipe);
        
        // Page 4: Using the Field Scanner
        InfoPage scannerUsage = new InfoPage();
        scannerUsage.title = "research.strangematter.foundation.scanner_usage.title";
        scannerUsage.content = "research.strangematter.foundation.scanner_usage.content";
        scannerUsage.hasRecipes = false;
        scannerUsage.hasScreenshots = false;
        scannerUsage.screenshotPath = "strangematter:textures/ui/foundation_scanner_usage.png";
        pages.add(scannerUsage);
    }
    
    private void initializeResonatorPages() {
        // Page 1: Introduction to Anomaly Resonator
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.resonator.intro.title";
        intro.content = "research.strangematter.resonator.intro.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = false;
        pages.add(intro);
        
        // Page 2: How It Works
        InfoPage mechanics = new InfoPage();
        mechanics.title = "research.strangematter.resonator.mechanics.title";
        mechanics.content = "research.strangematter.resonator.mechanics.content";
        mechanics.hasRecipes = false;
        mechanics.hasScreenshots = false;
        pages.add(mechanics);
        
        // Page 3: Crafting Recipe
        InfoPage recipe = new InfoPage();
        recipe.title = "research.strangematter.resonator.recipe.title";
        recipe.content = "research.strangematter.resonator.recipe.content";
        recipe.hasRecipes = true;
        recipe.hasScreenshots = false;
        // recipeItems no longer needed - extracted programmatically from recipe registry
        recipe.recipeName = "anomaly_resonator";
        pages.add(recipe);
        
        // Page 4: Usage Guide
        InfoPage usage = new InfoPage();
        usage.title = "research.strangematter.resonator.usage.title";
        usage.content = "research.strangematter.resonator.usage.content";
        usage.hasRecipes = false;
        usage.hasScreenshots = false;
        pages.add(usage);
    }
    
    private void initializeDefaultPages() {
        // Default pages for other research nodes
        InfoPage basicInfo = new InfoPage();
        basicInfo.title = "Overview";
        basicInfo.content = node.getDescription();
        basicInfo.hasRecipes = hasRecipes();
        basicInfo.hasScreenshots = hasScreenshots();
        pages.add(basicInfo);
        
        if (hasRecipes()) {
            InfoPage recipes = new InfoPage();
            recipes.title = "Recipes";
            recipes.content = "Crafting recipes and construction details.";
            recipes.hasRecipes = true;
            recipes.hasScreenshots = false;
            pages.add(recipes);
        }
        
        if (hasScreenshots()) {
            InfoPage screenshots = new InfoPage();
            screenshots.title = "Examples";
            screenshots.content = "Visual examples and usage demonstrations.";
            screenshots.hasRecipes = false;
            screenshots.hasScreenshots = false;
            pages.add(screenshots);
        }
    }
    
    private boolean hasRecipes() {
        // TODO: Check if this node has associated recipes
        return node.getId().equals("foundation") || node.getId().equals("basic_scanner");
    }
    
    private boolean hasScreenshots() {
        // TODO: Check if this node has associated screenshots
        return node.getId().equals("gravity_control") || node.getId().equals("temporal_stability");
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Center the GUI
        guiX = (this.width - GUI_WIDTH) / 2;
        guiY = (this.height - GUI_HEIGHT) / 2;
        
        // Create navigation buttons
        prevButton = Button.builder(
            Component.translatable("gui.strangematter.info_page.previous"),
            (button) -> previousPage()
        )
        .bounds(guiX + 10, guiY + GUI_HEIGHT - 30, 40, 20)
        .build();
        
        nextButton = Button.builder(
            Component.translatable("gui.strangematter.info_page.next"),
            (button) -> nextPage()
        )
        .bounds(guiX + GUI_WIDTH - 50, guiY + GUI_HEIGHT - 30, 40, 20)
        .build();
        
        closeButton = Button.builder(
            Component.translatable("gui.strangematter.info_page.close"),
            (button) -> onClose()
        )
        .bounds(guiX + GUI_WIDTH - 30, guiY + 10, 20, 20)
        .build();
        
        addRenderableWidget(prevButton);
        addRenderableWidget(nextButton);
        addRenderableWidget(closeButton);
        
        updateButtonStates();
    }
    
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateButtonStates();
        }
    }
    
    private void nextPage() {
        if (currentPage < pages.size() - 1) {
            currentPage++;
            updateButtonStates();
        }
    }
    
    private void updateButtonStates() {
        prevButton.active = currentPage > 0;
        nextButton.active = currentPage < pages.size() - 1;
    }
    
    @Override
    public void onClose() {
        // Restore drag position in parent screen before closing
        if (parentScreen instanceof ResearchTabletScreen) {
            ((ResearchTabletScreen) parentScreen).restoreDragPosition();
        }
        this.minecraft.setScreen(parentScreen);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        
        // Render GUI background
        guiGraphics.fill(guiX, guiY, guiX + GUI_WIDTH, guiY + GUI_HEIGHT, 0xFF2C2C2C);
        guiGraphics.renderOutline(guiX, guiY, GUI_WIDTH, GUI_HEIGHT, 0xFF555555);
        
        // Render page content
        if (currentPage < pages.size()) {
            InfoPage page = pages.get(currentPage);
            renderPage(guiGraphics, page);
        }
        
        // Render page indicator
        Component pageText = Component.translatable("gui.strangematter.info_page.page")
            .append(" ")
            .append(Component.literal(String.valueOf(currentPage + 1)))
            .append(" ")
            .append(Component.translatable("gui.strangematter.info_page.of"))
            .append(" ")
            .append(Component.literal(String.valueOf(pages.size())));
        guiGraphics.drawCenteredString(this.font, pageText, guiX + GUI_WIDTH / 2, guiY + GUI_HEIGHT - 25, 0xFFFFFF);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void renderPage(GuiGraphics guiGraphics, InfoPage page) {
        // Render title
        Component titleComponent = Component.translatable(page.title);
        guiGraphics.drawCenteredString(this.font, titleComponent, guiX + GUI_WIDTH / 2, guiY + 20, 0xFFFFFF);
        
        if (page.hasRecipes) {
            // Two-column layout: text on left, recipe on right
            renderTwoColumnLayout(guiGraphics, page);
        } else {
            // Single column layout
            renderSingleColumnLayout(guiGraphics, page);
        }
    }
    
    private void renderTwoColumnLayout(GuiGraphics guiGraphics, InfoPage page) {
        int contentY = guiY + 50;
        int contentHeight = GUI_HEIGHT - 100;
        
        // Left column: text content
        int leftColumnX = guiX + 20;
        int leftColumnWidth = (GUI_WIDTH - 60) / 2; // Half width minus padding
        
        // Right column: recipe
        int rightColumnX = guiX + 20 + leftColumnWidth + 20;
        int rightColumnWidth = (GUI_WIDTH - 60) / 2;
        
        // Render text content in left column
        guiGraphics.drawWordWrap(this.font, Component.translatable(page.content), leftColumnX, contentY, leftColumnWidth, 0xCCCCCC);
        
        // Render recipe in right column
        renderRecipes(guiGraphics, rightColumnX, contentY, rightColumnWidth);
        
        // Render screenshots below if needed
        if (page.hasScreenshots) {
            int textHeight = this.font.wordWrapHeight(Component.translatable(page.content), leftColumnWidth);
            renderScreenshots(guiGraphics, leftColumnX, contentY + textHeight + 20, leftColumnWidth + rightColumnWidth + 20);
        }
    }
    
    private void renderSingleColumnLayout(GuiGraphics guiGraphics, InfoPage page) {
        int contentX = guiX + 20;
        int contentY = guiY + 50;
        int contentWidth = GUI_WIDTH - 40;
        
        // Render content
        guiGraphics.drawWordWrap(this.font, Component.translatable(page.content), contentX, contentY, contentWidth, 0xCCCCCC);
        
        // Render screenshots below if needed
        if (page.hasScreenshots) {
            int textHeight = this.font.wordWrapHeight(Component.translatable(page.content), contentWidth);
            renderScreenshots(guiGraphics, contentX, contentY + textHeight + 20, contentWidth);
        }
    }
    
    private void renderRecipes(GuiGraphics guiGraphics, int x, int y, int width) {
        if (currentPage < pages.size()) {
            InfoPage page = pages.get(currentPage);
            
            if (page.hasRecipes && page.recipeName != null) {
                // Draw recipe title
                guiGraphics.drawString(this.font, Component.translatable("gui.strangematter.info_page.crafting_recipe"), x, y, 0xFFFFFF);
                y += 15;
                
                // Draw recipe grid (3x3 crafting grid)
                int recipeX = x + 20;
                int recipeY = y;
                int slotSize = 18;
                
                // Draw 3x3 crafting grid background
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 3; col++) {
                        int slotX = recipeX + col * slotSize;
                        int slotY = recipeY + row * slotSize;
                        
                        // Draw slot background
                        guiGraphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0xFF404040);
                        guiGraphics.renderOutline(slotX, slotY, slotSize, slotSize, 0xFF808080);
                    }
                }
                
                // Draw actual recipe items in the crafting grid
                if (page.recipeName != null) {
                    drawRecipeInGrid(guiGraphics, page.recipeName, recipeX, recipeY, slotSize);
                }
                
                // Draw required materials list - now programmatic!
                y += 70;
                guiGraphics.drawString(this.font, Component.translatable("gui.strangematter.info_page.required_materials"), x, y, 0xFFFFAA00);
                y += 15;
                
                // Get unique ingredients from the actual recipe
                List<String> uniqueIngredients = getUniqueIngredients(page.recipeName);
                for (String ingredientName : uniqueIngredients) {
                    guiGraphics.drawString(this.font, "• " + ingredientName, x + 10, y, 0xCCCCCC);
                    y += 12;
                }
            }
        }
    }
    
    private void drawRecipeInGrid(GuiGraphics guiGraphics, String recipeName, int gridX, int gridY, int slotSize) {
        // Get the recipe from Minecraft's recipe registry
        ResourceLocation recipeId = ResourceLocation.parse("strangematter:" + recipeName);
        Recipe<?> recipe = this.minecraft.level.getRecipeManager().byKey(recipeId).orElse(null);

        if (recipe == null) {
            // Recipe not found, draw empty grid
            drawEmptyRecipeGrid(guiGraphics, gridX, gridY, slotSize);
            return;
        }

        // Handle different recipe types
        if (recipe instanceof CraftingRecipe craftingRecipe) {
            drawCraftingRecipe(guiGraphics, craftingRecipe, gridX, gridY, slotSize);
        } else {
            // Unsupported recipe type, draw empty grid
            drawEmptyRecipeGrid(guiGraphics, gridX, gridY, slotSize);
        }
    }
    
    private void drawCraftingRecipe(GuiGraphics guiGraphics, CraftingRecipe recipe, int gridX, int gridY, int slotSize) {
        // Get the ingredients from the recipe
        List<Ingredient> ingredients = recipe.getIngredients();

        // Draw the 3x3 crafting grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = row * 3 + col;
                int slotX = gridX + col * slotSize;
                int slotY = gridY + row * slotSize;

                // Draw slot background
                guiGraphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0xFF404040);
                guiGraphics.renderOutline(slotX, slotY, slotSize, slotSize, 0xFF808080);

                // Draw ingredient if present
                if (slotIndex < ingredients.size()) {
                    Ingredient ingredient = ingredients.get(slotIndex);
                    if (!ingredient.isEmpty()) {
                        ItemStack[] stacks = ingredient.getItems();
                        if (stacks.length > 0) {
                            // Render the first item in the ingredient
                            guiGraphics.renderItem(stacks[0], slotX + 1, slotY + 1);
                        }
                    }
                }
            }
        }

        // Draw result item on the right side
        ItemStack resultStack = recipe.getResultItem(this.minecraft.level.registryAccess());
        int resultX = gridX + 3 * slotSize + 10; // 10 pixels spacing from grid
        int resultY = gridY + slotSize; // Center vertically

        // Draw result slot background
        guiGraphics.fill(resultX, resultY, resultX + slotSize, resultY + slotSize, 0xFF404040);
        guiGraphics.renderOutline(resultX, resultY, slotSize, slotSize, 0xFF808080);

        // Draw result item
        guiGraphics.renderItem(resultStack, resultX + 1, resultY + 1);
    }
    
    private void drawEmptyRecipeGrid(GuiGraphics guiGraphics, int gridX, int gridY, int slotSize) {
        // Draw empty 3x3 grid when recipe is not found
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = gridX + col * slotSize;
                int slotY = gridY + row * slotSize;
                
                guiGraphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0xFF404040);
                guiGraphics.renderOutline(slotX, slotY, slotSize, slotSize, 0xFF808080);
            }
        }
    }
    
    private List<String> getUniqueIngredients(String recipeName) {
        List<String> uniqueIngredients = new ArrayList<>();
        
        // Get the recipe from Minecraft's recipe registry
        ResourceLocation recipeId = ResourceLocation.parse("strangematter:" + recipeName);
        Recipe<?> recipe = this.minecraft.level.getRecipeManager().byKey(recipeId).orElse(null);
        
        if (recipe instanceof CraftingRecipe craftingRecipe) {
            List<Ingredient> ingredients = craftingRecipe.getIngredients();
            for (Ingredient ingredient : ingredients) {
                if (!ingredient.isEmpty()) {
                    ItemStack[] stacks = ingredient.getItems();
                    if (stacks.length > 0) {
                        // Get the first item in the ingredient and get its localized name
                        ItemStack stack = stacks[0];
                        String localizedName = stack.getHoverName().getString();
                        
                        // Only add if not already in the list
                        if (!uniqueIngredients.contains(localizedName)) {
                            uniqueIngredients.add(localizedName);
                        }
                    }
                }
            }
        }
        
        return uniqueIngredients;
    }
    
    private void drawTextureInSlot(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y) {
        // Simple texture drawing - no unnecessary mapping functions
        RenderSystem.setShaderTexture(0, texture);
        guiGraphics.blit(texture, x + 1, y + 1, 0, 0, 16, 16, 16, 16);
    }
    
    private String getItemDisplayName(String itemId) {
        // Use proper localization keys that will be resolved by Minecraft's localization system
        return "item." + itemId.replace(":", ".");
    }
    
    private void renderScreenshots(GuiGraphics guiGraphics, int x, int y, int width) {
        if (currentPage < pages.size()) {
            InfoPage page = pages.get(currentPage);
            
            if (page.hasScreenshots && page.screenshotPath != null) {
                // Draw screenshot title
                guiGraphics.drawString(this.font, "Visual Example:", x, y, 0xFFFFFF);
                y += 15;
                
                // Draw screenshot placeholder
                int screenshotWidth = 120;
                int screenshotHeight = 80;
                
                // Draw screenshot background
                guiGraphics.fill(x, y, x + screenshotWidth, y + screenshotHeight, 0xFF202020);
                guiGraphics.renderOutline(x, y, screenshotWidth, screenshotHeight, 0xFF555555);
                
                // Draw screenshot content (placeholder text for now)
                guiGraphics.drawCenteredString(this.font, "Screenshot", x + screenshotWidth / 2, y + screenshotHeight / 2 - 5, 0x888888);
                guiGraphics.drawCenteredString(this.font, "Coming Soon", x + screenshotWidth / 2, y + screenshotHeight / 2 + 5, 0x888888);
                
                // Draw caption
                y += screenshotHeight + 10;
                guiGraphics.drawString(this.font, "Image: Field Scanner in action", x, y, 0xAAAAAA);
            }
        }
    }
    
    
    // Helper class to store page information
    private static class InfoPage {
        String title;
        String content;
        boolean hasRecipes;
        boolean hasScreenshots;
        String[] recipeItems; // Items needed for recipe
        String recipeName; // Recipe identifier
        String screenshotPath; // Path to screenshot texture
    }
}
