package aq.oceanbase.skyscroll.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import aq.oceanbase.skyscroll.commons.GraphicsCommons;
import aq.oceanbase.skyscroll.generators.TreeGenerator;
import aq.oceanbase.skyscroll.loaders.ShaderLoader;
import aq.oceanbase.skyscroll.commons.MathMisc;
import aq.oceanbase.skyscroll.math.Vector2f;
import aq.oceanbase.skyscroll.math.Vector3f;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class MainRenderer  implements GLSurfaceView.Renderer {

    //Constants and sizes
    final int mBytesPerFloat = 4;
    final int mPositionDataSize = 3;

    //Rendering settings
    final float mNearPlane = 1.0f;
    final float mFarPlane = 30.0f;

    //Matrices
    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    //Handlers
    private int mNodeShaderProgram;
    private int mLineShaderProgram;
    private int mMVPMatrixHandler;
    private int mNodesPositionHandler;
    private int mLinesPositionHandler;

    //FloatBuffers
    private FloatBuffer mNodesPositions;
    private FloatBuffer mLinesPositions;

    //Navigation variables
    private float mDistance = 15.0f;         //cam distance from origin
    private float mHeight = 0.0f;
    private float mAngle = 0.0f;

    //Touch variables
    private Vector2f mMomentum = new Vector2f(0.0f, 0.0f);

    //Constraints
    private float mMinHeight = 0.0f;
    private float mMaxHeight = 30.0f;
    private float mMinDist = 8.0f;
    private float mMaxDist = 20.0f;

    //Camera parameters
    private Vector3f camPos = new Vector3f(0.0f, 0.0f, -mDistance);
    private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
    private Vector3f look = new Vector3f(0.0f, 0.0f, -1.0f);

    public MainRenderer() {
        Log.e("RunDebug", "Renderer constructor stage passed");
        TreeGenerator generator = new TreeGenerator();
        float[] nodesPositionData = generator.getNodesPositionData();
        mNodesPositions = ByteBuffer.allocateDirect(nodesPositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mNodesPositions.put(nodesPositionData).position(0);

        float[] linesPositionData = generator.getLinesPositionData();
        mLinesPositions = ByteBuffer.allocateDirect(linesPositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mLinesPositions.put(linesPositionData).position(0);
    }


    public float getAngle() {
        return this.mAngle;
    }

    public void setAngle(float angle) {
        this.mAngle = angle;
    }

    public float getHeight() {
        return this.mHeight;
    }

    public void setHeight(float height) {
        if (height < mMinHeight) this.mHeight = mMinHeight;
        else if (height > mMaxHeight) this.mHeight = mMaxHeight;
        else this.mHeight = height;
    }

    public void setMomentum (Vector2f momentum) {
        //TODO: add operator override
        this.mMomentum.x = momentum.x;
        this.mMomentum.y = momentum.y;
    }

    public void zoom(float distance) {
        if (Math.abs(distance) > 0.1) mDistance = mDistance - distance;
        if (mDistance <= mMinDist) mDistance = mMinDist;
        else if (mDistance > mMaxDist) mDistance = mMaxDist;
    }


    private void updateMomentum() {
        Log.e("NavDebug", new StringBuilder().append("mMomentum.x: ").append(mMomentum.x).toString());
        Log.e("NavDebug", new StringBuilder().append("mMomentum.y: ").append(mMomentum.y).toString());

        if (mMomentum.x != 0.0f) {
            mMomentum.x = MathMisc.decrementConvergingValue(mMomentum.x, 1.7f);
        }

        if (mMomentum.y != 0.0f) {
            mMomentum.y = MathMisc.decrementConvergingValue(mMomentum.y, 0.1f);
        }
    }

    private void updateAngle() {
        //Log.e("NavDebug", new StringBuilder().append("mAngle: ").append(mAngle).toString());

        mAngle = mAngle + mMomentum.x;
        if (mAngle >= 360.0f) mAngle = mAngle - 360.0f;
        if (mAngle <= -360.0f) mAngle = mAngle + 360.0f;
    }

    private void updateHeight() {
        //Log.e("NavDebug", new StringBuilder().append("mHeight: ").append(mHeight).toString());

        mHeight = mHeight + mMomentum.y;
        if (mHeight > mMaxHeight) mHeight = mMaxHeight;
        if (mHeight < mMinHeight) mHeight = mMinHeight;
    }

    private void updateCameraPosition() {
        //Log.e("NavDebug", new StringBuilder().append("camPos.z: ").append(camPos.z).toString());
        camPos.z = mDistance;
        camPos.y = mHeight;
        look.y = mHeight;
    }



    private void drawNodes() {
        GLES20.glUseProgram(mNodeShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mNodeShaderProgram, "u_MVPMatrix");
        mNodesPositionHandler = GLES20.glGetAttribLocation(mNodeShaderProgram, "a_Position");

        mNodesPositions.position(0);
        GLES20.glVertexAttribPointer(mNodesPositionHandler, mPositionDataSize, GLES20.GL_FLOAT, false, 0, mNodesPositions);
        GLES20.glEnableVertexAttribArray(mNodesPositionHandler);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 32);
    }

    private void drawLines() {
        GLES20.glUseProgram(mLineShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mLineShaderProgram, "u_MVPMatrix");
        mLinesPositionHandler = GLES20.glGetAttribLocation(mLineShaderProgram, "a_Position");

        mLinesPositions.position(0);
        GLES20.glVertexAttribPointer(mLinesPositionHandler, mPositionDataSize, GLES20.GL_FLOAT, false, 0, mLinesPositions);
        GLES20.glEnableVertexAttribArray(mLinesPositionHandler);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 56);
    }

    private void drawTree() {
        drawLines();
        drawNodes();
    }



    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.05f, 0.0f, 0.1f, 0.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        //TODO: set up starting view angle
        Matrix.setLookAtM(mViewMatrix, 0,
                camPos.x, camPos.y, camPos.z,
                look.x, look.y, look.z,
                up.x, up.y, up.z);

        final String nodeVertexShaderSource = ShaderLoader.getShader("/shaders/nodes/nodeVertexShader.glsl");
        final String nodeFragmentShaderSource = ShaderLoader.getShader("/shaders/nodes/nodeFragmentShader.glsl");

        final int nodeVertexShader = GraphicsCommons.compileShader(GLES20.GL_VERTEX_SHADER, nodeVertexShaderSource);
        final int nodeFragmentShader = GraphicsCommons.compileShader(GLES20.GL_FRAGMENT_SHADER, nodeFragmentShaderSource);

        mNodeShaderProgram = GraphicsCommons.
                createAndLinkProgram(nodeVertexShader, nodeFragmentShader,
                        new String[] {"a_Position"});

        final String lineVertexShaderSource = ShaderLoader.getShader("/shaders/lines/lineVertexShader.glsl");
        final String lineFragmentShaderSource = ShaderLoader.getShader("/shaders/lines/lineFragmentShader.glsl");

        final int lineVertexShader = GraphicsCommons.compileShader(GLES20.GL_VERTEX_SHADER, lineVertexShaderSource);
        final int lineFragmentShader = GraphicsCommons.compileShader(GLES20.GL_FRAGMENT_SHADER, lineFragmentShaderSource);

        mLineShaderProgram = GraphicsCommons.
                createAndLinkProgram(lineVertexShader, lineFragmentShader,
                        new String[]{"a_Position"});
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        //TODO: move
        final float ratio = (float) width/height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, mNearPlane, mFarPlane);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        /*GLES20.glUseProgram(mNodeShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mNodeShaderProgram, "u_MVPMatrix");
        mNodesPositionHandler = GLES20.glGetAttribLocation(mNodeShaderProgram, "a_Position");*/

        updateMomentum();
        updateAngle();

        Matrix.setIdentityM(mModelMatrix, 0);
        //Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -mDistance);
        Matrix.rotateM(mModelMatrix, 0, mAngle, 0.0f, 1.0f, 0.0f);

        drawTree();

        updateHeight();
        updateCameraPosition();

        Matrix.setLookAtM(mViewMatrix, 0,
                camPos.x, camPos.y, camPos.z,
                look.x, look.y, look.z,
                up.x, up.y, up.z);
    }
}
