package geek.jai.angelbot.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by JAID on 28-05-2016.
 * Application object
 */
public class ChatBotApp extends Application {
    private static ChatBotApp instance;
    public static String API_URL = "";
    public static final String HPE_API_KEY = "";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static ChatBotApp getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

}
