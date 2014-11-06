package aq.oceanbase.skyscroll.graphics.elements;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.graphics.render.Renderable;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Line3D implements Renderable {
    private boolean mInititialized = false;

    private int mShaderProgram;

    private ShortBuffer mOrderBuffer;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;

    private float[] mModelMatrix = new float[16];

    private Vector3f mStartPos;
    private Vector3f mEndPos;
    private Vector3f mCenter;
    private Vector3f mDirectionNorm;

    private float mLength;
    private float mWidth = 0.5f;

    private float[] mColor = new float[] {1.0f, 1.0f, 1.0f, 1.0f};


    public Line3D(Vector3f startPos, Vector3f endPos) {
        this.mStartPos = startPos;
        this.mEndPos = endPos;

        this.buildLine();
    }

    public Line3D(Vector3f startPos, Vector3f endPos, float width, float[] color) {
        this(startPos, endPos);
        this.mWidth = width;
        this.mColor = color;
    }

    public void setModelMatrix(float[] matrix) {
        this.mModelMatrix = matrix;
    }


    private void buildLine() {
        Vector3f diff = mEndPos.subtractV(mStartPos);
        mCenter = mStartPos.addV(diff.multiplySf(0.5f));
        mDirectionNorm = diff.normalize();
        mLength = diff.length();
    }

    public boolean isInitialized() {
        return this.mInititialized;
    }

    public void initialize(Context context, ProgramManager programManager) {
        float[] vertexData =
                new float[] {
                        -mWidth/2, mLength/2, 0.0f,
                        -mWidth/2, -mLength/2, 0.0f,
                        mWidth/2, -mLength/2, 0.0f,
                        mWidth/2, mLength/2, 0.0f,
                };

        short[] orderData =
                new short[] {
                        0, 1, 3,
                        3, 1, 2
                };

        Matrix.setIdentityM(mModelMatrix, 0);

        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * (Float.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(vertexData).position(0);

        mOrderBuffer = ByteBuffer.allocateDirect(orderData.length * (Short.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asShortBuffer();
        mOrderBuffer.put(orderData).position(0);

        mShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.LINE3D);
    }

    public void release() {

        this.mInititialized = false;
    }

    public void draw(Camera cam) {
        float[] orientationMatrix;
        float[] VPMatrix = new float[16];

        GLES20.glUseProgram(mShaderProgram);

        int VPMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_VPMatrix");
        int modelMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_ModelMatrix");
        int orientationMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_OrientationMatrix");

        int positionHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Position");
        int colorHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Color");

        Vector3f lookAxis = cam.getPos().subtractV(mCenter).normalize();
        Vector3f upAxis = new Vector3f(mDirectionNorm);
        Vector3f rightAxis = upAxis.crossV(lookAxis).normalize();
        lookAxis = rightAxis.crossV(upAxis);

        orientationMatrix = new float[] {
                rightAxis.x, rightAxis.y, rightAxis.z, 0.0f,
                upAxis.x,    upAxis.y,    upAxis.z,    0.0f,
                lookAxis.x,  lookAxis.y,  lookAxis.z,  0.0f,
                mCenter.x,   mCenter.y,   mCenter.z,   1.0f
        };

        Matrix.multiplyMM(VPMatrix, 0, cam.getProjM(), 0, cam.getViewM(), 0);

        GLES20.glUniformMatrix4fv(VPMatrixHandle, 1, false, VPMatrix, 0);
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(orientationMatrixHandle, 1, false, orientationMatrix, 0);

        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        GLES20.glVertexAttrib4f(colorHandle, 1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glDisableVertexAttribArray(colorHandle);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
