package com.hexvane.strangematter.client;

import com.hexvane.strangematter.StrangeMatterMod;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

/**
 * Client-only mod events - separated from main mod class to prevent client class loading on server
 */
@Mod.EventBusSubscriber(modid = StrangeMatterMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        // Some client setup code
        LOGGER.info("Strange Matter client initialized - prepare for reality distortion!");
        LOGGER.info("MINECRAFT NAME >> {}", net.minecraft.client.Minecraft.getInstance().getUser().getName());
        
        // Register entity renderers
        event.enqueueWork(() -> {
            net.minecraft.client.renderer.entity.EntityRenderers.register(StrangeMatterMod.GRAVITY_ANOMALY.get(), GravityAnomalyRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(StrangeMatterMod.ENERGETIC_RIFT.get(), EnergeticRiftRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(StrangeMatterMod.ECHOING_SHADOW.get(), EchoingShadowRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(StrangeMatterMod.WARP_GATE_ANOMALY_ENTITY.get(), WarpGateAnomalyRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(StrangeMatterMod.TEMPORAL_BLOOM.get(), TemporalBloomRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(StrangeMatterMod.THOUGHTWELL.get(), ThoughtwellRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(StrangeMatterMod.WARP_PROJECTILE_ENTITY.get(), WarpProjectileRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(StrangeMatterMod.CHRONO_BLISTER_PROJECTILE_ENTITY.get(), ChronoBlisterProjectileRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(StrangeMatterMod.MINI_WARP_GATE_ENTITY.get(), MiniWarpGateRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(StrangeMatterMod.THROWABLE_CONTAINMENT_CAPSULE.get(), com.hexvane.strangematter.client.renderer.ThrowableContainmentCapsuleRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(StrangeMatterMod.HOVERBOARD_ENTITY.get(), HoverboardRenderer::new);
            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(StrangeMatterMod.RESEARCH_MACHINE_BLOCK_ENTITY.get(), ResearchMachineRenderer::new);
            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(StrangeMatterMod.STASIS_PROJECTOR_BLOCK_ENTITY.get(), StasisProjectorRenderer::new);
            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(StrangeMatterMod.RIFT_STABILIZER_BLOCK_ENTITY.get(), RiftStabilizerRenderer::new);
            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(StrangeMatterMod.RESONANT_CONDUIT_BLOCK_ENTITY.get(), ResonantConduitRenderer::new);
            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(StrangeMatterMod.LEVITATION_PAD_BLOCK_ENTITY.get(), com.hexvane.strangematter.client.renderer.LevitationPadRenderer::new);
            
            // Set render layers for blocks with transparency
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.RESONITE_TRAPDOOR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.RESONITE_DOOR_BLOCK.get(), net.minecraft.client.renderer.RenderType.cutout());
            
            // Set render layers for shard crystal blocks (semi-transparent)
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.SHADE_SHARD_CRYSTAL.get(), net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.GRAVITIC_SHARD_CRYSTAL.get(), net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.ENERGETIC_SHARD_CRYSTAL.get(), net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.INSIGHT_SHARD_CRYSTAL.get(), net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.CHRONO_SHARD_CRYSTAL.get(), net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.SPATIAL_SHARD_CRYSTAL.get(), net.minecraft.client.renderer.RenderType.translucent());
            
            // Set render layer for time dilation block (semi-transparent amber)
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.TIME_DILATION_BLOCK.get(), net.minecraft.client.renderer.RenderType.translucent());
            
            // Set render layers for shard lantern blocks (cutout transparency)
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.SHADE_SHARD_LANTERN.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.GRAVITIC_SHARD_LANTERN.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.ENERGETIC_SHARD_LANTERN.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.INSIGHT_SHARD_LANTERN.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.CHRONO_SHARD_LANTERN.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(StrangeMatterMod.SPATIAL_SHARD_LANTERN.get(), net.minecraft.client.renderer.RenderType.cutout());
            
            // Register Echo Vacuum client handler for proper first/third person rendering
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new EchoVacuumClientHandler());
        });
        
        // Initialize custom sound manager
        event.enqueueWork(() -> {
            com.hexvane.strangematter.client.sound.CustomSoundManager.getInstance().initialize();
        });
        
        // Register compass angle property for anomaly resonator
        event.enqueueWork(() -> {
            net.minecraft.client.renderer.item.ItemProperties.register(
                StrangeMatterMod.ANOMALY_RESONATOR.get(),
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
        
        // Register particle providers
        event.enqueueWork(() -> {
            net.minecraft.client.Minecraft.getInstance().particleEngine.register(
                StrangeMatterMod.ENERGY_ABSORPTION_PARTICLE.get(), 
                com.hexvane.strangematter.particle.EnergyAbsorptionParticle.Provider::new);
        });
        
        // Register menu screens
        event.enqueueWork(() -> {
            net.minecraft.client.gui.screens.MenuScreens.register(StrangeMatterMod.RESONANCE_CONDENSER_MENU.get(), com.hexvane.strangematter.client.screen.ResonanceCondenserScreen::new);
            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(StrangeMatterMod.RESONANCE_CONDENSER_BLOCK_ENTITY.get(), com.hexvane.strangematter.client.ResonanceCondenserRenderer::new);
            
            net.minecraft.client.gui.screens.MenuScreens.register(StrangeMatterMod.RESONANT_BURNER_MENU.get(), com.hexvane.strangematter.client.screen.ResonantBurnerScreen::new);
            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(StrangeMatterMod.RESONANT_BURNER_BLOCK_ENTITY.get(), com.hexvane.strangematter.client.ResonantBurnerRenderer::new);
            
            net.minecraft.client.gui.screens.MenuScreens.register(StrangeMatterMod.REALITY_FORGE_MENU.get(), com.hexvane.strangematter.client.screen.RealityForgeScreen::new);
        });
    }
}

