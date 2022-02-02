package com.example.budgeting_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChooseAnalyticsActivity extends AppCompatActivity {

    @BindView(R.id.analyticsTodayCardView)
    CardView analyticsTodayCardView;
    @BindView(R.id.analyticsWeekCardView)
    CardView analyticsWeekCardView;
    @BindView(R.id.analyticsMonthCardView)
    CardView analyticsMonthCardView;

    private Toolbar analyticsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_analytics);
        ButterKnife.bind(this);

        analyticsToolbar = findViewById(R.id.analyticsToolbar);
        setSupportActionBar(analyticsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Analytics");

        analyticsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseAnalyticsActivity.this,MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.stay);
            }
        });

        analyticsTodayCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseAnalyticsActivity.this, DailyAnalyticsActivity.class);
                startActivity(intent);
            }
        });

        analyticsWeekCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseAnalyticsActivity.this, WeeklyAnalyticsActivity.class);
                startActivity(intent);
            }
        });

        analyticsMonthCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseAnalyticsActivity.this, MonthlyAnalyticsActivity.class);
                startActivity(intent);
            }
        });
    }
}