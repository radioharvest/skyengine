package aq.oceanbase.skyscroll.utils.math;

public class Vector2f {
    public float x;
    public float y;

    public Vector2f() {};

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float[] getVector() {
        return new float[] {x, y};
    }

    public boolean nonZero() {
        if (x != 0.0f || y!= 0.0f) return true;
        else return false;
    }
}
