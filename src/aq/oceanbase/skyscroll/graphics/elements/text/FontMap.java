package aq.oceanbase.skyscroll.graphics.elements.text;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.graphics.*;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.graphics.render.Renderable;
import aq.oceanbase.skyscroll.utils.loaders.TextureLoader;

public class FontMap implements Renderable {

    public static int CHAR_START = 32;
    public static int CHAR_END = 126;
    public static int CHAR_CNT = ((CHAR_END - CHAR_START) + 1) + 1;

    public static int CHAR_NONE;
    public static int CHAR_UNKNOWN;

    // Min and max Font Sizes (Pixels)
    //TODO: why do I need this?
    public static int FONT_SIZE_MIN = 6;
    public static int FONT_SIZE_MAX = 180;

    private boolean mInitialized = false;

    private AssetManager mAssets;
    private String mFontFile;

    private int mTextureId;
    private int mTextureSize;
    private TextureRegion mTextureRgn;          //full texture region. TODO: why do I need this?

    private int mCellWidth, mCellHeight;
    private int mRowCnt, mColCnt;

    private int mFontSize;
    private float[] mColor;
    private float mScaleX, mScaleY;

    private int mFontPadX, mFontPadY;           //font padding in pixels
    private float mFontAscent, mFontDescent;
    private float mFontHeight;

    private float mCharWidthMax;
    private float mCharHeightMax;       //TODO: why do I need this?

    private float[] mCharWidths;
    private TextureRegion[] mCharRgns;

    private SpriteBatch mSpriteBatch;

    public FontMap(String fontFile, int fontSize, AssetManager assets) {
        this.mFontSize = 10;
        this.mFontPadX = this.mFontPadY = 1;
        this.mScaleX = this.mScaleY = 1.0f;

        this.mCharWidths = new float[CHAR_CNT];
        this.mCharRgns = new TextureRegion[CHAR_CNT];

        this.mFontFile = fontFile;
        this.mAssets = assets;
    }

    public void setScaling(float x, float y) {
        this.mScaleX = x;
        this.mScaleY = y;
    }


    //TODO: consider refactoring to avoid scaling
    public boolean generate() {
        //setup paint instance
        Typeface tf = Typeface.createFromAsset( mAssets, mFontFile );
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(mFontSize);
        //paint.setColor(0xFFFFFFFF);
        paint.setARGB(255, 255, 255, 255);
        paint.setTypeface(Typeface.create(tf, Typeface.BOLD));
        paint.setTypeface(tf);

        //get font metrics
        Paint.FontMetrics fm = paint.getFontMetrics();
        this.mFontHeight = (float)Math.ceil( Math.abs(fm.bottom) + Math.abs(fm.top) );
        this.mFontAscent = (float)Math.ceil( Math.abs(fm.ascent) );
        this.mFontDescent = (float)Math.ceil( Math.abs(fm.descent) );

        //determine the width of each character and also biggest width and height
        //TODO: change arrays to String
        this.mCharWidthMax = mCharHeightMax = 0;
        int arrCnt = 0;
        char[] s = new char[2];
        float[] w = new float[2];
        for (char c = (char)CHAR_START; c <= (char)CHAR_END; c++) {
            s[0] = c;
            paint.getTextWidths(s, 0, 1, w);
            mCharWidths[arrCnt] = w[0];

            if ( mCharWidths[arrCnt] > mCharWidthMax )         //check if current char is the widest
                mCharWidthMax = mCharWidths[arrCnt];

            arrCnt++;
        }

        //get width for NONE character
        s[0] = (char)CHAR_NONE;
        paint.getTextWidths(s, 0, 1, w);
        mCharWidths[arrCnt] = w[0];
        if ( mCharWidths[arrCnt] > mCharWidthMax )
            mCharWidthMax = mCharWidths[arrCnt];


        mCharHeightMax = mFontHeight;
        mCellWidth = (int)mCharWidthMax + ( 2*mFontPadX );
        mCellHeight = (int)mCharHeightMax + ( 2*mFontPadY );

        //TODO: why do I need this?
        int maxSize = mCellWidth > mCellHeight ? mCellWidth : mCellHeight;
        if ( maxSize < FONT_SIZE_MIN || maxSize > FONT_SIZE_MAX ) return false;

        //setting the texture sizes
        //TODO: rewrite later
        if ( maxSize <= 24 ) mTextureSize = 256;
        else if ( maxSize <= 40 ) mTextureSize = 512;
        else if ( maxSize <= 80 ) mTextureSize = 1024;
        else mTextureSize = 2048;

        //create empty bitmap with alpha only
        Bitmap bitmap = Bitmap.createBitmap(mTextureSize, mTextureSize, Bitmap.Config.ARGB_8888);       //originally was ALPHA_8
        Canvas canvas = new Canvas(bitmap);
        bitmap.eraseColor( 0x00000000 );        //Set transparent background (ARGB)

        mColCnt = mTextureSize / mCellWidth;
        mRowCnt = (int)Math.ceil( (float)CHAR_CNT / (float)mColCnt );

        //Draw fontmap
        float x = mFontPadX;
        float y = (mCellHeight - 1) - mFontDescent - mFontPadY;
        for ( char c = (char)CHAR_START; c <= (char)CHAR_END; c++ ) {
            s[0] = c;
            canvas.drawText(s, 0, 1, x, y, paint);          //TODO: check for better methods
            x += mCellWidth;
            if ( (x + mCellWidth - mFontPadX) > mTextureSize ) {
                x = mFontPadX;
                y += mCellHeight;
            }
        }
        //Draw none character
        s[0] = (char)CHAR_NONE;
        canvas.drawText(s, 0, 1, x, y, paint);


        //TODO: save bitmap as member or leave it like this?
        mTextureId = TextureLoader.loadTexture(bitmap);


        //calculate texture regions
        x = 0;
        y = 0;
        for ( int c = 0; c < CHAR_CNT; c++ ) {
            mCharRgns[c] = new TextureRegion( mTextureSize, mTextureSize, x, y, mCellWidth-1, mCellHeight-1 );      //TODO: why -1?
            x += mCellWidth;
            if ( x + mCellWidth > mTextureSize ) {
                x = 0;
                y += mCellHeight;
            }
        }

        //generate full texture region. TODO: why do I need this?
        this.mTextureRgn = new TextureRegion( mTextureSize, mTextureSize, 0, 0, mTextureSize, mTextureSize );

        return true;
    }

    public void drawText(String text, float x, float y, float z, Camera cam, float angleX, float angleY, float angleZ) {
        float[] modelMatrix = new float[16];

        float charWidth = mCellWidth*mScaleX;
        float charHeight = mCellHeight*mScaleY;

        int len = text.length();

        x += (charWidth / 2.0f) - (mFontPadX * mScaleX);            // starting positions to draw (centers of sprite)
        y += (charHeight / 2.0f) - (mFontPadY * mScaleY);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, x, y, z);
        Matrix.rotateM(modelMatrix, 0, angleX, 1, 0, 0);
        Matrix.rotateM(modelMatrix, 0, angleY, 0, 1, 0);
        Matrix.rotateM(modelMatrix, 0, angleZ, 0, 0, 1);

        float posX, posY;
        posX = posY = 0;

        mSpriteBatch.beginBatch(cam);

        for (int i = 0; i < len; i++) {
            int c = (int)text.charAt(i) - CHAR_START;
            if (c < 0 || c >= CHAR_CNT) c = CHAR_UNKNOWN;

            Matrix.translateM(modelMatrix, 0, posX, posY, 0);
            mSpriteBatch.batchElement(charWidth, charHeight, mCharRgns[c], modelMatrix);
            posX = mCharWidths[c] * mScaleX;
        }

        mSpriteBatch.endBatch();
    }

    public boolean isInitialized() {
        return this.mInitialized;
    }

    public void initialize(Context context, ProgramManager programManager) {
        this.generate();

        mSpriteBatch = new SpriteBatch(SpriteBatch.VERTEX_3D, mTextureId);
        mSpriteBatch.initialize(context, programManager);

        this.mInitialized = true;
    }

    public void release() {
        //TODO: FILL
    }

    public void draw(Camera cam) {
        float[] modelMatrix = new float[16];

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, 0.0f);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mSpriteBatch.beginBatch(cam);
        mSpriteBatch.batchElement(2.0f, 2.0f, mTextureRgn, modelMatrix);
        mSpriteBatch.endBatch();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
}
