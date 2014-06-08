package aq.oceanbase.skyscroll.touch;

import android.util.Log;
import aq.oceanbase.skyscroll.math.Vector3f;

public class TouchRay {
    private Vector3f near;
    private Vector3f far;
    private Vector3f delta;
    private Vector3f deltaNorm;
    private float radius;

    private Vector3f bBoxMax;    //Bounding Box Up-Right coordinates
    private Vector3f bBoxMin;    //Bounding Box Down-Left coordinates

    public TouchRay(float nearX, float nearY, float nearZ, float farX, float farY, float farZ, float rad) {
        this.near = new Vector3f(nearX, nearY, nearZ);
        this.far = new Vector3f(farX, farY, farZ);
        this.radius = rad;
        buildRay();
    }

    public TouchRay(Vector3f nearPoint, Vector3f farPoint, float rad) {
        this.near = new Vector3f(nearPoint);
        this.far = new Vector3f(farPoint);
        this.radius = rad;

        //this.near.print("Touch", "created-near");
        //this.far.print("Touch", "created-far");
        buildRay();
    }

    private void buildRay() {
        delta = far.subtractV(near);
        delta.calculateLength();
        deltaNorm = delta.normalize();

        float up = Math.max(near.y, far.y) + radius;
        float down = Math.min(near.y, far.y) - radius;
        float right = Math.max(near.x, far.x) + radius;
        float left = Math.min(near.x, far.x) - radius;
        bBoxMax = new Vector3f(right, up, near.z);      //if Z coord detection will be used reconsidering status of
        bBoxMin = new Vector3f(left, down, far.z);      //max and min Z is needed
    }


    //<editor-fold desc="Getters">
    public float[] getPositionArray() {
        return new float[] {near.x, near.y, near.z, far.x, far.y, far.z};
    }

    public float[] getNearPointF() {
        return new float[] {near.x, near.y, near.z};
    }

    public float[] getFarPointF() {
        return new float[] {far.x, far.y, far.z};
    }

    public Vector3f getNearPointV() {
        return this.near;
    }

    public Vector3f getFarPointV() {
        return this.far;
    }

    public float getSqrDistTo(Vector3f input) {
        return input.subtractV(this.near).lengthSqr();
    }
    //</editor-fold>


    public boolean notNull() {
        if (near.nonZero() && far.nonZero()) return true;
        else return false;
    }

    public boolean insideBox(Vector3f point) {
        if ( (bBoxMin.x <= point.x && point.x <= bBoxMax.x) &&
             (bBoxMin.y <= point.y && point.y <= bBoxMax.y) ) return true;
             //(bBoxMin.z <= point.z && point.z <= bBoxMax.z) ) return true;
        else return false;
    }

    public boolean onRay(Vector3f input) {
        if (!this.insideBox(input)) return false;
        input = input.subtractV(this.near);
        float projection = input.projectOnV(this.delta);
        float intersectionSqr = this.radius*this.radius - input.lengthSqr() + projection*projection;

        if (intersectionSqr >= 0) return true;
        else return false;
    }

    public boolean closestSelected(Vector3f sel, Vector3f vect) {
        if (this.getSqrDistTo(sel) < this.getSqrDistTo(vect)) return true;
        else return false;
    }
}
