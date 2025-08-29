package com.hexvane.strangematter;

import com.hexvane.strangematter.entity.GravityAnomalyEntity;
import com.hexvane.strangematter.client.GravityAnomalyRenderer;
import com.hexvane.strangematter.command.AnomalyCommand;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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

    // Creates a new Block with the id "strangematter:anomaly_core", combining the namespace and path
    public static final RegistryObject<Block> ANOMALY_CORE = BLOCKS.register("anomaly_core", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)));
    // Creates a new BlockItem with the id "strangematter:anomaly_core", combining the namespace and path
    public static final RegistryObject<Item> ANOMALY_CORE_ITEM = ITEMS.register("anomaly_core", () -> new BlockItem(ANOMALY_CORE.get(), new Item.Properties()));

    // Creates a new research item with the id "strangematter:field_scanner"
    public static final RegistryObject<Item> FIELD_SCANNER = ITEMS.register("field_scanner", () -> new Item(new Item.Properties()));

    // Gravity Anomaly Entity
    public static final RegistryObject<EntityType<GravityAnomalyEntity>> GRAVITY_ANOMALY = ENTITY_TYPES.register("gravity_anomaly", 
        () -> EntityType.Builder.<GravityAnomalyEntity>of(GravityAnomalyEntity::new, MobCategory.MISC)
            .sized(1.0f, 1.0f) // Size of the entity
            .build("gravity_anomaly"));

    // Sound Events
    public static final RegistryObject<SoundEvent> GRAVITY_ANOMALY_LOOP = SOUND_EVENTS.register("gravity_anomaly_loop", 
        () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(MODID, "gravity_anomaly_loop")));



    // Creates a creative tab with the id "strangematter:strange_matter_tab" for the anomaly items
    public static final RegistryObject<CreativeModeTab> STRANGE_MATTER_TAB = CREATIVE_MODE_TABS.register("strange_matter_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ANOMALY_CORE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ANOMALY_CORE_ITEM.get());
                output.accept(FIELD_SCANNER.get());
            }).build());

    public StrangeMatterMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

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
        
        // Set up sound events for entities
        event.enqueueWork(() -> {
            GravityAnomalyEntity.setGravityAnomalyLoopSound(GRAVITY_ANOMALY_LOOP.get());
        });
    }

    // Register commands
    @SubscribeEvent
    public void registerCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        AnomalyCommand.register(event.getDispatcher());
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
