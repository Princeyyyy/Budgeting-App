package com.example.budgeting_app.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.budgeting_app.R;

public class Splash_ScreenActivity extends AppCompatActivity {

    private static int SPLASH = 3000;
    Animation animation;

    private TextView appName;
    private ImageView appImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        appName = findViewById(R.id.appName);
        appImage = findViewById(R.id.appImage);

        animation = AnimationUtils.loadAnimation(this, R.anim.animation);
        appImage.setAnimation(animation);
        appName.setAnimation(animation);

        SharedPreferences sharedPreferences = getSharedPreferences("State", MODE_PRIVATE);
        boolean preferences = sharedPreferences.getBoolean("isChecked", false);
        String value = String.valueOf(preferences);


        new Handler().postDelayed(() -> {
            Intent intent;
            if (value.equals("true")) {
                intent = new Intent(Splash_ScreenActivity.this, PassCodeActivity.class);
            } else {
                intent = new Intent(Splash_ScreenActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH);
    }
}