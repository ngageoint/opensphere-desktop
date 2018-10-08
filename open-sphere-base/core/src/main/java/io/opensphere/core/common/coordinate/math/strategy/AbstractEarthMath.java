package io.opensphere.core.common.coordinate.math.strategy;

import io.opensphere.core.common.coordinate.math.EarthMath;

abstract public class AbstractEarthMath implements EarthMath
{

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateBearing(double latY1, double lonX1, double latY2, double lonX2)
    {

        // convert to radians
        latY1 = Math.toRadians(latY1);
        lonX1 = Math.toRadians(lonX1);
        latY2 = Math.toRadians(latY2);
        lonX2 = Math.toRadians(lonX2);

        /**
         * θ = atan2( sin(Δlong).cos(lat2), cos(lat1).sin(lat2) −
         * sin(lat1).cos(lat2).cos(Δlong) )
         */
        double x = Math.sin(lonX2 - lonX1) * Math.cos(latY2);
        double y = Math.cos(latY1) * Math.sin(latY2) - Math.sin(latY1) * Math.cos(latY2) * (Math.cos(lonX2 - lonX1));

        // Returns values from -180 to 180.
        double rads = Math.atan2(x, y);

        // Normalize for 0-360
        double retval = (Math.toDegrees(rads) + 360) % 360;

        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    abstract public double[] calculateDestination(double latY, double lonX, double distance, double bearing);

    /**
     * {@inheritDoc}
     */
    @Override
    abstract public double calculateDistanceBetweenPoints(double latY, double lonX, double latY2, double lonX2);

}
