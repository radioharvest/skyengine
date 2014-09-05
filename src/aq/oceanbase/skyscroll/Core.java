package aq.oceanbase.skyscroll;

import android.app.Application;
import android.util.Log;
import aq.oceanbase.skyscroll.logic.Game;

public class Core extends Application {
    private Game mGameInstance;

    public Game getGameInstance() {
        return mGameInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGameInstance = new Game();
        Log.e("Debug", new StringBuilder().append("APPLICATION CREATED").toString());
    }
}
