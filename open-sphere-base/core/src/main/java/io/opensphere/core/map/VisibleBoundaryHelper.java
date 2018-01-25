package io.opensphere.core.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.Viewer3D;

/**
 * Methods for calculating the visible boundary of the map.
 */
public final class VisibleBoundaryHelper
{
    /**
     * Calculates the visible geographic boundaries of the map.
     *
     * @param view The view
     * @param projection The projection
     * @return A list of the visible geographic boundary positions
     */
    public static List<GeographicPosition> getVisibleBoundaries(DynamicViewer view, Projection projection)
    {
        if (view.getViewportWidth() == 0 || view.getViewportHeight() == 0)
        {
            return Collections.emptyList();
        }

        // Get screen boundaries
        int segmentsPerSide = view instanceof Viewer3D ? 16 : 1;
        List<Vector2i> screenBoundaries = getScreenBoundaries(view, segmentsPerSide);

        // For 3D mode, calculate values to be used below
        LatLonAlt cameraGroundLLA = null;
        Vector2i cameraGroundWindowCoords2i = null;
        double heading = 0.0;
        double footprintAngle = MathUtil.HALF_PI;
        if (view instanceof Viewer3D)
        {
            // Get location of camera, and camera on the ground
            Vector3d cameraModel = view.getPosition().getLocation();
            GeographicPosition cameraPosition = projection.convertToPosition(cameraModel, ReferenceLevel.ELLIPSOID);
            cameraGroundLLA = LatLonAlt.createFromDegrees(cameraPosition.getLatLonAlt().getLatD(),
                    cameraPosition.getLatLonAlt().getLonD());
            GeographicPosition cameraGroundPosition = new GeographicPosition(cameraGroundLLA);
            Vector3d cameraGroundModel = projection.convertToModel(cameraGroundPosition, Vector3d.ORIGIN);
            Vector3d cameraGroundWindowCoords = view.modelToWindowCoords(cameraGroundModel);
            cameraGroundWindowCoords2i = new Vector2i(Math.round((float)cameraGroundWindowCoords.getX()),
                    Math.round((float)cameraGroundWindowCoords.getY()));

            // Get the heading
            heading = view.getHeading();

            // Calculate footprint angle in radians
            double ratio = WGS84EarthConstants.RADIUS_MEAN_M / (WGS84EarthConstants.RADIUS_MEAN_M + view.getAltitude());
            footprintAngle = Math.acos(ratio);
        }

        // Create a list of geographic positions based on the screen boundaries
        List<GeographicPosition> geoPoints = new ArrayList<>(screenBoundaries.size() + 1);
        for (Vector2i screenBoundaryPoint : screenBoundaries)
        {
            Vector3d modelVector = view.windowToModelCoords(screenBoundaryPoint);
            // Handle where the screen cuts across the earth
            if (modelVector != null)
            {
                geoPoints.add(projection.convertToPosition(modelVector, ReferenceLevel.ELLIPSOID));
            }
            // Handle horizon boundaries
            else if (view instanceof Viewer3D)
            {
                // Calculate the angle from the screen center to this screen
                // boundary
                @SuppressWarnings("null")
                int x = screenBoundaryPoint.getX() - cameraGroundWindowCoords2i.getX();
                int y = screenBoundaryPoint.getY() - cameraGroundWindowCoords2i.getY();
                double pieSliceAngle = Math.atan((double)x / y);
                if (y < 0)
                {
                    pieSliceAngle += Math.PI;
                }
                pieSliceAngle += heading;
                if (pieSliceAngle < 0.0)
                {
                    pieSliceAngle += MathUtil.TWO_PI;
                }
                else if (pieSliceAngle > MathUtil.TWO_PI)
                {
                    pieSliceAngle -= MathUtil.TWO_PI;
                }

                geoPoints.add(new GeographicPosition(
                        GeographicBody3D.greatCircleEndPosition(cameraGroundLLA, pieSliceAngle, footprintAngle)));
            }
        }
        // Add the first point again to close the polygon
        geoPoints.add(geoPoints.get(0));

        return geoPoints;
    }

    /**
     * Gets the current visible bounding box.
     *
     * @param view The view
     * @param projection The projection
     * @return The visible bounding box.
     */
    public static GeographicBoundingBox getVisibleBoundingBox(DynamicViewer view, Projection projection)
    {
        // Get the list of geographic boundary points
        List<GeographicPosition> boundaryPoints = getVisibleBoundaries(view, projection);

        if (boundaryPoints.isEmpty())
        {
            return new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 0), LatLonAlt.createFromDegrees(0, 0));
        }

        // Determine number of antimeridian crossings
        int numMeridianCrossings = getNumberOfMeridianCrossings(boundaryPoints);
        boolean isSimpleMeridianCrossing = numMeridianCrossings > 0 && numMeridianCrossings % 2 == 0;
        boolean isPolarMeridianCrossing = numMeridianCrossings > 0 && numMeridianCrossings % 2 != 0;

        // Find min/max lat/lon
        double minLat = Float.MAX_VALUE;
        double maxLat = -Float.MAX_VALUE;
        double minLon = Float.MAX_VALUE;
        double maxLon = -Float.MAX_VALUE;
        for (GeographicPosition point : boundaryPoints)
        {
            double lat = point.getLatLonAlt().getLatD();
            double lon = point.getLatLonAlt().getLonD();

            // For simple antimeridian crossings, adjust the negative
            // longitudes.
            // This is what Google Earth does.
            if (isSimpleMeridianCrossing && lon < 0.)
            {
                lon += 360.;
            }

            if (lat > maxLat)
            {
                maxLat = lat;
            }
            if (lat < minLat)
            {
                minLat = lat;
            }
            if (lon > maxLon)
            {
                maxLon = lon;
            }
            if (lon < minLon)
            {
                minLon = lon;
            }
        }

        // Correct looking at a pole
        if (isPolarMeridianCrossing)
        {
            maxLon = 180.;
            minLon = -180.;

            // North pole
            if (maxLat + minLat > 0)
            {
                maxLat = 90.;
            }
            // South pole
            else
            {
                minLat = -90.;
            }
        }

        // Create a bounding box
        LatLonAlt lowerLeftCorner = LatLonAlt.createFromDegrees(minLat, minLon);
        LatLonAlt upperRightCorner = LatLonAlt.createFromDegrees(maxLat, maxLon);
        return new GeographicBoundingBox(lowerLeftCorner, upperRightCorner);
    }

    /**
     * Gets the number of anti meridian crossings of the given points.
     *
     * @param points The list of points
     * @return The number of crossings
     */
    private static int getNumberOfMeridianCrossings(List<GeographicPosition> points)
    {
        int numMeridianCrossings = 0;
        for (int i = 0; i < points.size(); i++)
        {
            GeographicPosition curPoint = points.get(i);
            int prevIndex = i - 1;
            if (prevIndex < 0)
            {
                prevIndex = points.size() - 1;
            }
            GeographicPosition prevPoint = points.get(prevIndex);
            if (prevPoint.getLatLonAlt().positionsCrossLongitudeBoundary(curPoint.getLatLonAlt()))
            {
                numMeridianCrossings++;
            }
        }
        return numMeridianCrossings;
    }

    /**
     * Helper method to get samples of the screen boundaries.
     *
     * @param view The viewer.
     * @param segmentsPerSide The number of segments (or points) per side.
     * @return A list of screen boundaries
     */
    private static List<Vector2i> getScreenBoundaries(Viewer view, int segmentsPerSide)
    {
        List<Vector2i> screenVectors = new ArrayList<>(segmentsPerSide * 4);
        for (int p = 0; p < segmentsPerSide; p++)
        {
            int y = p * view.getViewportHeight() / segmentsPerSide;
            screenVectors.add(new Vector2i(0, y));
        }
        for (int p = 0; p < segmentsPerSide; p++)
        {
            int x = p * view.getViewportWidth() / segmentsPerSide;
            screenVectors.add(new Vector2i(x, view.getViewportHeight()));
        }
        for (int p = segmentsPerSide; p > 0; p--)
        {
            int y = p * view.getViewportHeight() / segmentsPerSide;
            screenVectors.add(new Vector2i(view.getViewportWidth(), y));
        }
        for (int p = segmentsPerSide; p > 0; p--)
        {
            int x = p * view.getViewportWidth() / segmentsPerSide;
            screenVectors.add(new Vector2i(x, 0));
        }
        return screenVectors;
    }

    /**
     * Private Constructor.
     */
    private VisibleBoundaryHelper()
    {
    }
}
