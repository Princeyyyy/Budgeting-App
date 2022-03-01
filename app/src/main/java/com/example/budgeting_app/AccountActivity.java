package com.example.budgeting_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountActivity extends AppCompatActivity {

    private Toolbar settingsToolbar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        aSwitch = findViewById(R.id.enableEncryption);

        settingsToolbar = findViewById(R.id.my_Feed_Toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Account");

        updateDetailsBtn = findViewById(R.id.updateDetailsBtn);

        settingsToolbar.setNavigationOnClickListener(view -> {
            Intent intent = new Intent(AccountActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.stay);
        });

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
                password = user.getPasscode();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("State", MODE_PRIVATE);
        SharedPreferences.Editor preferences = sharedPreferences.edit();
        aSwitch.setChecked(sharedPreferences.getBoolean("isChecked", false));
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
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

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fname = uFname.getText().toString().trim();
                lname = uLname.getText().toString().trim();
                pin = uPin.getText().toString().trim();

                updateData.child("fname").setValue(fname);
                updateData.child("lname").setValue(lname);
                updateData.child("passcode").setValue(pin);

                SharedPreferences sharedPreferences2 = getSharedPreferences("State2", MODE_PRIVATE);
                SharedPreferences.Editor preferences2 = sharedPreferences2.edit();
                preferences2.putString("pin", pin);
                preferences2.apply();


                Toast.makeText(AccountActivity.this, "Account Update Successful", Toast.LENGTH_SHORT).show();

                dialog.dismiss();

            }
        });


        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}