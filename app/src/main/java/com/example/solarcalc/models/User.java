package com.example.solarcalc.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.TimeZone;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;


public class User implements SolarCalc , LunarCalc{
    private double[] latLng;
    private final int offSet;
    private final FusedLocationProviderClient fusedLocationProviderClient;

    public User(Context context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        this.offSet = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3600000; // milliseconds to hours
        this.latLng = getUserLatLng();
    }

    public int getOffSet() {
        return offSet;
    }

    public double getLat() {
        return latLng[0];
    }

    public double getLng() {
        return latLng[1];
    }

    public double getJD() {
        Calendar calender = Calendar.getInstance();
        int year = 1992; // calender.get(Calendar.YEAR);
        int month = 4 ;// calender.get(Calendar.MONTH) + 1; //class starts month count at 0
        int day = 12; // calender.get(Calendar.DAY_OF_MONTH);
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double A = Math.floor(year / 100);
        double B = 2 - A + Math.floor(A / 4);
        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;
    }

    @SuppressLint("MissingPermission")
    private double[] getUserLatLng() {
        this.fusedLocationProviderClient.getCurrentLocation(102, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnSuccessListener(location -> latLng = new double[]{location.getLatitude(), location.getLongitude()});
        return latLng;
    }
}