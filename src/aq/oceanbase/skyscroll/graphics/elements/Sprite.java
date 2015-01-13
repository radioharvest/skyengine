package aq.oceanbase.skyscroll.graphics.elements;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.TextureRegion;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.graphics.render.Renderable;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * This is Sprite Class
 * Basic usage goes as this:
 * If it is just drawn without any additional params - it is a true spherical sprite, longest calculation time, sprite is always pointed to the camera
 * If origin axis is set - it is a true cylindrical sprite, less time for calculation, a bit more control
 * If orientation matrix is set - it is a fake preset sprite, oriented relating to the matrix. fastest and weakest
 */

public class Sprite implements Renderable {
    public static enum SPRITEMODE {
        FAKE_PRESET,
        TRUE_CYLINDRICAL,
        TRUE_SPHERICAL,
    }

    public final static int POSITION_DATA_SIZE = 3;         // X, Y, Z
    public final static int TEXTURE_DATA_SIZE = 2;          // U, V
    public final static int INDEX_DATA_SIZE = 1;            // Index
    public final static int COLOR_DATA_SIZE = 4;            // R, G, B, A

    public final static int VERTEX_3D = 6;                  // X, Y, Z, U, V, Index
    public final static int COLORED_VERTEX_3D = 10;         // X, Y, Z, U, V, Index, R, G, B, A

    public final static int VERTICES_PER_SPRITE = 4;        // Quad structure
    public final static int INDICES_PER_SPRITE = 6;         // Two triangles


    private boolean mInitialized = false;
    private boolean mFiltered = false;

    private int mShaderProgram;
    private int mTextureHandle;

    private ShortBuffer mOrderBuffer;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordinateBuffer;
    private FloatBuffer mColorBuffer;

    private float[] mModelMatrix = new float[16];
    private float[] mOrientationMatrix = new float[16];

    private int mVertexTypeSize;
    private SPRITEMODE mSpriteMode;

    private Vector3f mPos;
    private Vector3f mOriginAxis = Vector3f.getZero();

    private float mWidth;
    private float mHeight;

    private Vertex[] mVertices = new Vertex[4];

    private float[] mColor = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

    private TextureRegion mTexRgn = new TextureRegion();


    public Sprite(Vector3f pos, float width, float height) {
        this.mPos = pos;
        this.mWidth = width;
        this.mHeight = height;
        this.mSpriteMode = SPRITEMODE.TRUE_SPHERICAL;
        this.mVertexTypeSize = VERTEX_3D;

        Matrix.setIdentityM(mModelMatrix, 0);

        buildVertices();
    }

    private void buildVertices() {
        float halfWidth = mWidth/2;
        float halfHeight = mHeight/2;

        mVertices[0] = new Vertex(-halfWidth,  halfHeight, 0.0f, mTexRgn.u1, mTexRgn.v1);          // top left
        mVertices[1] = new Vertex(-halfWidth, -halfHeight, 0.0f, mTexRgn.u1, mTexRgn.v2);          // bottom left
        mVertices[2] = new Vertex( halfWidth, -halfHeight, 0.0f, mTexRgn.u2, mTexRgn.v2);          // bottom right
        mVertices[3] = new Vertex( halfWidth,  halfHeight, 0.0f, mTexRgn.u2, mTexRgn.v1);          // top right
    }


    public Sprite setFiltered(boolean value) {
        this.mFiltered = value;

        return this;
    }

    public Sprite setTexture(int textureHandle) {
        this.mTextureHandle = textureHandle;

        return this;
    }

    public Sprite setOrientationMatrix(float[] matrix) {
        if (matrix.length != 16)
            return this;

        mOrientationMatrix = matrix;
        mSpriteMode = SPRITEMODE.FAKE_PRESET;

        return this;
    }

    public Sprite setModelMatrix(float[] matrix) {
        if (matrix.length != 16)
            return this;

        mModelMatrix = matrix;

        return this;
    }

    public Sprite setOriginAxis(Vector3f axis) {
        this.mOriginAxis = new Vector3f(axis);
        this.mSpriteMode = SPRITEMODE.TRUE_CYLINDRICAL;

        return this;
    }

    public Sprite setColor(float[] color) {
        if (color.length == 4) this.mColor = color;

        if (color.length == 3)
            this.mColor = new float[] {color[0], color[1], color[2], 1.0f};

        if (color.length > 4)
            this.mColor = new float[] {color[0], color[1], color[2], color[3]};

        return this;
    }


    public Vector3f getRightVector() {
        if (mOrientationMatrix.length < 16)
            return Vector3f.getZero();

        return new Vector3f(mOrientationMatrix[0], mOrientationMatrix[1], mOrientationMatrix[2]);
    }

    public Vector3f getUpVector() {
        if (mOrientationMatrix.length < 16)
            return Vector3f.getZero();

        return new Vector3f(mOrientationMatrix[4], mOrientationMatrix[5], mOrientationMatrix[6]);
    }

    public Vector3f getLookVector() {
        if (mOrientationMatrix.length < 16)
            return Vector3f.getZero();

        return new Vector3f(mOrientationMatrix[8], mOrientationMatrix[9], mOrientationMatrix[10]);
    }

    public TextureRegion getTexRgn() {
        return this.mTexRgn;
    }


    public boolean isInitialized() {
        return mInitialized;
    }

    public void initialize(Context context, ProgramManager programManager) {
        short[] orderData = {
                0, 1, 3,
                3, 1, 2
        };

        float[] vertexData = {
                mVertices[0].getX(), mVertices[0].getY(), mVertices[0].getZ(),
                mVertices[1].getX(), mVertices[1].getY(), mVertices[1].getZ(),
                mVertices[2].getX(), mVertices[2].getY(), mVertices[2].getZ(),
                mVertices[3].getX(), mVertices[3].getY(), mVertices[3].getZ()
        };

        float[] textureData = {
                mTexRgn.u1, mTexRgn.v1,
                mTexRgn.u1, mTexRgn.v2,
                mTexRgn.u2, mTexRgn.v2,
                mTexRgn.u2, mTexRgn.v1
        };

        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * (Float.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(vertexData).position(0);

        mOrderBuffer = ByteBuffer.allocateDirect(orderData.length * (Short.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asShortBuffer();
        mOrderBuffer.put(orderData).position(0);

        mTextureCoordinateBuffer = ByteBuffer.allocateDirect(textureData.length * (Float.SIZE / 8) ).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureCoordinateBuffer.put(textureData).position(0);

        mShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.SPRITE);

        mInitialized = true;
    }

    public void release() {
        mInitialized = true;
    }

    public void draw(Camera cam) {
        float[] VPMatrix = new float[16];

        GLES20.glUseProgram(mShaderProgram);

        int VPMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_VPMatrix");
        int modelMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_ModelMatrix");
        int textureUniformHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_Texture");
        int orientationMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_OrientationMatrix");

        int positionHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Position");
        int texCoordHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_TexCoordinate");
        int colorHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Color");


        Matrix.setIdentityM(VPMatrix, 0);
        Matrix.multiplyMM(VPMatrix, 0, cam.getProjM(), 0, cam.getViewM(), 0);

        //Matrix.translateM(mModelMatrix, 0, mPos.x, mPos.y, mPos.z);

        if ( mSpriteMode != SPRITEMODE.FAKE_PRESET ) {
            Vector3f lookAxis = cam.getPos().subtractV(mPos).normalize();
            Vector3f upAxis;
            Vector3f rightAxis;

            if ( mSpriteMode == SPRITEMODE.TRUE_SPHERICAL ) {
                upAxis = new Vector3f(cam.getUp());
                rightAxis = upAxis.crossV(lookAxis).normalize();
                //upAxis = lookAxis.crossV(rightAxis).normalize();
            } else {
                upAxis = new Vector3f(mOriginAxis);
                rightAxis = upAxis.crossV(lookAxis).normalize();
            }

            lookAxis = rightAxis.crossV(upAxis);

            lookAxis.print("Debug Skyscroll", "lookAxis");

            mOrientationMatrix = new float[] {
                    rightAxis.x, rightAxis.y, rightAxis.z, 0.0f,
                    upAxis.x,    upAxis.y,    upAxis.z,    0.0f,
                    lookAxis.x,  lookAxis.y,  lookAxis.z,  0.0f,
                    mPos.x,      mPos.y,      mPos.z,      1.0f
            };
        }

        GLES20.glUniformMatrix4fv(VPMatrixHandle, 1, false, VPMatrix, 0);
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(orientationMatrixHandle, 1, false, mOrientationMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHandle);
        GLES20.glUniform1i(textureUniformHandle, 0);

        if (mFiltered) {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        }

        mVertexBuffer.position(0);        //set the position of position data in vertex buffer
        GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        mTextureCoordinateBuffer.position(0);     // set position of texcoords in vertex buffer
        GLES20.glVertexAttribPointer(texCoordHandle, TEXTURE_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mTextureCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        if (mVertexTypeSize == COLORED_VERTEX_3D) {
            mColorBuffer.position(0);
            GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mColorBuffer);
            GLES20.glEnableVertexAttribArray(colorHandle);
        } else {            // if vertex is not colored - use default color
            GLES20.glVertexAttrib4f(colorHandle, mColor[0], mColor[1], mColor[2], mColor[3]);
            GLES20.glDisableVertexAttribArray(colorHandle);
        }

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);

        if (mSpriteMode == SPRITEMODE.FAKE_PRESET) {
            mSpriteMode = SPRITEMODE.TRUE_SPHERICAL;
        }
    }
}
