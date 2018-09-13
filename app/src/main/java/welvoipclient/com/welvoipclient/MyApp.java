package welvoipclient.com.welvoipclient;

import com.orm.SugarApp;
import com.orm.SugarContext;

/**
 * Created by Krystiano on 2016-11-29.
 */

public class MyApp extends SugarApp {
    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }
}
