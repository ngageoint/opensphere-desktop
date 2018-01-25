package io.opensphere.core.viewer.impl;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import io.opensphere.core.math.RectangularCylinder;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.viewer.Viewer;

/**
 * Interface for viewers that support movement.
 */
public interface DynamicViewer extends Viewer
{
    /**
     * Get the current altitude of the viewer position above sea level.
     *
     * @return The altitude in meters.
     */
    double getAltitude();

    /**
     * Get a KML compatible camera definition from the current viewer position.
     *
     * @param position the position for which the camera is desired.
     * @return the KML compatible camera definition.
     */
    KMLCompatibleCamera getCamera(ViewerPosition position);

    /**
     * Get the viewer position which is centered at the given position without
     * changing the altitude.
     *
     * @param position The position at which to center the view.
     * @return The viewer position which is centered at the given position
     *         without changing the altitude.
     */
    ViewerPosition getCenteredView(Vector3d position);

    /**
     * Get the viewer position which is centered at the given position without
     * changing the altitude.
     *
     * @param position The position at which to center the view.
     * @param centroidHint A hint to help center the viewer when the boundary
     *            points are geographically diverse.
     * @return The viewer position which is centered at the given position
     *         without changing the altitude.
     */
    ViewerPosition getCenteredView(Vector3d position, Vector3d centroidHint);

    /**
     * Get the current heading angle value.
     *
     * @return The heading angle value in radians.
     */
    double getHeading();

    /**
     * Get the mapContext.
     *
     * @return the mapContext
     */
    MapContext<DynamicViewer> getMapContext();

    /**
     * Get the current pitch angle value.
     *
     * @return The pitch angle in radians.
     */
    double getPitch();

    /**
     * Get the menu items for supported actions associated with context menus
     * for polygons.
     *
     * @param polygonPoints The points which define the polygon.
     * @return Context menu items for supported view operations.
     */
    List<JMenuItem> getPolygonContexMenuItems(Collection<? extends Vector3d> polygonPoints);

    /**
     * Get the viewer position at the given location where the viewer is square
     * to the model at that position.
     *
     * @param location Location for which the square position is desired.
     * @return The square position.
     */
    ViewerPosition getRightedView(Vector3d location);

    /**
     * Get the viewer position at the same location as the given viewer position
     * where the viewer is square to the model at that position.
     *
     * @param position Position for which the square position is desired.
     * @return The square position.
     */
    ViewerPosition getRightedView(ViewerPosition position);

    /**
     * Get the viewer position where the viewer is square to the model at the
     * intersection of the viewer direction and the model. The current viewer
     * height is preserved.
     *
     * @param location Location for which the square position is desired.
     * @return The square position.
     */
    ViewerPosition getRightedViewAtIntersection(ViewerPosition location);

    /**
     * Get the intersection of the terrain with the viewer at the center of the
     * view.
     *
     * @return The nearest terrain intersection location in model coordinates.
     */
    Vector3d getTerrainIntersection();

    /**
     * Get the nearest intersection of the terrain with the viewer at the given
     * screen location.
     *
     * @param screenLoc Location on the screen based on the origin being in the
     *            upper left corner.
     * @return The nearest terrain intersection location in model coordinates.
     */
    Vector3d getTerrainIntersection(Vector2i screenLoc);

    /**
     * Get a viewer compatible viewer position given the KML compatible camera
     * definition.
     *
     * @param camera A camera which contains a KML compatible camera definition.
     * @return a viewer compatible viewer position.
     */
    ViewerPosition getViewerPosition(KMLCompatibleCamera camera);

    /**
     * Zoom the given viewer position to fit the bounds.
     *
     * @param bounds The bounds at which to center the view.
     * @return The viewer position which is centered at the center of the bounds
     *         with the altitude adjusted to fit the bounds within the view.
     */
    ViewerPosition getZoomToFitView(RectangularCylinder bounds);

    /**
     * Zoom the given viewer position to fit the bounds.
     *
     * @param bounds The bounds at which to center the view.
     * @param centroidHint A hint to help center the viewer when the boundary
     *            points are geographically diverse.
     * @return The viewer position which is centered at the center of the bounds
     *         with the altitude adjusted to fit the bounds within the view.
     */
    ViewerPosition getZoomToFitView(RectangularCylinder bounds, Vector3d centroidHint);

    /** Reset the view to a normalized position. */
    void resetView();

    /**
     * Set the view to match this viewer, but with the view centered.
     *
     * @param viewer viewer to match.
     */
    void setCenteredView(Viewer viewer);

    /**
     * Set the map context for the viewer.
     *
     * @param context The map context.
     */
    void setMapContext(MapContext<DynamicViewer> context);

    /**
     * Set the position of the viewer.
     *
     * @param viewerPosition The viewer position to which to set.
     */
    void setPosition(ViewerPosition viewerPosition);

    /**
     * Start animating the viewer to its preferred position.
     */
    void startAnimationToPreferredPosition();

    /** Ensure that the viewer position is valid and correct if it is not. */
    void validateViewerPosition();

    /**
     * A camera which contains the values as specified in the KML spec. Once the
     * viewer is translated to the altitude adjusted model position, the
     * rotation angles should be applied in the following order : heading, tilt,
     * roll.
     */
    class KMLCompatibleCamera
    {
        /** Direction (azimuth) of the camera, in degrees. */
        private final double myHeading;

        /** The location of the viewer. */
        private final LatLonAlt myLocation;

        /**
         * Rotation, in degrees, of the camera around the Z axis. Values range
         * from −180 to +180 degrees.
         */
        private final double myRoll;

        /**
         * Rotation, in degrees, of the camera around the X axis. A value of 0
         * indicates that the view is aimed straight down toward the earth (the
         * most common case). A value for 90 for tilt indicates that the view is
         * aimed toward the horizon. Values greater than 90 indicate that the
         * view is pointed up into the sky. Values for tilt are clamped at +180
         * degrees.
         */
        private final double myTilt;

        /**
         * Constructor.
         *
         * @param location The location.
         * @param heading The heading.
         * @param tilt The tilt.
         * @param roll The roll.
         */
        public KMLCompatibleCamera(LatLonAlt location, double heading, double tilt, double roll)
        {
            myLocation = location;
            myHeading = heading;
            myTilt = tilt;
            myRoll = roll;
        }

        /**
         * Get the heading.
         *
         * @return the heading
         */
        public double getHeading()
        {
            return myHeading;
        }

        /**
         * Get the location.
         *
         * @return the location
         */
        public LatLonAlt getLocation()
        {
            return myLocation;
        }

        /**
         * Get the roll.
         *
         * @return the roll
         */
        public double getRoll()
        {
            return myRoll;
        }

        /**
         * Get the tilt.
         *
         * @return the tilt
         */
        public double getTilt()
        {
            return myTilt;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder(64);
            builder.append(myLocation);
            builder.append(", Heading : ").append(myHeading);
            builder.append(", Tilt : ").append(myTilt);
            builder.append(", Roll : ").append(myRoll);
            return builder.toString();
        }
    }
}
