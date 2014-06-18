package aq.oceanbase.skyscroll.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.opengl.*;
import android.util.Log;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.commons.GraphicsCommons;
import aq.oceanbase.skyscroll.generators.TreeGenerator;
import aq.oceanbase.skyscroll.loaders.ShaderLoader;
import aq.oceanbase.skyscroll.commons.MathMisc;
import aq.oceanbase.skyscroll.loaders.TextureLoader;
import aq.oceanbase.skyscroll.math.Vector2f;
import aq.oceanbase.skyscroll.math.Vector3f;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.tree.nodes.Node;
import aq.oceanbase.skyscroll.tree.nodes.NodeOrderUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


//TODO: check scope of all variables
public class MainRenderer implements GLSurfaceView.Renderer {

    private final Context mContext;

    //Constants and sizes
    private final int mBytesPerFloat = 4;
    private final int mPositionDataSize = 3;
    private final int mTextureCoordinateDataSize = 2;
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
    //programs
    private int mNodeShaderProgram;
    private int mLineShaderProgram;
    private int mSpriteShaderProgram;
    private int mBckgndShaderProgram;

    //matrices
    private int mMVPMatrixHandler;
    private int mSpriteRotMatrixHandler;

    //positions
    private int mNodesPositionHandler;
    private int mLinesPositionHandler;
    private int mSpritePositionHandler;
    private int mSpriteMatrixHandler;

    //colors
    private int mNodeColorHandler;
    private int mSpriteColorHandler;

    //textures
    private int mTextureUniformHandler;
    private int mTextureCoordinateHandler;
    private int mSpriteTexDataHandler;
    private int mBckgrndTexDataHandler;

    //FloatBuffers
    private FloatBuffer mNodesPositions;
    private FloatBuffer mLinesPositions;
    private FloatBuffer mSpriteVertices;
    private FloatBuffer mSpriteTexCoords;

    //Arrays
    private Node mNodes[];

    //Navigation variables
    private float mDistance = 15.0f;         //cam distance from origin
    private float mHeight = 0.0f;
    private float mAngle = 0.0f;

    //Touch variables
    private Vector2f mMomentum = new Vector2f(0.0f, 0.0f);
    private Vector2f mTouchScreenCoords = new Vector2f(0.0f, 0.0f);

    //Nodes
    private int mSelectedNode;

    //FPS Counter
    private int mFrameCounter;
    private long mTime;

    public MainRenderer(Context context) {
        mContext = context;

        Log.e("RunDebug", "Renderer constructor stage passed");
        TreeGenerator generator = new TreeGenerator();
        final float[] nodesPositionData = generator.getNodesPositionData();
        mNodesPositions = ByteBuffer.allocateDirect(nodesPositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mNodesPositions.put(nodesPositionData).position(0);

        final float[] linesPositionData = generator.getLinesPositionData();
        mLinesPositions = ByteBuffer.allocateDirect(linesPositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mLinesPositions.put(linesPositionData).position(0);

        mNodes = generator.getNodes();

        final float[] trData =
                {
                        -1.0f,  1.0f, 0.0f,
                        -1.0f, -1.0f, 0.0f,
                         1.0f, -1.0f, 0.0f,

                         1.0f, -1.0f, 0.0f,
                         1.0f,  1.0f, 0.0f,
                        -1.0f,  1.0f, 0.0f
                };

        mSpriteVertices = ByteBuffer.allocateDirect(trData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mSpriteVertices.put(trData).position(0);

        final float[] spriteTextureCoordinateData =
                {
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,

                    1.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f
                };

        mSpriteTexCoords = ByteBuffer.allocateDirect(spriteTextureCoordinateData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mSpriteTexCoords.put(spriteTextureCoordinateData).position(0);
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


    //<editor-fold desc="Selection">
    public TouchRay castTouchRay(float touchX, float touchY) {
        float[] result = new float[4];
        float[] MVMatrix = new float[16];
        int[] view = {0, 0, mScreenWidth, mScreenHeight};

        Matrix.multiplyMM(MVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        float winX = touchX;
        float winY = (float)mScreenHeight - touchY;

        GLU.gluUnProject(winX, winY, 1.0f, MVMatrix, 0, mProjectionMatrix, 0, view, 0, result, 0);      //get point on the far plane
        Vector3f far = new Vector3f( result[0]/result[3], result[1]/result[3], result[2]/result[3]);    //divide by w-component

        Vector3f camRotated = camPos.rotate(-mAngle, 0.0f, 1.0f, 0.0f);     //derotate to get cam position in model space

        return new TouchRay(camRotated, far, 1.0f);
    }

    private int findSelectedNode(TouchRay tRay) {
        int sel = -1;
        for (int i = 0; i < mNodes.length; i++) {
            Vector3f curPos = mNodes[i].getPosV();
            if (tRay.pointOnRay(curPos)) {
                if (sel == -1) sel = i;
                else if (!tRay.closestSelected(mNodes[sel].getPosV(), curPos)) sel = i;
            }
        }

        return sel;
    }

    public void selectNode(float x, float y) {
        TouchRay ray = castTouchRay(x, y);

        if (mSelectedNode != -1) mNodes[mSelectedNode].deselect();
        int selected = findSelectedNode(ray);

        if (selected != -1) {
            mNodes[selected].select();
            mSelectedNode = selected;
        } else  {
            mSelectedNode = -1;
        }

    }
    //</editor-fold>


    //<editor-fold desc="Drawing functions">
    private void drawRay(TouchRay tRay) {
        GLES20.glUseProgram(mLineShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mLineShaderProgram, "u_MVPMatrix");
        mLinesPositionHandler = GLES20.glGetAttribLocation(mLineShaderProgram, "a_Position");

        //float[] rayPositions = mTouchRay.getPositionArray();
        //final FloatBuffer rayPositionsBuffer = ByteBuffer.allocateDirect(rayPositions.length * mBytesPerFloat)
        float[] posArray = tRay.getPositionArray();
        final FloatBuffer rayPositions = ByteBuffer.allocateDirect(posArray.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        tRay.getFarPointV().print("Draw", "FarPoint");
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

    private void drawSprite(Vector3f pos, float[] color) {
        GLES20.glUseProgram(mSpriteShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_MVPMatrix");
        mTextureUniformHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_Texture");
        mSpritePositionHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_Position");
        mSpriteColorHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_Color");
        mTextureCoordinateHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_TexCoordinate");

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        Matrix.translateM(mMVPMatrix, 0, pos.x, pos.y, pos.z);
        Matrix.scaleM(mMVPMatrix, 0, 2.0f, 2.0f, 2.0f);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        GLES20.glVertexAttribPointer(mSpritePositionHandler, 3, GLES20.GL_FLOAT, false, 3*mBytesPerFloat, mSpriteVertices);
        GLES20.glEnableVertexAttribArray(mSpritePositionHandler);

        GLES20.glVertexAttrib4f(mSpriteColorHandler, color[0], color[1], color[2], color[3]);
        GLES20.glDisableVertexAttribArray(mSpriteColorHandler);

        GLES20.glVertexAttribPointer(mTextureCoordinateHandler, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mSpriteTexCoords);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandler);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSpriteTexDataHandler);
        GLES20.glUniform1i(mTextureUniformHandler, 0);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES20.glDisable(GLES20.GL_BLEND);


    }

    private void drawPointNodes() {
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

    private void drawBackground() {
        GLES20.glUseProgram(mBckgndShaderProgram);

        mTextureUniformHandler = GLES20.glGetUniformLocation(mBckgndShaderProgram, "u_Texture");

        mSpritePositionHandler = GLES20.glGetAttribLocation(mBckgndShaderProgram, "a_Position");
        mTextureCoordinateHandler = GLES20.glGetAttribLocation(mBckgndShaderProgram, "a_TexCoordinate");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBckgrndTexDataHandler);
        GLES20.glUniform1i(mTextureUniformHandler, 0);

        mSpriteVertices.position(0);
        GLES20.glVertexAttribPointer(mSpritePositionHandler, 3, GLES20.GL_FLOAT, false, 3*mBytesPerFloat, mSpriteVertices);
        GLES20.glEnableVertexAttribArray(mSpritePositionHandler);

        mSpriteTexCoords.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandler, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mSpriteTexCoords);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandler);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    private void drawNodes() {
        int cur;
        float[] color;
        float[] spriteMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        float[] convMatrix = new float[16];
        float[] tempPos = new float[4];
        NodeOrderUnit[] renderOrder = new NodeOrderUnit[mNodes.length];

        Matrix.multiplyMM(convMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        for (int i = 0; i < mNodes.length; i++) {
            Matrix.multiplyMV(tempPos, 0, convMatrix, 0, mNodes[i].getPos4f(), 0);
            renderOrder[i] = new NodeOrderUnit(i, tempPos[2]);
        }

        Arrays.sort(renderOrder);

        GLES20.glUseProgram(mSpriteShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_MVPMatrix");
        mTextureUniformHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_Texture");
        mSpriteMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_SpriteMatrix");
        mSpriteRotMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_RotationMatrix");

        mSpritePositionHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_Position");
        mTextureCoordinateHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_TexCoordinate");
        mSpriteColorHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_Color");

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, -mAngle, 0.0f, 1.0f, 0.0f);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mSpriteRotMatrixHandler, 1, false, rotationMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSpriteTexDataHandler);
        GLES20.glUniform1i(mTextureUniformHandler, 0);

        mSpriteVertices.position(0);
        GLES20.glVertexAttribPointer(mSpritePositionHandler, 3, GLES20.GL_FLOAT, false, 3*mBytesPerFloat, mSpriteVertices);
        GLES20.glEnableVertexAttribArray(mSpritePositionHandler);

        mSpriteTexCoords.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandler, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mSpriteTexCoords);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandler);

        for (int i = 0; i < renderOrder.length; i++) {

            cur = renderOrder[i].getId();

            if (mNodes[cur].isSelected()) color = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
            else color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

            Matrix.setIdentityM(spriteMatrix, 0);
            Matrix.translateM(spriteMatrix, 0, mNodes[cur].posX, mNodes[cur].posY, mNodes[cur].posZ);

            GLES20.glUniformMatrix4fv(mSpriteMatrixHandler, 1, false, spriteMatrix, 0);


            GLES20.glVertexAttrib4f(mSpriteColorHandler, color[0], color[1], color[2], color[3]);
            GLES20.glDisableVertexAttribArray(mSpriteColorHandler);

            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }

    }

    private void drawNodesLoc() {
        int cur;
        float[] color;
        float[] spriteMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        float[] convMatrix = new float[16];
        float[] tempPos = new float[4];
        NodeOrderUnit[] renderOrder = new NodeOrderUnit[mNodes.length];

        Matrix.multiplyMM(convMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        for (int i = 0; i < mNodes.length; i++) {
            Matrix.multiplyMV(tempPos, 0, convMatrix, 0, mNodes[i].getPos4f(), 0);
            renderOrder[i] = new NodeOrderUnit(i, tempPos[2]);
        }

        Arrays.sort(renderOrder);

        GLES20.glUseProgram(mSpriteShaderProgram);

        int MVPMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_MVPMatrix");
        int TextureUniformHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_Texture");
        int SpriteMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_SpriteMatrix");
        int SpriteRotMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_RotationMatrix");

        int SpritePositionHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_Position");
        int TextureCoordinateHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_TexCoordinate");
        int SpriteColorHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_Color");

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, -mAngle, 0.0f, 1.0f, 0.0f);

        GLES20.glUniformMatrix4fv(MVPMatrixHandler, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(SpriteRotMatrixHandler, 1, false, rotationMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSpriteTexDataHandler);
        GLES20.glUniform1i(TextureUniformHandler, 0);

        mSpriteVertices.position(0);
        GLES20.glVertexAttribPointer(SpritePositionHandler, 3, GLES20.GL_FLOAT, false, 3*mBytesPerFloat, mSpriteVertices);
        GLES20.glEnableVertexAttribArray(SpritePositionHandler);

        mSpriteTexCoords.position(0);
        GLES20.glVertexAttribPointer(TextureCoordinateHandler, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mSpriteTexCoords);
        GLES20.glEnableVertexAttribArray(TextureCoordinateHandler);

        for (int i = 0; i < renderOrder.length; i++) {

            cur = renderOrder[i].getId();

            if (mNodes[cur].isSelected()) color = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
            else color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

            Matrix.setIdentityM(spriteMatrix, 0);
            Matrix.translateM(spriteMatrix, 0, mNodes[cur].posX, mNodes[cur].posY, mNodes[cur].posZ);

            GLES20.glUniformMatrix4fv(SpriteMatrixHandler, 1, false, spriteMatrix, 0);


            GLES20.glVertexAttrib4f(SpriteColorHandler, color[0], color[1], color[2], color[3]);
            GLES20.glDisableVertexAttribArray(SpriteColorHandler);

            //GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
            //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
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

        GLES20.glLineWidth(2.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 56);
    }

    private void drawTree() {
        drawLines();
        drawNodes();
    }
    //</editor-fold>


    private void countFPS() {
        long currentTime = new Date().getTime();
        if (currentTime - mTime >= 1000) {
            Log.e("RunDebug", new StringBuilder().append("FPS: ").append(mFrameCounter).toString());
            mFrameCounter = 0;
            mTime = currentTime;
        } else mFrameCounter += 1;
    }


    //TODO: add shader object and upgrade loader with attrib handling
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.05f, 0.0f, 0.1f, 0.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //TODO: set up starting view angle
        Matrix.setLookAtM(mViewMatrix, 0,
                camPos.x, camPos.y, camPos.z,
                look.x, look.y, look.z,
                up.x, up.y, up.z);

        mTime = new Date().getTime();
        mFrameCounter = 0;

        final String shaderFolder = "/aq/oceanbase/skyscroll/shaders";

        mLineShaderProgram = ShaderLoader.
                getShaderProgram(shaderFolder + "/lines/lineVertex.glsl", shaderFolder + "/lines/lineFragment.glsl");
        mSpriteShaderProgram = ShaderLoader.
                getShaderProgram(shaderFolder + "/sprites/spriteVertex.glsl", shaderFolder + "/sprites/spriteFragment.glsl");
        mBckgndShaderProgram = ShaderLoader.
                getShaderProgram(shaderFolder + "/background/bckgrndVertex.glsl", shaderFolder + "/background/bckgrndFragment.glsl");

        mSpriteTexDataHandler = TextureLoader.loadTexture(mContext, R.drawable.node);
        mBckgrndTexDataHandler = TextureLoader.loadTexture(mContext, R.drawable.bckgnd1);

        /*final String nodeVertexShaderSource = ShaderLoader.getShaderSource(shaderFolder + "/nodes/nodeVertex.glsl");
        final String nodeFragmentShaderSource = ShaderLoader.getShaderSource(shaderFolder + "/nodes/nodeFragment.glsl");

        final int nodeVertexShader = GraphicsCommons.compileShader(GLES20.GL_VERTEX_SHADER, nodeVertexShaderSource);
        final int nodeFragmentShader = GraphicsCommons.compileShader(GLES20.GL_FRAGMENT_SHADER, nodeFragmentShaderSource);

        mNodeShaderProgram = GraphicsCommons.
                createAndLinkProgram(nodeVertexShader, nodeFragmentShader,
                        new String[] {"a_Position", "a_Color"});

        final String lineVertexShaderSource = ShaderLoader.getShaderSource(shaderFolder + "/lines/lineVertex.glsl");
        final String lineFragmentShaderSource = ShaderLoader.getShaderSource(shaderFolder + "/lines/lineFragment.glsl");

        final int lineVertexShader = GraphicsCommons.compileShader(GLES20.GL_VERTEX_SHADER, lineVertexShaderSource);
        final int lineFragmentShader = GraphicsCommons.compileShader(GLES20.GL_FRAGMENT_SHADER, lineFragmentShaderSource);

        mLineShaderProgram = GraphicsCommons.
                createAndLinkProgram(lineVertexShader, lineFragmentShader,
                        new String[]{"a_Position"});


        final String spriteVertexShaderSource = ShaderLoader.getShaderSource(shaderFolder + "/sprites/spriteVertex.glsl");
        final String spriteFragmentShaderSource = ShaderLoader.getShaderSource(shaderFolder + "/sprites/spriteFragment.glsl");

        final int spriteVertexShader = GraphicsCommons.compileShader(GLES20.GL_VERTEX_SHADER, spriteVertexShaderSource);
        final int spriteFragmentShader = GraphicsCommons.compileShader(GLES20.GL_FRAGMENT_SHADER, spriteFragmentShaderSource);

        mSpriteShaderProgram = GraphicsCommons.
                createAndLinkProgram(spriteVertexShader, spriteFragmentShader, new String[]{"a_Position", "a_Color", "a_TexCoordinate"});



        final String bckgndVertexShaderSource = ShaderLoader.getShaderSource(shaderFolder + "/background/bckgrndVertex.glsl");
        final String bckgndFragmentShaderSource = ShaderLoader.getShaderSource(shaderFolder + "/background/bckgrndFragment.glsl");

        final int bckgndVertexShader = GraphicsCommons.compileShader(GLES20.GL_VERTEX_SHADER, bckgndVertexShaderSource);
        final int bckgndFragmentShader = GraphicsCommons.compileShader(GLES20.GL_FRAGMENT_SHADER, bckgndFragmentShaderSource);

        mBckgndShaderProgram = GraphicsCommons.
                createAndLinkProgram(bckgndVertexShader, bckgndFragmentShader, new String[]{"a_Position", "a_TexCoordinate"});*/

        //Log.e("Draw", new StringBuilder().append("Screen coords: ").append(mScreenWidth).append(", ").append(mScreenHeight).toString());
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
        Log.e("Draw", new StringBuilder().append("Screen coords: ").append(mScreenWidth).append(", ").append(mScreenHeight).toString());
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        drawBackground();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        updateMomentum();
        updateAngle();

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, mAngle, 0.0f, 1.0f, 0.0f);

        //drawTree();
        drawLines();

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        drawNodesLoc();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        updateHeight();
        updateCameraPosition();

        Matrix.setLookAtM(mViewMatrix, 0,
                camPos.x, camPos.y, camPos.z,
                look.x, look.y, look.z,
                up.x, up.y, up.z);

        countFPS();
    }
}
