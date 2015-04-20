package aq.oceanbase.skyscroll.engine.utils.math;

public class Vector2f {
    public float x;
    public float y;

    public Vector2f() {};

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f(Vector2f op) {
        this.x = op.x;
        this.y = op.y;
    }

    public float[] getVector() {
        return new float[] {x, y};
    }

    public boolean nonZero() {
        if (x != 0.0f || y!= 0.0f) return true;
        else return false;
    }

    public Vector2f addV(Vector2f op) {
        return new Vector2f(this.x + op.x, this.y + op.y);
    }

    public Vector2f subtractV(Vector2f op) {
        return new Vector2f(this.x - op.x, this.y - op.y);
    }

    public Vector2f multiplySf(float scalar) {
        return new Vector2f(this.x*scalar, this.y*scalar);
    }

    public float crossV(Vector2f op) {
        return (this.x * op.y - this.y * op.x);
    }

    public static Vector2f getZero() {
        return new Vector2f(0.0f, 0.0f);
    }
}
