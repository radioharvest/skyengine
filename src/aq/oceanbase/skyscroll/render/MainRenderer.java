package aq.oceanbase.skyscroll.render;

import android.opengl.*;
import android.os.SystemClock;
import android.util.Log;
import aq.oceanbase.skyscroll.commons.GraphicsCommons;
import aq.oceanbase.skyscroll.generators.TreeGenerator;
import aq.oceanbase.skyscroll.loaders.ShaderLoader;
import aq.oceanbase.skyscroll.commons.MathMisc;
import aq.oceanbase.skyscroll.math.Vector2f;
import aq.oceanbase.skyscroll.math.Vector3f;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.tree.nodes.Node;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;



//TODO: check scope of all variables
public class MainRenderer  implements GLSurfaceView.Renderer {

    //Constants and sizes
    private final int mBytesPerFloat = 4;
    private final int mPositionDataSize = 3;
    private int mScreenWidth;
    private int mScreenHeight;

    //Constraints
    private float mMinHeight = 0.0f;
    private float mMaxHeight = 30.0f;
    private float mMinDist = 8.0f;
    private float mMaxDist = 20.0f;

    //Rendering settings
    private final float mNearPlane = 1.0f;
    private final float mFarPlane = 30.0f;
    private final float mFrustumDepthFactor = mFarPlane/mNearPlane;

    //Camera parameters
    private Vector3f camPos = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
    private Vector3f look = new Vector3f(0.0f, 0.0f, -1.0f);

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
    private int mNodeColorHandler;

    //FloatBuffers
    private FloatBuffer mNodesPositions;
    private FloatBuffer mLinesPositions;

    //Arrays
    private Node mNodes[];

    //Navigation variables
    private float mDistance = 15.0f;         //cam distance from origin
    private float mHeight = 0.0f;
    private float mAngle = 0.0f;

    //Touch variables
    private Vector2f mMomentum = new Vector2f(0.0f, 0.0f);
    private Vector2f mTouchScreenCoords = new Vector2f(0.0f, 0.0f);
    private TouchRay mTouchRay = new TouchRay(0, 0, 0, 0, 0, 0, 0);

    //Time variables
    private long mSwitchTime;

    //Nodes
    private int mSelectedNode;

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

        mNodes = generator.getNodes();
    }


    public void setScreenMetrics(int height, int width) {
        this.mScreenHeight = height;
        this.mScreenWidth = width;
    }


    //<editor-fold desc="Getters and Setters">
    public void setTouchScreenCoords(float x, float y) {
        this.mTouchScreenCoords.x = x;
        this.mTouchScreenCoords.y = y;
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
    //</editor-fold>


    //<editor-fold desc="Updaters">
    public void zoom(float distance) {
        if (Math.abs(distance) > 0.1) mDistance = mDistance - distance;
        if (mDistance <= mMinDist) mDistance = mMinDist;
        else if (mDistance > mMaxDist) mDistance = mMaxDist;
    }

    private void updateMomentum() {
        //Log.e("NavDebug", new StringBuilder().append("mMomentum.x: ").append(mMomentum.x).toString());
        //Log.e("NavDebug", new StringBuilder().append("mMomentum.y: ").append(mMomentum.y).toString());

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
    //</editor-fold>


    private void drawNodes() {
        float[] color;

        GLES20.glUseProgram(mNodeShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mNodeShaderProgram, "u_MVPMatrix");
        mNodesPositionHandler = GLES20.glGetAttribLocation(mNodeShaderProgram, "a_Position");
        mNodeColorHandler = GLES20.glGetAttribLocation(mNodeShaderProgram, "a_Color");

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        for (int i = 0; i < 32; i++) {
            GLES20.glVertexAttrib3f(mNodesPositionHandler, mNodes[i].posX, mNodes[i].posY, mNodes[i].posZ);
            GLES20.glDisableVertexAttribArray(mNodesPositionHandler);

            if (mNodes[i].isSelected()) color = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
            else color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

            GLES20.glVertexAttrib4f(mNodeColorHandler, color[0], color[1], color[2], color[3]);
            GLES20.glDisableVertexAttribArray(mNodeColorHandler);

            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
        }


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

        GLES20.glLineWidth(1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 56);
    }

    private void drawTree() {
        drawLines();
        drawNodes();
    }

    private void drawRay() {
        GLES20.glUseProgram(mLineShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mLineShaderProgram, "u_MVPMatrix");
        mLinesPositionHandler = GLES20.glGetAttribLocation(mLineShaderProgram, "a_Position");

        //float[] rayPositions = mTouchRay.getPositionArray();
        //final FloatBuffer rayPositionsBuffer = ByteBuffer.allocateDirect(rayPositions.length * mBytesPerFloat)
        float[] posArray = mTouchRay.getPositionArray();
        final FloatBuffer rayPositions = ByteBuffer.allocateDirect(posArray.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        mTouchRay.getFarPointV().print("Draw", "FarPoint");
        rayPositions.put(posArray);

        rayPositions.position(0);
        GLES20.glVertexAttribPointer(mLinesPositionHandler, mPositionDataSize, GLES20.GL_FLOAT, false, 0, rayPositions);
        GLES20.glEnableVertexAttribArray(mLinesPositionHandler);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        GLES20.glLineWidth(3.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        //drawPoint(mTouchRay.getFarPointV());
    }

    private void drawPoint(Vector3f point) {
        float[] color;
        //point.print("Touch", "printpoint");

        GLES20.glUseProgram(mNodeShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mNodeShaderProgram, "u_MVPMatrix");
        mNodesPositionHandler = GLES20.glGetAttribLocation(mNodeShaderProgram, "a_Position");
        mNodeColorHandler = GLES20.glGetAttribLocation(mNodeShaderProgram, "a_Color");

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        GLES20.glVertexAttrib3f(mNodesPositionHandler, point.x, point.y, point.z);
        GLES20.glDisableVertexAttribArray(mNodesPositionHandler);

        color = new float[] {1.0f, 1.0f, 0.0f, 1.0f};

        GLES20.glVertexAttrib4f(mNodeColorHandler, color[0], color[1], color[2], color[3]);
        GLES20.glDisableVertexAttribArray(mNodeColorHandler);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);

    }


    public void castTouchRay(float touchX, float touchY) {
        float[] result = new float[4];
        float[] MVMatrix = new float[16];
        int[] view = {0, 0, mScreenWidth, mScreenHeight};

        Matrix.multiplyMM(MVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        float winX = touchX;
        float winY = (float)mScreenHeight - touchY;

        GLU.gluUnProject(winX, winY, 1.0f, MVMatrix, 0, mProjectionMatrix, 0, view, 0, result, 0);      //get point on the far plane
        Vector3f far = new Vector3f( result[0]/result[3], result[1]/result[3], result[2]/result[3]);    //divide by w-component

        Vector3f camRotated = camPos.rotate(-mAngle, 0.0f, 1.0f, 0.0f);     //derotate to get cam position in model space

        mTouchRay = new TouchRay(camRotated, far, 1.0f);
        mTouchScreenCoords = new Vector2f(0.0f, 0.0f);
    }

    private void findSelected() {
        if (mSelectedNode != -1) mNodes[mSelectedNode].deselect();
        int sel = -1;
        for (int i = 0; i < mNodes.length; i++) {
            Vector3f curPos = mNodes[i].getPosV();
            if (mTouchRay.onRay(curPos)) {
                if (sel == -1) sel = i;
                else if (!mTouchRay.closestSelected(mNodes[sel].getPosV(), curPos)) sel = i;
            }
        }

        if (sel != -1) {
            mNodes[sel].select();
            mSelectedNode = sel;
        } else  {
            mSelectedNode = -1;
        }
    }


    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.05f, 0.0f, 0.1f, 0.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        mSwitchTime = SystemClock.uptimeMillis();

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
                        new String[] {"a_Position", "a_Color"});

        final String lineVertexShaderSource = ShaderLoader.getShader("/shaders/lines/lineVertexShader.glsl");
        final String lineFragmentShaderSource = ShaderLoader.getShader("/shaders/lines/lineFragmentShader.glsl");

        final int lineVertexShader = GraphicsCommons.compileShader(GLES20.GL_VERTEX_SHADER, lineVertexShaderSource);
        final int lineFragmentShader = GraphicsCommons.compileShader(GLES20.GL_FRAGMENT_SHADER, lineFragmentShaderSource);

        mLineShaderProgram = GraphicsCommons.
                createAndLinkProgram(lineVertexShader, lineFragmentShader,
                        new String[]{"a_Position"});

        Log.e("Draw", new StringBuilder().append("Screen coords: ").append(mScreenWidth).append(", ").append(mScreenHeight).toString());
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

        mScreenWidth = width;
        mScreenHeight = height;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, mNearPlane, mFarPlane);
        //Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, mNearPlane, mFarPlane);
        Log.e("Draw", new StringBuilder().append("Screen coords: ").append(mScreenWidth).append(", ").append(mScreenHeight).toString());
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        updateMomentum();
        updateAngle();

        if (mTouchRay.notNull()){
            drawPoint(mTouchRay.getFarPointV());
            drawRay();
        }

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, mAngle, 0.0f, 1.0f, 0.0f);

        drawTree();

        updateHeight();
        updateCameraPosition();

        Matrix.setLookAtM(mViewMatrix, 0,
                camPos.x, camPos.y, camPos.z,
                look.x, look.y, look.z,
                up.x, up.y, up.z);

        if (mTouchScreenCoords.nonZero()) {
            castTouchRay(mTouchScreenCoords.x, mTouchScreenCoords.y);
            findSelected();
        }

    }
}
