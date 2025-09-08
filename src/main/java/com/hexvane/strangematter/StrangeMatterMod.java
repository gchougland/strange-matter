package com.hexvane.strangematter;

import com.hexvane.strangematter.entity.GravityAnomalyEntity;
import com.hexvane.strangematter.client.GravityAnomalyRenderer;
import com.hexvane.strangematter.client.sound.CustomSoundManager;
import com.hexvane.strangematter.command.AnomalyCommand;
import com.hexvane.strangematter.block.AnomalousGrassBlock;
import com.hexvane.strangematter.item.AnomalousGrassItem;
import com.hexvane.strangematter.item.AnomalyResonatorItem;
import com.hexvane.strangematter.worldgen.GravityAnomalyConfiguredFeature;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
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
    // Create a Deferred Register to hold SoundEvents
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    // Create a Deferred Register to hold Attributes
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MODID);
    // Create a Deferred Register to hold Features
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MODID);
    // Create a Deferred Register to hold PlacementModifierTypes
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MODID);

    // Creates a new Block with the id "strangematter:anomaly_core", combining the namespace and path
    public static final RegistryObject<Block> ANOMALY_CORE = BLOCKS.register("anomaly_core", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)));
    // Creates a new BlockItem with the id "strangematter:anomaly_core", combining the namespace and path
    public static final RegistryObject<Item> ANOMALY_CORE_ITEM = ITEMS.register("anomaly_core", () -> new BlockItem(ANOMALY_CORE.get(), new Item.Properties()));

    // Creates a new research item with the id "strangematter:field_scanner"
    public static final RegistryObject<Item> FIELD_SCANNER = ITEMS.register("field_scanner", () -> new Item(new Item.Properties()));
    
    // Anomaly Resonator - compass for finding anomalies
    public static final RegistryObject<Item> ANOMALY_RESONATOR = ITEMS.register("anomaly_resonator", AnomalyResonatorItem::new);

    // Anomalous Grass Block
    public static final RegistryObject<Block> ANOMALOUS_GRASS_BLOCK = BLOCKS.register("anomalous_grass", AnomalousGrassBlock::new);
    public static final RegistryObject<Item> ANOMALOUS_GRASS_ITEM = ITEMS.register("anomalous_grass", () -> new AnomalousGrassItem((AnomalousGrassBlock) ANOMALOUS_GRASS_BLOCK.get()));

    // Custom gravity attribute for low gravity effects
    public static final RegistryObject<Attribute> ENTITY_GRAVITY = ATTRIBUTES.register("entity_gravity", 
        () -> new RangedAttribute("strangematter.entity_gravity", 0.08D, -1.0D, 1.0D).setSyncable(true));

    // Gravity Anomaly Entity
    public static final RegistryObject<EntityType<GravityAnomalyEntity>> GRAVITY_ANOMALY = ENTITY_TYPES.register("gravity_anomaly", 
        () -> EntityType.Builder.<GravityAnomalyEntity>of(GravityAnomalyEntity::new, MobCategory.MISC)
            .sized(1.0f, 1.0f) // Size of the entity
            .build("gravity_anomaly"));

    // Sound Events
    public static final RegistryObject<SoundEvent> GRAVITY_ANOMALY_LOOP = SOUND_EVENTS.register("gravity_anomaly_loop", 
        () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(MODID, "gravity_anomaly_loop")));

    // World Generation Features
    public static final RegistryObject<Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration>> GRAVITY_ANOMALY_FEATURE = FEATURES.register("gravity_anomaly", 
        () -> new GravityAnomalyConfiguredFeature());



    // Creates a creative tab with the id "strangematter:strange_matter_tab" for the anomaly items
    public static final RegistryObject<CreativeModeTab> STRANGE_MATTER_TAB = CREATIVE_MODE_TABS.register("strange_matter_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ANOMALY_CORE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ANOMALY_CORE_ITEM.get());
                output.accept(FIELD_SCANNER.get());
                output.accept(ANOMALY_RESONATOR.get());
                output.accept(ANOMALOUS_GRASS_ITEM.get());
            }).build());

    public StrangeMatterMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        
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
        // Register the Deferred Register to the mod event bus so sound events get registered
        SOUND_EVENTS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so attributes get registered
        ATTRIBUTES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so features get registered
        FEATURES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so placement modifiers get registered
        PLACEMENT_MODIFIERS.register(modEventBus);

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
        // Register compass angle property for anomaly resonator
        event.enqueueWork(() -> {
            net.minecraft.client.renderer.item.ItemProperties.register(
                ANOMALY_RESONATOR.get(),
                new net.minecraft.resources.ResourceLocation("angle"),
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

    // Add the anomaly core block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(ANOMALY_CORE_ITEM);
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
            });
        }
    }
}
