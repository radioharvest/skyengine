package aq.oceanbase.skyscroll.graphics.windows;

import android.graphics.*;
import android.util.Log;
import aq.oceanbase.skyscroll.graphics.TextureRegion;
import aq.oceanbase.skyscroll.math.MathMisc;
import aq.oceanbase.skyscroll.math.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ButtonBlock {
    private Button[] mButtons;

    private Vector3f mPos;

    private float mOffset;

    private Bitmap mBitmap;

    private float[] mMetrics;
    private int[] mPixelMetrics;

    private FloatBuffer mButtonPackedBuffer;
    private FloatBuffer mButtonColorBuffer;

    public ButtonBlock(Vector3f position, float[] floatMetrics, int[] pixelMetrics, float offset, String[] answers) {
        this.mPos = position;
        this.mOffset = offset;
        this.mMetrics = floatMetrics;
        this.mPixelMetrics = pixelMetrics;

        this.computeButtonsMetrics(answers);
    }

    public Vector3f getPos() {
        return this.mPos;
    }

    public float getPosX() {
        return this.mPos.x;
    }

    public float getPosY() {
        return this.mPos.y;
    }

    public float getPosZ() {
        return this.mPos.z;
    }

    public int getButtonsAmount() {
        return this.mButtons.length;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public Button getButton(int i) {
        if (i <= mButtons.length - 1) return mButtons[i];
        else return null;
    }


    public FloatBuffer getButtonPackedBuffer() {
        mButtonPackedBuffer.position(0);
        return mButtonPackedBuffer;
    }

    public FloatBuffer getButtonColorBuffer() {
        float[] colorData = getButtonColorData();

        mButtonColorBuffer.clear();
        mButtonColorBuffer.put(colorData, 0, colorData.length);
        mButtonColorBuffer.flip();

        mButtonColorBuffer.position(0);
        return mButtonColorBuffer;
    }

    private float[] getButtonColorData() {
        float[] colors;
        float[] colorData = new float[ mButtons.length * Button.VERTEX_AMOUNT * Button.COLOR_DATA_SIZE ];

        for ( int i = 0; i < mButtons.length; i++ ) {
            int stride = i*Button.VERTEX_AMOUNT*Button.COLOR_DATA_SIZE;
            colors = mButtons[i].getColorData();
            for ( int j = 0; j < colors.length; j++ ) {
                colorData[stride + j] = colors[j];
            }
        }

        return colorData;
    }


    private void computeButtonsMetrics(String[] buttonValues) {
        this.mButtons = new Button[buttonValues.length];
        int buttonCount = (int)Math.ceil(buttonValues.length / 2);

        // Width and height are calculated for one single button.
        // The calculation is based on case when there are two buttons on one line
        float buttonWidth = (mMetrics[0] - 3*mOffset) / 2.0f;     //width of one button
        float buttonHeight = (mMetrics[1] - (buttonCount + 1)*mOffset);

        int buttonPixelWidth = (int)Math.ceil(buttonWidth * mPixelMetrics[0] / mMetrics[0]);
        int buttonPixelHeight;

        int remainder = buttonValues.length % 2;
        int lineFillCounter = 0;                    //TODO: update in layout improvement


        if (buttonHeight > 0) {
            buttonHeight /= (float)buttonCount;
            buttonPixelHeight = (int)Math.ceil(buttonHeight * mPixelMetrics[1] / mMetrics[1]);

            Vector3f position = new Vector3f(mOffset + (buttonWidth/2.0f), -mOffset - (buttonHeight/2.0f), 0.0f);

            int k = buttonValues.length;
            for ( int i = 0; i < buttonValues.length; i++, k-- ) {
                if ( k == 1 && remainder == 1) {
                    buttonWidth = mMetrics[0] - 2*mOffset;
                    buttonPixelWidth = (int)Math.ceil(buttonWidth * mPixelMetrics[0] / mMetrics[0]);
                }

                position.print("Draw", "CurrentPosition");
                mButtons[i] = new Button(new Vector3f(position), new float[] {buttonWidth, buttonHeight},
                        new int[] {buttonPixelWidth, buttonPixelHeight}, buttonValues[i]);

                lineFillCounter++;

                //if ( (position.x + buttonWidth + mOffset) >= mMetrics[0] ) {
                if ( lineFillCounter == 2 ) {
                    position.x = mOffset + (buttonWidth/2.0f);
                    position.y -= (buttonHeight + mOffset);
                    Log.e("Draw", new StringBuilder().append("Changed").toString());
                    position.print("Draw", "Updated position");
                    lineFillCounter = 0;
                }
                else position.x += (buttonWidth + mOffset);
            }
        }
    }

    public void generateBitmap(Typeface tf) {
        int maxWidth = 0;

        float[] packedData = new float[ mButtons.length * Button.VERTEX_AMOUNT * Button.BUFFER_DATA_SIZE ];

        for ( int i = 0; i < mButtons.length; i++ )if (mButtons[i].getWidth() > maxWidth ) maxWidth = mButtons[i].getPixelWidth();
        int cellHeight = mButtons[0].getPixelHeight();

        int bmpWidth = MathMisc.getClosestPowerOfTwo(maxWidth);
        int bmpHeight = MathMisc.getClosestPowerOfTwo(cellHeight * mButtons.length);

        Paint paint = new Paint();
        paint.setTypeface(tf);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(20);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);

        Paint.FontMetrics fm = paint.getFontMetrics();

        float fontsize = (float)Math.floor((cellHeight / 3) * 20 / (Math.abs(fm.top) + fm.leading));
        paint.setTextSize(fontsize);

        mBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);
        mBitmap.eraseColor( 0xFFFFFFFF );

        int x, y;

        for ( int i = 0; i < mButtons.length; i++ ) {
            int textWidth = (int)paint.measureText(mButtons[i].getText());
            int halfWidth = textWidth/2;


            if (textWidth >= mButtons[i].getPixelWidth()) {
                y = i * cellHeight + cellHeight / 6 + (int)Math.abs(fm.top);

                int lineWidth = 0;
                String[] words = mButtons[i].getText().split(" ");
                StringBuilder line = new StringBuilder();

                for ( int k = 0; k < words.length; k++ ) {
                    lineWidth += (int)paint.measureText(words[k] + " ");
                    line.append(words[k] + " ");

                    if ( lineWidth >= halfWidth ) {
                        x = (mButtons[i].getPixelWidth() - lineWidth)/2;
                        canvas.drawText(line.toString(), x, y, paint);

                        y += Math.abs((int)(fm.top + fm.leading));
                        lineWidth = 0;
                        line = new StringBuilder();
                    }
                }

                x = (mButtons[i].getPixelWidth() - lineWidth)/2;
                canvas.drawText(line.toString(), x, y, paint);

            } else {
                x = mButtons[i].getPixelWidth()/2 - halfWidth;
                y = i * cellHeight + cellHeight / 3 + (int)Math.abs(fm.top);

                canvas.drawText(mButtons[i].getText(), x, y, paint);
            }

            mButtons[i].setTexRgn( new TextureRegion( bmpWidth, bmpHeight, 0, i * cellHeight,
                    mButtons[i].getPixelWidth(), mButtons[i].getPixelHeight() ) );

            float[] buttonData = mButtons[i].getPackedData();
            int stride = i * Button.VERTEX_AMOUNT * Button.BUFFER_DATA_SIZE;
            for ( int k = 0; k < buttonData.length; k++ ) packedData[ stride + k ] = buttonData[k];
        }


        mButtonPackedBuffer = ByteBuffer.allocateDirect(mButtons.length * Button.VERTEX_AMOUNT * Button.BUFFER_DATA_SIZE * (Float.SIZE / 8)).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mButtonPackedBuffer.put(packedData).position(0);
    }

}
