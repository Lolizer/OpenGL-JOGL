package com.mulaev.ardnya.Game.Util;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;

import java.io.File;

import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static graphicslib3D.GLSLUtils.checkOpenGLError;
import static graphicslib3D.GLSLUtils.printProgramLog;
import static graphicslib3D.GLSLUtils.printShaderLog;
import static graphicslib3D.GLSLUtils.readShaderSource;
import static java.lang.Math.toRadians;

public class MyUtil {
    public static Texture loadTexture(String textureFileName) {
        Texture tex = null;

        try {
            tex = TextureIO.newTexture(new File(textureFileName), false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tex;
    }

    public static int createShaderProgram(
            String vShaderPath, String fShaderPath, String loc, int vlength, int flength) {
        int[ ] vertCompiled = new int[1];
        int[ ] fragCompiled = new int[1];
        int[ ] linked = new int[1];
        GL4 gl = (GL4) GLContext.getCurrentGL();
        String vshaderSource[] = readShaderSource(vShaderPath);
        String fshaderSource[] = readShaderSource(fShaderPath);
        /*String vshaderSource[] = readShaderSource("shaders/vert.shader");
        String fshaderSource[] = readShaderSource("shaders/frag.shader");*/

        int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
        gl.glShaderSource(vShader, vlength, vshaderSource, null, 0); // note: 3 lines of code
        gl.glCompileShader(vShader);

        System.out.println(loc);

        checkOpenGLError();
        gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
        if (vertCompiled[0] == 1)
        { System.out.println(". . . vertex compilation success.");
        } else
        { System.out.println(". . . vertex compilation failed.");
            printShaderLog(vShader);
        }

        int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
        gl.glShaderSource(fShader, flength, fshaderSource, null, 0); // note: 4 lines of code
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

    public static Matrix3D perspective(float fovy, float aspect, float n, float f) {
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

    public static Matrix3D lookAt(Vector3D eye, Vector3D target, Vector3D y) {
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
}
