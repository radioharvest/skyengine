package aq.oceanbase.skyscroll.graphics.elements.window.blocks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.elements.window.Window;
import aq.oceanbase.skyscroll.graphics.elements.window.WindowBlock;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.utils.loaders.TextureLoader;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class NodeDisplayBlock extends WindowBlock {
    private int mShaderProgram;

    private int mNodeTextureHandle;
    private int mScoreTextureHandle;

    private ShortBuffer mOrderBuffer;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordinateBuffer;

    private float mOffset = 0.1f;

    private Vector3f mPicOrigin;            // Topleft corner
    private float mPicSide;
    private final int mScoreResourceId;

    public NodeDisplayBlock(Window root, float fraction, final int pointsResourceId) {
        super(root, fraction);

        this.mScoreResourceId = pointsResourceId;
    }

    public void setOffset(float offset) {
        this.mOffset = offset;
    }

    @Override
    protected void onMetricsSet() {
        if (mWidth >= mHeight) mPicSide = mHeight - 2*mOffset;
        else mPicSide = mWidth - 2*mOffset;

        mPicOrigin = new Vector3f(mPos.x + (mWidth/2) - mPicSide/2, mPos.y - (mHeight/2) + (mPicSide/2), 0);
    }

    @Override
    public void initialize(Context context, ProgramManager programManager) {
        super.initialize(context, programManager);

        float [] vertexData =
                new float[] {
                        mPicOrigin.x,  mPicOrigin.y, mPicOrigin.z,
                        mPicOrigin.x, mPicOrigin.y - mPicSide, mPicOrigin.z,
                        mPicOrigin.x + mPicSide, mPicOrigin.y - mPicSide, mPicOrigin.z,
                        mPicOrigin.x + mPicSide,  mPicOrigin.y, mPicOrigin.z
                };

        float[] textureCoordinateData =
                new float[] {
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f
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

        mShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.SPRITE);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        //options.inSampleSize = (int)(512 / mPixelMetrics[0] * (mPicSide / mWidth));
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        mNodeTextureHandle = TextureLoader.loadTexture(context, R.drawable.node_display, options, GLES20.GL_LINEAR);
        mScoreTextureHandle = TextureLoader.loadTexture(context, mScoreResourceId, options);
    }

    @Override
    public void release() {
        super.release();

        GLES20.glDeleteTextures(1, new int[]{mNodeTextureHandle, mScoreTextureHandle}, 0);
        GLES20.glDeleteProgram(mShaderProgram);
    }

    @Override
    public void draw(Camera cam) {
        float[] spriteMatrix = new float[16];
        float[] rotationMatrix = new float[16];

        int MVPMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_VPMatrix");
        int spriteMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_SpriteMatrix");
        int rotationMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_RotationMatrix");
        int textureUniformHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_Texture");

        int colorHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Color");
        int positionHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Position");
        int texCoordHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_TexCoordinate");

        GLES20.glUseProgram(mShaderProgram);

        Matrix.setIdentityM(spriteMatrix, 0);
        Matrix.setIdentityM(rotationMatrix, 0);

        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mRoot.getMVPMatrix(), 0);
        GLES20.glUniformMatrix4fv(spriteMatrixHandle, 1, false, spriteMatrix, 0);
        GLES20.glUniformMatrix4fv(rotationMatrixHandle, 1, false, rotationMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mNodeTextureHandle);
        GLES20.glUniform1i(textureUniformHandle, 0);

        GLES20.glVertexAttrib4f(colorHandle, 1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glDisableVertexAttribArray(colorHandle);

        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        mTextureCoordinateBuffer.position(0);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTextureCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mScoreTextureHandle);
        GLES20.glUniform1i(textureUniformHandle, 0);

        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        mTextureCoordinateBuffer.position(0);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTextureCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }
}
