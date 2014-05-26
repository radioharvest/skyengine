package aq.oceanbase.skyscroll.Renderers;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import aq.oceanbase.skyscroll.math.Vector2f;
import aq.oceanbase.skyscroll.math.Vector3f;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class DemoRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "Demo Renderer";

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private float[] mMVPMatrix = new float[16];

    private float mAngle = 0.0f;
    private float mDistance = 15.0f;
    private float mHeight = 0.0f;

    private Vector2f mMomentum = new Vector2f(0.0f, 0.0f);

    private float mMinHeight = 1.0f;
    private float mMaxHeight = 45.0f;

    private final FloatBuffer mCubePositions;
    private final FloatBuffer mCubeColors;
    private final FloatBuffer mConnections;

    private int mMVPMatrixHandler;
    private int mPositionHandler;
    private int mConnectionHandler;
    private int mColorHandler;

    private int mPerVertexProgramHandler;
    private int mLineVertexProgramHandler;

    private final int mBytesPerFloat = 4;
    private final int mPositionDataSize = 3;
    private final int mConnectionDataSize = 3;
    private final int mColorDataSize = 4;

    private Vector3f camPos = new Vector3f(0.0f, 1.0f, -0.5f);
    private Vector3f look = new Vector3f(0.0f, 0.0f, -mDistance);

    public DemoRenderer() {

        // X, Y, Z
        final float[] cubePositionData = {
                //Remember counter-clockwise orientation of vertices in OpenGL

                // Front face
                -5.0f, 0.0f, 0.0f,
                0.0f, 1.0f, -5.0f,
                5.0f, 2.0f, 0.0f,
                0.0f, 3.0f, 5.0f,

                // Right face
                4.33f, 5.0f, 2.5f,
                -2.5f, 6.0f, 4.33f,
                -4.33f, 7.0f, -2.5f,
                2.5f, 8.0f, -4.33f,

                // Back face
                2.5f, 10.0f, 4.33f,
                -4.33f, 11.0f, 2.5f,
                -2.5f, 12.0f, -4.33f,
                4.33f, 13.0f, -2.5f,

                // Left face
                0.0f, 15.0f, 5.0f,
                -5.0f, 16.0f, 0.0f,
                0.0f, 17.0f, -5.0f,
                5.0f, 18.0f, 0.0f,

                // Front face
                -5.0f, 20.0f, 0.0f,
                0.0f, 21.0f, -5.0f,
                5.0f, 22.0f, 0.0f,
                0.0f, 23.0f, 5.0f,

                // Right face
                4.33f, 25.0f, 2.5f,
                -2.5f, 26.0f, 4.33f,
                -4.33f, 27.0f, -2.5f,
                2.5f, 28.0f, -4.33f,

                // Back face
                2.5f, 30.0f, 4.33f,
                -4.33f, 31.0f, 2.5f,
                -2.5f, 32.0f, -4.33f,
                4.33f, 33.0f, -2.5f,

                // Left face
                0.0f, 35.0f, 5.0f,
                -5.0f, 36.0f, 0.0f,
                0.0f, 37.0f, -5.0f,
                5.0f, 38.0f, 0.0f,
        };

        final float[] connectionsData = {
                -5.0f, 0.0f, 0.0f,
                4.33f, 5.0f, 2.5f,

                0.0f, 1.0f, -5.0f,
                -2.5f, 6.0f, 4.33f,

                5.0f, 2.0f, 0.0f,
                -4.33f, 7.0f, -2.5f,

                0.0f, 3.0f, 5.0f,
                2.5f, 8.0f, -4.33f,

                // Right face
                4.33f, 5.0f, 2.5f,
                2.5f, 10.0f, 4.33f,

                -2.5f, 6.0f, 4.33f,
                -4.33f, 11.0f, 2.5f,

                -4.33f, 7.0f, -2.5f,
                -2.5f, 12.0f, -4.33f,

                2.5f, 8.0f, -4.33f,
                4.33f, 13.0f, -2.5f,

                // Back face
                2.5f, 10.0f, 4.33f,
                0.0f, 15.0f, 5.0f,

                -4.33f, 11.0f, 2.5f,
                -5.0f, 16.0f, 0.0f,

                -2.5f, 12.0f, -4.33f,
                0.0f, 17.0f, -5.0f,

                4.33f, 13.0f, -2.5f,
                5.0f, 18.0f, 0.0f,

                // Left face
                0.0f, 15.0f, 5.0f,
                -5.0f, 20.0f, 0.0f,

                -5.0f, 16.0f, 0.0f,
                0.0f, 21.0f, -5.0f,

                0.0f, 17.0f, -5.0f,
                5.0f, 22.0f, 0.0f,

                5.0f, 18.0f, 0.0f,
                0.0f, 23.0f, 5.0f,

                // Front face
                -5.0f, 20.0f, 0.0f,
                4.33f, 25.0f, 2.5f,

                0.0f, 21.0f, -5.0f,
                -2.5f, 26.0f, 4.33f,

                5.0f, 22.0f, 0.0f,
                -4.33f, 27.0f, -2.5f,

                0.0f, 23.0f, 5.0f,
                2.5f, 28.0f, -4.33f,

                // Right face
                4.33f, 25.0f, 2.5f,
                2.5f, 30.0f, 4.33f,

                -2.5f, 26.0f, 4.33f,
                -4.33f, 31.0f, 2.5f,

                -4.33f, 27.0f, -2.5f,
                -2.5f, 32.0f, -4.33f,

                2.5f, 28.0f, -4.33f,
                4.33f, 33.0f, -2.5f,

                // Back face
                2.5f, 30.0f, 4.33f,
                0.0f, 35.0f, 5.0f,

                -4.33f, 31.0f, 2.5f,
                -5.0f, 36.0f, 0.0f,

                -2.5f, 32.0f, -4.33f,
                0.0f, 37.0f, -5.0f,

                4.33f, 33.0f, -2.5f,
                5.0f, 38.0f, 0.0f,
        };

        // R, G, B, Alpha
        final float[] cubeColorData = {
                // Front face (red)
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                // Right face (green)
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                // Front face (red)
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                // Right face (green)
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
        };

        mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePositionData).position(0);

        mCubeColors = ByteBuffer.allocateDirect(cubeColorData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColors.put(cubeColorData).position(0);

        mConnections = ByteBuffer.allocateDirect(connectionsData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mConnections.put(connectionsData).position(0);
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

    public void setDistance(float distance) {
        this.mDistance = distance;
    }

    public float getHeight() {
        return mHeight;
    }

    public void setHeight(float height) {
        if (height < mMinHeight) this.mHeight = mMinHeight;
        else if (height > mMaxHeight) this.mHeight = mMaxHeight;
        else this.mHeight = height;
    }

    public Vector2f getMomentum() {
        return mMomentum;
    }

    public void setMomentum(Vector2f momentum) {
        this.mMomentum.x = momentum.x;
        this.mMomentum.y = momentum.y;
    }


    protected String getVertexShader() {
        final String vertexShader =
                "uniform mat4 u_MVPMatrix;\n" +
                "attribute vec4 a_Position;\n" +
                "void main() {\n" +
                "    gl_Position = u_MVPMatrix * a_Position;\n" +
                "    gl_PointSize = 15.0;\n" +
                "}";

        return  vertexShader;
    }

    protected String getFragmentShader() {
        final String fragmentShader =
                "precision mediump float;\n" +
                "void main() {\n" +
                "    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);\n" +
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


    private void drawTree() {
        // Pass in the position information
        mCubePositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandler, mPositionDataSize, GLES20.GL_FLOAT, false, 0, mCubePositions);
        GLES20.glEnableVertexAttribArray(mPositionHandler);

        /*
        // Pass in the color information
        mCubeColors.position(0);
        GLES20.glVertexAttribPointer(mColorHandler, mColorDataSize, GLES20.GL_FLOAT, false, 0, mCubeColors);
        GLES20.glEnableVertexAttribArray(mColorHandler);*/

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        // Pass in combined matrix
        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        // Draw the cube
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 32);
    }

    private void drawConnections() {
        mConnections.position(0);
        GLES20.glVertexAttribPointer(mConnectionHandler, mConnectionDataSize, GLES20.GL_FLOAT, false, 0, mConnections);
        GLES20.glEnableVertexAttribArray(mConnectionHandler);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 56);
    }


    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES20.glClearColor(0.05f, 0.0f, 0.1f, 0.0f);
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
                new String[] {"a_Position"});

        final String lineVertexShader =
                "uniform mat4 uMVPMatrix;" +

                "attribute vec4 vPosition;" +
                "void main() {" +
                // the matrix must be included as a modifier of gl_Position
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}";

        final String lineFragmentShader =
                "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);;" +
                "}";

        final int pointVertexShaderHandler = compileShader(GLES20.GL_VERTEX_SHADER, lineVertexShader);
        final int pointFragmentShaderHandler = compileShader(GLES20.GL_FRAGMENT_SHADER, lineFragmentShader);
        mLineVertexProgramHandler = createAndLinkProgram(pointVertexShaderHandler, pointFragmentShaderHandler,
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
        final float far = 30.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mPerVertexProgramHandler);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mPerVertexProgramHandler, "u_MVPMatrix");

        mPositionHandler = GLES20.glGetAttribLocation(mPerVertexProgramHandler, "a_Position");
        mColorHandler = GLES20.glGetAttribLocation(mPerVertexProgramHandler, "a_Color");

        // Draw cube
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -mDistance);
        drawTree();

        drawConnections();

        //Log.d("Demo", new StringBuilder().append("Display momentum: ").append(mMomentum.x).toString());


        //TODO: move it to class?
        if (mMomentum.x != 0.0f) {
            if (mMomentum.x > 0.0f) {
                mMomentum.x = mMomentum.x - 1.7f;
                if (mMomentum.x < 0) mMomentum.x = 0.0f;
            }
            if (mMomentum.x < 0.0f) {
                mMomentum.x = mMomentum.x + 1.7f;
                if (mMomentum.x > 0) mMomentum.x = 0.0f;
            }
            mAngle = mAngle + mMomentum.x;
        }
        if (mMomentum.y != 0.0f) {
            if (mMomentum.y > 0.0f) {
                mMomentum.y = mMomentum.y - 0.1f;
                if (mMomentum.y < 0) mMomentum.y = 0.0f;
            }
            if (mMomentum.y < 0.0f) {
                mMomentum.y = mMomentum.y + 0.1f;
                if (mMomentum.y > 0) mMomentum.y = 0.0f;
            }
            mHeight = mHeight + mMomentum.y;
        }

        if (mAngle >= 360.0f) mAngle = mAngle - 360.0f;
        if (mAngle <= -360.0f) mAngle = mAngle + 360.0f;

        double angle = Math.toRadians(mAngle);

        //TODO: whatthefuck is this minus?
        camPos.x = (float)Math.sin(-angle)*mDistance;
        camPos.z = (float)(Math.cos(angle)*mDistance - mDistance);

        Matrix.setLookAtM(mViewMatrix, 0, camPos.x, camPos.y + mHeight, camPos.z, look.x, look.y + mHeight, look.z, 0.0f, 1.0f, 0.0f);
    }
}
