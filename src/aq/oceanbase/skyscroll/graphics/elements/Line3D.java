package aq.oceanbase.skyscroll.graphics.elements;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.TextureRegion;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.graphics.render.Renderable;
import aq.oceanbase.skyscroll.utils.math.Vector2f;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Line3D implements Renderable {
    private boolean mInititialized = false;
    private boolean mSmooth = false;
    private boolean mDotted = false;
    private boolean mOccluded = false;
    private boolean mOcclusionResetDirty = false;

    private int mShaderProgram;

    private ShortBuffer mOrderBuffer;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;

    private float[] mModelMatrix = new float[16];

    private Vector3f mStartPos;
    private Vector3f mEndPos;
    private Vector3f mCenter;
    private Vector3f mDirectionNorm;

    private float mLength;
    private float mWidth = 0.5f;

    private Vertex[] mVertexArray;

    private float[] mColor = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

    private TextureRegion mTexRgn = new TextureRegion();


    public Line3D(Vector3f startPos, Vector3f endPos) {
        this.mStartPos = startPos;
        this.mEndPos = endPos;

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
        rebuildVertices();
    }

    public Line3D setDotted(boolean value) {
        this.mDotted = value;
        return this;
    }

    public void setStartPosWithoutUpdate(Vector3f pos) {
        this.mStartPos = new Vector3f(pos);
    }

    public void setEndPosWithoutUpdate(Vector3f pos) {
        this.mEndPos = new Vector3f(pos);
    }

    public Vector3f getStartPos() {
        return new Vector3f(mStartPos);
    }

    public Vector3f getCenterPos() {
        return new Vector3f(mCenter);
    }

    public Vector3f getDirectionNorm() {
        return new Vector3f(mDirectionNorm);
    }

    public float getLength() {
        return mLength;
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
        Vector3f diff = mEndPos.subtractV(mStartPos);

        mLength = diff.length();
        mDirectionNorm = diff.normalize();
        mCenter = mStartPos.addV(diff.multiplySf(0.5f));

        if (mSmooth) mVertexArray = new Vertex[8];
        else mVertexArray = new Vertex[4];

        rebuildVertices();
    }

    private void rebuildVertices() {
        float halfWidth = mWidth/2;
        float halfLength = mLength/2;

        mVertexArray[0] = new Vertex(-halfWidth,  halfLength, 0.0f, mColor[0], mColor[1], mColor[2], mColor[3], mTexRgn.u1, mTexRgn.v1);          // top left
        mVertexArray[1] = new Vertex(-halfWidth, -halfLength, 0.0f, mColor[0], mColor[1], mColor[2], mColor[3], mTexRgn.u1, mTexRgn.v2);          // bottom left
        mVertexArray[2] = new Vertex( halfWidth, -halfLength, 0.0f, mColor[0], mColor[1], mColor[2], mColor[3], mTexRgn.u2, mTexRgn.v2);          // bottom right
        mVertexArray[3] = new Vertex( halfWidth,  halfLength, 0.0f, mColor[0], mColor[1], mColor[2], mColor[3], mTexRgn.u2, mTexRgn.v1);          // top right
    }

    // the update is done WITHOUT recalculating norm
    public void occludeStartPoint(float amount) {
        float halfLength = mLength/2;

        mVertexArray[1].setPosY(-halfLength + amount);
        mVertexArray[2].setPosY(-halfLength + amount);

        mOccluded = true;
    }

    public void occludeEndPoint(float amount) {
        float halfLength = mLength/2;

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


    public Vector3f intersectsLine(Line3D line) {
        Vector3f intersection = Vector3f.getZero();
        //first check in 2D XY
        Vector2f fractions = checkIntersection2D(
                new Vector2f(this.mStartPos.x, this.mStartPos.y),
                new Vector2f(this.mEndPos.x, this.mEndPos.y),
                new Vector2f(line.mStartPos.x, line.mStartPos.y),
                new Vector2f(line.mEndPos.x, line.mEndPos.y)
                );

        if (fractions == null)
            return null;

        Vector3f origDelta = mEndPos.subtractV(mStartPos).multiplySf(fractions.x);
        Vector3f inpDelta = line.mEndPos.subtractV(line.mStartPos).multiplySf(fractions.y);
        float collideDistanceSqr = (float)Math.pow((this.mWidth/2 + line.mWidth/2), 2.0f);

        if (origDelta.subtractV(inpDelta).lengthSqr() <= collideDistanceSqr) {
            return new Vector3f(mStartPos).addV(origDelta);
        }

        return null;
    }

    private Vector2f checkIntersection2D(Vector2f origStart, Vector2f origEnd, Vector2f inpStart, Vector2f inpEnd) {
        Vector2f intersect = Vector2f.getZero();
        Vector2f origDelta = origEnd.subtractV(origStart);
        Vector2f inpDelta = inpEnd.subtractV(inpStart);

        // math logic taken from this stackoverflow topic: http://stackoverflow.com/questions/563198
        // two lines: origStart + t*origDelta, inpStart + u*inpDelta
        // formula 1: t = ( inpStart - origStart ) x inpDelta / ( origDelta x inpDelta )
        // formula 2: u = ( inpStart - origStart ) x origDelta / ( origDelta x inpDelta )
        float numerator = inpStart.subtractV(origStart).crossV(origDelta);
        float denominator = origDelta.crossV(inpDelta);

        if ( (numerator == 0 && denominator == 0) || (numerator != 0 && denominator == 0) )
            return null;

        float u = numerator / denominator;
        if ( 0.0f <= u && u <= 1.0f ) {
            float t = inpStart.subtractV(origStart).crossV(inpDelta) / denominator;
            if ( 0.0f <= t && t <= 1.0f)
                return new Vector2f(t, u);
        }

        return null;
    }


    public boolean isInitialized() {
        return this.mInititialized;
    }

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
    }

    public void release() {

        this.mInititialized = false;
    }

    public void draw(Camera cam) {
        float[] orientationMatrix;
        float[] VPMatrix = new float[16];

        GLES20.glUseProgram(mShaderProgram);

        int VPMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_VPMatrix");
        int modelMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_ModelMatrix");
        int orientationMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_OrientationMatrix");

        int positionHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Position");
        int colorHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Color");

        Vector3f lookAxis = cam.getPos().subtractV(mCenter).normalize();
        Vector3f upAxis = new Vector3f(mDirectionNorm);
        Vector3f rightAxis = upAxis.crossV(lookAxis).normalize();
        lookAxis = rightAxis.crossV(upAxis);

        orientationMatrix = new float[] {
                rightAxis.x, rightAxis.y, rightAxis.z, 0.0f,
                upAxis.x,    upAxis.y,    upAxis.z,    0.0f,
                lookAxis.x,  lookAxis.y,  lookAxis.z,  0.0f,
                mCenter.x,   mCenter.y,   mCenter.z,   1.0f
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
