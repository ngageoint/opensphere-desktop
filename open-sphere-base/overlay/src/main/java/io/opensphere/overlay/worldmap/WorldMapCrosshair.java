package io.opensphere.overlay.worldmap;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
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
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ViewerAnimator;

/** Cross hair for the world map. */
public class WorldMapCrosshair extends AbstractWorldMapRenderable
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
                    drawCrosshair();
                }
            });
        }
    };

    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    /** Executor to handle view changes. */
    private final ProcrastinatingExecutor myViewChangeExecutor;

    /**
     * Constructor.
     *
     * @param parent parent component.
     * @param executor Executor shared by HUD components.
     */
    public WorldMapCrosshair(Component parent, ScheduledExecutorService executor)
    {
        super(parent);
        myViewChangeExecutor = new ProcrastinatingExecutor(executor);
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
    }

    @Override
    public void handleCleanupListeners()
    {
        super.handleCleanupListeners();
        getTransformer().getToolbox().getMapManager().getViewChangeSupport().removeViewChangeListener(myMainViewListener);
        myMouseSupport.cleanupListeners();
    }

    @Override
    public void init()
    {
        super.init();

        // Register as a listener for view change events
        getTransformer().getToolbox().getMapManager().getViewChangeSupport().addViewChangeListener(myMainViewListener);

        drawCrosshair();
    }

    @Override
    public void mouseDragged(Geometry geom, Point dragStart, MouseEvent event)
    {
        Point end = event.getPoint();
        GeographicPosition pos = convertToLatLon(end);

        ViewerAnimator animator = new ViewerAnimator(getTransformer().getToolbox().getMapManager().getStandardViewer(), pos);
        animator.snapToPosition();
    }

    /**
     * Draw the crosshair over our position on the mini world map.
     */
    protected synchronized void drawCrosshair()
    {
        ScreenPosition position = calculateScaledPosition();

        Set<Geometry> startGeoms = new HashSet<>();
        startGeoms.addAll(getGeometries());
        getGeometries().clear();

        // measured from center out
        int crossHairSize = 10;
        ScreenBoundingBox bbox = getDrawBounds();

        // Do a check to see if these points cross outside our bbox
        double bottomY = position.getY() + crossHairSize > bbox.getLowerRight().getY() ? bbox.getLowerRight().getY()
                : position.getY() + crossHairSize;
        double topY = position.getY() - crossHairSize < bbox.getUpperRight().getY() ? bbox.getUpperRight().getY()
                : position.getY() - crossHairSize;

        ScreenPosition bottom = new ScreenPosition(position.getX(), bottomY);
        ScreenPosition top = new ScreenPosition(position.getX(), topY);

        // Do a check to see if these points cross outside our bbox
        double leftX = position.getX() - crossHairSize < bbox.getLowerLeft().getX() ? bbox.getLowerLeft().getX()
                : position.getX() - crossHairSize;
        double rightX = position.getX() + crossHairSize > bbox.getLowerRight().getX() ? bbox.getLowerRight().getX()
                : position.getX() + crossHairSize;

        ScreenPosition left = new ScreenPosition(leftX, position.getY());
        ScreenPosition right = new ScreenPosition(rightX, position.getY());

        List<ScreenPosition> positions = new ArrayList<>();
        // Rather than create multiple lines, create a single line that
        // overlaps at parts
        positions.add(bottom);
        positions.add(top);
        positions.add(position);
        positions.add(left);
        positions.add(right);

        PolylineGeometry.Builder<ScreenPosition> polyBuilder = new PolylineGeometry.Builder<>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(getBaseZOrder() + 5, true, true);
        props.setColor(Color.ORANGE);
        props.setWidth(3);
        polyBuilder.setVertices(positions);
        PolylineGeometry line = new PolylineGeometry(polyBuilder, props, null);

        myMouseSupport.setActionGeometry(line);

        getGeometries().add(line);

        Set<Geometry> endGeoms = getGeometries();
        updateGeometries(endGeoms, startGeoms);
    }

    /**
     * Finds our current position from viewer and then sets the corresponding
     * scaled screen position on mini map.
     *
     * @return The position.
     */
    private ScreenPosition calculateScaledPosition()
    {
        DynamicViewer view = getTransformer().getToolbox().getMapManager().getStandardViewer();
        Vector3d position = new Vector3d(view.getPosition().getLocation());
        // Convert to lat/lon and then we can scale to our mini map
        GeographicPosition geoPos = getTransformer().getToolbox().getMapManager().getProjection().convertToPosition(position,
                ReferenceLevel.ELLIPSOID);

        ScreenBoundingBox bbox = getDrawBounds();

        int x = (int)(bbox.getWidth() * (geoPos.getLatLonAlt().getLonD() + 180) / 360);
        int y = (int)(bbox.getHeight() * (geoPos.getLatLonAlt().getLatD() + 90) / 180);

        return new ScreenPosition(bbox.getUpperLeft().getX() + x, bbox.getLowerRight().getY() - y);
    }
}
