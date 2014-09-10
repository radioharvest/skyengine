package aq.oceanbase.skyscroll.graphics.render;

import android.content.Context;
import android.opengl.*;
import android.util.Log;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.ProgramManager;
import aq.oceanbase.skyscroll.graphics.RenderContainer;
import aq.oceanbase.skyscroll.graphics.primitives.Background;
import aq.oceanbase.skyscroll.graphics.windows.Window;
import aq.oceanbase.skyscroll.logic.Game;
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
public class MainRenderer implements GLSurfaceView.Renderer {

    private final Context mContext;

    //Constants and sizes
    //TODO: read about statics. maybe it's a bad idea to do sizes static
    public static final int mBytesPerFloat = 4;
    private int mScreenWidth;
    private int mScreenHeight;

    private boolean mResolutionChanged = false;

    private final String mShaderFolder;
    private ProgramManager mProgramManager;

    //Rendering settings
    private final float mNearPlane;
    private final float mFarPlane;

    public Game mGameInstance;

    //Camera
    private Camera mCamera;

    //FPS Counter
    private int mFrameCounter;
    private long mTime;

    //Renderables
    private RenderContainer mRenderables;



    public MainRenderer(Context context, String shaderFolder) {
        mContext = context;
        mShaderFolder = shaderFolder;
        mProgramManager = new ProgramManager(shaderFolder);

        mNearPlane = 1.0f;
        mFarPlane = 40.0f;
    }

    public MainRenderer(Context context, String shaderFolder, float nearPlane, float farPlane) {
        mContext = context;
        mShaderFolder = shaderFolder;
        mProgramManager = new ProgramManager(shaderFolder);

        mNearPlane = nearPlane;
        mFarPlane = farPlane;
    }


    //<editor-fold desc="Getters and Setters">
    public Camera getCamera() {
        return mCamera;
    }

    public void setCamera(Camera cam) {
        this.mCamera = cam;
    }

    public RenderContainer getRenderables() {
        return mRenderables;
    }

    public void setRenderables(RenderContainer renderables) {
        this.mRenderables = renderables;
    }

    public void setGameInstance(Game gameInstance) {
        this.mGameInstance = gameInstance;
        //this.mGameInstance = new Game();
        this.mCamera = mGameInstance.getCamera();
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

        mGameInstance.mCurrentRenderables.initialize(mContext, mProgramManager);
        /*mGameInstance.mTreeBackground.initialize(mContext, mShaderFolder);
        mGameInstance.mGameSession.tree.initialize(mContext, mShaderFolder);*/
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

        mScreenWidth = width;
        mScreenHeight = height;

        mGameInstance.setScreenMetrics(new int[] {0, 0, mScreenWidth, mScreenHeight});

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, mNearPlane, mFarPlane);

        mCamera.setProjM(projectionMatrix);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //Log.e("Draw", new StringBuilder().append("Width: ").append(mScreenWidth).toString());
        if (this.mResolutionChanged) {
            //this.mWindow.rotate90();
            this.mResolutionChanged = false;
        }

        mGameInstance.mCurrentRenderables.initializeNewObjects(mContext, mProgramManager);
        mGameInstance.mCurrentRenderables.draw(mCamera);
        /*mGameInstance.mTreeBackground.draw(mCamera);

        if (mGameInstance.mWindow != null) {
            if (!mGameInstance.mWindow.isInitialized()) mGameInstance.mWindow.initialize(mContext, mShaderFolder);

            mGameInstance.mWindow.draw(mCamera);
        } else {
            mGameInstance.mGameSession.tree.draw(mCamera);
        }*/

        mGameInstance.update();

        countFPS();

    }
}
