package aq.oceanbase.skyscroll.game.graphics.elements.ui;

import android.content.Context;
import android.graphics.*;
import android.opengl.Matrix;
import android.util.Log;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.graphics.batches.SpriteBatch;
import aq.oceanbase.skyscroll.engine.graphics.TextureRegion;
import aq.oceanbase.skyscroll.engine.graphics.ProgramManager;
import aq.oceanbase.skyscroll.engine.graphics.RenderableObject;
import aq.oceanbase.skyscroll.engine.utils.loaders.TextureLoader;
import aq.oceanbase.skyscroll.engine.utils.math.MathUtilities;
import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;

public class ScoreBar extends RenderableObject {
    public static enum SCOREALIGN {
        LEFT, RIGHT, CENTER
    }

    private int mScore = 0;

    private Vector3f mPos;          // position of upperleft corner of the bar
    private float mBarHeight;

    private int mPixelHeight;

    private SCOREALIGN mAlignment;
    private float mFontSize;

    private float[] mDigitWidths = new float[10];
    private TextureRegion[] mDigitRegions = new TextureRegion[10];

    private Bitmap mBitmap;

    private SpriteBatch mBatch;


    public ScoreBar(SCOREALIGN align) {
        mAlignment = align;

        //Log.e("Debug", "ScoreBar position: " + mPos.x + " " + mPos.y + " " + mPos.z);
    }

    public ScoreBar setScore(int score) {
        this.mScore = score;
        Log.e("Debug", "" + mScore);
        return this;
    }

    private void generateAtlas(Typeface tf) {
        Paint paint = new Paint();

        paint.setTypeface(tf);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
        paint.setTextSize(mPixelHeight);
        paint.setTextAlign(Paint.Align.LEFT);

        Paint.FontMetrics fm = paint.getFontMetrics();
        mFontSize = mPixelHeight * ( mPixelHeight / (Math.abs(fm.ascent) + Math.abs(fm.descent)) );
        paint.setTextSize(mFontSize);
        fm = paint.getFontMetrics();

        int bmpWidth = MathUtilities.getClosestPowerOfTwo((int) paint.measureText("0123456789"));
        int bmpHeight = MathUtilities.getClosestPowerOfTwo(mPixelHeight);

        buildDigitMetrics(paint, bmpWidth, bmpHeight);

        mBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);

        int y = (int)Math.abs(fm.ascent);
        canvas.drawText("0123456789", 0, y, paint);
    }

    private void buildDigitMetrics(Paint paint, int texWidth, int texHeight) {
        float texRgnCursor = 0.0f;
        for (int i = 0; i < 10; i++) {
            float pixelWidth = paint.measureText("" + i);

            mDigitWidths[i] = mBarHeight * (pixelWidth / (float)mPixelHeight);
            mDigitRegions[i] = new TextureRegion(texWidth, texHeight, texRgnCursor, 0.0f, pixelWidth, mPixelHeight);

            texRgnCursor += pixelWidth;
        }
    }

    private float getCharArrayWidth(char[] array) {
        float width = 0.0f;

        for (int i = 0; i < array.length; i++) {
            int index = Character.getNumericValue(array[i]);
            if (0 <= index && index <= 9)
                width += mDigitWidths[index];
        }

        return width;
    }

    public void onScreenMetricsSet(int posX, int posY, int pixelHeight, int[] screenMetrics) {
        float x = ( 2 * posX / (float)screenMetrics[2] ) - 1.0f;       // converting pixel position to screen coordinates (-1.0, 1.0)
        float y = ( 2 * posY / (float)screenMetrics[3] ) - 1.0f;

        mPixelHeight = pixelHeight;

        mBarHeight = ( 2 * mPixelHeight / (float)screenMetrics[3]);

        mPos = new Vector3f(x, y - mBarHeight/2.0f, 0.0f);
    }

    @Override
    public void initialize(Context context, ProgramManager programManager) {
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "AGENCYB.TTF");

        generateAtlas(tf);

        int textureHandle = TextureLoader.loadTexture(mBitmap);

        mBatch = new SpriteBatch(SpriteBatch.VERTEX_3D, textureHandle);
        mBatch.set2DMode(true);
        mBatch.initialize(context, programManager);

        Log.e("Debug", "ScoreBar inited");

        super.initialize(context, programManager);
    }

    @Override
    public void release() {
        super.release();
    }

    @Override
    public void draw(Camera cam) {
        float cursor;
        float[] spriteMatrix = new float[16];
        char[] scoreChars = ("" + mScore).toCharArray();

        switch (mAlignment) {
            case LEFT:
                cursor = mPos.x;
                break;
            case CENTER:
                cursor = mPos.x - ( getCharArrayWidth(scoreChars) / 2 );
                break;
            case RIGHT:
                cursor = mPos.x - getCharArrayWidth(scoreChars);
                break;
            default:
                cursor = mPos.x;
                break;
        }

        if (mBatch == null) {
            Log.e("Debug", "SHITHAPPENED, MAN");
        }

        mBatch.beginBatch(cam);

        for (int i = 0; i < scoreChars.length; i++) {
            int index = Character.getNumericValue(scoreChars[i]);
            if (0 <= index && index <= 9) {
                Matrix.setIdentityM(spriteMatrix, 0);
                Matrix.translateM(spriteMatrix, 0, cursor + ( mDigitWidths[index]/2.0f ), mPos.y, mPos.z);

                mBatch.batchElement(mDigitWidths[index], mBarHeight, mDigitRegions[index], spriteMatrix);

                cursor += mDigitWidths[index];
            }

        }

        mBatch.endBatch();
    }
}
