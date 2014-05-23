package aq.oceanbase.skyscroll;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import aq.oceanbase.skyscroll.Renderers.GLSurfaceDemoRenderer;
import aq.oceanbase.skyscroll.Renderers.GLSurfaceRendererTwo;


public class DemoRenderActivity extends Activity {
    private GLSurfaceView mGLSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mGLSurfaceView = new GLSurfaceDemoRenderer(this);

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }
}