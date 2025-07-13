package com.example.she;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminPanel extends AppCompatActivity {

    private Button makeAdminBtn, addPoliceStationBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        makeAdminBtn = findViewById(R.id.makeAdminBtn);
        addPoliceStationBtn = findViewById(R.id.addPoliceStationBtn);
        logoutBtn = findViewById(R.id.logoutBtn);


        makeAdminBtn.setOnClickListener(view -> {
            Intent intent = new Intent(AdminPanel.this, MakeAdminActivity.class);
            startActivity(intent);
        });


        addPoliceStationBtn.setOnClickListener(view -> {
            Intent intent = new Intent(AdminPanel.this, AddPoliceStationActivity.class);
            startActivity(intent);
        });


        logoutBtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminPanel.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
