package io.opensphere.core.common.lobintersect;

public class LobIntersection
{

    private static double tol = 1e-12;

    /**
     * Calculates the lat/lon of where two lines of bearing intersect. Null is
     * returned if there is no unique intersection.
     *
     * @param p1 - point 1 location and bearing.
     * @param p2 - point 2 location and bearing.
     * @return
     */
    public static LatLon getLobIntersection(LobPoint p1, LobPoint p2)
    {
        double lat1 = p1.getLat();
        double lon1 = p1.getLon();
        double lat2 = p2.getLat();
        double lon2 = p2.getLon();
        double brng13 = p1.getBearing();
        double brng23 = p2.getBearing();
        double brng12, brng21;
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double dLat_2 = dLat * 0.5;
        double sindLat_2 = Math.sin(dLat_2);
        double dLon_2 = dLon * 0.5;
        double sindLon_2 = Math.sin(dLon_2);
        double dist12 = 2 * Math.asin(Math.sqrt(sindLat_2 * sindLat_2 + Math.cos(lat1) * Math.cos(lat2) * sindLon_2 * sindLon_2));

        /* Points are the same, no solution */
        if (Math.abs(dist12) < tol)
        {
            return null;
        }

        // initial/final bearings between points
        double denon = Math.sin(dist12) * Math.cos(lat1);
        if (Math.abs(denon) < tol)
        {
            return null;
        }
        double sl2 = Math.sin(lat2);
        double sl1 = Math.sin(lat1);
        double cd12 = Math.cos(dist12);
        double tmp = (sl2 - sl1 * cd12) / denon;

        if (tmp > 1.0)
        {
            tmp = 1.0;
        }
        if (tmp < -1.0)
        {
            tmp = -1.0;
        }

        double brngA = Math.acos(tmp);
        // isnan is in h not cmath
        if (Double.isNaN(brngA))
        {
            // protect against rounding
            brngA = 0;
        }

        denon = Math.sin(dist12) * Math.cos(lat2);
        if (Math.abs(denon) < tol)
        {
            return null;
        }
        tmp = (sl1 - sl2 * cd12) / denon;
        if (tmp > 1.0)
        {
            tmp = 1.0;
        }
        if (tmp < -1.0)
        {
            tmp = -1.0;
        }

        double brngB = Math.acos(tmp);
        if (Double.isNaN(brngB))
        {
            // protect against rounding
            brngB = 0;
        }

        if (Math.sin(lon2 - lon1) > 0)
        {
            brng12 = brngA;
            brng21 = 2 * Math.PI - brngB;
        }
        else
        {
            brng12 = 2 * Math.PI - brngA;
            brng21 = brngB;
        }

        // angle 2-1-3
        double alpha1 = (brng13 - brng12 + Math.PI) % (2 * Math.PI) - Math.PI;
        // angle 1-2-3
        double alpha2 = (brng21 - brng23 + Math.PI) % (2 * Math.PI) - Math.PI;

        if (Math.abs(Math.sin(alpha1)) < tol && Math.abs(Math.sin(alpha2)) < tol)
        {
            // infinite intersections
            return null;
        }
        if (Math.abs(Math.sin(alpha1) * Math.sin(alpha2)) < tol)
        {
            // ambiguous intersection
            return null;
        }

        double alpha3 = Math.acos(-Math.cos(alpha1) * Math.cos(alpha2) + Math.sin(alpha1) * Math.sin(alpha2) * Math.cos(dist12));
        double dist13 = Math.atan2(Math.sin(dist12) * Math.sin(alpha1) * Math.sin(alpha2),
                Math.cos(alpha2) + Math.cos(alpha1) * Math.cos(alpha3));
        double lat3 = Math.asin(Math.sin(lat1) * Math.cos(dist13) + Math.cos(lat1) * Math.sin(dist13) * Math.cos(brng13));
        double dLon13 = Math.atan2(Math.sin(brng13) * Math.sin(dist13) * Math.cos(lat1),
                Math.cos(dist13) - Math.sin(lat1) * Math.sin(lat3));
        double lon3 = lon1 + dLon13;
        LatLon latLon = new LatLon(lat3, lon3);

        return latLon;
    }

}
