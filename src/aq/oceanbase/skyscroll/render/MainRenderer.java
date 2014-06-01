package aq.oceanbase.skyscroll.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.legacy.DemoRenderer;
import aq.oceanbase.skyscroll.legacy.GLSurfaceRendererTwo;
import aq.oceanbase.skyscroll.loaders.ShaderLoader;
import aq.oceanbase.skyscroll.math.MathMisc;
import aq.oceanbase.skyscroll.math.Vector2f;
import aq.oceanbase.skyscroll.math.Vector3f;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer  implements GLSurfaceView.Renderer{

    //Rendering settings
    final float mNearPlane = 1.0f;
    final float mFarPlane = 30.0f;

    //Matrices
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mModelMatrix = new float[16];

    //Handlers
    private int mPointsShaderProgram;
    private int mLinesShaderProgram;
    private int mMVPMatrixHandler;
    private int mPositionHandler;

    //Navigation variables
    private float mDistance = 5.0f;         //cam distance from origin
    private float mHeight = 0.0f;
    private float mAngle = 0.0f;

    //Touch variables
    private Vector2f mMomentum = new Vector2f(0.0f, 0.0f);

    //Constraints
    private float mMinHeight = 0.0f;
    private float mMaxHeight = 30.0f;

    //Camera parameters
    private Vector3f camPos = new Vector3f(0.0f, 0.0f, -mDistance);
    private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
    private Vector3f look = new Vector3f(0.0f, 0.0f, -1.0f);

    public MainRenderer() {

    }


    private void updateMomentum() {
        if (mMomentum.x != 0.0f) {
            mMomentum.x = MathMisc.decrementConvergingValue(mMomentum.x, 1.7f);
        }

        if (mMomentum.y != 0.0f) {
            mMomentum.y = MathMisc.decrementConvergingValue(mMomentum.y, 0.1f);
        }
    }

    private void updateAngle() {
        mAngle = mAngle + mMomentum.x;
        if (mAngle >= 360.0f) mAngle = mAngle - 360.0f;
        if (mAngle <= -360.0f) mAngle = mAngle + 360.0f;
    }

    private void updateHeight() {
        mHeight = mHeight + mMomentum.y;
        if (mHeight > mMaxHeight) mHeight = mMaxHeight;
        if (mHeight < mMinHeight) mHeight = mMinHeight;
    }

    private void updateCameraPosition() {
        updateMomentum();
        updateAngle();
        updateHeight();

        double angle = Math.toRadians(mAngle);
        camPos.x = (float)Math.sin(-angle)*mDistance;
        camPos.z = (float)(Math.cos(angle)*mDistance - mDistance);

        look.x = -camPos.x;
        look.z = -camPos.z;
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

        final String pointsVertexShaderSource = ShaderLoader.getShader("/shaders/points/pointsVertexShader.glsl");
        final String pointsFragmentShaderSource = ShaderLoader.getShader("/shaders/points/pointFragmentShader.glsl");

        final int pointsVertexShader = GraphicsCommons.compileShader(GLES20.GL_VERTEX_SHADER, pointsVertexShaderSource);
        final int pointsFragmentShader = GraphicsCommons.compileShader(GLES20.GL_FRAGMENT_SHADER, pointsFragmentShaderSource);

        mPointsShaderProgram = GraphicsCommons.
                createAndLinkProgram(pointsVertexShader, pointsFragmentShader,
                        new String[] {"a_Position"});

        final String linesVertexShaderSource = ShaderLoader.getShader("/shaders/lines/linesVertexShader.glsl");
        final String linesFragmentShaderSource = ShaderLoader.getShader("/shaders/lines/linesFragmentShader.glsl");

        final int linesVertexShader = GraphicsCommons.compileShader(GLES20.GL_VERTEX_SHADER, linesVertexShaderSource);
        final int linesFragmentShader = GraphicsCommons.compileShader(GLES20.GL_FRAGMENT_SHADER, linesFragmentShaderSource);

        mLinesShaderProgram = GraphicsCommons.
                createAndLinkProgram(linesVertexShader, linesFragmentShader,
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

        GLES20.glUseProgram(mPointsShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mPointsShaderProgram, "u_MVPMatrix");
        mPositionHandler = GLES20.glGetAttribLocation(mPointsShaderProgram, "a_Position");

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -mDistance);

        //DRAW OPERATIONS HERE

        updateCameraPosition();

        Matrix.setLookAtM(mViewMatrix, 0,
                camPos.x, camPos.y, camPos.z,
                look.x, look.y + mHeight, look.z,
                up.x, up.y, up.z);
    }
}
