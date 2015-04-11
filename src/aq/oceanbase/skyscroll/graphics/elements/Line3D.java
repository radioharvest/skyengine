package aq.oceanbase.skyscroll.graphics.elements;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.TextureRegion;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.graphics.render.RenderableObject;
import aq.oceanbase.skyscroll.utils.math.Ray3v;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Line3D extends RenderableObject {
    private boolean mSmooth = false;
    private boolean mDotted = false;
    private boolean mOccluded = false;
    private boolean mOcclusionResetDirty = false;

    private int mShaderProgram;

    private ShortBuffer mOrderBuffer;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;

    private float[] mModelMatrix = new float[16];

    private Ray3v mRay;
    private float mWidth = 1.1f;

    private Vertex[] mVertexArray;

    private float[] mColor = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

    private TextureRegion mTexRgn = new TextureRegion();


    public Line3D(Vector3f startPos, Vector3f endPos) {
        this.mRay = new Ray3v(startPos, endPos);

        this.buildLine();
    }

    public Line3D(Vector3f startPos, Vector3f endPos, float width, float[] color) {
        this(startPos, endPos);
        this.mWidth = width;
        this.mColor = color;

        rebuildVertices();
    }

    public Line3D(Vector3f startPos, Vector3f endPos, float width, float[] color, float[] textureData) {
        this(startPos, endPos, width, color);
    }

    public void setModelMatrix(float[] matrix) {
        this.mModelMatrix = matrix;
    }

    public Line3D setWidth(float width) {
        this.mWidth = width;
        rebuildVertices();

        return this;
    }

    public Line3D setColor(float[] color) {
        if (color.length == 4) this.mColor = color;

        if (color.length == 3)
            this.mColor = new float[] {color[0], color[1], color[2], 1.0f};

        if (color.length > 4)
            this.mColor = new float[] {color[0], color[1], color[2], color[3]};

        rebuildVertices();
        return this;
    }

    public Line3D setWidthAndColor(float width, float[] color) {
        this.mWidth = width;

        if (color.length == 4) this.mColor = color;

        if (color.length == 3)
            this.mColor = new float[] {color[0], color[1], color[2], 1.0f};

        if (color.length > 4)
            this.mColor = new float[] {color[0], color[1], color[2], color[3]};

        rebuildVertices();

        return this;
    }

    public void setTexRgn(TextureRegion texRgn) {
        this.mTexRgn = texRgn;
        if (!mOccluded)
            rebuildVertices();
    }

    public Line3D setDotted(boolean value) {
        this.mDotted = value;
        return this;
    }

    public Ray3v getRay() {
        return this.mRay;
    }

    public Vertex getVertex(int id) {
        return mVertexArray[id];
    }

    public Vertex[] getVertexArray() {
        return mVertexArray;
    }

    public boolean isDotted() {
        return this.mDotted;
    }



    private void buildLine() {
        if (mSmooth) mVertexArray = new Vertex[8];
        else mVertexArray = new Vertex[4];

        rebuildVertices();
    }

    private void rebuildVertices() {
        float halfWidth = mWidth/2;
        float halfLength = mRay.getLength()/2;

        mVertexArray[0] = new Vertex(-halfWidth,  halfLength, 0.0f, mColor[0], mColor[1], mColor[2], mColor[3], mTexRgn.u1, mTexRgn.v1);          // top left
        mVertexArray[1] = new Vertex(-halfWidth, -halfLength, 0.0f, mColor[0], mColor[1], mColor[2], mColor[3], mTexRgn.u1, mTexRgn.v2);          // bottom left
        mVertexArray[2] = new Vertex( halfWidth, -halfLength, 0.0f, mColor[0], mColor[1], mColor[2], mColor[3], mTexRgn.u2, mTexRgn.v2);          // bottom right
        mVertexArray[3] = new Vertex( halfWidth,  halfLength, 0.0f, mColor[0], mColor[1], mColor[2], mColor[3], mTexRgn.u2, mTexRgn.v1);          // top right
    }

    // the update is done WITHOUT recalculating norm
    public void occludeStartPoint(float amount) {
        float halfLength = this.mRay.getLength()/2;

        mVertexArray[1].setPosY(-halfLength + amount);
        mVertexArray[2].setPosY(-halfLength + amount);

        mOccluded = true;
    }

    public void occludeEndPoint(float amount) {
        float halfLength = this.mRay.getLength()/2;

        mVertexArray[0].setPosY(halfLength - amount);
        mVertexArray[3].setPosY(halfLength - amount);

        mOccluded = true;
    }

    public void checkOcclusion() {
        if ( mOccluded ) {
            mOccluded = false;
            mOcclusionResetDirty = true;
        } else if ( mOcclusionResetDirty ) {
            rebuildVertices();
            mOcclusionResetDirty = false;
        }
    }

    @Override
    public void initialize(Context context, ProgramManager programManager) {
        float[] vertexData =
                new float[] {
                        mVertexArray[0].getX(), mVertexArray[0].getY(), mVertexArray[0].getZ(),
                        mVertexArray[1].getX(), mVertexArray[1].getY(), mVertexArray[1].getZ(),
                        mVertexArray[2].getX(), mVertexArray[2].getY(), mVertexArray[2].getZ(),
                        mVertexArray[3].getX(), mVertexArray[3].getY(), mVertexArray[3].getZ(),
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

        super.initialize(context, programManager);
    }

    @Override
    public void release() {

        super.release();
    }

    @Override
    public void draw(Camera cam) {
        float[] orientationMatrix;
        float[] VPMatrix = new float[16];

        GLES20.glUseProgram(mShaderProgram);

        int VPMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_VPMatrix");
        int modelMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_ModelMatrix");
        int orientationMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_OrientationMatrix");

        int positionHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Position");
        int colorHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Color");

        Vector3f lookAxis = cam.getPos().subtractV(mRay.getCenterPos()).normalize();
        Vector3f upAxis = new Vector3f(mRay.getDirectionNorm());
        Vector3f rightAxis = upAxis.crossV(lookAxis).normalize();
        lookAxis = rightAxis.crossV(upAxis);

        orientationMatrix = new float[] {
                rightAxis.x,            rightAxis.y,            rightAxis.z,            0.0f,
                upAxis.x,               upAxis.y,               upAxis.z,               0.0f,
                lookAxis.x,             lookAxis.y,             lookAxis.z,             0.0f,
                mRay.getCenterPos().x,  mRay.getCenterPos().y,  mRay.getCenterPos().z,  1.0f
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