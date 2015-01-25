package aq.oceanbase.skyscroll.graphics.elements.window;

import android.content.Context;
import android.util.Log;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.graphics.render.Renderable;
import aq.oceanbase.skyscroll.logic.events.WindowEvent;
import aq.oceanbase.skyscroll.logic.events.WindowEventListener;
import aq.oceanbase.skyscroll.touch.TouchHandler;
import aq.oceanbase.skyscroll.utils.math.Vector3f;


public class WindowBlock extends TouchHandler implements Renderable, WindowEventListener {
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

        this.mRoot.addWindowEventListener(this);
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


    @Override
    public void onClose(WindowEvent e) {}

    @Override
    public void onAnswer(WindowEvent e) {}


    @Override
    public boolean isInitialized() {
        return mInitialized;
    }

    @Override
    public void initialize(Context context, ProgramManager programManager) {
        mInitialized = true;
    }

    @Override
    public void release() {
        mInitialized = false;
    }

    @Override
    public void draw(Camera cam) {}
}
