package com.example.solarcalc.models;

public interface SolarCalc extends TimeCalc{

    default double calcSolNoon(double longitude, int timezone, double jd) {
        double tNoon = calcTimeJulianCent(jd - (longitude / 360.0));
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



    default double calcEquationOfTime(double jce) {
        double epsilon = calcObliquityCorrection(jce);
        double l0 = calcGeomMeanLongSun(jce);
        double e = calcEccentricityEarthOrbit(jce);
        double m = calcGeomMeanAnomalySun(jce);

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

    default double calcObliquityCorrection(double jce) {
        double e0 = calcMeanObliquityOfEcliptic(jce);
        double omega = 125.04 - 1934.136 * jce;
        return e0 + 0.00256 * Math.cos(degToRad(omega));        // in degrees
    }

    default double calcMeanObliquityOfEcliptic(double jce) {
        double seconds = 21.448 - jce * (46.8150 + jce * (0.00059 - jce * (0.001813)));
        return 23.0 + (26.0 + (seconds / 60.0)) / 60.0;        // in degrees
    }

    default double degToRad(double angleDeg) {
        return (Math.PI * angleDeg / 180.0);
    }

    default double radToDeg(double angleRad) {
        return (180.0 * angleRad / Math.PI);
    }

    default double calcGeomMeanLongSun(double jce) {
        double l0 = 280.46646 + jce * (36000.76983 + jce * (0.0003032));
        while (l0 > 360.0) {
            l0 -= 360.0;
        }
        while (l0 < 0.0) {
            l0 += 360.0;
        }
        return l0;        // in degrees
    }

    default double calcEccentricityEarthOrbit(double jce) {
        return 0.016708634 - jce * (0.000042037 + 0.0000001267 * jce);        // unitless
    }

    default double calcGeomMeanAnomalySun(double jce) {
        return 357.52911 + jce * (35999.05029 - 0.0001537 * jce);        // in degrees
    }
}
