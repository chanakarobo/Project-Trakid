package com.example.trakid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView iv = findViewById(R.id.imageView);

        Animation animation= AnimationUtils.loadAnimation(this, R.anim.splashtransition);
        iv.startAnimation(animation);

       final Intent i=new Intent(this,SignInActivity.class);

        Thread timer = new Thread(){
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    startActivity(i);
                    finish();
                }
            }
        };
        timer.start();
        }
}
