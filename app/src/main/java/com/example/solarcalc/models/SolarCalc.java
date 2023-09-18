package com.example.solarcalc.models;

import java.util.TimeZone;


public interface SolarCalc extends TimeCalc{

    default double calcSolNoon(double jd, double longitude, int timezone) {
        double tNoon = calcTimeJulianCent(jd - longitude / 360.0);
        double eqTime = calcEquationOfTime(tNoon);
        double solNoonOffset = 720.0 - (longitude * 4) - eqTime; // in minutes
        double newt = calcTimeJulianCent(jd - 0.5 + solNoonOffset / 1440.0);
        eqTime = calcEquationOfTime(newt);
        double solNoonLocal = 720 - (longitude * 4) - eqTime + (timezone * 60);// in minutes
        while (solNoonLocal < 0.0) {
            solNoonLocal += 1440.0;
        }
        while (solNoonLocal >= 1440.0) {
            solNoonLocal -= 1440.0;
        }
        return solNoonLocal;
    }



    default double calcEquationOfTime(double t) {
        double epsilon = calcObliquityCorrection(t);
        double l0 = calcGeomMeanLongSun(t);
        double e = calcEccentricityEarthOrbit(t);
        double m = calcGeomMeanAnomalySun(t);

        double y = Math.tan(degToRad(epsilon) / 2.0);
        y *= y;

        double sin2l0 = Math.sin(2.0 * degToRad(l0));
        double sinm = Math.sin(degToRad(m));
        double cos2l0 = Math.cos(2.0 * degToRad(l0));
        double sin4l0 = Math.sin(4.0 * degToRad(l0));
        double sin2m = Math.sin(2.0 * degToRad(m));

        double Etime = y * sin2l0 - 2.0 * e * sinm + 4.0 * e * y * sinm * cos2l0 - 0.5 * y * y * sin4l0 - 1.25 * e * e * sin2m;
        return radToDeg(Etime) * 4.0;    // in minutes of time
    }

    default double calcObliquityCorrection(double t) {
        double e0 = calcMeanObliquityOfEcliptic(t);
        double omega = 125.04 - 1934.136 * t;
        double e = e0 + 0.00256 * Math.cos(degToRad(omega));
        return e;        // in degrees
    }

    default double calcMeanObliquityOfEcliptic(double t) {
        double ti = t;
        double seconds = 21.448 - ti * (46.8150 + ti * (0.00059 - ti * (0.001813)));
        double e0 = 23.0 + (26.0 + (seconds / 60.0)) / 60.0;
        return e0;        // in degrees
    }

    default double degToRad(double angleDeg) {
        return (Math.PI * angleDeg / 180.0);
    }

    default double radToDeg(double angleRad) {
        return (180.0 * angleRad / Math.PI);
    }

    default double calcGeomMeanLongSun(double t) {
        double ti = t;
        double l0 = 280.46646 + ti * (36000.76983 + ti * (0.0003032));
        while (l0 > 360.0) {
            l0 -= 360.0;
        }
        while (l0 < 0.0) {
            l0 += 360.0;
        }
        return l0;        // in degrees
    }

    default double calcEccentricityEarthOrbit(double t) {
        double ti = t;
        double e = 0.016708634 - ti * (0.000042037 + 0.0000001267 * ti);
        return e;        // unitless
    }

    default double calcGeomMeanAnomalySun(double t) {
        double ti = t;
        double m = 357.52911 + ti * (35999.05029 - 0.0001537 * ti);
        return m;        // in degrees
    }
}
