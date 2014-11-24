package aq.oceanbase.skyscroll.graphics;

import android.opengl.Matrix;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

/**
 * This class is used to store camera information:
 * pos, dir and up vectors as well as view and projection matrices
 */

public class Camera {
    private Vector3f mPos;
    private Vector3f mDir;
    private Vector3f mUp;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    public Camera (Vector3f position, Vector3f direction, Vector3f up) {
        this.mPos = position;
        this.mDir = direction;
        this.mUp = up;
        this.updateCamera();
    }

    public Camera (Camera orig) {
        this.mPos = orig.mPos;
        this.mDir = orig.mDir;
        this.mUp = orig.mUp;
        this.mViewMatrix = orig.mViewMatrix;
        this.mProjectionMatrix = orig.mProjectionMatrix;
    }


    //<editor-fold desc="Getters and Setters">
    public Vector3f getPos() {
        return new Vector3f(this.mPos);
    }

    public Vector3f getDir() {
        return new Vector3f(mDir);
    }

    public Vector3f getUp() {
        return new Vector3f(mUp);
    }

    public float getPosX() {
        return this.mPos.x;
    }

    public float getPosY() {
        return this.mPos.y;
    }

    public float getPosZ() {
        return this.mPos.z;
    }

    public Camera setPos(Vector3f pos) {
        this.mPos = pos;
        return this;
    }

    public Camera setDir(Vector3f dir) {
        this.mDir = dir;
        return this;
    }

    public Camera setUp(Vector3f up) {
        this.mUp = up;
        return this;
    }

    public void setPosX(float x) {
        this.mPos.x = x;
    }

    public void setPosY(float y) {
        this.mPos.y = y;
    }

    public void setPosZ(float z) {
        this.mPos.z = z;
    }


    public void setProjM(float[] projMatrix) {
        this.mProjectionMatrix = projMatrix;
    }

    public float[] getProjM() {
        return this.mProjectionMatrix;
    }

    public float[] getViewM() {
        return this.mViewMatrix;
    }
    //</editor-fold>


    public void updateCamera () {
        Matrix.setLookAtM(mViewMatrix, 0,
                mPos.x, mPos.y, mPos.z,
                mDir.x, mDir.y, mDir.z,
                mUp.x, mUp.y, mUp.z);
    }
}
