package aq.oceanbase.skyscroll.engine.logic.window.blocks;

import android.content.Context;
import android.opengl.GLES20;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.logic.window.Window;
import aq.oceanbase.skyscroll.engine.logic.window.WindowBlock;
import aq.oceanbase.skyscroll.engine.graphics.ProgramManager;
import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TimerBarBlock extends WindowBlock {

    private FloatBuffer mVertexBuffer;
    private int mShaderProgram;

    private Vector3f mOrigin;
    private Vector3f mEnd;
    private Vector3f mCurrent;

    private boolean mHorizontal = false;
    private boolean mInversed = false;
    private boolean mCentered = false;

    private float mLength;

    private float mThickness = 0.0f;           // Thickness of the timer line

    private float mOffset = 0.0f;              // Offset from the edge perpendicular to direction of rising

    public TimerBarBlock(Window root, float fraction) {
        super(root, fraction);
    }

    public TimerBarBlock setThickness(float thickness) {
        this.mThickness = thickness;
        return this;
    }

    public TimerBarBlock setOffset(float offset) {
        this.mOffset = offset;
        return this;
    }

    public TimerBarBlock setHorizontal() {
        this.mHorizontal = true;
        return this;
    }

    public TimerBarBlock setVertical() {
        this.mHorizontal = false;
        return this;
    }

    public TimerBarBlock setInversed(boolean value) {
        this.mInversed = value;
        return this;
    }


    private void computeMetrics() {
        if (mHorizontal) computeMetricsHorizontal();
        else computeMetricsVertical();

        mCurrent = new Vector3f(mOrigin);
    }

    private void computeMetricsVertical() {
        if (mCentered) mOrigin = new Vector3f(mPos.x + mWidth/2, mPos.y - mHeight + mOffset, mPos.z);
        else mOrigin = new Vector3f(mPos.x + mThickness/2, mPos.y - mHeight + mOffset, mPos.z);

        mLength = mHeight - 2*mOffset;
        mEnd = new Vector3f(mOrigin.x, mOrigin.y + mLength, mOrigin.z);

        if (mInversed) {
            mOrigin.y = mPos.y - mOffset;
            mEnd.y = mOrigin.y - mLength;
        }
    }

    private void computeMetricsHorizontal() {
        if (mCentered) mOrigin = new Vector3f(mPos.x + mOffset, mPos.y - mHeight/2, mPos.z);
        else mOrigin = new Vector3f(mPos.x + mOffset, mPos.y - mThickness/2, mPos.z);
        mLength = mWidth - 2*mOffset;
        mEnd = new Vector3f(mOrigin.x + mLength, mOrigin.y, mOrigin.z);

        if (mInversed) {
            mOrigin.x = mPos.x + mWidth - mOffset;
            mEnd.x = mOrigin.x - mLength;
        }
    }

    public void rebuildVertexBuffer() {
        float[] vertexData = {
                mOrigin.x, mOrigin.y, mOrigin.z,
                mEnd.x, mEnd.y, mEnd.z,
                mOrigin.x, mOrigin.y, mOrigin.z,
                mCurrent.x, mCurrent.y, mCurrent.z
        };

        mVertexBuffer.clear();
        mVertexBuffer.put(vertexData, 0, 12);
        mVertexBuffer.flip();
    }

    @Override
    protected void onMetricsSet() {
        this.computeMetrics();
    }

    @Override
    public void update() {
        float delta = mRoot.getTimer().timePassedPercentage() * mLength;
        if (mInversed) delta *= (-1);

        if (mHorizontal) mCurrent.x = mOrigin.x + delta;
        else mCurrent.y = mOrigin.y + delta;

        if (!mRoot.isClosing()) this.rebuildVertexBuffer();
    }

    @Override
    public void initialize(Context context, ProgramManager programManager) {
        super.initialize(context, programManager);

        float[] vertexData = {
                mOrigin.x, mOrigin.y, mOrigin.z,
                mEnd.x, mEnd.y, mEnd.z,
                mOrigin.x, mOrigin.y, mOrigin.z,
                mCurrent.x, mCurrent.y, mCurrent.z
        };


        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * (Float.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(vertexData).position(0);

        mShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.LINE);
    }

    @Override
    public void release() {
        super.release();

        //GLES20.glDeleteProgram(mShaderProgram);
    }

    @Override
    public void draw(Camera cam) {
        GLES20.glUseProgram(mShaderProgram);

        int MVPMatrixHandler = GLES20.glGetUniformLocation(mShaderProgram, "u_MVPMatrix");
        int positionHandler = GLES20.glGetAttribLocation(mShaderProgram, "a_Position");
        int colorHandler = GLES20.glGetAttribLocation(mShaderProgram, "a_Color");

        GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandler);

        GLES20.glUniformMatrix4fv(MVPMatrixHandler, 1, false, mRoot.getMVPMatrix(), 0);

        GLES20.glLineWidth(5.0f);

        GLES20.glVertexAttrib4f(colorHandler, 0.0f, 0.0f, 0.0f, 0.1f);
        GLES20.glDisableVertexAttribArray(colorHandler);

        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        GLES20.glVertexAttrib4f(colorHandler, 1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glDisableVertexAttribArray(colorHandler);

        GLES20.glDrawArrays(GLES20.GL_LINES, 2, 2);
    }
}
