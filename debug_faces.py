#!/usr/bin/env python3

# Read the OBJ file and manually verify each face
with open('c:/Users/gchou/Downloads/icosphere.obj', 'r') as f:
    lines = f.readlines()

vertices = []
faces = []

# Extract vertices
for line in lines:
    if line.startswith('v '):
        parts = line.strip().split()[1:]
        if len(parts) >= 3:
            x = float(parts[0])
            y = float(parts[1])
            z = float(parts[2])
            vertices.append([x, y, z])

# Extract faces manually
for line in lines:
    if line.startswith('f '):
        parts = line.strip().split()[1:]
        face = []
        for part in parts:
            # Extract first number (vertex index) and subtract 1 (convert to 0-based)
            vertex_index = int(part.split('/')[0]) - 1
            face.append(vertex_index)
        faces.append(face)

print("Vertices:")
for i, v in enumerate(vertices):
    print(f"  {i}: {v}")

print("\nFaces (0-based indices):")
for i, face in enumerate(faces):
    print(f"  Face {i}: {face}")

print("\nJava array format:")
print("private static final int[][] ICOSAHEDRON_FACES = {")
for i, face in enumerate(faces):
    if i % 5 == 0 and i > 0:
        print()
    print(f"    {{{face[0]}, {face[1]}, {face[2]}}},", end=" ")
print("\n};")

# Verify each face by checking if vertices are valid
print("\nFace validation:")
for i, face in enumerate(faces):
    valid = True
    for vertex_idx in face:
        if vertex_idx < 0 or vertex_idx >= len(vertices):
            valid = False
            break
    print(f"  Face {i}: {face} - {'VALID' if valid else 'INVALID'}")
