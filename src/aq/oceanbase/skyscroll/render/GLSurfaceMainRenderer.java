package aq.oceanbase.skyscroll.render;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import aq.oceanbase.skyscroll.math.Vector2f;

public class GLSurfaceMainRenderer extends GLSurfaceView {

    private final MainRenderer mRenderer;


    //CONSTANTS
    private final float ROTATION_SCALE_FACTOR = 180.0f/360;
    private final float HEIGHT_SCALE_FACTOR = 1/30.0f;
    private final float ZOOM_FACTOR = 1/30.0f;

    private int mode;
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;

    private Vector2f mPrevious = new Vector2f();
    private Vector2f mDelta = new Vector2f(0.0f, 0.0f);

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mTapDetector;

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
            mRenderer.mTouchHandler.onScale(temp * ZOOM_FACTOR);
            lastSpan = span;

            return true;
        }
    };
    private final GestureDetector.OnGestureListener mTapGestureListener
            = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mRenderer.mTouchHandler.onTap(e.getX(), e.getY());

            return true;
        }
    };


    public GLSurfaceMainRenderer (Context context) {
        super(context);
        Log.e("RunDebug", "GLSurface stage passed");

        mScaleDetector = new ScaleGestureDetector(context, mScaleGestureListener);
        mTapDetector = new GestureDetector(context, mTapGestureListener);

        setEGLContextClientVersion(2);
        mRenderer = new MainRenderer(context);
        setRenderer(mRenderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();


        mScaleDetector.onTouchEvent(e);
        mTapDetector.onTouchEvent(e);

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
                        mDelta.x = (x - mPrevious.x)*ROTATION_SCALE_FACTOR;
                        mDelta.y = (y - mPrevious.y)*HEIGHT_SCALE_FACTOR;
                    }


                    if (mDelta.x != 0) mRenderer.mTouchHandler.onSwipeHorizontal(mDelta.x);
                    if (mDelta.y != 0) mRenderer.mTouchHandler.onSwipeVertical(mDelta.y);
                    break;
                }


            case MotionEvent.ACTION_UP:
                //Log.e("Touch", "ACTION UP");
                mode = NONE;

                mDelta.x = 0.0f;
                mDelta.y = 0.0f;

                break;
        }

        mPrevious.x = x;
        mPrevious.y = y;

        return true;
    }
}
