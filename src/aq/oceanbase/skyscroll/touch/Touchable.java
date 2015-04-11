package aq.oceanbase.skyscroll.touch;

public interface Touchable {
    public void onSwipeHorizontal(float amount);

    public void onSwipeVertical(float amount);

    public void onScale(float span);

    public void onTap(TouchRay touchRay);
}
