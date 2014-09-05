package aq.oceanbase.skyscroll.graphics.render;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import aq.oceanbase.skyscroll.Core;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.logic.Game;
import aq.oceanbase.skyscroll.utils.math.Vector2f;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

public class GLSurfaceOldMainRenderer extends GLSurfaceView {

    private final String mShaderFolder = "/aq/oceanbase/skyscroll/graphics/render/shaders";

    //private final MainRenderer mRenderer;
    private final OldMainRenderer mOldRenderer;
    private Game mGameInstance;

    private float mDistance = 15.0f;         //cam distance from origin
    private float mHeight = 0.0f;


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
            //mRenderer.mGameInstance.mTouchHandler.onScale(temp * ZOOM_FACTOR);
            mOldRenderer.mTouchHandler.onScale(temp * ZOOM_FACTOR);
            lastSpan = span;

            return true;
        }
    };
    private final GestureDetector.OnGestureListener mTapGestureListener
            = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //mRenderer.mGameInstance.mTouchHandler.onTap(e.getX(), e.getY());
            mOldRenderer.mTouchHandler.onTap(e.getX(), e.getY());

            return true;
        }
    };


    public GLSurfaceOldMainRenderer (Context context, Game gameInstance) {
        super(context);

        mScaleDetector = new ScaleGestureDetector(context, mScaleGestureListener);
        mTapDetector = new GestureDetector(context, mTapGestureListener);

        setEGLContextClientVersion(2);
        //setPreserveEGLContextOnPause(true);

        mGameInstance = gameInstance;

        //mRenderer = new MainRenderer(context, mShaderFolder);
        //setRenderer(mRenderer);
        //mRenderer.setGameInstance(mGameInstance);
        mOldRenderer = new OldMainRenderer(context);
        setRenderer(mOldRenderer);
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


                    //if (mDelta.x != 0) mRenderer.mGameInstance.mTouchHandler.onSwipeHorizontal(mDelta.x);
                    //if (mDelta.y != 0) mRenderer.mGameInstance.mTouchHandler.onSwipeVertical(mDelta.y);
                    if (mDelta.x != 0) mOldRenderer.mTouchHandler.onSwipeHorizontal(mDelta.x);
                    if (mDelta.y != 0) mOldRenderer.mTouchHandler.onSwipeVertical(mDelta.y);
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
