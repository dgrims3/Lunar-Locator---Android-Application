package com.example.LunarLocator;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;

import com.example.LunarLocator.databinding.ActivityMoonPathMapsBinding;
import com.example.LunarLocator.models.MoonLocator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;

public class MoonPathMapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private MoonLocator moonLocator;

    private ImageButton backButton;
    LocalDate localDate;
    private ActivityMoonPathMapsBinding binding;
    private ArrayList<Double> latLng;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.LunarLocator.databinding.ActivityMoonPathMapsBinding binding = ActivityMoonPathMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        backButton = findViewById(R.id.backButton);
        moonLocator = new MoonLocator(this);
        localDate = LocalDate.now();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        backButton.setOnClickListener(view -> {
            Intent intentToMain = new Intent(MoonPathMapsActivity.this, MainActivity.class);
            startActivity(intentToMain);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        double[][] twentyFourHourMoonLatLong = getTwentyFourHourMoonLatLongAtHourIntervals(localDate);

        for (int i = 0; i < 23; i++) {
            double[] latLngOne = twentyFourHourMoonLatLong[i];
            double[] latLngTwo = twentyFourHourMoonLatLong[i+1];
            LatLng location1 = new LatLng(latLngOne[0], latLngOne[1]);
            LatLng location2 = new LatLng(latLngTwo[0], latLngTwo[1]);

            mMap.addMarker(new MarkerOptions()
                    .position(location1)
                    .icon(BitmapDescriptorFactory.fromBitmap(getDotBitmap())) // Set custom dot icon
                    .anchor(0.5f, 0.5f)); // Center the dot
            mMap.addMarker(new MarkerOptions()
                    .position(location2)
                    .icon(BitmapDescriptorFactory.fromBitmap(getDotBitmap())) // Set custom dot icon
                    .anchor(0.5f, 0.5f)); // Center the dot

            // Add line between the two locations
            mMap.addPolyline(new PolylineOptions()
                    .add(location1, location2)
                    .width(5)
                    .color(getColor(R.color.button_color_secondary)));
        }

    }
    private Bitmap getDotBitmap() {
        int size = 25;
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
    private double[][] getTwentyFourHourMoonLatLongAtHourIntervals(LocalDate localDate) {
        double[][] twentyFourHourMoonLatLongAtHourIntervals = new double[24][2];
        LocalDateTime localDateTime = localDate.atStartOfDay();
        int offset = moonLocator.getOffSet();
        localDateTime = localDateTime.minusHours(offset);

        for (int i = 0; i < 24; i++) {
            double jd = moonLocator.getJDFromCalenderDate(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth());
            double fractionOfDay = localDateTime.getHour()/24.0;
            System.out.println(localDateTime);
            double[] latLng = moonLocator.getMoonLatAndLongAtInstant(jd,fractionOfDay);
            if (latLng[1] < -180) latLng[1] += 360;
            twentyFourHourMoonLatLongAtHourIntervals[i] = latLng;
            localDateTime = localDateTime.plusHours(1);
        }
        return twentyFourHourMoonLatLongAtHourIntervals;
    }
}