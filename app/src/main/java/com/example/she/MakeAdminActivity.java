package com.example.she;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MakeAdminActivity extends AppCompatActivity {

    private LinearLayout userListLayout;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_admin);

        userListLayout = findViewById(R.id.userListLayout);
        progressBar = findViewById(R.id.progressBar);
        ImageButton backbutton = findViewById(R.id.backButton_UL);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        fetchUsers();
    }

    private void fetchUsers() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                userListLayout.removeAllViews();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String username = userSnapshot.child("username").getValue(String.class);
                    String email = userSnapshot.child("email").getValue(String.class);
                    Boolean isAdmin = userSnapshot.child("isAdmin").getValue(Boolean.class);

                    if (userId != null && username != null && email != null && !Boolean.TRUE.equals(isAdmin)) {
                        addUserToView(userId, username, email);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MakeAdminActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addUserToView(String userId, String username, String email) {
        View userView = getLayoutInflater().inflate(R.layout.user_list_item, userListLayout, false);

        TextView userNameTextView = userView.findViewById(R.id.userNameTextView);
        TextView userEmailTextView = userView.findViewById(R.id.userEmailTextView);
        Button makeAdminButton = userView.findViewById(R.id.makeAdminButton);

        userNameTextView.setText(username);
        userEmailTextView.setText(email);

        makeAdminButton.setOnClickListener(view -> makeUserAdmin(userId));

        userListLayout.addView(userView);
    }

    private void makeUserAdmin(String userId) {
        databaseReference.child(userId).child("isAdmin").setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MakeAdminActivity.this, "User promoted to Admin!", Toast.LENGTH_SHORT).show();
                        fetchUsers();
                    } else {
                        Toast.makeText(MakeAdminActivity.this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
