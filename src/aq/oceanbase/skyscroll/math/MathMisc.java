package aq.oceanbase.skyscroll.math;
import android.util.Log;

public class MathMisc {
    public static float decrementConvergingValue(float value, float decrement) {
        float absValue = Math.abs(value);
        absValue = absValue - decrement;
        if (absValue < 0.0f) absValue = 0.0f;

        if (value >= 0.0f) value = absValue;
        else value = -absValue;

        return value;
    }

    public static void printMatrix(float[] matrix, String tag) {
        Log.e("Error", new StringBuilder().append(tag).toString());
        Log.e("Error", new StringBuilder().append(matrix[0]).append(" ").append(matrix[1]).append(" ").append(matrix[2]).append(" ").append(matrix[3]).toString());
        Log.e("Error", new StringBuilder().append(matrix[4]).append(" ").append(matrix[5]).append(" ").append(matrix[6]).append(" ").append(matrix[7]).toString());
        Log.e("Error", new StringBuilder().append(matrix[8]).append(" ").append(matrix[9]).append(" ").append(matrix[10]).append(" ").append(matrix[11]).toString());
        Log.e("Error", new StringBuilder().append(matrix[12]).append(" ").append(matrix[13]).append(" ").append(matrix[14]).append(" ").append(matrix[15]).toString());
    }
}
