package io.opensphere.core.common.geospatial.conversion;

import java.awt.geom.Point2D;

public class UTM
{
    public static final double MIN_LAT = Math.toRadians(-80.5);

    public static final double MAX_LAT = Math.toRadians(84.5);

    /** Semi-Major axis of UTM ellipsoid */
    public static final double SEMI_MAJOR = 6378137;

    /** Semi-Minor axis of UTM ellipsoid */
    public static final double SEMI_MINOR = 6356752.314;

    /** Eccentricity **/
    public static final double E = 0.081819191;

    /** (First Eccentricty)^2 **/
    public static final double E1SQ = 0.006739497;

    /** Scale factor **/
    public static final double SCALE = 0.9996;

    public static final int MIN_EASTING = 100000;

    public static final int MAX_EASTING = 900000;

    public static final int MIN_NORTHING = 0;

    public static final int MAX_NORTHING = 10000000;

    /** semi-major axis of ellipsoid in meters */
    private double utmA = 6378137.0;

    /** flattening of ellipsoid */
    private double utmF = 1 / 298.257223563;

    public long zone;

    public char hemisphere;

    public double easting;

    public double northing;

    public UTM(long z, char h, double e, double n)
    {
        zone = z;
        hemisphere = h;
        easting = e;
        northing = n;
    }

    public void setParameters(double a, double f)
    {
        utmA = a;
        utmF = f;

        double invF = 1 / f;
        if (a <= 0.0)
        {
            throw new RuntimeException("Error: Semi-major axis must be greater than zero.");
        }
        if (invF < 250 || invF > 350)
        {
            throw new RuntimeException("Error: Inverse flattening must be between 250 and 350");
        }
    }

    public String toString()
    {
        return "UTM{zone: " + zone + " hemisphere: " + hemisphere + " easting: " + easting + " northing: " + northing + " a: "
                + utmA + " f: " + utmF + "}";
    }

    public Point2D convertToLonLat()
    {
        double latitude = 0.0;
        double longitude = 0.0;

        if (hemisphere == 'S')
        {
            northing = 10000000 - northing;
        }

        /**
         * The following is the example from IBM
         * http://www.ibm.com/developerworks/java/library/j-coordconvert
         **/
        double arc = northing / SCALE;
        double mu = arc / (SEMI_MAJOR * (1 - Math.pow(E, 2) / 4.0 - 3 * Math.pow(E, 4) / 64.0 - 5 * Math.pow(E, 6) / 256.0));

        double ei = (1 - Math.pow((1 - E * E), (1 / 2.0))) / (1 + Math.pow((1 - E * E), (1 / 2.0)));

        double ca = 3 * ei / 2 - 27 * Math.pow(ei, 3) / 32.0;

        double cb = 21 * Math.pow(ei, 2) / 16 - 55 * Math.pow(ei, 4) / 32;
        double cc = 151 * Math.pow(ei, 3) / 96;
        double cd = 1097 * Math.pow(ei, 4) / 512;
        double phi1 = mu + ca * Math.sin(2 * mu) + cb * Math.sin(4 * mu) + cc * Math.sin(6 * mu) + cd * Math.sin(8 * mu);

        double n0 = SEMI_MAJOR / Math.pow((1 - Math.pow((E * Math.sin(phi1)), 2)), (1 / 2.0));

        double r0 = SEMI_MAJOR * (1 - E * E) / Math.pow((1 - Math.pow((E * Math.sin(phi1)), 2)), (3 / 2.0));
        double fact1 = n0 * Math.tan(phi1) / r0;

        double _a1 = 500000 - easting;
        double dd0 = _a1 / (n0 * SCALE);
        double fact2 = dd0 * dd0 / 2;

        double t0 = Math.pow(Math.tan(phi1), 2);
        double Q0 = E1SQ * Math.pow(Math.cos(phi1), 2);
        double fact3 = (5 + 3 * t0 + 10 * Q0 - 4 * Q0 * Q0 - 9 * E1SQ) * Math.pow(dd0, 4) / 24;

        double fact4 = (61 + 90 * t0 + 298 * Q0 + 45 * t0 * t0 - 252 * E1SQ - 3 * Q0 * Q0) * Math.pow(dd0, 6) / 720;

        double lof1 = _a1 / (n0 * SCALE);
        double lof2 = (1 + 2 * t0 + Q0) * Math.pow(dd0, 3) / 6.0;
        double lof3 = (5 - 2 * Q0 + 28 * t0 - 3 * Math.pow(Q0, 2) + 8 * E1SQ + 24 * Math.pow(t0, 2)) * Math.pow(dd0, 5) / 120;
        double _a2 = (lof1 - lof2 + lof3) / Math.cos(phi1);
        double _a3 = Math.toDegrees(_a2);

        latitude = Math.toDegrees(phi1 - fact1 * (fact2 + fact3 + fact4));
        double zoneCentralMeridian = 0;
        if (zone > 0)
        {
            zoneCentralMeridian = 6 * zone - 183.0;
        }
        else
        {
            zoneCentralMeridian = 3.0;
        }

        longitude = zoneCentralMeridian - _a3;
        if (hemisphere == 'S')
        {
            latitude = -latitude;
        }

        return new Point2D.Double(longitude, latitude);
    }
}
