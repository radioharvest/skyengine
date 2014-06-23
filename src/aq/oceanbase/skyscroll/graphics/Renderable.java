package aq.oceanbase.skyscroll.graphics;

import android.content.Context;

public interface Renderable {
    public void initialize(Context context, String shaderFolder);

    public void draw(Camera cam);
}
