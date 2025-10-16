package com.hexvane.strangematter.client.screen;

import com.hexvane.strangematter.research.ResearchNode;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ResearchNodeInfoScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
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
    
    public ResearchNodeInfoScreen(ResearchNode node, Screen parentScreen, int startingPage) {
        super(node.getDisplayName());
        this.node = node;
        this.parentScreen = parentScreen;
        this.currentPage = startingPage;
        this.pages = new ArrayList<>();
        
        // Initialize pages (will be populated with recipes and screenshots)
        initializePages();
    }
    
    private void initializePages() {
        // First, load the built-in pages
        if (node.getId().equals("research")) {
            initializeResearchPages();
        } else if (node.getId().equals("foundation")) {
            initializeFoundationPages();
        } else if (node.getId().equals("anomaly_resonator")) {
            initializeResonatorPages();
        } else if (node.getId().equals("field_scanner")) {
            initializeFieldScannerPages();
        } else if (node.getId().equals("anomaly_shards")) {
            initializeAnomalyShardsPages();
        } else if (node.getId().equals("anomaly_types")) {
            initializeAnomalyTypesPages();
        } else if (node.getId().equals("resonite")) {
            initializeResonitePages();
        } else if (node.getId().equals("resonant_energy")) {
            initializeResonantEnergyPages();
        } else if (node.getId().equals("reality_forge")) {
            initializeRealityForgePages();
        } else if (node.getId().equals("resonance_condenser")) {
            initializeResonanceCondenserPages();
        } else if (node.getId().equals("containment_basics")) {
            initializeContainmentBasicsPages();
        } else if (node.getId().equals("echoform_imprinter")) {
            initializeEchoformImprinterPages();
        } else if (node.getId().equals("warp_gun")) {
            initializeWarpGunPages();
        } else if (node.getId().equals("stasis_projector")) {
            initializeStasisProjectorPages();
        } else if (node.getId().equals("rift_stabilizer")) {
            initializeRiftStabilizerPages();
        } else if (node.getId().equals("levitation_pad")) {
            initializeLevitationPadPages();
        } else if (node.getId().equals("graviton_hammer")) {
            initializeGravitonHammerPages();
        } else if (node.getId().equals("gravity_anomalies")) {
            initializeGravityAnomaliesPages();
        } else if (node.getId().equals("temporal_anomalies")) {
            initializeTemporalAnomaliesPages();
        } else if (node.getId().equals("spatial_anomalies")) {
            initializeSpatialAnomaliesPages();
        } else if (node.getId().equals("energy_anomalies")) {
            initializeEnergyAnomaliesPages();
        } else if (node.getId().equals("shadow_anomalies")) {
            initializeShadowAnomaliesPages();
        } else if (node.getId().equals("cognitive_anomalies")) {
            initializeCognitiveAnomaliesPages();
        } else {
            // Check if this node has custom pages from KubeJS
            // If so, skip the default "Overview" page
            if (!hasCustomPagesFromKubeJS()) {
                // Default pages for other research nodes
                initializeDefaultPages();
            }
        }
        
        // After loading built-in pages, append any custom pages from KubeJS
        loadCustomPages();
    }
    
    /**
     * Check if custom pages will be loaded from KubeJS for this node.
     */
    private boolean hasCustomPagesFromKubeJS() {
        try {
            Class<?> customRegistry = Class.forName("com.hexvane.strangematter.kubejs.CustomResearchRegistry");
            Boolean hasPages = (Boolean) customRegistry.getMethod("hasCustomPages", String.class)
                .invoke(null, node.getId());
            return hasPages != null && hasPages;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Load and append custom info pages from KubeJS if available.
     * Custom pages are added AFTER the built-in pages.
     */
    private void loadCustomPages() {
        try {
            // Try to load custom pages from KubeJS
            Class<?> customRegistry = Class.forName("com.hexvane.strangematter.kubejs.CustomResearchRegistry");
            Class<?> researchInfoPage = Class.forName("com.hexvane.strangematter.kubejs.ResearchInfoPage");
            
            LOGGER.info("[Strange Matter] Checking for custom pages for node: {}", node.getId());
            
            // Check if custom pages exist for this node
            Boolean hasPages = (Boolean) customRegistry.getMethod("hasCustomPages", String.class)
                .invoke(null, node.getId());
            
            LOGGER.info("[Strange Matter] Has custom pages: {}", hasPages);
            
            if (hasPages != null && hasPages) {
                // Get the custom pages
                @SuppressWarnings("unchecked")
                java.util.List<Object> customPages = (java.util.List<Object>) customRegistry
                    .getMethod("getInfoPages", String.class)
                    .invoke(null, node.getId());
                
                LOGGER.info("[Strange Matter] Retrieved {} custom pages", (customPages != null ? customPages.size() : 0));
                
                if (customPages != null && !customPages.isEmpty()) {
                    // Convert custom pages to InfoPage objects
                    for (Object customPage : customPages) {
                        InfoPage page = new InfoPage();
                        
                        // Use reflection to get fields from ResearchInfoPage
                        page.title = (String) researchInfoPage.getField("title").get(customPage);
                        page.content = (String) researchInfoPage.getField("content").get(customPage);
                        page.hasRecipes = (Boolean) researchInfoPage.getField("hasRecipes").get(customPage);
                        page.hasScreenshots = (Boolean) researchInfoPage.getField("hasScreenshots").get(customPage);
                        page.recipeName = (String) researchInfoPage.getField("recipeName").get(customPage);
                        page.isRealityForgeRecipe = (Boolean) researchInfoPage.getField("isRealityForgeRecipe").get(customPage);
                        page.screenshotPath = (String) researchInfoPage.getField("screenshotPath").get(customPage);
                        
                        LOGGER.info("[Strange Matter] Adding custom page: {}", page.title);
                        pages.add(page);
                    }
                    
                    LOGGER.info("[Strange Matter] Appended {} custom pages to research node: {}", customPages.size(), node.getId());
                }
            }
        } catch (Exception e) {
            // Log the error for debugging
            LOGGER.error("[Strange Matter] Error loading custom pages for {}: {}", node.getId(), e.getMessage(), e);
        }
    }
    
    private void initializeResearchPages() {
        // Page 1: Introduction to Research
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.research.intro.title";
        intro.content = "research.strangematter.research.intro.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = false;
        pages.add(intro);
        
        // Page 2: Field Scanner (with recipe)
        InfoPage fieldScanner = new InfoPage();
        fieldScanner.title = "research.strangematter.research.field_scanner.title";
        fieldScanner.content = "research.strangematter.research.field_scanner.content";
        fieldScanner.hasRecipes = true;
        fieldScanner.hasScreenshots = false;
        fieldScanner.recipeName = "field_scanner";
        pages.add(fieldScanner);
        
        // Page 3: Research Tablet (with recipe)
        InfoPage researchTablet = new InfoPage();
        researchTablet.title = "research.strangematter.research.research_tablet.title";
        researchTablet.content = "research.strangematter.research.research_tablet.content";
        researchTablet.hasRecipes = true;
        researchTablet.hasScreenshots = false;
        researchTablet.recipeName = "research_tablet";
        pages.add(researchTablet);
        
        // Page 4: Using the Research Machine (with recipe)
        InfoPage machineUsage = new InfoPage();
        machineUsage.title = "research.strangematter.research.machine_usage.title";
        machineUsage.content = "research.strangematter.research.machine_usage.content";
        machineUsage.hasRecipes = true;
        machineUsage.hasScreenshots = false;
        machineUsage.recipeName = "research_machine";
        pages.add(machineUsage);
        
        // Page 3: Research Minigames
        InfoPage minigames = new InfoPage();
        minigames.title = "research.strangematter.research.minigames.title";
        minigames.content = "research.strangematter.research.minigames.content";
        minigames.hasRecipes = false;
        minigames.hasScreenshots = true;
        minigames.screenshotPath = "strangematter:textures/ui/research_minigames_overview.png";
        pages.add(minigames);
        
        // Page 4: Understanding Instability
        InfoPage instability = new InfoPage();
        instability.title = "research.strangematter.research.instability.title";
        instability.content = "research.strangematter.research.instability.content";
        instability.hasRecipes = false;
        instability.hasScreenshots = true;
        instability.screenshotPath = "strangematter:textures/ui/instability_gauge_example.png";
        pages.add(instability);
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
        // Page 1: Introduction with Recipe
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.anomaly_resonator.intro.title";
        intro.content = "research.strangematter.anomaly_resonator.intro.content";
        intro.hasRecipes = true;
        intro.hasScreenshots = false;
        intro.recipeName = "anomaly_resonator";
        pages.add(intro);
        
        // Page 2: How It Works and Usage
        InfoPage mechanics = new InfoPage();
        mechanics.title = "research.strangematter.anomaly_resonator.mechanics.title";
        mechanics.content = "research.strangematter.anomaly_resonator.mechanics.content";
        mechanics.hasRecipes = false;
        mechanics.hasScreenshots = false;
        pages.add(mechanics);
    }
    
    private void initializeFieldScannerPages() {
        // Page 1: Introduction to Field Scanner
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.field_scanner.intro.title";
        intro.content = "research.strangematter.field_scanner.intro.content";
        intro.hasRecipes = true;
        intro.hasScreenshots = false;
        intro.recipeName = "field_scanner";
        pages.add(intro);
    }
    
    private void initializeAnomalyShardsPages() {
        // Page 1: Introduction to Anomaly Shards
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.anomaly_shards.intro.title";
        intro.content = "research.strangematter.anomaly_shards.intro.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = true;
        intro.screenshotPath = "strangematter:textures/ui/anomaly_shard_ore.png";
        pages.add(intro);
        
        // Page 2: Formation Process
        InfoPage formation = new InfoPage();
        formation.title = "research.strangematter.anomaly_shards.formation.title";
        formation.content = "research.strangematter.anomaly_shards.formation.content";
        formation.hasRecipes = false;
        formation.hasScreenshots = false;
        formation.recipeName = null;
        formation.screenshotPath = null;
        pages.add(formation);
        
        // Page 3: Shard Categories
        InfoPage categories = new InfoPage();
        categories.title = "research.strangematter.anomaly_shards.categories.title";
        categories.content = "research.strangematter.anomaly_shards.categories.content";
        categories.hasRecipes = false;
        categories.hasScreenshots = false;
        categories.recipeName = null;
        categories.screenshotPath = null;
        pages.add(categories);
        
        // Page 4: Collection Methods
        InfoPage collection = new InfoPage();
        collection.title = "research.strangematter.anomaly_shards.collection.title";
        collection.content = "research.strangematter.anomaly_shards.collection.content";
        collection.hasRecipes = false;
        collection.hasScreenshots = false;
        collection.recipeName = null;
        collection.screenshotPath = null;
        pages.add(collection);
    }
    
    private void initializeAnomalyTypesPages() {
        // Page 1: Introduction to Anomaly Types
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.anomaly_types.intro.title";
        intro.content = "research.strangematter.anomaly_types.intro.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = false;
        pages.add(intro);
        
        // Page 2: The Six Fundamental Types
        InfoPage fundamentalTypes = new InfoPage();
        fundamentalTypes.title = "research.strangematter.anomaly_types.fundamental.title";
        fundamentalTypes.content = "research.strangematter.anomaly_types.fundamental.content";
        fundamentalTypes.hasRecipes = false;
        fundamentalTypes.hasScreenshots = false;
        pages.add(fundamentalTypes);
        
        // Page 3: Type Interactions
        InfoPage interactions = new InfoPage();
        interactions.title = "research.strangematter.anomaly_types.interactions.title";
        interactions.content = "research.strangematter.anomaly_types.interactions.content";
        interactions.hasRecipes = false;
        interactions.hasScreenshots = false;
        pages.add(interactions);
        
        // Page 4: Research Applications
        InfoPage applications = new InfoPage();
        applications.title = "research.strangematter.anomaly_types.applications.title";
        applications.content = "research.strangematter.anomaly_types.applications.content";
        applications.hasRecipes = false;
        applications.hasScreenshots = false;
        pages.add(applications);
    }
    
    private void initializeResonitePages() {
        // Page 1: Introduction with Screenshot
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.resonite.intro.title";
        intro.content = "research.strangematter.resonite.intro.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = true;
        intro.screenshotPath = "strangematter:textures/ui/resonite_ore.png";
        pages.add(intro);

        // Page 2: Resonant Coil Recipe
        InfoPage coil = new InfoPage();
        coil.title = "research.strangematter.resonite.coil.title";
        coil.content = "research.strangematter.resonite.coil.content";
        coil.hasRecipes = true;
        coil.hasScreenshots = false;
        coil.recipeName = "resonant_coil";
        pages.add(coil);

        // Page 3: Resonant Circuit Recipe
        InfoPage circuit = new InfoPage();
        circuit.title = "research.strangematter.resonite.circuit.title";
        circuit.content = "research.strangematter.resonite.circuit.content";
        circuit.hasRecipes = true;
        circuit.hasScreenshots = false;
        circuit.recipeName = "resonant_circuit";
        pages.add(circuit);
    }
    
    private void initializeResonantEnergyPages() {
        // Page 1: Introduction to Resonant Energy
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.resonant_energy.intro.title";
        intro.content = "research.strangematter.resonant_energy.intro.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = false;
        intro.recipeName = null;
        intro.screenshotPath = null;
        pages.add(intro);

        // Page 2: Resonant Burner Recipe
        InfoPage burner = new InfoPage();
        burner.title = "research.strangematter.resonant_energy.burner.title";
        burner.content = "research.strangematter.resonant_energy.burner.content";
        burner.hasRecipes = true;
        burner.hasScreenshots = false;
        burner.recipeName = "resonant_burner";
        pages.add(burner);
    }
    
    private void initializeRealityForgePages() {
        // Page 1: Introduction with Recipe
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.reality_forge.intro.title";
        intro.content = "research.strangematter.reality_forge.intro.content";
        intro.hasRecipes = true;
        intro.hasScreenshots = false;
        intro.recipeName = "reality_forge";
        pages.add(intro);

        // Page 2: Usage with Screenshot
        InfoPage usage = new InfoPage();
        usage.title = "research.strangematter.reality_forge.usage.title";
        usage.content = "research.strangematter.reality_forge.usage.content";
        usage.hasRecipes = false;
        usage.hasScreenshots = true;
        usage.screenshotPath = "strangematter:textures/ui/reality_forge.png";
        pages.add(usage);
    }
    
    private void initializeResonanceCondenserPages() {
        // Page 1: Introduction with Reality Forge Recipe
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.resonance_condenser.intro.title";
        intro.content = "research.strangematter.resonance_condenser.intro.content";
        intro.hasRecipes = true;
        intro.hasScreenshots = false;
        intro.recipeName = "resonance_condenser";
        intro.isRealityForgeRecipe = true;
        // Shard requirements are now read from the recipe registry
        pages.add(intro);

        // Page 2: Mechanics with Screenshot
        InfoPage mechanics = new InfoPage();
        mechanics.title = "research.strangematter.resonance_condenser.mechanics.title";
        mechanics.content = "research.strangematter.resonance_condenser.mechanics.content";
        mechanics.hasRecipes = false;
        mechanics.hasScreenshots = true;
        mechanics.screenshotPath = "strangematter:textures/ui/resonance_condenser.png";
        pages.add(mechanics);
    }
    
    private void initializeContainmentBasicsPages() {
        // Page 1: Introduction with Echo Vacuum Recipe
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.containment_basics.intro.title";
        intro.content = "research.strangematter.containment_basics.intro.content";
        intro.hasRecipes = true;
        intro.hasScreenshots = false;
        intro.recipeName = "echo_vacuum";
        intro.isRealityForgeRecipe = true;
        pages.add(intro);

        // Page 2: Containment Capsule Recipe
        InfoPage capsule = new InfoPage();
        capsule.title = "research.strangematter.containment_basics.capsule.title";
        capsule.content = "research.strangematter.containment_basics.capsule.content";
        capsule.hasRecipes = true;
        capsule.hasScreenshots = false;
        capsule.recipeName = "containment_capsule";
        capsule.isRealityForgeRecipe = true;
        pages.add(capsule);
    }
    
    private void initializeEchoformImprinterPages() {
        // Page 1: Introduction with Recipe
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.echoform_imprinter.intro.title";
        intro.content = "research.strangematter.echoform_imprinter.intro.content";
        intro.hasRecipes = true;
        intro.hasScreenshots = false;
        intro.recipeName = "echoform_imprinter";
        intro.isRealityForgeRecipe = true;
        pages.add(intro);
        
        // Page 2: Operational Protocols
        InfoPage usage = new InfoPage();
        usage.title = "research.strangematter.echoform_imprinter.usage.title";
        usage.content = "research.strangematter.echoform_imprinter.usage.content";
        usage.hasRecipes = false;
        usage.hasScreenshots = false;
        usage.recipeName = null;
        usage.screenshotPath = null;
        pages.add(usage);
        
        // Page 3: Shadow-Cognitive Theory
        InfoPage theory = new InfoPage();
        theory.title = "research.strangematter.echoform_imprinter.theory.title";
        theory.content = "research.strangematter.echoform_imprinter.theory.content";
        theory.hasRecipes = false;
        theory.hasScreenshots = false;
        theory.recipeName = null;
        theory.screenshotPath = null;
        pages.add(theory);
    }
    
    private void initializeWarpGunPages() {
        // Page 1: Introduction with Recipe
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.warp_gun.intro.title";
        intro.content = "research.strangematter.warp_gun.intro.content";
        intro.hasRecipes = true;
        intro.hasScreenshots = false;
        intro.recipeName = "warp_gun";
        intro.isRealityForgeRecipe = true;
        pages.add(intro);

        // Page 2: Teleportation Mechanics
        InfoPage mechanics = new InfoPage();
        mechanics.title = "research.strangematter.warp_gun.mechanics.title";
        mechanics.content = "research.strangematter.warp_gun.mechanics.content";
        mechanics.hasRecipes = false;
        mechanics.hasScreenshots = false;
        pages.add(mechanics);
    }
    
    private void initializeGravitonHammerPages() {
        // Page 1: Introduction with Recipe
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.graviton_hammer.intro.title";
        intro.content = "research.strangematter.graviton_hammer.intro.content";
        intro.hasRecipes = true;
        intro.hasScreenshots = false;
        intro.recipeName = "graviton_hammer";
        intro.isRealityForgeRecipe = true;
        pages.add(intro);

        // Page 2: Mining Mechanics
        InfoPage mechanics = new InfoPage();
        mechanics.title = "research.strangematter.graviton_hammer.mechanics.title";
        mechanics.content = "research.strangematter.graviton_hammer.mechanics.content";
        mechanics.hasRecipes = false;
        mechanics.hasScreenshots = false;
        mechanics.recipeName = null;
        mechanics.screenshotPath = null;
        pages.add(mechanics);
    }
    
    private void initializeStasisProjectorPages() {
        // Page 1: Introduction with Recipe
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.stasis_projector.intro.title";
        intro.content = "research.strangematter.stasis_projector.intro.content";
        intro.hasRecipes = true;
        intro.hasScreenshots = false;
        intro.recipeName = "stasis_projector";
        intro.isRealityForgeRecipe = true;
        pages.add(intro);

        // Page 2: How It Works
        InfoPage mechanics = new InfoPage();
        mechanics.title = "research.strangematter.stasis_projector.mechanics.title";
        mechanics.content = "research.strangematter.stasis_projector.mechanics.content";
        mechanics.hasRecipes = false;
        mechanics.hasScreenshots = false;
        mechanics.recipeName = null;
        mechanics.screenshotPath = null;
        pages.add(mechanics);
        
        // Page 3: Applications
        InfoPage applications = new InfoPage();
        applications.title = "research.strangematter.stasis_projector.applications.title";
        applications.content = "research.strangematter.stasis_projector.applications.content";
        applications.hasRecipes = false;
        applications.hasScreenshots = false;
        applications.recipeName = null;
        applications.screenshotPath = null;
        pages.add(applications);
    }
    
    private void initializeRiftStabilizerPages() {
        // Page 1: Introduction with Recipe
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.rift_stabilizer.intro.title";
        intro.content = "research.strangematter.rift_stabilizer.intro.content";
        intro.hasRecipes = true;
        intro.hasScreenshots = false;
        intro.recipeName = "rift_stabilizer";
        intro.isRealityForgeRecipe = true;
        pages.add(intro);

        // Page 2: Power Generation Theory
        InfoPage mechanics = new InfoPage();
        mechanics.title = "research.strangematter.rift_stabilizer.mechanics.title";
        mechanics.content = "research.strangematter.rift_stabilizer.mechanics.content";
        mechanics.hasRecipes = false;
        mechanics.hasScreenshots = false;
        mechanics.recipeName = null;
        mechanics.screenshotPath = null;
        pages.add(mechanics);
        
        // Page 3: Installation & Operation
        InfoPage usage = new InfoPage();
        usage.title = "research.strangematter.rift_stabilizer.usage.title";
        usage.content = "research.strangematter.rift_stabilizer.usage.content";
        usage.hasRecipes = false;
        usage.hasScreenshots = false;
        usage.recipeName = null;
        usage.screenshotPath = null;
        pages.add(usage);
    }
    
    private void initializeGravityAnomaliesPages() {
        // Page 1: Gravity Anomaly Description with Screenshot
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.gravity_anomalies.title";
        intro.content = "research.strangematter.gravity_anomalies.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = true;
        intro.screenshotPath = "strangematter:textures/ui/gravity_anomaly.png";
        pages.add(intro);
    }
    
    private void initializeTemporalAnomaliesPages() {
        // Page 1: Temporal Anomaly Description with Screenshot
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.temporal_anomalies.title";
        intro.content = "research.strangematter.temporal_anomalies.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = true;
        intro.screenshotPath = "strangematter:textures/ui/temporal_bloom.png";
        pages.add(intro);
    }
    
    private void initializeSpatialAnomaliesPages() {
        // Page 1: Spatial Anomaly Description with Screenshot
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.spatial_anomalies.title";
        intro.content = "research.strangematter.spatial_anomalies.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = true;
        intro.screenshotPath = "strangematter:textures/ui/warp_gate_anomaly.png";
        pages.add(intro);
    }
    
    private void initializeEnergyAnomaliesPages() {
        // Page 1: Energy Anomaly Description with Screenshot
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.energy_anomalies.title";
        intro.content = "research.strangematter.energy_anomalies.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = true;
        intro.screenshotPath = "strangematter:textures/ui/energetic_rift.png";
        pages.add(intro);
    }
    
    private void initializeShadowAnomaliesPages() {
        // Page 1: Shadow Anomaly Description with Screenshot
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.shadow_anomalies.title";
        intro.content = "research.strangematter.shadow_anomalies.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = true;
        intro.screenshotPath = "strangematter:textures/ui/echoing_shadow.png";
        pages.add(intro);
    }
    
    private void initializeCognitiveAnomaliesPages() {
        // Page 1: Cognitive Anomaly Description with Screenshot
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.cognitive_anomalies.title";
        intro.content = "research.strangematter.cognitive_anomalies.content";
        intro.hasRecipes = false;
        intro.hasScreenshots = true;
        intro.screenshotPath = "strangematter:textures/ui/thoughtwell.png";
        pages.add(intro);
    }
    
    private void initializeLevitationPadPages() {
        // Page 1: Introduction with Recipe (Half Page)
        InfoPage intro = new InfoPage();
        intro.title = "research.strangematter.levitation_pad.intro.title";
        intro.content = "research.strangematter.levitation_pad.intro.content";
        intro.hasRecipes = true;
        intro.hasScreenshots = false;
        intro.recipeName = "levitation_pad";
        intro.isRealityForgeRecipe = true;
        pages.add(intro);
        
        // Page 2: Mechanics (Full Page)
        InfoPage mechanics = new InfoPage();
        mechanics.title = "research.strangematter.levitation_pad.mechanics.title";
        mechanics.content = "research.strangematter.levitation_pad.mechanics.content";
        mechanics.hasRecipes = false;
        mechanics.hasScreenshots = false;
        pages.add(mechanics);
    }
    
    private void initializeDefaultPages() {
        // Default pages for other research nodes
        InfoPage basicInfo = new InfoPage();
        basicInfo.title = "Overview";
        basicInfo.content = node.getDisplayDescription().getString();
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
        .bounds(guiX - 50, guiY + GUI_HEIGHT - 30, 40, 20)
        .build();
        
        nextButton = Button.builder(
            Component.translatable("gui.strangematter.info_page.next"),
            (button) -> nextPage()
        )
        .bounds(guiX + GUI_WIDTH + 10, guiY + GUI_HEIGHT - 30, 40, 20)
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
        
        // Clear tooltip slots from previous frame
        tooltipSlots.clear();
        
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
        
        // Check for tooltips on all rendered item slots
        for (TooltipSlot slot : tooltipSlots) {
            // Calculate absolute position of the slot in screen coordinates
            int absoluteSlotX = slot.x;
            int absoluteSlotY = slot.y;
            
            // Check if mouse is over this tooltip slot
            if (mouseX >= absoluteSlotX && mouseX < absoluteSlotX + slot.size &&
                mouseY >= absoluteSlotY && mouseY < absoluteSlotY + slot.size) {
                // Render tooltip at mouse position
                guiGraphics.renderTooltip(this.font, slot.stack, mouseX, mouseY);
                break; // Only show one tooltip at a time
            }
        }
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void renderPage(GuiGraphics guiGraphics, InfoPage page) {
        // Render title
        Component titleComponent = Component.translatable(page.title);
        guiGraphics.drawCenteredString(this.font, titleComponent, guiX + GUI_WIDTH / 2, guiY + 20, 0xFFFFFF);
        
        if (page.hasRecipes || page.hasScreenshots) {
            // Two-column layout: text on left, recipe/screenshot on right
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
        
        // Render right column content (recipe or screenshot)
        if (page.hasRecipes) {
            renderRecipes(guiGraphics, rightColumnX, contentY, rightColumnWidth);
        } else if (page.hasScreenshots) {
            renderScreenshots(guiGraphics, rightColumnX, contentY, rightColumnWidth);
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
                if (page.isRealityForgeRecipe) {
                    // Render reality forge recipe with shards in circle
                    renderRealityForgeRecipe(guiGraphics, x, y, page);
                } else {
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
    }
    
    private void renderRealityForgeRecipe(GuiGraphics guiGraphics, int x, int y, InfoPage page) {
        // Draw recipe title
        guiGraphics.drawString(this.font, Component.translatable("gui.strangematter.info_page.reality_forge_recipe"), x, y, 0xFFFFFF);
        y += 15;
        
        if (page.recipeName == null) return;
        
        // Null safety check
        if (this.minecraft == null || this.minecraft.level == null) {
            guiGraphics.drawString(this.font, "Loading...", x, y, 0xFF666666);
            return;
        }
        
        // Get the recipe from the registry
        // If recipeName already has a namespace (contains ':'), use it as-is
        // Otherwise, prepend "strangematter:"
        String fullRecipeName = page.recipeName.contains(":") ? page.recipeName : "strangematter:" + page.recipeName;
        ResourceLocation resultItemId = ResourceLocation.parse(fullRecipeName);
        com.hexvane.strangematter.recipe.RealityForgeRecipe recipe = 
            com.hexvane.strangematter.recipe.RealityForgeRecipeRegistry.findRecipeByResult(resultItemId, this.minecraft.level);
        
        if (recipe == null) {
            guiGraphics.drawString(this.font, "Recipe not found: " + page.recipeName, x, y, 0xFF666666);
            return;
        }
        
        // Draw the 3x3 crafting grid with tooltips
        renderCraftingGridWithTooltips(guiGraphics, x, y, recipe);
        
        // Draw the result item on the right side with tooltip
        int resultX = x + 100;
        int resultY = y + 20;
        renderResultSlotWithTooltip(guiGraphics, resultX, resultY, recipe.getResultItem(null));
        
        // Draw shards below the crafting grid with tooltips
        renderShardRequirementsWithTooltips(guiGraphics, x, y + 80, recipe);
    }
    
    private void renderCraftingGridWithTooltips(GuiGraphics guiGraphics, int x, int y, com.hexvane.strangematter.recipe.RealityForgeRecipe recipe) {
        int slotSize = 18;
        int gridStartX = x;
        int gridStartY = y;
        
        // Draw 3x3 grid background
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = gridStartX + col * slotSize;
                int slotY = gridStartY + row * slotSize;
                
                // Draw slot background
                guiGraphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0xFF404040);
                guiGraphics.renderOutline(slotX, slotY, slotSize, slotSize, 0xFF808080);
                
                // Get ingredient for this slot
                int slotIndex = row * 3 + col;
                if (slotIndex < recipe.getIngredients().size()) {
                    net.minecraft.world.item.crafting.Ingredient ingredient = recipe.getIngredients().get(slotIndex);
                    if (ingredient != net.minecraft.world.item.crafting.Ingredient.EMPTY) {
                        // Get matching items from the ingredient
                        net.minecraft.world.item.ItemStack[] matchingStacks = ingredient.getItems();
                        if (matchingStacks.length > 0) {
                            // For tagged ingredients (like anomaly_shards), cycle through different options
                            if (matchingStacks.length > 1) {
                                // Calculate which item to show based on current time for cycling effect
                                long currentTime = System.currentTimeMillis();
                                int cycleIndex = (int) ((currentTime / 1000) % matchingStacks.length); // Change every second
                                net.minecraft.world.item.ItemStack displayStack = matchingStacks[cycleIndex];
                                guiGraphics.renderItem(displayStack, slotX + 1, slotY + 1);
                                
                                // Render tooltip if mouse is over this slot
                                renderItemTooltip(guiGraphics, slotX, slotY, slotSize, displayStack);
                            } else {
                                // Single item ingredient
                                net.minecraft.world.item.ItemStack displayStack = matchingStacks[0];
                                guiGraphics.renderItem(displayStack, slotX + 1, slotY + 1);
                                
                                // Render tooltip if mouse is over this slot
                                renderItemTooltip(guiGraphics, slotX, slotY, slotSize, displayStack);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void renderResultSlotWithTooltip(GuiGraphics guiGraphics, int x, int y, net.minecraft.world.item.ItemStack result) {
        int slotSize = 18;
        
        // Draw result slot background
        guiGraphics.fill(x, y, x + slotSize, y + slotSize, 0xFF404040);
        guiGraphics.renderOutline(x, y, slotSize, slotSize, 0xFF808080);
        
        // Draw result item
        if (!result.isEmpty()) {
            guiGraphics.renderItem(result, x + 1, y + 1);
            
            // Render tooltip if mouse is over this slot
            renderItemTooltip(guiGraphics, x, y, slotSize, result);
        }
    }
    
    private void renderShardRequirementsWithTooltips(GuiGraphics guiGraphics, int x, int y, com.hexvane.strangematter.recipe.RealityForgeRecipe recipe) {
        java.util.Map<String, Integer> shardRequirements = recipe.getShardRequirements();
        
        if (shardRequirements.isEmpty()) return;
        
        guiGraphics.drawString(this.font, Component.translatable("gui.strangematter.info_page.required_shards"), x, y, 0xFFFFAA00);
        y += 15;
        
        // Convert shard type names to items
        java.util.Map<String, net.minecraft.world.item.Item> shardTypeToItem = new java.util.HashMap<>();
        shardTypeToItem.put("gravitic", com.hexvane.strangematter.StrangeMatterMod.GRAVITIC_SHARD.get());
        shardTypeToItem.put("gravity", com.hexvane.strangematter.StrangeMatterMod.GRAVITIC_SHARD.get()); // Alias
        shardTypeToItem.put("energetic", com.hexvane.strangematter.StrangeMatterMod.ENERGETIC_SHARD.get());
        shardTypeToItem.put("spatial", com.hexvane.strangematter.StrangeMatterMod.SPATIAL_SHARD.get());
        shardTypeToItem.put("chrono", com.hexvane.strangematter.StrangeMatterMod.CHRONO_SHARD.get());
        shardTypeToItem.put("temporal", com.hexvane.strangematter.StrangeMatterMod.CHRONO_SHARD.get()); // Alias
        shardTypeToItem.put("shade", com.hexvane.strangematter.StrangeMatterMod.SHADE_SHARD.get());
        shardTypeToItem.put("shadow", com.hexvane.strangematter.StrangeMatterMod.SHADE_SHARD.get()); // Alias
        shardTypeToItem.put("insight", com.hexvane.strangematter.StrangeMatterMod.INSIGHT_SHARD.get());
        shardTypeToItem.put("cognitive", com.hexvane.strangematter.StrangeMatterMod.INSIGHT_SHARD.get()); // Alias
        
        // Draw shards in a horizontal line
        int shardX = x;
        int shardY = y;
        int shardSpacing = 20;
        int shardSize = 16;
        
        for (java.util.Map.Entry<String, Integer> entry : shardRequirements.entrySet()) {
            String shardType = entry.getKey();
            int count = entry.getValue();
            net.minecraft.world.item.Item shardItem = shardTypeToItem.get(shardType);
            
            if (shardItem != null) {
                net.minecraft.world.item.ItemStack shardStack = new net.minecraft.world.item.ItemStack(shardItem);
                shardStack.setCount(count);
                
                // Draw shard item
                guiGraphics.renderItem(shardStack, shardX, shardY);
                
                // Draw count text next to the shard
                String countText = count + "x";
                int textX = shardX + shardSize + 2;
                int textY = shardY + (shardSize - this.font.lineHeight) / 2;
                guiGraphics.drawString(this.font, countText, textX, textY, 0xFFFFFF);
                
                // Render tooltip if mouse is over this shard
                renderItemTooltip(guiGraphics, shardX, shardY, shardSize, shardStack);
                
                // Move to next shard position (account for text width)
                shardX += shardSpacing + this.font.width(countText);
            }
        }
    }
    
    private void renderItemTooltip(GuiGraphics guiGraphics, int x, int y, int size, net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) return;
        
        // Add this slot to the tooltip slots list
        this.tooltipSlots.add(new TooltipSlot(x, y, size, stack));
    }
    
    // Helper class to store tooltip slot information
    private static class TooltipSlot {
        final int x, y, size;
        final net.minecraft.world.item.ItemStack stack;
        
        TooltipSlot(int x, int y, int size, net.minecraft.world.item.ItemStack stack) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.stack = stack;
        }
    }
    
    private java.util.List<TooltipSlot> tooltipSlots = new java.util.ArrayList<>();
    
    private void drawRecipeInGrid(GuiGraphics guiGraphics, String recipeName, int gridX, int gridY, int slotSize) {
        // Get the recipe from Minecraft's recipe registry
        // If recipeName already has a namespace (contains ':'), use it as-is
        // Otherwise, prepend "strangematter:"
        String fullRecipeName = recipeName.contains(":") ? recipeName : "strangematter:" + recipeName;
        ResourceLocation recipeId = ResourceLocation.parse(fullRecipeName);
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
                            // For tagged ingredients (like anomaly_shards), cycle through different options
                            if (stacks.length > 1) {
                                // Calculate which item to show based on current time for cycling effect
                                long currentTime = System.currentTimeMillis();
                                int cycleIndex = (int) ((currentTime / 1000) % stacks.length); // Change every second
                                ItemStack displayStack = stacks[cycleIndex];
                                guiGraphics.renderItem(displayStack, slotX + 1, slotY + 1);
                                
                                // Render tooltip if mouse is over this slot
                                renderItemTooltip(guiGraphics, slotX, slotY, slotSize, displayStack);
                            } else {
                                // Single item ingredient
                                ItemStack displayStack = stacks[0];
                                guiGraphics.renderItem(displayStack, slotX + 1, slotY + 1);
                                
                                // Render tooltip if mouse is over this slot
                                renderItemTooltip(guiGraphics, slotX, slotY, slotSize, displayStack);
                            }
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
        
        // Render tooltip if mouse is over this slot
        renderItemTooltip(guiGraphics, resultX, resultY, slotSize, resultStack);
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
        // If recipeName already has a namespace (contains ':'), use it as-is
        // Otherwise, prepend "strangematter:"
        String fullRecipeName = recipeName.contains(":") ? recipeName : "strangematter:" + recipeName;
        ResourceLocation recipeId = ResourceLocation.parse(fullRecipeName);
        Recipe<?> recipe = this.minecraft.level.getRecipeManager().byKey(recipeId).orElse(null);
        
        if (recipe instanceof CraftingRecipe craftingRecipe) {
            List<Ingredient> ingredients = craftingRecipe.getIngredients();
            for (Ingredient ingredient : ingredients) {
                if (!ingredient.isEmpty()) {
                    ItemStack[] stacks = ingredient.getItems();
                    if (stacks.length > 0) {
                        // Check if this is a tagged ingredient (multiple items = likely a tag)
                        if (stacks.length > 1) {
                            // Check if this looks like the anomaly_shards tag
                            boolean isAnomalyShards = false;
                            for (ItemStack stack : stacks) {
                                String itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                                if (itemId.contains("shard")) {
                                    isAnomalyShards = true;
                                    break;
                                }
                            }
                            
                            if (isAnomalyShards) {
                                // Add a descriptive name for the anomaly shards tag
                                if (!uniqueIngredients.contains("Any Anomaly Shard")) {
                                    uniqueIngredients.add("Any Anomaly Shard");
                                }
                            } else {
                                // For other tags, show the first item name with indication it's a tag
                                ItemStack firstStack = stacks[0];
                                String localizedName = firstStack.getHoverName().getString();
                                if (!uniqueIngredients.contains(localizedName + " (or similar)")) {
                                    uniqueIngredients.add(localizedName + " (or similar)");
                                }
                            }
                        } else {
                            // Single item ingredient - get its localized name
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
                // Draw screenshot
                int screenshotWidth = 120;
                int screenshotHeight = 80;
                
                // Draw screenshot background
                guiGraphics.fill(x, y, x + screenshotWidth, y + screenshotHeight, 0xFF202020);
                
                // Render the actual screenshot texture
                try {
                    ResourceLocation screenshotTexture = ResourceLocation.parse(page.screenshotPath);
                    RenderSystem.setShaderTexture(0, screenshotTexture);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    guiGraphics.blit(screenshotTexture, x, y, 0, 0, screenshotWidth, screenshotHeight, screenshotWidth, screenshotHeight);
                } catch (Exception e) {
                    // Fallback to placeholder if screenshot fails to load
                    guiGraphics.drawCenteredString(this.font, "Screenshot", x + screenshotWidth / 2, y + screenshotHeight / 2 - 5, 0x888888);
                    guiGraphics.drawCenteredString(this.font, "Not Found", x + screenshotWidth / 2, y + screenshotHeight / 2 + 5, 0x888888);
                }
                
                // Draw outline on top of the screenshot
                guiGraphics.renderOutline(x, y, screenshotWidth, screenshotHeight, 0xFF555555);
                
                // Draw caption
                y += screenshotHeight + 10;
                guiGraphics.drawString(this.font, getScreenshotCaption(page.screenshotPath), x, y, 0xAAAAAA);
            }
        }
    }
    
    private String getScreenshotCaption(String screenshotPath) {
        if (screenshotPath == null) {
            return Component.translatable("gui.strangematter.info_page.screenshot.default").getString();
        }
        
        // Extract filename from path and create translation key
        String filename = screenshotPath.substring(screenshotPath.lastIndexOf('/') + 1);
        String filenameWithoutExt = filename.replace(".png", "");
        
        // Create translation key using the filename
        String translationKey = "gui.strangematter.info_page.screenshot." + filenameWithoutExt;
        Component caption = Component.translatable(translationKey);
        
        // If translation doesn't exist, return the key (fallback)
        if (caption.getString().equals(translationKey)) {
            return Component.translatable("gui.strangematter.info_page.screenshot.fallback", filenameWithoutExt.replace("_", " ")).getString();
        }
        
        return caption.getString();
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
        boolean isRealityForgeRecipe = false; // Whether this is a reality forge recipe
        java.util.List<net.minecraft.world.item.Item> realityForgeShards = java.util.List.of(); // Shards needed for reality forge recipe
    }
}
