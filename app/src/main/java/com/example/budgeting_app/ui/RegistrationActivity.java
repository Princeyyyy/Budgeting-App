package com.example.budgeting_app.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgeting_app.R;
import com.example.budgeting_app.models.UserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    private EditText regEmail;
    private EditText regPassword;
    private Button btnReg;
    private TextView gotoLogin;

    private FirebaseAuth mAuth;
    private ProgressBar load;
    private DatabaseReference users;

    private ImageView shownhide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        regEmail = findViewById(R.id.regEmail);
        regPassword = findViewById(R.id.regPassword);
        btnReg = findViewById(R.id.btnReg);
        gotoLogin = findViewById(R.id.gotoLogin);

        mAuth = FirebaseAuth.getInstance();
        load = findViewById(R.id.register_load);

        //Show/Hide Password
        shownhide = findViewById(R.id.regshow);
        shownhide.setImageResource(R.drawable.ic_show_pwd);
        shownhide.setOnClickListener(view -> {
            if (regPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                //If password is visible the hide it
                regPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                //Change Icon
                shownhide.setImageResource(R.drawable.ic_show_pwd);
            } else {
                //Show password
                regPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                //Change Icon
                shownhide.setImageResource(R.drawable.ic_hide_pwd);
            }
        });

        gotoLogin.setOnClickListener(this::onLoginClick);

        btnReg.setOnClickListener(v -> {
            String email = regEmail.getText().toString();
            String password = regPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                regEmail.setError("Email is Required");
            }

            if (TextUtils.isEmpty(password)) {
                regPassword.setError("Password is Required");
            } else {
                load.setVisibility(View.VISIBLE);
                btnReg.setEnabled(false);
                btnReg.setText("Creating your account");

                UserDetails userDetails = new UserDetails("null", "null", email, password, "null");
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        load.setVisibility(View.INVISIBLE);

                        users = FirebaseDatabase.getInstance().getReference().child("user-details").child(mAuth.getCurrentUser().getUid());

                        users.setValue(userDetails).addOnCompleteListener(task1 -> {
                            //Do nothing
                        });
                    } else {
                        Toast.makeText(RegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        load.setVisibility(View.INVISIBLE);
                        btnReg.setText("Register");
                        btnReg.setEnabled(true);
                    }
                });
            }
        });
    }

    public void onLoginClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.stay);
    }
}