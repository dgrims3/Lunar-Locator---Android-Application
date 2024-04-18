package com.example.LunarLocator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.LunarLocator.databinding.ActivityMoonPathMapsBinding;
import com.example.LunarLocator.models.MoonLocator;
import com.example.LunarLocator.models.utils.CustomInfoWindowAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Locale;

public class MoonPathMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private GoogleMap mMap;
    private MoonLocator moon_locator;
    private LocalDate local_date;
    private DatePicker date_picker;
    private ImageButton back_button;
    private double[][] twenty_four_hour_moon_lat_long;
    private double[] currentMoonPosition;
    private boolean is_processing_event;
    private ArrayList<Double> intentPassedLatLng;
    @Nullable
    private Marker observerMarker, currentMoonMarker;
    private MarkerOptions[] markerOptionArrayByHour;
    private String[] azimuthAndAltitudeByHour, moonLatAndLongByHour, markerSnippetByHour;
    private String currentMoonPositionMarkerSnippet;
    private Marker[] markerArrayByHour;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.LunarLocator.databinding.ActivityMoonPathMapsBinding binding = ActivityMoonPathMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intentFromMain = getIntent();
        intentPassedLatLng = (ArrayList<Double>) intentFromMain.getSerializableExtra("latLng");

        back_button = findViewById(R.id.backButton);
        date_picker = findViewById(R.id.mapMenuDatePicker);

        moon_locator = new MoonLocator(this);
        local_date = LocalDate.now();
        is_processing_event = false;
        currentMoonPosition = moon_locator.getCurrentMoonLatLong(moon_locator.getOffSet());
        markerArrayByHour = new Marker[24];
        markerOptionArrayByHour = new MarkerOptions[24];
        azimuthAndAltitudeByHour = new String[24];
        moonLatAndLongByHour = new String[24];
        markerSnippetByHour = new String[24];
        for (int i = 0; i < 24; i++) {
            azimuthAndAltitudeByHour[i] = "";
            moonLatAndLongByHour[i] = "";
            markerSnippetByHour[i] = "";
        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        date_picker.init(local_date.getYear(), local_date.getMonthValue() - 1, local_date.getDayOfMonth(), (view, year1, month1, day1) -> {
            if (is_processing_event) {
                return;
            }
            is_processing_event = true;
            mMap.clear();

            local_date = LocalDate.from(LocalDate.of(year1, month1 + 1, day1).atStartOfDay());
            twenty_four_hour_moon_lat_long = moon_locator.getTwentyFourHourMoonLatLongAtHourIntervals(local_date, moon_locator.getOffSet());
            plotTwentyFourHourMoonLatLong(twenty_four_hour_moon_lat_long);
            if (local_date.equals(LocalDate.now())) {
                plotCurrentMoonLatLong(currentMoonPosition);
            }
            is_processing_event = false;
        });

        back_button.setOnClickListener(view -> {
            Intent intentToMain = new Intent(MoonPathMapsActivity.this, MainActivity.class);
            startActivity(intentToMain);
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getLayoutInflater()));

        if (intentPassedLatLng != null && intentPassedLatLng.size() == 2) {
            LatLng observerLatLng = new LatLng(intentPassedLatLng.get(0), intentPassedLatLng.get(1));
            observerMarker = mMap.addMarker(new MarkerOptions()
                    .position(observerLatLng)
                    .snippet(String.format(Locale.getDefault(), "Lat/Long: %f, %f", intentPassedLatLng.get(0), intentPassedLatLng.get(1)))
                    .draggable(true));
            buildTwentyFourHourAltitudeAndAzimuth();
        }
        twenty_four_hour_moon_lat_long = moon_locator.getTwentyFourHourMoonLatLongAtHourIntervals(local_date, moon_locator.getOffSet());
        plotCurrentMoonLatLong(currentMoonPosition);
        plotTwentyFourHourMoonLatLong(twenty_four_hour_moon_lat_long);

        LatLng startPosition = new LatLng(55, currentMoonPosition[1]);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startPosition));

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                marker.setSnippet(String.format(Locale.getDefault(), "Lat/Long: %f, %f", marker.getPosition().latitude, marker.getPosition().longitude));
                updateAllSnippets();
            }

            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMapClick(@NonNull LatLng selectedLatLng) {
        if (observerMarker == null) {
            observerMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(selectedLatLng)
                            .snippet(String.format(Locale.getDefault(), "Lat/Long %f, %f", selectedLatLng.latitude, selectedLatLng.longitude))
                            .draggable(true));
            updateAllSnippets();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void plotCurrentMoonLatLong(double[] currentMoonPosition) {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.moon_icon);
        assert drawable != null;
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        int width = (int) Math.floor(bitmap.getWidth() * .7);
        int height = (int) Math.floor(bitmap.getHeight() * .7);
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
        buildCurrentMoonPositionMarkerSnippet();
        currentMoonMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(currentMoonPosition[0], currentMoonPosition[1]))
                .icon(icon)
                .anchor(0.5f, 0.5f)
                .zIndex(1)
                .title("Current Location")
                .snippet(currentMoonPositionMarkerSnippet));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void plotTwentyFourHourMoonLatLong(double[][] twentyFourHourMoonLatLong) {
        LatLng lastLocation = null;
        for (int i = 0; i < 24; i++) {
            double[] latLng = twentyFourHourMoonLatLong[i];
            LatLng location = new LatLng(latLng[0], latLng[1]);
            LocalTime time = moon_locator.fractionOfDayToLocalTime(i / 24.0);
            moonLatAndLongByHour[i] = String.format(Locale.getDefault(), "Lat/Long: %f, %f\n", location.latitude, location.longitude);
            String title = String.format(Locale.getDefault(), "Time: %tT", time);
            buildTwentyFourHourMarkerSnippet();
            markerOptionArrayByHour[i] = new MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(getDotBitmap()))
                    .anchor(0.5f, 0.5f)
                    .title(title)
                    .snippet(markerSnippetByHour[i]);
            markerArrayByHour[i] = mMap.addMarker(markerOptionArrayByHour[i]);

            // Add line between the two locations
            if (lastLocation != null) {
                mMap.addPolyline(new PolylineOptions()
                        .add(location, lastLocation)
                        .width(5)
                        .color(getColor(R.color.button_color_secondary)));
            }
            lastLocation = location;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void buildTwentyFourHourAltitudeAndAzimuth() {
        if (observerMarker != null) {
            for (int i = 0; i < 24; i++) {
                LocalTime time = moon_locator.fractionOfDayToLocalTime(i / 24.0);
                double[] azimuthAndAltitude = moon_locator.getAzimuthAndAltitudeForMoonAtInstant(LocalDateTime.of(local_date.getYear(), local_date.getMonthValue(), local_date.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond()), moon_locator.getOffSet(), observerMarker.getPosition().latitude, observerMarker.getPosition().longitude);
                String azimuthAndAltitudeFormattedString = String.format(Locale.getDefault(), "Azimuth: %f\nAltitude: %f\n", azimuthAndAltitude[0], azimuthAndAltitude[1]);
                azimuthAndAltitudeByHour[i] = azimuthAndAltitudeFormattedString;
            }
        }
    }

    private void buildTwentyFourHourMarkerSnippet() {
        for (int i = 0; i < 24; i++) {
            markerSnippetByHour[i] = azimuthAndAltitudeByHour[i] + moonLatAndLongByHour[i];
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void buildCurrentMoonPositionMarkerSnippet() {
        String moonLatAndLongString = String.format(Locale.getDefault(), "Lat/Long: %f, %f\n", currentMoonPosition[0], currentMoonPosition[1]);
        String azimuthAndAltitudeString = "";
        if (observerMarker != null) {
            double[] azimuthAndAltitude = moon_locator.getAzimuthAndAltitudeForMoonAtInstant(LocalDateTime.now(), moon_locator.getOffSet(), observerMarker.getPosition().latitude, observerMarker.getPosition().longitude);
            azimuthAndAltitudeString = String.format(Locale.getDefault(), "Azimuth: %f\nAltitude: %f\n", azimuthAndAltitude[0], azimuthAndAltitude[1]);
        }
        currentMoonPositionMarkerSnippet = azimuthAndAltitudeString + moonLatAndLongString;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateAllSnippets() {
        buildCurrentMoonPositionMarkerSnippet();
        currentMoonMarker.setSnippet(currentMoonPositionMarkerSnippet);
        buildTwentyFourHourAltitudeAndAzimuth();
        buildTwentyFourHourMarkerSnippet();
        for (int i = 0; i < 24; i++) {
            markerArrayByHour[i].setSnippet(markerSnippetByHour[i]);
        }
    }

    private Bitmap getDotBitmap() {
        int size = 50;
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(Color.BLACK);
        shapeDrawable.setBounds(0, 0, size, size);

        // Convert the shape drawable to a bitmap drawable
        Bitmap dotBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dotBitmap);
        shapeDrawable.draw(canvas);

        return dotBitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onDestroy() {
        super.onDestroy();
        date_picker.setOnDateChangedListener(null);
        mMap = null;
        back_button.setOnClickListener(null);
    }


}