package aq.oceanbase.skyscroll.math;

import android.opengl.Matrix;
import android.util.Log;

public class Vector3f {
    public float x;
    public float y;
    public float z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f(Vector3f vect) {
        this.x = vect.x;
        this.y = vect.y;
        this.z = vect.z;
    }

    public void print(String tag, String name) {
        Log.e(tag, new StringBuilder().append(name + ": X: ").append(this.x).append(" Y: ").append(this.y).append(" Z: ").append(this.z).toString());
    }

    public float[] toArray() {
        return new float[] {x, y, z};
    }

    public boolean nonZero() {
        if (x != 0.0f || y!= 0.0f || z!= 0.0f) return true;
        else return false;
    }

    public Vector3f subtractV(Vector3f op) {
        this.x = this.x - op.x;
        this.y = this.y - op.y;
        this.z = this.z - op.z;

        return this;
    }

    public Vector3f addV(Vector3f op) {
        this.x = this.x + op.x;
        this.y = this.y + op.y;
        this.z = this.z + op.z;

        return this;
    }

    public Vector3f rotate(float a, float origX, float origY, float origZ) {
        float[] rotationMatrix = new float[16];
        float[] inVec = new float[4];
        float[] outVec = new float[4];

        inVec[0] = this.x;
        inVec[1] = this.y;
        inVec[2] = this.z;
        inVec[3] = 1.0f;

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, a, origX, origY, origZ);
        Matrix.multiplyMV(outVec, 0, rotationMatrix, 0, inVec, 0);

        this.x = outVec[0];
        this.y = outVec[1];
        this.z = outVec[2];

        return this;
    }
}
