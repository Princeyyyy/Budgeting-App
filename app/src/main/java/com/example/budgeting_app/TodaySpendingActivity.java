package com.example.budgeting_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TodaySpendingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView totalAmountSpentOn;
    private ProgressBar progressBar;
    private RecyclerView recyclerView2;
    private FloatingActionButton fab;
    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private String onlineUserId = "";
    private DatabaseReference expensesRef;

    private TodayItemsAdapter todayItemsAdapter;
    private List<Data> myDataList;

    private TextView display;

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todays_spending);

        display = findViewById(R.id.display);

        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Today's Spending");

        drawerLayout = findViewById(R.id.today_drawer);
        NavigationView navigationView = findViewById(R.id.today_navigation);
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

        totalAmountSpentOn = findViewById(R.id.totalAmountSpentOn);
        progressBar = findViewById(R.id.progressBar);

        fab = findViewById(R.id.fab);
        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();
        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);

        recyclerView2 = findViewById(R.id.recyclerView2);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView2.setHasFixedSize(true);
        recyclerView2.setLayoutManager(linearLayoutManager);

        myDataList = new ArrayList<>();
        todayItemsAdapter = new TodayItemsAdapter(TodaySpendingActivity.this, myDataList);
        recyclerView2.setAdapter(todayItemsAdapter);

        readItems();

        fab.setOnClickListener(view -> addItemSpentOn());
    }

    private void readItems() {

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Data data = dataSnapshot.getValue(Data.class);
                    myDataList.add(data);
                }

                todayItemsAdapter.notifyDataSetChanged();

                if (myDataList.isEmpty()) {
                    totalAmountSpentOn.setVisibility(View.GONE);
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

                        totalAmountSpentOn.setText("Today's Spending: Ksh." + totalAmount);
                        totalAmountSpentOn.setVisibility(View.VISIBLE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                display.setVisibility(View.VISIBLE);
            }
        });


    }

    private void addItemSpentOn() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);
        dialog.show();

        final Spinner itemSpinner = myView.findViewById(R.id.itemsspinner);
        final EditText amount = myView.findViewById(R.id.amount);
        final Button cancel = myView.findViewById(R.id.cancel);
        final Button save = myView.findViewById(R.id.save);
        final EditText note = myView.findViewById(R.id.note);

        note.setVisibility(View.VISIBLE);

        save.setOnClickListener(v -> {

            String Amount = amount.getText().toString();
            String Item = itemSpinner.getSelectedItem().toString();
            String notes = note.getText().toString();

            if (TextUtils.isEmpty(Amount)) {
                amount.setError("Amount is Required");
                return;
            }

            if (Item.equals("Select Item")) {
                Toast.makeText(TodaySpendingActivity.this, "Select a valid item", Toast.LENGTH_SHORT).show();
                return;

            }
            if (notes.equals("Select Item")) {
                Toast.makeText(TodaySpendingActivity.this, "Select a valid item", Toast.LENGTH_SHORT).show();
                return;
            } else {
                loader.setMessage("Adding expense item");
                loader.setCanceledOnTouchOutside(false);
                loader.show();

                String id = expensesRef.push().getKey();
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Calendar calendar = Calendar.getInstance();
                String date = dateFormat.format(calendar.getTime());

                String itemNday = Item + date;
                String itemNweek = Item + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.WEEK_OF_YEAR);
                String itemNmonth = Item + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.MONTH);
                String week = calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.WEEK_OF_YEAR);
                String month = calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.MONTH);

                Data data = new Data(Item, date, id, itemNday, itemNweek, itemNmonth, week, month, Integer.parseInt(Amount), notes);
                expensesRef.child(id).setValue(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(TodaySpendingActivity.this, "Today's Expense added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TodaySpendingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }

                    loader.dismiss();
                });
            }
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}