package com.mulaev.ardnya.Game.Entity;

import com.mulaev.ardnya.Game.Parser.OBJFileLoader;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;

import java.util.ArrayList;

public class GrossTerrain {
    private ArrayList<LoadedObject> plates;
    private Point3D pos;

    public GrossTerrain(int len, Point3D pos, Matrix3D pMat, int proj_loc, int mv_loc) {
        this.pos = pos;
        plates = new ArrayList<>();
        LoadedObject inst = OBJFileLoader.loadOBJ("res/gross.obj").
                convertToLoadedObj(pMat, proj_loc, mv_loc, false);
        int div = len / 2;
        float offset = 200.0f;

        inst.eraseData();

        for (int i = 0; i < div + 1; i++) { // верт
            for (int j = 0; j < div + 1; j++) { // горизонт
                // верх (слева и справа)
                if (!(j == div && len % 2 == 0))
                    if (j != 0)
                        add(inst.clone(), pos, i * offset, -j * offset);
                add(inst.clone(), pos,i * offset, j * offset);
                // низ (слева и справа)
                if (!(i == div && len % 2 == 0))
                    if (!(i == j && i == 0)) {
                        if (!(j == div && len % 2 == 0))
                            if (j != 0)
                                add(inst.clone(), pos,-i * offset, -j * offset);
                        add(inst.clone(), pos,-i * offset, j * offset);
                    }
            }
        }
    }

    public void add(LoadedObject inst, Point3D pos, float zOffset, float xOffset) {
        inst.translate(pos.getX() + xOffset, pos.getY() + 0.0, pos.getZ() + zOffset);
        inst.scale(5.2, 1.0, 7.2);
        inst.setTex("textures/gross.jpg");
        plates.add(inst);
    }

    public void draw(Matrix3D lookAt) {
        for (LoadedObject obj : plates) {
            obj.setLookAt(lookAt);
            obj.draw();
        }
    }
}
