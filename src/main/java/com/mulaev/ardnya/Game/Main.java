package com.mulaev.ardnya.Game;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.mulaev.ardnya.Game.Entity.GrossTerrain;
import com.mulaev.ardnya.Game.Entity.LoadedObject;
import com.mulaev.ardnya.Game.Lights.Point;
import com.mulaev.ardnya.Game.Orient.Arrowhead;
import com.mulaev.ardnya.Game.Parser.OBJFileLoader;
import com.mulaev.ardnya.Game.Util.ControlSampler;
import com.mulaev.ardnya.Game.Util.MyUtil;
import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;

import javax.swing.*;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_LEQUAL;

public class Main extends JFrame implements GLEventListener {
    private GLCanvas myCanvas;
    private Matrix3D pMat;
    private int rendering_program;
    private int proj_loc;
    private int mv_loc;
    private int n_loc;
    private ControlSampler control;
    private Point glob;
    private LoadedObject obj;
    private LoadedObject obj2;
    private LoadedObject obj3;
    private GrossTerrain terrain;

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
        control = new ControlSampler(this);

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
        rendering_program = MyUtil.createShaderProgram("shaders/vert.shader",
                "shaders/frag.shader", this.getClass().getSimpleName(),
                58, 34);
        float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
        pMat = MyUtil.perspective(25.0f, aspect, 1.0f, 1000.0f);
        proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
        mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
        n_loc = gl.glGetUniformLocation(rendering_program, "norm_matrix");
        obj = OBJFileLoader.loadOBJ("res/Buliding.obj").
                convertToLoadedObj(pMat, rendering_program, proj_loc, mv_loc, n_loc, false);
        obj.setTex("textures/space.jpg");
        obj2 = OBJFileLoader.loadOBJ("res/temple.obj").
                convertToLoadedObj(pMat, rendering_program, proj_loc, mv_loc, n_loc,false);
        obj2.setTex("textures/template.png");
        obj3 = OBJFileLoader.loadOBJ("res/man.obj").
                convertToLoadedObj(pMat, rendering_program, proj_loc, mv_loc, n_loc, false);
        obj3.setTex("textures/pattern.jpg");
        terrain = new GrossTerrain(7, new Point3D(0,-2.0,0),
                pMat, rendering_program, proj_loc, mv_loc, n_loc);
        glob = new Point(rendering_program);

        myCanvas.addKeyListener(control.getOptionListener(obj));
        myCanvas.addKeyListener(control.getMotionListener());
        myCanvas.addMouseMotionListener(control.getRotationListener(this));
        myCanvas.addMouseListener(control.getRotationEndListener(this));
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {
        animator.stop(); // stops the animator after jframe disposal
    }

    public void display(GLAutoDrawable glAutoDrawable) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        Matrix3D lookAt = MyUtil.lookAt(
                control.getCameraPos(), control.getCameraPos().add(control.getDirection()), control.getUp());
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(rendering_program);

        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);
        gl.glEnable(GL_CULL_FACE);
        //gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        //glob.installLights(lookAt);

        obj.setLookAt(lookAt);
        // build object's MV matrix
        obj.translate(0.0, 0.3, -20.0);
        // add additional matrix for object's rotation and scale
        obj.scale(.15, .15, .15);
        obj.draw();

        //obj2.setStack(obj.getStack(true));
        obj2.setLookAt(lookAt);
        obj2.rotate(0.0, 180.0, 0.0);
        obj2.translate(0.0, -2.0, 200.0);
        obj2.scale(1.5, 1.5, 1.5);
        obj2.draw();

        obj3.setLookAt(lookAt);
        obj3.rotate(0.0, 180.0, 0.0);
        obj3.translate(0.0, 0.0, 150.0);
        obj3.scale(0.15, 0.15, 0.15);
        obj3.draw();

        terrain.draw(lookAt);

        // pendulum sama
        Arrowhead.draw(this, pMat, control.getPitch(), control.getYaw());
    }

    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {

    }
}
