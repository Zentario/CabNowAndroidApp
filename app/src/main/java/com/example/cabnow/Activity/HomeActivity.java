package com.example.cabnow.Activity;


import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cabnow.R;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "HomeActivity";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready");

        mMap = googleMap;

        getDeviceLocation();
        mMap.setMyLocationEnabled(true);
    }

    private void moveCamera(LatLng latLng, float zoom)
    {
        Log.d(TAG, "moveCamera: Moving the camera to " + latLng.latitude + " " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getDeviceLocation()
    {
        Log.d(TAG, "getDeviceLocation: Getting devices current location");

        // CRASHES FROM BELOW
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            Task<Location> location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful())
                    {
                        Log.d(TAG, "onComplete: Found location");
                        Log.d(TAG, "onComplete: " + task.getResult());
                        Location currentLocation = (Location) task.getResult();
                        if(currentLocation != null)
                        {
                            Log.d(TAG, "onComplete: Location found at " + "Latitude : " + currentLocation.getLatitude()
                             + " " + " Longitude : " + currentLocation.getLongitude());
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);

                        }
                        else
                            Toast.makeText(HomeActivity.this, "Current location is NULL", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(HomeActivity.this, "Cannot find your location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch(SecurityException e)
        {
            Log.e(TAG, "onMapReady: " + e.getMessage());
        }

    }

}
