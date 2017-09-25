package io.agora.agolet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import io.agora.agolet.data.Channel;

/**
 * Created by Lucy on 8/30/17.
 */
public class SplashActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 3000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this,MiddleActivity.class);

                /* Skip Login Activity if user has already logged once
                 since if logged, channel database cannot be empty */
                if (Channel.listAll(Channel.class).size() != 0){
                    mainIntent.putExtra("goMain", "goMain");
                }
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
