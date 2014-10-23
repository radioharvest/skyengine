package aq.oceanbase.skyscroll.graphics.elements.window;

/**
 * Window is container class for different types of info: text, images, etc
 * Window is rectangular plane that can be rotated with the content
 */

import android.content.Context;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.elements.window.blocks.Button;
import aq.oceanbase.skyscroll.graphics.elements.window.blocks.ButtonBlock;
import aq.oceanbase.skyscroll.graphics.elements.window.blocks.WindowContent;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.graphics.render.Renderable;
import aq.oceanbase.skyscroll.logic.Game;
import aq.oceanbase.skyscroll.logic.events.WindowEvent;
import aq.oceanbase.skyscroll.logic.events.WindowEventListener;
import aq.oceanbase.skyscroll.touch.TouchHandler;
import aq.oceanbase.skyscroll.utils.math.Vector3f;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.logic.Question;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//TODO: add touch handling.
public class Window extends TouchHandler implements Renderable {
    public static enum ALIGN {
        LEFT, RIGHT, TOP, BOTTOM
    }

    private ShortBuffer mOrderBuffer;
    private FloatBuffer mVertexBuffer;

    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private boolean mInitialized = false;

    private int mShaderProgram;

    private Vector3f mPos;          //the position of upper left corner
    private int[] mWindowPixelMetrics;
    private float[] mWindowMetrics;
    private float[] mColor;
    private float mOpacity;

    private WindowLayout mLayout;

    private int mBorderPixelOffset;
    private float mBorderOffset;
    private float mBorderWidth;

    private List<Object> mEventListeners = new ArrayList<Object>();

    private int mFontSize;
    private Typeface mTypeface;

    private long mTimer = -1;
    private int mClosingTime = 700;
    private int mBlinkPeriod = 300;

    private Question mQuestion;

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

        float fWidth = ((dx/(screenMetrics[2]/2 - screenX))*(-mPos.x)) - mPos.x;       //calc width
        float fHeight = ((dy/(screenMetrics[3]/2 - screenY))*(-(mPos.y - cam.getPosY()))) - (mPos.y - cam.getPosY());      //calc height
        this.mWindowMetrics = new float[] {Math.abs(fWidth), Math.abs(fHeight)};
        this.mWindowPixelMetrics = new int[] {width, height};

        this.setDefaultSettings();
    }

    public Window (Vector3f position, float width, float height, Camera cam, int[] screenMetrics) {
        this(position.x, position.y, position.z, width, height, cam, screenMetrics);
    }

    public Window (int screenX, int screenY, float depth, Camera cam, int[] screenMetrics) {
        this(screenX, screenY, depth, screenMetrics[2] - 2*screenX, screenMetrics[3] - 2*screenY, cam, screenMetrics);
    }

    public Window (int offset, float depth, Camera cam, int[] screenMetrics) {
        this(offset, offset, depth, screenMetrics[2] - 2*offset, screenMetrics[3] - 2*offset, cam, screenMetrics);
    }




    //<editor-fold desc="Getters and Setters">
    public Vector3f getPosition() {
        return mPos;
    }

    public float[] getColor() {
        return mColor;
    }

    public float[] getWindowMetrics() {
        return mWindowMetrics;
    }

    public float getBorderOffset() {
        return this.mBorderOffset;
    }

    public int[] getWindowPixelMetrics() {
        return mWindowPixelMetrics;
    }

    public int getFontSize() {
        return mFontSize;
    }

    public float[] getModelMatrix() { return this.mModelMatrix; }

    public float[] getMVPMatrix() {
        return this.mMVPMatrix;
    }

    public Typeface getTypeface() {
        return this.mTypeface;
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
        this.mColor = new float[] {1.0f, 1.0f, 1.0f, 0.3f};
        this.mFontSize = 10;

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

        this.mLayout = new WindowLayout(WindowLayout.LAYOUT.VERTICAL, this, 1.0f);
        this.mLayout.setMetrics(pos, width, height, pixelMetrics);
    }

    public void addQuestion(Question question) {
        this.mQuestion = question;

        Log.e("Debug", "Win Metrics: " + mPos.x + " " + mPos.y + " " + mPos.z + " " + mWindowMetrics[0] + " " + mWindowMetrics[1]);

        this.mLayout.addChild(new WindowContent(this, 1, mQuestion.getBody(), 35));
        this.mLayout.addChild(new ButtonBlock(this, 1, mQuestion.getVariants(), mBorderOffset, 0.0f));
    }
    //</editor-fold>


    //<editor-fold desc="Touch functions">
    public void onButtonPressed(ButtonBlock buttonBlock, int buttonId) {
        mTimer = new Date().getTime();

        if (buttonId == mQuestion.getAnswer()) {
            buttonBlock.highlightButton(buttonId, Button.STATE.CORRECT);
            fireAnswerEvent(Game.ANSWER.CORRECT);
        }
        else {
            buttonBlock.highlightButton(buttonId, Button.STATE.WRONG);
            fireAnswerEvent(Game.ANSWER.WRONG);
        }
    }
    //</editor-fold>


    //<editor-fold desc="Event functions">
    public void addWindowEventListener(Object obj) {
        mEventListeners.add(obj);
    }

    public void removeWindowEventListener(Object obj) {
        mEventListeners.remove(obj);
    }

    private void fireCloseEvent() {
        WindowEvent event = new WindowEvent(this);

        if (!mEventListeners.isEmpty()) {
            for (int i = 0; i < mEventListeners.size(); i++) {
                WindowEventListener listener = (WindowEventListener)mEventListeners.get(i);
                listener.onClose(event);
            }
        }
    }

    private void fireAnswerEvent(Game.ANSWER answer) {
        WindowEvent event = new WindowEvent(this, answer);

        if (!mEventListeners.isEmpty()) {
            for (int i = 0; i < mEventListeners.size(); i++) {
                WindowEventListener listener = (WindowEventListener)mEventListeners.get(i);
                listener.onAnswer(event);
            }
        }
    }
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

        GLES20.glVertexAttrib4f(colorHandler, mColor[0], mColor[1], mColor[2], mColor[3]);
        GLES20.glDisableVertexAttribArray(colorHandler);

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);     //S*alpha + D*(1-alpha)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderBuffer);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }
    //</editor-fold>


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
    public void onTap(float x, float y) {
        mLayout.onTap(x - mPos.x, y - mPos.y);
        Log.e("Touch", "WindowTap: " + x + " " + y);
    }


    public boolean isInitialized() {
        return this.mInitialized;
    }


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

        this.mInitialized = true;
    }

    public void release() {
        GLES20.glDeleteProgram(mShaderProgram);
        mLayout.release();

        this.mInitialized = false;
    }


    private void update() {
        if (mTimer != -1 && new Date().getTime() - mTimer >= mClosingTime) fireCloseEvent();
    }

    public void draw(Camera cam) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, mPos.x, mPos.y, mPos.z);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);     //S*alpha + D*(1-alpha)
        this.drawWindow(cam);
        this.mLayout.draw(cam);

        this.update();
    }
}