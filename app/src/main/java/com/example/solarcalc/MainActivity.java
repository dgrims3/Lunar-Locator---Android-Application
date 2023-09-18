package com.example.solarcalc;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.solarcalc.models.TimeCalc;
import com.example.solarcalc.models.User;

import java.time.LocalTime;


public class MainActivity extends AppCompatActivity implements TimeCalc {
    private final static int REQUEST_CODE = 99;
    PackageManager packageManager;
    TextView solar_noon_text_view, moon_rise_text_view;
    User user;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageManager = getPackageManager();
        user = new User(this);
        solar_noon_text_view = findViewById(R.id.solar_noon_text_view);

        checkLocationPermission();
        solar_noon_text_view.setText(displayTimeBeautifier(user.calcSolNoon(user.getJD(), -95.852966, user.getOffSet())).toString());
        user.getMoonLatLngDist(user.getJD());

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