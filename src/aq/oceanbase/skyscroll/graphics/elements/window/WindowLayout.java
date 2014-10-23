package aq.oceanbase.skyscroll.graphics.elements.window;

import android.content.Context;
import android.util.Log;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

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


    public void addChild(WindowBlock object) {
        this.mChildren.add(object);
    }

    public void buildChildrenMetrics() {
        Vector3f currentOrigin = new Vector3f(mPos);
        float blockSize = 0.0f;
        float unitSize = 1;


        for (WindowBlock item : mChildren) blockSize += item.getFraction();

        if (blockSize > 1.0f) unitSize = 1 / blockSize; /// mChildren.size();

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
    public void initialize(Context context, ProgramManager programManager) {
        super.initialize(context, programManager);
        this.buildChildrenMetrics();

        if (!mChildren.isEmpty()) {
            for (WindowBlock item : mChildren) {
                item.initialize(context, programManager);
            }
        }
    }

    public void release() {
        super.release();

        if (!mChildren.isEmpty()) {
            for (WindowBlock item : mChildren) {
                item.release();
            }
        }
    }

    public void draw(Camera cam) {
        if (!mChildren.isEmpty()) {
            for (WindowBlock item : mChildren) {
                item.draw(cam);
            }
        }
    }
}