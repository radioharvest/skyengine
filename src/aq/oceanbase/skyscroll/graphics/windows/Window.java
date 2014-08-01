package aq.oceanbase.skyscroll.graphics.windows;

/**
 * Window is container class for different types of info: text, images, etc
 * Window is rectangular plane that can be rotated with the content
 */

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.Renderable;
import aq.oceanbase.skyscroll.loaders.ShaderLoader;
import aq.oceanbase.skyscroll.math.Vector3f;
import aq.oceanbase.skyscroll.render.MainRenderer;
import aq.oceanbase.skyscroll.touch.TouchRay;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Window implements Renderable {
    private Vector3f mPos;          //the position of upper left corner
    private int[] mWindowPixelMetrics;
    private float[] mWindowMetrics;
    private float[] mColor;
    private float mOpacity;

    private float mBorderOffset;
    private float mBorderWidth;

    private int mFontSize;
    private int mSymbolsAmount;

    private int mShaderProgram;
    private float[] mModelMatrix = new float[16];;
    private FloatBuffer mVertexDataBuffer;
    private ShortBuffer mOrderDataBuffer;

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

        float fWidth = ((dx/(screenMetrics[2]/2 - screenX))*(-this.mPos.x)) - mPos.x;       //calc width
        float fHeight = ((dy/(screenMetrics[3]/2 - screenY))*(-this.mPos.y)) - mPos.y;      //calc height
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

    public int getSymbolsAmount() {
        return mSymbolsAmount;
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
        this.mSymbolsAmount = (int)Math.floor( (mWindowPixelMetrics[0]*(1 - (2*mBorderOffset/mWindowMetrics[0]))) / mFontSize );
    }
    //</editor-fold>


    private int getLowerSide() {
        if (mWindowPixelMetrics[0] <= mWindowPixelMetrics[1]) return 0;
        else return 1;
    }

    private void setDefaultSettings() {
        int ls = this.getLowerSide();         //lower side

        this.mBorderOffset = mWindowMetrics[ls]/20;              //offset is set to 5% by default
        this.mBorderWidth = 3.0f;
        this.mColor = new float[] {1.0f, 1.0f, 1.0f, 0.3f};
        this.mFontSize = 10;
        this.mSymbolsAmount = (int)Math.floor((mWindowPixelMetrics[0] * 0.9)/mFontSize);
    }

    public void computePixelMetrics(Camera cam, int[] screenMatrix) {
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
        this.mSymbolsAmount = (int)Math.floor( (mWindowPixelMetrics[0]*(1 - (2*mBorderOffset/mWindowMetrics[0]))) / mFontSize );
        Log.e("Draw", "YEAP");
    }


    private void drawPlane(Camera cam) {
        float[] MVPMatrix = new float[16];

        int MVPMatrixHandler = GLES20.glGetUniformLocation(mShaderProgram, "u_MVPMatrix");
        int positionHandler = GLES20.glGetAttribLocation(mShaderProgram, "a_Position");
        int colorHandler = GLES20.glGetAttribLocation(mShaderProgram, "a_Color");

        GLES20.glUseProgram(mShaderProgram);

        Matrix.multiplyMM(MVPMatrix, 0, cam.getViewM(), 0, mModelMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, cam.getProjM(), 0, MVPMatrix, 0);

        GLES20.glUniformMatrix4fv(MVPMatrixHandler, 1, false, MVPMatrix, 0);

        mVertexDataBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT, false, 3*MainRenderer.mBytesPerFloat, mVertexDataBuffer);
        GLES20.glEnableVertexAttribArray(positionHandler);

        GLES20.glVertexAttrib4f(colorHandler, mColor[0], mColor[1], mColor[2], mColor[3]);
        GLES20.glDisableVertexAttribArray(colorHandler);

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glLineWidth(this.mBorderWidth);
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);     //S*alpha + D*(1-alpha)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderDataBuffer);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void initialize(Context context, String shaderFolder) {
        float[] vertexData = new float[] {
            0.0f, 0.0f, 0.0f,      //UL
            0.0f, -this.mWindowMetrics[1], 0.0f,     //LL
            this.mWindowMetrics[0], -this.mWindowMetrics[1], 0.0f,     //LR
            this.mWindowMetrics[0], 0.0f, 0.0f       //UR
        };

        short[] orderData = new short[]
                {
                        0, 1, 3,
                        3, 1, 2
                };

        mVertexDataBuffer = ByteBuffer.allocateDirect(vertexData.length * MainRenderer.mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexDataBuffer.put(vertexData).position(0);

        mOrderDataBuffer = ByteBuffer.allocateDirect(orderData.length * MainRenderer.mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mOrderDataBuffer.put(orderData).position(0);

        mShaderProgram = ShaderLoader.
                getShaderProgram(shaderFolder + "/window/windowVertex.glsl", shaderFolder + "/window/windowFragment.glsl");
    }

    public void draw(Camera cam) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, mPos.x, mPos.y, mPos.z);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);     //S*alpha + D*(1-alpha)
        this.drawPlane(cam);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
