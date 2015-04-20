package aq.oceanbase.skyscroll;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import aq.oceanbase.skyscroll.engine.activities.MainRendererActivity;
import aq.oceanbase.skyscroll.legacy.DemoRenderActivity;
import aq.oceanbase.skyscroll.legacy.RenderActivityOne;
import aq.oceanbase.skyscroll.legacy.RenderActivityTwo;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /*Intent intent = new Intent(this, MainRendererActivity.class);
        Log.e("RunDebug", "Main Activity stage passed");
        startActivity(intent);*/
    }

    public void startRenderOne(View view) {
        Intent intent = new Intent(this, RenderActivityOne.class);
        startActivity(intent);
    }

    public void startRenderTwo(View view) {
        Intent intent = new Intent(this, RenderActivityTwo.class);
        startActivity(intent);
    }

    public void startDemo(View view) {
        Intent intent = new Intent(this, DemoRenderActivity.class);
        startActivity(intent);
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, MainRendererActivity.class);
        Log.e("RunDebug", "Main Activity stage passed");
        startActivity(intent);
    }
}
