package io.opensphere.overlay.worldmap;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.model.GeographicUtilities;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.Viewer3D;

/** Footprint for the world map. */
public class WorldMapFootPrint extends Renderable
{
    /** Listen to events from the main viewer. */
    private final ViewChangeListener myMainViewListener = new ViewChangeListener()
    {
        @Override
        public void viewChanged(Viewer viewer, ViewChangeSupport.ViewChangeType type)
        {
            myViewChangeExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    drawFootPrint();
                }
            });
        }
    };

    /** Executor to handle view changes. */
    private final ProcrastinatingExecutor myViewChangeExecutor;

    /**
     * Construct me.
     *
     * @param parent parent component.
     * @param executor Executor shared by HUD components.
     */
    public WorldMapFootPrint(Component parent, ScheduledExecutorService executor)
    {
        super(parent);
        myViewChangeExecutor = new ProcrastinatingExecutor(executor);
    }

    @Override
    public void handleCleanupListeners()
    {
        super.handleCleanupListeners();
        getTransformer().getToolbox().getMapManager().getViewChangeSupport().removeViewChangeListener(myMainViewListener);
    }

    @Override
    public void init()
    {
        super.init();

        // Register as a listener for view change events
        getTransformer().getToolbox().getMapManager().getViewChangeSupport().addViewChangeListener(myMainViewListener);

        drawFootPrint();
    }

    /**
     * This method assures the point passed in is within the range of -180 to
     * 180. If the longitude is outside of the range it will be converted. So
     * for example a value of -185 becomes 175.
     *
     * @param position The GeographicPosition to check.
     * @return position The updated position.
     */
    private GeographicPosition checkLongitudes(GeographicPosition position)
    {
        GeographicPosition geoPosition = null;
        if (Math.abs(position.getLatLonAlt().getLonD()) > 180d)
        {
            if (position.getLatLonAlt().getLonD() < -180d)
            {
                geoPosition = new GeographicPosition(LatLonAlt.createFromDegreesMeters(position.getLatLonAlt().getLatD(),
                        position.getLatLonAlt().getLonD() + 360d, position.getLatLonAlt().getAltM(),
                        Altitude.ReferenceLevel.TERRAIN));
            }
            else if (position.getLatLonAlt().getLonD() > 180d)
            {
                geoPosition = new GeographicPosition(LatLonAlt.createFromDegreesMeters(position.getLatLonAlt().getLatD(),
                        position.getLatLonAlt().getLonD() - 360d, position.getLatLonAlt().getAltM(),
                        Altitude.ReferenceLevel.TERRAIN));
            }
        }
        else
        {
            geoPosition = position;
        }

        return geoPosition;
    }

    /**
     * Draw a footprint of the visual part of the map currently seen.
     */
    private synchronized void drawFootPrint()
    {
        List<List<ScreenPosition>> footPrint = getFootPrint();

        Set<Geometry> startGeoms = new HashSet<>(getGeometries());
        getGeometries().clear();

        for (List<ScreenPosition> positions : footPrint)
        {
            if (!positions.isEmpty())
            {
                PolylineGeometry.Builder<ScreenPosition> polyBuilder = new PolylineGeometry.Builder<>();
                PolylineRenderProperties props = new DefaultPolylineRenderProperties(getBaseZOrder() + 4, true, false);
                props.setColor(Color.ORANGE);
                props.setWidth(2f);
                polyBuilder.setVertices(positions);
                PolylineGeometry line = new PolylineGeometry(polyBuilder, props, null);

                getGeometries().add(line);
            }
        }
        Set<Geometry> endGeoms = getGeometries();
        updateGeometries(endGeoms, startGeoms);
    }

    /**
     * Calculate the screen positions that compose the footprint.
     *
     * @return List of screen positions describing footprint.
     */
    private List<List<ScreenPosition>> getFootPrint()
    {
        List<List<ScreenPosition>> screenPositions = new ArrayList<>();

        // Get the list of geographic boundary points
        List<GeographicPosition> boundaryPoints = getTransformer().getToolbox().getMapManager().getVisibleBoundaries();

        // Now do a check for crossing the longitude boundary
        // This may result in two collections of points.
        // This only needs to be performed for 3-d
        List<List<GeographicPosition>> footPrints = new ArrayList<>();
        Viewer view = getTransformer().getToolbox().getMapManager().getStandardViewer();
        if (view instanceof Viewer3D)
        {
            footPrints = longitudeBoundaryCheck(boundaryPoints);
        }
        else
        {
            footPrints.add(boundaryPoints);
        }

        // For these points we need to scale to our mini map and
        // convert to screen coordinates.
        ScreenBoundingBox bbox = getDrawBounds();
        for (List<GeographicPosition> footprint : footPrints)
        {
            List<ScreenPosition> screenPos = GeographicUtilities.toScreenPositions(footprint, bbox);
            screenPositions.add(screenPos);
        }

        return screenPositions;
    }

    /**
     * Given a list of positions that constitute a polygon, go through and check
     * if the longitude boundary is crossed. If so, create two separate
     * collections of points to return.
     *
     * @param origPositions The original polygon points.
     * @return A list of lists of points. If there are no border crossings only
     *         one list is returned and if there is a border crossing there will
     *         be two lists returned.
     */
    private List<List<GeographicPosition>> longitudeBoundaryCheck(List<GeographicPosition> origPositions)
    {
        List<List<GeographicPosition>> newPositions = new ArrayList<>();

        GeographicPosition p0 = null;

        if (!origPositions.isEmpty())
        {
            p0 = origPositions.get(0);
        }
        else
        {
            return newPositions;
        }

        List<GeographicPosition> positions = new ArrayList<>();
        List<GeographicPosition> crossedPositions = new ArrayList<>();

        boolean crossLongitudeBorder = false;

        GeographicPosition p1 = null;

        for (int i = 0; i < origPositions.size(); ++i)
        {
            p1 = origPositions.get(i);

            // Need to assure longitude is within -180 to 180 range for p0
            p0 = checkLongitudes(p0);

            // And again for p1
            p1 = checkLongitudes(p1);

            // Now check to see if we cross the longitude boundary
            if (p0.getLatLonAlt().positionsCrossLongitudeBoundary(p1.getLatLonAlt()))
            {
                if (!crossLongitudeBorder) // p0 on the inside and p1 on the
                                           // outside
                {
                    // Add border points to collections
                    if (p0.getLatLonAlt().getLonD() > 0)
                    {
                        // Add border intersection points back in
                        // TODO find correct Latitude intersection point for
                        // these
                        positions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(p1.getLatLonAlt().getLatD(), 180d,
                                p1.getLatLonAlt().getAltM(), p1.getLatLonAlt().getAltitudeReference())));
                        crossedPositions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(p1.getLatLonAlt().getLatD(),
                                -180d, p1.getLatLonAlt().getAltM(), p1.getLatLonAlt().getAltitudeReference())));
                    }
                    else
                    {
                        positions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(p1.getLatLonAlt().getLatD(), -180d,
                                p1.getLatLonAlt().getAltM(), p1.getLatLonAlt().getAltitudeReference())));
                        crossedPositions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(p1.getLatLonAlt().getLatD(),
                                180d, p1.getLatLonAlt().getAltM(), p1.getLatLonAlt().getAltitudeReference())));
                    }
                }
                else
                // p1 on the inside and p0 on the outside (coming back in)
                {
                    // Add border points to collections
                    if (p1.getLatLonAlt().getLonD() < 0)
                    {
                        // Add border intersection points back in
                        // TODO find correct Latitude intersection point for
                        // these
                        positions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(p1.getLatLonAlt().getLatD(), -180d,
                                p1.getLatLonAlt().getAltM(), p1.getLatLonAlt().getAltitudeReference())));
                        crossedPositions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(p1.getLatLonAlt().getLatD(),
                                180d, p1.getLatLonAlt().getAltM(), p1.getLatLonAlt().getAltitudeReference())));
                    }
                    else
                    {
                        positions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(p1.getLatLonAlt().getLatD(), 180d,
                                p1.getLatLonAlt().getAltM(), p1.getLatLonAlt().getAltitudeReference())));
                        crossedPositions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(p1.getLatLonAlt().getLatD(),
                                -180d, p1.getLatLonAlt().getAltM(), p1.getLatLonAlt().getAltitudeReference())));
                    }
                }

                // First time through set the boolean that we have crossed.
                // Assuming a polygon, if we cross over the border we should
                // come back in. Second time through set back to false;
                crossLongitudeBorder = !crossLongitudeBorder;
            }

            // If we have crossed the border, then put these points in
            // crossedPositions
            if (crossLongitudeBorder)
            {
                crossedPositions.add(p1);
            }
            else
            {
                positions.add(p1);
            }

            p0 = p1;
        }

        newPositions.add(positions);
        if (!crossedPositions.isEmpty())
        {
            newPositions.add(crossedPositions);
        }

        return newPositions;
    }
}
