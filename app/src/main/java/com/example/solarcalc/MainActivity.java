package com.example.solarcalc;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.example.solarcalc.models.TimeCalc;
import com.example.solarcalc.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity implements TimeCalc {

    enum time {RISING, TRANSIT, SETTING}
    enum coords {LAT,LNG}

    private final static int REQUEST_CODE = 99;
    PackageManager packageManager;
    TextView solar_noon_text_view, moon_rise_text_view, moon_transit_text_view, moon_set_text_view;
    SwitchCompat location_toggle;
    LinearLayout custom_input;
    User user;
    Calendar today;
    List<ZonedDateTime> moonTimes;
    FusedLocationProviderClient fusedLocationProviderClient;
    double[] userLatLng, customLatLng;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageManager = getPackageManager();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_main);

        user = new User(this);

        location_toggle = findViewById(R.id.location_toggle);
        custom_input = findViewById(R.id.custom_input);
        solar_noon_text_view = findViewById(R.id.solar_noon_text_view);
        moon_rise_text_view = findViewById(R.id.moon_rise_text_view);
        moon_transit_text_view = findViewById(R.id.moon_transit_text_view);
        moon_set_text_view = findViewById(R.id.moon_set_text_view);
        today = Calendar.getInstance();
        custom_input.setVisibility(View.GONE);
        location_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // Handle the switch state change here
                        if (isChecked) {
                            custom_input.setVisibility(View.VISIBLE);
                        } else {
                            custom_input.setVisibility(View.GONE);
                        }
                    }
                });
        getUserLocationAndCalculateMoonTimes(new LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                // Handle the received location
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // Calculate moon times using received location
                moonTimes = user.calcMoonTimes(latitude, longitude,
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH) + 1,
                        today.get(Calendar.DAY_OF_MONTH));

                solar_noon_text_view.setText(displayTimeBeautifier(user.calcSolNoon(-95.848160, user.getOffSet(), getJDFromCalenderDate(
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH) + 1,
                        today.get(Calendar.DAY_OF_MONTH)))).toString());
                moon_rise_text_view.setText(dateTimeToString(moonTimes.get(time.RISING.ordinal())));
                moon_transit_text_view.setText(dateTimeToString(moonTimes.get(time.TRANSIT.ordinal())));
                moon_set_text_view.setText(dateTimeToString(moonTimes.get(time.SETTING.ordinal())));
            }
            @Override
            public void onLocationError(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserLocationAndCalculateMoonTimes(final LocationCallback callback){
        checkLocationPermission();
        fusedLocationProviderClient.getCurrentLocation(102, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnSuccessListener(location -> {
            if (location != null) {
                // Pass received location to the callback
                callback.onLocationReceived(location);
            } else {
                // Notify callback about location retrieval error
                callback.onLocationError("Unable to retrieve location");
            }
        });
    }

    interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationError(String errorMessage);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton(
                                "OK",
                                (dialog, which) -> requestLocationPermission())
                        .create()
                        .show();
            } else {
                requestLocationPermission();
            }
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_CODE
        );
    }

}