package io.opensphere.core.common.convolve;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.opensphere.core.math.WGS84EarthConstants;

/**
 * This class converts localization problems on the Earth into more tractable
 * problems in the plane.  The LeastSqLineLocator is then used to estimate
 * locations in the plane, along with the 95% confidence ellipse based on the
 * inverse exponential sum of squared distances probability density.
 * <br><br>
 * The method of the conversion is orthographic projection onto a plane
 * tangent to the idealized spherical Earth.  Though not mathematically
 * precise, this approach produces very good results when the inputs are
 * sufficiently localized.  For less localized problems, a gnomonic projection
 * might give better results, though that is not currently supported.
 */
public class PlaneUtils
{
    /** Presumed confidence probability. */
    private static double P_CONF = 0.95;

    /**
     * Convert a confidence probability into the radius of a disk containing
     * that portion of the probability measure for the standard bivariate
     * normal distribution [i.e., f(x, y) = e^(-x^2 - y^2)].
     *
     * @param p the confidence probability
     * @return the confidence disk radius
     */
    private static double stdConfRadius(double p)
    {
        return Math.sqrt(Math.log(1.0 / (1.0 - p)));
    }

    /** The confidence radius for the presumed confidence probability. */
    public static final double CONF_95_RADIUS = stdConfRadius(P_CONF);

    /** Conversion factor for meters and nautical miles. */
    private static double M_PER_NMI = 1852;

    /** Equatorial radius of Earth in nautical miles. */
    private static double EARTH_R_NMI = WGS84EarthConstants.RADIUS_EQUATORIAL_M / M_PER_NMI;

    /** The projection from Earth to an Euclidian plane. */
    private Projection proj;

    /** Latitude (in degrees) of the output location. */
    private double locLatDeg;

    /** Longitude (in degrees) of the output location. */
    private double locLonDeg;

    /** Orientation (degrees east of north) of the output ellipse. */
    private double orientDeg;

    /** The major radius (semimajor axis) of the output ellipse. */
    private double majorRadiusNmi;

    /** The minor radius (semiminor axis) of the output ellipse. */
    private double minorRadiusNmi;

    /**
     * A list of indices for lines-of-bearing that do not appear to point
     * toward the concensus location.  These outliers may be rejected before
     * recalculating.
     */
    private List<Integer> badPointers = new LinkedList<>();

    /** Report on problems that arise during the calculation. */
    private String errorMessage = null;

    /**
     * Get the latitude in degrees.
     *
     * @return the latitude
     */
    public double getLatitude()
    {
        return locLatDeg;
    }

    /**
     * Get the longitude in degrees.
     *
     * @return the longitude
     */
    public double getLongitude()
    {
        return locLonDeg;
    }

    /**
     * Get the orientation in degrees east of north.
     *
     * @return the orientation
     */
    public double getOrientation()
    {
        return orientDeg;
    }

    /**
     * Get the major radius (semimajor axis) in nautical miles.
     *
     * @return the major radius
     */
    public double getMajorRadius()
    {
        return majorRadiusNmi;
    }

    /**
     * Get the minor radius (semiminor axis) in nautical miles.
     *
     * @return the minor radius
     */
    public double getMinorRadius()
    {
        return minorRadiusNmi;
    }

    /**
     * Find out what, if anything, went wrong during the calculation.
     *
     * @return the error message, if any, or null
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }

    /**
     * Get the list of indices of lines-of-bearing that should probably have
     * been excluded from the calculation.
     *
     * @return indices of the bad inputs
     */
    public List<Integer> getBadPointers()
    {
        return badPointers;
    }

    /**
     * Perform the least-square-distances localization for the specified set
     * of lines, expressed as triples of latitude, longitude, and bearing.
     *
     * @param lobs the line-of-bearing inputs
     */
    public void convolve(List<LatLonBear> lobs)
    {
        // first find a point on which to center the projection
        R3 avg = new R3();
        for (LatLonBear llb :  lobs)
        {
            avg.add(fromLatLon(llb.latDeg, llb.lonDeg));
        }
        avg.nz();

        // create the projection
        proj = Projection.atSpherePoint(avg);

        // transform data to tangent plane
        List<LobVec> lobVecList = lobs.stream().map(llb -> proj.tangentVec(llb)).collect(Collectors.toList());

        // create workspace and insert data
        LeastSqLineLocator lsqLoc = new LeastSqLineLocator();
        lsqLoc.setNumLines(lobVecList.size());
        int i = 0;
        for (LobVec v :  lobVecList)
        {
            lsqLoc.addLine(v.p0.x, v.p0.y, v.dirUnit.x, v.dirUnit.y, i++);
        }
        lsqLoc.localize();
        errorMessage = lsqLoc.getErrorMessage();
        if (errorMessage != null)
        {
            return;
        }

        // see if any lines point away from the convergence
        for (int j = 0; j < lobs.size(); j++)
        {
            if (!lsqLoc.checkPointingAngle(j, 30.0))
            {
                badPointers.add(j);
            }
        }

        // extract and convert location data
        double[] lsqPoint = lsqLoc.getLocation();
        R3 planeLoc = new R3(lsqPoint[0], lsqPoint[1], 0.0);
        R3 sphereLoc = proj.toSphere(planeLoc);
        locLatDeg = latitudeOf(sphereLoc);
        locLonDeg = longitudeOf(sphereLoc);

        // get the matrix representing the ellipse
        double[] lambda = lsqLoc.getEigenvals();
        double[][] basis = lsqLoc.getEigenVecs();
        if (lambda.length == 1)
        {
            // circular case
            orientDeg = 0.0;
            minorRadiusNmi = CONF_95_RADIUS * Math.sqrt(EARTH_R_NMI / lambda[0]);
            majorRadiusNmi = minorRadiusNmi;
        }
        else
        {
            // elliptical case
            orientDeg = Math.toDegrees(angleOf(basis[1][1], basis[1][0]));
            if (orientDeg < 0.0)
            {
                orientDeg += 180.0;
            }
            minorRadiusNmi = CONF_95_RADIUS * Math.sqrt(EARTH_R_NMI / lambda[0]);
            majorRadiusNmi = CONF_95_RADIUS * Math.sqrt(EARTH_R_NMI / lambda[1]);
        }
    }

    /** Line-of-bearing in geodetic coordinates. */
    public static class LatLonBear
    {
        /** Latitude in degrees. */
        public double latDeg;

        /** Longitude in degrees. */
        public double lonDeg;

        /** Bearing in degrees east of north. */
        public double bearDeg;

        /**
         * Construct from the three component field values.
         *
         * @param lat the latitude
         * @param lon the longitude
         * @param bear the bearing
         */
        public LatLonBear(double lat, double lon, double bear)
        {
            latDeg = lat;
            lonDeg = lon;
            bearDeg = bear;
        }
    }

    /**
     * Find the latitude in degrees for the cartesian point.
     *
     * @param p a point in three-space
     * @return the latitude of <i>p</i>
     */
    private static double latitudeOf(R3 p)
    {
        return Math.toDegrees(Math.atan(p.z / Math.sqrt(sq(p.x) + sq(p.y))));
    }

    /**
     * Find the longitude in degrees for the cartesian point.
     *
     * @param p a point in three-space
     * @return the longitude of <i>p</i>
     */
    private static double longitudeOf(R3 p)
    {
        return Math.toDegrees(angleOf(p.x, p.y));
    }

    /**
     * Find the standard plane angle for point (<i>x</i>, <i>y</i>).
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return the angle (radians) counter-clockwise of the positive x-axis
     */
    private static double angleOf(double x, double y)
    {
        if (x > 0.0)
        {
            return Math.atan(y / x);
        }
        if (y > 0.0)
        {
            return Math.PI + Math.atan(y / x);
        }
        if (y < 0.0)
        {
            return -Math.PI + Math.atan(y / x);
        }
        return -Math.PI;
    }

    /**
     * Square function.
     *
     * @param x a value
     * @return <i>x</i> squared
     */
    private static double sq(double x)
    {
        return x * x;
    }

    /**
     * Convert geodetic coordinates into cartesian 3-space.
     *
     * @param latDeg latitude in degrees
     * @param lonDeg longitude in degrees
     * @return a point in 3-space
     */
    private static R3 fromLatLon(double latDeg, double lonDeg)
    {
        double latRad = Math.toRadians(latDeg);
        double lonRad = Math.toRadians(lonDeg);
        double cosLat = Math.cos(latRad);
        R3 u = new R3();
        u.x = cosLat * Math.cos(lonRad);
        u.y = cosLat * Math.sin(lonRad);
        u.z = Math.sin(latRad);
        return u;
    }

    /**
     * This class does bidirectional conversion between points on the sphere
     * and corresponding points on the tangent plane.  Coordinates in the
     * plane have <i>x</i> pointing east and <i>y</i> pointing north.
     * <br><br>
     * The projection used by this class is orthographic. It is gives good
     * results when the problem space is not too large (e.g., radius less than
     * 20 or 30 degrees of latitude and/or longitude).
     * <br><br>
     * If that seems to be too limiting, we could use a gnomonic projection
     * instead.
     */
    private static class Projection
    {
        /** The unit x vector. */
        private R3 xVec;

        /** The unit x vector. */
        private R3 yVec;

        /** The unit x vector. */
        private R3 zVec;

        /**
         * Construct a Projection for a plane tangent to the unit sphere in
         * the specified direction.
         *
         * @param origin the vector indicating the plane's origin
         * @return the created Projection instance
         */
        public static Projection atSpherePoint(R3 origin)
        {
            Projection p = new Projection();
            p.zVec = origin.unit();
            p.yVec = p.zVec.projOrth(new R3(0.0, 0.0, 1.0));
            p.yVec.nz();
            // the cross product should complete the orthonormal basis, but
            // accuracy can be significantly improved by explicitly
            // orthogonalizing to y and z and then normalizing
            p.xVec = p.yVec.projOrth(p.zVec.projOrth(p.yVec.cross(p.zVec)));
            p.xVec.nz();
            return p;
        }

        /**
         * Convert a point in space to a point on the tangent plane.
         *
         * @param u a point in space
         * @return the projection of <i>u</i> onto the plane
         */
        public R3 toTangent(R3 u)
        {
            return new R3(xVec.dot(u), yVec.dot(u), 0.0);
        }

        /**
         * Convert a point on the tangent plane into a point on the sphere.
         *
         * @param u a point on the plane
         * @return the projection of <i>u</i> onto the sphere
         */
        public R3 toSphere(R3 u)
        {
            R3 v = new R3();
            v.addMult(u.x, xVec);
            v.addMult(u.y, yVec);
            v.addMult(Math.sqrt(1.0 - sq(u.x) - sq(u.y)), zVec);
            return v;
        }

        /**
         * Convert the latitude, longitude, and bearing angle in geodetic
         * coordinates into a corresponding position and bearing vector in the
         * tangent plane.
         *
         * @param llb Latitude, longitude and bearing angle
         * @return the converted line-of-bearing
         */
        public LobVec tangentVec(LatLonBear llb)
        {
            return tangentVec(llb.latDeg, llb.lonDeg, llb.bearDeg);
        }

        /**
         * Convert standard line-of-bearing parameters to a directional vector.
         *
         * @param latDeg latitude in degrees
         * @param lonDeg longitude in degrees
         * @param bearingDeg bearing in degrees east of north
         * @return a line-of-bearing vector
         */
        public LobVec tangentVec(double latDeg, double lonDeg, double bearingDeg)
        {
            double bearingRad = Math.toRadians(bearingDeg);
            LobVec lv = new LobVec();
            lv.p0 = toTangent(fromLatLon(latDeg, lonDeg));
            lv.dirUnit = new R3(Math.sin(bearingRad), Math.cos(bearingRad), 0.0);
            return lv;
        }
    }

    /** Line-of-bearing in the tangent plane. */
    private static class LobVec
    {
        /** The point of origin. */
        public R3 p0;

        /** The directional unit vector. */
        public R3 dirUnit;
    }

    /** Standard mutable vector in Euclidian 3-space. */
    private static class R3
    {
        /** X-coordinate. */
        public double x;

        /** Y-coordinate. */
        public double y;

        /** Z-coordinate. */
        public double z;

        /** Constructs the zero vector. */
        public R3()
        {
        }

        /**
         * Constructs the specified vector.
         *
         * @param xVal X-coordinate
         * @param yVal Y-coordinate
         * @param zVal Z-coordinate
         */
        public R3(double xVal, double yVal, double zVal)
        {
            x = xVal;
            y = yVal;
            z = zVal;
        }

        /** Creates a copy of this vector. */
        public R3 copy()
        {
            return new R3(x, y, z);
        }

        /**
         * Multiplies this vector by a scalar.
         *
         * @param a a scalar value
         */
        public void mult(double a)
        {
            x *= a;
            y *= a;
            z *= a;
        }

        /**
         * Creates a new vector as a scalar multiple of this one.
         *
         * @param a the scalar
         * @return the scalar multiple
         */
        public R3 times(double a)
        {
            return new R3(a * x, a * y, a * z);
        }

        /**
         * Add another vector to this one.
         *
         * @param u the vector to add
         */
        public void add(R3 u)
        {
            x += u.x;
            y += u.y;
            z += u.z;
        }

        /**
         * Add a scalar multiple of another vector to this one.
         *
         * @param a the scalar
         * @param u the vector
         */
        public void addMult(double a, R3 u)
        {
            x += a * u.x;
            y += a * u.y;
            z += a * u.z;
        }

        /** Normalize this vector. */
        public void nz()
        {
            mult(1.0 / mag());
        }

        /**
         * Construct a unit-length scalar multiple of this vector.
         *
         * @return a unit vector
         */
        public R3 unit()
        {
            return times(1.0 / mag());
        }

        /**
         * Find the inner product of this vector with another.
         *
         * @param u the other vector
         * @return the dot product
         */
        public double dot(R3 u)
        {
            return x * u.x + y * u.y + z * u.z;
        }

        /**
         * Find the vector cross product of this vector and another.
         *
         * @param u the other vector
         * @return the cross product
         */
        public R3 cross(R3 u)
        {
            return new R3(y * u.z - z * u.y, z * u.x - x * u.z, x * u.y - y * u.z);
        }

        /**
         * Find the squared magnitude of this vector.
         *
         * @return squared magnitude
         */
        public double magSq()
        {
            return dot(this);
        }

        /**
         * Find the magnitude of this vector.
         *
         * @return the magnitude
         */
        public double mag()
        {
            return Math.sqrt(magSq());
        }

        /**
         * Find the projection of a vector onto the orthogonal complement of
         * this one.  Note:  only call on unit vectors.
         *
         * @param u the vector
         * @return the projection
         */
        public R3 projOrth(R3 u)
        {
            R3 v = copy();
            v.mult(-dot(u));
            v.add(u);
            return v;
        }
    }
}
