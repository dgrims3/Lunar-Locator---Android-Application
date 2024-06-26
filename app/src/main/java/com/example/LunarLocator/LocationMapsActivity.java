package com.example.LunarLocator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.LunarLocator.databinding.ActivityLocationMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

public class LocationMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private ArrayList<Double> latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.LunarLocator.databinding.ActivityLocationMapsBinding binding = ActivityLocationMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intentFromMain = getIntent();
        Serializable serializableArray = intentFromMain.getSerializableExtra("latLng");
        if (serializableArray instanceof ArrayList) {
            latLng = (ArrayList<Double>) serializableArray;
        }

        RelativeLayout map_select_button = findViewById(R.id.map_select_button);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        map_select_button.setOnClickListener(view -> {
            Intent intentToMain = new Intent(LocationMapsActivity.this, MainActivity.class);
            intentToMain.putExtra("latLng", latLng);
            startActivity(intentToMain);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng userLatLng = new LatLng(latLng.get(0), latLng.get(1));
        mMap.addMarker(new MarkerOptions().position(userLatLng).title(String.format(Locale.getDefault(), "%f, %f", latLng.get(0), latLng.get(1))));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
        mMap.setOnMapClickListener(this);

        mMap.setOnCameraMoveListener(() -> {
            if (mMap.getCameraPosition().bearing != 0) {
                findViewById(R.id.toolbar).setVisibility(View.INVISIBLE);
            } else {
                findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public void onMapClick(@NonNull LatLng selectedLatLng) {
        mMap.clear();
        latLng.add(0, selectedLatLng.latitude);
        latLng.add(1, selectedLatLng.longitude);
        mMap.addMarker(new MarkerOptions().position(selectedLatLng).title(String.format(Locale.getDefault(), "%f, %f", latLng.get(0), latLng.get(1))));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(selectedLatLng));
    }

}