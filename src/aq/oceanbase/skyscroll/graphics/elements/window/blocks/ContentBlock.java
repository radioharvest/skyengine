package aq.oceanbase.skyscroll.graphics.elements.window.blocks;

import android.content.Context;
import android.graphics.*;
import android.opengl.GLES20;
import android.util.Log;
import aq.oceanbase.skyscroll.graphics.*;
import aq.oceanbase.skyscroll.graphics.elements.window.Window;
import aq.oceanbase.skyscroll.graphics.elements.window.WindowBlock;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.utils.loaders.TextureLoader;
import aq.oceanbase.skyscroll.utils.math.MathUtilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * This class represents the text/picture content block for window
 * Contents are firstly generated as texture, then applied to quad
 */

public class ContentBlock extends WindowBlock {
    private ShortBuffer mOrderBuffer;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordinateBuffer;

    private int mShaderProgram;
    private int mTextureHandle;

    private Bitmap mBitmap;

    private int mFontsize;

    private String mText;
    private ArrayList<String> mTextLines = new ArrayList<String>();

    private Bitmap mPicture;

    private float mUpperLimit;       // filled region of texture is smaller, than the texture itself, hence the limit
    public TextureRegion mTexRgn;

    // -- Constructor -- //
    // Desc:
    // Inputs:
    //      text: text, that should be displayed in content
    public ContentBlock(Window root, float fraction) {
        super(root, fraction);

        this.mText = "NULL";
        this.mFontsize = 35;

        this.mTextLines = new ArrayList<String>();
        this.mPicture = null;
    }

    public ContentBlock(Window root, float fraction, String text) {
        this(root, fraction);
        this.mText = text;
    }

    public ContentBlock(Window root, float fraction, String text, int fontsize) {
        this(root, fraction, text);
        this.mFontsize = fontsize;
    }


    public ContentBlock setText(String text) {
        this.mText = text;
        return this;
    }

    public ContentBlock setFontsize(int size) {
        this.mFontsize = size;
        return this;
    }

    public ContentBlock addPicture(Bitmap pic) {
        this.mPicture = pic;
        return this;
    }

    public int getHeight() {
        return mBitmap.getHeight();
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public FloatBuffer getTextureBuffer() {
        mTextureCoordinateBuffer.position(0);
        return mTextureCoordinateBuffer;
    }

    public boolean bitmapRecycled() {
        return mBitmap.isRecycled();
    }


    public void rebuildTextureBuffer() {
        float[] textureCoordinatesData = {
                mTexRgn.u1, mTexRgn.v1,
                mTexRgn.u1, mTexRgn.v2,
                mTexRgn.u2, mTexRgn.v2,
                mTexRgn.u2, mTexRgn.v1
        };

        mTextureCoordinateBuffer.clear();
        mTextureCoordinateBuffer.put(textureCoordinatesData, 0, 8);
        mTextureCoordinateBuffer.flip();

    }

    public void scroll(int amount) {
        if (!(mTexRgn.v1 == 0.0f && mTexRgn.v2 == 1.0f)) {
            float uvAmount = (float)amount / (float)mBitmap.getHeight();
            mTexRgn.moveVertically(-uvAmount, 0.0f, mUpperLimit);
            this.rebuildTextureBuffer();
        }
    }

    // -- Parse text --
    // Desc: parsed input text and divides it into array of lines which depending on texture size (word wrapping)
    // Inputs:
    //      text: original text
    //      paint: Paint object, containing font information
    // Returns: nothing
    private void parseText(String text, Paint paint) {
        if (!mTextLines.isEmpty()) {
            return;                                     // this is pretty hacky, investigate when possible
        }

        int linePosition;
        int spaceWidth = (int)paint.measureText(" ");

        StringBuilder currentLine;

        String[] lines = text.split("\\n");


        for ( int k = 0; k < lines.length; k++ ) {
            String[] words = lines[k].split(" ");
            linePosition = 0;
            currentLine = new StringBuilder();

            for ( int i = 0; i < words.length; i++ ) {
                int wordWidth = (int)paint.measureText(words[i]);

                if ( wordWidth > mPixelMetrics[0] ) {            // Check if word is larger than the border
                    if (currentLine.length() != 0) {
                        mTextLines.add(currentLine.toString());
                        currentLine = new StringBuilder();
                    }
                    currentLine.append(words[i]);
                }


                if ( (linePosition + wordWidth) > mPixelMetrics[0] ) {
                    mTextLines.add(currentLine.toString());
                    linePosition = 0;
                    currentLine = new StringBuilder();
                }

                linePosition += (wordWidth + spaceWidth);
                currentLine.append(words[i]).append(" ");

            }

            if ( currentLine.length() != 0 ) mTextLines.add(currentLine.toString());

        }
    }

    // -- Generate texture bitmap --
    // Desc: generates bitmap that will further be used as texture.
    // Inputs:
    //      tf: Typeface object preloaded on initialize
    // Returns: nothing
    public void generateBitmap(Typeface tf) {
        int contentHeight;                       // Height of the filled region of the texture

        Log.e("Debug", "TEXTUREGENISCALLED");

        Paint paint = new Paint();
        paint.setTypeface(tf);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(mFontsize);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
        paint.setTextAlign(Paint.Align.CENTER);

        this.parseText(mText, paint);
        Paint.FontMetrics fm = paint.getFontMetrics();

        contentHeight = (int)(Math.abs(fm.top) + fm.leading) * mTextLines.size();
        if (contentHeight < mPixelMetrics[1]) contentHeight = mPixelMetrics[1];

        int bmpWidth = MathUtilities.getClosestPowerOfTwo(mPixelMetrics[0]);
        int bmpHeight = MathUtilities.getClosestPowerOfTwo(contentHeight);

        mBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);


        int y = (int)Math.abs(fm.top);
        for ( int i = 0; i < mTextLines.size(); i++ ) {
            canvas.drawText(mTextLines.get(i), mPixelMetrics[0]/2, y, paint);

            y += Math.abs((int)(fm.top + fm.leading));
        }

        // Texture region metrics are calculated. Upperlimit is a fraction of bitmap that is filled.
        this.mTexRgn = new TextureRegion(bmpWidth, bmpHeight, 0, 0, mPixelMetrics[0], mPixelMetrics[1]);
        this.mUpperLimit = ((float)contentHeight / (float)bmpHeight);

        float[] textureCoordinatesData = {
                mTexRgn.u1, mTexRgn.v1,
                mTexRgn.u1, mTexRgn.v2,
                mTexRgn.u2, mTexRgn.v2,
                mTexRgn.u2, mTexRgn.v1
        };

        this.mTextureCoordinateBuffer = ByteBuffer.allocateDirect(textureCoordinatesData.length * (Float.SIZE / 8))
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mTextureCoordinateBuffer.put(textureCoordinatesData).position(0);
    }


    @Override
    public void onSwipeVertical(float amount) {
        this.scroll((int)(-amount * 40));
    }

    @Override
    public void initialize(Context context, ProgramManager programManager) {
        super.initialize(context, programManager);

        float[] vertexData = new float[] {
                mPos.x, mPos.y, 0.0f,
                mPos.x, mPos.y - mHeight, 0.0f,
                mPos.x + mWidth, mPos.y - mHeight, 0.0f,
                mPos.x + mWidth, mPos.y, 0.0f
        };

        short[] orderData = new short[]
                {
                        0, 1, 3,
                        3, 1, 2
                };

        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * (Float.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(vertexData).position(0);

        mOrderBuffer = ByteBuffer.allocateDirect(orderData.length * (Short.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asShortBuffer();
        mOrderBuffer.put(orderData).position(0);

        mShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.WINDOWCONTENT);

        this.generateBitmap(mRoot.getTypeface());

        mTextureHandle = TextureLoader.loadTexture(mBitmap);
    }

    @Override
    public void release() {
        super.release();

        //GLES20.glDeleteTextures(1, new int[]{mTextureHandle}, 0);
        //GLES20.glDeleteProgram(mShaderProgram);
    }

    @Override
    public void draw(aq.oceanbase.skyscroll.graphics.Camera cam) {
        int MVPMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_MVPMatrix");
        int textureUniformHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_Texture");

        int colorHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Color");
        int positionHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Position");
        int texCoordHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_TexCoordinate");

        GLES20.glUseProgram(mShaderProgram);

        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mRoot.getMVPMatrix(), 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHandle);
        GLES20.glUniform1i(textureUniformHandle, 0);

        GLES20.glVertexAttrib4f(colorHandle, 1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glDisableVertexAttribArray(colorHandle);

        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        mTextureCoordinateBuffer.position(0);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTextureCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        //GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mOrderBuffer);
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
}
