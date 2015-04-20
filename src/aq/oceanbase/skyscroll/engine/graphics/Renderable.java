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

public interface Renderable {
    public boolean isInitialized();

    public void initialize(Context context, ProgramManager programManager);

    public void release();

    public void draw(Camera cam);
}
