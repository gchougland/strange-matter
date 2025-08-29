package com.hexvane.strangematter.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import com.hexvane.strangematter.client.render.TriangleRenderType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OBJLoader {
    
    public static class OBJModel {
        public List<float[]> vertices = new ArrayList<>();
        public List<float[]> normals = new ArrayList<>();
        public List<float[]> texCoords = new ArrayList<>();
        public List<int[]> faces = new ArrayList<>();
        
        public void render(PoseStack poseStack, MultiBufferSource buffer, 
                          ResourceLocation texture, int packedLight, float r, float g, float b, float a) {
            
            Matrix4f matrix4f = poseStack.last().pose();
            Matrix3f matrix3f = poseStack.last().normal();
            
            // Choose render type based on alpha value
            RenderType renderType = (a < 1.0f) ? 
                TriangleRenderType.createTranslucentTriangles(texture) : 
                TriangleRenderType.createTriangles(texture);
            VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
            
            for (int[] face : faces) {
                // Each face is a triangle with 3 vertices
                for (int i = 0; i < 3; i++) {
                    int vertexIndex = face[i * 3] - 1; // OBJ uses 1-based indexing
                    int texIndex = face[i * 3 + 1] - 1;
                    int normalIndex = face[i * 3 + 2] - 1;
                    
                    // Bounds checking
                    if (vertexIndex >= 0 && vertexIndex < vertices.size() &&
                        texIndex >= 0 && texIndex < texCoords.size() &&
                        normalIndex >= 0 && normalIndex < normals.size()) {
                        
                        float[] vertex = vertices.get(vertexIndex);
                        float[] texCoord = texCoords.get(texIndex);
                        float[] normal = normals.get(normalIndex);
                        
                        vertexConsumer.vertex(matrix4f, vertex[0], vertex[1], vertex[2])
                                .color(r, g, b, a)
                                .uv(texCoord[0], texCoord[1])
                                .uv2(packedLight)
                                .endVertex();
                    }
                }
            }
        }
    }
    
    public static OBJModel loadModel(ResourceLocation location) {
        OBJModel model = new OBJModel();
        
        try (InputStream stream = Minecraft.getInstance().getResourceManager()
                .getResource(location).get().open()) {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.startsWith("v ")) {
                    // Vertex
                    String[] parts = line.substring(2).split("\\s+");
                    float x = Float.parseFloat(parts[0]);
                    float y = Float.parseFloat(parts[1]);
                    float z = Float.parseFloat(parts[2]);
                    model.vertices.add(new float[]{x, y, z});
                    
                } else if (line.startsWith("vn ")) {
                    // Normal
                    String[] parts = line.substring(3).split("\\s+");
                    float x = Float.parseFloat(parts[0]);
                    float y = Float.parseFloat(parts[1]);
                    float z = Float.parseFloat(parts[2]);
                    model.normals.add(new float[]{x, y, z});
                    
                } else if (line.startsWith("vt ")) {
                    // Texture coordinate
                    String[] parts = line.substring(3).split("\\s+");
                    float u = Float.parseFloat(parts[0]);
                    float v = Float.parseFloat(parts[1]);
                    model.texCoords.add(new float[]{u, v});
                    
                } else if (line.startsWith("f ")) {
                    // Face - each face has 3 vertices, each vertex has 3 indices (v/t/n)
                    String[] parts = line.substring(2).split("\\s+");
                    int[] face = new int[9]; // 3 vertices * 3 indices each
                    
                    for (int i = 0; i < 3; i++) {
                        String[] vertexData = parts[i].split("/");
                        face[i * 3] = Integer.parseInt(vertexData[0]);     // vertex
                        face[i * 3 + 1] = Integer.parseInt(vertexData[1]); // texture
                        face[i * 3 + 2] = Integer.parseInt(vertexData[2]); // normal
                    }
                    
                    model.faces.add(face);
                }
            }
            
            System.out.println("Loaded OBJ model: " + model.vertices.size() + " vertices, " + 
                             model.texCoords.size() + " tex coords, " + model.normals.size() + 
                             " normals, " + model.faces.size() + " faces");
            
        } catch (IOException e) {
            System.err.println("Failed to load OBJ model: " + location);
            e.printStackTrace();
        }
        
        return model;
    }
}
