package aq.oceanbase.skyscroll;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
}
