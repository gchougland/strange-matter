package com.hexvane.strangematter.client;

import com.hexvane.strangematter.item.EchoVacuumItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class EchoVacuumRenderer implements IClientItemExtensions {
    
    private static final ResourceLocation BEAM_TEXTURE = ResourceLocation.fromNamespaceAndPath("strangematter", "textures/particle/beam.png");
    private static final double BEAM_RANGE = 8.0;
    private static final float BEAM_WIDTH = 0.1f;
    
    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, 
                                         ItemStack itemInHand, float partialTick, float equipProcess, 
                                         float swingProcess) {
        
        // Always return false to let Minecraft handle the bow charging animation naturally
        // The bow animation will make the gun face forward and show the charging effect
        return false;
    }
}
