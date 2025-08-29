package com.hexvane.strangematter.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class TriangleRenderType extends RenderType {
    
    public static RenderType createTriangles(ResourceLocation texture) {
        return create("triangles", 
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, 
            VertexFormat.Mode.TRIANGLES, 
            256, 
            false, 
            false, 
            CompositeState.builder()
                .setShaderState(RENDERTYPE_SOLID_SHADER)
                .setLightmapState(LIGHTMAP)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(NO_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .createCompositeState(false));
    }
    
    public static RenderType createTranslucentTriangles(ResourceLocation texture) {
        return create("translucent_triangles", 
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, 
            VertexFormat.Mode.TRIANGLES, 
            256, 
            false, 
            true, // sortOnUpload for transparency
            CompositeState.builder()
                .setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
                .setLightmapState(LIGHTMAP)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .createCompositeState(false));
    }
    
    private TriangleRenderType(String name, VertexFormat format, VertexFormat.Mode mode, 
                              int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, 
                              Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
}
