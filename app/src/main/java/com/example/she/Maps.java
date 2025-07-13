package com.example.she;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Maps extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String FIREBASE_URL = "https://womensafety-d8f80-default-rtdb.firebaseio.com/live_locations/";

    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private ProgressBar progressBar;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private String userID;
    private DatabaseReference locationRef;
    private boolean isSharingLocation = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "unknown_user";

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    updateMapLocation(currentLocation);
                    shareLocationToFirebase(currentLocation);
                }
            }
        };

        locationRef = FirebaseDatabase.getInstance().getReference("live_locations").child(userID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progress_bar);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button shareLocationButton = view.findViewById(R.id.btn_share_location);
        shareLocationButton.setOnClickListener(v -> {
            if (isSharingLocation) {
                stopLocationUpdates();
                shareLocationButton.setText("Share Live Location");
            } else {
                startLocationUpdates();
                shareLiveLocationLink();
                shareLocationButton.setText("Stop Sharing");
            }
            isSharingLocation = !isSharingLocation;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (checkLocationPermission()) {
            enableUserLocation();
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void enableUserLocation() {
        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            progressBar.setVisibility(View.VISIBLE);

            fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    updateMapLocation(currentLocation);
                } else {
                    Toast.makeText(requireContext(), "Unable to fetch location.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateMapLocation(LatLng location) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    private void startLocationUpdates() {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            Toast.makeText(requireContext(), "Sharing live location...", Toast.LENGTH_SHORT).show();
        } else {
            requestLocationPermission();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Toast.makeText(requireContext(), "Stopped sharing live location.", Toast.LENGTH_SHORT).show();
    }

    private void shareLocationToFirebase(LatLng location) {
        long timestamp = System.currentTimeMillis();

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", location.latitude);
        locationData.put("longitude", location.longitude);
        locationData.put("timestamp", timestamp);

        locationRef.setValue(locationData)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Location shared successfully"))
                .addOnFailureListener(e -> Log.e("Firebase", "Error sharing location", e));
    }

    private void shareLiveLocationLink() {
        String liveLocationUrl = FIREBASE_URL + userID + ".json";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Live Location");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "User ID: "+ userID +"  Track my live location here: " + "https://razz-62.github.io/web__map/");

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    enableUserLocation();
                    startLocationUpdates();
                }
            } else {
                Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}