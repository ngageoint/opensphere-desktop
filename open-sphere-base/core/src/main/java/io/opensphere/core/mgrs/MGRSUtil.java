package io.opensphere.core.mgrs;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.math.Line2d;
import io.opensphere.core.math.LineSegment2d;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;

/** Class that holds utility methods for MGRS calculations. */
@SuppressWarnings("PMD.GodClass")
public final class MGRSUtil
{
    /** The default altitude to use. */
    private static final double ALTM = 0.5;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MGRSUtil.class);

    /**
     * Check the given corner positions and create a bounding box for a hammer
     * projection. Crossing the 180 border and distortion near the poles are
     * accounted for.
     *
     * @param southEast The south east position.
     * @param southWest The south west position.
     * @param northWest The north west position.
     * @param northEast The north east position.
     * @return The bounding box.
     */
    public static GeographicBoundingBox calculateBoundingBox(GeographicPosition southEast, GeographicPosition southWest,
            GeographicPosition northWest, GeographicPosition northEast)
    {
        GeographicPosition lowerRightPos = southEast;
        GeographicPosition lowerLeftPos = southWest;
        GeographicPosition upperLeftPos = northWest;
        GeographicPosition upperRightPos = northEast;

        // Check for crossing 180 border (lower corners)
        if (Math.signum(lowerLeftPos.getLatLonAlt().getLonD()) != Math.signum(lowerRightPos.getLatLonAlt().getLonD()))
        {
            if (Math.signum(lowerLeftPos.getLatLonAlt().getLonD()) == 1)
            {
                lowerLeftPos = new GeographicPosition(LatLonAlt.createFromDegrees(lowerLeftPos.getLatLonAlt().getLatD(), -180));
            }
            else
            {
                lowerRightPos = new GeographicPosition(LatLonAlt.createFromDegrees(lowerRightPos.getLatLonAlt().getLatD(), 180));
            }
        }
        // Check for both lower corners being across
        else if (Math.signum(lowerLeftPos.getLatLonAlt().getLonD()) != Math.signum(upperRightPos.getLatLonAlt().getLonD()))
        {
            if (Math.signum(upperRightPos.getLatLonAlt().getLonD()) == -1)
            {
                lowerLeftPos = new GeographicPosition(LatLonAlt.createFromDegrees(lowerLeftPos.getLatLonAlt().getLatD(), -180));
                lowerRightPos = new GeographicPosition(LatLonAlt.createFromDegrees(lowerRightPos.getLatLonAlt().getLatD(), -180));
            }
            else
            {
                lowerLeftPos = new GeographicPosition(LatLonAlt.createFromDegrees(lowerLeftPos.getLatLonAlt().getLatD(), 180));
                lowerRightPos = new GeographicPosition(LatLonAlt.createFromDegrees(lowerRightPos.getLatLonAlt().getLatD(), 180));
            }
        }

        // Check for crossing 180 border (upper corners)
        if (Math.signum(upperLeftPos.getLatLonAlt().getLonD()) != Math.signum(upperRightPos.getLatLonAlt().getLonD()))
        {
            if (Math.signum(upperLeftPos.getLatLonAlt().getLonD()) == 1)
            {
                upperLeftPos = new GeographicPosition(LatLonAlt.createFromDegrees(upperLeftPos.getLatLonAlt().getLatD(), -180));
            }
            else
            {
                upperRightPos = new GeographicPosition(LatLonAlt.createFromDegrees(upperRightPos.getLatLonAlt().getLatD(), 180));
            }
        }
        // Check for both upper corners being across
        else if (Math.signum(upperLeftPos.getLatLonAlt().getLonD()) != Math.signum(lowerRightPos.getLatLonAlt().getLonD()))
        {
            if (Math.signum(lowerRightPos.getLatLonAlt().getLonD()) == -1)
            {
                upperLeftPos = new GeographicPosition(LatLonAlt.createFromDegrees(upperLeftPos.getLatLonAlt().getLatD(), -180));
                upperRightPos = new GeographicPosition(LatLonAlt.createFromDegrees(upperRightPos.getLatLonAlt().getLatD(), -180));
            }
            else
            {
                upperLeftPos = new GeographicPosition(LatLonAlt.createFromDegrees(lowerLeftPos.getLatLonAlt().getLatD(), 180));
                upperRightPos = new GeographicPosition(LatLonAlt.createFromDegrees(lowerRightPos.getLatLonAlt().getLatD(), 180));
            }
        }

        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;

        GeographicPosition[] positionCorners = new GeographicPosition[4];
        positionCorners[0] = lowerLeftPos;
        positionCorners[1] = upperLeftPos;
        positionCorners[2] = lowerRightPos;
        positionCorners[3] = upperRightPos;

        for (GeographicPosition corner : positionCorners)
        {
            minLon = Math.min(minLon, corner.getLatLonAlt().getLonD());
            maxLon = Math.max(maxLon, corner.getLatLonAlt().getLonD());
            minLat = Math.min(minLat, corner.getLatLonAlt().getLatD());
            maxLat = Math.max(maxLat, corner.getLatLonAlt().getLatD());
        }

        GeographicPosition newLowerLeft = new GeographicPosition(LatLonAlt.createFromDegrees(minLat, minLon));
        GeographicPosition newUpperRight = new GeographicPosition(LatLonAlt.createFromDegrees(maxLat, maxLon));

        return new GeographicBoundingBox(newLowerLeft, newUpperRight);
    }

    /**
     * Given the grid (which should have the four corner positions already set)
     * and the original un-clipped positions for the four corners this helper
     * method will do a comparison to determine if this grid borders it's parent
     * grid.
     *
     * @param grid The grid containing the clipped corner positions.
     * @param origSEPos The original SouthEast position.
     * @param origSWPos The original SouthWest position.
     * @param origNWPos The original NorthWest position.
     * @param origNEPos The original NorthEast position.
     * @return True if this grid borders it's parent, false otherwise.
     */
    public static boolean checkForBorder(GenericGrid grid, GeographicPosition origSEPos, GeographicPosition origSWPos,
            GeographicPosition origNWPos, GeographicPosition origNEPos)
    {
        boolean onBorder = false;
        // If the clipped points are not the same as the original, they are on
        // the border
        if (grid.getNWPos() == null || !grid.getNWPos().equals(origNWPos))
        {
            onBorder = true;
        }
        else if (grid.getNEPos() == null || !grid.getNEPos().equals(origNEPos))
        {
            onBorder = true;
        }
        else if (grid.getSEPos() == null || !grid.getSEPos().equals(origSEPos))
        {
            onBorder = true;
        }
        else if (grid.getSWPos() == null || !grid.getSWPos().equals(origSWPos))
        {
            onBorder = true;
        }

        return onBorder;
    }

    /**
     * Check for north or south points not existing due to being a triangle. Set
     * the missing point to be the same as the adjacent horizontal point.
     *
     * @param grid The grid that contains the points to check.
     */
    public static void checkForTriangle(GenericGrid grid)
    {
        if (grid.getNEPos() == null && grid.getNWPos() != null)
        {
            grid.setNEPos(grid.getNWPos());
        }
        else if (grid.getNWPos() == null && grid.getNEPos() != null)
        {
            grid.setNWPos(grid.getNEPos());
        }

        if (grid.getSEPos() == null && grid.getSWPos() != null)
        {
            grid.setSEPos(grid.getSWPos());
        }
        else if (grid.getSWPos() == null && grid.getSEPos() != null)
        {
            grid.setSWPos(grid.getSEPos());
        }
    }

    /**
     * This method will check to see if a particular point is outside the parent
     * box. If so, it will use the adjacent horizontal and vertical points to
     * determine the intersections with the parent and will return a new point
     * on the parents border.
     *
     * @param bigBox The bounding box to check against.
     * @param position The geographic position to check.
     * @param adjacentHorizontal The adjacent horizontal position.
     * @param adjacentVertical The adjacent vertical position.
     * @return The updated point that could be on the parents border (if it was
     *         originally outside of it).
     */
    public static GeographicPosition checkPosition(GeographicBoundingBox bigBox, GeographicPosition position,
            GeographicPosition adjacentHorizontal, GeographicPosition adjacentVertical)
    {
        GeographicPosition newPos = position;

        if (!bigBox.contains(newPos, 0))
        {
            // check if we are above the north or below the south segments of
            // parent bounding box.
            if (newPos.getLatLonAlt().getLatD() > bigBox.getUpperRight().getLatLonAlt().getLatD()
                    || newPos.getLatLonAlt().getLatD() < bigBox.getLowerLeft().getLatLonAlt().getLatD())
            {
                GeographicPosition adjVert = checkLonPosition(bigBox, adjacentVertical);
                GeographicPosition interimPos = checkLonPosition(bigBox, newPos);

                if (!bigBox.contains(adjVert, 0) && !bigBox.contains(interimPos, 0))
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Both points outside of bounding box, can't determine intersection.");
                    }
                    return null;
                }

                GeographicPosition geoPos = findIntersection(bigBox, adjVert.getLatLonAlt().asVec2d(),
                        interimPos.getLatLonAlt().asVec2d());
                if (geoPos != null)
                {
                    newPos = geoPos;
                }
            }

            // check if we are outside the east or west segments of parent
            // bounding box.
            if (newPos.getLatLonAlt().getLonD() > bigBox.getUpperRight().getLatLonAlt().getLonD()
                    || newPos.getLatLonAlt().getLonD() < bigBox.getLowerLeft().getLatLonAlt().getLonD())
            {
                GeographicPosition adjHoriz = checkLatPosition(bigBox, adjacentHorizontal);
                GeographicPosition interimPos = checkLatPosition(bigBox, newPos);

                if (!bigBox.contains(adjHoriz, 0) && !bigBox.contains(interimPos, 0))
                {
                    // Both points are outside so check if we have a sliver of a
                    // grid shaped as a triangle.
                    if (bigBox.contains(adjacentVertical, 0))
                    {
                        GeographicPosition geoPos = findIntersection(bigBox, adjacentVertical.getLatLonAlt().asVec2d(),
                                interimPos.getLatLonAlt().asVec2d());
                        if (geoPos != null)
                        {
                            newPos = geoPos;
                        }
                        return newPos;
                    }
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Both points outside of bounding box, can't determine intersection.");
                    }
                    return null;
                }

                // Check for special case when these two points cross the
                // 180/-180 line
                if (Math.signum(adjHoriz.getLatLonAlt().getLonD()) != Math.signum(interimPos.getLatLonAlt().getLonD())
                        && Math.abs(interimPos.getLatLonAlt().getLonD()) > 170)
                {
                    double boundary = Math.signum(adjHoriz.getLatLonAlt().getLonD()) * 180;
                    newPos = new GeographicPosition(LatLonAlt.createFromDegreesMeters(adjHoriz.getLatLonAlt().getLatD(), boundary,
                            ALTM, Altitude.ReferenceLevel.TERRAIN));
                }
                else
                {
                    GeographicPosition geoPos = findIntersection(bigBox, adjHoriz.getLatLonAlt().asVec2d(),
                            interimPos.getLatLonAlt().asVec2d());
                    if (geoPos != null)
                    {
                        newPos = geoPos;
                    }
                }
            }
        }

        return newPos;
    }

    /**
     * Construct a bounding box from the four corners. There are a number of
     * special cases that are handled. 1. If we have a triangle and either both
     * north points or both south points are the same. 2. If the longitude
     * values of the east points or west points are not the same. 3. If the
     * latitude values of the south or north points are not the same.
     *
     * @param southEast The SouthEast corner.
     * @param southWest The SouthWest corner.
     * @param northWest The NorthWest corner.
     * @param northEast The NorthEast corner.
     * @return The GeographicBoundingBox.
     */
    public static GeographicBoundingBox createBoundingBox(GeographicPosition southEast, GeographicPosition southWest,
            GeographicPosition northWest, GeographicPosition northEast)
    {
        double eastLongitude;
        double westLongitude;

        double southLatitude;
        double northLatitude;

        // Check longitude values.
        // I don't think we have to worry about crossing the 180 degree
        // boundary.
        if (northEast.getLatLonAlt().getLonD() > southEast.getLatLonAlt().getLonD())
        {
            eastLongitude = northEast.getLatLonAlt().getLonD();
        }
        else
        {
            eastLongitude = southEast.getLatLonAlt().getLonD();
        }

        if (northWest.getLatLonAlt().getLonD() < southWest.getLatLonAlt().getLonD())
        {
            westLongitude = northWest.getLatLonAlt().getLonD();
        }
        else
        {
            westLongitude = southWest.getLatLonAlt().getLonD();
        }

        // Check latitude values.
        if (northEast.getLatLonAlt().getLatD() > northWest.getLatLonAlt().getLatD())
        {
            northLatitude = northEast.getLatLonAlt().getLatD();
        }
        else
        {
            northLatitude = northWest.getLatLonAlt().getLatD();
        }

        if (southEast.getLatLonAlt().getLatD() < southWest.getLatLonAlt().getLatD())
        {
            southLatitude = southEast.getLatLonAlt().getLatD();
        }
        else
        {
            southLatitude = southWest.getLatLonAlt().getLatD();
        }

        // Find the NorthEast corner.
        GeographicPosition upperRight;
        upperRight = new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(northLatitude, eastLongitude, ALTM, Altitude.ReferenceLevel.TERRAIN));

        // Find the SouthWest corner
        GeographicPosition lowerLeft;
        lowerLeft = new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(southLatitude, westLongitude, ALTM, Altitude.ReferenceLevel.TERRAIN));

        return new GeographicBoundingBox(lowerLeft, upperRight);
    }

    /**
     * Helper function that creates a 2-d line from two geographic positions
     * (ignoring altitude).
     *
     * @param geoPos1 The first position.
     * @param geoPos2 The second position.
     * @return A line created from two points
     */
    public static Line2d createLine(GeographicPosition geoPos1, GeographicPosition geoPos2)
    {
        // Create a line
        Vector2d point1 = new Vector2d(geoPos1.getLatLonAlt().asVec2d());
        Vector2d point2 = new Vector2d(geoPos2.getLatLonAlt().asVec2d());

        Vector2d normalPerp = point2.subtract(point1).getNormalized();
        Vector2d normal = new Vector2d(normalPerp.getY(), -normalPerp.getX());
        return new Line2d(point1, normal);
    }

    /**
     * Given the original (unclipped) corners of a grid, check these against the
     * parent bounding box to find the line segments (that may be different than
     * just connecting corner points) which will be used in displaying this
     * grid.
     *
     * @param bbox The GeographicBoundingBox of the parent to check against.
     * @param sePos The SouthEast position of grid.
     * @param swPos The SouthWest position of grid.
     * @param nwPos The NorthWest position of grid.
     * @param nePos The NorthEast position of grid.
     * @return All the line segments used to display a grid.
     */
    public static GridSegments determineRenderableLines(GeographicBoundingBox bbox, GeographicPosition sePos,
            GeographicPosition swPos, GeographicPosition nwPos, GeographicPosition nePos)
    {
        LineSegment south = null;
        LineSegment north = null;
        LineSegment east = null;
        LineSegment west = null;

        final double tolerance = .001;

        // Check each line segment (North, South, East, West) to see if we
        // intersect parent

        // South
        if (!bbox.contains(sePos, tolerance) || !bbox.contains(swPos, tolerance))
        {
            List<GeographicPosition> points = getClippedPoints(bbox, sePos, swPos);
            if (!points.isEmpty())
            {
                south = new LineSegment(points.get(0), points.get(1));
            }
        }
        else
        {
            // Both are inside so just add the points
            south = new LineSegment(sePos, swPos);
        }

        // West
        if (!bbox.contains(swPos, tolerance) || !bbox.contains(nwPos, tolerance))
        {
            List<GeographicPosition> points = getClippedPoints(bbox, swPos, nwPos);
            if (!points.isEmpty())
            {
                west = new LineSegment(points.get(0), points.get(1));
            }
        }
        else
        {
            // Both are inside so just add the points
            west = new LineSegment(swPos, nwPos);
        }

        // North
        if (!bbox.contains(nwPos, tolerance) || !bbox.contains(nePos, tolerance))
        {
            List<GeographicPosition> points = getClippedPoints(bbox, nwPos, nePos);
            if (!points.isEmpty())
            {
                north = new LineSegment(points.get(0), points.get(1));
            }
        }
        else
        {
            // Both are inside so just add the points
            north = new LineSegment(nwPos, nePos);
        }

        // East
        if (!bbox.contains(nePos, tolerance) || !bbox.contains(sePos, tolerance))
        {
            List<GeographicPosition> points = getClippedPoints(bbox, nePos, sePos);
            if (!points.isEmpty())
            {
                east = new LineSegment(points.get(0), points.get(1));
            }
        }
        else
        {
            // Both are inside so just add the points
            east = new LineSegment(nePos, sePos);
        }

        return new GridSegments(south, north, east, west);
    }

    /**
     * Given the four corner positions of a grid, calculate the center position.
     * A check is made to make sure we have width and not using the single tip
     * of a triangle (both points are the same).
     *
     * @param southEast The SouthEast position.
     * @param southWest The SouthWest position.
     * @param northWest The NorthWest position.
     * @param northEast The NorthEast position.
     * @return The center location.
     */
    public static GeographicPosition getCenterPoint(GeographicPosition southEast, GeographicPosition southWest,
            GeographicPosition northWest, GeographicPosition northEast)
    {
        GeographicPosition centerPos;
        // Do a check to see if we are a triangle and both south points are
        // equal.
        if (southWest.equals(southEast))
        {
            // Use north positions
            double centerLat = (southWest.getLatLonAlt().getLatD() + northWest.getLatLonAlt().getLatD()) / 2.;
            double centerLon = (northWest.getLatLonAlt().getLonD() + northEast.getLatLonAlt().getLonD()) / 2.;
            centerPos = new GeographicPosition(
                    LatLonAlt.createFromDegreesMeters(centerLat, centerLon, ALTM, Altitude.ReferenceLevel.TERRAIN));
        }
        else
        {
            double centerLat = (southWest.getLatLonAlt().getLatD() + northWest.getLatLonAlt().getLatD()) / 2.;
            double centerLon = (southWest.getLatLonAlt().getLonD() + southEast.getLatLonAlt().getLonD()) / 2.;
            centerPos = new GeographicPosition(
                    LatLonAlt.createFromDegreesMeters(centerLat, centerLon, ALTM, Altitude.ReferenceLevel.TERRAIN));
        }
        return centerPos;
    }

    /**
     * Reduce the precision of an MGRS string value, assuming input is the full
     * 10 digits after the letters (15 total characters). Allowable precisions
     * are 4, 6, 8, and 10. All other values will throw
     * {@link IllegalArgumentException}.
     *
     * @param mgrsFullPrecision the mgrs full precision 10 digit numeric code
     *            after the letters.
     * @param desiredPrecision the desired precision 4, 6, 8, or 10.
     * @return the reduced precision string.
     * @throws IllegalArgumentException if desiredPrecision is not 4, 6, 8, or
     *             10
     */
    public static String reducePrecision(String mgrsFullPrecision, int desiredPrecision)
    {
        if (desiredPrecision != 4 && desiredPrecision != 6 && desiredPrecision != 8 && desiredPrecision != 10)
        {
            throw new IllegalArgumentException(
                    "MGRS Precison must be 4, 6, 8, or 10 only \"" + desiredPrecision + "\" is not valid.");
        }
        String result = mgrsFullPrecision;
        if (mgrsFullPrecision != null && mgrsFullPrecision.length() == 15 && desiredPrecision != 10)
        {
            int numDigits = desiredPrecision / 2;
            result = mgrsFullPrecision.substring(0, 5) + mgrsFullPrecision.substring(5, 5 + numDigits)
                    + mgrsFullPrecision.substring(10, 10 + numDigits);
        }
        return result;
    }

    /**
     * This method ensures the positions are trimmed to fit inside the bounding
     * box and will clip coordinates that are outside of zone (in relation to
     * latitude) to be on the border.
     *
     * @param bigBox The bounding box to check against.
     * @param geoPos The location to check.
     * @return The updated location.
     */
    private static GeographicPosition checkLatPosition(GeographicBoundingBox bigBox, GeographicPosition geoPos)
    {
        double longitude = geoPos.getLatLonAlt().getLonD();
        double latitude = geoPos.getLatLonAlt().getLatD();

        double minLatitude = bigBox.getLowerLeft().getLatLonAlt().getLatD();
        double maxLatitude = bigBox.getUpperRight().getLatLonAlt().getLatD();
        if (geoPos.getLatLonAlt().getLatD() < minLatitude)
        {
            latitude = minLatitude;
        }
        else if (geoPos.getLatLonAlt().getLatD() > maxLatitude)
        {
            latitude = maxLatitude;
        }

        return new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(latitude, longitude, ALTM, Altitude.ReferenceLevel.TERRAIN));
    }

    /**
     * This method ensures the positions are trimmed to fit inside the bounding
     * box and will clip coordinates that are outside of zone (in relation to
     * longitude) to be on the border.
     *
     * @param bigBox The bounding box to check against.
     * @param geoPos The location to check.
     * @return The updated location.
     */
    private static GeographicPosition checkLonPosition(GeographicBoundingBox bigBox, GeographicPosition geoPos)
    {
        double longitude = geoPos.getLatLonAlt().getLonD();

        final double minLongitude = bigBox.getLowerLeft().getLatLonAlt().getLonD();
        final double maxLongitude = bigBox.getUpperRight().getLatLonAlt().getLonD();

        // We have to check for the special case of crossing the 180 degree
        // border. And we
        // can take advantage of the fact that points will never be more than 6
        // degrees apart.
        if (Math.signum(geoPos.getLatLonAlt().getLonD()) == 1.0 && Math.signum(minLongitude) == -1.0
                && geoPos.getLatLonAlt().getLonD() > 170)
        {
            longitude = -180;
        }
        else if (Math.signum(geoPos.getLatLonAlt().getLonD()) == -1.0 && Math.signum(maxLongitude) == 1.0
                && geoPos.getLatLonAlt().getLonD() < -170)
        {
            longitude = 180;
        }
        else if (geoPos.getLatLonAlt().getLonD() < minLongitude)
        {
            longitude = minLongitude;
        }
        else if (geoPos.getLatLonAlt().getLonD() > maxLongitude)
        {
            longitude = maxLongitude;
        }

        double latitude = geoPos.getLatLonAlt().getLatD();

        return new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(latitude, longitude, ALTM, Altitude.ReferenceLevel.TERRAIN));
    }

    /**
     * Find the intersection of the bounding box and the line formed by the two
     * points. If known, the first point should be the point inside the bounding
     * box while the second should be the point outside. And this should only be
     * called when you know there is an intersection otherwise null will be
     * returned.
     *
     * @param bbox The GeographicBoundingBox to check against.
     * @param pointA The first point in the line (inside point).
     * @param pointB The second point in the line (outside point).
     * @return The position of the intersection (or null if intersection not
     *         found).
     */
    private static GeographicPosition findIntersection(GeographicBoundingBox bbox, Vector2d pointA, Vector2d pointB)
    {
        GeographicPosition geoPos = null;

        // Create a line for each segment of parent bounding box.
        Line2d southLine = createLine(bbox.getLowerLeft(), bbox.getLowerRight());
        Line2d westLine = createLine(bbox.getLowerLeft(), bbox.getUpperLeft());
        Line2d northLine = createLine(bbox.getUpperLeft(), bbox.getUpperRight());
        Line2d eastLine = createLine(bbox.getUpperRight(), bbox.getLowerRight());

        // Need to check for special case of crossing 180 border.
        if (Math.signum(pointA.getX()) != Math.signum(pointB.getX()) && Math.abs(pointA.getX()) > 170)
        {
            double boundary = 180 * Math.signum(pointA.getX());
            geoPos = new GeographicPosition(
                    LatLonAlt.createFromDegreesMeters(pointA.getY(), boundary, ALTM, Altitude.ReferenceLevel.TERRAIN));
        }
        else
        {
            // We could be intersecting the parent on any side, so need to check
            // them all. If
            // intersection is collinear, continue on to check other sides.

            // South
            if (LineSegment2d.segmentsIntersect(bbox.getLowerLeft().getLatLonAlt().asVec2d(),
                    bbox.getLowerRight().getLatLonAlt().asVec2d(), pointA, pointB))
            {
                List<? extends Vector2d> intersection = southLine.getSegmentIntersection(pointA, pointB);
                // Should only have one point, two points are returned if we are
                // colinear
                if (intersection.size() == 1)
                {
                    return new GeographicPosition(LatLonAlt.createFromDegreesMeters(intersection.get(0).getY(),
                            intersection.get(0).getX(), ALTM, Altitude.ReferenceLevel.TERRAIN));
                }
            }
            // West
            if (LineSegment2d.segmentsIntersect(bbox.getLowerLeft().getLatLonAlt().asVec2d(),
                    bbox.getUpperLeft().getLatLonAlt().asVec2d(), pointA, pointB))
            {
                List<? extends Vector2d> intersection = westLine.getSegmentIntersection(pointA, pointB);
                if (intersection.size() == 1)
                {
                    return new GeographicPosition(LatLonAlt.createFromDegreesMeters(intersection.get(0).getY(),
                            intersection.get(0).getX(), ALTM, Altitude.ReferenceLevel.TERRAIN));
                }
            }
            // North
            if (LineSegment2d.segmentsIntersect(bbox.getUpperLeft().getLatLonAlt().asVec2d(),
                    bbox.getUpperRight().getLatLonAlt().asVec2d(), pointA, pointB))
            {
                List<? extends Vector2d> intersection = northLine.getSegmentIntersection(pointA, pointB);
                if (intersection.size() == 1)
                {
                    return new GeographicPosition(LatLonAlt.createFromDegreesMeters(intersection.get(0).getY(),
                            intersection.get(0).getX(), ALTM, Altitude.ReferenceLevel.TERRAIN));
                }
            }
            // East
            if (LineSegment2d.segmentsIntersect(bbox.getUpperRight().getLatLonAlt().asVec2d(),
                    bbox.getLowerRight().getLatLonAlt().asVec2d(), pointA, pointB))
            {
                List<? extends Vector2d> intersection = eastLine.getSegmentIntersection(pointA, pointB);
                if (intersection.size() == 1)
                {
                    geoPos = new GeographicPosition(LatLonAlt.createFromDegreesMeters(intersection.get(0).getY(),
                            intersection.get(0).getX(), ALTM, Altitude.ReferenceLevel.TERRAIN));
                }
            }
        }
        return geoPos;
    }

    /**
     * Check the two grid corner points against the bounding box and clip them
     * if needed to return the line segment points for the given segment type.
     * If both points are outside of the bounding box, then they are ignored and
     * an empty list is returned. The two points to check should always be given
     * in the clockwise direction. So for checking the south segment the
     * SouthEast location should be given first and the SouthWest location
     * second.
     *
     * @param bbox The GeographicBoundingBox to check against.
     * @param point1 The first point.
     * @param point2 The second point.
     * @return A List of two locations that describe the segment if there is an
     *         intersection or an empty List if there is no intersection (both
     *         points outside).
     */
    private static List<GeographicPosition> getClippedPoints(GeographicBoundingBox bbox, GeographicPosition point1,
            GeographicPosition point2)
    {
        List<GeographicPosition> points = new ArrayList<>();

        boolean pt1Outside = !bbox.contains(point1, 0);
        boolean pt2Outside = !bbox.contains(point2, 0);

        if (pt1Outside && pt2Outside)
        {
            // Both points outside of parent.
            return points;
        }

        // Find the intersection
        if (pt1Outside)
        {
            GeographicPosition geoPos = findIntersection(bbox, point2.getLatLonAlt().asVec2d(), point1.getLatLonAlt().asVec2d());
            if (geoPos != null)
            {
                points.add(point2);
                points.add(geoPos);
            }
        }
        else
        {
            GeographicPosition geoPos = findIntersection(bbox, point1.getLatLonAlt().asVec2d(), point2.getLatLonAlt().asVec2d());
            if (geoPos != null)
            {
                points.add(point1);
                points.add(geoPos);
            }
        }

        return points;
    }

    /**
     * Private constructor.
     */
    private MGRSUtil()
    {
    }
}
