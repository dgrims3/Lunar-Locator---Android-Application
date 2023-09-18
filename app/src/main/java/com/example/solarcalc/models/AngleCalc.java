package com.example.solarcalc.models;

public interface AngleCalc {
    double obliquityOfEcliptic = 23.440636; // ε
    double PI = 3.1415926535897932384626433832795028841971;

    default double rad2deg(double radians) {
        return (180.0 / PI) * radians;
    }

    default double deg2rad(double degrees) {
        return (PI / 180.0) * degrees;
    }

    default double limit_degrees(double degrees) {
        double limited;
        degrees /= 360.0;
        limited = 360.0 * (degrees - Math.floor(degrees));
        if (limited < 0) limited += 360.0;

        return limited;
    }


    default double[] degreesToHoursMinutesSeconds(double degrees) {
        double hours = Math.floor(degrees / 15);
        double minutes = Math.floor(((degrees / 15) - hours) * 60);
        double seconds = ((((degrees / 15) - hours) * 60) - minutes) * 60;
        return new double[]{hours, minutes, seconds};
    }


    default double getDeclination(double lat, double lng) {
        double obOfEclip = deg2rad(obliquityOfEcliptic);
        lat = deg2rad(lat);
        lng = deg2rad(lng);
        return rad2deg(
                Math.asin(
                        (Math.sin(lat) * Math.cos(obOfEclip)) + (Math.cos(lat) * Math.sin(obOfEclip) * Math.sin(lng))
                )
        );
    }

    default double getRightAscension(double lat, double lng) {
        double obOfEclip = deg2rad(obliquityOfEcliptic);
        lat = deg2rad(lat);
        lng = deg2rad(lng);
        //  α=atan2(cosβsinλcosε−sinβsinε,cosβcosλ)
        double inpt1 = (Math.cos(lat) * Math.sin(lng) * Math.cos(obOfEclip)) - (Math.sin(lat) * Math.sin(obOfEclip));
        double inpt2 = Math.cos(lat) * Math.cos(lng);
        return rad2deg(Math.atan2(inpt1, inpt2));
    }

}
