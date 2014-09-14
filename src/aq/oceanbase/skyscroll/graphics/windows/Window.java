package aq.oceanbase.skyscroll.graphics.windows;

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
import aq.oceanbase.skyscroll.graphics.ProgramManager;
import aq.oceanbase.skyscroll.graphics.Renderable;
import aq.oceanbase.skyscroll.graphics.SpriteBatch;
import aq.oceanbase.skyscroll.utils.loaders.TextureLoader;
import aq.oceanbase.skyscroll.utils.math.Vector2f;
import aq.oceanbase.skyscroll.utils.math.Vector3f;
import aq.oceanbase.skyscroll.graphics.render.MainRenderer;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.logic.Question;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Window implements Renderable {
    public static enum ALIGN {
        LEFT, RIGHT, TOP, BOTTOM
    }

    private boolean mInitialized = false;

    private Vector3f mPos;          //the position of upper left corner
    private int[] mWindowPixelMetrics;
    private float[] mWindowMetrics;
    private float[] mColor;
    private float mOpacity;

    private int mBorderPixelOffset;
    private float mBorderOffset;

    private float mBorderWidth;

    private int mFontSize;

    private WindowContent mContent = null;
    private ButtonBlock mButtonBlock = null;

    private int mWindowProgram;
    private int mContentProgram;

    private int mContentTextureHandle;
    private int mButtonBlockTextureHandle;

    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private FloatBuffer mWindowVertexBuffer;
    private FloatBuffer mContentVertexBuffer;
    private ShortBuffer mOrderDataBuffer;

    private SpriteBatch mButtonBatch;

    private Question mQuestion;

    public Window (float x, float y, float z, float width, float height, Camera cam, int[] screenMetrics) {
        this.mPos = new Vector3f(x, y, z);
        this.mWindowMetrics = new float[] {width, height};

        this.computePixelMetrics(cam, screenMetrics);
        this.setDefaultSettings();

        String text = "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30";
        this.mContent = new WindowContent(mWindowPixelMetrics, text);
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

    public float getOpacity() {
        return mOpacity;
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

    public float getBorderWidth() {
        return mBorderWidth;
    }

    public int getFontSize() {
        return mFontSize;
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

    public void setQuestion(Question question) {
        this.mQuestion = question;

        this.addContentBlock(new float[] {1.0f, 0.6f}, mQuestion.getBody());
        this.addButtonBlock(new float[] {1.0f, 0.4f}, mQuestion.getVariants());
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
    }

    public void rotate90() {
        float temp;
        this.mWindowMetrics = new float[] {mWindowMetrics[1], mWindowMetrics[0]};
        this.mWindowPixelMetrics = new int[] {mWindowPixelMetrics[1], mWindowPixelMetrics[0]};
        this.mPos = new Vector3f(mPos.y, mPos.x, mPos.z);
    }
    //</editor-fold>


    //<editor-fold desc="Block functions">
    public void addContentBlock(float[] sectionMetrics, String text) {
        this.addContentBlock(sectionMetrics, ALIGN.LEFT, ALIGN.TOP, text);
    }

    public void addContentBlock(float[] sectionMetrics, ALIGN horizontalAlign, ALIGN verticalAlign, String text) {
        int[] pixelMetrics = new int[2];

        if (sectionMetrics[0] > 1.0f) sectionMetrics[0] = 1.0f;
        if (sectionMetrics[0] < 0.0f) sectionMetrics[0] = 0.0f;

        if (sectionMetrics[1] > 1.0f) sectionMetrics[1] = 1.0f;
        if (sectionMetrics[1] < 0.0f) sectionMetrics[1] = 0.0f;

        pixelMetrics[0] = (int)( (mWindowPixelMetrics[0] - 2*mBorderPixelOffset)*sectionMetrics[0] );
        pixelMetrics[1] = (int)( (mWindowPixelMetrics[1] - 2*mBorderPixelOffset)*sectionMetrics[1] );

        Vector2f topLeft = new Vector2f(0, 0);
        Vector2f bottomRight = new Vector2f(0, 0);

        if (horizontalAlign == ALIGN.RIGHT) {
            topLeft.x = mBorderOffset + ( (1.0f - sectionMetrics[0]) * (mWindowMetrics[0] - 2*mBorderOffset) );
        } else {
            topLeft.x = mBorderOffset;
        }
        bottomRight.x = topLeft.x + (sectionMetrics[0] * (mWindowMetrics[0] - 2*mBorderOffset));

        if (verticalAlign == ALIGN.BOTTOM) {
            topLeft.y = -mBorderOffset - ( (1.0f - sectionMetrics[1]) * (mWindowMetrics[1] - 2*mBorderOffset) );
        } else {
            topLeft.y = -mBorderOffset;
        }
        bottomRight.y = topLeft.y - (sectionMetrics[1] * (mWindowMetrics[1] - 2*mBorderOffset));


        float[] contentVertexData = new float[] {
                topLeft.x, topLeft.y, 0.0f,
                topLeft.x, bottomRight.y, 0.0f,
                bottomRight.x, bottomRight.y, 0.0f,
                bottomRight.x, topLeft.y, 0.0f
        };


        mContentVertexBuffer = ByteBuffer.allocateDirect(contentVertexData.length * ( Float.SIZE/8 ))
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mContentVertexBuffer.put(contentVertexData).position(0);

        mContent = new WindowContent(pixelMetrics, text);
    }

    public void addButtonBlock(float[] sectionMetrics, String[] buttons) {
        addButtonBlock(sectionMetrics, ALIGN.LEFT, ALIGN.BOTTOM, buttons);
    }

    public void addButtonBlock(float[] sectionMetrics, ALIGN horizontalAlign, ALIGN verticalAlign, String[] buttons) {
        float[] metrics = new float[2];
        int[] pixelMetrics = new int[2];

        if (sectionMetrics[0] > 1.0f) sectionMetrics[0] = 1.0f;
        if (sectionMetrics[0] < 0.0f) sectionMetrics[0] = 0.0f;

        if (sectionMetrics[1] > 1.0f) sectionMetrics[1] = 1.0f;
        if (sectionMetrics[1] < 0.0f) sectionMetrics[1] = 0.0f;

        pixelMetrics[0] = (int)( mWindowPixelMetrics[0]*sectionMetrics[0] );
        pixelMetrics[1] = (int)( mWindowPixelMetrics[1]*sectionMetrics[1] );

        Vector3f pos = new Vector3f(0.0f, 0.0f, 0.0f);

        if (horizontalAlign == ALIGN.RIGHT) {
            pos.x = ( (1.0f - sectionMetrics[0]) * mWindowMetrics[0] );
        } else {
            pos.x = 0;
        }
        metrics[0] = (sectionMetrics[0] * mWindowMetrics[0]);

        if (verticalAlign == ALIGN.TOP) {
            pos.y = 0;
        } else {
            pos.y = -( (1.0f - sectionMetrics[1]) * mWindowMetrics[1] );
        }
        metrics[1] = (sectionMetrics[1] * mWindowMetrics[1]);

        mButtonBlock = new ButtonBlock(pos, metrics, pixelMetrics, mBorderOffset, buttons);
    }
    //</editor-fold>


    //<editor-fold desc="Touch functions">
    public void scrollContent(int amount) {
        if (mContent != null && !(mContent.mTexRgn.v1 == 0.0f && mContent.mTexRgn.v2 == 1.0f)) {
            float uvAmount = (float)amount / (float)mContent.getHeight();
            mContent.mTexRgn.moveVertically(uvAmount, 0.0f, mContent.mUpperLimit);
            mContent.rebuildTextureBuffer();
        }
    }

    public boolean pressButton(int inputTouchX, int inputTouchY, Camera cam, int[] screenMetrics) {
        int buttonId;
        Vector3f touch = new TouchRay(inputTouchX, inputTouchY, 1.0f, cam, screenMetrics)
                .getPointPositionOnRay(cam.getPosZ() - this.mPos.z);

        touch = touch.subtractV(mPos).subtractV(mButtonBlock.getPos());
        buttonId = mButtonBlock.findPressedButton(touch.x, touch.y);

        if (buttonId != -1) {
            if (buttonId == mQuestion.getAnswer()) return true;
            else mButtonBlock.highlightButton(buttonId);
        }

        return false;
    }
    //</editor-fold>


    //<editor-fold desc="Draw functions">
    private void drawWindow(Camera cam) {
        //this.mPos.print("Draw", "WindowPos");
        int MVPMatrixHandler = GLES20.glGetUniformLocation(mWindowProgram, "u_MVPMatrix");
        int positionHandler = GLES20.glGetAttribLocation(mWindowProgram, "a_Position");
        int colorHandler = GLES20.glGetAttribLocation(mWindowProgram, "a_Color");

        GLES20.glUseProgram(mWindowProgram);

        Matrix.multiplyMM(mMVPMatrix, 0, cam.getViewM(), 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, cam.getProjM(), 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(MVPMatrixHandler, 1, false, mMVPMatrix, 0);

        mWindowVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT, false, 0, mWindowVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandler);

        GLES20.glVertexAttrib4f(colorHandler, mColor[0], mColor[1], mColor[2], mColor[3]);
        GLES20.glDisableVertexAttribArray(colorHandler);

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        //GLES20.glLineWidth(this.mBorderWidth);
        //GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);     //S*alpha + D*(1-alpha)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderDataBuffer);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void drawContent() {
        int MVPMatrixHandler = GLES20.glGetUniformLocation(mContentProgram, "u_MVPMatrix");
        int textureUniformHandler = GLES20.glGetUniformLocation(mContentProgram, "u_Texture");

        int colorHandler = GLES20.glGetAttribLocation(mContentProgram, "a_Color");
        int positionHandler = GLES20.glGetAttribLocation(mContentProgram, "a_Position");
        int texCoordHandler = GLES20.glGetAttribLocation(mContentProgram, "a_TexCoordinate");

        GLES20.glUseProgram(mContentProgram);

        //Matrix.multiplyMM(mMVPMatrix, 0, cam.getViewM(), 0, mModelMatrix, 0);
        //Matrix.multiplyMM(mMVPMatrix, 0, cam.getProjM(), 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(MVPMatrixHandler, 1, false, mMVPMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mContentTextureHandle);
        GLES20.glUniform1i(textureUniformHandler, 0);

        GLES20.glVertexAttrib4f(colorHandler, 1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glDisableVertexAttribArray(colorHandler);

        mContentVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT, false, 0, mContentVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandler);

        GLES20.glVertexAttribPointer(texCoordHandler, 2, GLES20.GL_FLOAT, false, 0, mContent.getTextureBuffer());
        GLES20.glEnableVertexAttribArray(texCoordHandler);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderDataBuffer);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    private void drawButtons(Camera cam) {
        float[] buttonMatrix = new float[16];
        float[] blockMatrix = new float[16];
        Vector3f pos = new Vector3f(mButtonBlock.getPos());

        Matrix.setIdentityM(blockMatrix, 0);
        Matrix.translateM(blockMatrix, 0, mModelMatrix, 0, pos.x, pos.y, pos.z);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mButtonBatch.beginBatch(cam);

        for ( int i = 0; i < mButtonBlock.getButtonsAmount(); i++ ) {

            pos = new Vector3f(mButtonBlock.getButton(i).getPos());

            float[] metrics = mButtonBlock.getButton(i).getMetrics();

            float[] color = mButtonBlock.getButton(i).getColor();

            Matrix.setIdentityM(buttonMatrix, 0);
            Matrix.translateM(buttonMatrix, 0, blockMatrix, 0, pos.x, pos.y, pos.z);

            //MathMisc.printMatrix(buttonMatrix, "Draw");

            mButtonBatch.batchElement(metrics[0], metrics[1], color, mButtonBlock.getButton(i).getTexRgn(), buttonMatrix);
        }

        mButtonBatch.endBatch();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
    //</editor-fold>


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

        mWindowVertexBuffer = ByteBuffer.allocateDirect(windowVertexData.length * MainRenderer.mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mWindowVertexBuffer.put(windowVertexData).position(0);

        mOrderDataBuffer = ByteBuffer.allocateDirect(orderData.length * MainRenderer.mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mOrderDataBuffer.put(orderData).position(0);

        mWindowProgram = programManager.getProgram(ProgramManager.PROGRAM.WINDOW);

        Typeface tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");

        if (mContent != null) {
            mContent.generateBitmap(tf);
            mContentTextureHandle = TextureLoader.loadTexture(mContent.getBitmap());
            Log.e("Debug", new StringBuilder().append(mContent.bitmapRecycled()).toString());

            mContentProgram = programManager.getProgram(ProgramManager.PROGRAM.WINDOWCONTENT);
        }

        if (mButtonBlock != null) {
            mButtonBlock.generateBitmap(tf);
            //TODO: check if I really need that handle as a class member
            mButtonBlockTextureHandle = TextureLoader.loadTexture(mButtonBlock.getBitmap());

            mButtonBatch = new SpriteBatch(SpriteBatch.COLORED_VERTEX_3D, mButtonBlockTextureHandle);
            mButtonBatch.initialize(context, programManager);
        }

        this.mInitialized = true;
    }

    public void release() {
        GLES20.glDeleteProgram(mWindowProgram);
        if (mContent != null) {
            GLES20.glDeleteTextures(1, new int[] {mContentTextureHandle}, 0);
            GLES20.glDeleteProgram(mContentProgram);
        }
        if (mButtonBlock != null) {
            GLES20.glDeleteTextures(1, new int[] {mButtonBlockTextureHandle}, 0);
            //TODO: add button batch release
        }

        this.mInitialized = false;
    }

    public void draw(Camera cam) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, mPos.x, mPos.y, mPos.z);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);     //S*alpha + D*(1-alpha)
        this.drawWindow(cam);
        //GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        if (mContent != null) this.drawContent();
        if (mButtonBlock != null) this.drawButtons(cam);
        //if (mButtonBlock != null) this.drawButtons();
        //GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }
}