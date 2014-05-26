package aq.oceanbase.skyscroll.Renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import aq.oceanbase.skyscroll.math.Vector2f;

public class GLSurfaceDemoRenderer extends GLSurfaceView {

    private final DemoRenderer mRenderer;

    private final float TOUCH_SCALE_FACTOR = 180.0f/320;

    private Vector2f mPrevious = new Vector2f();
    private float mPreviousX;
    private float mPreviousY;
    private Vector2f mDelta = new Vector2f(0.0f, 0.0f);

    public GLSurfaceDemoRenderer (Context context) {
        super(context);

        setEGLContextClientVersion(2);
        mRenderer = new DemoRenderer();
        setRenderer(mRenderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

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

            case MotionEvent.ACTION_CANCEL:
                //TODO: add operator override

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
