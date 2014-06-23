package aq.oceanbase.skyscroll.graphics;

import android.opengl.Matrix;
import aq.oceanbase.skyscroll.math.Vector3f;

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

    public float[] getViewM() {
        return this.viewMatrix;
    }

    public float[] getProjM() {
        return this.projectionMatrix;
    }

    public void updateCamera () {
        Matrix.setLookAtM(viewMatrix, 0,
                pos.x, pos.y, pos.z,
                dir.x, dir.y, dir.z,
                up.x, up.y, up.z);
    }
}
