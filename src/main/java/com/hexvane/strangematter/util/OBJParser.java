package com.hexvane.strangematter.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to parse OBJ files and extract geometry data
 */
public class OBJParser {
    
    public static class GeometryData {
        public List<float[]> vertices = new ArrayList<>();
        public List<int[]> faces = new ArrayList<>();
        public List<float[]> uvs = new ArrayList<>();
        public List<float[]> normals = new ArrayList<>();
    }
    
    public static GeometryData parseOBJ(String filePath) throws IOException {
        GeometryData data = new GeometryData();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.startsWith("v ")) {
                    // Vertex data
                    String[] parts = line.substring(2).trim().split("\\s+");
                    if (parts.length >= 3) {
                        float x = Float.parseFloat(parts[0]);
                        float y = Float.parseFloat(parts[1]);
                        float z = Float.parseFloat(parts[2]);
                        data.vertices.add(new float[]{x, y, z});
                    }
                } else if (line.startsWith("vt ")) {
                    // UV data
                    String[] parts = line.substring(3).trim().split("\\s+");
                    if (parts.length >= 2) {
                        float u = Float.parseFloat(parts[0]);
                        float v = Float.parseFloat(parts[1]);
                        data.uvs.add(new float[]{u, v});
                    }
                } else if (line.startsWith("vn ")) {
                    // Normal data
                    String[] parts = line.substring(3).trim().split("\\s+");
                    if (parts.length >= 3) {
                        float x = Float.parseFloat(parts[0]);
                        float y = Float.parseFloat(parts[1]);
                        float z = Float.parseFloat(parts[2]);
                        data.normals.add(new float[]{x, y, z});
                    }
                } else if (line.startsWith("f ")) {
                    // Face data
                    String[] parts = line.substring(2).trim().split("\\s+");
                    if (parts.length >= 3) {
                        // Parse face indices (format: vertex/texture/normal)
                        int[] face = new int[3];
                        for (int i = 0; i < 3; i++) {
                            String[] indices = parts[i].split("/");
                            // OBJ indices are 1-based, convert to 0-based
                            face[i] = Integer.parseInt(indices[0]) - 1;
                        }
                        data.faces.add(face);
                    }
                }
            }
        }
        
        return data;
    }
    
    public static String generateJavaCode(GeometryData data, String className) {
        StringBuilder code = new StringBuilder();
        
        code.append("// Generated icosahedron geometry from OBJ file\n");
        code.append("private static final float[][] ICOSAHEDRON_VERTICES = {\n");
        
        for (float[] vertex : data.vertices) {
            code.append(String.format("    {%.6ff, %.6ff, %.6ff},\n", vertex[0], vertex[1], vertex[2]));
        }
        
        code.append("};\n\n");
        
        code.append("private static final int[][] ICOSAHEDRON_FACES = {\n");
        for (int[] face : data.faces) {
            code.append(String.format("    {%d, %d, %d},\n", face[0], face[1], face[2]));
        }
        
        code.append("};\n\n");
        
        if (!data.uvs.isEmpty()) {
            code.append("private static final float[][] ICOSAHEDRON_UVS = {\n");
            for (float[] uv : data.uvs) {
                code.append(String.format("    {%.6ff, %.6ff},\n", uv[0], uv[1]));
            }
            code.append("};\n\n");
        }
        
        if (!data.normals.isEmpty()) {
            code.append("private static final float[][] ICOSAHEDRON_NORMALS = {\n");
            for (float[] normal : data.normals) {
                code.append(String.format("    {%.6ff, %.6ff, %.6ff},\n", normal[0], normal[1], normal[2]));
            }
            code.append("};\n\n");
        }
        
        return code.toString();
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: OBJParser <path-to-obj-file>");
            return;
        }
        
        try {
            GeometryData data = parseOBJ(args[0]);
            System.out.println("Extracted " + data.vertices.size() + " vertices");
            System.out.println("Extracted " + data.faces.size() + " faces");
            System.out.println("Extracted " + data.uvs.size() + " UV coordinates");
            System.out.println("Extracted " + data.normals.size() + " normals");
            
            String javaCode = generateJavaCode(data, "IcosahedronGeometry");
            System.out.println("\nGenerated Java code:");
            System.out.println(javaCode);
            
        } catch (IOException e) {
            System.err.println("Error reading OBJ file: " + e.getMessage());
        }
    }
}
