package com.example.solarcalc.models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public interface LunarCalc extends TimeCalc, AngleCalc {
    // moonLat β, moonLong λ, earthMoonDistance ∆

    int COUNT = 60;

    enum position {ASCENSION, DECLINATION}

    enum time {RISING, TRANSIT, SETTING}

    enum coords {LAT, LNG, DISTANCE}

    enum Term {
        TERM_D,
        TERM_M,
        TERM_MPR,
        TERM_F,
        TERM_LB,
        TERM_R
    }

    //  Moon's Periodic Terms for Longitude and Distance
    double[][] ML_TERMS =
            {
                    {0, 0, 1, 0, 6288774, -20905355},
                    {2, 0, -1, 0, 1274027, -3699111},
                    {2, 0, 0, 0, 658314, -2955968},
                    {0, 0, 2, 0, 213618, -569925},
                    {0, 1, 0, 0, -185116, 48888},
                    {0, 0, 0, 2, -114332, -3149},
                    {2, 0, -2, 0, 58793, 246158},
                    {2, -1, -1, 0, 57066, -152138},
                    {2, 0, 1, 0, 53322, -170733},
                    {2, -1, 0, 0, 45758, -204586},
                    {0, 1, -1, 0, -40923, -129620},
                    {1, 0, 0, 0, -34720, 108743},
                    {0, 1, 1, 0, -30383, 104755},
                    {2, 0, 0, -2, 15327, 10321},
                    {0, 0, 1, 2, -12528, 0},
                    {0, 0, 1, -2, 10980, 79661},
                    {4, 0, -1, 0, 10675, -34782},
                    {0, 0, 3, 0, 10034, -23210},
                    {4, 0, -2, 0, 8548, -21636},
                    {2, 1, -1, 0, -7888, 24208},
                    {2, 1, 0, 0, -6766, 30824},
                    {1, 0, -1, 0, -5163, -8379},
                    {1, 1, 0, 0, 4987, -16675},
                    {2, -1, 1, 0, 4036, -12831},
                    {2, 0, 2, 0, 3994, -10445},
                    {4, 0, 0, 0, 3861, -11650},
                    {2, 0, -3, 0, 3665, 14403},
                    {0, 1, -2, 0, -2689, -7003},
                    {2, 0, -1, 2, -2602, 0},
                    {2, -1, -2, 0, 2390, 10056},
                    {1, 0, 1, 0, -2348, 6322},
                    {2, -2, 0, 0, 2236, -9884},
                    {0, 1, 2, 0, -2120, 5751},
                    {0, 2, 0, 0, -2069, 0},
                    {2, -2, -1, 0, 2048, -4950},
                    {2, 0, 1, -2, -1773, 4130},
                    {2, 0, 0, 2, -1595, 0},
                    {4, -1, -1, 0, 1215, -3958},
                    {0, 0, 2, 2, -1110, 0},
                    {3, 0, -1, 0, -892, 3258},
                    {2, 1, 1, 0, -810, 2616},
                    {4, -1, -2, 0, 759, -1897},
                    {0, 2, -1, 0, -713, -2117},
                    {2, 2, -1, 0, -700, 2354},
                    {2, 1, -2, 0, 691, 0},
                    {2, -1, 0, -2, 596, 0},
                    {4, 0, 1, 0, 549, -1423},
                    {0, 0, 4, 0, 537, -1117},
                    {4, -1, 0, 0, 520, -1571},
                    {1, 0, -2, 0, -487, -1739},
                    {2, 1, 0, -2, -399, 0},
                    {0, 0, 2, -2, -381, -4421},
                    {1, 1, 1, 0, 351, 0},
                    {3, 0, -2, 0, -340, 0},
                    {4, 0, -3, 0, 330, 0},
                    {2, -1, 2, 0, 327, 0},
                    {0, 2, 1, 0, -323, 1165},
                    {1, 1, -1, 0, 299, 0},
                    {2, 0, 3, 0, 294, 0},
                    {2, 0, -1, -2, 0, 8752}
            };

    //  Moon's Periodic Terms for Latitude
    double[][] MB_TERMS =
            {
                    {0, 0, 0, 1, 5128122, 0},
                    {0, 0, 1, 1, 280602, 0},
                    {0, 0, 1, -1, 277693, 0},
                    {2, 0, 0, -1, 173237, 0},
                    {2, 0, -1, 1, 55413, 0},
                    {2, 0, -1, -1, 46271, 0},
                    {2, 0, 0, 1, 32573, 0},
                    {0, 0, 2, 1, 17198, 0},
                    {2, 0, 1, -1, 9266, 0},
                    {0, 0, 2, -1, 8822, 0},
                    {2, -1, 0, -1, 8216, 0},
                    {2, 0, -2, -1, 4324, 0},
                    {2, 0, 1, 1, 4200, 0},
                    {2, 1, 0, -1, -3359, 0},
                    {2, -1, -1, 1, 2463, 0},
                    {2, -1, 0, 1, 2211, 0},
                    {2, -1, -1, -1, 2065, 0},
                    {0, 1, -1, -1, -1870, 0},
                    {4, 0, -1, -1, 1828, 0},
                    {0, 1, 0, 1, -1794, 0},
                    {0, 0, 0, 3, -1749, 0},
                    {0, 1, -1, 1, -1565, 0},
                    {1, 0, 0, 1, -1491, 0},
                    {0, 1, 1, 1, -1475, 0},
                    {0, 1, 1, -1, -1410, 0},
                    {0, 1, 0, -1, -1344, 0},
                    {1, 0, 0, -1, -1335, 0},
                    {0, 0, 3, 1, 1107, 0},
                    {4, 0, 0, -1, 1021, 0},
                    {4, 0, -1, 1, 833, 0},
                    {0, 0, 1, -3, 777, 0},
                    {4, 0, -2, 1, 671, 0},
                    {2, 0, 0, -3, 607, 0},
                    {2, 0, 2, -1, 596, 0},
                    {2, -1, 1, -1, 491, 0},
                    {2, 0, -2, 1, -451, 0},
                    {0, 0, 3, -1, 439, 0},
                    {2, 0, 2, 1, 422, 0},
                    {2, 0, -3, -1, 421, 0},
                    {2, 1, -1, 1, -366, 0},
                    {2, 1, 0, 1, -351, 0},
                    {4, 0, 0, 1, 331, 0},
                    {2, -1, 1, 1, 315, 0},
                    {2, -2, 0, -1, 302, 0},
                    {0, 0, 1, 3, -283, 0},
                    {2, 1, 1, -1, -229, 0},
                    {1, 1, 0, -1, 223, 0},
                    {1, 1, 0, 1, 223, 0},
                    {0, 1, -2, -1, -220, 0},
                    {2, 1, -1, -1, -220, 0},
                    {1, 0, 1, 1, -185, 0},
                    {2, -1, -2, -1, 181, 0},
                    {0, 1, 2, 1, -177, 0},
                    {4, 0, -2, -1, 176, 0},
                    {4, -1, -1, -1, 166, 0},
                    {1, 0, 1, -1, -164, 0},
                    {4, 0, 1, -1, 132, 0},
                    {1, 0, -1, -1, -119, 0},
                    {4, -1, 0, -1, 115, 0},
                    {2, -2, 0, 1, 107, 0}
            };


    default double third_order_polynomial(double a, double b, double c, double d, double jce) {
        return ((a * jce + b) * jce + c) * jce + d;
    }

    default double fourth_order_polynomial(double a, double b, double c, double d, double e, double jce) {
        return (((a * jce + b) * jce + c) * jce + d) * jce + e;
    }

    default double moon_ascending_node(double jce) {
        return rad2deg(Math.sin(deg2rad((125.04452 - (1934.136261 * jce) + .0020708 / 450000))));
    }

    default double sun_mean_longitude_L(double jce) {
        return limit_degrees(280.4664 + 36000.7698 * jce);
    }

    default double moon_mean_longitude_L_PRIME(double jce) {
        return limit_degrees(fourth_order_polynomial(
                -1.0 / 65194000, 1.0 / 538841, -0.0015786, 481267.88123421, 218.3164477, jce));
    }

    default double moon_mean_elongation_D(double jce) {
        return limit_degrees(fourth_order_polynomial(
                -1.0 / 113065000, 1.0 / 545868, -0.0018819, 445267.1114034, 297.8501921, jce));
    }

    default double sun_mean_anomaly_M(double jce) {
        return limit_degrees(third_order_polynomial(
                1.0 / 24490000, -0.0001536, 35999.0502909, 357.5291092, jce));
    }

    default double moon_mean_anomaly_M_PRIME(double jce) {
        return limit_degrees(fourth_order_polynomial(
                -1.0 / 14712000, 1.0 / 69699, 0.0087414, 477198.8675055, 134.9633964, jce));
    }

    default double moon_latitude_argument_F(double jce) {
        return limit_degrees(fourth_order_polynomial(
                1.0 / 863310000, -1.0 / 3526000, -0.0036539, 483202.0175233, 93.2720950, jce));
    }

    default double summationAdditiveA1(double jce) {
        return limit_degrees(119.75 + 131.859 * jce);
    }

    default double summationAdditiveA2(double jce) {
        return limit_degrees(53.09 + 479264.290 * jce);
    }

    default double summationAdditiveA3(double jce) {
        return limit_degrees(313.45 + 481266.484 * jce);
    }

    default double additiveToMoonLat(double jce) {
        return ((-2235 * Math.sin(moon_mean_longitude_L_PRIME(jce))) +
                (382 * Math.sin(deg2rad(summationAdditiveA3(jce)))) +
                (175 * Math.sin(deg2rad(summationAdditiveA1(jce) - moon_latitude_argument_F(jce)))) +
                (175 * Math.sin(deg2rad(summationAdditiveA1(jce) + moon_latitude_argument_F(jce)))) +
                (127 * Math.sin(deg2rad(moon_mean_longitude_L_PRIME(jce) - moon_mean_anomaly_M_PRIME(jce)))) -
                (115 * Math.sin(deg2rad(moon_mean_longitude_L_PRIME(jce) + moon_mean_anomaly_M_PRIME(jce)))));
    }

    default double additiveToMoonLng(double jce) {
        return ((3958 * Math.sin(deg2rad(summationAdditiveA1(jce)))) +
                (1962 * Math.sin(deg2rad(moon_mean_longitude_L_PRIME(jce) - moon_latitude_argument_F(jce)))) +
                (318 * Math.sin(deg2rad(summationAdditiveA2(jce)))));
    }

    default double[] moon_periodic_term_summation(double d, double m, double m_prime, double f, double jce, double[][] terms) {
        double e, e_mult, trig_arg, sin_sum, cos_sum;
        e = 1.0 - jce * (0.002516 + jce * 0.0000074);
        sin_sum = 0;
        cos_sum = 0;
        for (int i = 0; i < COUNT; i++) {
            e_mult = Math.pow(e, Math.abs(terms[i][Term.TERM_M.ordinal()]));
            trig_arg = deg2rad(terms[i][Term.TERM_D.ordinal()] * d + terms[i][Term.TERM_M.ordinal()] * m + terms[i][Term.TERM_MPR.ordinal()] * m_prime + terms[i][Term.TERM_F.ordinal()] * f);
            sin_sum += e_mult * terms[i][Term.TERM_LB.ordinal()] * Math.sin(trig_arg);
            cos_sum += e_mult * terms[i][Term.TERM_R.ordinal()] * Math.cos(trig_arg);
        }
        return new double[]{sin_sum, cos_sum};
    }

    default double moon_earth_distance_in_km(double dist) {
        return 385000.56 + dist / 1000;
    }

    default double moon_longitude_coordinates_in_degrees(double longitude, double jce) {
        return moon_mean_longitude_L_PRIME(jce) + (longitude + additiveToMoonLng(jce)) / 1000000;
    }

    default double moon_latitude_coordinates_in_degrees(double latitude, double jce) {
        return (latitude + additiveToMoonLat(jce)) / 1000000;
    }

    // pg 287
    default double equatorialHorizontalParallax(double distance) {
        return Math.asin(6378.14 / distance);
    }

    // This can get added to the moon's longitude from greater accuracy. Currently the number returned is angle seconds and should be converted to degrees.
    // page 151 || 143
    default double nutation_in_longitude(double jce) {
        double node = moon_ascending_node(jce);
        double L = deg2rad(280.4665 + (36000.7698 * jce));
        double LPR = deg2rad(218.3165 + (481267.8813 * jce));
        return rad2deg(((-17.2 * Math.sin(node)) - (-1.32 * Math.sin(2 * L)) - (.23 * Math.sin(2 * LPR)) + (.21 * Math.sin(2 * node))));
    }

    // page 340/351
    default double[] getMoonLatLngDist(double jd) {
        double jce = calcTimeJulianCent(jd);
        double lat = moon_periodic_term_summation(
                moon_mean_elongation_D(jce),
                sun_mean_anomaly_M(jce),
                moon_mean_anomaly_M_PRIME(jce),
                moon_latitude_argument_F(jce),
                jce,
                MB_TERMS
        )[0];
        double[] lngDist = moon_periodic_term_summation(
                moon_mean_elongation_D(jce),
                sun_mean_anomaly_M(jce),
                moon_mean_anomaly_M_PRIME(jce),
                moon_latitude_argument_F(jce),
                jce,
                ML_TERMS
        );
        return new double[]{moon_latitude_coordinates_in_degrees(lat, jce), moon_longitude_coordinates_in_degrees(lngDist[0], jce), moon_earth_distance_in_km(lngDist[1])};
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    default double[] lunarAscensionDeclinationDistance(double jd) {
        double[] moonLatLngDist = getMoonLatLngDist(jd);
        double moonDeclination = getDeclination(moonLatLngDist[coords.LAT.ordinal()], moonLatLngDist[coords.LNG.ordinal()]);
        double moonRightAscension = getRightAscension(moonLatLngDist[coords.LAT.ordinal()], moonLatLngDist[coords.LNG.ordinal()]);

        return new double[]{moonRightAscension, moonDeclination, moonLatLngDist[coords.DISTANCE.ordinal()]};
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    default double[] risingTransitSetting(double jd, double[] latlng, double[] lunarAscDecDist) {
        double jce = calcTimeJulianCent(jd);
        double sidereal = greenwichMeanSiderealTime(jce);
        double lunarDistance = lunarAscDecDist[coords.DISTANCE.ordinal()];
        double standardAltitude = deg2rad(-.583 - equatorialHorizontalParallax(lunarDistance)); // deg2rad(.125); // Lunar

        double lat = latlng[coords.LAT.ordinal()];
        double lng = -1 * latlng[coords.LNG.ordinal()]; // reverse the longitude so it is measured positively west from Greenwich.

        double localHourAngle = Math.sin(standardAltitude) - (Math.sin(deg2rad(lat)) * Math.sin(deg2rad(lunarAscDecDist[position.DECLINATION.ordinal()]))) / (Math.cos(deg2rad(lat)) * Math.cos(deg2rad(lunarAscDecDist[position.DECLINATION.ordinal()])));
        localHourAngle = rad2deg(Math.acos(localHourAngle));

        double transit = getInRange((lunarAscDecDist[position.ASCENSION.ordinal()] + lng - sidereal) / 360);
        double rising = transit - (localHourAngle / 360);
        double setting = transit + (localHourAngle / 360);

        return new double[]{rising, transit, setting};
    }

    default double[] interpolate(double m, double[] one, double[] two, double[] three) {
        double n = m + (56.0 / 86400);
        double aAsc = two[0] - one[0];
        double bAsc = three[0] - two[0];
        double cAsc = bAsc - aAsc;

        double asc = two[0] + (n / 2) * (aAsc + bAsc + (cAsc * n));

        double aDec = two[1] - one[1];
        double bDec = three[1] - two[1];
        double cDec = bDec - aDec;

        double dec = two[1] + (n / 2) * (aDec + bDec + (cDec * n));
        return new double[]{asc, dec};

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    default List<ZonedDateTime> calcMoonTimes() {
        List<ZonedDateTime> zonedDateTimeList = new ArrayList<>();
        int[] positions = {time.RISING.ordinal(), time.TRANSIT.ordinal(), time.SETTING.ordinal()};
        double[] coords = {42.3601, -71.0589}; //Boston
        LocalDate date = LocalDate.of(2024, 2, 6);

        for (int position: positions
             ) {
            double timeDifference = 0;
            LocalDate tempDate = date;
            for (int i = 0; i <6; i++) {
                double jd = getJDFromCalenderDate(tempDate.getYear(), tempDate.getMonthValue(), tempDate.getDayOfMonth() + timeDifference);
                double[] td = lunarAscensionDeclinationDistance(jd);
                double[] riseTransitSet = risingTransitSetting(jd, coords, td);
                double fractionOfDay = riseTransitSet[position];
                if (fractionOfDay < 0) {
                    tempDate.minusDays((long) Math.abs(Math.floor(fractionOfDay)));
                    timeDifference = 1.0 + ( Math.abs(Math.ceil(fractionOfDay)) + fractionOfDay);
                } else if (fractionOfDay > 1) {
                    tempDate.plusDays((long) Math.floor(fractionOfDay));
                    timeDifference = (fractionOfDay - Math.floor(fractionOfDay));
                } else timeDifference = fractionOfDay;
            }
            zonedDateTimeList.add(position, (ZonedDateTime) localFractionOfDayFromUTCToLocal(timeDifference, tempDate, coords, false));
        }
        return zonedDateTimeList;
    }

}
