package welvoipclient.com.welvoipclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Krystiano on 2016-11-28.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //background tasks
        for (int i=0;i<20000;i++){
            Log.i("LOG", "i: "+i);
        }
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
}
