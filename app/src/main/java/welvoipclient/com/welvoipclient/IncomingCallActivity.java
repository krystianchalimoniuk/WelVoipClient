package welvoipclient.com.welvoipclient;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Krystiano on 2016-12-31.
 */

public class IncomingCallActivity extends AppCompatActivity {

ImageView accept,reject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incominggui);


        accept = (ImageView)findViewById(R.id.accept_call);
        reject = (ImageView)findViewById(R.id.reject_call);

        final String IP = getIntent().getStringExtra("ip");
        final MediaPlayer mediaPlayer = new MediaPlayer();

        Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(2000); //dwie sekundy wibracji

        try
        {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mediaPlayer.setDataSource(this, alert);
            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0)
            {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
        } catch(Exception e) {e.printStackTrace();}

        accept.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mediaPlayer.stop();
                Intent data = new Intent();
                data.putExtra("ip", IP);
                setResult(1, data);
                finish();
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

    }
}