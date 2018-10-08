package io.opensphere.core.common.coordinate.math.strategy;

/**
 * The Vincenty Model uses a ellipsoid Earth which is accurate down to the
 * millimeter. It uses the WGS84 ellipsoid model.
 *
 * @see http://www.movable-type.co.uk/scripts/latlong.html
 * @see http://en.wikipedia.org/wiki/Vincenty%27s_formulae
 */
public class Vincenty extends AbstractEarthMath
{

    /** Earth's Semi Major Axis in meters */
    public static final double a = 6378137.0;

    /** Earth's Seimi Minor Axis in meters */
    public static final double b = 6356752.3142;

    /** Earth's flattening */
    public static final double f = 1.0 / 298.257223563;

    /**
     * Tolerance for floating point equality checks.
     */
    private static final double tol = 1e-12;

    private static final int MAX_ITERS = 500;

    /**
     * Performs the direct Vincenty algorithm calculating the end point when
     * given a starting point, distance, and bearing. All calculations are done
     * in radians and meters.
     *
     * @param lat1 radians
     * @param lon1 radians
     * @param lat2 radians
     * @param lon2 radians
     * @return array of doubles, lat(rad)=index 0, lon(rad)=index 1,
     *         alpha2(rad)=index 3
     */

    public double[] calculateDestinationRadians(double lat1, double lon1, double dist, double bearing)
    {

        double[] retVal = { 0, 0, 0, 0 };

        double s = dist;
        double alpha1 = bearing;
        double sinAlpha1 = Math.sin(alpha1);
        double cosAlpha1 = Math.cos(alpha1);

        double tanU1 = (1 - f) * Math.tan(lat1);
        double cosU1 = 1 / Math.sqrt(1 + tanU1 * tanU1);
        double sinU1 = tanU1 * cosU1;
        double sigma1 = Math.atan2(tanU1, cosAlpha1);
        double sinAlpha = cosU1 * sinAlpha1;
        double cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1.0 + uSq / 16384.0 * (4096.0 + uSq * (-768.0 + uSq * (320.0 - 175.0 * uSq)));
        double B = uSq / 1024.0 * (256.0 + uSq * (-128.0 + uSq * (74.0 - 47.0 * uSq)));

        double sinSigma = 0.0;
        double cosSigma = 0.0;
        double cos2SigmaM = 0.0;
        double sigma = s / (b * A), sigmaP = 2 * Math.PI;
        int nIters = 0;
        while (Math.abs(sigma - sigmaP) > tol && nIters < MAX_ITERS)
        {
            cos2SigmaM = Math.cos(2 * sigma1 + sigma);
            sinSigma = Math.sin(sigma);
            cosSigma = Math.cos(sigma);
            double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)
                    - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
            sigmaP = sigma;
            sigma = s / (b * A) + deltaSigma;
            nIters++;
        }

        double tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1;
        double lat2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1,
                (1 - f) * Math.sqrt(sinAlpha * sinAlpha + tmp * tmp));
        double lambda = Math.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
        double C = f / 16.0 * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha));
        double L = lambda - (1.0 - C) * f * sinAlpha
                * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)));

        // final bearing
        double revAz = Math.atan2(sinAlpha, -tmp);

        double lon2 = lon1 + L;
        retVal[0] = lat2;
        retVal[1] = lon2;
        retVal[2] = revAz;

        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] calculateDestination(double lat1, double lon1, double dist, double bearing)
    {

        double pt1Lat = Math.toRadians(lat1);
        double pt1Lon = Math.toRadians(lon1);
        double b = Math.toRadians(bearing);

        double[] vals = calculateDestinationRadians(pt1Lat, pt1Lon, dist, b);
        // order reversed to preserve existing functionality.
        double[] retVals = { Math.toDegrees(vals[1]), Math.toDegrees(vals[0]) };

        return retVals;
    }

    /**
     * Performs the inverse Vincenty algorithm calculating the distance between
     * two lat/lon pairs and the azimuth angles betweeen them. All calculations
     * are done in radians and meters.
     *
     * @param lat1 radians
     * @param lon1 radians
     * @param lat2 radians
     * @param lon2 radians
     * @return array of doubles, distance(m)=index 0, alpha1(rad)=index 1,
     *         alpha2(rad) = index 2
     */
    public double[] calculateDistanceBetweenPointsRadians(double latY, double lonX, double latY2, double lonX2)
    {

        double[] retVals = { 0, 0, 0 };

        double L = lonX2 - lonX;
        double U1 = Math.atan((1.0 - f) * Math.tan(latY));
        double U2 = Math.atan((1.0 - f) * Math.tan(latY2));
        double sinU1 = Math.sin(U1);
        double cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2);
        double cosU2 = Math.cos(U2);

        double lambda = L;
        double lambdaP;
        int iterLimit = MAX_ITERS;

        double sinSigma;
        double cosSqAlpha;
        double cos2SigmaM;
        double cosSigma;
        double sigma;
        double tmp;
        double sinLambda;
        double cosLambda;
        do
        {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            tmp = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            sinSigma = Math.sqrt(cosU2 * sinLambda * (cosU2 * sinLambda) + tmp * tmp);
            if (Math.abs(sinSigma) < tol)
            {
                retVals[0] = 0;
                // co-incident points
                return retVals;
            }
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2.0 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM))
            {
                // equatorial line: cosSqAlpha=0 (§6)
                cos2SigmaM = 0;
            }
            double C = f / 16.0 * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1.0 - C) * f * sinAlpha
                    * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        }
        while (Math.abs(lambda - lambdaP) > tol && --iterLimit > 0);

        if (iterLimit == 0)
        {
            retVals[0] = Double.NaN;
            // formula failed to converge
            return retVals;
        }

        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1.0 + uSq / 16384.0 * (4096.0 + uSq * (-768.0 + uSq * (320.0 - 175.0 * uSq)));
        double B = uSq / 1024.0 * (256.0 + uSq * (-128.0 + uSq * (74.0 - 47.0 * uSq)));
        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4.0 * (cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)
                - B / 6.0 * cos2SigmaM * (-3.0 + 4.0 * sinSigma * sinSigma) * (-3.0 + 4.0 * cos2SigmaM * cos2SigmaM)));
        double s = b * A * (sigma - deltaSigma);
        retVals[0] = s;

        double y = cosU2 * sinLambda;
        double x = tmp;
        double alpha = Math.atan2(y, x);
        retVals[1] = alpha;

        y = cosU1 * sinLambda;
        x = -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda;
        alpha = Math.atan2(y, x);
        retVals[2] = alpha;

        return retVals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateDistanceBetweenPoints(double latY, double lonX, double latY2, double lonX2)
    {

        double pt1Lat = Math.toRadians(latY);
        double pt1Lon = Math.toRadians(lonX);
        double pt2Lat = Math.toRadians(latY2);
        double pt2Lon = Math.toRadians(lonX2);

        double[] vals = calculateDistanceBetweenPointsRadians(pt1Lat, pt1Lon, pt2Lat, pt2Lon);

        return vals[0];
    }
}
