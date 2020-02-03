package com.mulaev.ardnya.Game.Lights;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.light.PositionalLight;

public class Point {
    private static float [ ] globalAmbient;
    private PositionalLight currentLight;
    private Point3D lightLoc;
    private int rendering_program;
    private int ambLoc;
    private int diffLoc;
    private int specLoc;
    private int posLoc;

    public Point(int rendering_program) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        setGlobal(null);
        this.rendering_program = rendering_program;
        currentLight = new PositionalLight();
        lightLoc = new Point3D(10.0f, 10.0f, 10.0f);
        currentLight.setPosition(lightLoc);

        // get the locations of the light and material fields in the shader
        ambLoc = gl.glGetUniformLocation(rendering_program, "light.ambient");
        diffLoc = gl.glGetUniformLocation(rendering_program, "light.diffuse");
        specLoc = gl.glGetUniformLocation(rendering_program,
                "light.specular");
        posLoc = gl.glGetUniformLocation(rendering_program, "light.position");
    }

    private void installGlobal() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        // set the current globalAmbient settings
        int globalAmbLoc = gl.glGetUniformLocation(rendering_program,
                "globalAmbient");
        gl.glProgramUniform4fv(rendering_program, globalAmbLoc, 1, globalAmbient,
                0);
    }

    public static void setGlobal(float[] globalAmbient) {
        if (globalAmbient != null)
            Point.globalAmbient = globalAmbient;
        else Point.globalAmbient = new float[ ] { 0.7f, 0.7f, 0.7f, 1.0f };
    }

    public void installLights(Matrix3D v_matrix, int rendering_program)
    {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        installGlobal();
// convert light’s position to view space, and save it in a float array
        Point3D lightP = currentLight.getPosition();
        Point3D lightPv = lightP.mult(v_matrix);
        float [ ] viewspaceLightPos =
                new float[ ] { (float) lightPv.getX(), (float) lightPv.getY(),
                        (float) lightPv.getZ() };

// set the uniform light and material values in the shader
        gl.glProgramUniform4fv(rendering_program, ambLoc, 1,
                currentLight.getAmbient(), 0);
        gl.glProgramUniform4fv(rendering_program, diffLoc, 1,
                currentLight.getDiffuse(), 0);
        gl.glProgramUniform4fv(rendering_program, specLoc, 1,
                currentLight.getSpecular(), 0);
        gl.glProgramUniform3fv(rendering_program, posLoc, 1,
                viewspaceLightPos,0);
    }
}
