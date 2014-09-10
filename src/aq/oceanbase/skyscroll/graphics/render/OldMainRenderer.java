package aq.oceanbase.skyscroll.graphics.render;

import android.content.Context;
import android.opengl.*;
import android.util.Log;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.ProgramManager;
import aq.oceanbase.skyscroll.graphics.primitives.Background;
import aq.oceanbase.skyscroll.graphics.windows.Window;
import aq.oceanbase.skyscroll.utils.math.MathMisc;
import aq.oceanbase.skyscroll.utils.math.Vector2f;
import aq.oceanbase.skyscroll.utils.math.Vector3f;
import aq.oceanbase.skyscroll.touch.TouchHandler;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.logic.tree.Tree;
import aq.oceanbase.skyscroll.logic.tree.nodes.Question;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.util.Date;


//TODO: check scope of all variables
public class OldMainRenderer implements GLSurfaceView.Renderer {

    public static enum MODE {
        TREE, QUESTION
    }

    private final Context mContext;

    //Constants and sizes
    //TODO: read about statics. maybe it's a bad idea to do sizes static
    public static final int mBytesPerFloat = 4;
    private int mScreenWidth;
    private int mScreenHeight;
    private int[] mScreenMetrics;

    private boolean mResolutionChanged = false;
    private MODE mDrawmode = MODE.TREE;
    private boolean mModeSwitched = false;

    private final String mShaderFolder;
    private ProgramManager mProgramManager;


    //Rendering settings
    private final float mNearPlane = 1.0f;
    private final float mFarPlane = 40.0f;

    //Constraints
    private float mMinHeight = 0.0f;
    private float mMaxHeight = 30.0f;
    private float mMinDist = 8.0f;
    private float mMaxDist = mFarPlane;

    //Navigation variables
    private float mDistance = 15.0f;         //cam distance from origin
    private float mHeight = 0.0f;

    //Camera
    private Camera mCamera = new Camera(new Vector3f(0.0f, 0.0f, mDistance),
            new Vector3f(0.0f, 0.0f, -1.0f),
            new Vector3f(0.0f, 1.0f, 0.0f));

    //FPS Counter
    private int mFrameCounter;
    private long mTime;

    //Tree
    private Tree mTree;

    //Windows
    private Window mWindow;

    //Backgrounds
    private Background mCurrentBackground;
    private Background mTreeBackground = new Background(R.drawable.bckgnd1);

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
            selected = mTree.performRaySelection(new TouchRay(x, y, 1.0f, mCamera, mScreenMetrics));
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


    public OldMainRenderer(Context context) {
        mContext = context;

        Log.e("RunDebug", "Renderer constructor stage passed");

        mTree = new Tree();

        mCurrentBackground = mTreeBackground;
        mTouchHandler = mTreeTouchHandler;

        mShaderFolder = "/aq/oceanbase/skyscroll/graphics/render/shaders";
        mProgramManager = new ProgramManager(mShaderFolder);
    }


    //<editor-fold desc="Getters and Setters">
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
    //</editor-fold>


    //<editor-fold desc="Updaters">
    private void updateAngle() {
        mTree.updateAngle(mMomentum.x);
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

    private void update() {
        if (mMomentum.nonZero()) {
            updateHeight();
            updateAngle();
            updateMomentum();
        }

        updateCameraPosition();
    }
    //</editor-fold>


    public TouchRay castTouchRay(float touchX, float touchY) {
        float[] result = new float[4];
        int[] view = {0, 0, mScreenWidth, mScreenHeight};

        float winX = touchX;
        float winY = (float)mScreenHeight - touchY;

        GLU.gluUnProject(winX, winY, 1.0f, mCamera.getViewM(), 0, mCamera.getProjM(), 0, view, 0, result, 0);     //get point on the far plane
        Vector3f far = new Vector3f( result[0]/result[3], result[1]/result[3], result[2]/result[3]);    //divide by w-component

        return new TouchRay(mCamera.getPos(), far, 1.0f);
    }

    private void countFPS() {
        long currentTime = new Date().getTime();
        if (currentTime - mTime >= 1000) {
            Log.e("RunDebug", new StringBuilder().append("FPS: ").append(mFrameCounter).toString());
            mFrameCounter = 0;
            mTime = currentTime;
        } else mFrameCounter += 1;
    }

    private void switchMode (MODE mode) {
        this.mModeSwitched = true;
        this.mDrawmode = mode;
    }


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

    //TODO: add shader object and upgrade loader with attrib handling
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.05f, 0.0f, 0.1f, 0.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //TODO: set up starting view angle
        mCamera.updateCamera();

        mTime = new Date().getTime();
        mFrameCounter = 0;

        mTree.initialize(mContext, mProgramManager);
        mTreeBackground.initialize(mContext, mProgramManager);

        /*mFontMap = new FontMap("Roboto-Regular.ttf", 1800, mContext.getAssets());
        mFontMap.initialize(mContext, mShaderFolder);*/
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float[] projectionMatrix = new float[16];

        final float ratio = (float) width/height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;

        /*Log.e("Draw", new StringBuilder().append("Width: ").append(mScreenWidth).toString());
        if (mScreenWidth != 0 && mScreenWidth != width) {           //check if screen has been initialized and rotated
            this.mResolutionChanged = true;
            Log.e("Draw", new StringBuilder().append("FLIPPED").toString());
        }*/

        mScreenWidth = width;
        Log.e("Draw", new StringBuilder().append("Width: ").append(mScreenWidth).toString());
        mScreenHeight = height;
        mScreenMetrics = new int[] {0, 0, mScreenWidth, mScreenHeight};

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, mNearPlane, mFarPlane);

        mCamera.setProjM(projectionMatrix);

        Log.e("Draw", new StringBuilder("Context: ").append(mContext.toString()).toString());

        /*mWindow = new Window(20, 1.0f, mCamera, mScreenMetrics);

        mWindow.initialize(mContext, mShaderFolder);*/
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //Log.e("Draw", new StringBuilder().append("Width: ").append(mScreenWidth).toString());
        if (this.mResolutionChanged) {
            //this.mWindow.rotate90();
            this.mResolutionChanged = false;
        }

        update();

        if (mModeSwitched) {
            if (mDrawmode == MODE.QUESTION) {
                mWindow = new Window(20, 2.0f, mCamera, mScreenMetrics);        //TODO: debug the value of 1.0
                mWindow.setQuestion(this.getQuestion(1));
                mWindow.initialize(mContext, mProgramManager);
            }
            else mWindow.release();

            mModeSwitched = false;
        }

        mCurrentBackground.draw(mCamera);
        if (mDrawmode == MODE.TREE) {
            mTree.draw(mCamera);
            mTouchHandler = mTreeTouchHandler;
        } else {
            mWindow.draw(mCamera);
            mTouchHandler = mWindowTouchHandler;
        }
        //mTree.draw(mCamera);
        //mWindow.draw(mCamera);

        countFPS();

    }
}
