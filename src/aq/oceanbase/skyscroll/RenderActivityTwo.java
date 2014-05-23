package aq.oceanbase.skyscroll;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import aq.oceanbase.skyscroll.Renderers.GLSurfaceRendererTwo;
import aq.oceanbase.skyscroll.Renderers.RendererOne;
import aq.oceanbase.skyscroll.Renderers.RendererTwo;


public class RenderActivityTwo extends Activity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mGLSurfaceView = new GLSurfaceRendererTwo(this);

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