package aq.oceanbase.skyscroll.graphics.elements.window.blocks;

import android.content.Context;
import android.graphics.*;
import android.opengl.Matrix;
import android.opengl.GLES20;
import android.util.Log;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.elements.SpriteBatch;
import aq.oceanbase.skyscroll.graphics.TextureRegion;
import aq.oceanbase.skyscroll.graphics.elements.window.Window;
import aq.oceanbase.skyscroll.graphics.elements.window.WindowBlock;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.utils.loaders.TextureLoader;
import aq.oceanbase.skyscroll.utils.math.MathUtilities;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

public class ButtonBlock extends WindowBlock {

    private int mTextureHandle;

    private Button[] mButtons;
    private String[] mButtonValues;
    private SpriteBatch mButtonBatch;

    private int mHighlighted = -1;
    private int mPressedButton = -1;

    private float mOffset = 0.0f;
    private float mInterval = 0.1f;

    private Bitmap mBitmap;

    public ButtonBlock(Window root, float fraction) {
        super(root, fraction);
        mOffset = 0.1f;
    }

    public ButtonBlock(Window root, float fraction, String[] buttonValues, float interval, float offset) {
        super(root, fraction);
        this.mInterval = interval;
        this.mOffset = offset;
        mButtonValues = buttonValues;
    }

    public ButtonBlock(Window root, float fraction, Button[] buttons, float interval, float offset) {
        super(root, fraction);
        mButtons = buttons;
        this.mInterval = interval;
        this.mOffset = offset;
    }

    //<editor-fold desc="Getters and Setters">
    public ButtonBlock setOffset(float offset) {
        this.mOffset = offset;
        return this;
    }

    public ButtonBlock setAnswers(String[] answers) {
        this.computeButtonsMetrics(answers);
        return this;
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
    //</editor-fold>


    //<editor-fold desc="Metrics and Texture">
    private void computeButtonsMetrics(String[] buttonValues) {
        this.mButtons = new Button[buttonValues.length];
        int buttonCount = (int)Math.ceil(buttonValues.length / 2);

        // Width and height are calculated for one single button.
        // The calculation is based on case when there are two buttons on one line
        float buttonWidth = (mWidth - 2*mOffset - mInterval) / 2.0f;     //width of one button
        float buttonHeight = (mHeight - ((buttonCount - 1)*mInterval) + 2*mOffset);

        int buttonPixelWidth = (int)Math.ceil(buttonWidth * mPixelMetrics[0] / mWidth);
        int buttonPixelHeight;

        int remainder = buttonValues.length % 2;
        int lineFillCounter = 0;                    //TODO: update in layout improvement


        if (buttonHeight > 0) {
            buttonHeight /= (float)buttonCount;
            buttonPixelHeight = (int)Math.ceil(buttonHeight * mPixelMetrics[1] / mHeight);

            Vector3f position = new Vector3f(mOffset + (buttonWidth/2.0f), -mOffset - (buttonHeight/2.0f), 0.0f);

            int k = buttonValues.length;
            for ( int i = 0; i < buttonValues.length; i++, k-- ) {
                if ( k == 1 && remainder == 1) {
                    buttonWidth = mWidth - 2*mOffset;
                    buttonPixelWidth = (int)Math.ceil(buttonWidth * mPixelMetrics[0] / mWidth);
                }

                mButtons[i] = new Button(new Vector3f(position), new float[] {buttonWidth, buttonHeight},
                        new int[] {buttonPixelWidth, buttonPixelHeight}, buttonValues[i]);

                lineFillCounter++;

                if ( lineFillCounter == 2 ) {
                    position.x = mOffset + (buttonWidth/2.0f);
                    position.y -= (buttonHeight + mInterval);
                    lineFillCounter = 0;
                }
                else position.x += (buttonWidth + mInterval);
            }
        }
    }

    public void generateBitmap(Typeface tf) {
        int maxWidth = 0;

        for ( int i = 0; i < mButtons.length; i++ )if (mButtons[i].getWidth() > maxWidth ) maxWidth = mButtons[i].getPixelWidth();
        int cellHeight = mButtons[0].getPixelHeight();

        int bmpWidth = MathUtilities.getClosestPowerOfTwo(maxWidth);
        int bmpHeight = MathUtilities.getClosestPowerOfTwo(cellHeight * mButtons.length);

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
    //</editor-fold>


    //<editor-fold desc="Button highlighting">
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
    //</editor-fold>

    @Override
    protected void onMetricsSet() {
        this.computeButtonsMetrics(mButtonValues);
    }

    @Override
    public void onTap(float x, float y) {
        Log.e("Touch", "BlockTap: " + x + " " + y);
        float curX, curY;
        Vector3f pos;
        float[] metrics;
        for ( int i = 0; i < mButtons.length; i++ ) {
            pos = mButtons[i].getPos().addV(mPos);
            metrics = mButtons[i].getMetrics();

            //pos.print("Draw", "Pos");
            curX = x - (pos.x - metrics[0]/2);
            curY = y - (pos.y - metrics[1]/2);

            if ( curX >= 0 && curX <= metrics[0]) {
                if ( curY >= 0 && curY <= metrics[1]) {
                    mPressedButton = i;
                    mRoot.onButtonPressed(this, i);
                }
            }
        }
    }


    @Override
    public void initialize(Context context, ProgramManager programManager) {
        super.initialize(context, programManager);
        this.generateBitmap(mRoot.getTypeface());

        //TODO: check if I really need that handle as a class member
        mTextureHandle = TextureLoader.loadTexture(mBitmap);

        mButtonBatch = new SpriteBatch(SpriteBatch.COLORED_VERTEX_3D, mTextureHandle);
        mButtonBatch.initialize(context, programManager);
    }

    @Override
    public void release() {
        super.release();
        GLES20.glDeleteTextures(1, new int[] {mTextureHandle}, 0);
        mButtonBatch.release();
    }

    @Override
    public void draw(Camera cam) {
        float[] buttonMatrix = new float[16];
        float[] blockMatrix = new float[16];

        Matrix.setIdentityM(blockMatrix, 0);
        Matrix.translateM(blockMatrix, 0, mRoot.getModelMatrix(), 0, mPos.x, mPos.y, mPos.z);

        //GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mButtonBatch.beginBatch(cam);

        /*if (mButtonBlock.isButtonHighlighted()) {
            Time now = new Time();
            now.setToNow();
            if (now.toMillis(false) - mTimer > mButtonBlinkPeriod) {
                mButtonBlock.blink();
                Log.e("Debug", new StringBuilder("Times: ").append(now.toMillis(false)).append(" ").append(mTimer).toString());
                mTimer = now.toMillis(false);
            }
            now.clear("any");

        }*/

        for ( int i = 0; i < this.mButtons.length; i++ ) {

            Vector3f pos = new Vector3f(mButtons[i].getPos());

            float[] metrics = mButtons[i].getMetrics();

            float[] color = mButtons[i].getColor();

            Matrix.setIdentityM(buttonMatrix, 0);
            Matrix.translateM(buttonMatrix, 0, blockMatrix, 0, pos.x, pos.y, pos.z);

            mButtonBatch.batchElement(metrics[0], metrics[1], color, mButtons[i].getTexRgn(), buttonMatrix);
        }

        mButtonBatch.endBatch();
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
}
