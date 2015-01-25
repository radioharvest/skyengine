package aq.oceanbase.skyscroll.graphics.elements.window.blocks;

import android.util.Log;
import aq.oceanbase.skyscroll.graphics.TextureRegion;
import aq.oceanbase.skyscroll.logic.events.ButtonEvent;
import aq.oceanbase.skyscroll.logic.events.ButtonEventListener;
import aq.oceanbase.skyscroll.touch.TouchHandler;
import aq.oceanbase.skyscroll.utils.math.Vector2f;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Button extends TouchHandler{
    public static final int BUFFER_DATA_SIZE = 5;
    public static final int COLOR_DATA_SIZE = 4;
    public static final int VERTEX_AMOUNT = 4;

    public static enum STATE {
        PRESSED, NEUTRAL, WRONG, CORRECT
    }

    private int mId;

    private String mText;
    private float[] mColor;

    private Vector3f mPos;
    private Vector3f mLowerCornerPos;

    private float[] mMetrics;
    private int[] mPixelMetrics;

    private TextureRegion mTexRgn;

    private STATE mState;

    private List<Object> mEventListeners = new ArrayList<Object>();

    public Button (String text) {
        this.mState = STATE.NEUTRAL;
        this.mColor = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
        this.mText = text;
    }

    public Button (Vector3f position, float[] metrics, int[] pixelMetrics, String text) {
        this(text);

        this.setMetrics(position, metrics, pixelMetrics);
    }

    public float getWidth() {
        return mMetrics[0];
    }

    public int getPixelWidth() {
        return mPixelMetrics[0];
    }

    public float getHeight() {
        return mMetrics[1];
    }

    public int getPixelHeight() {
        return mPixelMetrics[1];
    }

    public float[] getMetrics() {
        return mMetrics;
    }

    public Vector3f getPos() {
        return mPos;
    }

    public String getText() {
        return mText;
    }

    public float[] getColor() {
        return mColor;
    }

    public TextureRegion getTexRgn() {
        return mTexRgn;
    }

    public STATE getState() {
        return this.mState;
    }

    public float[] getVertexData() {
        return new float[] {
                mPos.x, mPos.y, mPos.z,
                mPos.x, mPos.y - mMetrics[1], mPos.z,
                mPos.x + mMetrics[0], mPos.y - mMetrics[1], mPos.z,
                mPos.x + mMetrics[0], mPos.y, mPos.z
        };
    }

    public float[] getTextureData() {
        return new float[] {
                mTexRgn.u1, mTexRgn.v1,
                mTexRgn.u1, mTexRgn.v2,
                mTexRgn.u2, mTexRgn.v2,
                mTexRgn.u2, mTexRgn.v1
        };
    }

    public float[] getPackedData() {
        return new float[] {
            mPos.x, mPos.y, mPos.z, mTexRgn.u1, mTexRgn.v1,
            mPos.x, mPos.y - mMetrics[1], mPos.z, mTexRgn.u1, mTexRgn.v2,
            mPos.x + mMetrics[0], mPos.y - mMetrics[1], mPos.z, mTexRgn.u2, mTexRgn.v2,
            mPos.x + mMetrics[0], mPos.y, mPos.z, mTexRgn.u2, mTexRgn.v1
        };
    }

    public float[] getColorData() {
        return new float[] {
                mColor[0], mColor[1], mColor[2], mColor[3],
                mColor[0], mColor[1], mColor[2], mColor[3],
                mColor[0], mColor[1], mColor[2], mColor[3],
                mColor[0], mColor[1], mColor[2], mColor[3]
        };
    }


    public void setId(int id) {
        this.mId = id;
    }

    public void setColor(float[] color) {
        this.mColor = color;
    }

    public void setState (STATE state) {
        this.mState = state;

        switch (mState) {
            case PRESSED:
                mColor = new float[] {0.9f, 0.9f, 0.9f, 1.0f};
                break;
            case NEUTRAL:
                mColor = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
                break;
            case WRONG:
                mColor = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
                break;
            case CORRECT:
                mColor = new float[] {0.0f, 1.0f, 0.0f, 1.0f};
                break;
        }
    }

    public void setMetrics(Vector3f pos, float[] sizeMetrics, int[] pixelMetrics) {
        this.mPos = pos;
        this.mMetrics = sizeMetrics;
        this.mPixelMetrics = pixelMetrics;
        this.mLowerCornerPos = new Vector3f(mPos.x - mMetrics[0]/2, mPos.y - mMetrics[1]/2, mPos.z );
    }

    public void setTexRgn (TextureRegion texRgn) {
        this.mTexRgn = texRgn;
    }


    public boolean isInitialized() {
        return (mMetrics[0] == 0 || mMetrics[1] == 0);
    }


    public void addButtonEventListener(Object obj) {
        mEventListeners.add(obj);
    }

    public void removeButtonWindowEventListener(Object obj) {
        mEventListeners.remove(obj);
    }

    public void fireButtonPressedEvent() {
        ButtonEvent event = new ButtonEvent(this, this.mId, true);

        Log.e("Debug", "Firing button event");

        if (!mEventListeners.isEmpty()) {
            for (int i = 0; i < mEventListeners.size(); i++) {
                ButtonEventListener listener = (ButtonEventListener)mEventListeners.get(i);
                listener.onButtonPressed(event);
            }
        }
    }


    @Override
    public void onTap(float x, float y) {
        x = x - mLowerCornerPos.x;
        y = y - mLowerCornerPos.y;
        Log.e("Debug", "Click pos: x: " + x + " y: " + y);
        Log.e("Debug", "Button " + mId + " borders: x: " + mMetrics[0] + " y: " + mMetrics[1]);
        if (x > 0 && x < mMetrics[0]) {
            if (y > 0 && y < mMetrics[1]) {
                Log.e("Debug", "Button " + mId + " pressed");
                fireButtonPressedEvent();
            }
        }
    }
}
