package com.example.solarcalc.models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalTime;

public interface TimeCalc {
    default double calcTimeJulianCent(double jd) {
        double t = (jd - 2451545.0) / 36525.0;
        return t;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    default LocalTime displayTimeBeautifier(double minutes) {
        double doubleHour = minutes / 60;
        int hour = (int) Math.floor(doubleHour);
        double doubleMinute = 60 * (doubleHour - hour);
        int minute = (int) Math.floor(doubleMinute);
        double doubleSecond = 60 * (doubleMinute - minute);
        int second = (int) Math.floor(doubleSecond);
        return LocalTime.of(hour, minute, second);
    }
}
