package com.example.she;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomePage extends Fragment {

    private static final int SMS_PERMISSION_CODE = 1;
    private static final int LOCATION_PERMISSION_CODE = 2;
    private static final String EMERGENCY_CONTACTS_NODE = "EmergencyContacts";
    private static final String SOS_MESSAGE = "I need help! Please reach out to me immediately.";

    private FusedLocationProviderClient locationClient;
    private ProgressBar progressBar;
    private ArrayList<EmergencyContactsActivity.EmergencyContact> contactList;
    private String currentUserId;
    private int volumeDownPressCount = 0;
    private long lastPressTime = 0;
    private static final int MAX_PRESS_COUNT = 3;
    private static final long PRESS_TIMEOUT = 2000;
    private Button btnSOS;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        btnSOS = view.findViewById(R.id.btn_sos);
        progressBar = view.findViewById(R.id.progressBar);
        locationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        contactList = new ArrayList<>();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
            fetchEmergencyContacts();
        } else {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
        }

        btnSOS.setOnClickListener(v -> sendSOSWithLocation());

        return view;
    }

    private void fetchEmergencyContacts() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(EMERGENCY_CONTACTS_NODE).child(currentUserId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    EmergencyContactsActivity.EmergencyContact contact = child.getValue(EmergencyContactsActivity.EmergencyContact.class);
                    if (contact != null) {
                        contactList.add(contact);
                    }
                }
                progressBar.setVisibility(View.GONE);
                if (contactList.isEmpty()) {
                    Toast.makeText(requireContext(), "No emergency contacts found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to fetch contacts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSOSWithLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            locationClient.getLastLocation().addOnSuccessListener(location -> {
                String locationMessage = SOS_MESSAGE;
                if (location != null) {
                    locationMessage += "\nLocation: https://www.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
                } else {
                    locationMessage += "\nLocation: Not available";
                }
                sendMessageToContacts(locationMessage);
            }).addOnFailureListener(e -> {
                sendMessageToContacts(SOS_MESSAGE + "\nLocation: Not available");
                Toast.makeText(requireContext(), "Failed to fetch location", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void sendMessageToContacts(String message) {
        if (contactList.isEmpty()) {
            Toast.makeText(requireContext(), "No emergency contacts to send SOS", Toast.LENGTH_SHORT).show();
            return;
        }

        for (EmergencyContactsActivity.EmergencyContact contact : contactList) {
            try {
                SmsManager.getDefault().sendTextMessage(contact.getPhoneNumber(), null, message, null, null);
                Toast.makeText(requireContext(), "SOS sent to " + contact.getName(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Failed to send SOS to " + contact.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSOSWithLocation();
            } else {
                Toast.makeText(requireContext(), "SMS permission denied. Cannot send SOS", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSOSWithLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission denied. Sending SOS without location", Toast.LENGTH_SHORT).show();
                sendMessageToContacts(SOS_MESSAGE + "\nLocation: Not available");
            }
        }
    }


    public void handleVolumeButtonPress(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            long currentTime = System.currentTimeMillis();


            if (currentTime - lastPressTime > PRESS_TIMEOUT) {
                volumeDownPressCount = 0;
            }

            volumeDownPressCount++;

            if (volumeDownPressCount == MAX_PRESS_COUNT) {
                btnSOS.performClick();
                volumeDownPressCount = 0;
            }

            lastPressTime = currentTime;
        }
    }
}