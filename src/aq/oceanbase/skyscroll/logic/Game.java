package aq.oceanbase.skyscroll.logic;

import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.RenderContainer;
import aq.oceanbase.skyscroll.graphics.primitives.Background;
import aq.oceanbase.skyscroll.graphics.windows.Window;
import aq.oceanbase.skyscroll.logic.tree.nodes.Question;
import aq.oceanbase.skyscroll.touch.TouchHandler;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.utils.math.MathMisc;
import aq.oceanbase.skyscroll.utils.math.Vector2f;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

public class Game {

    public static enum MODE {
        TREE, QUESTION
    }

    private MODE mDrawmode = MODE.TREE;
    private boolean mModeSwitched = false;

    private int[] mScreenMetrics;

    public RenderContainer mCurrentRenderables;
    private RenderContainer mTreeRenderables = new RenderContainer();
    private RenderContainer mQuestionRenderables = new RenderContainer();

    //Constraints
    private float mMinHeight = 0.0f;
    private float mMaxHeight = 30.0f;
    private float mMinDist = 8.0f;
    private float mMaxDist = 40.0f;

    //Tree
    public GameSession mGameSession;

    //Windows
    public Window mWindow;

    //Backgrounds
    private Background mCurrentBackground;
    public Background mTreeBackground = new Background(R.drawable.bckgnd1);
    private Background mQuestionBackground = mTreeBackground;


    //Navigation variables
    private float mDistance = 15.0f;         //cam distance from origin
    private float mHeight = 0.0f;

    private Camera mCamera = new Camera(new Vector3f(0.0f, 0.0f, mDistance),
            new Vector3f(0.0f, 0.0f, -1.0f),
            new Vector3f(0.0f, 1.0f, 0.0f));

    //Touch variables
    public TouchHandler mTouchHandler;
    private Vector2f mMomentum = new Vector2f(0.0f, 0.0f);
    private final TouchHandler mTreeTouchHandler = new TouchHandler() {
        @Override
        public void onSwipeHorizontal(float amount) {
            mMomentum.x = amount;
        }

        @Override
        public void onSwipeVertical(float amount) {
            mMomentum.y = amount;
        }

        @Override
        public void onScale(float span) {
            if (Math.abs(span) > 0.1) mDistance = mDistance - span;
            if (mDistance <= mMinDist) mDistance = mMinDist;
            else if (mDistance > mMaxDist) mDistance = mMaxDist;
        }

        @Override
        public void onTap(float x, float y) {
            boolean selected;
            selected = mGameSession.tree.performRaySelection(new TouchRay(x, y, 1.0f, mCamera, mScreenMetrics));
            //Log.e("Draw", new StringBuilder().append("Pos: ").append(x).append(" ").append(y).toString());
            if (selected) switchMode(MODE.QUESTION);
        }
    };

    private final TouchHandler mWindowTouchHandler = new TouchHandler() {
        @Override
        public void onSwipeVertical(float amount) {
            mWindow.scrollContent((int)(-amount * 40));
        }

        @Override
        public void onTap(float x, float y) {
            //switchMode(MODE.TREE);
            if (mWindow.pressButton((int) x, (int) y, mCamera, mScreenMetrics)) switchMode(MODE.TREE);
        }
    };


    public Game() {
        mGameSession = new GameSession();
        //mCurrentBackground = mTreeBackground;

        mTreeRenderables.addRenderable(mTreeBackground).addRenderable(mGameSession.tree);
        setMode(MODE.TREE);
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void setCamera(Camera cam) {
        this.mCamera = cam;
    }

    public float getHeight() {
        return this.mHeight;
    }

    public void setHeight(float height) {
        if (height < mMinHeight) this.mHeight = mMinHeight;
        else if (height > mMaxHeight) this.mHeight = mMaxHeight;
        else this.mHeight = height;
    }

    public void setScreenMetrics(int[] screenMetrics) {
        this.mScreenMetrics = screenMetrics;
    }

    public RenderContainer getRenderables() {
        return mCurrentRenderables;
    }


    //<editor-fold desc="Updaters">
    private void updateAngle() {
        mGameSession.tree.updateAngle(mMomentum.x);
    }

    private void updateHeight() {
        mHeight = mHeight + mMomentum.y;
        if (mHeight > mMaxHeight) mHeight = mMaxHeight;
        if (mHeight < mMinHeight) mHeight = mMinHeight;
    }

    private void updateMomentum() {
        if (mMomentum.x != 0.0f) {
            mMomentum.x = MathMisc.decrementConvergingValue(mMomentum.x, 1.7f);
        }

        if (mMomentum.y != 0.0f) {
            mMomentum.y = MathMisc.decrementConvergingValue(mMomentum.y, 0.1f);
        }
    }

    private void updateCameraPosition() {
        Vector3f updPos = mCamera.getPos();
        updPos.y = mHeight;
        updPos.z = mDistance;

        Vector3f updDir = mCamera.getDir();
        updDir.y = mHeight;

        mCamera.setPos(updPos);
        mCamera.setDir(updDir);
        mCamera.updateCamera();
    }
    //</editor-fold>


    private Question getQuestion(int id) {
        String text = "Alright then, picture this if you will:\n" +
                "10 to 2 AM, X, Yogi DMT, and a box of Krispy Kremes, in my \"need to know\" post, just outside of Area 51.\n" +
                "Contemplating the whole \"chosen people\" thing with just a flaming stealth banana split the sky like one would hope but never really expect to see in a place like this.\n" +
                "Cutting right angle donuts on a dime and stopping right at my Birkenstocks, and me yelping...\n" +
                "Holy fucking shit!\n" +
                "\n" +
                "Then the X-Files being, looking like some kind of blue-green Jackie Chan with Isabella Rossellini lips and breath that reeked of vanilla Chig Champa,\n" +
                "did a slow-mo Matrix descent out of the butt end of the banana vessel and hovered above my bug-eyes, my gaping jaw, and my sweaty L. Ron Hubbard upper lip and all I could think was: \"I hope Uncle Martin here doesn't notice that I pissed my fuckin' pants.\"\n" +
                "\n" +
                "So light in his way,\n" +
                "Like an apparition,\n" +
                "He had me crying out,\n" +
                "\"Fuck me,\n" +
                "It's gotta be,\n" +
                "Deadhead Chemistry,\n" +
                "The blotter got right on top of me,\n" +
                "Got me seein' E-motherfuckin'-T!\"\n" +
                "\n" +
                "And after calming me down with some orange slices and some fetal spooning, E.T. revealed to me his singular purpose.\n" +
                "He said, \"You are the Chosen One, the One who will deliver the message. A message of hope for those who choose to hear it and a warning for those who do not.\"\n" +
                "Me. The Chosen One?\n" +
                "They chose me!!!\n" +
                "And I didn't even graduate from fuckin' high school.";

        String[] buttons = new String[] {"Tool", "Queen", "The Cult", "Primal Scream"};

        return new Question(text, buttons, 0);
    }



    private void createWindow(Question question) {
        mQuestionRenderables.clear();
        mWindow = new Window(20, 2.0f, mCamera, mScreenMetrics);
        mWindow.setQuestion(question);
        mQuestionRenderables.addRenderable(mTreeBackground).addRenderable(mWindow);
    }

    private void killWindow() {
        mQuestionRenderables.clear();
    }


    private void setMode(MODE mode) {
        switch (mode) {
            case TREE:
                mCurrentRenderables = mTreeRenderables;
                mTouchHandler = mTreeTouchHandler;
                break;
            case QUESTION:
                mCurrentRenderables = mQuestionRenderables;
                mTouchHandler = mWindowTouchHandler;
                break;
        }

        mDrawmode = mode;
    }

    private void switchMode (MODE mode) {
        if (mode == MODE.QUESTION)
            createWindow(this.getQuestion(1));
        else {
            killWindow();
            mWindow = null;
        }


        setMode(mode);
    }

    public void update() {
        if (mMomentum.nonZero()) {
            updateHeight();
            updateAngle();
            updateMomentum();
        }

        updateCameraPosition();
    }

}
