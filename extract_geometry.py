#!/usr/bin/env python3

# Read the OBJ file and extract geometry data
with open('c:/Users/gchou/Downloads/cube.obj', 'r') as f:
    lines = f.readlines()

vertices = []
faces = []

for line in lines:
    if line.startswith('v '):
        # Parse vertex line like "v 0.000000 -1.000000 0.000000"
        parts = line.strip().split()[1:]  # Remove 'v' and split
        if len(parts) >= 3:
            x = float(parts[0])
            y = float(parts[1])
            z = float(parts[2])
            vertices.append([x, y, z])
    elif line.startswith('f '):
        # Parse face line like "f 1/1/1 14/26/1 13/24/1"
        parts = line.strip().split()[1:]  # Remove 'f' and split
        face = []
        for part in parts:
            # Extract first number (vertex index) and subtract 1 (convert to 0-based)
            vertex_index = int(part.split('/')[0]) - 1
            face.append(vertex_index)
        faces.append(face)

print("// Cube vertices (" + str(len(vertices)) + " vertices)")
print("// Generated from OBJ file")
print("private static final float[][] CUBE_VERTICES = {")

for i, vertex in enumerate(vertices):
    print(f"    {{{vertex[0]:.6f}f, {vertex[1]:.6f}f, {vertex[2]:.6f}f}},")

print("};\n")

print("// Cube faces (" + str(len(faces)) + " triangular faces) with correct winding order")
print("// Properly extracted from OBJ file - vertex indices are 1-based in OBJ, converted to 0-based")
print("private static final int[][] CUBE_FACES = {")

for i, face in enumerate(faces):
    if i % 5 == 0 and i > 0:
        print()
    print(f"    {{{face[0]}, {face[1]}, {face[2]}}},", end=" ")

print("\n};")

print(f"\n// Summary: {len(vertices)} vertices, {len(faces)} faces")
