package com.mulaev.ardnya.Game.Entity;

import graphicslib3D.MatrixStack;

public class ModelStack extends MatrixStack implements Cloneable {
    private int stackCount;

    public ModelStack(int maxSize) {
        super(maxSize);
    }

    @Override
    public void pushMatrix() {
        super.pushMatrix();
        stackCount++;
    }

    @Override
    public void popMatrix() {
        super.popMatrix();
        stackCount--;
    }

    @Override
    public ModelStack clone() {
        ModelStack model = null;
        try {
            model = (ModelStack) super.clone();
        } catch (CloneNotSupportedException cnse) {
            cnse.printStackTrace();
            System.exit(1);
        }
        return model;
    }

    public int getStackCount() { return stackCount; }
}
