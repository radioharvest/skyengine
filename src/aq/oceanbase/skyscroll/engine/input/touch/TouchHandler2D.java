package aq.oceanbase.skyscroll.engine.input.touch;

public abstract class TouchHandler2D {
    public void onSwipeHorizontal(float amount) {}

    public void onSwipeVertical(float amount) {}

    public void onScale(float span) {}

    public void onTap(float x, float y) {}
}
