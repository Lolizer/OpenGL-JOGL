package com.mulaev.ardnya.Game.Parser;

import graphicslib3D.Vector3D;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class OBJFileLoader {

	public static ModelData loadOBJ(String objFileName) {
		FileReader isr = null;
		File objFile = new File(objFileName);
		try {
			isr = new FileReader(objFile);
		} catch (FileNotFoundException e) {
			System.err.println("File not found in res;");
		}
		BufferedReader reader = new BufferedReader(isr);
		String line;
		List<Vertex> vertices = new ArrayList<>();
		List<Vector3D> textures = new ArrayList<>();
		List<Vector3D> normals = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();
		List<String[]> parsedIndices = new ArrayList<>();
		try {
			while ((line = reader.readLine()) != null) {
				String[] currentLine = line.split(" +");
				if (line.startsWith("v ")) {
					Vector3D vertex = new Vector3D(Float.valueOf(currentLine[1]),
							Float.valueOf(currentLine[2]),
							Float.valueOf(currentLine[3]));
					Vertex newVertex = new Vertex(vertices.size(), vertex);
					vertices.add(newVertex);

				} else if (line.startsWith("vt ")) {
					Vector3D texture = new Vector3D(Float.valueOf(currentLine[1]),
							Float.valueOf(currentLine[2]), 0.0);
					textures.add(texture);
				} else if (line.startsWith("vn ")) {
					Vector3D normal = new Vector3D(Float.valueOf(currentLine[1]),
							Float.valueOf(currentLine[2]),
							Float.valueOf(currentLine[3]));
					normals.add(normal);
				} else if (line.startsWith("f ")) {
					for (int i = 1; i < currentLine.length; i++) {
						String[] parsedVertices = currentLine[i].split("/");
						parsedIndices.add(parsedVertices);
					}
				}
			}
			reader.close();

			for (int i = 0; i < parsedIndices.size(); i++) {
				processVertex(parsedIndices.get(i), vertices, indices);
			}
		} catch (IOException e) {
			System.err.println("Error reading the file");
		}

		removeUnusedVertices(vertices);
		float[] verticesArray = new float[vertices.size() * 3];
		float[] texturesArray = new float[vertices.size() * 2];
		float[] normalsArray = new float[vertices.size() * 3];
		float furthest = convertDataToArrays(vertices, textures, normals, verticesArray,
				texturesArray, normalsArray);
		int[] indicesArray = convertIndicesListToArray(indices);
		ModelData data = new ModelData(verticesArray, texturesArray, normalsArray, indicesArray,
				furthest);
		return data;
	}

	private static void processVertex(String[] vertex, List<Vertex> vertices, List<Integer> indices) {
		int index = Integer.parseInt(vertex[0]) - 1;
		Vertex currentVertex = vertices.get(index);
		int textureIndex = vertex.length > 1 && !vertex[1].isEmpty() ? Integer.parseInt(vertex[1]) - 1 : 0;
		int normalIndex = vertex.length > 2 && !vertex[2].isEmpty() ? Integer.parseInt(vertex[2]) - 1 : 0;
		if (!currentVertex.isSet()) {
			currentVertex.setTextureIndex(textureIndex);
			currentVertex.setNormalIndex(normalIndex);
			indices.add(index);
		} else {
			dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices,
					vertices);
		}
	}

	private static int[] convertIndicesListToArray(List<Integer> indices) {
		int[] indicesArray = new int[indices.size()];
		for (int i = 0; i < indicesArray.length; i++) {
			indicesArray[i] = indices.get(i);
		}
		return indicesArray;
	}

	private static float convertDataToArrays(List<Vertex> vertices, List<Vector3D> textures,
			List<Vector3D> normals, float[] verticesArray, float[] texturesArray,
			float[] normalsArray) {
		float furthestPoint = 0;
		for (int i = 0; i < vertices.size(); i++) {
			Vertex currentVertex = vertices.get(i);
			if (currentVertex.getLength() > furthestPoint) {
				furthestPoint = currentVertex.getLength();
			}
			Vector3D position = currentVertex.getPosition();

			verticesArray[i * 3] = (float) position.getX();
			verticesArray[i * 3 + 1] = (float) position.getY();
			verticesArray[i * 3 + 2] = (float) position.getZ();

			if (textures.size() != 0) {
				Vector3D textureCoord = textures.get(currentVertex.getTextureIndex());
				texturesArray[i * 2] = (float) textureCoord.getX();
				texturesArray[i * 2 + 1] = (float) textureCoord.getY();
			} else {
				texturesArray[i * 2] = System.currentTimeMillis() % 2.0f;
				texturesArray[i * 2 + 1] = System.currentTimeMillis() % 2.0f;
			}
			if (normals.size() != 0) {
				Vector3D normalVector = normals.get(currentVertex.getNormalIndex());
				normalsArray[i * 3] = (float) normalVector.getX();
				normalsArray[i * 3 + 1] = (float) normalVector.getY();
				normalsArray[i * 3 + 2] = (float) normalVector.getZ();
			}
		}
		return furthestPoint;
	}

	private static void dealWithAlreadyProcessedVertex(Vertex previousVertex, int newTextureIndex,
			int newNormalIndex, List<Integer> indices, List<Vertex> vertices) {
		if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
			indices.add(previousVertex.getIndex());
		} else {
			Vertex anotherVertex = previousVertex.getDuplicateVertex();
			if (anotherVertex != null) {
				dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex,
						indices, vertices);
			} else {
				Vertex duplicateVertex = new Vertex(vertices.size(), previousVertex.getPosition());
				duplicateVertex.setTextureIndex(newTextureIndex);
				duplicateVertex.setNormalIndex(newNormalIndex);
				previousVertex.setDuplicateVertex(duplicateVertex);
				vertices.add(duplicateVertex);
				indices.add(duplicateVertex.getIndex());
			}

		}
	}
	
	private static void removeUnusedVertices(List<Vertex> vertices){
		for(Vertex vertex:vertices){
			if(!vertex.isSet()){
				vertex.setTextureIndex(0);
				vertex.setNormalIndex(0);
			}
		}
	}

}