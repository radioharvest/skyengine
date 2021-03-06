package aq.oceanbase.skyscroll.engine.graphics.elements;

import android.content.Context;
import android.opengl.GLES20;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.graphics.TextureRegion;
import aq.oceanbase.skyscroll.engine.graphics.ProgramManager;
import aq.oceanbase.skyscroll.engine.graphics.RenderableObject;
import aq.oceanbase.skyscroll.engine.utils.loaders.TextureLoader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Background extends RenderableObject {
    private ShortBuffer mOrderBuffer;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordinateBuffer;

    private TextureRegion mTexRgn = new TextureRegion();

    private int mTextureId;
    private int mTextureHandler;

    private int mShaderProgram;

    public Background(final int resourceId) {

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

        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * (Float.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(vertexData).position(0);

        mTextureCoordinateBuffer = ByteBuffer.allocateDirect(textureCoordinateData.length * (Float.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureCoordinateBuffer.put(textureCoordinateData).position(0);

        mOrderBuffer = ByteBuffer.allocateDirect(orderData.length * (Short.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asShortBuffer();
        mOrderBuffer.put(orderData).position(0);
    }

    public Background(final int resourceId, float shiftFactor) {
        this(resourceId);
        mTexRgn.v1 = 1.0f - shiftFactor;
        updateTexRegion();
    }

    // offset is a part of image that should appear on the screen whilst factor - is fraction of amount
    // on which background should be moved. fraction is needed for background to move accordingly to
    // camera's height and reach end at the same time as camera reaches it's limit.
    // Inputs:
    //      amount - amount of camera movement
    //      factor - fraction to calculate amount of movement for background
    //      offset - fraction of texture that should be on the background at a time
    public void setShift(float amount, float factor, float offset) {
        mTexRgn.v1 = 1.0f - offset - factor * amount;         // Remember, that 0, 0 UV coordinate is UP left
        mTexRgn.v2 = 1.0f - factor * amount;                    // NOT DOWN left.

        updateTexRegion();
    }

    // factor is used as a matter of offset since there's no texture repeating and factor equals offset in original func
    public void setShift(float amount, float factor) {
        this.setShift(amount, factor, factor);
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

    @Override
    public void initialize(Context context, ProgramManager programManager) {
        mShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.BACKGROUND);

        mTextureHandler = TextureLoader.loadTexture(context, mTextureId);

        super.initialize(context, programManager);
    }

    @Override
    public void release() {
        //TODO: FILL
        super.release();
    }

    @Override
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
