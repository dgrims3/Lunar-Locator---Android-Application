package com.example.LunarLocator;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.LunarLocator.models.MoonLocator;
import com.example.LunarLocator.models.TimeCalc;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;


public class MainActivity extends AppCompatActivity implements TimeCalc {

    private final static int REQUEST_CODE = 99;
    private TextView main_text_view_first, main_text_view_second, main_text_view_third, main_text_view_fourth,
            main_text_view_title_first, main_text_view_title_second, main_text_view_title_third, main_text_view_title_fourth,
            latitude_text_view, longitude_text_view, date_text_view;
    private MoonLocator moonLocator;
    private Calendar selectedDate;
    private Map<ZonedDateTime, String> moonTimes;
    public ArrayList<Double> latLng;
    private TextView[][] sortedTextViews;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        Intent intentFromLocationMap = getIntent();
        moonLocator = new MoonLocator(this);

        latLng = new ArrayList<>();
        selectedDate = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        AppCompatButton get_date_picker_button = findViewById(R.id.get_date_picker_button);
        AppCompatButton get_map_button = findViewById(R.id.get_map_button);
        AppCompatButton calculate_moon_position_button = findViewById(R.id.calculate_moon_position_button);
        AppCompatButton view_moon_on_map_button = findViewById(R.id.view_moon_on_map_button);

        main_text_view_title_first = findViewById(R.id.main_text_view_title_first);
        main_text_view_first = findViewById(R.id.main_text_view_first);
        main_text_view_title_second = findViewById(R.id.main_text_view_title_second);
        main_text_view_second = findViewById(R.id.main_text_view_second);
        main_text_view_title_third = findViewById(R.id.main_text_view_title_third);
        main_text_view_third = findViewById(R.id.main_text_view_third);
        main_text_view_title_fourth = findViewById(R.id.main_text_view_title_fourth);
        main_text_view_fourth = findViewById(R.id.main_text_view_fourth);

        latitude_text_view = findViewById(R.id.latitude_text_view);
        longitude_text_view = findViewById(R.id.longitude_text_view);
        date_text_view = findViewById(R.id.date_text_view);

        sortedTextViews = new TextView[][]{
                {main_text_view_title_first, main_text_view_first},
                {main_text_view_title_second, main_text_view_second},
                {main_text_view_title_third, main_text_view_third},
                {main_text_view_title_fourth, main_text_view_fourth}
        };

        setDateTextView(selectedDate);

        get_date_picker_button.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(MainActivity.this,
                    (datePicker, i, i1, i2) -> {
                        selectedDate.set(i, i1, i2);
                        setDateTextView(selectedDate);
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        get_map_button.setOnClickListener(view -> {
            Intent intentToLocationMap = new Intent(MainActivity.this, LocationMapsActivity.class);
            if (latLng.isEmpty()) {
                latLng.add(0, 0.0);
                latLng.add(1, 0.0);
            }
            intentToLocationMap.putExtra("latLng", latLng);
            startActivity(intentToLocationMap);
        });

        calculate_moon_position_button.setOnClickListener(view -> {
            if (latitude_text_view.getText().toString().isEmpty() || longitude_text_view.getText().toString().isEmpty()) {
                Toast.makeText(this, "Choose Location or Enable Phone's Location Service", Toast.LENGTH_SHORT).show();
            } else {
                Date date;
                try {
                    date = dateFormat.parse(String.valueOf(date_text_view.getText()));
                } catch (ParseException e) {
                    return;
                }
                Calendar calendar = Calendar.getInstance();
                if (date != null) {
                    calendar.setTime(date);
                } else {
                    calendar = selectedDate;
                }
                moonTimes = moonLocator.moonRisingSettingTransitIterationCaller(Double.parseDouble(String.valueOf(latitude_text_view.getText())), Double.parseDouble(String.valueOf(longitude_text_view.getText())),
                        LocalDate.of(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH)
                        ));
                TreeMap<ZonedDateTime, String> sortedMoonTimes = new TreeMap<>(moonTimes);
                int i = 0;
                for (Map.Entry<ZonedDateTime, String> entry : sortedMoonTimes.entrySet()) {
                    String title = "Moon "+ entry.getValue();
                    String body = dateTimeToString(entry.getKey());
                    sortedTextViews[i][0].setText(title);
                    sortedTextViews[i][1].setText(body);
                    i++;
                }
            }
        });

        view_moon_on_map_button.setOnClickListener(view -> {
            Intent intentToMoonPathMap = new Intent(MainActivity.this, MoonPathMapsActivity.class);
            startActivity(intentToMoonPathMap);
        });

        BroadcastReceiver locationChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.equals(intent.getAction(), LocationManager.PROVIDERS_CHANGED_ACTION)) {
                    getUserLocation(locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationReceived(Location location) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            if (latitude_text_view.getText().toString().isEmpty() &&
                                    longitude_text_view.getText().toString().isEmpty()) {
                                latitude_text_view.setText(String.valueOf(latitude));
                                longitude_text_view.setText(String.valueOf(longitude));
                                latLng.add(0, latitude);
                                latLng.add(1, longitude);
                            }
                        }

                        @Override
                        public void onLocationError(String errorMessage) {
                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
        registerReceiver(locationChangeReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        if (intentFromLocationMap != null && intentFromLocationMap.hasExtra("latLng") && latLng != null) {
            latLng = (ArrayList<Double>) intentFromLocationMap.getSerializableExtra("latLng");

            latitude_text_view.setText(String.valueOf(latLng != null ? latLng.get(0) : null));
            longitude_text_view.setText(String.valueOf(latLng != null ? latLng.get(1) : null));
        } else {
            getUserLocation(new LocationCallback() {
                @Override
                public void onLocationReceived(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    latitude_text_view.setText(String.valueOf(latitude));
                    longitude_text_view.setText(String.valueOf(longitude));
                    latLng.add(0, latitude);
                    latLng.add(1, longitude);
                }

                @Override
                public void onLocationError(String errorMessage) {
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

        swipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            swipeRefreshLayout.setRefreshing(false);
            selectedDate = Calendar.getInstance();
            setDateTextView(selectedDate);
            latLng.clear();
            latitude_text_view.setText("");
            longitude_text_view.setText("");
            main_text_view_title_first.setText(R.string.moon_rise_text_view);
            main_text_view_first.setText("");
            main_text_view_title_second.setText(R.string.moon_transit_text_view);
            main_text_view_second.setText("");
            main_text_view_title_third.setText(R.string.moon_set_text_view);
            main_text_view_third.setText("");
            main_text_view_title_fourth.setText("");
            main_text_view_fourth.setText("");
            getUserLocation(new LocationCallback() {
                @Override
                public void onLocationReceived(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    latitude_text_view.setText(String.valueOf(latitude));
                    longitude_text_view.setText(String.valueOf(longitude));
                    latLng.add(0, latitude);
                    latLng.add(1, longitude);
                }

                @Override
                public void onLocationError(String errorMessage) {
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }, 1000));
    }

    private void setDateTextView(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String formattedDate = String.format("%s-%s-%s", year, month, day);
        date_text_view.setText(formattedDate);
    }

    private void getUserLocation(final LocationCallback callback) {
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
        getUserLocation(new LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                latitude_text_view.setText(String.valueOf(latitude));
                longitude_text_view.setText(String.valueOf(longitude));
                latLng.add(0, latitude);
                latLng.add(1, longitude);
            }

            @Override
            public void onLocationError(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

}