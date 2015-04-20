package aq.oceanbase.skyscroll.engine.graphics;

import android.content.Context;
import aq.oceanbase.skyscroll.engine.graphics.ProgramManager;
import aq.oceanbase.skyscroll.engine.graphics.Camera;

/**
 * This interface is used for renderable objects
 * initialize method is called from onSurfaceCreated method
 * in GLSurfaceView.Renderer object and usually contains
 * shader programs setup or/and texture loading
 * draw method is called from onDrawFrame
 */

public class RenderableObject {
    private boolean mInititialized = false;
    private boolean mLocked = false;

    public boolean isInitialized() {
        return mInititialized;
    }

    public boolean isLocked() {
        return mLocked;
    }

    public void lock() {
        mLocked = true;
    }

    public void unlock() {
        mLocked = false;
    }

    public void initialize(Context context, ProgramManager programManager) {
        mInititialized = true;
    }

    public void release() {
        mInititialized = false;
    }

    public void draw(Camera cam) {

    }
}
