package com.example.budgeting_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hanks.passcodeview.PasscodeView;

public class PassCodeActivity extends AppCompatActivity {

    PasscodeView passcodeView;

    private FirebaseAuth auth;
    private String onlineUserId = "";
    private DatabaseReference reference;

    private String pin;

    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_code);


        auth = FirebaseAuth.getInstance();

        onlineUserId = auth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance()
                .getReference("user-details")
                .child(onlineUserId);

        pin = String.valueOf(reference.child("passcode").get());
        passcodeView = findViewById(R.id.passcode);

        SharedPreferences sharedPreferences = getSharedPreferences("State2", MODE_PRIVATE);
        String preferences = sharedPreferences.getString("pin", "");

        passcodeView.setPasscodeLength(preferences.length());
        passcodeView.setLocalPasscode(preferences)
                .setListener(new PasscodeView.PasscodeViewListener() {
                    @Override
                    public void onFail() {
                        Toast.makeText(PassCodeActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(String number) {
                        Intent intent = new Intent(PassCodeActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                });

    }
}