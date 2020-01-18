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
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;
import graphicslib3D.MatrixStack;
import graphicslib3D.Vector3D;
import graphicslib3D.Vertex3D;

import javax.swing.*;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.nio.FloatBuffer;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
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
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;


public class Main extends JFrame implements GLEventListener {
    private GLCanvas myCanvas;
    private Matrix3D pMat;
    private MatrixStack scene;
    private int rendering_program;
    private int vao[] = new int[1];
    private int vbo[ ] = new int[3];
    private float lastX, lastY;
    private float pitch, yaw;
    private Vector3D up;
    private Vector3D cameraPos;
    private Vector3D direction;
    private float sunX, sunY, sunZ;
    private float earthX, earthY, earthZ;
    private float moonX, moonY, moonZ;
    private Sphere mySphere;

    private GLSLUtils util;
    private FPSAnimator animator;

    public Main(){
        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);

        setTitle("Game");
        setSize(800, 600);
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        myCanvas = new GLCanvas(caps);
        myCanvas.addGLEventListener(this);

        myCanvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                float camSpeed = 1.5f;
                Vector3D directionOffset = direction.mult(camSpeed);
                Vector3D sidewaysOffset = direction.cross(up).normalize().mult(camSpeed * 0.75);
                if (e.getKeyCode() == KeyEvent.VK_W)
                    cameraPos = cameraPos.add(directionOffset);
                if (e.getKeyCode() == KeyEvent.VK_S)
                    cameraPos = cameraPos.minus(directionOffset);
                if (e.getKeyCode() == KeyEvent.VK_A)
                    cameraPos = cameraPos.minus(sidewaysOffset);
                if (e.getKeyCode() == KeyEvent.VK_D)
                    cameraPos = cameraPos.add(sidewaysOffset);
            }
        });

        myCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

                int xOnScreen = e.getX();
                int yOnScreen = e.getY();

                if (Math.abs(lastX - getWidth() / 2) < 0.01 && Math.abs(lastY - getHeight() / 2) < 0.01) {
                    lastX = xOnScreen;
                    lastY = yOnScreen;
                }

                float sensibility = 0.2f;
                float xoffset = xOnScreen - lastX;
                float yoffset = lastY - yOnScreen;

                lastX = xOnScreen;
                lastY = yOnScreen;

                if (!SwingUtilities.isMiddleMouseButton(e))
                    return;

                xoffset *= sensibility;
                yoffset *= sensibility;

                yaw += xoffset;
                pitch += yoffset;

                if(pitch > 89.0f)
                    pitch =  89.0f;
                if(pitch < -89.0f)
                    pitch = -89.0f;

                direction.setX(Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
                direction.setY(Math.sin(Math.toRadians(pitch)));
                direction.setZ(Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
                direction.normalize();
            }
        });

        myCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    lastX = getWidth() / 2;
                    lastY = getHeight() /2;
                }
            }
        });

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
        util = new GLSLUtils();
        rendering_program = createShaderProgram(glAutoDrawable);
        setupVertices();
        mySphere = new Sphere(gl, 64);
        cameraPos = new Vector3D(.0, .0, 30.0);
        direction = new Vector3D(.0, .0, -1.0);
        up = new Vector3D(.0, 1.0, .0);
        lastX = getWidth() / 2; lastY = getHeight() / 2;
        yaw = -90.0f;
        sunX = 0.0f; sunY = 0.0f; sunZ = 0.0f;
        earthX = 8.0f; earthY = 0.0f; earthZ = 0.0f;
        moonX = 2.0f; moonY = 4.0f; moonZ = 0.0f;
        float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
        pMat = perspective(25.0f, aspect, 1.0f, 1000.0f);
        scene = new MatrixStack(20);
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {
        animator.stop(); // stops the animator after jframe disposal
    }

    public void display(GLAutoDrawable glAutoDrawable) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
        int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
        double amt = (double)(System.currentTimeMillis()) / 1000.0;
        gl.glClear(GL_DEPTH_BUFFER_BIT);

        FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(new float[] { 0.0f, 0.0f, 0.0f, 1.0f });
        gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

        gl.glUseProgram(rendering_program);

        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);
        gl.glEnable(GL_CULL_FACE);
        //gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        // pendulum sama
        Arrowhead.draw(this, gl, vao, pMat, proj_loc, mv_loc, pitch, yaw);

        // build view matrix
        scene.pushMatrix();
        scene.multMatrix(lookAt(cameraPos, cameraPos.add(direction), up));

        // build sun's MV matrix
        scene.pushMatrix();
        scene.translate(sunX, sunY, sunZ);

        // add additional matrix for sun's rotation
        scene.pushMatrix();
        scene.rotate(amt * 100.0,1.0,1.0,1.0);
        scene.scale(1.25, 1.25, 1.25);

        gl.glUniformMatrix4fv(proj_loc, 1, false,
                pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(mv_loc, 1, false,
                scene.peek().getFloatValues(), 0);

        mySphere.draw();
        // remove sun's rotation
        scene.popMatrix();
        // build earth's mv
        scene.pushMatrix();
        scene.translate(- cos(amt)*10.0f, 0.0f, sin(amt)*10.0f);
        //scene.translate(earthX, earthY, earthZ);

        // earth's rotation
        scene.pushMatrix();
        scene.rotate(System.currentTimeMillis() / 10.0,
                1.0,1.0,1.0);
        scene.scale(0.75, 0.75, 0.75);

        gl.glUniformMatrix4fv(mv_loc, 1,
                false, scene.peek().getFloatValues(), 0);

        mySphere.draw();
        // remove earth's rotation
        scene.popMatrix();

        // build moon's mv
        scene.pushMatrix();
        scene.translate(sin(amt) * 1.5f, sin(amt) * 0.8f,
                cos(amt) * 1.5f);
        //scene.translate(moonX, moonY, moonZ);
        scene.rotate(amt * 10.0,
                1.0,1.0,1.0);

        scene.scale(0.25, 0.25, 0.25);

        gl.glUniformMatrix4fv(mv_loc, 1,
                false, scene.peek().getFloatValues(), 0);

        mySphere.draw();

        // remove moon's mv
        scene.popMatrix();

        // remove earth's, sun's, view's translations
        scene.popMatrix(); scene.popMatrix(); scene.popMatrix();
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
        gl.glShaderSource(vShader, 12, vshaderSource, null, 0); // note: 3 lines of code
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
        float q = 1.0f / ((float) Math.tan(toRadians(0.5f * fovy)));
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

        float[] pyramid_positions =
                {
                        -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                        1.0f, -1.0f, 1.0f,1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                        1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                        -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                        -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f,
                        1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f
                };
        float[] cube_positions =
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

        // global vao
        gl.glGenVertexArrays(vao.length, vao, 0);
        gl.glBindVertexArray(0);
    }

    private Matrix3D lookAt(Vector3D eye, Vector3D target, Vector3D y) {
        Vector3D fwd = (target.minus(eye)).normalize();
        Vector3D side = (fwd.cross(y)).normalize();
        Vector3D up = (side.cross(fwd)).normalize();

        Matrix3D look = new Matrix3D();
        look.setElementAt(0,0, side.getX());
        look.setElementAt(1,0, up.getX());
        look.setElementAt(2,0, -fwd.getX());
        look.setElementAt(3,0, 0.0f);
        look.setElementAt(0,1, side.getY());
        look.setElementAt(1,1, up.getY());
        look.setElementAt(2,1, -fwd.getY());
        look.setElementAt(3,1, 0.0f);
        look.setElementAt(0,2, side.getZ());
        look.setElementAt(1,2, up.getZ());
        look.setElementAt(2,2, -fwd.getZ());
        look.setElementAt(3,2, 0.0f);
        look.setElementAt(0,3, side.dot(eye.mult(-1)));
        look.setElementAt(1,3, up.dot(eye.mult(-1)));
        look.setElementAt(2,3, (fwd.mult(-1)).dot(eye.mult(-1)));
        look.setElementAt(3,3, 1.0f);
        return look;
    }

    public Texture loadTexture(String textureFileName) {
        Texture tex = null;
        try { tex = TextureIO.newTexture(new File(textureFileName), false); }
        catch (Exception e) { e.printStackTrace(); }
        return tex;
    }
}
