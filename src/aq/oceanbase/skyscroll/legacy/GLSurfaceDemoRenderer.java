package aq.oceanbase.skyscroll.legacy;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import aq.oceanbase.skyscroll.engine.utils.math.Vector2f;

public class GLSurfaceDemoRenderer extends GLSurfaceView {

    private final DemoRenderer mRenderer;

    private final float TOUCH_SCALE_FACTOR = 180.0f/320;
    private final float ZOOM_FACTOR = 1/30.0f;

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;

    private int mode;

    private ScaleGestureDetector mScaleDetector;

    private Vector2f mPrevious = new Vector2f();
    private Vector2f mDelta = new Vector2f(0.0f, 0.0f);

    public GLSurfaceDemoRenderer (Context context) {
        super(context);

        mScaleDetector = new ScaleGestureDetector(context, mScaleGestureListener);

        setEGLContextClientVersion(2);
        mRenderer = new DemoRenderer();
        setRenderer(mRenderer);
    }

    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        private float lastSpan;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            lastSpan = scaleGestureDetector.getCurrentSpan();
            return true;
        }

        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

                mode = ZOOM;

                float span = scaleGestureDetector.getCurrentSpan();
                float temp = span - lastSpan;
                mRenderer.zoom(temp * ZOOM_FACTOR);
                lastSpan = span;


                Log.e("Span", new StringBuilder().append("Span: ").append(span).toString());
                Log.e("Span", new StringBuilder().append("Temp: ").append(temp).toString());



            return true;
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        mScaleDetector.onTouchEvent(e);

        switch (e.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode != ZOOM) {
                    if (mPrevious.nonZero()) {
                    mDelta.x = x - mPrevious.x;
                    mDelta.y = (y - mPrevious.y)/30;
                    //mDelta.y = dy;
                    }

                    if (mDelta.x != 0) {
                        mDelta.x = mDelta.x * TOUCH_SCALE_FACTOR;
                        mRenderer.setAngle(mRenderer.getAngle() + mDelta.x);
                    }

                    //if (mDelta.y != 0) mRenderer.setHeight(mRenderer.getHeight() + mDelta.y/30);
                    mRenderer.setHeight(mRenderer.getHeight() + mDelta.y);
                }

            case MotionEvent.ACTION_UP:
                //TODO: add operator override

                mode = NONE;

                Log.d("Demo", new StringBuilder().append(mDelta.x).toString());

                if (mDelta.nonZero()) {
                    Log.d("Demo", new StringBuilder().append("Before: ").append(mDelta.x).toString());
                    mRenderer.setMomentum(mDelta);
                    mDelta.x = 0.0f;
                    mDelta.y = 0.0f;
                    Log.d("Demo", new StringBuilder().append("After: ").append(mDelta.x).toString());
                }
        }

        mPrevious.x = x;
        mPrevious.y = y;

        return true;
    }
}
