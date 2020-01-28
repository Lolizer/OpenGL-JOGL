package com.mulaev.ardnya.Game;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.mulaev.ardnya.Game.Util.MyUtil;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import graphicslib3D.Vertex3D;

import java.nio.FloatBuffer;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
import static com.jogamp.opengl.GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

public class Sphere {
    private GL4 gl;
    private int numVertices, numIndices, prec; // prec = precision
    private int[] indices;
    private int[] vbo;
    private Vertex3D[] vertices;
    private int tex;

    public Sphere(GL4 gl, int p) {
        prec = p;
        vbo = new int[3];
        tex = MyUtil.loadTexture("dstar.png").getTextureObject();
        this.gl = gl;
        initSphere();
    }

    private void initSphere() {
        numVertices = (prec + 1) * (prec + 1);
        numIndices = prec * prec * 6;
        vertices = new Vertex3D[numVertices];
        indices = new int[numIndices];
        for (int i = 0; i < numVertices; i++) {
            vertices[i] = new Vertex3D();
        }
// calculate triangle vertices
        for (int i = 0; i <= prec; i++) {
            for (int j = 0; j <= prec; j++) {
                float y = (float) cos(toRadians(180 - i * 180 / prec));
                float x = -(float) cos(toRadians(j * 360 / prec)) * (float)
                        abs(cos(asin(y)));
                float z = (float) sin(toRadians(j * 360 / prec)) * (float)
                        abs(cos(asin(y)));
                vertices[i * (prec + 1) + j].setLocation(new Point3D(x, y, z));
                vertices[i * (prec + 1) + j].setS((float) j / prec); // texture coordinates
                vertices[i * (prec + 1) + j].setT((float) i / prec);
                vertices[i * (prec + 1) + j].setNormal(new Vector3D(vertices[i *
                        (prec + 1) + j].getLocation()));
            }
        }
// calculate triangle indices
        for (int i = 0; i < prec; i++) {
            for (int j = 0; j < prec; j++) {
                indices[6 * (i * prec + j) + 0] = i * (prec + 1) + j;
                indices[6 * (i * prec + j) + 1] = i * (prec + 1) + j + 1;
                indices[6 * (i * prec + j) + 2] = (i + 1) * (prec + 1) + j;
                indices[6 * (i * prec + j) + 3] = i * (prec + 1) + j + 1;
                indices[6 * (i * prec + j) + 4] = (i + 1) * (prec + 1) + j + 1;
                indices[6 * (i * prec + j) + 5] = (i + 1) * (prec + 1) + j;
            }
        }

        setBuffers();
    }

    private void setBuffers() {
        float[ ] pvalues = new float[indices.length * 3];
        float[ ] tvalues = new float[indices.length * 2];
        float[ ] nvalues = new float[indices.length * 3];

        for (int i = 0; i < indices.length; i++)
        {
            pvalues[i * 3] = (float) (vertices[indices[i]]).getX();
            pvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getY();
            pvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getZ();

            tvalues[i * 2] = (float) (vertices[indices[i]]).getS();
            tvalues[i * 2 + 1] = (float) (vertices[indices[i]]).getT();

            nvalues[i * 3] = (float) (vertices[indices[i]]).getNormalX();
            nvalues[i * 3 + 1] = (float)(vertices[indices[i]]).getNormalY();
            nvalues[i * 3 + 2]=(float) (vertices[indices[i]]).getNormalZ();
        }

        gl.glGenBuffers(vbo.length, vbo, 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf,
                GL_STATIC_DRAW);

        // put the texture coordinates into buffer #1
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

        // put the normal coordinates into buffer #2
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
        gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4, norBuf, GL_STATIC_DRAW);
    }

    public int[] getIndices() {
        return indices;
    }

    public Vertex3D[] getVertices() {
        return vertices;
    }

    public void dispose() {
        gl.glDeleteBuffers(vbo.length, vbo,0);
    }

    public void draw() {
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        // activate buffer #1, which contains the texture coordinates
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);

        // activate texture unit #0 and bind it to the texture object
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, tex);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
                GL_LINEAR_MIPMAP_LINEAR);
        gl.glGenerateMipmap(GL_TEXTURE_2D);

        if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
        {
            float max[ ] = new float[1];
            gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]);
        }

        gl.glDrawArrays(GL_TRIANGLES, 0, getIndices().length);
    }
}
