package com.example.LunarLocator;

import android.app.DatePickerDialog;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.LunarLocator.databinding.ActivityMoonPathMapsBinding;
import com.example.LunarLocator.models.MoonLocator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Locale;

public class MoonPathMapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private MoonLocator moon_locator;
    private LocalDate local_date;
    private DatePicker date_picker;
    private ImageButton back_button;
    private double[][] twenty_four_hour_moon_lat_long;
    private boolean is_processing_event;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.LunarLocator.databinding.ActivityMoonPathMapsBinding binding = ActivityMoonPathMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        back_button = findViewById(R.id.backButton);

        moon_locator = new MoonLocator(this);
        local_date = LocalDate.now();
        date_picker = findViewById(R.id.mapMenuDatePicker);
        is_processing_event = false;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        date_picker.init(local_date.getYear(), local_date.getMonthValue() -1, local_date.getDayOfMonth(), (view, year1, month1, day1) -> {
            if (is_processing_event) {
                return;
            }
            is_processing_event = true;
            mMap.clear();

            local_date = LocalDate.from(LocalDate.of(year1, month1 + 1, day1).atStartOfDay());
            twenty_four_hour_moon_lat_long = moon_locator.getTwentyFourHourMoonLatLongAtHourIntervals(local_date, moon_locator.getOffSet());
            plotTwentyFourHourMoonLatLong(twenty_four_hour_moon_lat_long);
            if (local_date.equals(LocalDate.now())) {
                plotCurrentMoonLatLong();
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

        twenty_four_hour_moon_lat_long = moon_locator.getTwentyFourHourMoonLatLongAtHourIntervals(local_date, moon_locator.getOffSet());
        plotCurrentMoonLatLong();
        plotTwentyFourHourMoonLatLong(twenty_four_hour_moon_lat_long);

        LatLng startPosition = new LatLng(-95, 45);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startPosition));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void plotCurrentMoonLatLong() {
        double[] currentMoonPosition = moon_locator.getCurrentMoonLatLong(moon_locator.getOffSet());
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.moon_icon);
        assert drawable != null;
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        int width = (int) Math.floor(bitmap.getWidth() * .7);
        int height = (int) Math.floor( bitmap.getHeight() * .7);
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(currentMoonPosition[0], currentMoonPosition[1]))
                .icon(icon)
                .anchor(0.5f, 0.5f)
                .zIndex(1)
                .title("Current Location")
                .snippet(String.format(Locale.getDefault(), "%f, %f", currentMoonPosition[0], currentMoonPosition[1])));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void plotTwentyFourHourMoonLatLong(double[][] twentyFourHourMoonLatLong) {
        LatLng lastLocation = null;
        for (int i = 0; i < 24; i++) {
            double[] latLng = twentyFourHourMoonLatLong[i];
            LatLng location = new LatLng(latLng[0], latLng[1]);
            LocalTime time = moon_locator.fractionOfDayToLocalTime(i/24.0);
            String markerSnippet = String.format(Locale.getDefault(), "%f, %f", location.latitude, location.longitude);
            String title = String.format(Locale.getDefault() ,"%tT", time);
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(getDotBitmap()))
                    .anchor(0.5f, 0.5f)
                    .title(title)
                    .snippet(markerSnippet));

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