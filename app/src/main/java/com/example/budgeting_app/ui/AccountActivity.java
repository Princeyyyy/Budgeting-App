package com.example.budgeting_app.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgeting_app.R;
import com.example.budgeting_app.models.UserDetails;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class AccountActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private Toolbar toolbar;
    private TextView userFName, userLName, userEmail, userPassword, userPin;
    private Button logoutBtn, updateDetailsBtn;
    private Switch aSwitch;

    private FirebaseAuth auth;
    private String onlineUserId = "";
    private DatabaseReference reference;

    String fname;
    String lname;
    String pin;
    String email;
    String password;

    private DrawerLayout drawerLayout;

    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";

    private Button btnDate;

    private ImageView show;

    private int day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        btnDate = findViewById(R.id.budgetResetBtn);

        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Account");

        drawerLayout = findViewById(R.id.account_drawer);
        NavigationView navigationView = findViewById(R.id.account_navigation);
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
                case R.id.about:
                    Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
                    startActivity(aboutIntent);
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

        aSwitch = findViewById(R.id.enableEncryption);

        updateDetailsBtn = findViewById(R.id.updateDetailsBtn);

        logoutBtn = findViewById(R.id.logoutBtn);
        userFName = findViewById(R.id.userFName);
        userLName = findViewById(R.id.userLName);
        userEmail = findViewById(R.id.userEmail);
        userPassword = findViewById(R.id.userPass);
        userPin = findViewById(R.id.userPin);

        auth = FirebaseAuth.getInstance();
        onlineUserId = auth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference("user-details").child(auth.getCurrentUser().getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserDetails user = snapshot.getValue(UserDetails.class);
                userFName.setText("First Name: " + user.getFname());
                userLName.setText("Last Name: " + user.getLname());
                userEmail.setText("Email: " + user.getEmail());
                userPassword.setText("Password: " + user.getPassword());
                userPin.setText("Pin: " + user.getPasscode());

                fname = user.getFname();
                lname = user.getLname();
                pin = user.getPasscode();
                email = user.getEmail();
                password = user.getPassword();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("State", MODE_PRIVATE);
        SharedPreferences.Editor preferences = sharedPreferences.edit();
        aSwitch.setChecked(sharedPreferences.getBoolean("isChecked", false));
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && pin.equals("null")) {
                Toast.makeText(this, "You need to set a passcode first!!", Toast.LENGTH_SHORT).show();
                aSwitch.setChecked(false);
            } else if (isChecked) {
                preferences.putBoolean("isChecked", true);
            } else {
                preferences.putBoolean("isChecked", false);
            }
            preferences.apply();
        });


        updateDetailsBtn.setOnClickListener(view -> updateUser());

        logoutBtn.setOnClickListener(view -> new AlertDialog.Builder(AccountActivity.this)
                .setTitle("Personal Budgeting App")
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                    startActivity(intent);

                    SharedPreferences sharedPreferences1 = getSharedPreferences("State", MODE_PRIVATE);
                    SharedPreferences.Editor preferences1 = sharedPreferences1.edit();
                    preferences1.putBoolean("isChecked", false);
                    preferences1.apply();

                    finish();
                })
                .setNegativeButton("No", null)
                .show());

        btnDate.setOnClickListener(view -> {
            showDatePickerDialog();
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

    private void updateUser() {
        DatabaseReference updateData = FirebaseDatabase.getInstance().getReference("user-details").child(onlineUserId);
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.update_user, null);
        myDialog.setView(view);

        AlertDialog dialog = myDialog.create();

        EditText uFname = view.findViewById(R.id.updateFname);
        EditText uLname = view.findViewById(R.id.updateLname);
        EditText uPin = view.findViewById(R.id.updatePin);

        uFname.setText(fname);
        uLname.setText(lname);
        uPin.setText(pin);

        Button cancelBtn = view.findViewById(R.id.cancelBtn);
        Button updateButton = view.findViewById(R.id.update);

        updateButton.setOnClickListener(v -> {
            fname = uFname.getText().toString().trim();
            lname = uLname.getText().toString().trim();
            pin = uPin.getText().toString().trim();

            updateData.child("fname").setValue(fname);
            updateData.child("lname").setValue(lname);
            updateData.child("passcode").setValue(pin);


            Toast.makeText(AccountActivity.this, "Account Updated Successful", Toast.LENGTH_SHORT).show();

            dialog.dismiss();

        });


        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        day = dayOfMonth;
//        int months = month + 1;
//        String date = dayOfMonth + "-" + months + "-" + year;
//        Toast.makeText(this, "Reset will happen every " + date, Toast.LENGTH_SHORT).show();
        informResetDateNotification();
    }

    private void informResetDateNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            // Configure the notification channel.
            notificationChannel.setDescription("Budgeting App Reset Date");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.image2)
                .setTicker("Budgeting App")
                //.setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Budgeting App")
                .setContentText("Reset date has been set for " + day + " of every month")
                .setContentInfo("Budget Reset Date");
        notificationManager.notify(1, notificationBuilder.build());
    }
}