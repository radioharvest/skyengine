package aq.oceanbase.skyscroll.touch;

public interface TouchHandler {
    public void onSwipeHorizontal(float swipe);

    public void onSwipeVertical(float swipe);

    public void onScale(float span);

    public void onTap(float tap);
}
