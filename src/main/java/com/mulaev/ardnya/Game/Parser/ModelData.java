package com.mulaev.ardnya.Game.Parser;

import com.jogamp.opengl.GL4;
import com.mulaev.ardnya.Game.Entity.LoadedObject;
import graphicslib3D.Matrix3D;

public class ModelData {

	private float[] vertices;
	private float[] textureCoords;
	private float[] normals;
	private int[] indices;
	private float furthestPoint;

	public ModelData(float[] vertices, float[] textureCoords, float[] normals, int[] indices,
			float furthestPoint) {
		this.vertices = vertices;
		this.textureCoords = textureCoords;
		this.normals = normals;
		this.indices = indices;
		this.furthestPoint = furthestPoint;
	}

	public float[] getVertices() {
		return vertices;
	}

	public float[] getTextureCoords() {
		return textureCoords;
	}

	public float[] getNormals() {
		return normals;
	}

	public int[] getIndices() {
		return indices;
	}

	public float getFurthestPoint() {
		return furthestPoint;
	}

	public LoadedObject convertToLoadedObj(Matrix3D pMat, int rendering_program, int proj_loc, int mv_loc, int n_loc, boolean notBase) {
		return new LoadedObject(pMat, rendering_program, proj_loc, mv_loc, n_loc,
				getVertices(), getTextureCoords(), getNormals(), getIndices(), notBase);
	}

}
