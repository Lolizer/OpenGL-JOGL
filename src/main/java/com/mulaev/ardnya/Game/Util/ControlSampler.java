package com.mulaev.ardnya.Game.Util;

import com.mulaev.ardnya.Game.Entity.LoadedObject;
import graphicslib3D.Vector3D;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

public class ControlSampler {
    private Vector3D up;
    private Vector3D cameraPos;
    private Vector3D direction;
    private float lastX, lastY;
    private float pitch, yaw;
    private long lastRCTime;
    private long lastMCTime;

    public ControlSampler(JFrame frame) {
        cameraPos = new Vector3D(0.0, 3.0, 30.0);
        direction = new Vector3D(.0, .0, -1.0);
        up = new Vector3D(.0, 1.0, .0);
        lastX = frame.getWidth() / 2; lastY = frame.getHeight() / 2;
        yaw = -90.0f;
    }

    public KeyListener getOptionListener(LoadedObject obj) {
        return new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_R)
                    obj.setCull(!obj.getCull());
                if (e.getKeyCode() == KeyEvent.VK_T)
                    obj.setPoly(!obj.getPoly());
            }
        };
    }

    public KeyListener getMotionListener() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                if (!(key == KeyEvent.VK_W || key == KeyEvent.VK_S
                        || key == KeyEvent.VK_A || key == KeyEvent.VK_D))
                    return;

                long current;
                if ((current = System.nanoTime()) - lastMCTime < 25000000)
                    return;
                else lastMCTime = current;

                float camSpeed = 1.0f;
                float x, y, z, mag;

                switch (key) {
                    case KeyEvent.VK_W: {
                        Vector3D directionOffset = direction.mult(camSpeed);

                        x = (float) cameraPos.getX();
                        y = (float) cameraPos.getY();
                        z = (float) cameraPos.getZ();

                        cameraPos.setX(x + directionOffset.getX());
                        cameraPos.setY(y + directionOffset.getY());
                        cameraPos.setZ(z + directionOffset.getZ());
                    } break;
                    case KeyEvent.VK_S: {
                        Vector3D directionOffset = direction.mult(camSpeed);

                        x = (float) cameraPos.getX();
                        y = (float) cameraPos.getY();
                        z = (float) cameraPos.getZ();

                        cameraPos.setX(x - directionOffset.getX());
                        cameraPos.setY(y - directionOffset.getY());
                        cameraPos.setZ(z - directionOffset.getZ());
                    } break;
                    case KeyEvent.VK_A: {
                        Vector3D sidewaysOffset = direction.cross(up);
                        mag = (float) sidewaysOffset.magnitude();
                        sidewaysOffset.setX((float) ((sidewaysOffset.getX() / mag) * camSpeed * 0.75));
                        sidewaysOffset.setY((float) ((sidewaysOffset.getY() / mag) * camSpeed * 0.75));
                        sidewaysOffset.setZ((float) ((sidewaysOffset.getZ() / mag) * camSpeed * 0.75));

                        x = (float) cameraPos.getX();
                        y = (float) cameraPos.getY();
                        z = (float) cameraPos.getZ();

                        cameraPos.setX(x - sidewaysOffset.getX());
                        cameraPos.setY(y - sidewaysOffset.getY());
                        cameraPos.setZ(z - sidewaysOffset.getZ());
                    } break;
                    case KeyEvent.VK_D: {
                        Vector3D sidewaysOffset = direction.cross(up);
                        mag = (float) sidewaysOffset.magnitude();
                        sidewaysOffset.setX((float) ((sidewaysOffset.getX() / mag) * camSpeed * 0.75));
                        sidewaysOffset.setY((float) ((sidewaysOffset.getY() / mag) * camSpeed * 0.75));
                        sidewaysOffset.setZ((float) ((sidewaysOffset.getZ() / mag) * camSpeed * 0.75));

                        x = (float) cameraPos.getX();
                        y = (float) cameraPos.getY();
                        z = (float) cameraPos.getZ();

                        cameraPos.setX(x + sidewaysOffset.getX());
                        cameraPos.setY(y + sidewaysOffset.getY());
                        cameraPos.setZ(z + sidewaysOffset.getZ());
                    } break;
                }
            }
        };
    }

    public MouseMotionListener getRotationListener(JFrame frame) {
        return new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!SwingUtilities.isMiddleMouseButton(e))
                    return;

                int xOnScreen = e.getX();
                int yOnScreen = e.getY();

                if (Math.abs(lastX - frame.getWidth() / 2) < 0.01 && Math.abs(lastY - frame.getHeight() / 2) < 0.01) {
                    lastX = xOnScreen;
                    lastY = yOnScreen;
                } else if (Math.abs(xOnScreen - lastX) < 0.01 && Math.abs(yOnScreen - lastY) < 0.01)
                    return;

                float sensibility = 0.2f;
                float xoffset = xOnScreen - lastX;
                float yoffset = lastY - yOnScreen;

                lastX = xOnScreen;
                lastY = yOnScreen;

                xoffset *= sensibility;
                yoffset *= sensibility;

                yaw += xoffset;
                pitch += yoffset;

                if (yaw > 359.9f || yaw < -359.9f)
                    yaw = 0.0f;

                if(pitch > 89.0f)
                    pitch =  89.0f;
                if(pitch < -89.0f)
                    pitch = -89.0f;

                double x, y, z;
                double mag;

                x = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
                y = Math.sin(Math.toRadians(pitch));
                z = Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));

                mag = Math.sqrt(x * x + y * y + z * z);

                direction.setX(x / mag);
                direction.setY(y / mag);
                direction.setZ(z / mag);
            }
        };
    }

    public MouseListener getRotationEndListener(JFrame frame) {
        return new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    lastX = frame.getWidth() / 2;
                    lastY = frame.getHeight() /2;
                }
            }
        };
    }

    public Vector3D getUp() { return up; }
    public Vector3D getCameraPos() { return cameraPos; }
    public Vector3D getDirection() { return direction; }
    public float getLastX() { return lastX; }
    public float getLastY() { return lastY; }
    public float getPitch() { return pitch; }
    public float getYaw() { return yaw; }
}
