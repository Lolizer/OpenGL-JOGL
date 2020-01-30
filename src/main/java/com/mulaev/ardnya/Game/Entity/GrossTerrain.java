package com.mulaev.ardnya.Game.Entity;

import com.mulaev.ardnya.Game.Parser.OBJFileLoader;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

public class GrossTerrain {
    private LoadedObject inst;
    private ArrayList<Pair<Float, Float>> coords;
    private Point3D pos;

    public GrossTerrain(int len, Point3D pos, Matrix3D pMat, int proj_loc, int mv_loc) {
        this.pos = pos;
        coords = new ArrayList<Pair<Float, Float>>();
        inst = OBJFileLoader.loadOBJ("res/gross.obj").
                convertToLoadedObj(pMat, proj_loc, mv_loc, false);

        inst.scale(5.2, 1.0, 7.2);
        inst.setTex("textures/gross.jpg");

        int div = len / 2;
        float offset = 200.0f;

        for (int i = 0; i < div + 1; i++) { // верт
            for (int j = 0; j < div + 1; j++) { // горизонт
                // верх (слева и справа)
                if (!(j == div && len % 2 == 0))
                    if (j != 0)
                        add(i * offset, -j * offset);
                add(i * offset, j * offset);
                // низ (слева и справа)
                if (!(i == div && len % 2 == 0))
                    if (!(i == j && i == 0)) {
                        if (!(j == div && len % 2 == 0))
                            if (j != 0)
                                add(-i * offset, -j * offset);
                        add(-i * offset, j * offset);
                    }
            }
        }
    }

    public void add(float zOffset, float xOffset) {
        coords.add(Pair.of(xOffset, zOffset));
    }

    public void draw(Matrix3D lookAt) {
        inst.setLookAt(lookAt);

        for (Pair<Float, Float> pair : coords) {
            inst.translate(pos.getX() + pair.getKey(),
                    pos.getY() + 0.0, pos.getZ() + pair.getValue());
            inst.draw();
        }
    }

    public Point3D getPose() {
        return pos;
    }
}
