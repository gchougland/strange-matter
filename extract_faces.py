#!/usr/bin/env python3

# Read the OBJ file and extract face data
with open('c:/Users/gchou/Downloads/icosphere.obj', 'r') as f:
    lines = f.readlines()

faces = []
for line in lines:
    if line.startswith('f '):
        # Parse face line like "f 1/1/1 14/26/1 13/24/1"
        parts = line.strip().split()[1:]  # Remove 'f' and split
        face = []
        for part in parts:
            # Extract first number (vertex index) and subtract 1 (convert to 0-based)
            vertex_index = int(part.split('/')[0]) - 1
            face.append(vertex_index)
        faces.append(face)

print("// Icosahedron faces (80 triangular faces) with correct winding order")
print("// Properly extracted from OBJ file - vertex indices are 1-based in OBJ, converted to 0-based")
print("private static final int[][] ICOSAHEDRON_FACES = {")

for i, face in enumerate(faces):
    if i % 5 == 0 and i > 0:
        print()
    print(f"    {{{face[0]}, {face[1]}, {face[2]}}},", end=" ")

print("\n};")
