package com.example.budgeting_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class WeekSpendingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView totalWeekAmountTextView;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private WeekSpendingAdapter weekSpendingAdapter;
    private List<Data> myDataList;

    private FirebaseAuth mAuth;
    private String onlineUserId = "";
    private DatabaseReference expensesRef;

    private String type = "";

    private TextView display;

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weeks_spending);

        display = findViewById(R.id.display);

        toolbar = findViewById(R.id.toolbar2);

        drawerLayout = (DrawerLayout) findViewById(R.id.week_drawer);
        NavigationView navigationView = findViewById(R.id.week_navigation);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int menuId = menuItem.getItemId();

            switch (menuId) {
                case R.id.main:
                    Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(mainIntent);
                    break;
                case R.id.budget:
                    Intent budgetIntent = new Intent(getApplicationContext(), BudgetActivity.class);
                    startActivity(budgetIntent);
                    break;
                case R.id.today:
                    Intent todayIntent = new Intent(getApplicationContext(), TodaySpendingActivity.class);
                    startActivity(todayIntent);
                    break;
                case R.id.week:
                    Intent weekIntent = new Intent(getApplicationContext(), WeekSpendingActivity.class);
                    weekIntent.putExtra("type", "week");
                    startActivity(weekIntent);
                    break;
                case R.id.month:
                    Intent monthIntent = new Intent(getApplicationContext(), WeekSpendingActivity.class);
                    monthIntent.putExtra("type", "month");
                    startActivity(monthIntent);
                    break;
                case R.id.todayAnalytics:
                    Intent todayAnalyticsIntent = new Intent(getApplicationContext(), DailyAnalyticsActivity.class);
                    startActivity(todayAnalyticsIntent);
                    break;
                case R.id.weekAnalytics:
                    Intent weekAnalyticsIntent = new Intent(getApplicationContext(), WeeklyAnalyticsActivity.class);
                    startActivity(weekAnalyticsIntent);
                    break;
                case R.id.monthAnalytics:
                    Intent monthAnalyticsIntent = new Intent(getApplicationContext(), MonthlyAnalyticsActivity.class);
                    startActivity(monthAnalyticsIntent);
                    break;
                case R.id.history:
                    Intent historyIntent = new Intent(getApplicationContext(), HistoryActivity.class);
                    startActivity(historyIntent);
                    break;
                case R.id.profile:
                    Intent profileIntent = new Intent(getApplicationContext(), AccountActivity.class);
                    startActivity(profileIntent);
                    break;
                case R.id.logout:
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Personal Budgeting App")
                            .setMessage("Are you sure you want to exit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", (dialog, id) -> {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);

                                SharedPreferences sharedPreferences1 = getSharedPreferences("State", MODE_PRIVATE);
                                SharedPreferences.Editor preferences1 = sharedPreferences1.edit();
                                preferences1.putBoolean("isChecked", false);
                                preferences1.apply();

                                finish();
                            })
                            .setNegativeButton("No", null)
                            .show();
                    break;
                default:
                    setContentView(R.layout.activity_main);
                    break;
            }
            drawerLayout.closeDrawers();
            return true;
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.Drawer_open, R.string.Drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        totalWeekAmountTextView = findViewById(R.id.totalWeekAmountTextView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();


        myDataList = new ArrayList<>();
        weekSpendingAdapter = new WeekSpendingAdapter(WeekSpendingActivity.this, myDataList);
        recyclerView.setAdapter(weekSpendingAdapter);

        if (getIntent().getExtras() != null) {
            type = getIntent().getStringExtra("type");
            if (type.equals("week")) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setIcon(R.drawable.ic_baseline_menu_24);
                getSupportActionBar().setTitle("Week's Spending");
                readWeeksSpendingItems();
            } else if (type.equals("month")) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setIcon(R.drawable.ic_baseline_menu_24);
                getSupportActionBar().setTitle("Month's Spending");
                readMonthsSpendingItems();
            }
        }


    }

    private void readMonthsSpendingItems() {
        Calendar calendar = Calendar.getInstance();
        String month = calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.MONTH);

        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = expensesRef.orderByChild("month").equalTo(month);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Data data = dataSnapshot.getValue(Data.class);
                    myDataList.add(data);
                }

                weekSpendingAdapter.notifyDataSetChanged();

                if (myDataList.isEmpty()) {
                    totalWeekAmountTextView.setVisibility(View.GONE);
                    display.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    display.setVisibility(View.GONE);

                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;

                        totalWeekAmountTextView.setText("Month's Spending: Ksh." + totalAmount);
                        totalWeekAmountTextView.setVisibility(View.VISIBLE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readWeeksSpendingItems() {
        Calendar calendar = Calendar.getInstance();
        String week = calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.WEEK_OF_YEAR);

        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = expensesRef.orderByChild("week").equalTo(week);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Data data = dataSnapshot.getValue(Data.class);
                    myDataList.add(data);
                }

                weekSpendingAdapter.notifyDataSetChanged();

                if (myDataList.isEmpty()) {
                    totalWeekAmountTextView.setVisibility(View.GONE);
                    display.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    display.setVisibility(View.GONE);

                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;

                        totalWeekAmountTextView.setText("Week's Spending: Ksh." + totalAmount);
                        totalWeekAmountTextView.setVisibility(View.VISIBLE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}