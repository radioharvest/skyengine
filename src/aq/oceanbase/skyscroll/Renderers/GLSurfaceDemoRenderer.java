package aq.oceanbase.skyscroll.Renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GLSurfaceDemoRenderer extends GLSurfaceView {

    private final DemoRenderer mRenderer;

    private final float TOUCH_SCALE_FACTOR = 180.0f/320;

    private float mPreviousX;
    private float mPreviousY;

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

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                mRenderer.setAngle(mRenderer.getAngle() + (dx * TOUCH_SCALE_FACTOR));
                mRenderer.setHeight(mRenderer.getHeight() + dy/100);
        }

        mPreviousX = x;
        mPreviousY = y;

        return true;
    }
}
