package io.opensphere.core.common.coordinate.math;

public interface EarthMath
{

    /** True north-based azimuth in degrees */
    static double BEARING_N = 0.0;

    /** True north-based azimuth in degrees */
    static double BEARING_NNE = 22.5;

    /** True north-based azimuth in degrees */
    static double BEARING_NE = 45.0;

    /** True north-based azimuth in degrees */
    static double BEARING_ENE = 67.5;

    /** True north-based azimuth in degrees */
    static double BEARING_E = 90.0;

    /** True north-based azimuth in degrees */
    static double BEARING_ESE = 112.5;

    /** True north-based azimuth in degrees */
    static double BEARING_SE = 135.0;

    /** True north-based azimuth in degrees */
    static double BEARING_SSE = 157.5;

    /** True north-based azimuth in degrees */
    static double BEARING_S = 180.0;

    /** True north-based azimuth in degrees */
    static double BEARING_SSW = 202.5;

    /** True north-based azimuth in degrees */
    static double BEARING_SW = 225.0;

    /** True north-based azimuth in degrees */
    static double BEARING_WSW = 247.5;

    /** True north-based azimuth in degrees */
    static double BEARING_W = 270.0;

    /** True north-based azimuth in degrees */
    static double BEARING_WNW = 292.5;

    /** True north-based azimuth in degrees */
    static double BEARING_NW = 315;

    /** True north-based azimuth in degrees */
    static double BEARING_NNW = 337.5;

    /**
     * Determines the distances in meters between two points.
     *
     * @param p1LatY - decimal degrees
     * @param p1LonX - decimal degrees
     * @param p2LatY - decimal degrees
     * @param p2LonX - decimal degrees
     * @return
     */
    double calculateDistanceBetweenPoints(double p1LatY, double p1LonX, double p2LatY, double p2LonX);

    /**
     * Determines the end point, given a starting point and bearing.
     *
     * @param latY - decimal degrees
     * @param lonX - decimal degrees
     * @param distance
     * @param bearing
     * @return double[] - [0]=lon, [1]=lat;
     */
    double[] calculateDestination(double latY, double lonX, double distance, double bearing);

    /**
     * Determines the bearing, given two points, from due north.
     *
     * @param p1LatY - decimal degrees
     * @param p1LonX - decimal degrees
     * @param p2LatY - decimal degrees
     * @param p2LonX - decimal degrees
     * @return
     */
    double calculateBearing(double p1LatY, double p1LonX, double p2LatY, double p2LonX);
}
