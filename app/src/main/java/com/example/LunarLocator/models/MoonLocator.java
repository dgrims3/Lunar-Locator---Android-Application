package com.example.LunarLocator.models;

import android.content.Context;

import java.util.TimeZone;


public class MoonLocator implements SolarCalc, LunarCalc {
    private final int offSet;

    public MoonLocator() {

        this.offSet = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3600000; // milliseconds to hours
    }

    public int getOffSet() {
        return offSet;
    }

}