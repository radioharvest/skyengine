package aq.oceanbase.skyscroll.engine.graphics.batches;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.graphics.ProgramManager;
import aq.oceanbase.skyscroll.engine.graphics.RenderableObject;
import aq.oceanbase.skyscroll.engine.graphics.primitives.Line3D;
import aq.oceanbase.skyscroll.engine.graphics.primitives.Vertex;
import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Line3DBatch extends RenderableObject {
    public final static int INDEX_DATA_SIZE = 1;            // Index

    public final static int POSITION_DATA_SIZE = 3;         // X, Y, Z
    public final static int COLOR_DATA_SIZE = 4;            // R, G, B, Alpha
    public final static int TEXTURE_DATA_SIZE = 2;          // U, V

    public final static int VERTEX_SIZE = 8;                //X, Y, Z, Index, R, G, B, A
    public final static int TEXTURED_VERTEX_SIZE = 10;      //X, Y, Z, Index, R, G, B, A, U, V

    public final static int VERTICES_PER_LINE = 4;
    public final static int INDICES_PER_LINE = 6;

    public final static int VERTICES_PER_LINE_SMOOTH = 8;
    public final static int INDICES_PER_LINE_SMOOTH = 18;

    public final static int MAX_BATCHSIZE = 16;


    private int mShaderProgram;
    protected int mTextureHandle = 0;

    // line type info
    private boolean mSmooth = false;

    // matrices
    private float[] mModelMatrix;
    private float[] mOrientationMatrices;

    // arrays
    private short[] mIndices;
    private float[] mVertices;

    // buffers
    private ShortBuffer mOrderBuffer;
    private FloatBuffer mVertexBuffer;

    // constants
    private int mVertexStride;
    private int mIndicesPerLine = INDICES_PER_LINE;
    private int mVerticesPerLine = VERTICES_PER_LINE;
    private int mVertexSize = VERTEX_SIZE;
    private int mMaxBatchSize = MAX_BATCHSIZE;

    // runtime batch info
    private int mBatchSize;
    private int mBufferCounter;

    private boolean mBatchStarted = false;

    private Camera mCam;


    public Line3DBatch(int textureHandle, boolean smoothed, int batchSize) {
        if (textureHandle != 0) {
            mTextureHandle = textureHandle;
            mVertexSize = TEXTURED_VERTEX_SIZE;
        }

        mSmooth = smoothed;

        if (batchSize < MAX_BATCHSIZE && batchSize > 0)
            mMaxBatchSize = batchSize;

        if (mSmooth) {
            mIndicesPerLine = INDICES_PER_LINE_SMOOTH;
            mVerticesPerLine = VERTICES_PER_LINE_SMOOTH;
        }

        mBufferCounter = 0;
        mVertexStride = mVertexSize * (Float.SIZE / 8);

        this.mIndices = new short[mMaxBatchSize * mIndicesPerLine];
        this.mVertices = new float[mMaxBatchSize * mVerticesPerLine * mVertexSize];
        this.mOrientationMatrices = new float[mMaxBatchSize * 16];

        generateOrderData();

        mOrderBuffer = ByteBuffer.allocateDirect(mIndices.length * (Short.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asShortBuffer();
        mOrderBuffer.put(mIndices).position(0);

        mVertexBuffer = ByteBuffer.allocateDirect(mVertices.length * (Float.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public Line3DBatch(int textureHandle, boolean smoothed) {
        this(textureHandle, smoothed, MAX_BATCHSIZE);
    }

    public Line3DBatch(int textureHandle) {
        this(textureHandle, false, 0);
    }

    public Line3DBatch(boolean smoothed, int batchSize) {
        this(0, smoothed, batchSize);
    }

    public Line3DBatch(boolean smoothed) {
        this(0, smoothed, MAX_BATCHSIZE);
    }

    public Line3DBatch() {
        this(0, false, MAX_BATCHSIZE);
    }

    public int getBatchSize() {
        return this.mBatchSize;
    }

    private void generateOrderData() {
        int len = mIndices.length;
        short j = 0;
        if (mSmooth) {
            for (int i = 0; i < len; i += mIndicesPerLine, j += mVerticesPerLine) {
                mIndices[i + 0] = (short)(j + 0);
                mIndices[i + 1] = (short)(j + 1);
                mIndices[i + 2] = (short)(j + 3);
                mIndices[i + 3] = (short)(j + 3);
                mIndices[i + 4] = (short)(j + 1);
                mIndices[i + 5] = (short)(j + 2);
            }
        } else {
            for (int i = 0; i < len; i += mIndicesPerLine, j += mVerticesPerLine) {
                mIndices[i + 0] = (short)(j + 0);
                mIndices[i + 1] = (short)(j + 1);
                mIndices[i + 2] = (short)(j + 3);
                mIndices[i + 3] = (short)(j + 3);
                mIndices[i + 4] = (short)(j + 1);
                mIndices[i + 5] = (short)(j + 2);
            }
        }
    }


    public void beginBatch(Camera cam, float[] modelMatrix) {
        mBatchSize = 0;
        mBufferCounter = 0;

        mCam = cam;
        mModelMatrix = modelMatrix;

        mBatchStarted = true;
    }

    public void beginBatch(Camera cam) {
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);

        beginBatch(cam, matrix);
    }

    public void batchElement(Line3D line) {
        if (!mBatchStarted) return;

        if (mBatchSize == mMaxBatchSize) {
            endBatch();

            mBatchSize = 0;
            mBufferCounter = 0;

            mBatchStarted = true;
        }

        float[] orientationMatrix;


        Vector3f center = line.getRay().getCenterPos();

        Vector3f lookAxis = mCam.getPos().subtractV(center).normalize();
        Vector3f upAxis = new Vector3f(line.getRay().getDirectionNorm());
        Vector3f rightAxis = upAxis.crossV(lookAxis).normalize();
        lookAxis = rightAxis.crossV(upAxis);

        orientationMatrix = new float[] {
                rightAxis.x, rightAxis.y, rightAxis.z, 0.0f,
                upAxis.x,    upAxis.y,    upAxis.z,    0.0f,
                lookAxis.x,  lookAxis.y,  lookAxis.z,  0.0f,
                center.x,    center.y,    center.z,    1.0f
        };


        Vertex[] vertexArray = line.getVertexArray();

        for(int i = 0; i < vertexArray.length; i++) {
            mVertices[mBufferCounter++] = mBatchSize;
            mVertices[mBufferCounter++] = vertexArray[i].getX();
            mVertices[mBufferCounter++] = vertexArray[i].getY();
            mVertices[mBufferCounter++] = vertexArray[i].getZ();
            mVertices[mBufferCounter++] = vertexArray[i].getColorRed();
            mVertices[mBufferCounter++] = vertexArray[i].getColorGreen();
            mVertices[mBufferCounter++] = vertexArray[i].getColorBlue();
            mVertices[mBufferCounter++] = vertexArray[i].getColorAlpha();

            if (mTextureHandle != 0) {              // textured mode
                mVertices[mBufferCounter++] = vertexArray[i].getTexU();
                mVertices[mBufferCounter++] = vertexArray[i].getTexV();
            }

            for (int k = 0; k < 16; k++) {
                //TODO: possible optimization
                mOrientationMatrices[mBatchSize*16 + k] = orientationMatrix[k];
            }

            mBatchSize++;
        }

        line.checkOcclusion();
    }

    public void endBatch() {
        if ( mBatchSize > 0 ) {
            mVertexBuffer.clear();
            mVertexBuffer.put(mVertices, 0, mBufferCounter);
            mVertexBuffer.flip();

            draw(mCam);
        }

        mBatchStarted = false;
    }

    @Override
    public void initialize(Context context, ProgramManager programManager) {
        if (mTextureHandle != 0)
            mShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.LINE3D_BATCH_TEXTURED);
        else
            mShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.LINE3D_BATCH);

        super.initialize(context, programManager);
    }

    @Override
    public void release() {
        GLES20.glDeleteProgram(mShaderProgram);

        super.release();
    }

    @Override
    public void draw(Camera cam) {
        float[] VPMatrix = new float[16];

        GLES20.glUseProgram(mShaderProgram);

        int VPMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_VPMatrix");
        int modelMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_ModelMatrix");
        int orientationMatricesHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_OrientationMatrix");

        int positionHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Position");
        int indexHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_OrientationMatrixIndex");
        int colorHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Color");

        Matrix.setIdentityM(VPMatrix, 0);
        Matrix.multiplyMM(VPMatrix, 0, cam.getProjM(), 0, cam.getViewM(), 0);

        GLES20.glUniformMatrix4fv(VPMatrixHandle, 1, false, VPMatrix, 0);

        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, mModelMatrix, 0);

        GLES20.glUniformMatrix4fv(orientationMatricesHandle, mBatchSize, false, mOrientationMatrices, 0);
        GLES20.glEnableVertexAttribArray(orientationMatricesHandle);

        mVertexBuffer.position(0);     // set position of index in vertex buffer
        GLES20.glVertexAttribPointer(indexHandle, INDEX_DATA_SIZE, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(indexHandle);

        mVertexBuffer.position(INDEX_DATA_SIZE);        //set the position of position data in vertex buffer
        GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        mVertexBuffer.position(INDEX_DATA_SIZE + POSITION_DATA_SIZE);     // set position of texcoords in vertex buffer
        GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(colorHandle);

        if (mTextureHandle != 0) {
            int textureUniformHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_Texture");
            int texCoordHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_TexCoordinate");

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHandle);
            GLES20.glUniform1i(textureUniformHandle, 0);

            mVertexBuffer.position(INDEX_DATA_SIZE + POSITION_DATA_SIZE + COLOR_DATA_SIZE);     // set position of texcoords in vertex buffer
            GLES20.glVertexAttribPointer(texCoordHandle, TEXTURE_DATA_SIZE, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(texCoordHandle);
        }

        mOrderBuffer.position(0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mBatchSize * mIndicesPerLine, GLES20.GL_UNSIGNED_SHORT, mOrderBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(indexHandle);
    }
}
