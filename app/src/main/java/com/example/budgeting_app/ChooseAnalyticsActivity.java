package com.example.budgeting_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChooseAnalyticsActivity extends AppCompatActivity {

    @BindView(R.id.analyticsTodayCardView)
    CardView analyticsTodayCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_analytics);
        ButterKnife.bind(this);

        analyticsTodayCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseAnalyticsActivity.this, DailyAnalyticsActivity.class);
                startActivity(intent);
            }
        });
    }
}