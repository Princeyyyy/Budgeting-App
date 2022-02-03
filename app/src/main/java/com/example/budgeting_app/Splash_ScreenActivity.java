package com.example.budgeting_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Splash_ScreenActivity extends AppCompatActivity {

    private static int SPLASH = 3000;
    Animation animation;
    @BindView(R.id.appName)
    TextView appName;
    @BindView(R.id.appImage)
    ImageView appImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);
        ButterKnife.bind(this);

        animation = AnimationUtils.loadAnimation(this, R.anim.animation);
        appImage.setAnimation(animation);
        appName.setAnimation(animation);

        SharedPreferences sharedPreferences = getSharedPreferences("State", MODE_PRIVATE);
        boolean preferences = sharedPreferences.getBoolean("isChecked", false);
        String value = String.valueOf(preferences);

//        SharedPreferences sharedPreferences2 = getSharedPreferences("State2", MODE_PRIVATE);
//        String preferences2 = sharedPreferences2.getString("pin", "");

        SharedPreferences sharedPreferences3 = getSharedPreferences("State3", MODE_PRIVATE);
        String preferences3 = sharedPreferences3.getString("status", "");


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (preferences3.equals("logout")) {
                    Intent intent = new Intent(Splash_ScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else if (value.equals("true")) {
                    Intent intent = new Intent(Splash_ScreenActivity.this, PassCodeActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Splash_ScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }, SPLASH);
    }
}