package aq.oceanbase.skyscroll.graphics;

import android.content.Context;

/**
 * This interface is used for renderable objects
 * initialize method is called from onSurfaceCreated method
 * in GLSurfaceView.Renderer object and usually contains
 * shader programs setup or/and texture loading
 * draw method is called from onDrawFrame
 */

public interface Renderable {
    public void initialize(Context context, String shaderFolder);

    public void draw(Camera cam);
}
