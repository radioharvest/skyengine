package aq.oceanbase.skyscroll.graphics.render;

import android.content.Context;
import android.util.Log;
import aq.oceanbase.skyscroll.graphics.Camera;

import java.util.ArrayList;
import java.util.List;

public class RenderContainer extends RenderableObject {

    private List<Object> mRenderList;
    private List<Object> mInitList;

    public RenderContainer() {
        mRenderList = new ArrayList<>();
        mInitList = new ArrayList<>();
    }

    public RenderContainer addRenderable(RenderableObject object) {
        mRenderList.add(object);

        if (!object.isInitialized()) {
            mInitList.add(object);
            Log.e("Debug", new StringBuilder().append("NEW OBJECTS ADDED").toString());
        }

        return this;        //added for continuous calls
    }

    public RenderContainer addContainer(RenderContainer container) {
        for (Object rend : container.mRenderList) {
            mRenderList.add(rend);
        }

        for (Object rend : container.mInitList) {
            mInitList.add(rend);
        }

        return this;
    }

    public List<Object> getRenderList() {
        return mRenderList;
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
                RenderableObject rend = (RenderableObject)object;
                rend.initialize(context, programManager);
                Log.e("Debug", new StringBuilder().append("NEW OBJECTS INITED").toString());
            }

            mInitList.clear();
        }

    }

    public void clear() {
        mInitList.clear();
        release();
        mRenderList.clear();
    }

    @Override
    public void unlock() {
        if (!mRenderList.isEmpty()) {
            for (Object object: mRenderList) {
                RenderableObject rend = (RenderableObject)object;
                rend.unlock();
            }
        }
    }

    @Override
    public void initialize(Context context, ProgramManager programManager) {
        for (Object object: mRenderList) {
            RenderableObject rend = (RenderableObject)object;
            rend.initialize(context, programManager);
            Log.e("Debug", new StringBuilder().append("ORIG INITED").toString());
        }

        super.initialize(context, programManager);
    }

    @Override
    public void release() {
        if (!mRenderList.isEmpty()) {
            for (Object object: mRenderList) {
                RenderableObject rend = (RenderableObject)object;
                if (!rend.isLocked()) {
                    rend.release();
                }
            }
        }

        super.release();
    }

    @Override
    public void draw(Camera cam) {
        if (!mRenderList.isEmpty()) {
            for (Object object: mRenderList) {
                RenderableObject rend = (RenderableObject)object;
                rend.draw(cam);
            }
        }
    }
}
