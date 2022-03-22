package com.example.budgeting_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private RecyclerView recyclerView;

    private TodayItemsAdapter todayItemsAdapter2;
    private List<Data> myDataList;

    private FirebaseAuth mAuth;
    private String onlineUserId = "";

    private Toolbar toolbar;

    private CalendarView calendar;
    private TextView historyTotalAmountSpent,show;

    private TextView displayError;

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        displayError = findViewById(R.id.displayError);

        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("History");

        drawerLayout = findViewById(R.id.history_drawer);
        NavigationView navigationView = findViewById(R.id.history_navigation);
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

        historyTotalAmountSpent = findViewById(R.id.historyTotalAmountSpent);
        show = findViewById(R.id.show);

        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recycler_View_Id_Feed);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        myDataList = new ArrayList<>();
        todayItemsAdapter2 = new TodayItemsAdapter(HistoryActivity.this, myDataList);
        recyclerView.setAdapter(todayItemsAdapter2);

        calendar = findViewById(R.id.calendar);

        // Add Listener in calendar
        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            int months = month + 1;
            String padded = String.format("%02d", dayOfMonth);
            String date = padded + "-" + "0" + months + "-" + year;

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
            Query query = reference.orderByChild("date").equalTo(date);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    myDataList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Data data = snapshot.getValue(Data.class);
                        myDataList.add(data);
                    }

                    todayItemsAdapter2.notifyDataSetChanged();

                    if (myDataList.isEmpty()) {
                        historyTotalAmountSpent.setVisibility(View.GONE);
                        show.setVisibility(View.GONE);
                        displayError.setText("No data was entered on this day\nKindly pick another day");
                        displayError.setVisibility(View.VISIBLE);
                    } else {
                        show.setVisibility(View.GONE);
                        displayError.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        int totalAmount = 0;
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Map<String, Object> map = (Map<String, Object>) ds.getValue();
                            Object total = map.get("amount");
                            int pTotal = Integer.parseInt(String.valueOf(total));
                            totalAmount += pTotal;
                            if (totalAmount > 0) {
                                historyTotalAmountSpent.setText("This day you spent Ksh." + totalAmount);
                                historyTotalAmountSpent.setVisibility(View.VISIBLE);
                            }

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(HistoryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

        int months = month + 1;
        String padded = String.format("%02d", dayOfMonth);
        String date = padded + "-" + "0" + months + "-" + year;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myDataList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Data data = snapshot.getValue(Data.class);
                    myDataList.add(data);
                }

                todayItemsAdapter2.notifyDataSetChanged();

                if (myDataList.isEmpty()) {
                    historyTotalAmountSpent.setVisibility(View.GONE);
                    displayError.setText("No data was entered on this day\nKindly pick another day");
                    displayError.setVisibility(View.VISIBLE);
                } else {
                    displayError.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    int totalAmount = 0;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        if (totalAmount > 0) {
                            historyTotalAmountSpent.setText("This day you spent Ksh." + totalAmount);
                            historyTotalAmountSpent.setVisibility(View.VISIBLE);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HistoryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
