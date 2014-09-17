package aq.oceanbase.skyscroll.graphics;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RenderContainer implements Renderable {

    private boolean mInitialized = false;

    private List<Object> mRenderList;
    private List<Object> mInitList;

    public RenderContainer() {
        mRenderList = new ArrayList<Object>();
        mInitList = new ArrayList<Object>();
    }

    public RenderContainer addRenderable(Object object) {
        mRenderList.add(object);

        Renderable rend = (Renderable)object;
        if (!rend.isInitialized()) {
            mInitList.add(object);
            Log.e("Debug", new StringBuilder().append("NEW OBJECTS ADDED").toString());
        }


        return this;        //added for continuous calls
    }

    public void initializeNewObjects(Context context, ProgramManager programManager) {
        if ( mInitList.size() > 0) {
            //for (int n: mInitList) {
            /*for (int i = 0; i < mInitList.size(); i++ ) {
                Renderable rend = (Renderable)mRenderList.get(i);
                rend.initialize(context, shaderFolder);
                Log.e("Debug", new StringBuilder().append("NEW OBJECTS INIT").toString());

            }*/
            for (Object object: mInitList) {
                Renderable rend = (Renderable)object;
                rend.initialize(context, programManager);
                Log.e("Debug", new StringBuilder().append("NEW OBJECTS INITED").toString());
            }

            mInitList.clear();
        }

    }

    public void clear() {
        mInitList.clear();
        mRenderList.clear();
    }

    public boolean isInitialized() {
        return this.mInitialized;
    }

    public void initialize(Context context, ProgramManager programManager) {
        for (Object object: mRenderList) {
            Renderable rend = (Renderable)object;
            rend.initialize(context, programManager);
            Log.e("Debug", new StringBuilder().append("ORIG INITED").toString());
        }
        mInitialized = true;
    }

    public void release() {

    }

    public void draw(Camera cam) {
        if (!mRenderList.isEmpty()) {                       // to prevent rare bug
            for (Object object: mRenderList) {
                Renderable rend = (Renderable)object;
                rend.draw(cam);
            }
        }
    }
}
