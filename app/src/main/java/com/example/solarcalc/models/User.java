package com.example.solarcalc.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;


public class User implements SolarCalc, LunarCalc {
    private final int offSet;

    public User(Context context) {
        this.offSet = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3600000; // milliseconds to hours
    }

    public int getOffSet() {
        return offSet;
    }

}