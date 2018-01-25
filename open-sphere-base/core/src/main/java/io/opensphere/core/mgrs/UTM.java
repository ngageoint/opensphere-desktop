package io.opensphere.core.mgrs;

import org.apache.log4j.Logger;

import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;

/**
 * This class handles UTM conversions. TODO Currently using WGS 84 but would be
 * nice to be able to use others
 */
public class UTM
{
    /** False easting. */
    private static final double FALSE_EASTING = 500000d;

    /** Logger used. */
    private static final Logger LOGGER = Logger.getLogger(UTM.class);

    /**
     * The Scale factor is an arbitrary reduction applied to all geodetic
     * lengths to reduce the maximum scale distortion of the projection.
     **/
    private static final double SCALE = 0.9996d;

    /** The easting. */
    private final double myEasting;

    /** The hemisphere (North or South). */
    private final Hemisphere myHemisphere;

    /** The northing. */
    private final double myNorthing;

    /** The zone. */
    private final int myZone;

    /**
     * Constructor.
     *
     * @param latD The latitude of the position I represent in degrees.
     * @param lonD The longitude of the position I represent in degrees.
     */
    public UTM(double latD, double lonD)
    {
        double falseNorthing = 0;
        if (latD < 0.)
        {
            falseNorthing = 10000000;
            myHemisphere = Hemisphere.SOUTH;
        }
        else
        {
            myHemisphere = Hemisphere.NORTH;
        }

        // Make sure the longitude is in the -180 to 179.999... range.
        double lon = lonD + 180. - (int)((lonD + 180.) / 360.) * 360. - 180.;

        // First find the zone number.
        myZone = findZone(latD, lon);

        final double zoneHalfWidth = 3.;
        double orginLongitude = (myZone - 1) * 6 - 180. + zoneHalfWidth;

        double φ = Math.toRadians(latD);
        double λ = Math.toRadians(lon);
        double λ0 = Math.toRadians(orginLongitude);
        double Δλ = λ - λ0;

        double sinφ = Math.sin(φ);
        double sin2φ = sinφ * sinφ;
        double cosφ = Math.cos(φ);
        double cos2φ = cosφ * cosφ;
        double tanφ = sinφ / cosφ;
        double tan2φ = tanφ * tanφ;
        double tan4φ = tanφ * tanφ;

        double ePrime2 = WGS84EarthConstants.SECOND_ECCENTRICITY_SQ;
        double e2 = WGS84EarthConstants.FIRST_ECCENTRICITY_SQ;
        double e4 = e2 * e2;
        double e6 = e4 * e2;
        double semiMajor = WGS84EarthConstants.SEMI_MAJOR_AXIS_M;

        double v = 1 / Math.sqrt(1 - e2 * sin2φ);
        double a = Δλ * cosφ;
        double a2 = a * a;
        double a3 = a2 * a;
        double a4 = a3 * a;
        double a5 = a4 * a;
        double a6 = a5 * a;

        final double quarter = 0.25;
        // 3/64
        final double n3d64 = 0.046875;
        // 5/256
        final double n5d256 = 0.01953125;
        // 3/8
        final double n3d8 = 0.375;
        // 3/32
        final double n3d32 = 0.09375;
        // 45/1024
        final double n45d1024 = 0.043945313;
        // 15/256
        final double n15d156 = 0.05859375;
        // 35/3072
        final double n35d3072 = 0.011393229;

        double s = (1. - quarter * e2 - n3d64 * e4 - n5d256 * e6) * φ - (n3d8 * e2 + n3d32 * e4 + n45d1024 * e6) * Math.sin(2 * φ)
                + (n15d156 * e4 + n45d1024 * e6) * Math.sin(4 * φ) - n35d3072 * e6 * Math.sin(6 * φ);

        double c = ePrime2 * cos2φ;
        double c2 = c * c;
        myEasting = FALSE_EASTING + SCALE * semiMajor * v * (a + (1 - tan2φ + c) * a3 / 6 + (5 - 18 * tan2φ + tan4φ) * a5 / 120);
        myNorthing = falseNorthing + SCALE * semiMajor
                * (s + v * tanφ * (a2 * .5 + (5 - tan2φ + 9 * c + 4 * c2) * a4 / 24 + (61 - 58 * tan2φ + tan4φ) * a6 / 720));
    }

    /**
     * Constructor.
     *
     * @param geoPos The geographic position (lat/lon).
     */
    public UTM(GeographicPosition geoPos)
    {
        this(geoPos.getLatLonAlt().getLatD(), geoPos.getLatLonAlt().getLonD());
    }

    /**
     * Constructor.
     *
     * @param zone The zone.
     * @param hemisphere The hemisphere.
     * @param easting The easting.
     * @param northing The northing.
     */
    public UTM(int zone, Hemisphere hemisphere, double easting, double northing)
    {
        if (zone < 0 || zone > 60)
        {
            throw new IllegalArgumentException("Zone is outside of allowable range (0 - 60)");
        }
        myZone = zone;
        myHemisphere = hemisphere;

        if (easting < 167000 || easting > 833000)
        {
            throw new IllegalArgumentException("Easting is outside of allowable range (0 - 99999)");
        }
        myEasting = easting;

        if (northing < 0 || northing > 10000000)
        {
            throw new IllegalArgumentException("Northing is outside of allowable range (0 - 99999)");
        }
        myNorthing = northing;
    }

    /**
     * Convert to lat/lon (GeographicPosition).
     *
     * @return The geographic position.
     */
    public GeographicPosition convertToLatLon()
    {
        double ecc2 = WGS84EarthConstants.FIRST_ECCENTRICITY_SQ;
        double ecc4 = WGS84EarthConstants.FIRST_ECCENTRICITY_FOURTH;
        double ecc6 = WGS84EarthConstants.FIRST_ECCENTRICITY_SIXTH;

        double e = (1 - Math.sqrt(1 - ecc2)) / (1 + Math.sqrt(1 - ecc2));
        double e2 = e * e;
        double e3 = e2 * e;
        double e4 = e3 * e;

        double x = myEasting - 500000;
        double y = myNorthing;
        if (myHemisphere == Hemisphere.SOUTH)
        {
            y -= 10000000;
        }

        final double zoneHalfWidth = 3.;
        double orginLongitude = (myZone - 1) * 6 - 180. + zoneHalfWidth;

        double m = y / SCALE;

        double mu = m / (WGS84EarthConstants.RADIUS_EQUATORIAL_M * (1 - ecc2 / 4 - 3 * ecc4 / 64 - 5 * ecc6 / 256));

        double phiRad = mu + (3 * e / 2 - 27 * e3 / 32) * Math.sin(2 * mu) + (21 * e2 / 16 - 55 * e4 / 32) * Math.sin(4 * mu)
                + 151 * e3 / 96 * Math.sin(6 * mu);
        double phiRadSin = Math.sin(phiRad);
        double phiRadCos = Math.cos(phiRad);
        double phiRadTan = phiRadSin / phiRadCos;

        double n = WGS84EarthConstants.RADIUS_EQUATORIAL_M / Math.sqrt(1 - ecc2 * phiRadSin * phiRadSin);
        double t = phiRadTan * phiRadTan;
        double t2 = t * t;
        double ePrime2 = WGS84EarthConstants.SECOND_ECCENTRICITY_SQ;
        double c = ePrime2 * phiRadCos * phiRadCos;
        double c2 = c * c;
        final double onePointFive = 1.5;
        double r = WGS84EarthConstants.RADIUS_EQUATORIAL_M * (1 - ecc2)
                / Math.pow(1 - ecc2 * phiRadSin * phiRadSin, onePointFive);
        double d = x / (n * SCALE);
        double d2 = d * d;
        double d3 = d2 * d;
        double d4 = d3 * d;
        double d5 = d4 * d;
        double d6 = d5 * d;

        double latitudeRad = phiRad - n * phiRadTan / r * (d2 / 2 - (5 + 3 * t + 10 * c - 4 * c2 - 9 * ePrime2) * d4 / 24
                + (61 + 90 * t + 298 * c + 45 * t2 - 252 * ePrime2 - 3 * c2) * d6 / 720);

        double longitudeRad = (d - (1 + 2 * t + c) * d3 / 6 + (5 - 2 * c + 28 * t - 3 * c2 + 8 * ePrime2 + 24 * t2) * d5 / 120)
                / phiRadCos;

        double longitudeDeg = orginLongitude + Math.toDegrees(longitudeRad);

        GeographicPosition geoPos = new GeographicPosition(
                LatLonAlt.createFromDegrees(Math.toDegrees(latitudeRad), longitudeDeg).getNormalized());

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(" Latitude = " + Math.toDegrees(latitudeRad) + " Longitude = " + longitudeDeg);
        }

        return geoPos;
    }

    /**
     * Standard getter.
     *
     * @return The easting value.
     */
    public double getEasting()
    {
        return myEasting;
    }

    /**
     * Standard getter.
     *
     * @return The hemisphere.
     */
    public Hemisphere getHemisphere()
    {
        return myHemisphere;
    }

    /**
     * Standard getter.
     *
     * @return The northing value.
     */
    public double getNorthing()
    {
        return myNorthing;
    }

    /**
     * Standard getter.
     *
     * @return The zone.
     */
    public int getZone()
    {
        return myZone;
    }

    /**
     * Determines the zone from the longitude (accounts for special cases).
     *
     * @param latitude The latitude (degrees).
     * @param longitude The longitude (degrees).
     * @return The zone.
     */
    private int findZone(double latitude, double longitude)
    {
        int zone = (int)((longitude + 180) / 6) + 1;

        // There are special cases that we need to check for.
        // Norway
        if (latitude >= 56 && latitude < 64)
        {
            if (longitude >= 3 && longitude < 12)
            {
                zone = 32;
            }
        }
        // X band in artic circle
        else if (latitude >= 72 && latitude < 84)
        {
            if (longitude >= 0 && longitude < 9)
            {
                zone = 31;
            }
            else if (longitude >= 9 && longitude < 21)
            {
                zone = 33;
            }
            else if (longitude >= 21 && longitude < 33)
            {
                zone = 35;
            }
            else if (longitude >= 33 && longitude < 42)
            {
                zone = 37;
            }
        }
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("The calculated zone = " + zone);
        }

        return zone;
    }

    /** The hemisphere. */
    public enum Hemisphere
    {
        /** The north hemisphere. */
        NORTH,

        /** The south hemisphere. */
        SOUTH
    }
}
