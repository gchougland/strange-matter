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
import java.util.HashMap;
import java.util.Map;

public class OBJLoader {
    
    public static class OBJModel {
        public List<float[]> vertices = new ArrayList<>();
        public List<float[]> normals = new ArrayList<>();
        public List<float[]> texCoords = new ArrayList<>();
        public List<int[]> faces = new ArrayList<>();
        public List<String> faceMaterials = new ArrayList<>(); // Material for each face
        public Map<String, ResourceLocation> materials = new HashMap<>(); // Material name -> texture
        
        public void render(PoseStack poseStack, MultiBufferSource buffer, 
                          ResourceLocation defaultTexture, int packedLight, float r, float g, float b, float a) {
            
            Matrix4f matrix4f = poseStack.last().pose();
            Matrix3f matrix3f = poseStack.last().normal();
            
            // Group faces by material
            Map<String, List<Integer>> materialGroups = new HashMap<>();
            for (int i = 0; i < faces.size(); i++) {
                String material = (i < faceMaterials.size()) ? faceMaterials.get(i) : "default";
                if (!materialGroups.containsKey(material)) {
                    materialGroups.put(material, new ArrayList<>());
                }
                materialGroups.get(material).add(i);
            }
            
            // Render each material group
            for (Map.Entry<String, List<Integer>> entry : materialGroups.entrySet()) {
                String materialName = entry.getKey();
                List<Integer> faceIndices = entry.getValue();
                
                // Get texture for this material
                ResourceLocation texture = materials.getOrDefault(materialName, defaultTexture);
                
                // Choose render type based on alpha value
                RenderType renderType = (a < 1.0f) ? 
                    TriangleRenderType.createTranslucentTriangles(texture) : 
                    TriangleRenderType.createTriangles(texture);
                VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
                
                // Render faces for this material
                for (Integer faceIndex : faceIndices) {
                    int[] face = faces.get(faceIndex);
                    
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
                                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                                    .uv2(packedLight)
                                    .normal(matrix3f, normal[0], normal[1], normal[2])
                                    .endVertex();
                        }
                    }
                }
            }
        }
        
        public void renderWithDistortion(PoseStack poseStack, MultiBufferSource buffer, 
                                       ResourceLocation defaultTexture, int packedLight, float r, float g, float b, float a,
                                       float time) {
            
            Matrix4f matrix4f = poseStack.last().pose();
            Matrix3f matrix3f = poseStack.last().normal();
            
            // Choose render type based on alpha value
            RenderType renderType = (a < 1.0f) ? 
                TriangleRenderType.createTranslucentTriangles(defaultTexture) : 
                TriangleRenderType.createTriangles(defaultTexture);
            VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
            
            // Render all faces with per-vertex distortion
            for (int faceIndex = 0; faceIndex < faces.size(); faceIndex++) {
                int[] face = faces.get(faceIndex);
                
                // Each face is a triangle with 3 vertices
                for (int i = 0; i < 3; i++) {
                    int vertexIndex = face[i * 3] - 1; // OBJ uses 1-based indexing
                    int texIndex = face[i * 3 + 1] - 1;
                    int normalIndex = face[i * 3 + 2] - 1;
                    
                    // Bounds checking
                    if (vertexIndex >= 0 && vertexIndex < vertices.size() &&
                        texIndex >= 0 && texIndex < texCoords.size() &&
                        normalIndex >= 0 && normalIndex < normals.size()) {
                        
                        float[] originalVertex = vertices.get(vertexIndex);
                        float[] distortedVertex = applyVertexDistortion(originalVertex, vertexIndex, time);
                        float[] texCoord = texCoords.get(texIndex);
                        float[] normal = normals.get(normalIndex);
                        
                        vertexConsumer.vertex(matrix4f, distortedVertex[0], distortedVertex[1], distortedVertex[2])
                                .color(r, g, b, a)
                                .uv(texCoord[0], texCoord[1])
                                .overlayCoords(OverlayTexture.NO_OVERLAY)
                                .uv2(packedLight)
                                .normal(matrix3f, normal[0], normal[1], normal[2])
                                .endVertex();
                    }
                }
            }
        }
        
        private float[] applyVertexDistortion(float[] originalVertex, int vertexIndex, float time) {
            // Create unique animation for each vertex based on its index
            float vertexTime = time + (vertexIndex * 0.2f); // Offset each vertex
            
            // Calculate multiple sine waves for complex distortion (reduced intensity)
            float distortion1 = (float) (Math.sin(vertexTime * 1.5) * 0.08);
            float distortion2 = (float) (Math.sin(vertexTime * 2.3 + vertexIndex * 0.3) * 0.05);
            float distortion3 = (float) (Math.sin(vertexTime * 0.8 + vertexIndex * 0.6) * 0.03);
            
            // Combine distortions
            float totalDistortion = distortion1 + distortion2 + distortion3;
            
            // Apply distortion radially from the vertex position
            float[] result = new float[3];
            float magnitude = (float) Math.sqrt(originalVertex[0] * originalVertex[0] + 
                                              originalVertex[1] * originalVertex[1] + 
                                              originalVertex[2] * originalVertex[2]);
            
            if (magnitude > 0) {
                // Normalize the vertex position
                float nx = originalVertex[0] / magnitude;
                float ny = originalVertex[1] / magnitude;
                float nz = originalVertex[2] / magnitude;
                
                // Apply distortion along the radial direction
                result[0] = originalVertex[0] + nx * totalDistortion;
                result[1] = originalVertex[1] + ny * totalDistortion;
                result[2] = originalVertex[2] + nz * totalDistortion;
            } else {
                // If magnitude is 0, just return the original vertex
                result[0] = originalVertex[0];
                result[1] = originalVertex[1];
                result[2] = originalVertex[2];
            }
            
            return result;
        }
    }
    
    private static Map<String, ResourceLocation> loadMTL(ResourceLocation mtlLocation) {
        Map<String, ResourceLocation> materials = new HashMap<>();
        
        try (InputStream stream = Minecraft.getInstance().getResourceManager()
                .getResource(mtlLocation).get().open()) {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            String currentMaterial = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.startsWith("newmtl ")) {
                    currentMaterial = line.substring(7);
                } else if (line.startsWith("map_Kd ") && currentMaterial != null) {
                    String texturePath = line.substring(7);
                    // Convert texture path to ResourceLocation
                    ResourceLocation texture = new ResourceLocation(mtlLocation.getNamespace(), 
                        "textures/block/" + texturePath);
                    materials.put(currentMaterial, texture);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load MTL file: " + e.getMessage());
        }
        
        return materials;
    }
    
    public static OBJModel loadModel(ResourceLocation location) {
        return loadModel(location, false);
    }
    
    public static OBJModel loadModel(ResourceLocation location, boolean flipUVs) {
        OBJModel model = new OBJModel();
        String currentMaterial = "default";
        
        // Load MTL file if it exists
        String objPath = location.getPath();
        String mtlPath = objPath.replace(".obj", ".mtl");
        ResourceLocation mtlLocation = new ResourceLocation(location.getNamespace(), mtlPath);
        model.materials = loadMTL(mtlLocation);
        
        try (InputStream stream = Minecraft.getInstance().getResourceManager()
                .getResource(location).get().open()) {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.startsWith("mtllib ")) {
                    // Material library reference - already handled above
                } else if (line.startsWith("usemtl ")) {
                    // Material usage
                    currentMaterial = line.substring(7);
                } else if (line.startsWith("v ")) {
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
                    if (flipUVs) {
                        v = 1.0f - v; // Flip V coordinate for models that need it
                    }
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
                    model.faceMaterials.add(currentMaterial);
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
