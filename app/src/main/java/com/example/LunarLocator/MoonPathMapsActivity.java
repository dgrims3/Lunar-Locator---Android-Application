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
import android.view.View;
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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Locale;

public class MoonPathMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    enum coords {LAT, LNG, DISTANCE}

    private GoogleMap mMap;
    private MoonLocator moon_locator;
    private LocalDate local_date;
    private DatePicker date_picker;
    private ImageButton back_button;
    private double[][] twenty_four_hour_moon_lat_long;
    private double[] currentMoonPosition;
    private boolean is_processing_event;
    private ArrayList<Double> observerLatLng;
    @Nullable
    private Marker observerMarker, currentMoonMarker;
    private MarkerOptions[] markerOptionArrayByHour;
    private String[] azimuthAndAltitudeByHour, moonLatAndLongByHour, illuminationByHour, distanceByHour, markerSnippetByHour;
    private String currentMoonPositionMarkerSnippet;
    private Marker[] markerArrayByHour;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.LunarLocator.databinding.ActivityMoonPathMapsBinding binding = ActivityMoonPathMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intentFromMain = getIntent();
        Serializable serializableExtra = intentFromMain.getSerializableExtra("latLng");
        if (serializableExtra instanceof ArrayList) {
            observerLatLng = (ArrayList<Double>) serializableExtra;
        }
        Serializable serializableDate = intentFromMain.getSerializableExtra("date");
        if (serializableDate instanceof LocalDate) {
            local_date = (LocalDate) serializableDate;
        } else {
            local_date = LocalDate.now();
        }

        back_button = findViewById(R.id.backButton);
        date_picker = findViewById(R.id.mapMenuDatePicker);

        moon_locator = new MoonLocator();
        is_processing_event = false;
        currentMoonPosition = moon_locator.getCurrentMoonLatLongDistance(moon_locator.getOffSet());
        markerArrayByHour = new Marker[24];
        markerOptionArrayByHour = new MarkerOptions[24];
        azimuthAndAltitudeByHour = new String[24];
        moonLatAndLongByHour = new String[24];
        markerSnippetByHour = new String[24];
        illuminationByHour = new String[24];
        distanceByHour = new String[24];
        for (int i = 0; i < 24; i++) {
            azimuthAndAltitudeByHour[i] = "";
            moonLatAndLongByHour[i] = "";
            markerSnippetByHour[i] = "";
            illuminationByHour[i] = "";
            distanceByHour[i] = "";
        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        date_picker.init(local_date.getYear(), local_date.getMonthValue() - 1,// Convert to Calendar month index from LocalDate index
                local_date.getDayOfMonth(), (view, year1, month1, day1) -> {
                    if (is_processing_event) {
                        return;
                    }
                    is_processing_event = true;
                    mMap.clear();
                    if (observerMarker != null) {
                        LatLng latLng = new LatLng(observerLatLng.get(0), observerLatLng.get(1));
                        observerMarker = mMap.addMarker(new MarkerOptions().title("Observation Location").position(latLng).snippet(String.format(Locale.getDefault(), "Lat/Long: %s°, %s°", trimLatLngDoubleAndReturnString(this.observerLatLng.get(0)), trimLatLngDoubleAndReturnString(this.observerLatLng.get(1)))).draggable(true));
                    }
                    this.local_date = LocalDate.from(LocalDate.of(year1, month1 + 1, // Convert from Calendar month index to LocalDate index
                            day1).atStartOfDay());
                    twenty_four_hour_moon_lat_long = moon_locator.getTwentyFourHourMoonLatLongDistanceAtHourIntervals(this.local_date, moon_locator.getOffSet());
                    plotTwentyFourHourMoonLatLong(twenty_four_hour_moon_lat_long);
                    if (this.local_date.equals(LocalDate.now())) {
                        plotCurrentMoonLatLong(currentMoonPosition);
                    }
                    updateAllSnippets();
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

        if (observerLatLng != null && observerLatLng.size() >= 2) {
            LatLng observerLatLng = new LatLng(this.observerLatLng.get(0), this.observerLatLng.get(1));
            observerMarker = mMap.addMarker(new MarkerOptions().title("Observation Location").position(observerLatLng).snippet(String.format(Locale.getDefault(), "Lat/Long: %s°, %s°", trimLatLngDoubleAndReturnString(this.observerLatLng.get(0)), trimLatLngDoubleAndReturnString(this.observerLatLng.get(1)))).draggable(true));
            buildTwentyFourHourAltitudeAndAzimuth();
        }
        twenty_four_hour_moon_lat_long = moon_locator.getTwentyFourHourMoonLatLongDistanceAtHourIntervals(local_date, moon_locator.getOffSet());
        plotTwentyFourHourMoonLatLong(twenty_four_hour_moon_lat_long);
        if (local_date.equals(LocalDate.now())) {
            plotCurrentMoonLatLong(currentMoonPosition);
        }

        LatLng startPosition = new LatLng(55, currentMoonPosition[coords.LNG.ordinal()]);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startPosition));

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                observerLatLng.set(0, marker.getPosition().latitude);
                observerLatLng.set(1, marker.getPosition().longitude);
                marker.setSnippet(String.format(Locale.getDefault(), "Lat/Long: %s°, %s°", trimLatLngDoubleAndReturnString(marker.getPosition().latitude), trimLatLngDoubleAndReturnString(marker.getPosition().longitude)));
                updateAllSnippets();
            }

            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {

            }
        });

        mMap.setOnCameraMoveListener(() -> {
            if (mMap.getCameraPosition().bearing != 0) {
                findViewById(R.id.toolbar).setVisibility(View.INVISIBLE);
            } else {
                findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMapClick(@NonNull LatLng selectedLatLng) {
        if (observerMarker == null) {
            observerMarker = mMap.addMarker(new MarkerOptions()
                    .title("Observation Location").position(selectedLatLng)
                    .snippet(String.format(Locale.getDefault(), "Lat/Long %s°, %s°", trimLatLngDoubleAndReturnString(selectedLatLng.latitude), trimLatLngDoubleAndReturnString(selectedLatLng.longitude))).draggable(true));
            if (observerMarker != null) {
                observerLatLng.add(0, observerMarker.getPosition().latitude);
                observerLatLng.add(1, observerMarker.getPosition().longitude);
            }
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
        currentMoonMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentMoonPosition[0], currentMoonPosition[1])).icon(icon).anchor(0.5f, 0.5f).zIndex(1).title("Current Location").snippet(currentMoonPositionMarkerSnippet));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void plotTwentyFourHourMoonLatLong(double[][] twentyFourHourMoonLatLongDistance) {
        LatLng lastLocation = null;
        for (int i = 0; i < 24; i++) {
            LatLng location = new LatLng(twentyFourHourMoonLatLongDistance[i][coords.LAT.ordinal()], twentyFourHourMoonLatLongDistance[i][coords.LNG.ordinal()]);
            LocalTime time = moon_locator.fractionOfDayToLocalTime(i / 24.0);
            moonLatAndLongByHour[i] = String.format(Locale.getDefault(), "Lat/Long: %s°, %s°\n", trimLatLngDoubleAndReturnString(location.latitude), trimLatLngDoubleAndReturnString(location.longitude));
            illuminationByHour[i] = getIllumination(LocalDateTime.of(local_date.getYear(), local_date.getMonthValue(), local_date.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond()));
            distanceByHour[i] = String.format(Locale.getDefault(), "Distance: %d km\n", (int) twentyFourHourMoonLatLongDistance[i][coords.DISTANCE.ordinal()]);
            String title = String.format(Locale.getDefault(), "Time: %tT", time);
            buildTwentyFourHourMarkerSnippet();
            markerOptionArrayByHour[i] = new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromBitmap(getDotBitmap())).anchor(0.5f, 0.5f).title(title).snippet(markerSnippetByHour[i]);
            markerArrayByHour[i] = mMap.addMarker(markerOptionArrayByHour[i]);

            // Add line between the two locations
            if (lastLocation != null) {
                mMap.addPolyline(new PolylineOptions().add(location, lastLocation).width(5).color(getColor(R.color.button_color_secondary)));
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
                String azimuthAndAltitudeFormattedString = String.format(Locale.getDefault(), "Azimuth: %d°\nAltitude: %d°\n", (int) azimuthAndAltitude[0], (int) azimuthAndAltitude[1]);
                azimuthAndAltitudeByHour[i] = azimuthAndAltitudeFormattedString;
            }
        }
    }

    private void buildTwentyFourHourMarkerSnippet() {
        for (int i = 0; i < 24; i++) {
            markerSnippetByHour[i] = azimuthAndAltitudeByHour[i] + illuminationByHour[i] + distanceByHour[i] + moonLatAndLongByHour[i];
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void buildCurrentMoonPositionMarkerSnippet() {
        String moonLatAndLongString = String.format(Locale.getDefault(), "Lat/Long: %s°, %s°\n", trimLatLngDoubleAndReturnString(currentMoonPosition[coords.LAT.ordinal()]), trimLatLngDoubleAndReturnString(currentMoonPosition[coords.LNG.ordinal()]));
        String illuminationString = getIllumination(LocalDateTime.now());
        String distanceString = String.format(Locale.getDefault(), "Distance: %d km\n", (int) currentMoonPosition[coords.DISTANCE.ordinal()]);
        String azimuthAndAltitudeString = "";
        if (observerMarker != null) {
            double[] azimuthAndAltitude = moon_locator.getAzimuthAndAltitudeForMoonAtInstant(LocalDateTime.now(), moon_locator.getOffSet(), observerMarker.getPosition().latitude, observerMarker.getPosition().longitude);
            azimuthAndAltitudeString = String.format(Locale.getDefault(), "Azimuth: %d°\nAltitude: %d°\n", (int) azimuthAndAltitude[0], (int) azimuthAndAltitude[1]);
        }
        currentMoonPositionMarkerSnippet = azimuthAndAltitudeString + illuminationString + distanceString + moonLatAndLongString;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateAllSnippets() {
        if (local_date.equals(LocalDate.now())) {
            buildCurrentMoonPositionMarkerSnippet();
            if (currentMoonMarker != null) {
                currentMoonMarker.setSnippet(currentMoonPositionMarkerSnippet);
            }
        }
        buildTwentyFourHourAltitudeAndAzimuth();
        buildTwentyFourHourMarkerSnippet();
        for (int i = 0; i < 24; i++) {
            markerArrayByHour[i].setSnippet(markerSnippetByHour[i]);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getIllumination(LocalDateTime localDateTime) {
        localDateTime = localDateTime.minusHours(moon_locator.getOffSet());
        double fractionOfDay = moon_locator.localDateTimeToFractionOfDay(localDateTime);
        double jd = moon_locator.getJDFromCalenderDate(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth());
        return String.format(Locale.getDefault(), "Illumination: %d%%\n", (int) moon_locator.moonIlluminatedFractionOfDisk(jd + fractionOfDay));
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

    public String trimLatLngDoubleAndReturnString(double inputDouble) {
        String inputString = String.valueOf(inputDouble);
        int decimalIndex = inputString.indexOf('.');
        if (decimalIndex == -1) {
            return inputString;
        }
        int end = decimalIndex + 5;
        if (end >= inputString.length()) {
            return inputString;
        }
        return inputString.substring(0, end);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onDestroy() {
        super.onDestroy();
        date_picker.setOnDateChangedListener(null);
        mMap.clear();
        back_button.setOnClickListener(null);
    }


}