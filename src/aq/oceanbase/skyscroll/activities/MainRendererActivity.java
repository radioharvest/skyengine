package aq.oceanbase.skyscroll.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import aq.oceanbase.skyscroll.Core;
import aq.oceanbase.skyscroll.graphics.render.GLSurfaceMainRenderer;

public class MainRendererActivity extends Activity {
    private GLSurfaceMainRenderer mGLSurfaceView;
    private Core mCore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCore = (Core)this.getApplication();

        mGLSurfaceView = new GLSurfaceMainRenderer(this, mCore.getGameInstance());

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsES2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (!supportsES2) return;

        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
        mGLSurfaceView.requestInit();
        if (mCore != null) {
            mCore.getGameInstance().onActivityResume();
        }
        Log.e("Debug", "Render On Resume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
        if (mCore != null) {
            mCore.getGameInstance().onActivityPause();
        }
        Log.e("Debug", "Render On Pause called");
    }
}
