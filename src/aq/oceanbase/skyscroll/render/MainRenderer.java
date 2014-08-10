package aq.oceanbase.skyscroll.render;

import android.content.Context;
import android.opengl.*;
import android.util.Log;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.primitives.Background;
import aq.oceanbase.skyscroll.graphics.primitives.Sprite;
import aq.oceanbase.skyscroll.graphics.windows.Window;
import aq.oceanbase.skyscroll.loaders.ShaderLoader;
import aq.oceanbase.skyscroll.math.MathMisc;
import aq.oceanbase.skyscroll.loaders.TextureLoader;
import aq.oceanbase.skyscroll.math.Vector2f;
import aq.oceanbase.skyscroll.math.Vector3f;
import aq.oceanbase.skyscroll.touch.TouchHandler;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.tree.Tree;
import aq.oceanbase.skyscroll.tree.nodes.NodeOrderUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.nio.FloatBuffer;
import java.util.Date;


//TODO: check scope of all variables
public class MainRenderer implements GLSurfaceView.Renderer {

    private final Context mContext;

    //Constants and sizes
    //TODO: read about statics. maybe it's a bad idea to do sizes static
    public static final int mBytesPerFloat = 4;
    private int mScreenWidth;
    private int mScreenHeight;
    private int[] mScreenMetrics;
    private boolean mResolutionChanged = false;

    private final String mShaderFolder;

    //Constraints
    private float mMinHeight = 0.0f;
    private float mMaxHeight = 30.0f;
    private float mMinDist = 8.0f;
    private float mMaxDist = 20.0f;

    //Rendering settings
    private final float mNearPlane = 1.0f;
    private final float mFarPlane = 30.0f;

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
        };

        @Override
        public void onSwipeVertical(float amount) {
            mMomentum.y = amount;
        };

        @Override
        public void onScale(float span) {
            if (Math.abs(span) > 0.1) mDistance = mDistance - span;
            if (mDistance <= mMinDist) mDistance = mMinDist;
            else if (mDistance > mMaxDist) mDistance = mMaxDist;
        };

        @Override
        public void onTap(float x, float y) {
            mTree.performRaySelection(new TouchRay(x, y, 1.0f, mCamera, mScreenMetrics));
            //Log.e("Draw", new StringBuilder().append("Pos: ").append(x).append(" ").append(y).toString());
        };
    };


    public MainRenderer(Context context) {
        mContext = context;

        Log.e("RunDebug", "Renderer constructor stage passed");

        mTree = new Tree();

        mCurrentBackground = mTreeBackground;
        mTouchHandler = mTreeTouchHandler;

        mShaderFolder = "/aq/oceanbase/skyscroll/shaders";
    }


    //<editor-fold desc="Getters and Setters">
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

        mTree.initialize(mContext, mShaderFolder);
        mTreeBackground.initialize(mContext, mShaderFolder);
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

        mWindow = new Window(20, 1.0f, mCamera, mScreenMetrics);

        mWindow.initialize(mContext, mShaderFolder);
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
        mCurrentBackground.draw(mCamera);
        mTree.draw(mCamera);
        //mWindow.draw(mCamera);

        countFPS();
    }
}
