package com.example.she;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pos extends AppCompatActivity {

    private Spinner districtSpinner;
    private Spinner areaSpinner;
    private TextView policeStationTextView;

    private DatabaseReference databaseReference;
    private Map<String, List<String>> districtAreas = new HashMap<>();
    private Map<String, Map<String, PoliceStation>> areaPoliceStations = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);

        districtSpinner = findViewById(R.id.districtSpinner);
        areaSpinner = findViewById(R.id.areaSpinner);
        policeStationTextView = findViewById(R.id.policeStationTextView);
        ImageButton backbutton = findViewById(R.id.backButton);

        backbutton.setOnClickListener(v -> startActivity(new Intent(Pos.this, Drawer_menu.class)));

        databaseReference = FirebaseDatabase.getInstance().getReference("PoliceStations");
        fetchPoliceStationsFromFirebase();
    }

    private void fetchPoliceStationsFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                districtAreas.clear();
                areaPoliceStations.clear();

                for (DataSnapshot districtSnapshot : dataSnapshot.getChildren()) {
                    String district = districtSnapshot.getKey();
                    List<String> areas = new ArrayList<>();
                    Map<String, PoliceStation> stationsInDistrict = new HashMap<>();

                    for (DataSnapshot stationSnapshot : districtSnapshot.getChildren()) {
                        PoliceStation policeStation = stationSnapshot.getValue(PoliceStation.class);
                        if (policeStation != null) {
                            areas.add(policeStation.area);
                            stationsInDistrict.put(policeStation.area, policeStation);
                        }
                    }
                    districtAreas.put(district, areas);
                    areaPoliceStations.put(district, stationsInDistrict);
                }
                populateDistrictSpinner();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Pos.this, "Failed to load police stations: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Firebase Error", databaseError.getMessage());
            }
        });
    }

    private void populateDistrictSpinner() {
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(districtAreas.keySet()));
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        districtSpinner.setAdapter(districtAdapter);

        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDistrict = parent.getItemAtPosition(position).toString();
                populateAreaSpinner(selectedDistrict);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void populateAreaSpinner(String selectedDistrict) {
        List<String> areas = districtAreas.get(selectedDistrict);
        if (areas == null) {
            areas = new ArrayList<>();
        }

        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, areas);
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(areaAdapter);

        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedArea = parent.getItemAtPosition(position).toString();
                String selectedDistrict = districtSpinner.getSelectedItem().toString();
                displayPoliceStationDetails(selectedDistrict, selectedArea);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void displayPoliceStationDetails(String selectedDistrict, String selectedArea) {
        if (areaPoliceStations.containsKey(selectedDistrict)) {
            Map<String, PoliceStation> stationsInDistrict = areaPoliceStations.get(selectedDistrict);
            if (stationsInDistrict.containsKey(selectedArea)) {
                PoliceStation policeStation = stationsInDistrict.get(selectedArea);
                policeStationTextView.setText(String.format("Station: %s\nLocation: %s\nPhone: %s",
                        policeStation.name, policeStation.area, policeStation.contact));
            } else {
                policeStationTextView.setText("Police station details not available for this area.");
            }
        } else {
            policeStationTextView.setText("No police stations found in this district.");
        }
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
