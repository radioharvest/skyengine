package aq.oceanbase.skyscroll.legacy;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class RendererOne implements GLSurfaceView.Renderer{
    private final FloatBuffer mTriangle1Vertices;
    private final FloatBuffer mTriangle2Vertices;
    private final FloatBuffer mTriangle3Vertices;

    private int mMVPMatrixHandler;
    private int mPositionHandler;
    private int mColorHandler;

    private final int mBytesPerFloat = 4;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mModelMatrix = new float[16];

    private float[] mMVPMatrix = new float[16];

    private final int mStrideBytes = 7 * mBytesPerFloat;
    private final int mPositionOffset = 0;
    private final int mPositionDataSize = 3;
    private final int mColorOffset = 3;
    private final int mColorDataSize = 4;


    public RendererOne() {

        // This triangle is red, green, and blue.
        final float[] triangle1VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f};

        // This triangle is yellow, cyan, and magenta.
        final float[] triangle2VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 1.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                1.0f, 0.0f, 1.0f, 1.0f};

        // This triangle is white, gray, and black.
        final float[] triangle3VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f};

        // Initialize the buffers.
        mTriangle1Vertices = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangle2Vertices = ByteBuffer.allocateDirect(triangle2VerticesData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangle3Vertices = ByteBuffer.allocateDirect(triangle3VerticesData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();

        mTriangle1Vertices.put(triangle1VerticesData).position(0);
        mTriangle2Vertices.put(triangle2VerticesData).position(0);
        mTriangle3Vertices.put(triangle3VerticesData).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;

        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);


        final String vertexShader =
                        "uniform mat4 u_MVPMatrix;                  \n" +

                        "attribute vec4 a_Position;                 \n" +
                        "attribute vec4 a_Color;                    \n" +

                        "varying vec4 v_Color;                      \n" +

                        "void main() {                              \n" +
                        "    v_Color = a_Color;                     \n" +
                        "    gl_Position = u_MVPMatrix * a_Position;\n" +
                        "}                                            ";


        final String fragmentShader =
                        "precision mediump float;    \n" +
                        "varying vec4 v_Color;       \n" +

                        "void main() {               \n" +
                        "    gl_FragColor = v_Color; \n" +
                        "}                             ";

        int vertexShaderHandler = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        System.out.println("vertex shader: " + vertexShaderHandler);

        if (vertexShaderHandler != 0) {
            GLES20.glShaderSource(vertexShaderHandler, vertexShader);
            GLES20.glCompileShader(vertexShaderHandler);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandler, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            if (compileStatus[0] == 0) {
                System.out.println(GLES20.glGetShaderInfoLog(vertexShaderHandler));
                GLES20.glDeleteShader(vertexShaderHandler);
                vertexShaderHandler = 0;
            }
        }

        if (vertexShaderHandler == 0) {
            throw new RuntimeException("Error creating vertex shader.");
        }


        int fragmentShaderHandler = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        System.out.println("fragment shader: " + fragmentShaderHandler);

        if (fragmentShaderHandler != 0) {
            GLES20.glShaderSource(fragmentShaderHandler, fragmentShader);
            GLES20.glCompileShader(fragmentShaderHandler);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandler, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            if (compileStatus[0] == 0) {
                System.out.println(GLES20.glGetShaderInfoLog(fragmentShaderHandler));
                GLES20.glDeleteShader(fragmentShaderHandler);
                fragmentShaderHandler = 0;
            }

        }

        if (fragmentShaderHandler == 0) {
            throw new RuntimeException("Error creating fragment shader.");
        }


        //create program object and store handler to it
        int programHandler = GLES20.glCreateProgram();

        if (programHandler != 0) {
            //attach shaders to the program
            GLES20.glAttachShader(programHandler, vertexShaderHandler);
            GLES20.glAttachShader(programHandler, fragmentShaderHandler);

            //attaching parameters
            GLES20.glBindAttribLocation(programHandler, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandler, 1, "a_Color");

            //link two shaders
            GLES20.glLinkProgram(programHandler);

            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandler, GLES20.GL_LINK_STATUS, linkStatus, 0);

            if (linkStatus[0] == 0) {
                System.out.println(GLES20.glGetProgramInfoLog(programHandler));
                GLES20.glDeleteProgram(programHandler);
                programHandler = 0;
            }

        }

        if (programHandler == 0) {
            throw new RuntimeException("Error creating program");
        }


        mMVPMatrixHandler = GLES20.glGetUniformLocation(programHandler, "u_MVPMatrix");
        mPositionHandler = GLES20.glGetAttribLocation(programHandler, "a_Position");
        mColorHandler = GLES20.glGetAttribLocation(programHandler, "a_Color");

        GLES20.glUseProgram(programHandler);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        //set the openGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
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
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        //do complete rotation every 10 seconds
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        //draw triangle facing straight on
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriangle1Vertices);

        // Draw one translated a bit down and rotated to be flat on the ground.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees*2, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriangle2Vertices);

        // Draw one translated a bit to the right and rotated to be facing to the left.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 45.0f, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees*3, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriangle3Vertices);
    }

    private void drawTriangle(final FloatBuffer aTriangleBuffer) {

        //pass position info
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandler, mPositionDataSize, GLES20.GL_FLOAT, false, mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandler);

        //pass the color info
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandler, mColorDataSize, GLES20.GL_FLOAT, false, mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandler);

        //multiply model matrix by the view matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        //multiply MVP (modelview) by projection matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }

}
