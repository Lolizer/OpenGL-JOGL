package com.mulaev.ardnya.Game.Parser;

import com.jogamp.opengl.GL4;
import com.mulaev.ardnya.Game.Entity.LoadedObject;
import graphicslib3D.Vector3D;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * deprecated @author ardnya
 */
public class OBJLoader {
    public static LoadedObject loadedObject(GL4 gl, int vao[], String filename) {
        try {
            try (FileReader fr = new FileReader(new File("res/" + filename))) {
                BufferedReader reader = new BufferedReader(fr);
                String line = null;
                List<Vector3D> vertices = new ArrayList<>();
                List<Vector3D> textures = new ArrayList<>();
                List<Vector3D> normals = new ArrayList<>();
                List<Integer> indices = new ArrayList<>();
                List<String[]> parsedIndices = new ArrayList<>();
                float[] verticesArray = null;
                float[] normalsArray = null;
                float[] textureArray = null;
                int[] indicesArray = null;

                while ((line = reader.readLine()) != null) {
                    String[] currentLine = line.split(" +");

                    if (line.startsWith("v ")) {
                        Vector3D vertex = new Vector3D(Float.parseFloat(currentLine[1]),
                                Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]));
                        vertices.add(vertex);
                    } else if (line.startsWith("vt ")) {
                        Vector3D texture = new Vector3D(Float.parseFloat(currentLine[1]),
                                Float.parseFloat(currentLine[2]), 0.0);
                        textures.add(texture);
                    } else if (line.startsWith("vn ")) {
                        Vector3D normal = new Vector3D(Float.parseFloat(currentLine[1]),
                                Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]));
                        normals.add(normal);
                    } else if (line.startsWith("f ")) {
                        for (int i = 1; i < currentLine.length; i++) {
                            String[] parsedVertices = currentLine[i].split("/");
                            parsedIndices.add(parsedVertices);
                        }
                    }
                }

                if (parsedIndices.size() != 0) {
                    textureArray = new float[vertices.size() * 2];
                    normalsArray = new float[vertices.size() * 3];

                    for (int i = 0; i < parsedIndices.size(); i++) {
                        processVertex(parsedIndices.get(i), indices,
                                textures, normals, textureArray, normalsArray);
                    }

                    verticesArray = new float[vertices.size() * 3];
                    indicesArray = new int[indices.size()];

                    int vertexPointer = 0;
                    for (Vector3D vertex : vertices) {
                        verticesArray[vertexPointer++] = (float) vertex.getX();
                        verticesArray[vertexPointer++] = (float) vertex.getY();
                        verticesArray[vertexPointer++] = (float) vertex.getZ();
                    }

                    for (int i = 0; i < indices.size(); i++) {
                        indicesArray[i] = indices.get(i);
                    }

                    return null;
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void processVertex(String[] vertexData, List<Integer> indices,
            List<Vector3D> textures, List<Vector3D> normals, float[] textureArray,
            float[] normalsArray) {

        int currentVertexPointer = Integer.parseInt(vertexData[0]) - 1;
        indices.add(currentVertexPointer);

        if (vertexData.length > 1) {
            if (!vertexData[1].isEmpty()) {
                Vector3D currentTex = textures.get(Integer.parseInt(vertexData[1]) - 1);
                textureArray[currentVertexPointer * 2] = (float) currentTex.getX();
                textureArray[currentVertexPointer * 2 + 1] = (float) currentTex.getY();
            }

            if (vertexData.length > 2) {
                Vector3D currentNorm = normals.get(Integer.parseInt(vertexData[2]) - 1);
                normalsArray[currentVertexPointer * 3] = (float) currentNorm.getX();
                normalsArray[currentVertexPointer * 3 + 1] = (float) currentNorm.getY();
                normalsArray[currentVertexPointer * 3 + 2] = (float) currentNorm.getZ();
            }
        }
    }
}
