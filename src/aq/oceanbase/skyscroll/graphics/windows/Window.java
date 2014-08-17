package aq.oceanbase.skyscroll.graphics.windows;

/**
 * Window is container class for different types of info: text, images, etc
 * Window is rectangular plane that can be rotated with the content
 */

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.Renderable;
import aq.oceanbase.skyscroll.loaders.ShaderLoader;
import aq.oceanbase.skyscroll.loaders.TextureLoader;
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

    private WindowContent mContent = null;

    private int mWindowProgram;
    private int mContentProgram;
    private int mTextureHandler;

    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private FloatBuffer mWindowVertexBuffer;
    private FloatBuffer mContentVertexBuffer;
    private ShortBuffer mOrderDataBuffer;

    public Window (float x, float y, float z, float width, float height, Camera cam, int[] screenMetrics) {
        this.mPos = new Vector3f(x, y, z);
        this.mWindowMetrics = new float[] {width, height};

        this.computePixelMetrics(cam, screenMetrics);
        this.setDefaultSettings();

        String text = "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30";
        this.mContent = new WindowContent(mWindowPixelMetrics, text, "Roboto-Regular.ttf");
    }

    public Window (int screenX, int screenY, float depth, int width, int height, Camera cam, int[] screenMetrics) {
        this.mPos = new TouchRay(screenX, screenY, 1.0f, cam, screenMetrics).getPointPositionOnRay(depth);
        int dx = (screenX + width) - screenMetrics[2]/2;           //Bx - Sx/2
        int dy = ((screenY + height) - screenMetrics[3]/2);        //By - Sy/2

        float fWidth = ((dx/(screenMetrics[2]/2 - screenX))*(-this.mPos.x)) - mPos.x;       //calc width
        float fHeight = ((dy/(screenMetrics[3]/2 - screenY))*(-this.mPos.y)) - mPos.y;      //calc height
        this.mWindowMetrics = new float[] {Math.abs(fWidth), Math.abs(fHeight)};
        this.mWindowPixelMetrics = new int[] {width, height};

        String text = "Alright then, picture this if you will:\n" +
                "10 to 2 AM, X, Yogi DMT, and a box of Krispy Kremes, in my \"need to know\" post, just outside of Area 51.\n" +
                "Contemplating the whole \"chosen people\" thing with just a flaming stealth banana split the sky like one would hope but never really expect to see in a place like this.\n" +
                "Cutting right angle donuts on a dime and stopping right at my Birkenstocks, and me yelping...\n" +
                "Holy fucking shit!\n" +
                "\n" +
                "Then the X-Files being, looking like some kind of blue-green Jackie Chan with Isabella Rossellini lips and breath that reeked of vanilla Chig Champa,\n" +
                "did a slow-mo Matrix descent out of the butt end of the banana vessel and hovered above my bug-eyes, my gaping jaw, and my sweaty L. Ron Hubbard upper lip and all I could think was: \"I hope Uncle Martin here doesn't notice that I pissed my fuckin' pants.\"\n" +
                "\n" +
                "So light in his way,\n" +
                "Like an apparition,\n" +
                "He had me crying out,\n" +
                "\"Fuck me,\n" +
                "It's gotta be,\n" +
                "Deadhead Chemistry,\n" +
                "The blotter got right on top of me,\n" +
                "Got me seein' E-motherfuckin'-T!\"\n" +
                "\n" +
                "And after calming me down with some orange slices and some fetal spooning, E.T. revealed to me his singular purpose.\n" +
                "He said, \"You are the Chosen One, the One who will deliver the message. A message of hope for those who choose to hear it and a warning for those who do not.\"\n" +
                "Me. The Chosen One?\n" +
                "They chose me!!!\n" +
                "And I didn't even graduate from fuckin' high school.";

        String testText = "ddddddddddddddddd ffffffffffffff aaaaaaaaaa kkkk ddddddddddd ppppppppp\n" +
                "fffffff aaaaaaa kkkkk ssssssss mmmmmm zzzzzzzzzzz vvvvvvvvvvvv llllllllll\n" +
                "qqqqqqqqqqqqqqqq tttttttttt yyyyyyyyyyyy uuuuuuuuuu hhhhhhhhhhhh ggggggggg";

        String shortText = "Alright then, picture this if you will:\n" +
                "10 to 2 AM, X, Yogi DMT, and a box of Krispy Kremes, in my \"need to know\" post, just outside of Area 51.\n" +
                "Contemplating the whole \"chosen people\" thing with just a flaming stealth banana split the sky like one would hope but never really expect to see in a place like this.\n" +
                "Cutting right angle donuts on a dime and stopping right at my Birkenstocks, and me yelping...\n";
        this.mContent = new WindowContent(mWindowPixelMetrics, text, "Roboto-Regular.ttf");
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
    }


    public void scrollContent(int amount) {
        if (mContent != null) {
            float uvAmount = (float)amount / (float)mContent.getHeight();
            Log.e("Draw", new StringBuilder("Amount: ").append(amount).append(" UV: ").append(uvAmount).toString());
            Log.e("Draw", new StringBuilder("UL: ").append(mContent.mUpperLimit).toString());
            mContent.mTexRgn.moveVertically(uvAmount, 0.0f, mContent.mUpperLimit);
            mContent.rebuildTextureBuffer();
        }
    }


    private void drawWindow(Camera cam) {
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
        GLES20.glLineWidth(this.mBorderWidth);
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);     //S*alpha + D*(1-alpha)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderDataBuffer);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void drawContent() {
        int MVPMatrixHandler = GLES20.glGetUniformLocation(mContentProgram, "u_MVPMatrix");
        int textureUniformHandler = GLES20.glGetUniformLocation(mContentProgram, "u_Texture");

        int positionHandler = GLES20.glGetAttribLocation(mContentProgram, "a_Position");
        int texCoordHandler = GLES20.glGetAttribLocation(mContentProgram, "a_TexCoordinate");

        GLES20.glUseProgram(mContentProgram);

        //Matrix.multiplyMM(mMVPMatrix, 0, cam.getViewM(), 0, mModelMatrix, 0);
        //Matrix.multiplyMM(mMVPMatrix, 0, cam.getProjM(), 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(MVPMatrixHandler, 1, false, mMVPMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHandler);
        GLES20.glUniform1i(textureUniformHandler, 0);

        mContentVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT, false, 0, mContentVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandler);

        GLES20.glVertexAttribPointer(texCoordHandler, 2, GLES20.GL_FLOAT, false, 0, mContent.getTextureBuffer());
        GLES20.glEnableVertexAttribArray(texCoordHandler);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderDataBuffer);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }


    public void initialize(Context context, String shaderFolder) {
        float[] windowVertexData = new float[] {
            0.0f, 0.0f, 0.0f,      //TL
            0.0f, -mWindowMetrics[1], 0.0f,     //BL
            mWindowMetrics[0], -mWindowMetrics[1], 0.0f,     //BR
            mWindowMetrics[0], 0.0f, 0.0f       //TR
        };

        float[] contentVertexData = new float[] {
            mBorderOffset, -mBorderOffset, 0.0f,
            mBorderOffset, mBorderOffset - mWindowMetrics[1], 0.0f,
            mWindowMetrics[0] - mBorderOffset, mBorderOffset - mWindowMetrics[1], 0.0f,
            mWindowMetrics[0] - mBorderOffset, -mBorderOffset, 0.0f
        };

        short[] orderData = new short[]
                {
                        0, 1, 3,
                        3, 1, 2
                };

        mWindowVertexBuffer = ByteBuffer.allocateDirect(windowVertexData.length * MainRenderer.mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mWindowVertexBuffer.put(windowVertexData).position(0);

        mContentVertexBuffer = ByteBuffer.allocateDirect(contentVertexData.length * ( Float.SIZE/8 ))
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mContentVertexBuffer.put(contentVertexData).position(0);

        mOrderDataBuffer = ByteBuffer.allocateDirect(orderData.length * MainRenderer.mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mOrderDataBuffer.put(orderData).position(0);


        if (mContent != null) {
            mContent.generateBitmap(context.getAssets());
            mTextureHandler = TextureLoader.loadTexture(mContent.getBitmap());
            //mTextureHandler = TextureLoader.loadTexture(context, R.drawable.bckgnd1);
        }


        mWindowProgram = ShaderLoader.
                getShaderProgram(shaderFolder + "/window/windowVertex.glsl", shaderFolder + "/window/windowFragment.glsl");

        mContentProgram = ShaderLoader.
                getShaderProgram(shaderFolder + "/window/windowContentVertex.glsl", shaderFolder + "/window/windowContentFragment.glsl");
    }

    public void draw(Camera cam) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, mPos.x, mPos.y, mPos.z);

        //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);     //S*alpha + D*(1-alpha)
        this.drawWindow(cam);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        this.drawContent();
        //GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
