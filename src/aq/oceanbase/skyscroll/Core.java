package aq.oceanbase.skyscroll;

import android.app.Application;
import android.util.Log;
import aq.oceanbase.skyscroll.logic.Game;

public class Core extends Application {
    private static Core mCore;
    private Game mGameInstance;

    public Core getCore() {
        return mCore;
    }

    public Game getGameInstance() {
        return mGameInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCore = this;

        mGameInstance = new Game(getApplicationContext());
        Log.e("Debug", new StringBuilder().append("APPLICATION CREATED").toString());
    }
}
