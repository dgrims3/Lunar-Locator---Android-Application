package com.example.solarcalc.models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.github.bfsmith.geotimezone.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public interface TimeCalc extends AngleCalc {
    enum coords {LAT, LNG}

    default double calcTimeJulianCent(double jd) {
        return (jd - 2451545.0) / 36525.0;
    }

    default double getJDFromCalenderDate(int year, int month, double day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double A = Math.floor(year / 100);
        double B = 2 - A + Math.floor(A / 4);
        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;
    }

    // page 64
    @RequiresApi(api = Build.VERSION_CODES.O)
    default LocalDate getCalendarDateFromJD(double jd) {
        jd += .5;
        double A;
        double B;
        double C;
        double D;
        double E;
        double F = (double) jd % 1;
        double Z = Math.floor(jd);
        if (Z >= 2299161) {
            double val = Math.floor(((Z - 1867216.25) / 36524.25));
            A = Math.floor(Z + 1 + val - Math.floor((val / 4)));
        } else {
            A = Z;
        }
        B = A + 1524;
        C = Math.floor(((B - 122.1) / 365.25));
        D = Math.floor(365.25 * C);
        E = Math.floor((B - D) / 30.6001);

        int Day = (int) Math.floor(B - D - (30.6001 * E) + F);
        int Month = (int) Math.floor(E < 14 ? E - 1 : E - 13);
        int Year = (int) Math.floor(Month > 2 ? C - 4716 : C - 4715);
        return LocalDate.of(Year, Month, Day);
    }

    default double greenwichMeanSiderealTime(double jce) {
        return limit_degrees(((((float) 1 / 38710000) * jce + .000387933) * jce + 36000.770053608) * jce + 100.46061837);
    }

    default double siderealTimeAtInstantAtGreenwichInDegrees(double siderealTime, double fractionOfDay) {
        return limit_degrees(siderealTime + (360.985647 * fractionOfDay));
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    default String dateTimeToString(ZonedDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss ");
        String formattedDateTime = time.format(formatter);
        return formattedDateTime;
    }

    default void fractionOfDayToTime (double fractionOfDay) {
        double doubleHour = fractionOfDay * 24;
        int hour = (int) Math.floor(doubleHour);
        double doubleMinute = 60 * (doubleHour - hour);
        int minute = (int) Math.floor(doubleMinute);
        System.out.println(Integer.toString(hour) + ":" + Integer.toString(minute));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    default Object localFractionOfDayFromUTCToLocal(double fractionOfDay, LocalDate date, double[] latlng, boolean returnAsFraction) {
        TimeZoneLookup timeZoneLookup = new TimeZoneLookup();
        ZoneId zoneId = ZoneId.of(timeZoneLookup.getTimeZone(latlng[coords.LAT.ordinal()], latlng[coords.LNG.ordinal()]).getResult());

        double doubleHour = fractionOfDay * 24;
        int hour = (int) Math.floor(doubleHour);
        double doubleMinute = 60 * (doubleHour - hour);
        int minute = (int) Math.floor(doubleMinute);
        ZonedDateTime dateTimeInUTC = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), hour, minute)
                .atZone(ZoneId.of("UTC"));

        ZonedDateTime inLocalDateTime = dateTimeInUTC.withZoneSameInstant(zoneId);
        if (returnAsFraction) {
            float utcHour = dateTimeInUTC.getHour();
            float utcMinute = dateTimeInUTC.getMinute() / 60;
            utcHour += utcMinute;
            if (date.getDayOfMonth() < inLocalDateTime.getDayOfMonth()) {
                utcHour += 24;
            }
            if (date.getDayOfMonth() > inLocalDateTime.getDayOfMonth()) {
                utcHour -= 24;
            }
            return utcHour / 24;
        }
        return inLocalDateTime;

    }

}
