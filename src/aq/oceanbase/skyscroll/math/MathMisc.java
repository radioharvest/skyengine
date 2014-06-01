package aq.oceanbase.skyscroll.math;

public class MathMisc {
    public static float decrementConvergingValue(float value, float decrement) {
        float absValue = Math.abs(value);
        absValue = absValue - decrement;
        if (absValue < 0.0f) absValue = 0.0f;

        if (value >= 0.0f) value = absValue;
        else value = -absValue;

        return value;
    }
}
