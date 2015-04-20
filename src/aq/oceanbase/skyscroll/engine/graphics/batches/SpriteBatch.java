package aq.oceanbase.skyscroll.engine.graphics.batches;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.graphics.TextureRegion;
import aq.oceanbase.skyscroll.engine.graphics.ProgramManager;
import aq.oceanbase.skyscroll.engine.graphics.RenderableObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

// TODO: describe usage in detail
// Firstly initialize(), then beginBatch(), then add elements, then endBatch()
// Remember, that sprite origin is located in the CENTER of the sprite, not top left corner

public class SpriteBatch extends RenderableObject {
    public final static int POSITION_DATA_SIZE = 3;         // X, Y, Z
    public final static int TEXTURE_DATA_SIZE = 2;          // U, V
    public final static int INDEX_DATA_SIZE = 1;            // Index
    public final static int COLOR_DATA_SIZE = 4;            // R, G, B, A


    public final static int VERTEX_3D = 6;                  // X, Y, Z, U, V, Index
    public final static int COLORED_VERTEX_3D = 10;         // X, Y, Z, U, V, Index, R, G, B, A

    public final static int VERTICES_PER_SPRITE = 4;        // Quad structure
    public final static int INDICES_PER_SPRITE = 6;         // Two triangles
    public final static int MAX_BATCHSIZE = 16;             // Maximum size of a batch

    private boolean mFiltered = false;
    private boolean mIgnoreCamera = false;
    private boolean mDepthTest = true;

    private Camera mCam;                        // Camera instance

    private int mProgram;
    private int mTextureHandle;

    private short[] mIndices;                   // Indices for triangle vertices
    private float[] mVertices;                  // Triangle vertices
    private float[] mModelMatrices;             // Array of model matrices for each sprite
    private float[] mOrientationMatrix;         // Orientation matrix for each sprite (to support incam orientation)
    private FloatBuffer mVertexBuffer;          // Buffer of packaged vertex information
    private ShortBuffer mIndexBuffer;           // Buffer of indices

    private int mVertexStride;                  // Stride for vertex buffer
    private int mBatchSize;                     // Amount of sprites in the batch
    private int mBufferCounter;                 // Vertex buffer element counter

    private int mVertexTypeSize;                // Size of vertex type (plain 3d vertex, colored 3d vertex)
    private float[] mColor;                     // Color info for sprites

    // -- Constructor -- //
    // Desc: Prepares the SpriteBatch instance for initialization and further use
    // Inputs:
    //      vertexType: the type of the vertex: colored or plain
    //      programHandle: handle to the shader program
    //      textureHandle: handle to the texture program
    public SpriteBatch(int vertexType, int textureHandle) {
        this.mVertexTypeSize = vertexType;
        this.mVertexStride = mVertexTypeSize * (Float.SIZE / 8);        // Size is represented by bits hence the division
        this.mColor = new float[] {1.0f, 1.0f, 1.0f, 1.0f};             // Default color is white with full alpha

        this.mIndices = new short[ MAX_BATCHSIZE * INDICES_PER_SPRITE ];
        this.mVertices = new float[ MAX_BATCHSIZE * VERTICES_PER_SPRITE * mVertexTypeSize ];
        this.mModelMatrices = new float[ MAX_BATCHSIZE * 16 ];

        this.mTextureHandle = textureHandle;

        this.mBufferCounter = 0;

        // The whole array of indices is calculated
        int len = mIndices.length;
        short j = 0;
        for (int i = 0; i < len; i += INDICES_PER_SPRITE, j += VERTICES_PER_SPRITE) {
            mIndices[i + 0] = (short)(j + 0);
            mIndices[i + 1] = (short)(j + 2);
            mIndices[i + 2] = (short)(j + 1);
            mIndices[i + 3] = (short)(j + 1);
            mIndices[i + 4] = (short)(j + 2);
            mIndices[i + 5] = (short)(j + 3);
        }

        // The memory for the whole buffer is allocated to reallocation during execution
        mIndexBuffer = ByteBuffer.allocateDirect( INDICES_PER_SPRITE * MAX_BATCHSIZE * (Short.SIZE / 8) ).
                order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndexBuffer.put(mIndices).position(0);

        mVertexBuffer = ByteBuffer.allocateDirect( mVertexTypeSize * VERTICES_PER_SPRITE * MAX_BATCHSIZE * (Float.SIZE / 8) ).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public void setFiltered(boolean value) {
        this.mFiltered = value;
    }

    public void set2DMode(boolean value) {
        this.mIgnoreCamera = value;
    }

    public void useDepthTest(boolean value) {
        this.mDepthTest = value;
    }

    // -- Begin batching -- //
    // Desc: Setup the batch. Reset the counter variables and calculate the VP matrix
    // Inputs:
    //      cam: Camera object for VP matrix
    //      orientationMatrix: matrix to enable incam sprite orientation
    // Returns: none
    public void beginBatch(Camera cam, float[] orientationMatrix) {
        mBatchSize = 0;
        mBufferCounter = 0;

        mCam = cam;
        mOrientationMatrix = orientationMatrix;
    }

    public void beginBatch(Camera cam) {
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);

        beginBatch(cam, matrix);
    }

    // -- End batching -- //
    // Desc: if batch is not empty fill the vertex buffer and start drawing
    // Inputs: none
    // Returns: none
    public void endBatch() {
        if ( mBatchSize > 0 ) {

            mVertexBuffer.clear();
            mVertexBuffer.put(mVertices, 0, mBufferCounter);
            mVertexBuffer.flip();

            draw(mCam);
        }
    }


    // -- Add element to a batch -- //
    // Desc: Add element to the batch. Color info is added to the packaged buffer last
    //       to support packaging with default color.
    // Inputs:
    //      width: width of the sprite in world coordinates
    //      height: height of the sprite in world coordinates
    //      color: the color of the sprite. another variant of the function uses default value
    //      texRgn: texture region of the sprite
    //      modelMatrix: model matrix for the sprite to multiply by VPMatrix
    // Returns: none
    public void batchElement(float width, float height, float[] color, TextureRegion texRgn, float[] modelMatrix) {
        if ( mBatchSize == MAX_BATCHSIZE ) {        // If maximum batch size is reached
            endBatch();                             // current batch is rendered

            mBatchSize = 0;                         // Then batch counters are reset
            mBufferCounter = 0;                     // and batch is filled from beginning
        }

        float halfWidth = width/2.0f;
        float halfHeight = height/2.0f;
        float x1 = -halfWidth;           //left
        float x2 = halfWidth;           //right
        float y1 = halfHeight;          //top
        float y2 = -halfHeight;          //bottom

        //Top left vertex of the sprite (Vertex 0)
        mVertices[mBufferCounter++] = x1;             // X coordinate of the sprite
        mVertices[mBufferCounter++] = y1;             // Y coordinate of the sprite
        mVertices[mBufferCounter++] = 0.0f;           // Z coordinate of the sprite
        mVertices[mBufferCounter++] = texRgn.u1;      // U texture coordinate
        mVertices[mBufferCounter++] = texRgn.v1;      // V texture coordinate
        mVertices[mBufferCounter++] = mBatchSize;     // Sprite index is saved in vertex
        mVertices[mBufferCounter++] = color[0];
        mVertices[mBufferCounter++] = color[1];
        mVertices[mBufferCounter++] = color[2];
        mVertices[mBufferCounter++] = color[3];

        //Top right vertex of the sprite (Vertex 1)
        mVertices[mBufferCounter++] = x2;
        mVertices[mBufferCounter++] = y1;
        mVertices[mBufferCounter++] = 0.0f;
        mVertices[mBufferCounter++] = texRgn.u2;
        mVertices[mBufferCounter++] = texRgn.v1;
        mVertices[mBufferCounter++] = mBatchSize;
        mVertices[mBufferCounter++] = color[0];
        mVertices[mBufferCounter++] = color[1];
        mVertices[mBufferCounter++] = color[2];
        mVertices[mBufferCounter++] = color[3];

        //Bottom left vertex of the sprite (Vertex 2)
        mVertices[mBufferCounter++] = x1;
        mVertices[mBufferCounter++] = y2;
        mVertices[mBufferCounter++] = 0.0f;
        mVertices[mBufferCounter++] = texRgn.u1;
        mVertices[mBufferCounter++] = texRgn.v2;
        mVertices[mBufferCounter++] = mBatchSize;
        mVertices[mBufferCounter++] = color[0];
        mVertices[mBufferCounter++] = color[1];
        mVertices[mBufferCounter++] = color[2];
        mVertices[mBufferCounter++] = color[3];

        //Bottom right vertex of the sprite (Vertex 3)
        mVertices[mBufferCounter++] = x2;
        mVertices[mBufferCounter++] = y2;
        mVertices[mBufferCounter++] = 0.0f;
        mVertices[mBufferCounter++] = texRgn.u2;
        mVertices[mBufferCounter++] = texRgn.v2;
        mVertices[mBufferCounter++] = mBatchSize;
        mVertices[mBufferCounter++] = color[0];
        mVertices[mBufferCounter++] = color[1];
        mVertices[mBufferCounter++] = color[2];
        mVertices[mBufferCounter++] = color[3];

        // Model matrix is copied to the array
        for (int i = 0; i < 16; i++) {
            //TODO: possible optimization
            mModelMatrices[mBatchSize*16 + i] = modelMatrix[i];
        }

        mBatchSize++;
    }

    public void batchElement(float width, float height, TextureRegion texRgn, float[] modelMatrix) {
        if ( mBatchSize == MAX_BATCHSIZE ) {
            endBatch();

            mBatchSize = 0;
            mBufferCounter = 0;
        }

        float halfWidth = width/2.0f;
        float halfHeight = height/2.0f;
        float x1 = -halfWidth;           //left
        float x2 = halfWidth;           //right
        float y1 = halfHeight;          //top
        float y2 = -halfHeight;          //bottom

        //Top left vertex of the sprite (Vertex 0)
        mVertices[mBufferCounter++] = x1;             //X coordinate of the sprite
        mVertices[mBufferCounter++] = y1;             //Y coordinate of the sprite
        mVertices[mBufferCounter++] = 0.0f;           //Z coordinate of the sprite
        mVertices[mBufferCounter++] = texRgn.u1;
        mVertices[mBufferCounter++] = texRgn.v1;
        mVertices[mBufferCounter++] = mBatchSize;

        //Top right vertex of the sprite (Vertex 1)
        mVertices[mBufferCounter++] = x2;
        mVertices[mBufferCounter++] = y1;
        mVertices[mBufferCounter++] = 0.0f;
        mVertices[mBufferCounter++] = texRgn.u2;
        mVertices[mBufferCounter++] = texRgn.v1;
        mVertices[mBufferCounter++] = mBatchSize;

        //Bottom left vertex of the sprite (Vertex 2)
        mVertices[mBufferCounter++] = x1;
        mVertices[mBufferCounter++] = y2;
        mVertices[mBufferCounter++] = 0.0f;
        mVertices[mBufferCounter++] = texRgn.u1;
        mVertices[mBufferCounter++] = texRgn.v2;
        mVertices[mBufferCounter++] = mBatchSize;

        //Bottom right vertex of the sprite (Vertex 3)
        mVertices[mBufferCounter++] = x2;
        mVertices[mBufferCounter++] = y2;
        mVertices[mBufferCounter++] = 0.0f;
        mVertices[mBufferCounter++] = texRgn.u2;
        mVertices[mBufferCounter++] = texRgn.v2;
        mVertices[mBufferCounter++] = mBatchSize;

        for (int i = 0; i < 16; i++) {
            //TODO: possible optimization
            mModelMatrices[mBatchSize*16 + i] = modelMatrix[i];
        }

        mBatchSize++;
    }


    // -- Initialize -- //
    // Desc: standard initialize function from Renderable interface. Function initializes program and binds texture.
    // TODO: possible optimization: move buffers initialization here
    @Override
    public void initialize(Context context, ProgramManager programManager) {
        mProgram = programManager.getProgram(ProgramManager.PROGRAM.SPRITE_BATCH);

        super.initialize(context, programManager);
    }

    @Override
    public void release() {
        //TODO: FILL
        super.release();
    }


    // -- Draw batch -- //
    // Desc: Draw the whole batch.
    @Override
    public void draw(Camera cam) {
        float[] VPMatrix = new float[16];

        GLES20.glUseProgram(mProgram);

        int VPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_VPMatrix");
        int modelMatricesHandle = GLES20.glGetUniformLocation(mProgram, "u_ModelMatrix");
        int textureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        int orientationMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_OrientationMatrix");

        int positionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        int texCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
        int indexHandle = GLES20.glGetAttribLocation(mProgram, "a_ModelMatrixIndex");
        int colorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");


        Matrix.setIdentityM(VPMatrix, 0);
        if (!mIgnoreCamera)
            Matrix.multiplyMM(VPMatrix, 0, cam.getProjM(), 0, cam.getViewM(), 0);

        GLES20.glUniformMatrix4fv(VPMatrixHandle, 1, false, VPMatrix, 0);
        GLES20.glUniformMatrix4fv(orientationMatrixHandle, 1, false, mOrientationMatrix, 0);

        GLES20.glUniformMatrix4fv(modelMatricesHandle, mBatchSize, false, mModelMatrices, 0);
        GLES20.glEnableVertexAttribArray(modelMatricesHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHandle);
        GLES20.glUniform1i(textureUniformHandle, 0);

        if (mFiltered) {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        }

        mVertexBuffer.position(0);        //set the position of position data in vertex buffer
        GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        mVertexBuffer.position(POSITION_DATA_SIZE);     // set position of texcoords in vertex buffer
        GLES20.glVertexAttribPointer(texCoordHandle, TEXTURE_DATA_SIZE, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        mVertexBuffer.position(POSITION_DATA_SIZE + TEXTURE_DATA_SIZE);     // set position of index in vertex buffer
        GLES20.glVertexAttribPointer(indexHandle, INDEX_DATA_SIZE, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(indexHandle);

        if (mVertexTypeSize == COLORED_VERTEX_3D) {
            mVertexBuffer.position(POSITION_DATA_SIZE + TEXTURE_DATA_SIZE + INDEX_DATA_SIZE);
            GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(colorHandle);
        } else {            // if vertex is not colored - use default color
            GLES20.glVertexAttrib4f(colorHandle, mColor[0], mColor[1], mColor[2], mColor[3]);
            GLES20.glDisableVertexAttribArray(colorHandle);
        }

        if (!mDepthTest)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        mIndexBuffer.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mBatchSize * INDICES_PER_SPRITE, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);

        if (!mDepthTest)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(indexHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }
}
