package com.example.LunarLocator;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import com.example.LunarLocator.models.TimeCalc;
import com.example.LunarLocator.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements TimeCalc {

    enum time {RISING, TRANSIT, SETTING}

    private final static int REQUEST_CODE = 99;
    PackageManager packageManager;
    TextView solar_noon_text_view, moon_rise_text_view, moon_transit_text_view, moon_set_text_view, latitude_text_view, longitude_text_view, date_text_view;
    AppCompatButton get_date_picker_button, calculate_moon_position_button, get_map_button;
    User user;
    Calendar today;
    List<ZonedDateTime> moonTimes;
    FusedLocationProviderClient fusedLocationProviderClient;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageManager = getPackageManager();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_main);

        user = new User(this);

        today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        get_date_picker_button = findViewById(R.id.get_date_picker_button);
        get_map_button = findViewById(R.id.get_map_button);
        calculate_moon_position_button = findViewById(R.id.calculate_moon_position_button);
        solar_noon_text_view = findViewById(R.id.solar_noon_text_view);
        moon_rise_text_view = findViewById(R.id.moon_rise_text_view);
        moon_transit_text_view = findViewById(R.id.moon_transit_text_view);
        moon_set_text_view = findViewById(R.id.moon_set_text_view);
        latitude_text_view = findViewById(R.id.latitude_text_view);
        longitude_text_view = findViewById(R.id.longitude_text_view);
        date_text_view = findViewById(R.id.date_text_view);

        setDateTextView(today);


        get_date_picker_button.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(MainActivity.this,
                    (datePicker, i, i1, i2) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(i, i1, i2);
                        setDateTextView(selectedDate);
                    },
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        get_map_button.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        calculate_moon_position_button.setOnClickListener(view -> {
            Date date = null;
            try {
                date = dateFormat.parse(String.valueOf(date_text_view.getText()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            if (date != null) {
                calendar.setTime(date);
            } else {
                calendar = today;
            }
            moonTimes = user.calcMoonTimes(Double.parseDouble(String.valueOf(latitude_text_view.getText())), Double.parseDouble(String.valueOf(longitude_text_view.getText())),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));

            solar_noon_text_view.setText(displayTimeBeautifier(user.calcSolNoon(-95.848160, user.getOffSet(), getJDFromCalenderDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH)))).toString());
            moon_rise_text_view.setText(dateTimeToString(moonTimes.get(time.RISING.ordinal())));
            moon_transit_text_view.setText(dateTimeToString(moonTimes.get(time.TRANSIT.ordinal())));
            moon_set_text_view.setText(dateTimeToString(moonTimes.get(time.SETTING.ordinal())));
        });

        getUserLocationAndCalculateMoonTimes(new LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                latitude_text_view.setText(String.valueOf(location.getLatitude()));
                longitude_text_view.setText(String.valueOf(location.getLongitude()));
            }
            @Override
            public void onLocationError(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDateTextView(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String formattedDate = String.format("%s-%s-%s", year, month, day);
        date_text_view.setText(formattedDate);
    }

    private void getUserLocationAndCalculateMoonTimes(final LocationCallback callback) {
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