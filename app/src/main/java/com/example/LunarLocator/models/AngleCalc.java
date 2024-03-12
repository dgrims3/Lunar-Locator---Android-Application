package com.example.LunarLocator.models;

public interface AngleCalc {
    double PI = 3.1415926535897932384626433832795028841971;


    default double radToDeg(double radians) {
        return (180.0 / PI) * radians;
    }

    default double degToRad(double degrees) {
        return (PI / 180.0) * degrees;
    }

    default double limit_degrees(double degrees) {
        double limited;
        degrees /= 360.0;
        limited = 360.0 * (degrees - Math.floor(degrees));
        if (limited < 0) limited += 360.0;

        return limited;
    }
    default double obliquityOfEcliptic(double jce) {
        double e0 = calcMeanObliquityOfEcliptic(jce);
        double omega = 125.04452 - 1934.136261 * jce;
        return e0 + 0.00256 * Math.cos(degToRad(omega));
    }

    default double calcMeanObliquityOfEcliptic(double jce) {
        double seconds = 21.448 - jce * (46.8150 + jce * (0.00059 - jce * (0.001813)));
        return 23.0 + (26.0 + (seconds / 60.0)) / 60.0;
    }

    default double moon_ascending_node(double jce) {
        return limit_degrees((125.04452 - (1934.136261 * jce) + (.0020708 * Math.pow(jce, 2) ) +  (Math.pow(jce, 3) / 450000)));
    }

    default double localHourAngle (double siderealInDegrees, double observerLongitude, double ascension) {
        return siderealInDegrees - observerLongitude - ascension;
    }

    default double localAltitude(double localHourAngle, double observersLatitude, double declination) {
        double altitude = Math.asin( (Math.sin(degToRad(observersLatitude)) * Math.sin(degToRad(declination))) + (Math.cos(degToRad(observersLatitude)) * Math.cos(degToRad(declination)) * Math.cos(degToRad(localHourAngle))) );
        return radToDeg(altitude);
    }


    default double getDeclination(double lat, double lng, double jce) {
        double obOfEclip = degToRad(obliquityOfEcliptic(jce));
        lat = degToRad(lat);
        lng = degToRad(lng);
        return radToDeg(
                Math.asin(
                        (Math.sin(lat) * Math.cos(obOfEclip)) + (Math.cos(lat) * Math.sin(obOfEclip) * Math.sin(lng))
                )
        );
    }

    default double getRightAscension(double lat, double lng, double jce) {
        double obOfEclip = degToRad(obliquityOfEcliptic(jce));
        lat = degToRad(lat);
        lng = degToRad(lng);
        //  α=atan2(cosβsinλcosε−sinβsinε,cosβcosλ)
        double inpt1 = (Math.cos(lat) * Math.sin(lng) * Math.cos(obOfEclip)) - (Math.sin(lat) * Math.sin(obOfEclip));
        double inpt2 = Math.cos(lat) * Math.cos(lng);
        return radToDeg(Math.atan2(inpt1, inpt2));
    }

    default double getInRange(double decimal) {
        if (decimal < 0) {
            return decimal + 1;
        } else if (decimal > 1) {
            return decimal - 1;
        } else {
            return decimal;
        }
    }

    default double nutation_in_longitude(double jce) {
        double T2 = Math.pow(jce, 2);
        double T3 = Math.pow(jce, 3);

        double D = degToRad(297.85036 + 445267.11148* jce - 0.0019142*T2 + (T3 / 189474));
        double M = degToRad(357.52772 + 35999.05034* jce - 0.0001603*T2 - (T3 / 300000));
        double MPR = degToRad(134.96298 + 477198.867398* jce + 0.0086972*T2 + (T3 / 56250));
        double F = degToRad(93.27191 + 483202.017538* jce - 0.0036825*T2 + (T3 / 327270));
        double omega = degToRad(moon_ascending_node(jce));

        double w = Math.sin(omega)*(-174.2* jce - 171996);
        w = w + Math.sin(2*(F + omega - D))*(-1.6* jce - 13187);
        w = w + Math.sin(2*(F + omega))*(-2274 - 0.2* jce);
        w = w + Math.sin(2 * omega)*(0.2* jce + 2062);
        w = w + Math.sin(M)*(1426 - 3.4* jce);
        w = w + Math.sin(MPR)*(0.1* jce + 712);
        w = w + Math.sin(2*(F + omega - D) + M)*(1.2* jce - 517);
        w = w + Math.sin(2 * F + omega)*(-0.4* jce - 386);
        w = w + Math.sin(2*(F + omega - D) - M)*(217 - 0.5* jce);
        w = w + Math.sin(2*(F - D) + omega)*(129 + 0.1* jce);
        w = w + Math.sin(MPR + omega)*(0.1* jce + 63);
        w = w + Math.sin(omega - MPR)*(-0.1* jce - 58);
        w = w + Math.sin(2*M)*(17 - 0.1* jce);
        w = w + Math.sin(2*(M + F + omega - D))*(0.1* jce - 16);
        w = w - 301*Math.sin(2*(F + omega) + MPR);
        w = w - 158*Math.sin(MPR - 2*D);
        w = w + 123*Math.sin(2*(F + omega) - MPR);
        w = w +  63*Math.sin(2*D);
        w = w -  59*Math.sin(2*(D + F + omega) - MPR);
        w = w -  51*Math.sin(2 * F + MPR + omega);
        w = w +  48*Math.sin(2*(MPR - D));
        w = w +  46*Math.sin(2*(F - MPR) + omega);
        w = w -  38*Math.sin(2*(D + F + omega));
        w = w -  31*Math.sin(2*(MPR + F + omega));
        w = w +  29*Math.sin(2*MPR);
        w = w +  29*Math.sin(2*(F + omega - D) + MPR);
        w = w +  26*Math.sin(2*F);
        w = w -  22*Math.sin(2*(F - D));
        w = w +  21*Math.sin(2*F + omega - MPR);
        w = w +  16*Math.sin(2*D - MPR + omega);
        w = w -  15*Math.sin(M + omega);
        w = w -  13*Math.sin(MPR + omega - 2*D);
        w = w -  12*Math.sin(omega - M);
        w = w +  11*Math.sin(2*(MPR - F));
        w = w -  10*Math.sin(2*(F + D) + omega - MPR);
        w = w -   8*Math.sin(2*(F + D + omega) + MPR);
        w = w +   7*Math.sin(2*(F + omega) + M);
        w = w -   7*Math.sin(MPR - 2*D + M);
        w = w -   7*Math.sin(2*(F + omega) - M);
        w = w -   7*Math.sin(2*D + 2*F + omega);
        w = w +   6*Math.sin(2*D + MPR);
        w = w +   6*Math.sin(2*(MPR + F + omega - D));
        w = w +   6*Math.sin(2*(F - D) + MPR + omega);
        w = w -   6*Math.sin(2*(D - MPR) + omega);
        w = w -   6*Math.sin(2*D + omega);
        w = w +   5*Math.sin(MPR - M);
        w = w -   5*Math.sin(2*(F - D) + omega - M);
        w = w -   5*Math.sin(omega - 2*D);
        w = w -   5*Math.sin(2*(MPR + F) + omega);
        w = w +   4*Math.sin(2*(MPR - D) + omega);
        w = w +   4*Math.sin(2*(F - D) + M + omega);
        w = w +   4*Math.sin(MPR - 2*F);
        w = w -   4*Math.sin(MPR - D);
        w = w -   4*Math.sin(M - 2*D);
        w = w -   4*Math.sin(D);
        w = w +   3*Math.sin(2*F + MPR);
        w = w -   3*Math.sin(2*(F + omega - MPR));
        w = w -   3*Math.sin(MPR - D - M);
        w = w -   3*Math.sin(M + MPR);
        w = w -   3*Math.sin(2*(F + omega) + MPR - M);
        w = w -   3*Math.sin(2*(D + F + omega) - M - MPR);
        w = w -   3*Math.sin(2*(F + omega) + 3*MPR);
        w = w -   3*Math.sin(2*(D + F + omega) - M);
        w = radToDeg(w);
        return w / 36000000.0;
    }

}
