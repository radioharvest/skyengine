package aq.oceanbase.skyscroll.engine.input.touch;

import android.opengl.GLU;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;

public class TouchRay {
    private Vector3f mNear;
    private Vector3f mFar;
    private Vector3f mDelta;
    private Vector3f mDeltaNorm;
    private Vector3f mDeltaNormByZ;
    private float mRadius;

    private Vector3f mbBoxMax;    //Bounding Box Up-Right coordinates
    private Vector3f mbBoxMin;    //Bounding Box Down-Left coordinates

    public TouchRay(float nearX, float nearY, float nearZ, float farX, float farY, float farZ, float rad) {
        this.mNear = new Vector3f(nearX, nearY, nearZ);
        this.mFar = new Vector3f(farX, farY, farZ);
        this.mRadius = rad;
        buildRay();
    }

    public TouchRay(Vector3f nearPoint, Vector3f farPoint, float rad) {
        this.mNear = new Vector3f(nearPoint);
        this.mFar = new Vector3f(farPoint);
        this.mRadius = rad;
        buildRay();
    }

    //This constructor builds ray from coordinates to eliminate castTouchRay func from Render
    public TouchRay(float touchX, float touchY, float rad, Camera cam, int[] screenMetrics) {
        float[] result = new float[4];

        float winX = touchX;
        float winY = (float)screenMetrics[3] - touchY;         //screenHeight - touchY

        GLU.gluUnProject(winX, winY, 1.0f, cam.getViewM(), 0, cam.getProjM(), 0, screenMetrics, 0, result, 0);     //get point on the far plane
        Vector3f far = new Vector3f( result[0]/result[3], result[1]/result[3], result[2]/result[3]);    //divide by w-component

        this.mNear = new Vector3f(cam.getPos());
        this.mFar = new Vector3f(far);
        this.mRadius = rad;

        buildRay();
    }

    private void buildRay() {
        mDelta = mFar.subtractV(mNear);
        mDelta.calculateLength();
        mDeltaNormByZ = mDelta.multiplySf(1/Math.abs(mDelta.z));    //normalized xy vect for one unit on z axis
        //deltaNorm = delta.normalize();

        float up = Math.max(mNear.y, mFar.y) + mRadius;
        float down = Math.min(mNear.y, mFar.y) - mRadius;
        float right = Math.max(mNear.x, mFar.x) + mRadius;
        float left = Math.min(mNear.x, mFar.x) - mRadius;
        mbBoxMax = new Vector3f(right, up, mNear.z);      //if Z coord detection will be used reconsidering status of
        mbBoxMin = new Vector3f(left, down, mFar.z);      //max and min Z is needed
    }


    //<editor-fold desc="Getters">
    public float[] getPositionArray() {
        return new float[] {mNear.x, mNear.y, mNear.z, mFar.x, mFar.y, mFar.z};
    }

    public float[] getNearPointF() {
        return new float[] {mNear.x, mNear.y, mNear.z};
    }

    public float[] getFarPointF() {
        return new float[] {mFar.x, mFar.y, mFar.z};
    }

    public Vector3f getNearPointV() {
        return this.mNear;
    }

    public Vector3f getFarPointV() {
        return this.mFar;
    }

    public float getSqrDistTo(Vector3f input) {
        return input.subtractV(this.mNear).lengthSqr();
    }
    //</editor-fold>


    public TouchRay multiplyByMatrix(float[] matrix) {
        float[] rayNear = this.mNear.toArray4f();
        float[] rayFar = this.mFar.toArray4f();

        Matrix.multiplyMV(rayNear, 0, matrix, 0, rayNear, 0);
        Matrix.multiplyMV(rayFar, 0, matrix, 0, rayFar, 0);

        return new TouchRay(rayNear[0], rayNear[1], rayNear[2], rayFar[0], rayFar[1], rayFar[2], this.mRadius);
    }


    public boolean notNull() {
        if (mNear.nonZero() && mFar.nonZero()) return true;
        else return false;
    }

    public Vector3f getPointPositionOnRay(float z) {
        return this.mNear.addV(this.mDeltaNormByZ.multiplySf(z));
    }

    public boolean pointInsideBox(Vector3f point) {
        if ( (mbBoxMin.x <= point.x && point.x <= mbBoxMax.x) &&
             (mbBoxMin.y <= point.y && point.y <= mbBoxMax.y) ) return true;
             //(bBoxMin.z <= point.z && point.z <= bBoxMax.z) ) return true;
        else return false;
    }

    public boolean pointOnRay(Vector3f input) {
        if (!this.pointInsideBox(input)) return false;
        input = input.subtractV(this.mNear);
        float projection = input.projectionLengthOnV(this.mDelta);
        float intersectionSqr = this.mRadius*this.mRadius - input.lengthSqr() + projection*projection;

        if (intersectionSqr >= 0) return true;
        else return false;
    }

    public boolean closestSelected(Vector3f sel, Vector3f vect) {
        if (this.getSqrDistTo(sel) < this.getSqrDistTo(vect)) return true;
        else return false;
    }
}