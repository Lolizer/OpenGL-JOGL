package com.mulaev.ardnya.Game;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;

import javax.swing.*;

import java.nio.FloatBuffer;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;

import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static graphicslib3D.GLSLUtils.checkOpenGLError;
import static graphicslib3D.GLSLUtils.printProgramLog;
import static graphicslib3D.GLSLUtils.printShaderLog;
import static graphicslib3D.GLSLUtils.readShaderSource;


public class Main extends JFrame implements GLEventListener {
    private GLCanvas myCanvas;
    private int rendering_program;
    private int vao[] = new int[1];
    private int vbo[ ] = new int[2];
    private float cameraX, cameraY, cameraZ;
    private float cubeLocX, cubeLocY, cubeLocZ;
    private GLSLUtils util = new GLSLUtils();
    private Matrix3D pMat;
    private FPSAnimator animator;

    public Main(){
        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);

        setTitle("Game");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        myCanvas = new GLCanvas(caps);
        myCanvas.addGLEventListener(this);
        this.add(myCanvas);
        setVisible(true);

        animator = new FPSAnimator(myCanvas, 60);
        animator.start();
    }

    public static void main(String[] args) {
        new Main();
    }

    public void init(GLAutoDrawable glAutoDrawable) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        rendering_program = createShaderProgram(glAutoDrawable);
        setupVertices();
        cameraX = 0.0f; cameraY = 0.0f; cameraZ = 1000.0f;
        cubeLocX = 0.0f; cubeLocY = -2.0f; cubeLocZ = -4.0f;
        float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
        pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {
        animator.stop(); // stops the animator after jframe disposal
    }

    public void display(GLAutoDrawable glAutoDrawable) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        double t = (double) (System.currentTimeMillis() % 3600000) / 10000.0;
        gl.glClear(GL_DEPTH_BUFFER_BIT);

        float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
        FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
        gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

        gl.glUseProgram(rendering_program);

        // build view matrix
        Matrix3D vMat = new Matrix3D();
        vMat.translate(-cameraX,-cameraY,-cameraZ);

        // build model matrix
        Matrix3D mMat = new Matrix3D();

        int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
        int v_loc = gl.glGetUniformLocation(rendering_program, "v_matrix");
        int m_loc = gl.glGetUniformLocation(rendering_program, "m_matrix");
        int t_loc = gl.glGetUniformLocation(rendering_program, "t");

        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(v_loc, 1, false, vMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(m_loc, 1, false, mMat.getFloatValues(), 0);
        gl.glUniform1f(t_loc, (float)t);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);

        //gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        gl.glDrawArraysInstanced(GL_TRIANGLES, 0, 36, 100000);
    }

    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {

    }

    private int createShaderProgram(GLAutoDrawable glAutoDrawable) {
        int[ ] vertCompiled = new int[1];
        int[ ] fragCompiled = new int[1];
        int[ ] linked = new int[1];
        GL4 gl = (GL4) GLContext.getCurrentGL();
        String vshaderSource[] = readShaderSource("shaders/vert.shader");
        String fshaderSource[] = readShaderSource("shaders/frag.shader");

        int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
        gl.glShaderSource(vShader, 69, vshaderSource, null, 0); // note: 3 lines of code
        gl.glCompileShader(vShader);

        checkOpenGLError();
        gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
        if (vertCompiled[0] == 1)
        { System.out.println(". . . vertex compilation success.");
        } else
        { System.out.println(". . . vertex compilation failed.");
            printShaderLog(vShader);
        }

        int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
        gl.glShaderSource(fShader, 11, fshaderSource, null, 0); // note: 4 lines of code
        gl.glCompileShader(fShader);

        checkOpenGLError();
        gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
        if (fragCompiled[0] == 1)
        { System.out.println(". . . frag compilation success.");
        } else
        { System.out.println(". . . frag compilation failed.");
            printShaderLog(fShader);
        }

        int vfprogram = gl.glCreateProgram();
        gl.glAttachShader(vfprogram, vShader);
        gl.glAttachShader(vfprogram, fShader);
        gl.glLinkProgram(vfprogram);

        checkOpenGLError();
        gl.glGetProgramiv(vfprogram, GL_LINK_STATUS, linked,0);
        if (linked[0] == 1)
        { System.out.println(". . . linking succeeded.");
        } else
        { System.out.println(". . . linking failed.");
            printProgramLog(vfprogram);
        }

        gl.glDeleteShader(vShader);
        gl.glDeleteShader(fShader);
        return vfprogram;
    }

    private Matrix3D perspective(float fovy, float aspect, float n, float f) {
        float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
        float A = q / aspect;
        float B = (n + f) / (n - f);
        float C = (2.0f * n * f) / (n - f);
        Matrix3D r = new Matrix3D();
        r.setElementAt(0,0, A);
        r.setElementAt(1,1, q);
        r.setElementAt(2,2, B);
        r.setElementAt(3,2, -1.0f);
        r.setElementAt(2,3, C);
        r.setElementAt(3,3, 0.0f);
        return r;
    }

    private void setupVertices() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        float[ ] vertex_positions =
                {
                        -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f,
                        -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
                        -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f,
                        -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                        1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f
                };

        gl.glGenVertexArrays(vao.length, vao, 0);
        gl.glBindVertexArray(vao[0]);
        gl.glGenBuffers(vbo.length, vbo, 0);
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(vertex_positions);
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf,
                GL_STATIC_DRAW);
    }
}
