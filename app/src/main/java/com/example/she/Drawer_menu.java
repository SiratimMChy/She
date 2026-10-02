package com.example.she;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Drawer_menu extends AppCompatActivity {
    BottomNavigationView btnNv;
    DrawerLayout drawerLayout;
    ImageButton imageButton;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_menu);

        drawerLayout = findViewById(R.id.drawer_layout);
        imageButton = findViewById(R.id.ib_menu);
        btnNv = findViewById(R.id.BtnNv);
        navigationView = findViewById(R.id.nav_view);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int NvId = item.getItemId();

                if (NvId == R.id.nav_emergency_contacts) {
                    startActivity(new Intent(Drawer_menu.this, EmergencyContactsActivity.class));
                } else if (item.getItemId() == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(Drawer_menu.this, Login.class));
                    finish();
                } else if (item.getItemId() == R.id.nav_police_station) {
                    startActivity(new Intent(Drawer_menu.this, Pos.class));
                } else if (item.getItemId() == R.id.nav_Safety_tips) {
                    loadFrag(new SafetyTips(), false);
                } else if (item.getItemId() == R.id.nav_dev) {
                    startActivity(new Intent(Drawer_menu.this, DeveloperInfoActivity.class));
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        View headerView = navigationView.getHeaderView(0);
        TextView userNameTextView = headerView.findViewById(R.id.user_name_textview);
        TextView userEmailTextView = headerView.findViewById(R.id.user_email_textview);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("username").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);

                        userNameTextView.setText(name);
                        userEmailTextView.setText(email);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    userNameTextView.setText("User Name");
                    userEmailTextView.setText("User Email");
                }
            });
        }

        btnNv.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nv_home) {
                loadFrag(new HomePage(), true);
            } else if (id == R.id.nv_msg) {
                loadFrag(new Message(), false);
            } else if (id == R.id.nv_map) {
                loadFrag(new Maps(), false);
            } else if (id == R.id.nv_Blog) {
                loadFrag(new Blog(), false);
            }
            return true;
        });

        btnNv.setSelectedItemId(R.id.nv_home);

        imageButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));
    }

    public void loadFrag(Fragment fragment, boolean flag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.commit();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof HomePage) {
            ((HomePage) fragment).handleVolumeButtonPress(event);
        }
        return super.dispatchKeyEvent(event);
    }
}