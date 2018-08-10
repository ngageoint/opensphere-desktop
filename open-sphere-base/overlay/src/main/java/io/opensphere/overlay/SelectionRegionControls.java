package io.opensphere.overlay;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.MapManager;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.control.CompoundEventAdapter;
import io.opensphere.core.control.CompoundEventListener;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultKeyPressedBinding;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.action.MenuOptionListener;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.ui.RegionSelectionManager;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.event.RegionEvent;
import io.opensphere.core.event.RegionEvent.RegionEventType;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.util.SelectionMode;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * Responsible for handling the drawing of selection regions.
 */
@SuppressWarnings("PMD.GodClass")
public class SelectionRegionControls extends AbstractRegionControls implements RegionSelectionManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SelectionRegionControls.class);

    /** The color for the box. */
    private final Color myColor = new Color(50, 0, 255, 255);

    /**
     * When true, this control is drawing in the default mode and a control
     * toolbar button is not selected.
     */
    private boolean myDefaultDrawingMode;

    /** The last known mouse event. */
    private MouseEvent myLastMouse;

    /**
     * Listener for when a menu item has been selected or the menu has been
     * cancelled.
     */
    private final transient MenuOptionListener myMenuOptionListner = new MenuOptionListener()
    {
        @Override
        public void handleMenuCancelled()
        {
            myShowingMenu = false;
            getTransformer().clearRegion();
            mySelectionBoxHandler.setSelectionRegionDescription(null);
            finishRegion();

            if (myDefaultDrawingMode)
            {
                myDefaultDrawingMode = false;
                LOGGER.info("Handling menu cancellation.");
                myModeController.setSelectionMode(SelectionMode.NONE);
            }
            else
            {
                myModeController.setSelectionMode(myModeController.getDefaultSelectionMode());
            }
            LOGGER.info("Selection cancelled.");
        }

        @Override
        public void menuOptionSelected(String optionContext, String optionCommand)
        {
            cancelAndCleanup();
        }
    };

    /** The controller for the selection mode. */
    private final SelectionModeController myModeController;

    /** The selection box handler. */
    private final SelectionHandler mySelectionBoxHandler;

    /**
     * When true the menu is being displayed, so do not tell the transformer to
     * clean up the geometries yet.
     */
    private volatile boolean myShowingMenu;

    /** Listener to changes in the selection mode. */
    private final SelectionModeChangeListener mySelectionModeListener;

    /**
     * The context to which notifications are provided instead of the default
     * context.
     */
    private String myUsurpationContext;

    /** The selection mode before usurpation was activated. */
    private SelectionMode myDefaultMode;

    /** The event manager through which event pub / sub occurs. */
    private final EventManager myEventManager;

    /** The factory used to create new JTS geometries. */
    private final GeometryFactory myGeometryFactory;

    /**
     * Creates the polyline geometry.
     *
     * @param points the points
     * @param c the c
     * @param zOrder the z order
     * @return the polyline geometry
     */
    public static PolylineGeometry createPolylineGeometry(Collection<LatLonAlt> points, Color c, int zOrder)
    {
        PolylineGeometry.Builder<GeographicPosition> builder = new PolylineGeometry.Builder<>();
        List<GeographicPosition> posList = points.stream().map(
                lla -> new GeographicPosition(LatLonAlt.createFromDegrees(lla.getLatD(), lla.getLonD(), ReferenceLevel.TERRAIN)))
                .collect(Collectors.toList());

        builder.setVertices(posList);
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(zOrder, true, false);
        props.setColor(c);
        props.setWidth(3);
        return new PolylineGeometry(builder, props, null);
    }

    /**
     * Gets the distance string.
     *
     * @param distanceMeters the distance meters
     * @param units the units
     * @return the distance string
     */
    private static String getDistanceString(double distanceMeters, Class<? extends Length> units)
    {
        Length l;
        try
        {
            l = Length.create(units, new Meters(distanceMeters));
        }
        catch (InvalidUnitsException e)
        {
            LOGGER.warn("Could not use length type: " + e, e);
            l = new Meters(distanceMeters);
        }
        String lengthStr = String.format("%.3f " + l.getShortLabel(true), l.getDisplayMagnitudeObj());
        return lengthStr;
    }

    /**
     * Gets the total path length meters.
     *
     * @param llas the llas
     * @return the total path length meters
     */
    private static double getTotalPathLengthMeters(List<LatLonAlt> llas)
    {
        double totalLengthMeters = 0.0;
        if (llas != null)
        {
            LatLonAlt last = null;
            for (LatLonAlt lla : llas)
            {
                if (last != null)
                {
                    totalLengthMeters += GeographicBody3D.greatCircleDistanceM(lla, last,
                            WGS84EarthConstants.RADIUS_EQUATORIAL_M);
                }
                last = lla;
            }
        }
        return totalLengthMeters;
    }

    /**
     * Construct the Selection Box Controls.
     *
     * @param pEventManager the event manager through which event pub / sub
     *            occurs.
     * @param mapManager The map manager.
     * @param unitsRegistry The units registry.
     * @param uiRegistry The UI registry.
     * @param transformer The transformer used to generate selection region
     *            geometries.
     * @param handler The handler to notify when a bounding box is selected.
     * @param smc the smc
     */
    public SelectionRegionControls(EventManager pEventManager, MapManager mapManager, UnitsRegistry unitsRegistry,
            UIRegistry uiRegistry, SelectionRegionTransformer transformer, SelectionHandler handler, SelectionModeController smc)
    {
        super(mapManager, unitsRegistry, transformer);

        myGeometryFactory = new GeometryFactory();
        myEventManager = pEventManager;
        myModeController = smc;
        mySelectionBoxHandler = handler;

        mySelectionModeListener = mode ->
        {
            if (!myShowingMenu && !(myDefaultDrawingMode && mode != SelectionMode.NONE))
            {
                getTransformer().clearRegion();
                mySelectionBoxHandler.setSelectionRegionDescription(null);
                finishRegion();
            }
        };

        myModeController.addSelectionModeChangeListener(mySelectionModeListener);

        if (uiRegistry != null)
        {
            uiRegistry.registerAsRegionSelectionManager(this);
        }
    }

    @Override
    public void close(UIRegistry uiRegistry, ControlRegistry controlRegistry)
    {
        super.close(uiRegistry, controlRegistry);

        mySelectionBoxHandler.removeMenuOptionListener(myMenuOptionListner);
    }

    /**
     * Register with the control registry.
     *
     * @param controlRegistry The control registry.
     */
    public void register(ControlRegistry controlRegistry)
    {
        ControlContext controlContext = controlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);

        // Listeners for starting default selection mode
        TargetedNoModeMouseListener defaultStart = new TargetedNoModeMouseListener();
        defaultStart.setReassignable(false);
        controlContext.addListener(defaultStart,
                new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        addControlContextListener(defaultStart);

        TargetedCompoundKeyListener shiftListener = new TargetedCompoundKeyListener();
        shiftListener.setReassignable(false);
        addControlContextListener(shiftListener);
        controlContext.addListener((CompoundEventListener)shiftListener, new DefaultKeyPressedBinding(KeyEvent.VK_SHIFT));

        // Listener for drawing actions
        TargetedModedMouseListener drawListener = new TargetedModedMouseListener();
        drawListener.setReassignable(false);
        controlContext.addListener(drawListener, new DefaultMouseBinding(MouseEvent.MOUSE_CLICKED),
                new DefaultMouseBinding(MouseEvent.MOUSE_MOVED),
                new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK),
                new DefaultMouseBinding(MouseEvent.MOUSE_RELEASED, InputEvent.BUTTON1_DOWN_MASK),
                new DefaultMouseBinding(MouseEvent.MOUSE_DRAGGED, InputEvent.BUTTON1_DOWN_MASK),
                new DefaultMouseBinding(MouseEvent.MOUSE_CLICKED, InputEvent.SHIFT_DOWN_MASK),
                new DefaultMouseBinding(MouseEvent.MOUSE_MOVED, InputEvent.SHIFT_DOWN_MASK),
                new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                new DefaultMouseBinding(MouseEvent.MOUSE_RELEASED, InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                new DefaultMouseBinding(MouseEvent.MOUSE_DRAGGED, InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        addControlContextListener(drawListener);

        // Listener for keys for completion or cancelling
        TargetedDiscreteKeyListener resetCompleteListener = new TargetedDiscreteKeyListener();
        resetCompleteListener.setReassignable(false);
        addControlContextListener(resetCompleteListener);
        controlContext.addListener(resetCompleteListener, new DefaultKeyPressedBinding(KeyEvent.VK_ESCAPE),
                new DefaultKeyPressedBinding(KeyEvent.VK_SPACE), new DefaultKeyPressedBinding(KeyEvent.VK_ENTER));

        mySelectionBoxHandler.addMenuOptionListener(myMenuOptionListner);
    }

    @Override
    public synchronized void relinquishRegionContext(String usurpationContext)
    {
        myUsurpationContext = null;
        myModeController.setSelectionMode(myDefaultMode);
        myModeController.setSelectionMode(SelectionMode.NONE);
    }

    @Override
    public synchronized void usurpRegionContext(String usurpationContext, SelectionMode mode)
    {
        myUsurpationContext = usurpationContext;
        myDefaultMode = myModeController.getDefaultSelectionMode();
        myModeController.setSelectionMode(mode);
    }

    /**
     * Completes the creation of a line.
     * 
     * @return true when the line is complete, false otherwise.
     */
    protected synchronized boolean completeLine()
    {
        MouseEvent mousePosition = myLastMouse;
        if (myModeController.getSelectionMode() == SelectionMode.LINE && getPositions().size() > 2)
        {
            setCompleted(mousePosition);
            finishLine();
            return true;
        }
        return false;
    }

    /**
     * Complete the polygon.
     *
     * @return true when the polygon was completed.
     */
    protected synchronized boolean completePolygon()
    {
        MouseEvent mousePosition = myLastMouse;
        if (myModeController.getSelectionMode() == SelectionMode.POLYGON && getPositions().size() > 2)
        {
            setSelectionBox(getPositions(), null);
            setCompleted(mousePosition);
            finishRegion();
            return true;
        }
        return false;
    }

    @Override
    protected Color getColor()
    {
        return myColor;
    }

    @Override
    protected void setBoundingBoxRegion(LatLonAlt begin, LatLonAlt end)
    {
        GeographicBoundingBox bbox = getBoundingBox(begin, end);

        LatLonAlt lowerLeft = bbox.getLowerLeft().getLatLonAlt();
        LatLonAlt upperRight = bbox.getUpperRight().getLatLonAlt();
        double left = lowerLeft.getLonD();
        double right = upperRight.getLonD();
        double bottom = lowerLeft.getLatD();
        double top = upperRight.getLatD();

        double topDist = GeographicBody3D.greatCircleDistanceM(LatLonAlt.createFromDegrees(top, left),
                LatLonAlt.createFromDegrees(top, right), WGS84EarthConstants.RADIUS_EQUATORIAL_M);
        double sideDist = GeographicBody3D.greatCircleDistanceM(LatLonAlt.createFromDegrees(top, right),
                LatLonAlt.createFromDegrees(bottom, right), WGS84EarthConstants.RADIUS_EQUATORIAL_M);
        double botDist = GeographicBody3D.greatCircleDistanceM(LatLonAlt.createFromDegrees(bottom, left),
                LatLonAlt.createFromDegrees(bottom, right), WGS84EarthConstants.RADIUS_EQUATORIAL_M);

        StringBuilder sb = new StringBuilder(20);
        sb.append("TOP: ").append(getDistanceString(topDist, getUnitsRegistry().getPreferredUnits(Length.class)));
        sb.append("\nBOT: ").append(getDistanceString(botDist, getUnitsRegistry().getPreferredUnits(Length.class)));
        sb.append("\nSIDE: ").append(getDistanceString(sideDist, getUnitsRegistry().getPreferredUnits(Length.class)));

        String labelString = sb.toString();
        String desc = StringUtilities.concat("BoundingBox selection: ", begin, " ", end);

        Polygon geom = JTSUtilities.createPolygon(left, right, bottom, top, myGeometryFactory);

        getTransformer().setRegion(geom, getColor(), null, end, labelString);
        myEventManager.publishEvent(new RegionEvent(this, RegionEventType.REGION_SELECTED, bbox));

        mySelectionBoxHandler.setSelectionRegionDescription(desc);
    }

    /**
     * Set the selection region to be a circle.
     *
     * @param center The center of the circle.
     * @param edge An edge of the circle.
     */
    protected void setCircleSelection(LatLonAlt center, LatLonAlt edge)
    {
        Polygon geom = JTSUtilities.createCircle(center, edge, JTSUtilities.NUM_CIRCLE_SEGMENTS);
        double meters = GeographicBody3D.greatCircleDistanceM(center, edge, WGS84EarthConstants.RADIUS_EQUATORIAL_M);
        String labelString = StringUtilities.concat("Radius: ",
                getDistanceString(meters, getUnitsRegistry().getPreferredUnits(Length.class)));
        PolylineGeometry pg = createPolylineGeometry(List.of(center, edge), Color.blue, ZOrderRenderProperties.TOP_Z);
        String desc = StringUtilities.concat("Circle selection: center is ", center, " edge is ", edge);
        mySelectionBoxHandler.setSelectionRegionDescription(desc);
        getTransformer().setRegion(geom, myColor, Collections.singletonList((Geometry)pg), edge, labelString);
    }

    /**
     * @param llas
     */
    protected void setLineDraw(List<LatLonAlt> llas)
    {
        Coordinate[] coord = new Coordinate[llas.size() + 1];
        for (int i = 0; i < llas.size(); i++)
        {
            LatLonAlt lla = llas.get(i);
            coord[i] = new Coordinate(lla.getLonD(), lla.getLatD());
        }
        coord[coord.length - 1] = coord[coord.length - 2];
        try
        {
            LineString geom = myGeometryFactory.createLineString(coord);

            // Build up the label.
            StringBuilder sb = new StringBuilder("TOT: ");
            sb.append(getDistanceString(getTotalPathLengthMeters(llas), getUnitsRegistry().getPreferredUnits(Length.class)));
            if (llas.size() >= 2)
            {
                double lastSegDist = GeographicBody3D.greatCircleDistanceM(llas.get(llas.size() - 1), llas.get(llas.size() - 2),
                        WGS84EarthConstants.RADIUS_EQUATORIAL_M);
                sb.append("\nSEG: ").append(getDistanceString(lastSegDist, getUnitsRegistry().getPreferredUnits(Length.class)));
            }
            LatLonAlt labelLocation = llas.get(llas.size() - 1);

            LOGGER.info("Label Location: " + labelLocation);
            getTransformer().setRegion(geom, myColor, null, labelLocation, sb.toString());
        }
        catch (IllegalArgumentException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Failed to construct line: " + e, e);
            }
        }
    }

    /**
     * Set the selection box as pickable.
     *
     * @param mouseEvent The mouse event which occurred upon completion of the
     *            region.
     */
    protected synchronized void setCompleted(MouseEvent mouseEvent)
    {
        if (!myShowingMenu)
        {
            handleCompletion(mouseEvent, getTransformer().getGeometries());
        }
    }

    /**
     * Set the selection region to be a polygon.
     *
     * @param llas The vertices of the polygon.
     * @param finalLeg the final leg if true, false if not.
     */
    protected void setPolygonSelection(List<LatLonAlt> llas, boolean finalLeg)
    {
        Coordinate[] coord = new Coordinate[llas.size() + 1];
        for (int i = 0; i < llas.size(); i++)
        {
            LatLonAlt lla = llas.get(i);
            coord[i] = new Coordinate(lla.getLonD(), lla.getLatD());
        }
        coord[coord.length - 1] = coord[0];
        try
        {
            // Make sure we draw that first segment while we're dragging the
            // mouse by double adding the second point to ensure we have at
            // least 4 points otherwise createLinearRing will throw an
            // exception.
            if (coord.length == 3)
            {
                coord = Arrays.copyOf(coord, 4);
                coord[3] = coord[2];
                coord[2] = coord[1];
            }

            Polygon geom = myGeometryFactory.createPolygon(myGeometryFactory.createLinearRing(coord), null);

            // Build up the label.
            double totalDist = getTotalPathLengthMeters(llas);
            StringBuilder sb = new StringBuilder();
            sb.append("TOT: ").append(getDistanceString(totalDist, getUnitsRegistry().getPreferredUnits(Length.class)));
            if (llas.size() >= 2)
            {
                double lastSegDist = GeographicBody3D.greatCircleDistanceM(llas.get(llas.size() - 1), llas.get(llas.size() - 2),
                        WGS84EarthConstants.RADIUS_EQUATORIAL_M);
                sb.append("\nSEG: ").append(getDistanceString(lastSegDist, getUnitsRegistry().getPreferredUnits(Length.class)));
            }
            String labelString = sb.toString();
            LatLonAlt labelLoc = llas.get(llas.size() - 1);

            // Use a partially transparent color for the main selection region,
            // and decorate on top with an opaque version to make it appear that
            // the final leg is for guidance only.
            Color c = new Color(myColor.getRed(), myColor.getGreen(), myColor.getBlue(), 100);
            getTransformer().setRegion(geom, finalLeg ? myColor : c,
                    Collections.singletonList((Geometry)createPolylineGeometry(llas, myColor, ZOrderRenderProperties.TOP_Z)),
                    labelLoc, finalLeg ? null : labelString);
            getTransformer().setRegion(geom, finalLeg ? myColor : c,
                    Collections.singletonList((Geometry)createPolylineGeometry(llas, myColor, ZOrderRenderProperties.TOP_Z)),
                    labelLoc, finalLeg ? null : labelString);
            mySelectionBoxHandler.setSelectionRegionDescription(StringUtilities.concat("Polygon selection: ", llas));
        }
        catch (IllegalArgumentException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Failed to construct polygon: " + e, e);
            }
        }
    }

    @Override
    protected synchronized void setSelectionBox(List<? extends LatLonAlt> positions, MouseEvent endPoint)
    {
        if (getPositions().isEmpty())
        {
            return;
        }

        List<LatLonAlt> llas = addEndPoint(positions, endPoint == null ? null : endPoint.getPoint());
        if (llas == null)
        {
            return;
        }
        switch (myModeController.getSelectionMode())
        {
            case BOUNDING_BOX:
                setBoundingBoxRegion(llas.get(0), llas.get(1));
                break;
            case POLYGON:
                myLastMouse = endPoint;
                setPolygonSelection(llas, endPoint == null);
                break;
            case CIRCLE:
                setCircleSelection(llas.get(0), llas.get(1));
                break;
            case LINE:
                myLastMouse = endPoint;
                setLineDraw(llas);
                break;
            default:
                throw new UnexpectedEnumException(myModeController.getSelectionMode());
        }
    }

    /**
     * Called when the selection box has been drawn, will notify listeners.
     *
     * @param mouseEvent The mouse event which occurred upon completion of the
     *            region.
     * @param selectionBoxGeometries The selection box geometries.
     */
    protected synchronized void setSelectionBoxDrawn(MouseEvent mouseEvent, List<Geometry> selectionBoxGeometries)
    {
        if (!myShowingMenu && (myModeController.getSelectionMode() != SelectionMode.POLYGON
                && myModeController.getSelectionMode() != SelectionMode.LINE) && !selectionBoxGeometries.isEmpty())
        {
            handleCompletion(mouseEvent, selectionBoxGeometries);
        }
    }

    /**
     * Handles completion of a region.
     *
     * @param mouseEvent the mouse event
     * @param selectionBoxGeometries the geometries
     */
    private void handleCompletion(MouseEvent mouseEvent, List<? extends Geometry> selectionBoxGeometries)
    {
        if (myUsurpationContext == null)
        {
            myShowingMenu = true;
        }

        List<Geometry> geometries = New.list(selectionBoxGeometries);

        mySelectionBoxHandler.selectionRegionCompleted(mouseEvent,
                myUsurpationContext == null ? ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT : myUsurpationContext, geometries);

        LOGGER.info("Handling completion of polygon.");
        myModeController.setSelectionMode(SelectionMode.NONE);

        // TODO pass in bounding box
        myEventManager.publishEvent(new RegionEvent(this, RegionEventType.REGION_COMPLETED, null));
    }

    /** Cancel whatever is being drawn and perform any cleanup as necessary. */
    private synchronized void cancelAndCleanup()
    {
        myShowingMenu = false;
        getTransformer().clearRegion();
        mySelectionBoxHandler.setSelectionRegionDescription(null);
        finishRegion();
        if (myDefaultDrawingMode)
        {
            myDefaultDrawingMode = false;
            myModeController.setSelectionMode(SelectionMode.NONE);
        }
    }

    /**
     * Listener for key events which can occur during drawing. Since this is a
     * compound adapter it will only receive KEY_PRESSED or KEY_RELEASED events.
     */
    protected class TargetedCompoundKeyListener extends CompoundEventAdapter
    {
        /** Constructor. */
        public TargetedCompoundKeyListener()
        {
            super("Selection Controls", "Draw Selection Region", "Used for drawing selection regions.");
        }

        @Override
        public void eventEnded(final InputEvent event)
        {
            getQueryRegionExecutor().execute(() ->
            {
                KeyEvent keyEvent = (KeyEvent)event;
                if (keyEvent.getKeyCode() == KeyEvent.VK_SHIFT && myDefaultDrawingMode)
                {
                    if (myModeController.getSelectionMode() == SelectionMode.POLYGON)
                    {
                        if (!myShowingMenu && !completePolygon())
                        {
                            cancelAndCleanup();
                        }
                    }
                    else
                    {
                        if (!myShowingMenu)
                        {
                            cancelAndCleanup();
                        }
                    }
                    myDefaultDrawingMode = false;
                    myModeController.setSelectionMode(SelectionMode.NONE);
                }
            });
        }

        @Override
        public int getTargetPriority()
        {
            return 2000;
        }

        @Override
        public boolean isTargeted()
        {
            synchronized (SelectionRegionControls.this)
            {
                return myModeController.getSelectionMode() != SelectionMode.NONE;
            }
        }

        @Override
        public boolean mustBeTargeted()
        {
            return true;
        }
    }

    /**
     * Listener for key events which can occur during drawing. Since this is a
     * discrete adapter it will only receive KEY_TYPED events and never
     * KEY_PRESSED or KEY_RELEASED events.
     */
    protected class TargetedDiscreteKeyListener extends TargetedDiscreteEventAdapter
    {
        @Override
        public void eventOccurred(final InputEvent evt)
        {
            final KeyEvent keyEvent = (KeyEvent)evt;
            if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE || keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
            {
                keyEvent.consume();
            }
            getQueryRegionExecutor().execute(() ->
            {
                if (keyEvent.getID() == KeyEvent.KEY_PRESSED)
                {
                    if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
                    {
                        myShowingMenu = false;
                        getTransformer().clearRegion();
                        mySelectionBoxHandler.setSelectionRegionDescription(null);
                        finishRegion();
                        LOGGER.info("Selection cancelled.");
                    }
                    else if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE || keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                        completePolygon();
                    }
                }
            });
        }

        @Override
        public boolean isTargeted()
        {
            synchronized (SelectionRegionControls.this)
            {
                return myModeController.getSelectionMode() != SelectionMode.NONE;
            }
        }
    }

    /**
     * Listener for handling mouse events which are targeted to me when I have a
     * drawing mode currently set. This covers the majority of the actual
     * drawing operations.
     */
    protected class TargetedModedMouseListener extends TargetedDiscreteEventAdapter
    {
        @Override
        public void eventOccurred(final InputEvent evt)
        {
            final MouseEvent event = (MouseEvent)evt;

            // Check to see if this is an event that we should consume. It is
            // important to consume the event before we do the real work on the
            // executor.
            if (event.getID() == MouseEvent.MOUSE_PRESSED || event.getID() == MouseEvent.MOUSE_DRAGGED)
            {
                event.consume();
            }
            else if (event.getID() == MouseEvent.MOUSE_CLICKED && (event.getButton() == 1 && event.getClickCount() == 2
                    || event.getButton() == 3 && (myModeController.getSelectionMode() == SelectionMode.POLYGON
                            || myModeController.getSelectionMode() == SelectionMode.LINE)))
            {
                event.consume();
            }

            getQueryRegionExecutor().execute(() -> handleMouseDrawEvent(event));
        }

        /**
         * Handles the mouse draw event.
         * 
         * @param event the event to handle.
         */
        private void handleMouseDrawEvent(final MouseEvent event)
        {
            if (event.getID() == MouseEvent.MOUSE_PRESSED)
            {
                addPosition(event.getPoint());
            }
            else if (event.getID() == MouseEvent.MOUSE_RELEASED)
            {
                List<Geometry> selectionBoxGeometries = New.list(getTransformer().getGeometries());
                if (myModeController.getSelectionMode() != SelectionMode.POLYGON
                        && myModeController.getSelectionMode() != SelectionMode.LINE)
                {
                    finishRegion();
                }
                // Inform listeners that selection region has been
                // drawn.
                setSelectionBoxDrawn(event, selectionBoxGeometries);
            }
            else if (event.getID() == MouseEvent.MOUSE_DRAGGED)
            {
                setSelectionBox(getPositions(), event);
            }
            else if (event.getID() == MouseEvent.MOUSE_MOVED)
            {
                setSelectionBox(getPositions(), event);
            }
            else if (event.getID() == MouseEvent.MOUSE_CLICKED)
            {
                if (event.getButton() == 1 && event.getClickCount() == 2)
                {
                    if (myModeController.getSelectionMode() == SelectionMode.POLYGON)
                    {
                        completePolygon();
                    }
                    else if (myModeController.getSelectionMode() == SelectionMode.LINE)
                    {
                        completeLine();
                    }
                }
                else if (event.getButton() == 3 && myModeController.getSelectionMode() == SelectionMode.POLYGON
                        && getPositions().size() > 1)
                {
                    getPositions().remove(getPositions().size() - 1);
                    setSelectionBox(getPositions(), event);
                }
                else if (event.getButton() == 3 && myModeController.getSelectionMode() == SelectionMode.LINE
                        && getPositions().size() > 1)
                {
                    getPositions().remove(getPositions().size() - 1);
                    setSelectionBox(getPositions(), event);
                }
            }
        }

        @Override
        public boolean isTargeted()
        {
            synchronized (SelectionRegionControls.this)
            {
                return myModeController.getSelectionMode() != SelectionMode.NONE;
            }
        }
    }

    /**
     * Listener for handling mouse events which are targeted to me when I have
     * no current drawing mode. This is specifically for events which will cause
     * default drawing mode to be engaged.
     */
    private class TargetedNoModeMouseListener extends TargetedDiscreteEventAdapter
    {
        @Override
        public void eventOccurred(final InputEvent event)
        {
            event.consume();
            getQueryRegionExecutor().execute(() ->
            {
                myDefaultDrawingMode = true;
                myModeController.setSelectionMode(myModeController.getDefaultSelectionMode());
                addPosition(((MouseEvent)event).getPoint());
            });
        }

        @Override
        public boolean isTargeted()
        {
            synchronized (SelectionRegionControls.this)
            {
                return myModeController.getSelectionMode() == SelectionMode.NONE;
            }
        }
    }
}
