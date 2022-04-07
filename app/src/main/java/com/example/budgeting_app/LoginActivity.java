package com.example.budgeting_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.logEmail)
    EditText logEmail;
    @BindView(R.id.logPassword)
    EditText logPassword;
    @BindView(R.id.btnLogin)
    Button logBtn;
    @BindView(R.id.gotoRegister)
    TextView gotoRegister;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private ImageView shownhide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        //Show/Hide Password
        shownhide = findViewById(R.id.logshow);
        shownhide.setImageResource(R.drawable.ic_show_pwd);
        shownhide.setOnClickListener(view -> {
            if (logPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                //If password is visible the hide it
                logPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                //Change Icon
                shownhide.setImageResource(R.drawable.ic_show_pwd);
            } else {
                //Show password
                logPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                //Change Icon
                shownhide.setImageResource(R.drawable.ic_hide_pwd);
            }
        });

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

        gotoRegister.setOnClickListener(this::onRegistrationClick);

        logBtn.setOnClickListener(v -> {
            String emailString = logEmail.getText().toString();
            String passwordString = logPassword.getText().toString();

            if (TextUtils.isEmpty(emailString)) {
                logEmail.setError("Email is Required");
            }

            if (TextUtils.isEmpty(passwordString)) {
                logPassword.setError("Password is Required");
            } else {

                progressDialog.setMessage("Login in Progress");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                mAuth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(LoginActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }

        });
    }

    public void onRegistrationClick(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
    }
}