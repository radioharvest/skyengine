package aq.oceanbase.skyscroll.graphics.windows;

import aq.oceanbase.skyscroll.graphics.TextureRegion;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

public class Button {
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
    private float[] mMetrics;
    private int[] mPixelMetrics;

    private TextureRegion mTexRgn;

    private STATE mState;

    public Button (Vector3f position, float[] metrics, int[] pixelMetrics, String text) {
        this.mPos = position;
        this.mMetrics = metrics;
        this.mPixelMetrics = pixelMetrics;

        this.mState = STATE.NEUTRAL;
        this.mColor = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
        this.mText = text;
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

    public void setTexRgn (TextureRegion texRgn) {
        this.mTexRgn = texRgn;
    }

}
