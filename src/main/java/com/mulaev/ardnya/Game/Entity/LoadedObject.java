package com.mulaev.ardnya.Game.Entity;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import com.mulaev.ardnya.Game.Util.MyUtil;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRONT_AND_BACK;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
import static com.jogamp.opengl.GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE1;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL2GL3.GL_FILL;
import static com.jogamp.opengl.GL2GL3.GL_LINE;

/**
 * @author ardnya
 */
public class LoadedObject implements Cloneable {
    private GL4 gl;
    private Point3D position;
    private Matrix3D pMat;
    private Matrix3D lookAt;
    private Matrix3D translate;
    private Matrix3D rotate;
    private Matrix3D scale;
    private ModelStack model;
    private ModelStack retModel;
    private float[] vertices;
    private float[] textures;
    private float[] normals;
    private int[] indices;
    private int[] vao;
    private int[] vbo;
    private int indAmt;
    private int tex;
    private int proj_loc;
    private int mv_loc;
    private boolean culling;
    private boolean poly;
    private boolean notBase;
    private boolean erased;

    public LoadedObject(Matrix3D pMat, int proj_loc, int mv_loc,
            float[] vertices, float[] textures, float[] normals, int[] indices, boolean notBase) {
        this.gl = (GL4) GLContext.getCurrentGL();
        vao = new int[1];
        vbo = new int[4];
        this.notBase = notBase;
        this.proj_loc = proj_loc;
        this.mv_loc = mv_loc;
        this.vertices = vertices;
        this.textures = textures;
        this.normals = normals;
        this.indices = indices;
        this.pMat = pMat;
        position = new Point3D();
        model = new ModelStack(20);
        tex = -1;
        indAmt = indices.length;

        initBuffers();
        eraseData();
    }

    private void initBuffers() {
        gl.glGenVertexArrays(vao.length, vao, 0);
        gl.glBindVertexArray(vao[0]);

        gl.glGenBuffers(vbo.length, vbo, 0);
        if (vertices != null) {
            gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(vertices);
            gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf,
                    GL_STATIC_DRAW);
        } else throw new RuntimeException("Vertices have to be set!");

        if (textures != null) {
            // put the texture coordinates into buffer #1
            gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
            FloatBuffer texBuf = Buffers.newDirectFloatBuffer(textures);
            gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);
        }
        if (normals != null) {
            // put the normal coordinates into buffer #2
            gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
            FloatBuffer norBuf = Buffers.newDirectFloatBuffer(normals);
            gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL_STATIC_DRAW);
        }
        if (indices != null) {
            // put the indices coordinates into buffer #3
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
            IntBuffer indBuf = Buffers.newDirectIntBuffer(indices);
            gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indBuf.limit() * 4, indBuf, GL_STATIC_DRAW);
        }

        gl.glBindVertexArray(0);
    }

    private boolean isCullingOff() { return culling; }

    private boolean isPolyOn() { return poly; }

    private boolean isNotBase() {
        return notBase;
    }

    private void setMatrices() {
        if (!isNotBase()) {
            model.pushMatrix();
            if (lookAt != null)
                model.multMatrix(lookAt);
            else throw new RuntimeException("LookAt matrix have to be!");
        }

        model.pushMatrix();
        if (translate != null)
            model.multMatrix(translate);
        else model.translate(.0, .0, .0);

        // объектная mv
        retModel = model.clone();

        model.pushMatrix();
        if (rotate != null) {
            model.multMatrix(rotate);
        } else model.popMatrix();

        model.pushMatrix();
        if (scale != null) {
            model.multMatrix(scale);
        } else model.popMatrix();
    }

    private void setUpTex() {
        // activate texture unit #0 and bind it to the texture object
        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, tex);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
                GL_LINEAR_MIPMAP_LINEAR);
        gl.glGenerateMipmap(GL_TEXTURE_2D);

        if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) {
            float max[] = new float[1];
            gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]);
        }
    }

    private void eraseData() {
        vertices = null;
        if (textures != null)
            erased = true;
        textures = null;
        normals = null;
        indices = null;

        new Thread(() -> {
            try {
                System.gc();
                Thread.sleep(10);
                System.gc();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }).start();
    }

    private boolean wasErased() {
        return erased;
    }

    public void draw() {
        if (isCullingOff())
            gl.glDisable(GL_CULL_FACE);
        if (isPolyOn())
            gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        gl.glBindVertexArray(vao[0]);
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        if (textures != null || wasErased()) {
            // activate buffer #1, which contains the texture coordinates
            gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
            gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(1);
        }
        // indices
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);

        if (tex != -1) {
            setUpTex();
        }

        // lookAt, translation, rotation и scale матрицы
        setMatrices();

        gl.glUniformMatrix4fv(proj_loc, 1, false,
                pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(mv_loc, 1, false,
                model.peek().getFloatValues(), 0);

        //gl.glDrawElements(GL_QUADS, getIndices().length, GL_UNSIGNED_INT, 0);
        gl.glDrawElements(GL_TRIANGLES, getIndAmount(), GL_UNSIGNED_INT, 0);
        gl.glBindVertexArray(0);

        while (model.getStackCount() > 0) {
            model.popMatrix();
        }

        if (isPolyOn())
            gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        if (isCullingOff())
            gl.glEnable(GL_CULL_FACE);
    }

    public void dispose() {
        gl.glDeleteVertexArrays(vao.length, vao, 0);
        gl.glDeleteBuffers(vbo.length, vbo,0);
        gl.glDeleteTextures(1, new int[]{tex}, 0);
    }

    public void setTex(String texLoc) { tex = MyUtil.loadTexture(texLoc).getTextureObject(); }

    // для сборных моделей
    public void setStack(ModelStack model) {
        this.model = model.clone();
    }
    public void setCull(boolean culling) { this.culling = culling; }
    public void setPoly(boolean poly) { this.poly = poly; }
    public void setLookAt(Matrix3D lookAt) { this.lookAt = lookAt; }

    public void translate(double x, double y, double z) {
        translate = new Matrix3D();
        translate.translate(x, y, z);
        position.setX(x);
        position.setY(y);
        position.setZ(z);
    }
    public void rotate(double xDegrees, double yDegrees, double zDegrees) {
        rotate = new Matrix3D();
        rotate.rotate(xDegrees, yDegrees, zDegrees);
    }
    public void scale(double sx, double sy, double sz) {
        scale = new Matrix3D();
        scale.scale(sx, sy, sz);
    }

    public ModelStack getStack(boolean rot) {
        if (!rot || rotate == null)
            return retModel;

        retModel.multMatrix(rotate);
        return retModel;
    }

    public int getIndAmount() {return indAmt;}
    public boolean getCull() {return culling;}
    public boolean getPoly() {return poly;}
    public Point3D getPosition() { return position; }

    @Override
    public LoadedObject clone() {
        LoadedObject copy = null;
        try {
            copy = (LoadedObject) super.clone();
        } catch (CloneNotSupportedException cnse) {
            cnse.printStackTrace();
            System.exit(1);
        }
        return copy;
    }
}
