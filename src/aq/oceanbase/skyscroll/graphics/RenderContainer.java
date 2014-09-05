package aq.oceanbase.skyscroll.graphics;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class RenderContainer implements Renderable {

    private List<Object> mRenderList;
    private List<Integer> mInitList;

    public RenderContainer() {
        mRenderList = new ArrayList<Object>();
        mInitList = new ArrayList<Integer>();
    }

    public RenderContainer addRenderable(Object object) {
        this.mRenderList.add(object);
        mInitList.add(this.mRenderList.size() - 1);

        return this;        //added for continuous calls
    }

    public void initializeNewObjects(Context context, String shaderFolder) {
        if ( mInitList.size() > 0) {
            for (int n: mInitList) {
                Renderable rend = (Renderable)mRenderList.get(n);
                rend.initialize(context, shaderFolder);
            }

            mInitList.clear();
        }
    }

    public void clear() {
        mInitList.clear();
        mRenderList.clear();
    }

    public void initialize(Context context, String shaderFolder) {
        for (Object object: mRenderList) {
            Renderable rend = (Renderable)object;
            rend.initialize(context, shaderFolder);
        }
    }

    public void draw(Camera cam) {
        for (Object object: mRenderList) {
            Renderable rend = (Renderable)object;
            rend.draw(cam);
        }
    }
}
