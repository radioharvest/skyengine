package aq.oceanbase.skyscroll.engine.logic.window;

/**
 * Window is container class for different types of info: text, images, etc
 * Window is rectangular plane that can be rotated with the content
 */

import android.content.Context;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.graphics.ProgramManager;
import aq.oceanbase.skyscroll.engine.graphics.RenderableObject;
import aq.oceanbase.skyscroll.engine.logic.events.ButtonEvent;
import aq.oceanbase.skyscroll.engine.logic.events.ButtonEventListener;
import aq.oceanbase.skyscroll.engine.logic.events.WindowEvent;
import aq.oceanbase.skyscroll.engine.logic.events.WindowEventListener;
import aq.oceanbase.skyscroll.engine.input.touch.Touchable;
import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;
import aq.oceanbase.skyscroll.engine.input.touch.TouchRay;
import aq.oceanbase.skyscroll.engine.utils.Timer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

//TODO: add touch handling.
//TODO: add game event listener
public class Window extends RenderableObject implements ButtonEventListener, Touchable {
    public static enum ALIGN {
        LEFT, RIGHT, TOP, BOTTOM
    }

    private ShortBuffer mOrderBuffer;
    private FloatBuffer mVertexBuffer;

    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int mShaderProgram;

    private Vector3f mPos;          //the position of upper left corner
    private int[] mWindowPixelMetrics;
    private float[] mWindowMetrics;
    private float[] mColor;
    private float mOpacity;

    protected WindowLayout mLayout;

    private int mBorderPixelOffset;
    private float mBorderOffset;
    private float mBorderWidth;

    protected List<Object> mEventListeners = new ArrayList<Object>();

    private int mFontSize;
    private Typeface mTypeface;

    protected int mCloseTime = 700;               //TODO: really needed in parent class?
    protected boolean mClosing = false;
    protected Timer mTimer;

    public Window (float x, float y, float z, float width, float height, Camera cam, int[] screenMetrics) {
        this.mPos = new Vector3f(x, y, z);
        this.mWindowMetrics = new float[] {width, height};

        this.computePixelMetrics(cam, screenMetrics);
        this.setDefaultSettings();
    }

    public Window (int screenX, int screenY, float depth, int width, int height, Camera cam, int[] screenMetrics) {
        this.mPos = new TouchRay(screenX, screenY, 1.0f, cam, screenMetrics).getPointPositionOnRay(depth);
        int dx = (screenX + width) - screenMetrics[2]/2;           //Bx - Sx/2
        int dy = ((screenY + height) - screenMetrics[3]/2);        //By - Sy/2

        this.mPos.print("Debug", "POSWHY");

        float fWidth = ((dx/(screenMetrics[2]/2 - screenX))*(-mPos.x)) - mPos.x;       //calc width
        float fHeight = ((dy/(screenMetrics[3]/2 - screenY))*(-(mPos.y - cam.getPosY()))) - (mPos.y - cam.getPosY());      //calc height
        this.mWindowMetrics = new float[] {Math.abs(fWidth), Math.abs(fHeight)};
        this.mWindowPixelMetrics = new int[] {width, height};

        Log.e("Debug", "TestsWhy: " + (screenMetrics[2]/2 - screenX) + " " + (dx/(screenMetrics[2]/2 - screenX)) + " " + ((dx/(screenMetrics[2]/2 - screenX))*(-mPos.x)));

        Log.e("Debug", "WindowMetrics: " + mWindowMetrics[0] + " " + mWindowMetrics[1]);
        this.setDefaultSettings();
    }

    public Window(Camera cam, int[] screenMetrics) {
        this.mPos = new Vector3f(-1.0f, 1.0f, cam.getPosZ() - 2.0f);

        this.mWindowMetrics = new float[] {2.0f, 2.0f};
        this.mWindowPixelMetrics = new int[] {screenMetrics[2], screenMetrics[3]};

        this.setDefaultSettings();
    }

    //<editor-fold desc="Additional constructors">
    public Window (Vector3f position, float width, float height, Camera cam, int[] screenMetrics) {
        this(position.x, position.y, position.z, width, height, cam, screenMetrics);
    }

    public Window (int screenX, int screenY, float depth, Camera cam, int[] screenMetrics) {
        this(screenX, screenY, depth, screenMetrics[2] - 2*screenX, screenMetrics[3] - 2*screenY, cam, screenMetrics);
    }

    public Window (int offset, float depth, Camera cam, int[] screenMetrics) {
        this(offset, offset, depth, screenMetrics[2] - 2*offset, screenMetrics[3] - 2*offset, cam, screenMetrics);
    }
    //</editor-fold>



    //<editor-fold desc="Getters and Setters">
    public Vector3f getPosition() {
        return mPos;
    }

    public float[] getModelMatrix() { return this.mModelMatrix; }

    public float[] getMVPMatrix() {
        return this.mMVPMatrix;
    }


    public float[] getColor() {
        return new float[] {mColor[0], mColor[1], mColor[2], mOpacity};
    }

    public float getOpacity() {
        return this.mOpacity;
    }


    public float getBorderOffset() {
        return this.mBorderOffset;
    }

    public float getBorderWidth() { return this.mBorderWidth; }


    public float[] getWindowMetrics() {
        return mWindowMetrics;
    }

    public int[] getWindowPixelMetrics() {
        return mWindowPixelMetrics;
    }


    public int getFontSize() {
        return mFontSize;
    }

    public Typeface getTypeface() {
        return this.mTypeface;
    }


    public Timer getTimer() {
        return mTimer;
    }

    public boolean isClosing() {
        return this.mClosing;
    }


    public boolean setColor(float[] color) {
        if (color.length != 4) return false;
        else this.mColor = color;
        return true;
    }

    public void setOpacity(float opacity) {
        if (opacity <= 1.0f || opacity >= 0.0f) {
            this.mOpacity = opacity;
        }
    }

    public boolean setBorderOffset(int offset) {
        int ls = getLowerSide();         //ls (lower side) is needed to prevent offset overlapping

        if (offset >= mWindowPixelMetrics[ls]/2) return false;       //prevent offset overlapping

        this.mBorderOffset = (mWindowMetrics[ls] * offset)/mWindowPixelMetrics[ls];
        return true;
    }

    public void setFontSize(int size) {
        this.mFontSize = size;
    }
    //</editor-fold>


    //<editor-fold desc="Metric functions">
    private int getLowerSide() {
        if (mWindowPixelMetrics[0] <= mWindowPixelMetrics[1]) return 0;
        else return 1;
    }

    private void setDefaultSettings() {
        int ls = this.getLowerSide();         //lower side

        this.mBorderOffset = mWindowMetrics[ls]/20;              //offset is set to 5% by default
        this.mBorderPixelOffset = (int)(((mBorderOffset) / mWindowMetrics[0]) * mWindowPixelMetrics[0]);

        this.mBorderWidth = 3.0f;
        this.mColor = new float[] {1.0f, 1.0f, 1.0f};
        this.mOpacity = 0.3f;
        this.mFontSize = 10;

        Log.e("Debug", "WindowMetricsBeforeLayout: " + mWindowPixelMetrics[0] + " " + mWindowPixelMetrics[1]);

        this.addLayout(WindowLayout.LAYOUT.VERTICAL);
    }

    private void computePixelMetrics(Camera cam, int[] screenMatrix) {
        Vector3f lowerCorner = new Vector3f(mPos.x + mWindowMetrics[0], mPos.y - mWindowMetrics[1], mPos.z);
        float[] VPMatrix = new float[16];
        float[] result = new float[4];
        int[] upLeft;
        int[] downRight;
        //Matrix.multiplyMM(MVPMatrix, 0, cam.getViewM(), 0, mModelMatrix, 0);
        Matrix.multiplyMM(VPMatrix, 0, cam.getProjM(), 0, cam.getViewM(), 0);

        Matrix.multiplyMV(result, 0, VPMatrix, 0, mPos.toArray4f(), 0);
        upLeft = new int[] {
                (int)Math.ceil( (result[0] + 1.0f)/2 * screenMatrix[2] ),
                (int)Math.ceil( (1.0f - (result[1] + 1.0f)/2) * screenMatrix[3])
        };


        Matrix.multiplyMV(result, 0, VPMatrix, 0, lowerCorner.toArray4f(), 0);
        downRight = new int[] {
                (int)Math.floor( (result[0] + 1.0f)/2 * screenMatrix[2]),
                (int)Math.floor( (1.0f - (result[1] + 1.0f)/2) * screenMatrix[3])
        };

        this.mWindowPixelMetrics = new int[] {downRight[0] - upLeft[0], upLeft[1] - downRight[1]};
        Log.e("Debug", "WindowPixelMetrics: " + mWindowPixelMetrics[0] + " " + mWindowPixelMetrics[1]);
    }
    //</editor-fold>


    //<editor-fold desc="Block functions">
    private void addLayout(WindowLayout.LAYOUT type) {
        int[] pixelMetrics = {mWindowPixelMetrics[0] - 2*mBorderPixelOffset, mWindowPixelMetrics[1] - 2*mBorderPixelOffset};

        Vector3f pos = new Vector3f(mBorderOffset, -mBorderOffset, 0);

        float width = mWindowMetrics[0] - 2*mBorderOffset;
        float height = (mWindowMetrics[1] - 2*mBorderOffset);

        Log.e("Debug", "Layout debug windowMetrics: " + mWindowMetrics[0] + " " + mWindowMetrics[1]);

        this.mLayout = new WindowLayout(type, this, 1.0f);
        Log.e("Debug", "Layout params set: " + width + " " + height);
        pos.print("Debug", "pos");
        this.mLayout.setMetrics(pos, width, height, pixelMetrics);
    }
    //</editor-fold>


    //<editor-fold desc="Touch functions">
    @Override
    public void onSwipeHorizontal(float amount) {
        mLayout.onSwipeHorizontal(amount);
    }

    @Override
    public void onSwipeVertical(float amount) {
        mLayout.onSwipeVertical(amount);
    }

    @Override
    public void onScale(float span) {
        mLayout.onScale(span);
    }

    @Override
    public void onTap(TouchRay touchRay) {
        Vector3f touch = touchRay.getPointPositionOnRay(touchRay.getNearPointV().z - mPos.z);

        mLayout.onTap(touch.x - mPos.x, touch.y - mPos.y);
        Log.e("Touch", "WindowTap: " + touch.x + " " + touch.y);
    }
    //</editor-fold>


    //<editor-fold desc="Event functions">
    public void addWindowEventListener(Object obj) {
        mEventListeners.add(obj);
    }

    public void removeWindowEventListener(Object obj) {
        mEventListeners.remove(obj);
    }

    protected void fireCloseEvent() {
        WindowEvent event = new WindowEvent(this);

        if (!mEventListeners.isEmpty()) {
            for (int i = 0; i < mEventListeners.size(); i++) {
                WindowEventListener listener = (WindowEventListener)mEventListeners.get(i);
                listener.onClose(event);
            }
        }
    }

    @Override
    public void onButtonPressed(ButtonEvent e) {}
    //</editor-fold>


    //<editor-fold desc="Draw functions">
    private void drawWindow(Camera cam) {
        //this.mPos.print("Draw", "WindowPos");
        int MVPMatrixHandler = GLES20.glGetUniformLocation(mShaderProgram, "u_MVPMatrix");
        int positionHandler = GLES20.glGetAttribLocation(mShaderProgram, "a_Position");
        int colorHandler = GLES20.glGetAttribLocation(mShaderProgram, "a_Color");

        GLES20.glUseProgram(mShaderProgram);

        Matrix.multiplyMM(mMVPMatrix, 0, cam.getViewM(), 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, cam.getProjM(), 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(MVPMatrixHandler, 1, false, mMVPMatrix, 0);

        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandler);

        GLES20.glVertexAttrib4f(colorHandler, mColor[0], mColor[1], mColor[2], mOpacity);
        GLES20.glDisableVertexAttribArray(colorHandler);

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);     //S*alpha + D*(1-alpha)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderBuffer);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    protected void update() {
        mLayout.update();
    }
    //</editor-fold>


    @Override
    public void initialize(Context context, ProgramManager programManager) {
        float[] windowVertexData = new float[] {
            0.0f, 0.0f, 0.0f,      //TL
            0.0f, -mWindowMetrics[1], 0.0f,     //BL
            mWindowMetrics[0], -mWindowMetrics[1], 0.0f,     //BR
            mWindowMetrics[0], 0.0f, 0.0f       //TR
        };

        short[] orderData = new short[]
                {
                        0, 1, 3,
                        3, 1, 2
                };

        mVertexBuffer = ByteBuffer.allocateDirect(windowVertexData.length * (Float.SIZE / 8))
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(windowVertexData).position(0);

        mOrderBuffer = ByteBuffer.allocateDirect(orderData.length * (Short.SIZE / 8))
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mOrderBuffer.put(orderData).position(0);

        mShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.WINDOW);

        mTypeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");

        mLayout.initialize(context, programManager);

        super.initialize(context, programManager);
    }

    @Override
    public void release() {
        //GLES20.glDeleteProgram(mShaderProgram);
        mLayout.release();

        super.release();
    }


    @Override
    public void draw(Camera cam) {
        this.update();

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, mPos.x, mPos.y, mPos.z);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);     //S*alpha + D*(1-alpha)
        this.drawWindow(cam);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        this.mLayout.draw(cam);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
}