package com.mulaev.ardnya.Game.Orient;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import com.mulaev.ardnya.Game.Util.MyUtil;
import graphicslib3D.Matrix3D;

import javax.swing.*;
import java.nio.FloatBuffer;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRONT_AND_BACK;
import static com.jogamp.opengl.GL.GL_LINE_STRIP;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_VIEWPORT;
import static com.jogamp.opengl.GL2GL3.GL_FILL;
import static com.jogamp.opengl.GL2GL3.GL_LINE;

public class Arrowhead {
    private static Arrowhead instance;
    private static GL4 gl;
    private static int[] vao = new int[1];
    private static int[] vbo = new int[3];
    private static Matrix3D ortho;
    private static int rendering_program;
    private static int tex;
    private static int proj_loc;
    private static int mv_loc;

    private Arrowhead() {
        gl = (GL4) GLContext.getCurrentGL();

        float[] arrowVertices = {
                0.0f, 0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -0.5f, 0.0f,
                0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -0.5f, 0.0f, 0.0f,
                0.0f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f,
                -0.5f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.5f, 0.0f, 0.0f,
        };

        float[] borderVertices = {
                -1.0f, 1.0f, 2.0f, 1.0f, 1.0f, 2.0f,
                1.0f, -1.0f, 2.0f, -1.0f, -1.0f, 2.0f,
                -1.0f, 1.0f, 2.0f
        };

        float[] textures = new float[arrowVertices.length / 3];

        for (int i = 0; i < textures.length; i++) {
            textures[i] = textures[i] * System.currentTimeMillis() / 1000;
        }

        ortho = orthoProjection();
        String vertex = "shaders/arrowhead_vert.shader";
        String fragment = "shaders/arrowhead_frag.shader";
        rendering_program = MyUtil.createShaderProgram(vertex,
                fragment, this.getClass().getSimpleName());

        tex = MyUtil.loadTexture("textures/arrowhead.jpg").getTextureObject();

        proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
        mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");

        gl.glGenVertexArrays(vao.length, vao, 0);
        gl.glBindVertexArray(vao[0]);

        gl.glGenBuffers(vbo.length, vbo, 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        FloatBuffer borderBuff = Buffers.newDirectFloatBuffer(borderVertices);
        gl.glBufferData(GL_ARRAY_BUFFER, borderBuff.limit() * 4, borderBuff,
                GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        FloatBuffer arrowheadBuff = Buffers.newDirectFloatBuffer(arrowVertices);
        gl.glBufferData(GL_ARRAY_BUFFER, arrowheadBuff.limit() * 4, arrowheadBuff,
                GL_STATIC_DRAW);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        FloatBuffer arrowheadTex = Buffers.newDirectFloatBuffer(textures);
        gl.glBufferData(GL_ARRAY_BUFFER, arrowheadTex.limit() * 4, arrowheadTex,
                GL_STATIC_DRAW);

        gl.glBindVertexArray(0);
    }
    private static Arrowhead getInstance() {
        if (instance != null) {
            return instance;
        }
        return instance = new Arrowhead();
    }

    public static boolean dispose() {
        if (instance != null) {
            instance = null;
            gl.glDeleteVertexArrays(vao.length, vao, 0);
            gl.glDeleteBuffers(vbo.length, vbo,0);
            return true;
        }

        return false;
    }

    public static void draw(JFrame frame,
            Matrix3D pMat, float pitch, float yaw) {

        if (instance == null)
            getInstance();

        int[] viewProp = new int[4];
        int width = 150, height = 100;
        Matrix3D vMat = new Matrix3D();
        Matrix3D mMat = new Matrix3D();
        Matrix3D scene = new Matrix3D();

        gl.glUseProgram(rendering_program);

        // настройка матриц вида и модели
        vMat.translate(0, 0, -30);
        mMat.rotate(-pitch, 90 - yaw, .0);
        mMat.translate(.0, .0, .0);
        mMat.scale(4.0, 4.0, 2.0);
        scene.concatenate(vMat);
        scene.concatenate(mMat);

        gl.glDisable(GL_CULL_FACE);
        gl.glClear(GL_DEPTH_BUFFER_BIT);

        // сохранение свойств viewport'a
        gl.glGetIntegerv(GL_VIEWPORT, viewProp, 0);
        gl.glViewport(0, frame.getHeight() - 32 - height, width, height);

        // отрисовка линий
        gl.glUniformMatrix4fv(proj_loc, 1, false,
                ortho.getFloatValues(), 0);
        gl.glUniformMatrix4fv(mv_loc, 1, false,
                (new Matrix3D()).getFloatValues(), 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, instance.vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT,
                false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glDrawArrays(GL_LINE_STRIP, 0, 5);

        gl.glUniformMatrix4fv(proj_loc, 1, false,
                pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(mv_loc, 1, false,
                scene.getFloatValues(), 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT,
                false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT,
                false, 0, 0);
        gl.glEnableVertexAttribArray(1);

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, tex);

        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        gl.glDrawArrays(GL_TRIANGLES, 0, 12);

        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        gl.glViewport(viewProp[0], viewProp[1], viewProp[2], viewProp[3]);
        gl.glEnable(GL_CULL_FACE);
    }

    private static Matrix3D orthoProjection() {
        float l = -1.0f; float r = 1.0f;
        float t = 1.2f; float b = -1.2f;
        float n = 0.01f; float f = 100.f;
        Matrix3D ortho = new Matrix3D();

        ortho.setElementAt(0, 0, 2 / (r - l));
        ortho.setElementAt(0, 3, - (r + l) / (r - l));
        ortho.setElementAt(1, 1, 2 / (t - b));
        ortho.setElementAt(1, 3, - (t + b) / (t - b));
        ortho.setElementAt(2, 2, 2 / (f - n));
        ortho.setElementAt(2, 3, - n / (f - n));
        ortho.setElementAt(3, 3, 1);

        return ortho;
    }
}
