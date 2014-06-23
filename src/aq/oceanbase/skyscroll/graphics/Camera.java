package aq.oceanbase.skyscroll.graphics;

import android.opengl.Matrix;
import aq.oceanbase.skyscroll.math.Vector3f;

/**
 * This class is used to store camera information:
 * pos, dir and up vectors as well as view and projection matrices
 */

public class Camera {
    private Vector3f pos;
    private Vector3f dir;
    private Vector3f up;

    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];

    public Camera (Vector3f position, Vector3f direction, Vector3f up) {
        this.pos = position;
        this.dir = direction;
        this.up = up;
        this.updateCamera();
    }


    //<editor-fold desc="Getters and Setters">
    public Vector3f getPos() {
        return this.pos;
    }

    public Vector3f getDir() {
        return dir;
    }

    public Vector3f getUp() {
        return up;
    }

    public void setPos(Vector3f pos) {
        this.pos = pos;
    }

    public void setDir(Vector3f dir) {
        this.dir = dir;
    }

    public void setUp(Vector3f up) {
        this.up = up;
    }


    public void setProjM(float[] projMatrix) {
        this.projectionMatrix = projMatrix;
    }

    public float[] getProjM() {
        return this.projectionMatrix;
    }

    public float[] getViewM() {
        return this.viewMatrix;
    }
    //</editor-fold>


    public void updateCamera () {
        Matrix.setLookAtM(viewMatrix, 0,
                pos.x, pos.y, pos.z,
                dir.x, dir.y, dir.z,
                up.x, up.y, up.z);
    }
}
