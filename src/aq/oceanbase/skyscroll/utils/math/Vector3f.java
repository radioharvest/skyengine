package aq.oceanbase.skyscroll.utils.math;

import android.opengl.Matrix;
import android.util.Log;

public class Vector3f {
    public float x;
    public float y;
    public float z;

    private float length = -1;

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

    public float[] toArray3f() {
        return new float[] {this.x, this.y, this.z};
    }

    public float[] toArray4f() {
        return new float[] {this.x, this.y, this.z, 1.0f};
    }

    public void print(String tag, String name) {
        Log.e(tag, new StringBuilder().append(name + ": X: ").append(this.x).append(" Y: ").append(this.y).append(" Z: ").append(this.z).toString());
    }

    public boolean nonZero() {
        if (x != 0.0f || y!= 0.0f || z!= 0.0f) return true;
        else return false;
    }

    public void calculateLength() {
        this.length = (float) Math.sqrt( this.lengthSqr() );
    }

    public float length() {
        if (this.length == -1) calculateLength();
        return this.length;
    }

    public float lengthSqr() {
        return (this.x*this.x + this.y*this.y + this.z*this.z);
    }

    public Vector3f normalize() {
        if (this.length == -1) calculateLength();
        return new Vector3f(this.x/this.length, this.y/this.length, this.z/this.length);
    }

    public Vector3f addV(Vector3f op) {
        return new Vector3f(this.x + op.x, this.y + op.y, this.z + op.z);
    }

    public Vector3f subtractV(Vector3f op) {
        return new Vector3f(this.x - op.x, this.y - op.y, this.z - op.z);
    }

    public Vector3f multiplySf(float scalar) {
        return new Vector3f(this.x*scalar, this.y*scalar, this.z*scalar);
    }

    public float dotV(Vector3f op) {
        return (this.x*op.x + this.y*op.y + this.z*op.z);
    }

    public float projectionLengthOnV(Vector3f op) {
        return this.dotV(op.normalize());
    }

    public float projectionLengthOnNormV(Vector3f op) {
        return this.dotV(op);
    }

    public Vector3f projectOnV(Vector3f op) {
        return projectOnNormV(op.normalize());
    }

    public Vector3f projectOnNormV(Vector3f op) {
        return op.multiplySf( this.projectionLengthOnNormV(op) );
    }

    public Vector3f crossV(Vector3f op) {
        return new Vector3f( this.y*op.z - this.z*op.y,
                             this.z*op.x - this.x*op.z,
                             this.x*op.y - this.y*op.x );
    }

    public Vector3f reverse() {
        return new Vector3f(-this.x, -this.y, -this.z);
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

        return new Vector3f(outVec[0], outVec[1], outVec[2]);
    }


    public boolean equalsV(Vector3f inp) {
        return ( (this.x == inp.x) && (this.y == inp.y) && (this.z == inp.z) );
    }

    public boolean isZero() {
        if (this.x == 0.0f && this.y == 0.0f && this.z == 0.0f)
            return true;
        else return false;
    }


    public static Vector3f getZero() {
        return new Vector3f(0.0f, 0.0f, 0.0f);
    }
}
