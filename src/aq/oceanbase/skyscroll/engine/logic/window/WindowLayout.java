package aq.oceanbase.skyscroll.engine.logic.window;

import android.content.Context;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.graphics.ProgramManager;
import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class WindowLayout extends WindowBlock {
    public static enum LAYOUT {
        HORIZONTAL, VERTICAL
    }

    private LAYOUT mLayoutType;

    private List<WindowBlock> mChildren = new ArrayList<WindowBlock>();


    public WindowLayout (LAYOUT type, Window root, float fraction) {
        super(root, fraction);
        this.mLayoutType = type;
    }


    public void setLayoutType(LAYOUT type) {
        this.mLayoutType = type;
    }


    public void addChild(WindowBlock object) {
        this.mChildren.add(object);
    }

    public void buildChildrenMetrics() {
        Vector3f currentOrigin = new Vector3f(mPos);
        float blockSize = 0.0f;
        float unitSize;


        for (WindowBlock item : mChildren) blockSize += item.getFraction();

        unitSize = 1 / blockSize; /// mChildren.size();

        for (WindowBlock item : mChildren) {
            float factor = (item.getFraction() * unitSize);

            float width;
            float height;
            int[] pixelMetrics = new int[2];

            if (this.mLayoutType == LAYOUT.HORIZONTAL) {
                width = factor * this.mWidth;
                height = this.mHeight;

                pixelMetrics[0] = (int)(factor * mPixelMetrics[0]);
                pixelMetrics[1] = mPixelMetrics[1];

                item.setMetrics(new Vector3f(currentOrigin), width, height, pixelMetrics);

                currentOrigin.x += width;
            } else {                                    // if VERTICAL
                width = this.mWidth;
                height = factor * this.mHeight;

                pixelMetrics[0] = mPixelMetrics[0];
                pixelMetrics[1] = (int)(factor * mPixelMetrics[1]);

                item.setMetrics(new Vector3f(currentOrigin), width, height, pixelMetrics);

                currentOrigin.y -= height;
            }
        }
    }


    @Override
    public void onSwipeHorizontal(float amount) {
        if (!mChildren.isEmpty()) {
            for (WindowBlock item : mChildren) {
                item.onSwipeHorizontal(amount);
            }
        }
    }

    @Override
    public void onSwipeVertical(float amount) {
        if (!mChildren.isEmpty()) {
            for (WindowBlock item : mChildren) {
                item.onSwipeVertical(amount);
            }
        }
    }

    @Override
    public void onScale(float span) {
        if (!mChildren.isEmpty()) {
            for (WindowBlock item : mChildren) {
                item.onScale(span);
            }
        }
    }

    @Override
    public void onTap(float x, float y) {
        if (!mChildren.isEmpty()) {
            for (WindowBlock item : mChildren) {
                item.onTap(x, y);
            }
        }
    }


    @Override
    public void update() {
        for (WindowBlock item : mChildren) {
            item.update();
        }
    }


    @Override
    public void initialize(Context context, ProgramManager programManager) {
        super.initialize(context, programManager);
        this.buildChildrenMetrics();

        for (WindowBlock item : mChildren) {
            item.initialize(context, programManager);
        }
    }

    @Override
    public void release() {
        super.release();

        for (WindowBlock item : mChildren) {
            item.release();
        }
    }

    @Override
    public void draw(Camera cam) {

        for (WindowBlock item : mChildren) {
            item.draw(cam);
        }
    }
}