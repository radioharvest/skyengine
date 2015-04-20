package aq.oceanbase.skyscroll.engine.logic.window.blocks;

import android.content.Context;
import android.graphics.*;
import android.opengl.Matrix;
import android.util.Log;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.graphics.batches.SpriteBatch;
import aq.oceanbase.skyscroll.engine.graphics.TextureRegion;
import aq.oceanbase.skyscroll.engine.logic.window.Window;
import aq.oceanbase.skyscroll.engine.logic.window.WindowBlock;
import aq.oceanbase.skyscroll.engine.graphics.ProgramManager;
import aq.oceanbase.skyscroll.engine.logic.events.ButtonEvent;
import aq.oceanbase.skyscroll.engine.logic.events.ButtonEventListener;
import aq.oceanbase.skyscroll.game.events.QuestionEvent;
import aq.oceanbase.skyscroll.engine.utils.loaders.TextureLoader;
import aq.oceanbase.skyscroll.engine.utils.math.MathUtilities;
import aq.oceanbase.skyscroll.engine.utils.math.Vector2f;
import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

//TODO: add button block scrolling
public class ButtonBlock extends WindowBlock implements ButtonEventListener {

    public static enum BUTTONLAYOUT {
        HORIZONTAL, VERTICAL, GRID
    }

    private int mTextureHandle;

    private List<Button> mButtons = new ArrayList<Button>();
    private SpriteBatch mButtonBatch;

    private BUTTONLAYOUT mButtonLayout = BUTTONLAYOUT.GRID;

    private int mHighlighted = -1;
    private int mPressedButton = -1;

    private int[] mGridSettings = new int[] {-1, -1};        // columns and lines of the grid table

    private float mOffset = 0.0f;
    private float mInterval = 0.1f;

    private Bitmap mBitmap;

    private boolean mAlignUnevenToCenter = false;

    public ButtonBlock(Window root, float fraction) {
        super(root, fraction);
        mOffset = 0.1f;
    }

    public ButtonBlock(Window root, float fraction, float interval, float offset, BUTTONLAYOUT layout) {
        super(root, fraction);
        this.mInterval = interval;
        this.mOffset = offset;
        this.mButtonLayout = layout;

        this.setupGrid();
    }

    public ButtonBlock(Window root, float fraction, Button[] buttons, float interval, float offset, BUTTONLAYOUT layout) {
        this(root, fraction, interval, offset, layout);

        for (Button newButton: buttons) {
            newButton.addButtonEventListener(this);
            mButtons.add(newButton);
        }
    }

    //<editor-fold desc="Getters and Setters">
    public ButtonBlock setOffset(float offset) {
        this.mOffset = offset;
        return this;
    }

    public ButtonBlock setAnswers(String[] answers) {
        this.computeButtonsMetrics();
        return this;
    }

    public void addButton(Button button) {
        button.addButtonEventListener(this);
        button.setId(mButtons.size());
        this.mButtons.add(button);
        Log.e("Debug", "Button added");
    }

    public int getButtonsAmount() {
        return this.mButtons.size();
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public Button getButton(int i) {
        if (i <= mButtons.size() - 1) return mButtons.get(i);
        else return null;
    }

    public void setPressedButtonState(Button.STATE state) {
        if (mPressedButton != -1 && mPressedButton >= 0 && mPressedButton < mButtons.size()) {
            mButtons.get(mPressedButton).setState(state);
        }
    }

    public void setAlignUnevenToCenter(boolean value) {
        this.mAlignUnevenToCenter = value;
    }

    public Button.STATE getPressedButtonState() {
        if (mPressedButton != -1 && mPressedButton >= 0 && mPressedButton < mButtons.size())
            return mButtons.get(mPressedButton).getState();
        else
            return Button.STATE.NEUTRAL;
    }

    public boolean isButtonHighlighted() {
        if (mHighlighted != -1) return true;
        return false;
    }
    //</editor-fold>


    //<editor-fold desc="Metrics and Texture">
    private void setupGrid() {
        Log.e("Debug", "Buttonlayout: " + mButtonLayout);
        switch (mButtonLayout) {
            case VERTICAL:
                mGridSettings[0] = 1;
                mGridSettings[1] = -1;
                break;
            case HORIZONTAL:
                mGridSettings[0] = -1;
                mGridSettings[1] = 1;
                break;
            case GRID:                              // by default set to 2 columns
                mGridSettings[0] = 2;
                mGridSettings[1] = -1;
                break;
        }
    }

    private void computeButtonsMetrics() {
        Log.e("Debug", "Computing button metrics");
        if ( this.mButtons.size() <= 0 )
            return;

        if (mGridSettings[0] == -1 && mGridSettings[1] == -1)       // to prevent incorrect input recalc grid on broken
            setupGrid();


        if (mGridSettings[0] == -1) {
            mGridSettings[0] = (int) Math.ceil( mButtons.size() / mGridSettings[1] );
        }

        if (mGridSettings[1] == -1) {
            mGridSettings[1] = (int) Math.ceil( mButtons.size() / mGridSettings[0] );
        }

        Log.e("Debug", "GRID SETTINGS: " + mGridSettings[0] + " " + mGridSettings[1]);
        float buttonWidth = ( (1.0f - 2*mOffset ) * mWidth - ( (mGridSettings[0] - 1)*mInterval ) ) / mGridSettings[0] ;
        float buttonHeight = (1.0f - 2*mOffset - (mGridSettings[1] - 1)*mInterval ) * mHeight / mGridSettings[1];
        Log.e("Debug", "Button settings: offset: " + mOffset + " interval: " + mInterval + " button width: " + buttonWidth + " block width: " + mWidth);
        Log.e("Debug", "strangestuff: " + (mGridSettings[1] - 1) + " | " + (mGridSettings[1] - 1)*mInterval);

        // Width and height are calculated for one single button.
        // The calculation is based on case when there are two buttons on one line
        int buttonPixelWidth = (int)Math.ceil(buttonWidth * mPixelMetrics[0] / mWidth);
        int buttonPixelHeight = (int)Math.ceil(buttonHeight * mPixelMetrics[1] / mHeight);

        Vector3f origin = new Vector3f(mOffset + (buttonWidth/2.0f), -mOffset - (buttonHeight/2.0f), 0.0f);
        Vector3f lineStep = new Vector3f(0.0f, -(buttonHeight + mInterval), 0.0f);
        Vector3f columnStep = new Vector3f(buttonWidth + mInterval, 0.0f, 0.0f);
        Vector3f current;

        int index = 0;
        float[] metrics = new float[] {buttonWidth, buttonHeight};
        int[] pixelMetrics = new int[] {buttonPixelWidth, buttonPixelHeight};
        for ( int i = 0; i < mGridSettings[1]; i++ ) {
            current = origin.addV(lineStep.multiplySf(i));

            for (int k = 0; k < mGridSettings[0]; k++ ) {
                if (index >= mButtons.size())
                    break;

                current.print("Debug", "Current");
                mButtons.get(index).setMetrics( new Vector3f(current), metrics, pixelMetrics );
                mButtons.get(index).getPos().print("Debug", "WHADDAFUCK");
                this.getPos().print("Debug", "THATDAFUCK");
                this.mRoot.getPosition().print("Debug", "ROOTDAFUCK");
                columnStep.print("Debug", "ColumnStep");
                current = current.addV(columnStep);

                index++;
            }
        }
    }


    // this function builds button texture atlas
    // since all the buttons are of a fixed size
    // it is easier to calculate bmp size using
    // the grid concept. based on button metrics
    // texture grid is calculated and then used
    // to calculate the bitmap resolution and
    // texture regions for the buttons
    public void generateBitmap(Typeface tf) {
        //int maxWidth = 0;

        /*for ( int i = 0; i < mButtons.size(); i++ )
            if ( mButtons.get(i).getWidth() > maxWidth ) maxWidth = mButtons.get(i).getPixelWidth();*/

        if (mButtons.isEmpty())
            return;

        int buttonWidth = mButtons.get(0).getPixelWidth();
        int buttonHeight = mButtons.get(0).getPixelHeight();

        int textureGrid[] = computeTextureGrid(buttonWidth, buttonHeight, mButtons.size());

        int bmpWidth = MathUtilities.getClosestPowerOfTwo(buttonWidth * textureGrid[0]);
        int bmpHeight = MathUtilities.getClosestPowerOfTwo(buttonHeight * textureGrid[1]);

        Paint paint = new Paint();
        paint.setTypeface(tf);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);

        Paint.FontMetrics fm = paint.getFontMetrics();

        int lineHeight = (int)(Math.abs(fm.top) + fm.bottom);

        mBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);

        mBitmap.eraseColor( 0x30FFFFFF );

        Vector2f origin = new Vector2f( buttonWidth/2, buttonHeight/2 + fm.bottom );
        Vector2f originTexRgn = new Vector2f(0, 0);
        Vector2f lineStep = new Vector2f( 0, buttonHeight );
        Vector2f columnStep = new Vector2f( buttonWidth, 0 );

        Vector2f current = new Vector2f(origin);
        Vector2f currentTexRgn = new Vector2f(originTexRgn);
        Vector2f local;

        int lineCounter = 0;
        int columnCounter = 0;

        for (int i = 0; i < mButtons.size(); i++) {
            //draw
            List<String> textLines = breakTextToLines(paint, lineHeight, mButtons.get(i).getText(), new int[] {buttonWidth, buttonHeight});

            if (textLines.size() == 0) {
                return;
            }

            if ( textLines.size() > 1 ) {
                local = new Vector2f(current);
                local.y -= lineHeight * ( textLines.size() - 1 ) / 2;
                for (int k = 0; k < textLines.size(); k++) {
                    canvas.drawText( textLines.get(k), local.x, local.y, paint );
                    local.y += lineHeight;
                }
            } else {
                canvas.drawText( textLines.get(0), current.x, current.y, paint );
            }

            mButtons.get(i).setTexRgn(new TextureRegion(bmpWidth, bmpHeight, currentTexRgn.x, currentTexRgn.y, buttonWidth, buttonHeight));

            columnCounter++;

            if (columnCounter >= textureGrid[0] - 1) {
                lineCounter++;
                columnCounter = 0;
                current = origin.addV( lineStep.multiplySf(lineCounter) );
                currentTexRgn = originTexRgn.addV( lineStep.multiplySf(lineCounter) );
            } else {
                current = current.addV(columnStep);
                currentTexRgn = currentTexRgn.addV(columnStep);
            }
        }


    }

    private List<String> breakTextToLines(Paint paint, int lineHeight, String text, int[] buttonMetrics) {
        int totalTextWidth = (int)paint.measureText( text );
        List<String> lines = new ArrayList<>();
        String[] words;
        StringBuilder currentLine = new StringBuilder();

        if ( totalTextWidth < buttonMetrics[0] ) {
            lines.add(text);
            return lines;
        }

        words = text.split(" ");

        int lineWidth = 0;
        currentLine.append(words[0] + " ");
        for (int i = 1; i < words.length; i++) {
            float wordWidth = paint.measureText(words[i] + " ");

            if ( lineWidth + wordWidth >= buttonMetrics[0] ) {

                lines.add(currentLine.toString());

                lineWidth = 0;
                currentLine.delete(0, currentLine.length() - 1);

                if ( (lines.size() + 1)*lineHeight > buttonMetrics[1] )
                    break;
            }

            lineWidth += wordWidth;
            currentLine.append( words[i] + " " );
        }

        return lines;
    }

    private int[] computeTextureGrid(int buttonWidth, int buttonHeight, int buttonsAmount) {
        int grid[] = new int[] {-1, -1};

        if (buttonWidth >= buttonHeight) {
            grid[0] = 1;
            grid[1] = buttonsAmount;
            while (true) {
                if ( ( buttonHeight * grid[1] ) > buttonWidth * (grid[0] + 1) ) {     // if more, than
                    grid[0]++;                                                        // possible variant
                    grid[1] = (int) Math.ceil(buttonsAmount / grid[0]);               // then rescale
                }
                else
                    break;
            }
        } else if (buttonHeight > buttonWidth) {
            grid[0] = buttonsAmount;
            grid[1] = 1;
            while (true) {
                if ( ( buttonWidth * grid[0] ) > buttonHeight * (grid[1] + 1) ) {     // if more, than
                    grid[1]++;                                                        // possible variant
                    grid[0] = (int) Math.ceil(buttonsAmount / grid[1]);               // then rescale
                }
                else
                    break;
            }
        }

        return grid;
    }
    //</editor-fold>


    //<editor-fold desc="Button highlighting">
    public void highlightButton(int id, Button.STATE state) {
        if (mHighlighted != -1) {
            mButtons.get(mHighlighted).setState(Button.STATE.NEUTRAL);
        }

        mButtons.get(id).setState(state);
        mHighlighted = id;
    }

    public void blink(int buttonId, float[] color1, float[] color2) {
        float[] buttonColor = mButtons.get(buttonId).getColor();
        if (buttonColor[0] == color1[0] && buttonColor[1] == color1[1] && buttonColor[2] == color1[2] && buttonColor[3] == color1[3]) {
            mButtons.get(buttonId).setColor(color2);
        } else
            mButtons.get(buttonId).setColor(color1);
        Log.e("Debug", new StringBuilder().append("BLINKED").toString());
    }

    public void blink(int buttonId, float[] color) {
        this.blink(buttonId, color, new float[] {1.0f, 1.0f, 1.0f, 1.0f});
    }

    public void blink() {
        if (mHighlighted != -1) {
            float[] color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
            if (mButtons.get(mHighlighted).getState() == Button.STATE.CORRECT) color = new float[] {0.0f, 1.0f, 0.0f, 1.0f};
            if (mButtons.get(mHighlighted).getState() == Button.STATE.WRONG) color = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
            this.blink(mHighlighted, color, new float[] {1.0f, 1.0f, 1.0f, 1.0f});
        }
    }
    //</editor-fold>

    @Override
    protected void onMetricsSet() {
        //mInterval = mInterval * mWidth;
        this.computeButtonsMetrics();
    }

    @Override
    public void onTap(float x, float y) {
        Log.e("Touch", "BlockTap: " + x + " " + y);
        /*float curX, curY;
        Vector3f pos;
        float[] metrics;*/
        for (Button button : mButtons) {
            button.onTap(x - mPos.x, y - mPos.y);
        }
    }


    @Override
    public void onButtonPressed(ButtonEvent e) {
        this.mPressedButton = e.getButtonId();
    }

    @Override
    public void onAnswer(QuestionEvent e) {
        if (mPressedButton != -1) {
            if (e.isAnsweredCorrectly())
                highlightButton(mPressedButton, Button.STATE.CORRECT);
            else
                highlightButton(mPressedButton, Button.STATE.WRONG);
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
        //GLES20.glDeleteTextures(1, new int[] {mTextureHandle}, 0);
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

        for ( int i = 0; i < this.mButtons.size(); i++ ) {

            Vector3f pos = new Vector3f(mButtons.get(i).getPos());

            float[] metrics = mButtons.get(i).getMetrics();

            float[] color = mButtons.get(i).getColor();

            Matrix.setIdentityM(buttonMatrix, 0);
            Matrix.translateM(buttonMatrix, 0, blockMatrix, 0, pos.x, pos.y, pos.z);

            mButtonBatch.batchElement(metrics[0], metrics[1], color, mButtons.get(i).getTexRgn(), buttonMatrix);
        }

        mButtonBatch.endBatch();
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
}
