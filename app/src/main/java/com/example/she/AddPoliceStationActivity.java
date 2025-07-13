package com.example.she;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddPoliceStationActivity extends AppCompatActivity {

    private Spinner districtSpinner;
    private EditText areaEditText, stationNameEditText, contactEditText;
    private Button addStationButton;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private ImageButton backbutton;
    private static final String[] DISTRICTS = {
            "Dhaka", "Chattogram", "Rajshahi", "Khulna", "Sylhet",
            "Barishal", "Rangpur", "Mymensingh"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_police_station);

        districtSpinner = findViewById(R.id.districtSpinner);
        areaEditText = findViewById(R.id.areaEditText);
        stationNameEditText = findViewById(R.id.stationNameEditText);
        contactEditText = findViewById(R.id.contactEditText);
        addStationButton = findViewById(R.id.addStationButton);
        progressBar = findViewById(R.id.progressBar);
        backbutton = findViewById(R.id.backButton_PS);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddPoliceStationActivity.this, AdminPanel.class));
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference("PoliceStations");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, DISTRICTS);
        districtSpinner.setAdapter(adapter);

        addStationButton.setOnClickListener(view -> addPoliceStation());
    }

    private void addPoliceStation() {
        String district = districtSpinner.getSelectedItem().toString();
        String area = areaEditText.getText().toString().trim();
        String stationName = stationNameEditText.getText().toString().trim();
        String contact = contactEditText.getText().toString().trim();

        if (TextUtils.isEmpty(area) || TextUtils.isEmpty(stationName) || TextUtils.isEmpty(contact)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String key = databaseReference.child(district).push().getKey();
        PoliceStation policeStation = new PoliceStation(stationName, area, contact);

        assert key != null;
        databaseReference.child(district).child(key).setValue(policeStation)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(AddPoliceStationActivity.this, "Police station added successfully!", Toast.LENGTH_SHORT).show();
                        areaEditText.setText("");
                        stationNameEditText.setText("");
                        contactEditText.setText("");
                    } else {
                        Toast.makeText(AddPoliceStationActivity.this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class PoliceStation {
        public String name, area, contact;

        public PoliceStation() {}

        public PoliceStation(String name, String area, String contact) {
            this.name = name;
            this.area = area;
            this.contact = contact;
        }
    }
}
