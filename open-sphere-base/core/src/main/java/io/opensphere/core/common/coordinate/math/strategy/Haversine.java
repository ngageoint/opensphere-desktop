package io.opensphere.core.common.coordinate.math.strategy;

import io.opensphere.core.common.coordinate.math.EarthMath;

/**
 * The Haversine Model uses a spherical Earth which is good for most estimates.
 *
 * @see http://www.movable-type.co.uk/scripts/latlong.html
 */
public class Haversine extends AbstractEarthMath implements EarthMath
{

    /** Earth's Radius in meters. */
    public static double R = 6378137.0;

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] calculateDestination(double latY, double lonX, double distance, double bearing)
    {

        double lon1 = Math.toRadians(lonX);
        double lat1 = Math.toRadians(latY);
        bearing = Math.toRadians(bearing);

        double dOverR = distance / R;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dOverR) + Math.cos(lat1) * Math.sin(dOverR) * Math.cos(bearing));
        double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(dOverR) * Math.cos(lat1),
                Math.cos(dOverR) - Math.sin(lat1) * Math.sin(lat2));

        // normalize to -180...+180
        lon2 = (lon2 + Math.PI) % (2 * Math.PI) - Math.PI;
        if (Double.isNaN(lat2) || Double.isNaN(lon2))
        {
            return null;
        }

        lon2 = Math.toDegrees(lon2);
        lat2 = Math.toDegrees(lat2);

        double[] retval = { lon2, lat2 };
        return retval;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateDistanceBetweenPoints(double latY, double lonX, double latY2, double lonX2)
    {

        double distance = 0.0;
        double dLat = Math.toRadians(latY2 - latY);

        double dLon = Math.toRadians(lonX2 - lonX);

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(Math.toRadians(latY)) * Math.cos(Math.toRadians(latY2)) * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        distance = R * c;
        return distance;
    }

}
