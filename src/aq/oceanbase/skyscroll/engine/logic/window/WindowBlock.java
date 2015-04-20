package aq.oceanbase.skyscroll.engine.logic.window;

import android.content.Context;
import android.util.Log;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.graphics.ProgramManager;
import aq.oceanbase.skyscroll.engine.graphics.RenderableObject;
import aq.oceanbase.skyscroll.game.events.QuestionEvent;
import aq.oceanbase.skyscroll.game.events.QuestionEventListener;
import aq.oceanbase.skyscroll.engine.logic.events.WindowEvent;
import aq.oceanbase.skyscroll.engine.logic.events.WindowEventListener;
import aq.oceanbase.skyscroll.engine.input.touch.Touchable2D;
import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;


public class WindowBlock extends RenderableObject implements WindowEventListener, QuestionEventListener, Touchable2D {
    //TODO: check if fields could really be private and class could be non-abstract
    protected Vector3f mPos;                // position of upper left corner
    protected float mWidth;
    protected float mHeight;
    protected int[] mPixelMetrics;

    protected float mParentFraction;

    protected Window mRoot;

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

    public Vector3f getPos() {
        return mPos;
    }


    public void update() {}


    @Override
    public void onClose(WindowEvent e) {}

    @Override
    public void onAnswer(QuestionEvent e) {}


    @Override
    public void onSwipeHorizontal(float amount) {}

    @Override
    public void onSwipeVertical(float amount) {}

    @Override
    public void onScale(float span) {}

    @Override
    public void onTap(float x, float y) {}


    @Override
    public void initialize(Context context, ProgramManager programManager) {
        super.initialize(context, programManager);
    }

    @Override
    public void release() {
        super.release();
    }

    @Override
    public void draw(Camera cam) {}
}
