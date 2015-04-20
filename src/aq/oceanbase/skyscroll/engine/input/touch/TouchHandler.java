package aq.oceanbase.skyscroll.engine.input.touch;

/**
 * This class represents basic TouchHandler functionality.
 * It is designed to be overriden by anonymous classes
 * representing different TouchHadler objects.
 * Non-interface approach is chosen to avoid overriding
 * unused methods which will do nothing when called.
 */

public abstract class TouchHandler {
    public void onSwipeHorizontal(float amount) {}

    public void onSwipeVertical(float amount) {}

    public void onScale(float span) {}

    public void onTap(TouchRay touchRay) {}
}
