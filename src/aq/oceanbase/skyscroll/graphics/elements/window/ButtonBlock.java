package aq.oceanbase.skyscroll.graphics.elements.window;

import android.graphics.*;
import android.util.Log;
import aq.oceanbase.skyscroll.graphics.TextureRegion;
import aq.oceanbase.skyscroll.graphics.elements.window.Button;
import aq.oceanbase.skyscroll.utils.math.MathMisc;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

public class ButtonBlock {
    private Button[] mButtons;

    private int mHighlighted = -1;
    private int mPressedButton = -1;

    private Vector3f mPos;

    private float mOffset;

    private Bitmap mBitmap;

    private float[] mMetrics;
    private int[] mPixelMetrics;

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

    public void setPressedButtonState(Button.STATE state) {
        if (mPressedButton != -1 && mPressedButton >= 0 && mPressedButton < mButtons.length) {
            mButtons[mPressedButton].setState(state);
        }
    }

    public Button.STATE getPressedButtonState() {
        if (mPressedButton != -1 && mPressedButton >= 0 && mPressedButton < mButtons.length)
            return mButtons[mPressedButton].getState();
        else
            return Button.STATE.NEUTRAL;
    }

    public boolean isButtonHighlighted() {
        if (mHighlighted != -1) return true;
        return false;
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

                //position.print("Draw", "CurrentPosition");
                mButtons[i] = new Button(new Vector3f(position), new float[] {buttonWidth, buttonHeight},
                        new int[] {buttonPixelWidth, buttonPixelHeight}, buttonValues[i]);

                lineFillCounter++;

                //if ( (position.x + buttonWidth + mOffset) >= mMetrics[0] ) {
                if ( lineFillCounter == 2 ) {
                    position.x = mOffset + (buttonWidth/2.0f);
                    position.y -= (buttonHeight + mOffset);
                    //Log.e("Draw", new StringBuilder().append("Changed").toString());
                    //position.print("Draw", "Updated position");
                    lineFillCounter = 0;
                }
                else position.x += (buttonWidth + mOffset);
            }
        }
    }

    public void generateBitmap(Typeface tf) {
        int maxWidth = 0;

        for ( int i = 0; i < mButtons.length; i++ )if (mButtons[i].getWidth() > maxWidth ) maxWidth = mButtons[i].getPixelWidth();
        int cellHeight = mButtons[0].getPixelHeight();

        int bmpWidth = MathMisc.getClosestPowerOfTwo(maxWidth);
        int bmpHeight = MathMisc.getClosestPowerOfTwo(cellHeight * mButtons.length);

        Paint paint = new Paint();
        paint.setTypeface(tf);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(28);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);

        Paint.FontMetrics fm = paint.getFontMetrics();

        //float fontsize = (float)Math.floor((cellHeight / 3) * 20 / (Math.abs(fm.top) + fm.leading));
        //paint.setTextSize(fontsize);

        mBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);
        mBitmap.eraseColor( 0x30FFFFFF );

        int x, y;

        for ( int i = 0; i < mButtons.length; i++ ) {
            int textWidth = (int)paint.measureText(mButtons[i].getText());
            int halfWidth = textWidth/2;

            //Log.e("Draw", new StringBuilder("Text: ").append(textWidth).append(" Button: ").append(mButtons[i].getPixelWidth()).toString());


            if (textWidth >= mButtons[i].getPixelWidth()) {
                //y = i * cellHeight + cellHeight / 6 + (int)Math.abs(fm.top);
                y = i * cellHeight + ( cellHeight/2 );

                int lineWidth = 0;
                int wordWidth = 0;
                String[] words = mButtons[i].getText().split(" ");
                StringBuilder line = new StringBuilder();

                for ( int k = 0; k < words.length; k++ ) {
                    wordWidth = (int)paint.measureText(words[k] + " ");

                    if ( lineWidth + wordWidth >= halfWidth ) {
                        x = (mButtons[i].getPixelWidth() - lineWidth)/2;
                        canvas.drawText(line.toString(), x, y, paint);

                        y += (int)(Math.abs(fm.top) + fm.bottom);
                        lineWidth = 0;
                        line = new StringBuilder();
                    }

                    line.append(words[k] + " ");
                    lineWidth += wordWidth;
                }

                x = (mButtons[i].getPixelWidth() - lineWidth)/2;
                canvas.drawText(line.toString(), x, y, paint);

            } else {
                x = mButtons[i].getPixelWidth()/2 - halfWidth;
                y = i * cellHeight + ( cellHeight/2 ) + (int)(Math.abs(fm.top) + fm.leading)/2;

                canvas.drawText(mButtons[i].getText(), x, y, paint);
            }

            mButtons[i].setTexRgn( new TextureRegion( bmpWidth, bmpHeight, 0, i * cellHeight,
                    mButtons[i].getPixelWidth(), mButtons[i].getPixelHeight() ) );

        }

    }


    public int findPressedButton(float touchX, float touchY) {
        Vector3f pos;
        float x, y;
        float[] metrics;
        for ( int i = 0; i < mButtons.length; i++ ) {
            pos = mButtons[i].getPos();
            metrics = mButtons[i].getMetrics();

            //pos.print("Draw", "Pos");
            x = touchX - (pos.x - metrics[0]/2);
            y = touchY - (pos.y - metrics[1]/2);

            if ( x >= 0 && x <= metrics[0]) {
                if ( y >= 0 && y <= metrics[1]) {
                    mPressedButton = i;
                    return i;
                }
            }
        }

        return -1;
    }


    public void highlightButton(int id, Button.STATE state) {
        if (mHighlighted != -1) {
            mButtons[mHighlighted].setState(Button.STATE.NEUTRAL);
        }

        mButtons[id].setState(state);
        mHighlighted = id;
    }

    public void blink(int buttonId, float[] color1, float[] color2) {
        float[] buttonColor = mButtons[buttonId].getColor();
        if (buttonColor[0] == color1[0] && buttonColor[1] == color1[1] && buttonColor[2] == color1[2] && buttonColor[3] == color1[3]) {
            mButtons[buttonId].setColor(color2);
        } else mButtons[buttonId].setColor(color1);
        Log.e("Debug", new StringBuilder().append("BLINKED").toString());
    }

    public void blink(int buttonId, float[] color) {
        this.blink(buttonId, color, new float[] {1.0f, 1.0f, 1.0f, 1.0f});
    }

    public void blink() {
        if (mHighlighted != -1) {
            float[] color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
            if (mButtons[mHighlighted].getState() == Button.STATE.CORRECT) color = new float[] {0.0f, 1.0f, 0.0f, 1.0f};
            if (mButtons[mHighlighted].getState() == Button.STATE.WRONG) color = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
            this.blink(mHighlighted, color, new float[] {1.0f, 1.0f, 1.0f, 1.0f});
        }
    }
}
