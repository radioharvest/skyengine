package aq.oceanbase.skyscroll.render;

import android.content.Context;
import android.opengl.*;
import android.util.Log;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.commons.SpriteTemplate;
import aq.oceanbase.skyscroll.loaders.ShaderLoader;
import aq.oceanbase.skyscroll.commons.MathMisc;
import aq.oceanbase.skyscroll.loaders.TextureLoader;
import aq.oceanbase.skyscroll.math.Vector2f;
import aq.oceanbase.skyscroll.math.Vector3f;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.tree.Tree;
import aq.oceanbase.skyscroll.tree.nodes.NodeOrderUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Date;


//TODO: check scope of all variables
public class MainRenderer implements GLSurfaceView.Renderer {

    private final Context mContext;

    //Constants and sizes
    //TODO: read about statics. maybe it's a bad idea to do sizes static
    public static final int mBytesPerFloat = 4;
    private int mScreenWidth;
    private int mScreenHeight;
    private final int mNodePosDataSize;
    private final int mSpritePosDataSize;
    private final int mTexCoordDataSize;

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
    private int mLineShaderProgram;
    private int mSpriteShaderProgram;
    private int mBckgndShaderProgram;

    //<editor-fold desc="Redundant">
    /*//matrices
    private int mMVPMatrixHandler;
    private int mSpriteMatrixHandler;
    private int mSpriteRotMatrixHandler;

    //positions
    private int mPositionHandler;

    //colors
    private int mColorHandler;

    //textures
    private int mTextureUniformHandler;
    private int mTextureCoordinateHandler;*/
    //</editor-fold>

    //FloatBuffers
    private FloatBuffer mSpriteVertices;
    private FloatBuffer mSpriteTexCoords;

    //Textures
    private int mSpriteTexDataHandler;
    private int mBckgrndTexDataHandler;

    //Navigation variables
    private float mDistance = 15.0f;         //cam distance from origin
    private float mHeight = 0.0f;
    private float mAngle = 0.0f;

    //Touch variables
    private Vector2f mMomentum = new Vector2f(0.0f, 0.0f);
    private Vector2f mTouchScreenCoords = new Vector2f(0.0f, 0.0f);

    //FPS Counter
    private int mFrameCounter;
    private long mTime;

    //Sprite
    private SpriteTemplate mSprite;

    //Tree
    private Tree mTree;


    public MainRenderer(Context context) {
        mContext = context;

        Log.e("RunDebug", "Renderer constructor stage passed");

        mTree = new Tree();

        SpriteTemplate spriteTemplate = new SpriteTemplate();

        mSpriteVertices = spriteTemplate.getSpriteVertices();       //separate var is used because to increase readability
        mSpriteTexCoords = spriteTemplate.getSpriteTexCoords();

        mNodePosDataSize = Tree.positionDataSize;
        mSpritePosDataSize = SpriteTemplate.posDataSize;
        mTexCoordDataSize = SpriteTemplate.texCoordDataSize;



        /*final float[] trData =
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
        mSpriteTexCoords.put(spriteTextureCoordinateData).position(0);*/
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

    public void processTap(float x, float y) {
        TouchRay ray = castTouchRay(x, y);
        mTree.performRaySelection(ray);
    }
    //</editor-fold>


    //<editor-fold desc="Old drawing functions">
        /*private void drawRay(TouchRay tRay) {
        GLES20.glUseProgram(mLineShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mLineShaderProgram, "u_MVPMatrix");
        mPositionHandler = GLES20.glGetAttribLocation(mLineShaderProgram, "a_Position");

        //float[] rayPositions = mTouchRay.getPositionArray();
        //final FloatBuffer rayPositionsBuffer = ByteBuffer.allocateDirect(rayPositions.length * mBytesPerFloat)
        float[] posArray = tRay.getPositionArray();
        final FloatBuffer rayPositions = ByteBuffer.allocateDirect(posArray.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        tRay.getFarPointV().print("Draw", "FarPoint");
        rayPositions.put(posArray);

        rayPositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandler, mPositionDataSize, GLES20.GL_FLOAT, false, 0, rayPositions);
        GLES20.glEnableVertexAttribArray(mPositionHandler);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        GLES20.glLineWidth(3.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        //drawPoint(mTouchRay.getFarPointV());
    }*/

    /*private void drawSprite(Vector3f pos, float[] color) {
        GLES20.glUseProgram(mSpriteShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_MVPMatrix");
        mTextureUniformHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_Texture");
        mPositionHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_Position");
        mColorHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_Color");
        mTextureCoordinateHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_TexCoordinate");

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        Matrix.translateM(mMVPMatrix, 0, pos.x, pos.y, pos.z);
        Matrix.scaleM(mMVPMatrix, 0, 2.0f, 2.0f, 2.0f);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        GLES20.glVertexAttribPointer(mPositionHandler, 3, GLES20.GL_FLOAT, false, 3*mBytesPerFloat, mSpriteVertices);
        GLES20.glEnableVertexAttribArray(mPositionHandler);

        GLES20.glVertexAttrib4f(mColorHandler, color[0], color[1], color[2], color[3]);
        GLES20.glDisableVertexAttribArray(mColorHandler);

        GLES20.glVertexAttribPointer(mTextureCoordinateHandler, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mSpriteTexCoords);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandler);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSpriteTexDataHandler);
        GLES20.glUniform1i(mTextureUniformHandler, 0);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES20.glDisable(GLES20.GL_BLEND);


    }*/

    /*private void drawBackground() {
        GLES20.glUseProgram(mBckgndShaderProgram);

        mTextureUniformHandler = GLES20.glGetUniformLocation(mBckgndShaderProgram, "u_Texture");

        mPositionHandler = GLES20.glGetAttribLocation(mBckgndShaderProgram, "a_Position");
        mTextureCoordinateHandler = GLES20.glGetAttribLocation(mBckgndShaderProgram, "a_TexCoordinate");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBckgrndTexDataHandler);
        GLES20.glUniform1i(mTextureUniformHandler, 0);

        mSpriteVertices.position(0);
        GLES20.glVertexAttribPointer(mPositionHandler, 3, GLES20.GL_FLOAT, false, 3*mBytesPerFloat, mSpriteVertices);
        GLES20.glEnableVertexAttribArray(mPositionHandler);

        mSpriteTexCoords.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandler, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mSpriteTexCoords);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandler);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }*/

    /*private void drawNodes() {
        int cur;
        float[] color;
        float[] spriteMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        float[] convMatrix = new float[16];

        Matrix.multiplyMM(convMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        NodeOrderUnit[] renderOrder = mTree.getDrawOrder(convMatrix);

        GLES20.glUseProgram(mSpriteShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_MVPMatrix");
        mTextureUniformHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_Texture");
        mSpriteMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_SpriteMatrix");
        mSpriteRotMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_RotationMatrix");

        mPositionHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_Position");
        mTextureCoordinateHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_TexCoordinate");
        mColorHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_Color");

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
        GLES20.glVertexAttribPointer(mPositionHandler, 3, GLES20.GL_FLOAT, false, 3*mBytesPerFloat, mSpriteVertices);
        GLES20.glEnableVertexAttribArray(mPositionHandler);

        mSpriteTexCoords.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandler, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mSpriteTexCoords);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandler);

        for (int i = 0; i < renderOrder.length; i++) {

            cur = renderOrder[i].getId();

            if (mTree.nodes[cur].isSelected()) color = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
            else color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

            Matrix.setIdentityM(spriteMatrix, 0);
            Matrix.translateM(spriteMatrix, 0, mTree.nodes[cur].posX, mTree.nodes[cur].posY, mTree.nodes[cur].posZ);

            GLES20.glUniformMatrix4fv(mSpriteMatrixHandler, 1, false, spriteMatrix, 0);

            GLES20.glVertexAttrib4f(mColorHandler, color[0], color[1], color[2], color[3]);
            GLES20.glDisableVertexAttribArray(mColorHandler);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        }

    }*/

        /*private void drawLines() {
        GLES20.glUseProgram(mLineShaderProgram);

        mMVPMatrixHandler = GLES20.glGetUniformLocation(mLineShaderProgram, "u_MVPMatrix");
        mPositionHandler = GLES20.glGetAttribLocation(mLineShaderProgram, "a_Position");

        GLES20.glVertexAttribPointer(mPositionHandler, mPositionDataSize, GLES20.GL_FLOAT, false, 0, mTree.getLinesPositionsB());
        GLES20.glEnableVertexAttribArray(mPositionHandler);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMVPMatrix, 0);

        GLES20.glLineWidth(2.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 56);
    }*/
    //</editor-fold>


    //<editor-fold desc="Drawing functions">
    //TODO: introduction of class-binded data sizes may be bad idea. check later.
    private void drawTreeBackground() {
        GLES20.glUseProgram(mBckgndShaderProgram);

        int textureUniformHandler = GLES20.glGetUniformLocation(mBckgndShaderProgram, "u_Texture");

        int positionHandler = GLES20.glGetAttribLocation(mBckgndShaderProgram, "a_Position");
        int texCoordHandler = GLES20.glGetAttribLocation(mBckgndShaderProgram, "a_TexCoordinate");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBckgrndTexDataHandler);
        GLES20.glUniform1i(textureUniformHandler, 0);

        //mSpriteVertices.position(0);
        //GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT, false, 3*mBytesPerFloat, mSpriteVertices);
        mSpriteVertices.position(0);
        GLES20.glVertexAttribPointer(positionHandler, mSpritePosDataSize, GLES20.GL_FLOAT, false, 0, mSpriteVertices);
        GLES20.glEnableVertexAttribArray(positionHandler);

        //mSpriteTexCoords.position(0);
        //GLES20.glVertexAttribPointer(textureCoordinateHandler, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mSpriteTexCoords);
        mSpriteTexCoords.position(0);
        GLES20.glVertexAttribPointer(texCoordHandler, mTexCoordDataSize, GLES20.GL_FLOAT, false, 0, mSpriteTexCoords);
        GLES20.glEnableVertexAttribArray(texCoordHandler);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    private void drawNodes() {
        int cur;
        float[] color;
        float[] spriteMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        float[] convMatrix = new float[16];

        Matrix.multiplyMM(convMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        NodeOrderUnit[] renderOrder = mTree.getDrawOrder(convMatrix);

        GLES20.glUseProgram(mSpriteShaderProgram);

        int MVPMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_MVPMatrix");
        int textureUniformHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_Texture");
        int spriteMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_SpriteMatrix");
        int spriteRotMatrixHandler = GLES20.glGetUniformLocation(mSpriteShaderProgram, "u_RotationMatrix");

        int positionHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_Position");
        int texCoordHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_TexCoordinate");
        int colorHandler = GLES20.glGetAttribLocation(mSpriteShaderProgram, "a_Color");

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, -mAngle, 0.0f, 1.0f, 0.0f);

        GLES20.glUniformMatrix4fv(MVPMatrixHandler, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(spriteRotMatrixHandler, 1, false, rotationMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSpriteTexDataHandler);
        GLES20.glUniform1i(textureUniformHandler, 0);

        //mSpriteVertices.position(0);
        //GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT, false, 3*mBytesPerFloat, mSpriteVertices);
        mSpriteVertices.position(0);
        GLES20.glVertexAttribPointer(positionHandler, mSpritePosDataSize, GLES20.GL_FLOAT, false, 0, mSpriteVertices);
        GLES20.glEnableVertexAttribArray(positionHandler);

        //mSpriteTexCoords.position(0);
        //GLES20.glVertexAttribPointer(texCoordHandler, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mSpriteTexCoords);
        mSpriteTexCoords.position(0);
        GLES20.glVertexAttribPointer(texCoordHandler, mTexCoordDataSize, GLES20.GL_FLOAT, false, 0, mSpriteTexCoords);
        GLES20.glEnableVertexAttribArray(texCoordHandler);

        for (int i = 0; i < renderOrder.length; i++) {

            cur = renderOrder[i].getId();

            if (mTree.nodes[cur].isSelected()) color = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
            else color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

            Matrix.setIdentityM(spriteMatrix, 0);
            Matrix.translateM(spriteMatrix, 0, mTree.nodes[cur].posX, mTree.nodes[cur].posY, mTree.nodes[cur].posZ);

            GLES20.glUniformMatrix4fv(spriteMatrixHandler, 1, false, spriteMatrix, 0);

            GLES20.glVertexAttrib4f(colorHandler, color[0], color[1], color[2], color[3]);
            GLES20.glDisableVertexAttribArray(colorHandler);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        }

    }

    private void drawLines() {
        GLES20.glUseProgram(mLineShaderProgram);

        int MVPMatrixHandler = GLES20.glGetUniformLocation(mLineShaderProgram, "u_MVPMatrix");
        int positionHandler = GLES20.glGetAttribLocation(mLineShaderProgram, "a_Position");

        GLES20.glVertexAttribPointer(positionHandler, mNodePosDataSize, GLES20.GL_FLOAT, false, 0, mTree.getLinesPositionsB());
        GLES20.glEnableVertexAttribArray(positionHandler);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(MVPMatrixHandler, 1, false, mMVPMatrix, 0);

        GLES20.glLineWidth(2.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 56);
    }


    private void drawTree() {
        drawTreeBackground();

        updateMomentum();
        updateAngle();

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, mAngle, 0.0f, 1.0f, 0.0f);

        //TODO: redo enable/disable switch when performance optimizations are done
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        drawLines();
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        drawNodes();


        updateHeight();
        updateCameraPosition();

        Matrix.setLookAtM(mViewMatrix, 0,
                camPos.x, camPos.y, camPos.z,
                look.x, look.y, look.z,
                up.x, up.y, up.z);
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

        drawTree();

        countFPS();
    }
}
