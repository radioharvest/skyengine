package aq.oceanbase.skyscroll.game;

import aq.oceanbase.skyscroll.engine.graphics.RenderContainer;
import aq.oceanbase.skyscroll.engine.graphics.RenderableObject;
import aq.oceanbase.skyscroll.engine.input.touch.TouchHandler;
import aq.oceanbase.skyscroll.engine.input.touch.TouchRay;
import aq.oceanbase.skyscroll.engine.input.touch.Touchable;

import java.util.ArrayList;
import java.util.List;

public class GameContext implements Touchable {

    private RenderContainer mRenderContainer;
    private List<Touchable> mTouchables;
    private final TouchHandler mTouchHandler;

    // you can attach custom touchHandler if you need one or just simply use mTouchables list and default handler
    public GameContext(TouchHandler touchHandler) {
        mRenderContainer = new RenderContainer();
        mTouchables = new ArrayList<>();

        mTouchHandler = touchHandler;
    }

    public GameContext() {
        mRenderContainer = new RenderContainer();
        mTouchables = new ArrayList<>();

        mTouchHandler = new TouchHandler() {
            @Override
            public void onSwipeHorizontal(float amount) {

            }

            @Override
            public void onSwipeVertical(float amount) {

            }

            @Override
            public void onScale(float span) {

            }

            @Override
            public void onTap(TouchRay touchRay) {

            }
        };
    }

    public void addRenderable(RenderableObject renderableObject) {
        this.mRenderContainer.addRenderable(renderableObject);
    }

    public void addTouchable(Touchable touchable) {
        this.mTouchables.add(touchable);
    }


    public RenderContainer getRenderContainer() {
        return mRenderContainer;
    }

    public TouchHandler getTouchHandler() {
        return mTouchHandler;
    }


    private boolean isTouchable(Object object) {
        return Touchable.class.isInstance(object);
    }


    public void onPop() {
        mRenderContainer.release();
    }


    @Override
    public void onSwipeHorizontal(float amount) {
        for (Touchable touchable: mTouchables) {
            touchable.onSwipeHorizontal(amount);
        }

        mTouchHandler.onSwipeHorizontal(amount);
    }

    @Override
    public void onSwipeVertical(float amount) {
        for (Touchable touchable: mTouchables) {
            touchable.onSwipeVertical(amount);
        }

        mTouchHandler.onSwipeVertical(amount);
    }

    @Override
    public void onScale(float span) {
        for (Touchable touchable: mTouchables) {
            touchable.onScale(span);
        }

        mTouchHandler.onScale(span);
    }

    @Override
    public void onTap(TouchRay touchRay) {
        for (Touchable touchable: mTouchables) {
            touchable.onTap(touchRay);
        }

        mTouchHandler.onTap(touchRay);
    }
}
