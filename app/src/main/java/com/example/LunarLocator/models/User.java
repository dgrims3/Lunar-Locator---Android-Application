package com.example.LunarLocator.models;

import android.content.Context;

import java.util.TimeZone;


public class User implements SolarCalc, LunarCalc {
    private final int offSet;

    public User(Context context) {
        this.offSet = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3600000; // milliseconds to hours
    }

    public int getOffSet() {
        return offSet;
    }

}