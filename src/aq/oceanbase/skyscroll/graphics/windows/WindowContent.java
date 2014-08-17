package aq.oceanbase.skyscroll.graphics.windows;

import android.content.res.AssetManager;
import android.graphics.*;
import android.util.Log;
import aq.oceanbase.skyscroll.graphics.TextureRegion;
import aq.oceanbase.skyscroll.math.MathMisc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class WindowContent{
    private int mWidth;
    private int mHeight;

    private int mWindowHeight;

    private Bitmap mBitmap;

    private int mFontsize;

    private String mText;
    private ArrayList<String> mTextLines = new ArrayList<String>();

    private Bitmap mPicture;
    private String mFontfile;

    public float mUpperLimit;
    public TextureRegion mTexRgn;

    private FloatBuffer mTextureCoordinateBuffer;

    public WindowContent(String text) {
        this.mText = text;
        this.mFontsize = 20;
        this.mFontfile = "Roboto-Regular.ttf";
        this.mWidth = 100;
    }

    public WindowContent(int[] pixelMetrics, String text, String fontfile) {
        this.mWidth = pixelMetrics[0];
        this.mWindowHeight = pixelMetrics[1];

        this.mText = text;
        this.mFontfile = fontfile;
        this.mFontsize = 22;

        this.mTextLines = new ArrayList<String>();
        this.mPicture = null;
    }

    public WindowContent(int width, int height, String text, String fontfile, Bitmap picture) {
        this(new int[] {width, height}, text, fontfile);
        this.mPicture = picture;
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

    private void parseText(String text, Paint paint) {
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

                if ( wordWidth > mWidth ) {            // Check if word is larger than the border
                    if (currentLine.length() != 0) {
                        mTextLines.add(currentLine.toString());
                        currentLine = new StringBuilder();
                    }
                    currentLine.append(words[i]);
                }


                if ( (linePosition + wordWidth) > mWidth ) {
                    mTextLines.add(currentLine.toString());
                    linePosition = 0;
                    currentLine = new StringBuilder();
                }

                linePosition += (wordWidth + spaceWidth);
                currentLine.append(words[i]).append(" ");

            }

            if ( currentLine.length() != 0 ) mTextLines.add(currentLine.toString());

        }

        /*if ( currentLine.length() != 0 ) {
            mTextLines.add(currentLine.toString());
            Log.e("Draw", new StringBuilder().append(currentLine.toString()).toString());
        }*/
    }

    public void generateBitmap(AssetManager assets) {
        Typeface tf = Typeface.createFromAsset(assets, mFontfile);

        Paint paint = new Paint();
        //paint.setARGB(255, 255, 255, 255);
        paint.setTypeface(tf);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(mFontsize);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);

        parseText(mText, paint);
        Paint.FontMetrics fm = paint.getFontMetrics();

        this.mHeight = (int)(Math.abs(fm.top) + fm.leading) * mTextLines.size();
        if (mHeight < mWindowHeight) mHeight = mWindowHeight;

        int bmpWidth = MathMisc.getClosestPowerOfTwo(mWidth);
        int bmpHeight = MathMisc.getClosestPowerOfTwo(mHeight);

        //Log.e("Error", new StringBuilder().append("width: ").append(bmpWidth).append("height: ").append(bmpHeight).toString());

        mBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);
        //mBitmap.eraseColor( 0x00000000 );

        //canvas.drawPaint(paint);

        int y = (int)Math.abs(fm.top);
        //Log.e("Error", new StringBuilder().append(fm.top).toString());

        //Log.e("Draw", new StringBuilder().append(mTextLines.size()).toString());
        for ( int i = 0; i < mTextLines.size(); i++ ) {
            canvas.drawText(mTextLines.get(i), 0, y, paint);

            //Log.e("Draw", new StringBuilder().append(mTextLines.get(i)).toString());
            y += Math.abs((int)(fm.top + fm.leading));
            //Log.e("Draw", new StringBuilder("Y: ").append(y).toString());
        }

        this.mTexRgn = new TextureRegion(bmpWidth, bmpHeight, 0, 0, mWidth, mWindowHeight);
        this.mUpperLimit = ((float)mHeight / (float)bmpHeight);
        Log.e("Draw", new StringBuilder("Upper Limit: ").append(mHeight/(float)bmpHeight).toString());
        Log.e("Draw", new StringBuilder("Windowheight: ").append(mWindowHeight/(float)bmpHeight).toString());

        float[] textureCoordinatesData = {
                mTexRgn.u1, mTexRgn.v1,
                mTexRgn.u1, mTexRgn.v2,
                mTexRgn.u2, mTexRgn.v2,
                mTexRgn.u2, mTexRgn.v1
        };

        this.mTextureCoordinateBuffer = ByteBuffer.allocateDirect(textureCoordinatesData.length * (Float.SIZE / 8))
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mTextureCoordinateBuffer.put(textureCoordinatesData).position(0);
        //Log.e("Error", new StringBuilder().append("BUILT").toString());
    }
}
