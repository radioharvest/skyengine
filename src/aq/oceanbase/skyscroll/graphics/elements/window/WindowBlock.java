package aq.oceanbase.skyscroll.graphics.elements.window;

import android.content.Context;
import android.util.Log;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.graphics.render.Renderable;
import aq.oceanbase.skyscroll.touch.TouchHandler;
import aq.oceanbase.skyscroll.utils.math.Vector3f;


public abstract class WindowBlock extends TouchHandler implements Renderable {
    //TODO: check if fields could really be private and class could be non-abstract
    protected Vector3f mPos;                // position of upper left corner
    protected float mWidth;
    protected float mHeight;
    protected int[] mPixelMetrics;

    protected float mParentFraction;

    protected Window mRoot;

    protected boolean mInitialized = false;

    public WindowBlock (Window root, float fraction) {
        this.mRoot = root;
        this.mParentFraction = fraction;
    }

    public void setMetrics(Vector3f pos, float width, float height, int[] pixelMetrics) {
        this.mPos = pos;
        this.mWidth = width;
        this.mHeight = height;
        this.mPixelMetrics = pixelMetrics;
        Log.e("Debug", "New Metrics: " + pos.x + " " + pos.y + " " + pos.z + " " + mWidth + " " + mHeight);

        onMetricsSet();
    }

    protected void onMetricsSet() {}

    public float getFraction() {
        return this.mParentFraction;
    }


    public void update() {}


    public boolean isInitialized() {
        return mInitialized;
    }

    public void initialize(Context context, ProgramManager programManager) {
        mInitialized = true;
    }

    public void release() {
        mInitialized = false;
    }

    public void draw(Camera cam) {}
}
