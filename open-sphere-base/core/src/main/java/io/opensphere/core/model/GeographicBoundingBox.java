package io.opensphere.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * A bounding box using geographic coordinates.
 */
@SuppressWarnings("PMD.GodClass")
public class GeographicBoundingBox implements BoundingBox<GeographicPosition>, Comparable<GeographicBoundingBox>, Serializable
{
    /** The maximum amount a minimum bounding box can be flattened. */
    public static final double MAX_BOX_FLATTENING = .025;

    /** Singleton representing a box that comprises the whole globe. */
    public static final GeographicBoundingBox WHOLE_GLOBE = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90., -180.),
            LatLonAlt.createFromDegrees(90., 180.));

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The most southern and western corner of the box. */
    private final GeographicPosition myLowerLeftCorner;

    /** The most northern and eastern corner of the box. */
    private final GeographicPosition myUpperRightCorner;

    /**
     * Get the smallest bounding box which contains all of the positions. When
     * the longitudes are positioned so that there are small negative and small
     * positive values and there are also values close to both 180 and -180,
     * this algorithm will give a box which may not be the minimum bounding box.
     *
     * @param positions The positions which must be contained in the box.
     * @return The smallest bounding box which contains all of the positions.
     */
    public static GeographicBoundingBox getMinimumBoundingBox(Collection<? extends GeographicPosition> positions)
    {
        return getMinimumBoundingBoxLLA(positions.stream().map(p -> p.getLatLonAlt()).collect(Collectors.toList()));
    }

    /**
     * Get the smallest bounding box which contains all of the positions. When
     * the longitudes are positioned so that there are small negative and small
     * positive values and there are also values close to both 180 and -180,
     * this algorithm will give a box which may not be the minimum bounding box.
     *
     * @param positions The positions which must be contained in the box.
     * @return The smallest bounding box which contains all of the positions.
     */
    public static GeographicBoundingBox getMinimumBoundingBoxLLA(Collection<? extends LatLonAlt> positions)
    {
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;

        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        double minLonShifted = Double.MAX_VALUE;
        double maxLonShifted = -Double.MAX_VALUE;
        for (LatLonAlt pos : positions)
        {
            double latD = pos.getLatD();
            minLat = Math.min(minLat, latD);
            maxLat = Math.max(maxLat, latD);

            double lonD = pos.getLonD();
            minLon = Math.min(minLon, lonD);
            maxLon = Math.max(maxLon, lonD);

            if (lonD > 0.)
            {
                lonD -= 360.;
            }

            minLonShifted = Math.min(minLonShifted, lonD);
            maxLonShifted = Math.max(maxLonShifted, lonD);
        }

        double deltaLon = maxLon - minLon;
        double deltaLonShifted = maxLonShifted - minLonShifted;

        GeographicBoundingBox bbox;
        if (deltaLon - deltaLonShifted < MathUtil.DBL_EPSILON)
        {
            bbox = createBoxFromEdges(minLat, maxLat, minLon, maxLon, false);
        }
        else
        {
            bbox = createBoxFromEdges(minLat, maxLat, minLonShifted, maxLonShifted, true);
        }
        return bbox;
    }

    /**
     * Get the smallest bounding box which contains all of the positions. When
     * the longitudes are positioned so that there are small negative and small
     * positive values and there are also values close to both 180 and -180,
     * this algorithm will give a box which may not be the minimum bounding box.
     *
     * @param positions The positions which must be contained in the box.
     * @return The smallest bounding box which contains all of the positions.
     */
    public static GeographicBoundingBox getMinimumBoundingBoxWithAlt(Collection<? extends LatLonAlt> positions)
    {
        double maxAltitudeMeters = 0;

        for (LatLonAlt pos : positions)
        {
            if (pos.getAltM() > maxAltitudeMeters)
            {
                maxAltitudeMeters = pos.getAltM();
            }
        }

        GeographicBoundingBox bbox = getMinimumBoundingBoxLLA(positions);

        LatLonAlt lowerLeft = LatLonAlt.createFromDegreesMeters(bbox.getLowerLeft().getLatLonAlt().getLatD(),
                bbox.getLowerLeft().getLatLonAlt().getLonD(), maxAltitudeMeters, ReferenceLevel.ELLIPSOID);
        LatLonAlt upperRight = LatLonAlt.createFromDegreesMeters(bbox.getUpperRight().getLatLonAlt().getLatD(),
                bbox.getUpperRight().getLatLonAlt().getLonD(), maxAltitudeMeters, ReferenceLevel.ELLIPSOID);
        GeographicBoundingBox withAlt = new GeographicBoundingBox(lowerLeft, upperRight);

        return withAlt;
    }

    /**
     * Create a new geographic bounding box that merges two other boxes. The
     * result will have an altitude of 0 using the reference level of the input
     * boxes. If the reference levels do not match, an exception is thrown.
     *
     * @param box1 The first box.
     * @param box2 The second box.
     * @return The union of the two boxes.
     * @throws IllegalArgumentException If the boxes have different altitude
     *             reference levels.
     */
    public static GeographicBoundingBox merge(GeographicBoundingBox box1, GeographicBoundingBox box2)
        throws IllegalArgumentException
    {
        if (box1 == null)
        {
            if (box2 != null)
            {
                return box2;
            }
            return null;
        }
        else if (box2 == null)
        {
            return box1;
        }

        if (box1.getAltitudeReference() != box2.getAltitudeReference())
        {
            throw new IllegalArgumentException(
                    "Incompatible altitudes references: " + box1.getAltitudeReference() + " and " + box2.getAltitudeReference());
        }

        return merge(box1, box2, box1.getAltitudeReference());
    }

    /**
     * Create a new geographic bounding box that merges two other boxes. The
     * result will have an altitude of 0 using the provided reference level.
     *
     * @param box1 The first box.
     * @param box2 The second box.
     * @param refLevel The reference level to use for the altitude.
     * @return The union of the two boxes.
     */
    public static GeographicBoundingBox merge(GeographicBoundingBox box1, GeographicBoundingBox box2,
            Altitude.ReferenceLevel refLevel)
    {
        if (box1 == null)
        {
            if (box2 != null)
            {
                return box2;
            }
            return null;
        }
        else if (box2 == null)
        {
            return box1;
        }

        LatLonAlt box1LowerLeft = box1.getLowerLeft().getLatLonAlt();
        LatLonAlt box1UpperRight = box1.getUpperRight().getLatLonAlt();
        LatLonAlt box2LowerLeft = box2.getLowerLeft().getLatLonAlt();
        LatLonAlt box2UpperRight = box2.getUpperRight().getLatLonAlt();

        // there are two possible bounding boxes
        // bound box 1 is (minLat,minLon) to (maxLat,maxLon)
        // bound box 2 is (minLat,max(box1minLon,box2minLon)) to
        // (maxLat,min(box1maxLon,box2maxLon))
        // this second box reverses the direction around the earth.

        // the min/max lat can be strictly calculated
        double minLat = Math.min(box1LowerLeft.getLatD(), box2LowerLeft.getLatD());
        double maxLat = Math.max(box1UpperRight.getLatD(), box2UpperRight.getLatD());

        double box1minLon = box1LowerLeft.getLonD();
        double box1maxLon = box1UpperRight.getLonD();
        double box2minLon = box2LowerLeft.getLonD();
        double box2maxLon = box2UpperRight.getLonD();

        // Flags to determine if boxes to merge are crossing 180 longitude
        // boundary.
        boolean minCrosses180 = box1LowerLeft.getNormalized().positionsCrossLongitudeBoundary(box2LowerLeft.getNormalized());
        boolean maxCrosses180 = box1UpperRight.getNormalized().positionsCrossLongitudeBoundary(box2UpperRight.getNormalized());

        double minLon;
        double maxLon;
        // Check for special cases around 180 longitude boundary
        if (minCrosses180 && Math.abs(box1LowerLeft.getNormalized().getLonD()) > 90.0
                && Math.abs(box2LowerLeft.getNormalized().getLonD()) > 90.0)
        {
            minLon = Math.max(box1minLon, box2minLon);
        }
        else
        {
            minLon = Math.min(box1minLon, box2minLon);
        }
        if (maxCrosses180 && Math.abs(box1UpperRight.getNormalized().getLonD()) > 90.0
                && Math.abs(box2UpperRight.getNormalized().getLonD()) > 90.0)
        {
            maxLon = Math.min(box1maxLon, box2maxLon);
        }
        else
        {
            maxLon = Math.max(box1maxLon, box2maxLon);
        }

        double minMaxLon = Math.min(box1maxLon, box2maxLon);
        double maxMinLon = Math.max(box1minLon, box2minLon);

        double deltaLon1 = maxLon - minLon;
        if (deltaLon1 < 0.)
        {
            deltaLon1 += Constants.CIRCLE_DEGREES;
        }

        // if deltaLon2 > 360 the box's lon ranges overlap
        double deltaLon2 = minMaxLon - maxMinLon + Constants.CIRCLE_DEGREES;

        // Flag to determine if individual box is crossing 180 longitude
        // boundary.
        boolean crosses180 = box1LowerLeft.getNormalized().positionsCrossLongitudeBoundary(box1UpperRight.getNormalized());
        crosses180 |= box2LowerLeft.getNormalized().positionsCrossLongitudeBoundary(box2UpperRight.getNormalized());

        LatLonAlt llc = null;
        LatLonAlt urc = null;

        if (deltaLon1 < deltaLon2 || crosses180)
        {
            llc = LatLonAlt.createFromDegrees(minLat, minLon, refLevel);
            urc = LatLonAlt.createFromDegrees(maxLat, maxLon, refLevel);
        }
        else
        {
            llc = LatLonAlt.createFromDegrees(minLat, maxMinLon, refLevel);
            urc = LatLonAlt.createFromDegrees(maxLat, minMaxLon, refLevel);
        }

        return new GeographicBoundingBox(llc, urc);
    }

    /**
     * Create the box given the minimum and maximum latitudes and longitudes.
     * This method will also ensure that the box is not degenerate.
     *
     * @param minLat Minimum latitude of the box.
     * @param maxLat Maximum latitude of the box.
     * @param minLon Minimum longitude of the box.
     * @param maxLon Maximum longitude of the box.
     * @param lonShifted should be set to true when the longitude has been
     *            shifted by -360.
     * @return The newly created box.
     */
    private static GeographicBoundingBox createBoxFromEdges(double minLat, double maxLat, double minLon, double maxLon,
            boolean lonShifted)
    {
        double deltaLat = maxLat - minLat;
        double deltaLon = maxLon - minLon;
        double minDeltaLon = deltaLat * MAX_BOX_FLATTENING;
        double minDeltaLat = deltaLon * MAX_BOX_FLATTENING;

        double adjustedMinLat = minLat;
        double adjustedMaxLat = maxLat;
        double adjustedMinLon = minLon;
        double adjustedMaxLon = maxLon;
        if (deltaLat < minDeltaLat)
        {
            double shift = (minDeltaLat - deltaLat) * 0.5;
            adjustedMinLat -= shift;
            adjustedMaxLat += shift;
        }
        else if (deltaLon < minDeltaLon)
        {
            double shift = (minDeltaLon - deltaLon) * 0.5;
            adjustedMinLon -= shift;
            adjustedMaxLon += shift;
        }
        if (lonShifted)
        {
            adjustedMinLon += 360.;
        }
        GeographicPosition ll = new GeographicPosition(LatLonAlt.createFromDegrees(adjustedMinLat, adjustedMinLon));
        GeographicPosition ur = new GeographicPosition(LatLonAlt.createFromDegrees(adjustedMaxLat, adjustedMaxLon));
        return new GeographicBoundingBox(ll, ur);
    }

    /**
     * Construct the bounding box.
     *
     * @param lowerLeftCorner the lower-left corner
     * @param upperRightCorner the upper-right corner
     */
    public GeographicBoundingBox(GeographicPosition lowerLeftCorner, GeographicPosition upperRightCorner)
    {
        this(lowerLeftCorner.getLatLonAlt(), upperRightCorner.getLatLonAlt());
    }

    /**
     * Construct the bounding box.
     *
     * @param lowerLeftCorner the lower-left corner
     * @param upperRightCorner the upper-right corner
     */
    public GeographicBoundingBox(LatLonAlt lowerLeftCorner, LatLonAlt upperRightCorner)
    {
        if (lowerLeftCorner.getAltitudeReference() != upperRightCorner.getAltitudeReference())
        {
            throw new IllegalArgumentException("Incompatible altitudes references: " + lowerLeftCorner.getAltitudeReference()
                    + " and " + upperRightCorner.getAltitudeReference());
        }

        if (upperRightCorner.getLatD() < lowerLeftCorner.getLatD())
        {
            myLowerLeftCorner = new GeographicPosition(upperRightCorner);
            myUpperRightCorner = new GeographicPosition(lowerLeftCorner);
        }
        else
        {
            myLowerLeftCorner = new GeographicPosition(lowerLeftCorner);
            myUpperRightCorner = new GeographicPosition(upperRightCorner);
        }
    }

    /**
     * Create a polygon that represents this bounding box.
     *
     * @return A polygon that represents this bounding box.
     */
    public GeographicPolygon asGeographicPolygon()
    {
        return new GeographicConvexPolygon(getVertices());
    }

    /**
     * {@inheritDoc}
     *
     * This method intentionally ignores any altitude since altitude does not
     * alter the geographic region which is bounded.
     */
    @Override
    public int compareTo(GeographicBoundingBox other)
    {
        LatLonAlt ll = myLowerLeftCorner.getLatLonAlt();
        LatLonAlt ur = myUpperRightCorner.getLatLonAlt();
        LatLonAlt otherll = other.myLowerLeftCorner.getLatLonAlt();
        LatLonAlt otherur = other.myUpperRightCorner.getLatLonAlt();

        // Compare the lower left longitudes.
        int result = MathUtil.isZero(ll.getLonD() - otherll.getLonD()) ? 0 : ll.getLonD() < otherll.getLonD() ? -1 : 1;

        // Compare the lower left latitudes.
        if (result == 0)
        {
            result = MathUtil.isZero(ll.getLatD() - otherll.getLatD()) ? 0 : ll.getLatD() < otherll.getLatD() ? -1 : 1;
        }

        // Compare the upper right longitudes.
        if (result == 0)
        {
            result = MathUtil.isZero(ur.getLonD() - otherur.getLonD()) ? 0 : ur.getLonD() < otherur.getLonD() ? -1 : 1;
        }

        // Compare the upper right latitudes.
        if (result == 0)
        {
            result = MathUtil.isZero(ur.getLatD() - otherur.getLatD()) ? 0 : ur.getLatD() < otherur.getLatD() ? -1 : 1;
        }

        return result;
    }

    /**
     * Checks for containment of a bounding box within this bounding box.
     *
     * Note that this will not correctly report for bounding boxes that span the
     * 180 meridian. We will need to fix this eventually.
     *
     * @param otherBox the other box to check.
     * @return true, if successful
     */
    @Override
    public boolean contains(BoundingBox<GeographicPosition> otherBox)
    {
        return BoundingBoxes.contains(this, otherBox);
    }

    @Override
    public boolean contains(Position point, double radius)
    {
        if (!(point instanceof GeographicPosition))
        {
            return false;
        }

        // Check to make sure that the width and height are both larger
        // than 2 * ||radius|| when the radius is negative.
        if (radius < 0)
        {
            double radDoubleMag = 2 * Math.abs(radius);
            if (getWidth() < radDoubleMag || getHeight() < radDoubleMag)
            {
                return false;
            }
        }

        LatLonAlt pt = ((GeographicPosition)point).getLatLonAlt().getNormalized();
        double minLat = LatLonAlt.normalizeLatitude(getLowerLeft().getLatLonAlt().getLatD() - radius);
        double maxLat = LatLonAlt.normalizeLatitude(getUpperRight().getLatLonAlt().getLatD() + radius);
        if (minLat > pt.getLatD() || maxLat < pt.getLatD())
        {
            return false;
        }

        double minLon = LatLonAlt.normalizeLongitude(getLowerLeft().getLatLonAlt().getLonD() - radius);
        double maxLon = LatLonAlt.normalizeLongitude(getUpperRight().getLatLonAlt().getLonD() + radius);
        return minLon <= maxLon ? minLon <= pt.getLonD() && maxLon >= pt.getLonD()
                : minLon <= pt.getLonD() || maxLon >= pt.getLonD();
    }

    /**
     * Tell whether this bounding box spans the antimeridian.
     *
     * @return true when this bounding box spans the antimeridian.
     */
    public boolean crossesAntimeridian()
    {
        return myLowerLeftCorner.getLatLonAlt().getLonD() > myUpperRightCorner.getLatLonAlt().getLonD();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || obj != null && getClass() == obj.getClass()
                && myLowerLeftCorner.equals(((GeographicBoundingBox)obj).myLowerLeftCorner)
                && myUpperRightCorner.equals(((GeographicBoundingBox)obj).myUpperRightCorner);
    }

    /**
     * Get the altitude reference type of this box.
     *
     * @return The altitude reference type.
     */
    public Altitude.ReferenceLevel getAltitudeReference()
    {
        return getUpperRight().getLatLonAlt().getAltitudeReference();
    }

    @Override
    public GeographicPosition getCenter()
    {
        double lat = getCenterLatitudeD();
        double lon = getCenterLongitudeD();
        double alt = getCenterAltitudeM();
        return new GeographicPosition(LatLonAlt.createFromDegreesMeters(lat, lon, alt, getAltitudeReference()));
    }

    /**
     * Get the average altitude of the box.
     *
     * @return The altitude of the center of the box in meters.
     */
    public double getCenterAltitudeM()
    {
        return (getLowerLeft().getLatLonAlt().getAltM() + getUpperRight().getLatLonAlt().getAltM()) / 2.;
    }

    /**
     * Get the average latitude of the box.
     *
     * @return The latitude of the center of the box in degrees.
     */
    public double getCenterLatitudeD()
    {
        return (getLowerLeft().getLatLonAlt().getLatD() + getUpperRight().getLatLonAlt().getLatD()) / 2.;
    }

    /**
     * Get the average longitude of the box.
     *
     * @return The longitude of the center of the box in degrees.
     */
    public double getCenterLongitudeD()
    {
        double average = (getLowerLeft().getLatLonAlt().getLonD() + getUpperRight().getLatLonAlt().getLonD()) / 2.;
        if (getLowerLeft().getLatLonAlt().getLonD() <= getUpperRight().getLatLonAlt().getLonD())
        {
            return average;
        }
        else
        {
            return average - Constants.HALF_CIRCLE_DEGREES;
        }
    }

    /**
     * Get the difference in altitude between the corners of the box.
     *
     * @return The difference in altitude in meters.
     */
    public double getDeltaAltM()
    {
        return Math.abs(getUpperRight().getLatLonAlt().getAltM() - getLowerLeft().getLatLonAlt().getAltM());
    }

    /**
     * Get the difference in latitude between the corners of the box.
     *
     * @return The difference in latitude in degrees.
     */
    public double getDeltaLatD()
    {
        return getUpperRight().getLatLonAlt().getLatD() - getLowerLeft().getLatLonAlt().getLatD();
    }

    /**
     * Get the difference in longitude between the corners of the box.
     *
     * @return The difference in longitude in degrees.
     */
    public double getDeltaLonD()
    {
        double width = getUpperRight().getLatLonAlt().getLonD() - getLowerLeft().getLatLonAlt().getLonD();
        if (width < 0.)
        {
            width += Constants.CIRCLE_DEGREES;
        }
        return width;
    }

    @Override
    public double getDepth()
    {
        return 0;
    }

    /**
     * Get the difference in lat/lon/alt between the corners of the box as a set
     * of coordinates.
     *
     * @return The dimensions of the box.
     */
    public Vector3d getDimensions()
    {
        return new Vector3d(getDeltaLatD(), getDeltaLonD(), getDeltaAltM());
    }

    @Override
    public double getHeight()
    {
        return getDeltaLatD();
    }

    /**
     * Get the midpoint of the west-most side of the box.
     *
     * @return The center of the left edge.
     */
    public GeographicPosition getLeftCenter()
    {
        return new GeographicPosition(LatLonAlt.createFromDegreesMeters(getCenterLatitudeD(),
                getLowerLeft().getLatLonAlt().getLonD(), getCenterAltitudeM(), getAltitudeReference()));
    }

    /**
     * Get the midpoint of the southern side of the box.
     *
     * @return The center of the lower edge.
     */
    public GeographicPosition getLowerCenter()
    {
        return new GeographicPosition(LatLonAlt.createFromDegreesMeters(getLowerLeft().getLatLonAlt().getLatD(),
                getCenterLongitudeD(), getCenterAltitudeM(), getAltitudeReference()));
    }

    /**
     * Get the south-west corner of the box.
     *
     * @return The south-west corner.
     */
    @Override
    public GeographicPosition getLowerLeft()
    {
        return myLowerLeftCorner;
    }

    /**
     * Get the south-east corner of the box.
     *
     * @return The south-east corner.
     */
    @Override
    public GeographicPosition getLowerRight()
    {
        return new GeographicPosition(LatLonAlt.createFromDegreesMeters(getLowerLeft().getLatLonAlt().getLatD(),
                getUpperRight().getLatLonAlt().getLonD(), getCenterAltitudeM(), getAltitudeReference()));
    }

    /**
     * Gets the max latitude.
     *
     * @return the max latitude
     */
    public double getMaxLatD()
    {
        double lat1 = myLowerLeftCorner.getLatLonAlt().getLatD();
        double lat2 = myUpperRightCorner.getLatLonAlt().getLatD();
        return lat1 >= lat2 ? lat1 : lat2;
    }

    /**
     * Gets the max longitude.
     *
     * @return the max longitude
     */
    public double getMaxLonD()
    {
        double lon1 = myLowerLeftCorner.getLatLonAlt().getLonD();
        double lon2 = myUpperRightCorner.getLatLonAlt().getLonD();
        return lon1 >= lon2 ? lon1 : lon2;
    }

    /**
     * Gets the min latitude.
     *
     * @return the min latitude
     */
    public double getMinLatD()
    {
        double lat1 = myLowerLeftCorner.getLatLonAlt().getLatD();
        double lat2 = myUpperRightCorner.getLatLonAlt().getLatD();
        return lat1 <= lat2 ? lat1 : lat2;
    }

    /**
     * Gets the min longitude.
     *
     * @return the min longitude
     */
    public double getMinLonD()
    {
        double lon1 = myLowerLeftCorner.getLatLonAlt().getLonD();
        double lon2 = myUpperRightCorner.getLatLonAlt().getLonD();
        return lon1 <= lon2 ? lon1 : lon2;
    }

    @Override
    public Vector3d getOffset(BoundingBox<GeographicPosition> outerBox)
    {
        GeographicBoundingBox ob = (GeographicBoundingBox)outerBox;
        LatLonAlt lowerLeft = getLowerLeft().getLatLonAlt();
        LatLonAlt outerLowerLeft = ob.getLowerLeft().getLatLonAlt();
        double x = (lowerLeft.getLonD() - outerLowerLeft.getLonD()) / ob.getDeltaLonD();
        double y = (lowerLeft.getLatD() - outerLowerLeft.getLatD()) / ob.getDeltaLatD();
        return new Vector3d(x, y, 0);
    }

    @Override
    public Vector3d getOffsetPercent(Position position)
    {
        if (!(position instanceof GeographicPosition))
        {
            return null;
        }

        GeographicPosition geoPos = (GeographicPosition)position;
        double widthPct = (geoPos.getLatLonAlt().getLonD() - myLowerLeftCorner.getLatLonAlt().getLonD()) / getWidth();
        double heightPct = (geoPos.getLatLonAlt().getLatD() - myLowerLeftCorner.getLatLonAlt().getLatD()) / getHeight();

        return new Vector3d(widthPct, heightPct, 0);
    }

    @Override
    public Class<GeographicPosition> getPositionType()
    {
        return GeographicPosition.class;
    }

    /**
     * Get the midpoint of the eastern side of the box.
     *
     * @return The center of the right edge.
     */
    public GeographicPosition getRightCenter()
    {
        return new GeographicPosition(LatLonAlt.createFromDegreesMeters(getCenterLatitudeD(),
                getUpperRight().getLatLonAlt().getLonD(), getCenterAltitudeM(), getAltitudeReference()));
    }

    /**
     * Get the midpoint of the northern side of the box.
     *
     * @return The center of the upper edge.
     */
    public GeographicPosition getUpperCenter()
    {
        return new GeographicPosition(LatLonAlt.createFromDegreesMeters(getUpperRight().getLatLonAlt().getLatD(),
                getCenterLongitudeD(), getCenterAltitudeM(), getAltitudeReference()));
    }

    /**
     * Get the north-west corner of the box.
     *
     * @return The north-west corner.
     */
    @Override
    public GeographicPosition getUpperLeft()
    {
        return new GeographicPosition(LatLonAlt.createFromDegreesMeters(getUpperRight().getLatLonAlt().getLatD(),
                getLowerLeft().getLatLonAlt().getLonD(), getCenterAltitudeM(), getAltitudeReference()));
    }

    /**
     * Get the north-east corner of the box.
     *
     * @return The north-east corner.
     */
    @Override
    public GeographicPosition getUpperRight()
    {
        return myUpperRightCorner;
    }

    @Override
    public List<? extends GeographicPosition> getVertices()
    {
        List<GeographicPosition> result = new ArrayList<>(4);
        result.add(getLowerLeft());
        result.add(getLowerRight());
        result.add(getUpperRight());
        result.add(getUpperLeft());
        return result;
    }

    @Override
    public double getWidth()
    {
        return getDeltaLonD();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myLowerLeftCorner == null || myLowerLeftCorner.getLatLonAlt() == null ? 0
                : myLowerLeftCorner.getLatLonAlt().hashCode());
        result = prime * result + (myUpperRightCorner == null || myLowerLeftCorner.getLatLonAlt() == null ? 0
                : myUpperRightCorner.getLatLonAlt().hashCode());
        return result;
    }

    @Override
    public BoundingBox<GeographicPosition> intersection(BoundingBox<GeographicPosition> otherBox)
    {
        GeographicBoundingBox gbb = null;
        Geometry gc = BoundingBoxes.intersectionEnvelope(this, otherBox);
        if (gc != null && gc.getCoordinates().length > 0)
        {
            Coordinate[] cds = gc.getCoordinates();
            gbb = new GeographicBoundingBox(LatLonAlt.createFromDegrees(cds[0].y, cds[0].x),
                    LatLonAlt.createFromDegrees(cds[2].y, cds[2].x));
        }
        return gbb;
    }

    @Override
    public boolean intersects(BoundingBox<GeographicPosition> otherBox)
    {
        return BoundingBoxes.intersects(this, otherBox);
    }

    @Override
    public boolean overlaps(Quadrilateral<? extends GeographicPosition> other, double tolerance)
    {
        if (!(other instanceof GeographicBoundingBox))
        {
            if (other instanceof GeographicQuadrilateral)
            {
                return ((GeographicQuadrilateral)other).overlaps(this, tolerance);
            }
            return false;
        }

        GeographicBoundingBox otherGeo = (GeographicBoundingBox)other;
        if (contains(otherGeo.getLowerLeft(), tolerance) || contains(otherGeo.getUpperRight(), tolerance)
                || contains(otherGeo.getLowerRight(), tolerance) || contains(otherGeo.getUpperLeft(), tolerance)
                || contains(other.getCenter(), tolerance))
        {
            return true;
        }

        return other.contains(getLowerLeft(), tolerance) || other.contains(getUpperRight(), tolerance)
                || other.contains(getLowerRight(), tolerance) || other.contains(getUpperLeft(), tolerance)
                || other.contains(getCenter(), tolerance);
    }

    /**
     * Split this bounding box into four.
     *
     * @return The list of bounding boxes.
     */
    public List<GeographicBoundingBox> quadSplit()
    {
        LatLonAlt center = getCenter().getLatLonAlt();
        GeographicBoundingBox lowerLeft = new GeographicBoundingBox(getLowerLeft().getLatLonAlt(), center);
        GeographicBoundingBox lowerRight = new GeographicBoundingBox(getLowerCenter().getLatLonAlt(),
                getRightCenter().getLatLonAlt());
        GeographicBoundingBox upperLeft = new GeographicBoundingBox(getLeftCenter().getLatLonAlt(),
                getUpperCenter().getLatLonAlt());
        GeographicBoundingBox upperRight = new GeographicBoundingBox(center, getUpperRight().getLatLonAlt());
        return Arrays.asList(lowerLeft, lowerRight, upperLeft, upperRight);
    }

    /**
     * Get a string representation for this bounding box based on a nested grid.
     * <ul>
     * <li>The bounding box -90/-180,-45/-135 returns 0./0.</li>
     * <li>The bounding box -45/-180,0/-135 returns 0./1.</li>
     * <li>The bounding box -90/-180,-67.5/-157.5 returns 0.0/0.0</li>
     * <li>The bounding box -67.5/-180,-45/-157.5 returns 0.0/0.1</li>
     * </ul>
     *
     * @param gridSize The top-level grid size.
     * @return The string.
     */
    public String toGridString(int gridSize)
    {
        double level = -Math.log(getWidth() / gridSize) / Math.log(2.);
        double lonD = getLowerLeft().getLatLonAlt().getLonD() + 180;
        double latD = getLowerLeft().getLatLonAlt().getLatD() + 90;
        StringBuilder sbx = new StringBuilder();
        StringBuilder sby = new StringBuilder();
        for (int i = 0; i <= level; ++i)
        {
            double div = gridSize * Math.pow(2., -i);
            int x = (int)(lonD / div);
            int y = (int)(latD / div);
            sbx.append(x);
            sby.append(y);
            if (i == 0)
            {
                sbx.append('.');
                sby.append('.');
            }
            lonD -= x * div;
            latD -= y * div;
        }

        return sbx.append('/').append(sby).toString();
    }

    @Override
    public String toSimpleString()
    {
        return getLowerLeft().toSimpleString() + " | " + getUpperRight().toSimpleString();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("BoundingBox [lowerLeft=").append(getLowerLeft()).append(", upperRight=").append(getUpperRight()).append(']');
        return sb.toString();
    }

    /**
     * Creates the union between two bounding boxes.
     *
     * Note that this will not correctly union bounding boxes that span the 180
     * meridian. We will need to fix this eventually.
     *
     * @param otherBox the other box to union with.
     * @return the bounding box of the union.
     */
    @Override
    public BoundingBox<GeographicPosition> union(BoundingBox<GeographicPosition> otherBox)
    {
        GeographicBoundingBox gbb = null;
        Geometry gc = BoundingBoxes.unionEnvelope(this, otherBox);
        if (gc != null && gc.getCoordinates().length > 0)
        {
            Coordinate[] cds = gc.getCoordinates();
            gbb = new GeographicBoundingBox(LatLonAlt.createFromDegrees(cds[0].y, cds[0].x),
                    LatLonAlt.createFromDegrees(cds[2].y, cds[2].x));
        }
        return gbb;
    }
}
