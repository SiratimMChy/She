package com.example.she;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login_Check extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, Login.class));
            finish();
        } else {
            checkUserRole(currentUser.getUid());
        }
    }

    private void checkUserRole(String userId) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Boolean isAdmin = snapshot.child("isAdmin").getValue(Boolean.class);

                    if (isAdmin != null && isAdmin) {
                        startActivity(new Intent(Login_Check.this, AdminPanel.class));
                    } else {
                        startActivity(new Intent(Login_Check.this, Drawer_menu.class));
                    }
                } else {
                    Toast.makeText(Login_Check.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login_Check.this, Login.class));
                }
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Login_Check.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Login_Check.this, Login.class));
                finish();
            }
        });
    }
}
