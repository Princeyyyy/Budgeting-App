package com.example.budgeting_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.budgetCardView)
    CardView budgetCardView;
    @BindView(R.id.todayCardView)
    CardView todayCardView;
    @BindView(R.id.weekCardView)
    CardView weekCardView;
    @BindView(R.id.monthCardView)
    CardView monthCardView;
    @BindView(R.id.analyticsCardView)
    CardView analyticsCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        budgetCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BudgetActivity.class);
                startActivity(intent);
            }
        });

        todayCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TodaysSpendingActivity.class);
                startActivity(intent);
            }
        });

        weekCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WeeksSpendingActivity.class);
                intent.putExtra("type", "week");
                startActivity(intent);
            }
        });

        monthCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WeeksSpendingActivity.class);
                intent.putExtra("type", "month");
                startActivity(intent);
            }
        });

        analyticsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChooseAnalyticsActivity.class);
                startActivity(intent);
            }
        });
    }
}