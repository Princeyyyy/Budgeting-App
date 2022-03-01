package com.example.budgeting_app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    String pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_code);

        passcodeView = findViewById(R.id.passcode);

        auth = FirebaseAuth.getInstance();
        onlineUserId = auth.getCurrentUser().getUid();

        //Getting Firebase Instance
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        //Getting Reference to Root Node
        DatabaseReference myRef = database.getReference();

        //Getting reference to passcode "child" node
        myRef = myRef.child("user-details").child(onlineUserId).child("passcode");


        //Adding eventListener to that reference
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Getting the string value of that the passcode node
                pin = dataSnapshot.getValue(String.class);

                passcodeView.setPasscodeLength(pin.length());
                passcodeView.setLocalPasscode(pin)
                        .setListener(new PasscodeView.PasscodeViewListener() {
                            @Override
                            public void onFail() {

                            }

                            @Override
                            public void onSuccess(String number) {
                                Intent intent = new Intent(PassCodeActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: Something went wrong! Error:" + databaseError.getMessage());

            }
        });
    }
}