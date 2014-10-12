package aq.oceanbase.skyscroll.graphics.elements.background;

import android.content.Context;
import android.opengl.GLES20;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.TextureRegion;
import aq.oceanbase.skyscroll.graphics.render.MainRenderer;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.graphics.render.Renderable;
import aq.oceanbase.skyscroll.utils.loaders.TextureLoader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Background implements Renderable {
    private boolean mInitialized = false;

    private ShortBuffer mOrderBuffer;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordinateBuffer;

    private TextureRegion mTexRgn;

    private int mTextureId;
    private int mTextureHandler;

    private int mShaderProgram;

    public Background(final int resourceId, float shiftFactor) {

        this.mTexRgn = new TextureRegion();
        mTexRgn.v1 = 1.0f - shiftFactor;

        this.mTextureId = resourceId;

        float [] vertexData =
                new float[] {
                        -1.0f,  1.0f, 0.0f,
                        -1.0f, -1.0f, 0.0f,
                        1.0f, -1.0f, 0.0f,
                        1.0f,  1.0f, 0.0f
                };

        float[] textureCoordinateData =
                new float[] {
                        mTexRgn.u1, mTexRgn.v1,
                        mTexRgn.u1, mTexRgn.v2,
                        mTexRgn.u2, mTexRgn.v2,
                        mTexRgn.u2, mTexRgn.v1
                };

        short[] orderData =
                new short[] {
                        0, 1, 3,
                        3, 1, 2
                };

        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * MainRenderer.mBytesPerFloat).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(vertexData).position(0);

        mTextureCoordinateBuffer = ByteBuffer.allocateDirect(textureCoordinateData.length * MainRenderer.mBytesPerFloat).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureCoordinateBuffer.put(textureCoordinateData).position(0);

        mOrderBuffer = ByteBuffer.allocateDirect(orderData.length * (Short.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asShortBuffer();
        mOrderBuffer.put(orderData).position(0);
    }

    public Background(final int resourceId) {
        this(resourceId, 1.0f);
    }

    public void setShift(float amount, float factor) {
        mTexRgn.v1 = 1.0f - factor * (amount+1);        // Remember, that 0, 0 UV coordinate is UP left
        mTexRgn.v2 = 1.0f - factor * amount;            // NOT DOWN left.

        updateTexRegion();
    }

    private void updateTexRegion() {
        float[] textureCoordinateData =
                new float[] {
                        mTexRgn.u1, mTexRgn.v1,
                        mTexRgn.u1, mTexRgn.v2,
                        mTexRgn.u2, mTexRgn.v2,
                        mTexRgn.u2, mTexRgn.v1
                };

        mTextureCoordinateBuffer.clear();
        mTextureCoordinateBuffer.put(textureCoordinateData, 0, 8);
        mTextureCoordinateBuffer.flip();
    }

    public boolean isInitialized() {
        return this.mInitialized;
    }

    public void initialize(Context context, ProgramManager programManager) {
        mShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.BACKGROUND);

        mTextureHandler = TextureLoader.loadTexture(context, mTextureId);

        this.mInitialized = true;
    }

    public void release() {
        //TODO: FILL
    }

    public void draw(Camera cam) {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(mShaderProgram);

        int textureUniformHandler = GLES20.glGetUniformLocation(mShaderProgram, "u_Texture");

        int positionHandler = GLES20.glGetAttribLocation(mShaderProgram, "a_Position");
        int texCoordHandler = GLES20.glGetAttribLocation(mShaderProgram, "a_TexCoordinate");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHandler);
        GLES20.glUniform1i(textureUniformHandler, 0);

        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandler);

        mTextureCoordinateBuffer.position(0);
        GLES20.glVertexAttribPointer(texCoordHandler, 2, GLES20.GL_FLOAT, false, 0, mTextureCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(texCoordHandler);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderBuffer);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
}
