package aq.oceanbase.skyscroll.Renderers;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import aq.oceanbase.skyscroll.math.Vector3f;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.lang.Math;

public class RendererTwo implements GLSurfaceView.Renderer {

    private static final String TAG = "Lesson Two Renderer";

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private float[] mMVPMatrix = new float[16];

    private float[] mLightModelMatrix = new float[16];

    private float mAngle = 0.0f;
    private float mDistance = 5.0f;
    private float mHeight = 0.0f;

    private final FloatBuffer mCubePositions;
    private final FloatBuffer mCubeColors;
    private final FloatBuffer mCubeNormals;

    private int mMVPMatrixHandler;
    private int mMVMatrixHandler;

    private int mLightPosHandler;
    private int mPositionHandler;
    private int mColorHandler;
    private int mNormalHandler;

    private int mPerVertexProgramHandler;
    private int mLightPointProgramHandler;

    private final int mBytesPerFloat = 4;
    private final int mPositionDataSize = 3;
    private final int mColorDataSize = 4;
    private final int mNormalDataSize = 3;

    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
    private final float[] mLightPosInWorldSpace = new float[4];
    private final float[] mLightPosInEyeSpace = new float[4];

    //private float[] camPos = new float[] {0.0f, 0.0f, -0.5f};
    //private float[] look = new float[] {0.0f, 0.0f, -distance};

    private Vector3f camPos = new Vector3f(0.0f, 4.0f, -0.5f);
    private Vector3f look = new Vector3f(0.0f, 0.0f, -mDistance);

    public RendererTwo() {

        // X, Y, Z
        final float[] cubePositionData = {
                //Remember counter-clockwise orientation of vertices in OpenGL

                // Front face
                -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,

                // Right face
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,

                // Back face
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,

                // Left face
                -1.0f, 1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,

                // Top face
                -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,

                // Bottom face
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
        };

        // R, G, B, Alpha
        final float[] cubeColorData = {
                // Front face (red)
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                // Right face (green)
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,

                // Back face (blue)
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                // Left face (yellow)
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,

                // Top face (cyan)
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,

                // Bottom face (magenta)
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f
        };

        // X, Y, Z
        final float[] cubeNormalData = {
                // Front face
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,

                // Right face
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,

                // Back face
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,

                // Left face
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,

                // Top face
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,

                // Bottom face
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f
        };

        mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePositionData).position(0);

        mCubeColors = ByteBuffer.allocateDirect(cubeColorData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColors.put(cubeColorData).position(0);

        mCubeNormals = ByteBuffer.allocateDirect(cubeNormalData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals.put(cubeNormalData).position(0);
    }


    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        this.mAngle = angle;
    }

    public float getDistance() {
        return mDistance;
    }

    public void setDistance(float mDistance) {
        this.mDistance = mDistance;
    }

    public float getHeight() {
        return mHeight;
    }

    public void setHeight(float mHeight) {
        this.mHeight = mHeight;
    }


    protected String getVertexShader() {
        final String vertexShader =
            "uniform mat4 u_MVPMatrix;\n" +
            "uniform mat4 u_MVMatrix;\n" +
            "uniform vec3 u_LightPos;\n" +
            "attribute vec4 a_Position;\n" +
            "attribute vec4 a_Color;\n" +
            "attribute vec3 a_Normal;\n" +
            "varying vec4 v_Color;\n" +
            "void main() {\n" +
            "    vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);\n" +
            "    vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n" +
            "    float distance = length(u_LightPos - modelViewVertex);\n" +
            "    vec3 lightVector = normalize(u_LightPos - modelViewVertex);\n" +
            "    float diffuse = max(dot(modelViewNormal, lightVector), 0.1);\n" +
            "    diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));\n" +
            "    v_Color = a_Color * diffuse;\n" +
            "    gl_Position = u_MVPMatrix * a_Position;\n" +
            "}";

        return  vertexShader;
    }

    protected String getFragmentShader() {
        final String fragmentShader =
            "precision mediump float;\n" +
            "varying vec4 v_Color;\n" +
            "void main() {\n" +
            "    gl_FragColor = v_Color;\n" +
            "}";
        return fragmentShader;
    }

    private int compileShader (final int shaderType, final String shaderSource) {
        int shaderHandler = GLES20.glCreateShader(shaderType);

        if (shaderHandler != 0) {
            GLES20.glShaderSource(shaderHandler, shaderSource);
            GLES20.glCompileShader(shaderHandler);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandler, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            if (compileStatus[0] == 0) {
                Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandler));
                GLES20.glDeleteShader(shaderHandler);
                shaderHandler = 0;
            }

        }

        if (shaderHandler == 0) {
            throw new RuntimeException("Error creating shader");
        }

        return shaderHandler;
    }

    private int createAndLinkProgram(final int vertexShaderHandler, final int fragmentShaderHandler, final String[] attributes) {

        int programHandler = GLES20.glCreateProgram();

        if (programHandler != 0) {
            GLES20.glAttachShader(programHandler, vertexShaderHandler);
            GLES20.glAttachShader(programHandler, fragmentShaderHandler);

            if (attributes != null) {
                final int size = attributes.length;
                for (int i = 0; i < size; i++) {
                    GLES20.glBindAttribLocation(programHandler, i, attributes[i]);
                }
            }

            GLES20.glLinkProgram(programHandler);

            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandler, GLES20.GL_LINK_STATUS, linkStatus, 0);

            if (linkStatus[0] == 0) {
                Log.e(TAG, "Error compiling program: " + GLES20.glGetShaderInfoLog(programHandler));
                GLES20.glDeleteProgram(programHandler);
                programHandler = 0;
            }
        }

        if (programHandler == 0) {
            throw new RuntimeException("Error creating program");
        }

        return programHandler;
    }

    private void drawCube() {
        // Pass in the position information
        mCubePositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandler, mPositionDataSize, GLES20.GL_FLOAT, false, 0, mCubePositions);
        GLES20.glEnableVertexAttribArray(mPositionHandler);

        // Pass in the color information
        mCubeColors.position(0);
        GLES20.glVertexAttribPointer(mColorHandler, mColorDataSize, GLES20.GL_FLOAT, false, 0, mCubeColors);
        GLES20.glEnableVertexAttribArray(mColorHandler);

        // Pass in the normal information
        mCubeNormals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandler, mNormalDataSize, GLES20.GL_FLOAT, false, 0, mCubeNormals);
        GLES20.glEnableVertexAttribArray(mNormalHandler);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        // Pass in the modelview matrix
        GLES20.glUniformMatrix4fv(mMVMatrixHandler, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        // Pass in combined matrix
        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        // Pass in the light position in the eye space
        GLES20.glUniform3f(mLightPosHandler, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the cube
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    }

    private void drawLight() {
        final int pointMVPMatrixHandler = GLES20.glGetUniformLocation(mLightPointProgramHandler, "u_MVPMatrix");
        final int pointPositionHandler = GLES20.glGetAttribLocation(mLightPointProgramHandler, "a_Position");

        // Pass in the position of light
        GLES20.glVertexAttrib3f(pointPositionHandler, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);
        // Since we are not using the buffer object, disable vertex array for this attribute
        GLES20.glDisableVertexAttribArray(pointPositionHandler);

        // Pass in the transformation matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        // Draw the points
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }


    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        //Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        Matrix.setLookAtM(mViewMatrix, 0, camPos.x, camPos.y + mHeight, camPos.z, look.x, look.y + mHeight, look.z, upX, upY, upZ);

        final String vertexShader = getVertexShader();
        final String fragmentShader = getFragmentShader();

        final int vertexShaderHandler = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandler = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        mPerVertexProgramHandler = createAndLinkProgram(vertexShaderHandler, fragmentShaderHandler,
                new String[] {"a_Position", "a_Color", "a_Normal"});

        final String pointVertexShader =
                "uniform mat4 u_MVPMatrix;\n" +
                        "attribute vec4 a_Position;\n" +
                        "void main() {\n" +
                        "    gl_Position = u_MVPMatrix * a_Position;\n" +
                        "    gl_PointSize = 5.0;\n" +
                        "}";

        final String pointFragmentShader =
                "precision mediump float;\n" +
                        "void main() {\n" +
                        "    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);\n" +
                        "}";

        final int pointVertexShaderHandler = compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
        final int pointFragmentShaderHandler = compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
        mLightPointProgramHandler = createAndLinkProgram(pointVertexShaderHandler, pointFragmentShaderHandler,
                new String[] {"a_Position"});
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        final float ratio = (float) width/height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Do a complete rotation every 10 seconds
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        GLES20.glUseProgram(mPerVertexProgramHandler);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mPerVertexProgramHandler, "u_MVPMatrix");
        mMVMatrixHandler = GLES20.glGetUniformLocation(mPerVertexProgramHandler, "u_MVMatrix");
        mLightPosHandler = GLES20.glGetUniformLocation(mPerVertexProgramHandler, "u_LightPos");

        mPositionHandler = GLES20.glGetAttribLocation(mPerVertexProgramHandler, "a_Position");
        mColorHandler = GLES20.glGetAttribLocation(mPerVertexProgramHandler, "a_Color");
        mNormalHandler = GLES20.glGetAttribLocation(mPerVertexProgramHandler, "a_Normal");


        // Calculate position of light. Rotate and then push into the distance
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

        // Draw cube
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        drawCube();

        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 0.0f);
        GLES20.glUseProgram(mLightPointProgramHandler);
        drawLight();

        double angle = Math.toRadians(mAngle);

        //TODO: whatthefuck is this minus?
        camPos.x = (float)Math.sin(-angle)*mDistance;
        camPos.z = (float)(Math.cos(angle)*mDistance - mDistance);

        Matrix.setLookAtM(mViewMatrix, 0, camPos.x, camPos.y + mHeight, camPos.z, look.x, look.y + mHeight, look.z, 0.0f, 1.0f, 0.0f);
    }
}

