package aq.oceanbase.skyscroll.touch;

import aq.oceanbase.skyscroll.math.Vector3f;

public class TouchRay {
    private Vector3f near;
    private Vector3f far;
    private Vector3f delta;
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
        this.near = nearPoint;
        this.far = farPoint;
        this.radius = rad;
        buildRay();
    }

    public float[] getPositionArray() {
        float[] positions = new float[] {near.x, near.y, near.z, far.x, far.y, far.z};
        return positions;
    }

    public boolean notNull() {
        if (near.nonZero() && far.nonZero()) return true;
        else return false;
    }

    private void buildRay() {
        delta = far.subtractV(near);

        float up = Math.max(near.y, far.y) + radius;
        float down = Math.min(near.y, far.y) - radius;
        float right = Math.max(near.x, far.x) + radius;
        float left = Math.min(near.x, far.x) - radius;
        bBoxMax = new Vector3f(right, up, near.z);      //if Z coord detection will be used reconsidering status of
        bBoxMin = new Vector3f(left, down, far.z);      //max and min Z is needed
    }


    public boolean insideBox(Vector3f point) {
        if ( (bBoxMin.x <= point.x && point.x <= bBoxMax.x) &&
             (bBoxMin.y <= point.y && point.y <= bBoxMax.y) ) return true;
             //(bBoxMin.z <= point.z && point.z <= bBoxMax.z) ) return true;
        else return false;
    }

    public boolean insideBox(float x, float y, float z) {
        if ( (bBoxMin.x <= x && x <= bBoxMax.x) &&
             (bBoxMin.y <= y && y <= bBoxMax.y) ) return true;
             //(bBoxMin.z <= z && z <= bBoxMax.z) ) return true;
        else return false;
    }
}
