package io.opensphere.core.projection;

import org.apache.log4j.Logger;

import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Shape;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.viewer.Viewer;

/**
 * Abstract 3 dimensional body.
 */
public abstract class GeographicBody3D extends AbstractGeographicProjection
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GeographicBody3D.class);

    /** Manager for elevation providers. */
    private final ElevationManager myElevationManager = new ElevationManager();

    /** The name of the body. */
    private final String myName;

    /** The shape of the body. */
    private final Shape myShape;

    /**
     * Computes the azimuth angle (clockwise from North) that points from the
     * first location to the second location. This angle can be used as the
     * starting azimuth for a great circle arc that begins at the first
     * location, and passes through the second location.
     *
     * @param start The first location
     * @param end The second location
     * @return Angle in degrees that points from the first location to the
     *         second location.
     */
    public static double greatCircleAzimuthD(LatLonAlt start, LatLonAlt end)
    {
        double startLat = start.getLatD() * MathUtil.DEG_TO_RAD;
        double startLon = start.getLonD() * MathUtil.DEG_TO_RAD;
        double endLat = end.getLatD() * MathUtil.DEG_TO_RAD;
        double endLon = end.getLonD() * MathUtil.DEG_TO_RAD;

        if (MathUtil.isZero(startLon - endLon))
        {
            if (MathUtil.isZero(startLat - endLat))
            {
                return 0.;
            }

            return startLat > endLat ? Math.PI : 0.;
        }

        // Taken from "Map Projections - A Working Manual", page 30, equation
        // 5-4b. The atan2() function is used in place of the traditional
        // atan(y/x) to simplify the case when x is 0.
        double y = Math.cos(endLat) * Math.sin(endLon - startLon);
        double x = Math.cos(startLat) * Math.sin(endLat) - Math.sin(startLat) * Math.cos(endLat) * Math.cos(endLon - startLon);
        double azimuthRadians = Math.atan2(y, x);

        if (Double.isNaN(azimuthRadians))
        {
            LOGGER.error("greatCircleAzimuth calculation produced undefined results.");
            return 0.;
        }
        return azimuthRadians * MathUtil.RAD_TO_DEG;
    }

    /**
     * Computes the great circle distance along the globe in meters.
     *
     * @param start The first location.
     * @param end The second location.
     * @param radiusM The radius of the sphere in meters.
     * @return The great circle distance along the globe in meters.
     */
    public static double greatCircleDistanceM(LatLonAlt start, LatLonAlt end, double radiusM)
    {
        return greatCircleDistanceR(start, end) * radiusM;
    }

    /**
     * Computes the great circle angular distance between two locations. The
     * return value gives the distance as the angle between the two positions on
     * the unit circle. To compute an approximate globe distance in meters from
     * this value, multiply it by the radius of the globe.
     *
     * @param start The first location.
     * @param end The second location.
     * @return The angular distance between the two locations in radians. This
     *         value is the arc length on the unit circle.
     */
    public static double greatCircleDistanceR(LatLonAlt start, LatLonAlt end)
    {
        double startLat = start.getLatD() * MathUtil.DEG_TO_RAD;
        double startLon = start.getLonD() * MathUtil.DEG_TO_RAD;
        double endLat = end.getLatD() * MathUtil.DEG_TO_RAD;
        double endLon = end.getLonD() * MathUtil.DEG_TO_RAD;

        if (MathUtil.isZero(startLon - endLon) && MathUtil.isZero(startLat - endLat))
        {
            return 0.;
        }

        // Taken from "Map Projections - A Working Manual", page 30, equation
        // 5-3a. The traditional d=2*asin(a) form has been replaced with
        // d=2*atan2(sqrt(a), sqrt(1-a)) to reduce rounding errors with large
        // distances.
        double a = Math.sin((endLat - startLat) / 2.0) * Math.sin((endLat - startLat) / 2.0) + Math.cos(startLat)
                * Math.cos(endLat) * Math.sin((endLon - startLon) / 2.0) * Math.sin((endLon - startLon) / 2.0);
        double distanceRadians = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        if (Double.isNaN(distanceRadians))
        {
            LOGGER.error("greatCircleDistance calculation produced undefined results.");
            return 0.;
        }

        return distanceRadians;
    }

    /**
     * Computes the location on a great circle arc with the given starting
     * location, azimuth, and arc distance.
     *
     * @param start LatLonAlt of the starting location.
     * @param greatCircleAzimuthR great circle azimuth angle in radians
     *            (clockwise from North 0 - 2PI).
     * @param pathLengthR arc distance to travel in radians (Should always be
     *            positive).
     * @return The location (as LatLonAlt) on the great circle arc.
     */
    public static LatLonAlt greatCircleEndPosition(LatLonAlt start, double greatCircleAzimuthR, double pathLengthR)
    {
        if (MathUtil.isZero(pathLengthR))
        {
            return start;
        }

        double lat = Math.toRadians(start.getLatD());
        double lon = Math.toRadians(start.getLonD());

        double sinLat = Math.sin(lat);
        double cosLat = Math.cos(lat);
        double sinDistance = Math.sin(pathLengthR);
        double cosDistance = Math.cos(pathLengthR);
        double sinAzimuth = Math.sin(greatCircleAzimuthR);
        double cosAzimuth = Math.cos(greatCircleAzimuthR);

        double sinDcosA = sinDistance * cosAzimuth;

        // Taken from "Map Projections - A Working Manual", page 31, equation
        // 5-5 and 5-6.
        double endLatRadians = Math.asin(sinLat * cosDistance + cosLat * sinDcosA);
        double endLonRadians = lon + Math.atan2(sinDistance * sinAzimuth, cosLat * cosDistance - sinLat * sinDcosA);

        if (Double.isNaN(endLatRadians) || Double.isNaN(endLonRadians))
        {
            LOGGER.error("GreatCircleEndPosition calculation produced undefined results.");
            return start;
        }

        // Check that longitude is always in the range -180 to 180
        if (Math.toDegrees(endLonRadians) > 180.)
        {
            endLonRadians -= Math.toRadians(360);
        }
        if (Math.toDegrees(endLonRadians) < -180.)
        {
            endLonRadians += Math.toRadians(360);
        }

        return LatLonAlt.createFromDegrees(Math.toDegrees(endLatRadians), Math.toDegrees(endLonRadians));
    }

    /**
     * Computes the location on a great circle arc with the given starting
     * location, azimuth, radius of the circle, and distance.
     *
     * @param start LatLonAlt of the starting location.
     * @param greatCircleAzimuthR great circle azimuth angle in radians
     *            (clockwise from North 0 - 2PI).
     * @param radiusM The radius of the circle (geographic body) in meters.
     * @param pathLengthM The length to travel along the path in meters.
     * @return The location (as LatLonAlt) on the great circle arc.
     */
    public static LatLonAlt greatCircleEndPosition(LatLonAlt start, double greatCircleAzimuthR, double radiusM,
            double pathLengthM)
    {
        // The distance on the sphere
        double pathLengthR = pathLengthM / radiusM;
        return greatCircleEndPosition(start, greatCircleAzimuthR, pathLengthR);
    }

    /**
     * Determine the position which is a given percentage from the start point
     * to the end point along the great circle.
     *
     * @param start Start position.
     * @param end End position.
     * @param percent Percentage from start to end.
     * @return The position on the great circle which is a given percentage from
     *         the start point to the end point.
     */
    public static LatLonAlt greatCircleInterpolate(LatLonAlt start, LatLonAlt end, double percent)
    {
        double azimuthD = greatCircleAzimuthD(start, end);
        double lengthR = greatCircleDistanceR(start, end) * percent;
        LatLonAlt lla = greatCircleEndPosition(start, azimuthD * MathUtil.DEG_TO_RAD, lengthR);
        double alt = MathUtil.lerp(percent, start.getAltM(), end.getAltM());
        return LatLonAlt.createFromDegreesMeters(lla.getLatD(), lla.getLonD(), alt, start.getAltitudeReference());
    }

    /**
     * Construct the body.
     *
     * @param name The name of the body.
     * @param shape The shape of the body.
     */
    public GeographicBody3D(String name, Shape shape)
    {
        myName = name;
        myShape = shape;
    }

    @Override
    public ElevationManager getElevationManager()
    {
        return myElevationManager;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    /**
     * Get the shape of the body.
     *
     * @return The shape.
     */
    public Shape getShape()
    {
        return myShape;
    }

    @Override
    public Vector3d getSurfaceIntersection(Vector3d pointA, Vector3d pointB)
    {
        return getShape().getIntersection(new Ray3d(pointA, pointB.subtract(pointA)));
    }

    @Override
    public Vector3d getTerrainIntersection(Ray3d ray, Viewer view)
    {
        return getShape().getIntersection(ray);
    }
}
