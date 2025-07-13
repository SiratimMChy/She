package com.example.she;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private EditText signupUser, signupEmail, signupPass, signupCpass, signupMobile;
    private Button signupButton;
    private TextView loginText;
    private ProgressBar progressBar;
    private CheckBox showPasswordCheckBox;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z. ]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z]+\\.[a-zA-Z]{2,}$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^(?:\\+88|88)?(01[3-9]\\d{8})$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*.])[A-Za-z\\d!@#$%^&*.]{8,20}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        signupUser = findViewById(R.id.SUserEditTextId);
        signupEmail = findViewById(R.id.SEmailEditTextId);
        signupPass = findViewById(R.id.SPassEditTextId);
        signupCpass = findViewById(R.id.SCpassEditTextId);
        signupMobile = findViewById(R.id.SMobEditTextId);
        signupButton = findViewById(R.id.SignupBtn);
        progressBar = findViewById(R.id.progressBar);
        loginText = findViewById(R.id.LoginText);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);


        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                signupPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                signupCpass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                signupPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                signupCpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            signupPass.setSelection(signupPass.getText().length());
            signupCpass.setSelection(signupCpass.getText().length());
        });


        loginText.setOnClickListener(view -> {
            Intent intent = new Intent(SignUp.this, Login.class);
            startActivity(intent);
        });

        signupButton.setOnClickListener(v -> {
            String username = signupUser.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String mobile = signupMobile.getText().toString().trim();
            String password = signupPass.getText().toString();
            String confirmPassword = signupCpass.getText().toString();


            if (!validateInputs(username, email, mobile, password, confirmPassword)) {
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {

                                        saveUserToDatabase(user.getUid(), username, email, mobile);

                                        Toast.makeText(SignUp.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(SignUp.this, Login.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(SignUp.this, "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(SignUp.this, "Email already in use.", Toast.LENGTH_SHORT).show();
                            } else if (e instanceof FirebaseAuthWeakPasswordException) {
                                Toast.makeText(SignUp.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignUp.this, "Authentication Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }

    private boolean validateInputs(String username, String email, String mobile, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username) || !NAME_PATTERN.matcher(username).matches()) {
            signupUser.setError("Invalid Name: Use 3-20 letters and dots only.");
            signupUser.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            signupEmail.setError("Invalid Email: Use a correct email format.");
            signupEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mobile) || !MOBILE_PATTERN.matcher(mobile).matches()) {
            signupMobile.setError("Invalid Mobile: Use a valid Bangladeshi number.");
            signupMobile.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password) || !PASSWORD_PATTERN.matcher(password).matches()) {
            signupPass.setError("Invalid Password: Use 8-20 chars with uppercase, lowercase, number, and special character.");
            signupPass.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            signupCpass.setError("Passwords do not match.");
            signupCpass.requestFocus();
            return false;
        }
        return true;
    }

    private void saveUserToDatabase(String userId, String username, String email, String mobile) {
        User user = new User(username, email, mobile);
        databaseReference.child(userId).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SignUp.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SignUp.this, "Database error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public class User {
        public String username;
        public String email;
        public String mobile;

        public User() {
        }

        public User(String username, String email, String mobile) {
            this.username = username;
            this.email = email;
            this.mobile = mobile;
        }
    }
}