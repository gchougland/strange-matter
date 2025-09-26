package com.hexvane.strangematter;

import com.hexvane.strangematter.entity.GravityAnomalyEntity;
import com.hexvane.strangematter.entity.EchoingShadowEntity;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;
import com.hexvane.strangematter.entity.ThoughtwellEntity;
import com.hexvane.strangematter.client.GravityAnomalyRenderer;
import com.hexvane.strangematter.client.TemporalBloomRenderer;
import com.hexvane.strangematter.client.ThoughtwellRenderer;
import com.hexvane.strangematter.client.EnergeticRiftRenderer;
import com.hexvane.strangematter.client.EchoingShadowRenderer;
import com.hexvane.strangematter.client.WarpGateAnomalyRenderer;
import com.hexvane.strangematter.client.CrystalizedEctoplasmRenderer;
import com.hexvane.strangematter.client.ResearchMachineRenderer;
import com.hexvane.strangematter.block.CrystalizedEctoplasmBlockEntity;
import com.hexvane.strangematter.client.sound.CustomSoundManager;
import com.hexvane.strangematter.command.AnomalyCommand;
import com.hexvane.strangematter.command.ResearchCommand;
import com.hexvane.strangematter.sound.StrangeMatterSounds;
import com.hexvane.strangematter.block.AnomalousGrassBlock;
import com.hexvane.strangematter.block.CrystalizedEctoplasmBlock;
import com.hexvane.strangematter.block.ResoniteOreBlock;
import com.hexvane.strangematter.block.ResearchMachineBlock;
import com.hexvane.strangematter.block.ResearchMachineBlockEntity;
import com.hexvane.strangematter.item.AnomalousGrassItem;
import com.hexvane.strangematter.item.AnomalyResonatorItem;
import com.hexvane.strangematter.item.EctoplasmItem;
import com.hexvane.strangematter.item.FieldScannerItem;
import com.hexvane.strangematter.item.RawResoniteItem;
import com.hexvane.strangematter.item.ResoniteIngotItem;
import com.hexvane.strangematter.item.ResearchTabletItem;
import com.hexvane.strangematter.worldgen.GravityAnomalyConfiguredFeature;
import com.hexvane.strangematter.worldgen.EchoingShadowConfiguredFeature;
import com.hexvane.strangematter.worldgen.ThoughtwellConfiguredFeature;
import com.hexvane.strangematter.worldgen.CrystalizedEctoplasmConfiguredFeature;
import com.hexvane.strangematter.worldgen.WarpGateAnomalyStructure;
import com.hexvane.strangematter.worldgen.WarpGateAnomalyFeature;
import com.hexvane.strangematter.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StrangeMatterMod.MODID)
public class StrangeMatterMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "strangematter";
    
    // Attribute modifier ID for low gravity effect
    public static final java.util.UUID LOW_GRAVITY_MODIFIER_ID = java.util.UUID.fromString("12345678-1234-1234-1234-123456789abc");
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "strangematter" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "strangematter" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "strangematter" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    // Create a Deferred Register to hold EntityTypes
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    // Create a Deferred Register to hold BlockEntityTypes
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    // Create a Deferred Register to hold SoundEvents
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    // Create a Deferred Register to hold Attributes
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MODID);
    // Create a Deferred Register to hold Features
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MODID);
    // Create a Deferred Register to hold PlacementModifierTypes
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MODID);
    // Create a Deferred Register to hold StructureTypes
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, MODID);


    // Creates a new research item with the id "strangematter:field_scanner"
    public static final RegistryObject<Item> FIELD_SCANNER = ITEMS.register("field_scanner", FieldScannerItem::new);
    
    // Anomaly Resonator - compass for finding anomalies
    public static final RegistryObject<Item> ANOMALY_RESONATOR = ITEMS.register("anomaly_resonator", AnomalyResonatorItem::new);
    
    // Research Notes - basic research item
    public static final RegistryObject<Item> RESEARCH_NOTES = ITEMS.register("research_notes", () -> new com.hexvane.strangematter.item.ResearchNoteItem(new Item.Properties()));

    // Anomalous Grass Block
    public static final RegistryObject<Block> ANOMALOUS_GRASS_BLOCK = BLOCKS.register("anomalous_grass", AnomalousGrassBlock::new);
    public static final RegistryObject<Item> ANOMALOUS_GRASS_ITEM = ITEMS.register("anomalous_grass", () -> new AnomalousGrassItem((AnomalousGrassBlock) ANOMALOUS_GRASS_BLOCK.get()));

    // Crystalized Ectoplasm Block
    public static final RegistryObject<Block> CRYSTALIZED_ECTOPLASM_BLOCK = BLOCKS.register("crystalized_ectoplasm", CrystalizedEctoplasmBlock::new);
    public static final RegistryObject<Item> CRYSTALIZED_ECTOPLASM_ITEM = ITEMS.register("crystalized_ectoplasm", () -> new BlockItem(CRYSTALIZED_ECTOPLASM_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<CrystalizedEctoplasmBlockEntity>> CRYSTALIZED_ECTOPLASM_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("crystalized_ectoplasm", 
        () -> BlockEntityType.Builder.of((pos, state) -> new CrystalizedEctoplasmBlockEntity(pos, state), CRYSTALIZED_ECTOPLASM_BLOCK.get()).build(null));

    // Ectoplasm Item
    public static final RegistryObject<Item> ECTOPLASM = ITEMS.register("ectoplasm", EctoplasmItem::new);

    // Resonite Ore Block
    public static final RegistryObject<Block> RESONITE_ORE_BLOCK = BLOCKS.register("resonite_ore", ResoniteOreBlock::new);
    
    // Research Machine - advanced research device
    public static final RegistryObject<Block> RESEARCH_MACHINE_BLOCK = BLOCKS.register("research_machine", ResearchMachineBlock::new);
    public static final RegistryObject<Item> RESEARCH_MACHINE_ITEM = ITEMS.register("research_machine", () -> new BlockItem(RESEARCH_MACHINE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<ResearchMachineBlockEntity>> RESEARCH_MACHINE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("research_machine", 
        () -> BlockEntityType.Builder.of((pos, state) -> new ResearchMachineBlockEntity(pos, state), RESEARCH_MACHINE_BLOCK.get()).build(null));
    public static final RegistryObject<Item> RESONITE_ORE_ITEM = ITEMS.register("resonite_ore", () -> new BlockItem(RESONITE_ORE_BLOCK.get(), new Item.Properties()));

    // Raw Resonite Item
    public static final RegistryObject<Item> RAW_RESONITE = ITEMS.register("raw_resonite", RawResoniteItem::new);

    // Resonite Ingot Item
    public static final RegistryObject<Item> RESONITE_INGOT = ITEMS.register("resonite_ingot", ResoniteIngotItem::new);

    // Research Tablet Item
    public static final RegistryObject<Item> RESEARCH_TABLET = ITEMS.register("research_tablet", ResearchTabletItem::new);

    // Custom gravity attribute for low gravity effects
    public static final RegistryObject<Attribute> ENTITY_GRAVITY = ATTRIBUTES.register("entity_gravity", 
        () -> new RangedAttribute("strangematter.entity_gravity", 0.08D, -1.0D, 1.0D).setSyncable(true));

    // Gravity Anomaly Entity
    public static final RegistryObject<EntityType<GravityAnomalyEntity>> GRAVITY_ANOMALY = ENTITY_TYPES.register("gravity_anomaly", 
        () -> EntityType.Builder.<GravityAnomalyEntity>of(GravityAnomalyEntity::new, MobCategory.MISC)
            .sized(1.0f, 1.0f) // Size of the entity
            .build("gravity_anomaly"));
    
    // Energetic Rift Entity
    public static final RegistryObject<EntityType<com.hexvane.strangematter.entity.EnergeticRiftEntity>> ENERGETIC_RIFT = ENTITY_TYPES.register("energetic_rift", 
        () -> EntityType.Builder.<com.hexvane.strangematter.entity.EnergeticRiftEntity>of(
            (entityType, level) -> new com.hexvane.strangematter.entity.EnergeticRiftEntity(entityType, level), MobCategory.MISC)
            .sized(1.0f, 1.0f) // Size of the entity
            .build("energetic_rift"));

    // Echoing Shadow Entity
    public static final RegistryObject<EntityType<EchoingShadowEntity>> ECHOING_SHADOW = ENTITY_TYPES.register("echoing_shadow", 
        () -> EntityType.Builder.<EchoingShadowEntity>of(EchoingShadowEntity::new, MobCategory.MISC)
            .sized(1.0f, 1.0f) // Size of the entity
            .build("echoing_shadow"));

    // Warp Gate Anomaly Entity
    public static final RegistryObject<EntityType<WarpGateAnomalyEntity>> WARP_GATE_ANOMALY_ENTITY = ENTITY_TYPES.register("warp_gate_anomaly", 
        () -> EntityType.Builder.<WarpGateAnomalyEntity>of(WarpGateAnomalyEntity::new, MobCategory.MISC)
            .sized(2.0f, 3.0f) // Larger size for warp gate
            .build("warp_gate_anomaly"));

    // Temporal Bloom Entity
    public static final RegistryObject<EntityType<com.hexvane.strangematter.entity.TemporalBloomEntity>> TEMPORAL_BLOOM = ENTITY_TYPES.register("temporal_bloom", 
        () -> EntityType.Builder.<com.hexvane.strangematter.entity.TemporalBloomEntity>of(
            (entityType, level) -> new com.hexvane.strangematter.entity.TemporalBloomEntity(entityType, level), MobCategory.MISC)
            .sized(1.0f, 1.0f) // Size of the entity
            .build("temporal_bloom"));

    // Thoughtwell Entity
    public static final RegistryObject<EntityType<com.hexvane.strangematter.entity.ThoughtwellEntity>> THOUGHTWELL = ENTITY_TYPES.register("thoughtwell", 
        () -> EntityType.Builder.<com.hexvane.strangematter.entity.ThoughtwellEntity>of(
            (entityType, level) -> new com.hexvane.strangematter.entity.ThoughtwellEntity(entityType, level), MobCategory.MISC)
            .sized(1.0f, 1.0f) // Size of the entity
            .build("thoughtwell"));

    // Sound Events are now registered in StrangeMatterSounds class

    // World Generation Features
    public static final RegistryObject<Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration>> GRAVITY_ANOMALY_FEATURE = FEATURES.register("gravity_anomaly", 
        () -> new GravityAnomalyConfiguredFeature());
    
    public static final RegistryObject<Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration>> ENERGETIC_RIFT_FEATURE = FEATURES.register("energetic_rift", 
        () -> new com.hexvane.strangematter.worldgen.EnergeticRiftConfiguredFeature());
    
    public static final RegistryObject<Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration>> ECHOING_SHADOW_FEATURE = FEATURES.register("echoing_shadow", 
        () -> new EchoingShadowConfiguredFeature());
    
    public static final RegistryObject<Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration>> TEMPORAL_BLOOM_FEATURE = FEATURES.register("temporal_bloom", 
        () -> new com.hexvane.strangematter.worldgen.TemporalBloomConfiguredFeature());
    
    public static final RegistryObject<Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration>> THOUGHTWELL_FEATURE = FEATURES.register("thoughtwell", 
        () -> new com.hexvane.strangematter.worldgen.ThoughtwellConfiguredFeature());
    
    public static final RegistryObject<Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration>> CRYSTALIZED_ECTOPLASM_FEATURE = FEATURES.register("crystalized_ectoplasm", 
        () -> new CrystalizedEctoplasmConfiguredFeature());
    
    public static final RegistryObject<Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration>> WARP_GATE_ANOMALY_FEATURE = FEATURES.register("warp_gate_anomaly_feature", 
        () -> new WarpGateAnomalyFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC));
    

    // Structure Types
    public static final RegistryObject<StructureType<WarpGateAnomalyStructure>> WARP_GATE_ANOMALY_STRUCTURE = STRUCTURE_TYPES.register("warp_gate_anomaly_structure", 
        () -> () -> WarpGateAnomalyStructure.CODEC);



    // Creates a creative tab with the id "strangematter:strange_matter_tab" for the anomaly items
    public static final RegistryObject<CreativeModeTab> STRANGE_MATTER_TAB = CREATIVE_MODE_TABS.register("strange_matter_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup.strangematter.strange_matter_tab"))
            .icon(() -> FIELD_SCANNER.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(FIELD_SCANNER.get());
                output.accept(ANOMALY_RESONATOR.get());
                output.accept(RESEARCH_TABLET.get());
                output.accept(RESEARCH_NOTES.get());
                output.accept(ANOMALOUS_GRASS_ITEM.get());
                output.accept(CRYSTALIZED_ECTOPLASM_ITEM.get());
                output.accept(RESEARCH_MACHINE_ITEM.get());
                output.accept(ECTOPLASM.get());
                output.accept(RESONITE_ORE_ITEM.get());
                output.accept(RAW_RESONITE.get());
                output.accept(RESONITE_INGOT.get());
            }).build());

    public StrangeMatterMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        
        // Register network handler
        NetworkHandler.register();
        
        // Register the clientSetup method for client-side setup
        modEventBus.addListener(this::clientSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so entity types get registered
        ENTITY_TYPES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so block entity types get registered
        BLOCK_ENTITY_TYPES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so sound events get registered
        SOUND_EVENTS.register(modEventBus);
        // Register StrangeMatterSounds
        StrangeMatterSounds.SOUND_EVENTS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so attributes get registered
        ATTRIBUTES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so features get registered
        FEATURES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so placement modifiers get registered
        PLACEMENT_MODIFIERS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so structure types get registered
        STRUCTURE_TYPES.register(modEventBus); // Register structure types

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("Strange Matter mod initialized - reality anomalies detected!");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
        
        // Initialize custom sound manager
        event.enqueueWork(() -> {
            CustomSoundManager.getInstance().initialize();
        });
    }
    
    private void clientSetup(final FMLClientSetupEvent event)
    {
        // Initialize custom sound manager on client side
        event.enqueueWork(() -> {
            CustomSoundManager.getInstance().initialize();
        });
        
        // Register research overlay
        event.enqueueWork(() -> {
            // Register the research gain overlay using the event system
            // The overlay will be handled by ResearchOverlayEventHandler
        });
        
        // Register compass angle property for anomaly resonator
        event.enqueueWork(() -> {
            net.minecraft.client.renderer.item.ItemProperties.register(
                ANOMALY_RESONATOR.get(),
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "angle"),
                (stack, level, entity, seed) -> {
                    if (level == null || entity == null) {
                        return 0.0F;
                    }
                    
                    if (entity instanceof net.minecraft.world.entity.player.Player player) {
                        com.hexvane.strangematter.item.AnomalyResonatorItem resonator = 
                            (com.hexvane.strangematter.item.AnomalyResonatorItem) stack.getItem();
                        
                        net.minecraft.core.BlockPos targetPos = resonator.getTargetPosition(stack, level, player);
                        if (targetPos == null) {
                            // If no target, spin the needle
                            return (float)(System.currentTimeMillis() * 0.1) % 1.0F;
                        }
                        
                        // Calculate angle to target relative to player's look direction
                        double d0 = targetPos.getX() - entity.getX();
                        double d1 = targetPos.getZ() - entity.getZ();
                        float targetAngle = (float)(Math.atan2(d1, d0) * (180F / Math.PI)) - 90.0F;
                        
                        // Get player's yaw (look direction)
                        float playerYaw = entity.getYRot();
                        
                        // Calculate relative angle (target angle - player yaw)
                        float relativeAngle = targetAngle - playerYaw;
                        float wrappedAngle = net.minecraft.util.Mth.wrapDegrees(relativeAngle);
                        
                        // Convert to 0.0-1.0 range for model predicates
                        return (wrappedAngle + 180.0F) / 360.0F;
                    }
                    
                    return 0.0F;
                }
            );
        });
    }

    // Register commands
    @SubscribeEvent
    public void registerCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        AnomalyCommand.register(event.getDispatcher());
        ResearchCommand.register(event.getDispatcher());
        
        // Register test command for gravity anomaly feature
        event.getDispatcher().register(Commands.literal("test_gravity_anomaly")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                var source = context.getSource();
                var level = source.getLevel();
                var pos = source.getPosition();

                if (pos != null) {
                    // Test the gravity anomaly feature placement
                    int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, (int)pos.x, (int)pos.z);
                    BlockPos surfacePos = new BlockPos((int)pos.x, surfaceY, (int)pos.z);
                    
                    // Find the actual solid ground below the surface
                    BlockPos groundPos = surfacePos;
                    while (groundPos.getY() > level.getMinBuildHeight() + 10) {
                        var blockState = level.getBlockState(groundPos);
                        if (blockState.isSolid() && !blockState.isAir() && 
                            !blockState.getBlock().getDescriptionId().contains("leaves")) {
                            break; // Found solid ground (grass, dirt, stone, sand, etc. are all fine)
                        }
                        groundPos = groundPos.below();
                    }
                    
                    // Check if we found valid solid ground
                    if (groundPos.getY() <= level.getMinBuildHeight() + 10) {
                        source.sendFailure(Component.literal("No solid ground found below surface at " + surfacePos));
                        return 1;
                    }
                    
                    // Spawn the anomaly a few blocks above the surface
                    int anomalyY = surfaceY + 3;
                    final BlockPos anomalyPos = new BlockPos((int)pos.x, anomalyY, (int)pos.z);
                    final BlockPos finalGroundPos = groundPos;

                    // Place anomalous grass in a patchy circle following terrain contour
                    int radius = 3;
                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            double distance = Math.sqrt(x * x + z * z);
                            
                            // Only place grass within the circle and with some randomness for patchiness
                            if (distance <= radius && level.random.nextFloat() < 0.7f) { // 70% chance for patchiness
                                // Find the surface height at this offset position
                                int offsetSurfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, 
                                    (int)pos.x + x, (int)pos.z + z);
                                BlockPos offsetSurfacePos = new BlockPos((int)pos.x + x, offsetSurfaceY, (int)pos.z + z);
                                
                                // Find solid ground below this surface position
                                BlockPos grassPos = offsetSurfacePos;
                                while (grassPos.getY() > level.getMinBuildHeight() + 10) {
                                    var blockState = level.getBlockState(grassPos);
                                    if (blockState.isSolid() && !blockState.isAir() && 
                                        !blockState.getBlock().getDescriptionId().contains("leaves")) {
                                        break; // Found solid ground at this position
                                    }
                                    grassPos = grassPos.below();
                                }
                                
                                // Only place grass if we found valid solid ground
                                if (grassPos.getY() > level.getMinBuildHeight() + 10) {
                                    var blockState = level.getBlockState(grassPos);
                                    if (blockState.isSolid() && !blockState.isAir() && 
                                        !blockState.getBlock().getDescriptionId().contains("leaves")) {
                                        level.setBlock(grassPos, ANOMALOUS_GRASS_BLOCK.get().defaultBlockState(), 3);
                                    }
                                }
                            }
                        }
                    }

                    // Spawn the gravity anomaly entity above the surface
                    GravityAnomalyEntity anomaly = new GravityAnomalyEntity(GRAVITY_ANOMALY.get(), level);
                    anomaly.moveTo(anomalyPos.getX() + 0.5, anomalyPos.getY(), anomalyPos.getZ() + 0.5, 0.0f, 0.0f);
                    
                    if (level.addFreshEntity(anomaly)) {
                        source.sendSuccess(() -> Component.literal("Test gravity anomaly placed at " + anomalyPos + " (ground at " + finalGroundPos + ")"), true);
                    } else {
                        source.sendFailure(Component.literal("Failed to place gravity anomaly at " + anomalyPos));
                    }
                }

                return 1;
            }));
    }

    // Add items to creative tabs
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
        {
            event.accept(ANOMALOUS_GRASS_ITEM.get());
            event.accept(CRYSTALIZED_ECTOPLASM_ITEM.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
        {
            event.accept(FIELD_SCANNER.get());
            event.accept(ANOMALY_RESONATOR.get());
            event.accept(RESEARCH_NOTES.get());
            event.accept(RESEARCH_TABLET.get());
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("Strange Matter anomalies are spreading across the server...");
    }
    
    @SubscribeEvent
    public void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event)
    {
        // Clean up custom sound manager
        CustomSoundManager.getInstance().cleanup();
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("Strange Matter client initialized - prepare for reality distortion!");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            
            // Register entity renderers
            event.enqueueWork(() -> {
                net.minecraft.client.renderer.entity.EntityRenderers.register(GRAVITY_ANOMALY.get(), GravityAnomalyRenderer::new);
                net.minecraft.client.renderer.entity.EntityRenderers.register(ENERGETIC_RIFT.get(), EnergeticRiftRenderer::new);
                net.minecraft.client.renderer.entity.EntityRenderers.register(ECHOING_SHADOW.get(), EchoingShadowRenderer::new);
                net.minecraft.client.renderer.entity.EntityRenderers.register(WARP_GATE_ANOMALY_ENTITY.get(), WarpGateAnomalyRenderer::new);
                net.minecraft.client.renderer.entity.EntityRenderers.register(TEMPORAL_BLOOM.get(), TemporalBloomRenderer::new);
                net.minecraft.client.renderer.entity.EntityRenderers.register(THOUGHTWELL.get(), ThoughtwellRenderer::new);
                
                // Register block entity renderer for crystalized ectoplasm
                net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(CRYSTALIZED_ECTOPLASM_BLOCK_ENTITY.get(), CrystalizedEctoplasmRenderer::new);
                net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(RESEARCH_MACHINE_BLOCK_ENTITY.get(), ResearchMachineRenderer::new);
                
            });
        }
        
        
    }
}
