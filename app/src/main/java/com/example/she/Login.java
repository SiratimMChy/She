package com.example.she;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private EditText LoginEmail, LoginPass;
    private Button LoginBtn;
    private TextView SignupText, forgotPassword;
    private ProgressBar progressBar;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-z0-9_.+-]+@[a-z]+\\.[a-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*.])[A-Za-z\\d!@#$%^&*.]{8,20}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        LoginEmail = findViewById(R.id.LEmailEditText);
        LoginPass = findViewById(R.id.LpassEditText);
        LoginBtn = findViewById(R.id.Loginbtn);
        progressBar = findViewById(R.id.ProgressBar);
        SignupText = findViewById(R.id.SignupText);
        forgotPassword = findViewById(R.id.forgotPassword);

        SignupText.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, SignUp.class);
            startActivity(intent);
        });

        forgotPassword.setOnClickListener(view -> {
            String email = LoginEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                showToast("Please enter your email address.");
            } else if (!EMAIL_PATTERN.matcher(email).matches()) {
                showToast("Invalid email.");
            } else {
                resetPassword(email);
            }
        });

        LoginBtn.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        progressBar.setVisibility(View.VISIBLE);

        String email = LoginEmail.getText().toString().trim();
        String password = LoginPass.getText().toString().trim();

        if (!validateEmail(email) || !validatePassword(password)) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            fetchUserData(user.getUid());
                        } else {
                            showToast("Please verify your email before logging in.");
                            if (user != null) {
                                user.sendEmailVerification();
                                showToast("Verification email sent again.");
                            }
                        }
                    } else {
                        showToast("Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    private void fetchUserData(String userId) {
        databaseReference.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    boolean isAdmin = snapshot.child("isAdmin").getValue(Boolean.class) != null && snapshot.child("isAdmin").getValue(Boolean.class);
                    String name = snapshot.child("username").getValue(String.class);
                    String mobile = snapshot.child("mobile").getValue(String.class);
                    showToast("Welcome, " + name + "!");

                    Intent intent;
                    if (Boolean.TRUE.equals(isAdmin)) {
                        intent = new Intent(Login.this, AdminPanel.class);
                        intent.putExtra("userName", name);
                        intent.putExtra("userMobile", mobile);
                        startActivity(intent);
                    } else {
                        intent = new Intent(Login.this, Drawer_menu.class);
                        intent.putExtra("userName", name);
                        intent.putExtra("userMobile", mobile);
                    }

                    startActivity(intent);
                    finish();
                } else {
                    showToast("User data not found!");
                }
            } else {
                showToast("Failed to fetch user data: " + task.getException().getMessage());
            }
        });
    }

    private void resetPassword(String email) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        showToast("Password reset email sent. Please check your inbox.");
                    } else {
                        showToast("Failed to send password reset email: " + task.getException().getMessage());
                    }
                });
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            showToast("Enter Email");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showToast("Invalid Email Format");
            return false;
        }
        return true;
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            showToast("Enter Password");
            return false;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            showToast("Password must be 8-20 characters, with at least one uppercase, one lowercase, one number, and one special character.");
            return false;
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
    }
}