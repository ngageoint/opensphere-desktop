package io.opensphere.overlay.terrainprofile;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.opensphere.core.MapManager;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.UnitsProvider.UnitsChangeListener;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.util.TerrainUtil;
import io.opensphere.core.viewer.TrajectoryGenerator;
import io.opensphere.core.viewer.TrajectoryGenerator.TrajectorySegment;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.Viewer.TrajectoryGeneratorType;
import io.opensphere.core.viewer.Viewer.ViewerPosition;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.overlay.util.AbstractOverlayWindow;

/** The terrain profile display. */
public class TerrainProfile extends AbstractOverlayWindow
{
    /** The number of sample points to use from the terrain. */
    private static final int ourSampleNumber = 200;

    /** The elevation label class. */
    private TerrainChartElevationLabel myElevationLabel;

    /** Collection of points (in Lat/Lon) that contain positions. */
    private final List<GeographicPosition> myElevations = new ArrayList<>();

    /** Geographic equivalents of myPoints. */
    private final List<GeographicPosition> myGeoPoints = new ArrayList<>();

    /** The graph class of terrain profile. */
    private TerrainChartGraph myGraph;

    /** The initial location of our graph bounding box. */
    private final ScreenBoundingBox myGraphBox = new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(250, 150));

    /** A list of listeners interested in changes to map profile positions. */
    private final List<MapProfileListener> myMapListeners = new ArrayList<>();

    /** The maximum elevation of current profile. */
    private double myMaxElevation;

    /** The minimum elevation of current profile. */
    private double myMinElevation;

    /** Our min/max label class. */
    private TerrainChartMinMaxLabel myMinMaxLabel;

    /** Collection of points in model coords that describe profile line. */
    private final List<ViewerPosition> myPoints = new ArrayList<>();

    /** The length units provider. */
    private final UnitsProvider<Length> myUnitsProvider;

    /** The current units. */
    private volatile Class<? extends Length> myUnits;

    /** Listener for units changes. */
    private final UnitsChangeListener<Length> myListener = new UnitsChangeListener<Length>()
    {
        @Override
        public void availableUnitsChanged(Class<Length> superType, Collection<Class<? extends Length>> newTypes)
        {
        }

        @Override
        public void preferredUnitsChanged(Class<? extends Length> type)
        {
            myUnits = type;
            myGraph.drawElevations();
        }
    };

    /**
     * Construct me.
     *
     * @param hudTransformer The transformer.
     * @param size The bounding box we will use for size (but not location on
     *            screen).
     * @param location The predetermined location.
     * @param resize The resize behavior.
     * @param unitsProvider The length units provider.
     */
    public TerrainProfile(TransformerHelper hudTransformer, ScreenBoundingBox size, ToolLocation location, ResizeOption resize,
            UnitsProvider<Length> unitsProvider)
    {
        super(hudTransformer, size, location, resize, ZOrderRenderProperties.TOP_Z - 40);
        myUnitsProvider = unitsProvider;
        myUnitsProvider.addListener(myListener);
        myUnits = myUnitsProvider.getPreferredUnits();
    }

    /**
     * Add a listener for map profile updates.
     *
     * @param listener The listener.
     */
    public void addMapListener(MapProfileListener listener)
    {
        if (!myMapListeners.contains(listener))
        {
            myMapListeners.add(listener);
        }
    }

    /**
     * Draw a terrain profile line on the map with the given List of positions.
     *
     * @param profileEnds The points that describe the profile line.
     */
    public void drawProfileChard(List<Vector3d> profileEnds)
    {
        myGraph.redrawChartProfile(profileEnds);
    }

    @Override
    public void init()
    {
        super.init();

        // set the layout
        setLayout(new GridLayout(250, 170, this));

        // add terrain profile graph
        addChartGraph();

        // add crosshairs
        addChartCrosshairs();

        // add min/max elevation label
        addChartMinMaxLabel();

        // add current elevation label
        addChartElevationLabel();

        getLayout().complete();
    }

    /**
     * Remove a listener for map profile updates.
     *
     * @param listener The Listener
     */
    public void removeMapListener(MapProfileListener listener)
    {
        if (myMapListeners.contains(listener))
        {
            myMapListeners.remove(listener);
        }
    }

    @Override
    public void repositionForInsets()
    {
    }

    /** Add cross hairs to graph (when moused over). */
    private void addChartCrosshairs()
    {
        TerrainChartCrosshairs crosshairs = new TerrainChartCrosshairs(this);
        GridLayoutConstraints constr = new GridLayoutConstraints(myGraphBox);
        add(crosshairs, constr);
    }

    /** Add label for terrain profile. */
    private void addChartElevationLabel()
    {
        myElevationLabel = new TerrainChartElevationLabel(this, myUnitsProvider);
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 150), new ScreenPosition(250, 170)));
        add(myElevationLabel, constr);
    }

    /** Add graph of terrain profile. */
    private void addChartGraph()
    {
        myGraph = new TerrainChartGraph(this);
        GridLayoutConstraints constr = new GridLayoutConstraints(myGraphBox);
        add(myGraph, constr);
    }

    /** Add label for terrain profile. */
    private void addChartMinMaxLabel()
    {
        myMinMaxLabel = new TerrainChartMinMaxLabel(this);
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 150), new ScreenPosition(250, 170)));
        add(myMinMaxLabel, constr);
    }

    /** Class to draw the cross hairs. */
    private class TerrainChartCrosshairs extends Renderable
    {
        /** Support class for events from the control context. */
        private final ControlEventSupport myMouseSupport;

        /**
         * Construct me.
         *
         * @param parent parent component.
         */
        public TerrainChartCrosshairs(Component parent)
        {
            super(parent);
            myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
        }

        @Override
        public void handleCleanupListeners()
        {
            myMouseSupport.cleanupListeners();
        }

        @Override
        public void init()
        {
            super.init();

            // need to create a geometry that is a transparent background in
            // order to get mouse events
            ScreenBoundingBox bbox = getDrawBounds();
            TileGeometry pickTile = getPickTile(bbox, 20);
            myMouseSupport.setActionGeometry(pickTile);
            getGeometries().add(pickTile);
        }

        @Override
        public void mouseExited(Geometry geom, Point location)
        {
            // Clear out the cross-hairs
            updateGeometry(Collections.<Geometry>emptySet());

            // let any map listeners know to remove the map marker
            for (MapProfileListener listeners : myMapListeners)
            {
                listeners.removeMapMarker();
            }
        }

        @Override
        public void mouseMoved(Geometry geom, MouseEvent event)
        {
            Point cursorPos = event.getPoint();

            // Make sure cursor point is within our geometry and we have
            // positions
            if (pointInside(cursorPos) && !myElevations.isEmpty())
            {
                ScreenBoundingBox bbox = getDrawBounds();
                List<Geometry> newGeometries = new ArrayList<>();

                double xScaleFactor = (cursorPos.getX() - getAbsoluteLocation().getUpperLeft().getX()) / bbox.getWidth();

                // Get the point that corresponds to our location
                GeographicPosition point = myElevations.get((int)(xScaleFactor * myElevations.size()));

                // now draw horizontal and vertical line (start with vertical)
                ScreenPosition bottom = new ScreenPosition((int)cursorPos.getX() - getAbsoluteLocation().getUpperLeft().getX(),
                        bbox.getLowerRight().getY());
                ScreenPosition top = new ScreenPosition((int)cursorPos.getX() - getAbsoluteLocation().getUpperLeft().getX(),
                        bbox.getUpperLeft().getY());

                List<ScreenPosition> vLinePositions = new ArrayList<>();
                vLinePositions.add(bottom);
                vLinePositions.add(top);
                PolylineGeometry.Builder<ScreenPosition> vLineBuilder = new PolylineGeometry.Builder<>();
                PolylineRenderProperties props = new DefaultPolylineRenderProperties(getBaseZOrder() + 5, true, false);
                props.setColor(Color.YELLOW);
                props.setWidth(2f);
                vLineBuilder.setVertices(vLinePositions);
                PolylineGeometry line = new PolylineGeometry(vLineBuilder, props, null);
                newGeometries.add(line);

                // Draw horizontal line
                double scaleFactor = bbox.getHeight() / (myMinElevation - myMaxElevation);
                ScreenPosition left = new ScreenPosition(bbox.getUpperLeft().getX(),
                        (point.getLatLonAlt().getAltM() - myMaxElevation) * scaleFactor);
                ScreenPosition right = new ScreenPosition(bbox.getLowerRight().getX(),
                        (point.getLatLonAlt().getAltM() - myMaxElevation) * scaleFactor);

                List<ScreenPosition> hLinePositions = new ArrayList<>();
                hLinePositions.add(left);
                hLinePositions.add(right);
                PolylineGeometry.Builder<ScreenPosition> hLineBuilder = new PolylineGeometry.Builder<>();
                hLineBuilder.setVertices(hLinePositions);
                PolylineGeometry hLine = new PolylineGeometry(hLineBuilder, props, null);
                newGeometries.add(hLine);

                // And update the current elevation
                myElevationLabel.drawElevation(point.getLatLonAlt().getAltM());

                updateMapMarker(point);

                updateGeometry(newGeometries);
            }
            else
            {
                // Don't display current elevation if mouse isn't over graph
                myElevationLabel.removeElevation();
            }
        }

        /**
         * Determine if the point is within our bounding box.
         *
         * @param pt The point to check
         * @return true if successful, false otherwise
         */
        private boolean pointInside(Point pt)
        {
            ScreenPosition upperLeft = getAbsoluteLocation().getUpperLeft();
            ScreenPosition lowerRight = getAbsoluteLocation().getLowerRight();
            return pt.getX() > upperLeft.getX() && pt.getX() < lowerRight.getX() && pt.getY() > upperLeft.getY()
                    && pt.getY() < lowerRight.getY();
        }

        /**
         * This is a custom method that replaces current geometries with the
         * given geometries with one big difference. It does not remove the
         * single tile geometry that is used as a background for the purpose of
         * catching mouse events.
         *
         * @param geometries The geometries to update.
         */
        private void updateGeometry(Collection<Geometry> geometries)
        {
            Set<Geometry> startGeoms = new HashSet<>();
            startGeoms.addAll(getGeometries());

            List<Geometry> geometriesCopy = new ArrayList<>(getGeometries());

            for (Geometry geom : geometriesCopy)
            {
                if (!(geom instanceof TileGeometry))
                {
                    getGeometries().remove(geom);
                }
            }

            Set<Geometry> endGeoms = getGeometries();
            endGeoms.addAll(geometries);

            updateGeometries(endGeoms, startGeoms);
        }

        /**
         * This calculates the map marker for a given point and then informs the
         * listeners of the updated position.
         *
         * @param point The new position for the map's marker
         */
        private void updateMapMarker(GeographicPosition point)
        {
            Viewer view = getTransformer().getToolbox().getMapManager().getStandardViewer();
            Projection projection = getTransformer().getToolbox().getMapManager().getProjection();

            // Padding for marker line in degrees
            final double padding = 1.0;
            List<GeographicPosition> positions = new ArrayList<>();

            if (view instanceof Viewer3D)
            {
                // Can't just add or subtract latitude to get a vertical line
                // since we might be rotated or near a pole
                Vector3d up = new Vector3d(((Viewer3D)view).getPosition().getUp());
                Vector3d modelPoint = projection.convertToModel(point, Vector3d.ORIGIN);

                // Find the right of our position
                Vector3d positionRight = modelPoint.multiply(-1.).cross(up).getNormalized();

                Vector3d topVec = modelPoint.rotate(positionRight, Math.toRadians(padding));
                Vector3d bottomVec = modelPoint.rotate(positionRight, Math.toRadians(-padding));

                // Convert to geographic positions
                GeographicPosition topGeo = projection.convertToPosition(topVec, ReferenceLevel.ELLIPSOID);
                GeographicPosition bottomGeo = projection.convertToPosition(bottomVec, ReferenceLevel.ELLIPSOID);

                positions.add(bottomGeo);
                positions.add(point);
                positions.add(topGeo);
            }
            else
            {
                GeographicPosition bottom = point.add(new Vector3d(0, -padding, 0));
                GeographicPosition top = point.add(new Vector3d(0, padding, 0));

                positions.add(bottom);
                positions.add(point);
                positions.add(top);
            }

            // and let any map listeners know that there is an update
            // to the marker position.
            for (MapProfileListener listeners : myMapListeners)
            {
                listeners.updateMapMarker(positions);
            }
        }
    }

    /** Class to draw the terrain profile. */
    private class TerrainChartGraph extends Renderable
    {
        /**
         * Constructor.
         *
         * @param parent parent component.
         */
        public TerrainChartGraph(Component parent)
        {
            super(parent);
        }

        /** Use our elevation positions to draw the terrain profile. */
        public synchronized void drawElevations()
        {
            Set<Geometry> startGeoms = new HashSet<>();
            startGeoms.addAll(getGeometries());
            getGeometries().clear();

            // Draw the graph
            int size = myElevations.size();
            if (size > 2)
            {
                ScreenBoundingBox bbox = getDrawBounds();

                List<ScreenPosition> positions = new ArrayList<>();

                double xcoord = 0.;
                double step = bbox.getWidth() / (size - 1.);
                double scaleFactor = myMinElevation - myMaxElevation == 0. ? 0.
                        : bbox.getHeight() / (myMinElevation - myMaxElevation);

                for (int i = 0; i < size; i++)
                {
                    ScreenPosition screenPos = null;
                    if (myMaxElevation <= 0.)
                    {
                        screenPos = bbox.getUpperLeft().add(new Vector3d(xcoord, bbox.getHeight(), 0d));
                    }
                    else
                    {
                        screenPos = bbox.getUpperLeft().add(new Vector3d(xcoord,
                                (myElevations.get(i).getLatLonAlt().getAltM() - myMaxElevation) * scaleFactor, 0d));
                    }
                    positions.add(screenPos);

                    xcoord += step;
                }

                // Draw profile
                PolylineGeometry.Builder<ScreenPosition> polyBuilder = new PolylineGeometry.Builder<>();
                PolylineRenderProperties props = new DefaultPolylineRenderProperties(getBaseZOrder() + 2, true, false);
                props.setColor(Color.ORANGE);
                props.setWidth(2f);
                polyBuilder.setVertices(positions);
                PolylineGeometry line = new PolylineGeometry(polyBuilder, props, null);

                getGeometries().add(line);

                Length minElev = Length.create(myUnits, new Meters(myMinElevation));
                Length maxElev = Length.create(myUnits, new Meters(myMaxElevation));

                // And update our label
                myMinMaxLabel.drawChartLabel(minElev, maxElev);
            }

            Set<Geometry> endGeoms = getGeometries();
            updateGeometries(endGeoms, startGeoms);
        }

        /**
         * Calculate new terrain profile and draw it.
         *
         * @param profileEnds The end positions of the profile line.
         */
        public synchronized void redrawChartProfile(List<Vector3d> profileEnds)
        {
            drawMapProfile(profileEnds);
            findElevations();
            drawElevations();
        }

        /**
         * Display the profile line on the map.
         *
         * @param profileEnds The end positions of the profile line.
         */
        private synchronized void drawMapProfile(List<Vector3d> profileEnds)
        {
            findProfile(profileEnds);
            Projection projection = getTransformer().getToolbox().getMapManager().getProjection();
            myGeoPoints.clear();
            for (ViewerPosition point : myPoints)
            {
                myGeoPoints.add(projection.convertToPosition(point.getLocation(), ReferenceLevel.ELLIPSOID));
            }
        }

        /**
         * Create lat/lon (GeographicPosition) points from the terrain profile
         * and determine min/max values.
         */
        private void findElevations()
        {
            myElevations.clear();

            final double highNumber = 100000.;
            myMaxElevation = 0.;
            myMinElevation = highNumber;

            // Loop through terrain profile points to find elevations and set
            // min/max elevations
            for (GeographicPosition original : myGeoPoints)
            {
                double elevation = TerrainUtil.getInstance().getElevationInMeters(getTransformer().getToolbox().getMapManager(),
                        original);

                LatLonAlt lla = LatLonAlt.createFromDegreesMeters(original.getLatLonAlt().getLatD(),
                        original.getLatLonAlt().getLonD(), elevation, Altitude.ReferenceLevel.ELLIPSOID);

                if (lla.getAltM() > myMaxElevation)
                {
                    myMaxElevation = lla.getAltM();
                }
                if (lla.getAltM() < myMinElevation)
                {
                    myMinElevation = lla.getAltM();
                }
                GeographicPosition geoPos = new GeographicPosition(lla);
                myElevations.add(geoPos);
            }
        }

        /**
         * Find the points along the profile to sample.
         *
         * @param profileEnds A list containing two vectors pointing to the ends
         *            of the line.
         */
        private void findProfile(List<Vector3d> profileEnds)
        {
            MapManager mapMan = getTransformer().getToolbox().getMapManager();
            DynamicViewer view = mapMan.getStandardViewer();
            myPoints.clear();

            TrajectoryGenerator flatGen = view.getTrajectoryGenerator(TrajectoryGeneratorType.FLAT);

            List<TrajectorySegment> segments = new ArrayList<>(1);
            segments.add(new TrajectorySegment(view.getRightedView(profileEnds.get(0)), view.getRightedView(profileEnds.get(1)),
                    ourSampleNumber));
            myPoints.addAll(flatGen.generateTrajectory(segments));
        }
    }
}
