package io.opensphere.mantle.mappoint.impl;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.callout.Callout;
import io.opensphere.core.callout.CalloutImpl;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultPickListener;
import io.opensphere.core.control.PickListener.PickEvent;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.GeographicBoxAnchor;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.angle.DegreesMinutesSeconds;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.mantle.mp.MutableMapAnnotationPoint;

/** This is the transformer class for displaying map points. */
@SuppressWarnings("PMD.GodClass")
public class MapPointTransformer extends DefaultTransformer
{
    /** The latitude label. */
    private static final String LAT_LABEL = "Lat: ";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MapPointTransformer.class);

    /** The longitude label. */
    private static final String LON_LABEL = "Lon: ";

    /** The cross-hair lines inner point distance from center. */
    private static final int ourInnerDist = 5;

    /** The cross-hair lines outer point distance from center. */
    private static final int ourOuterDist = 25;

    /** The Annotation dots. */
    private final TLongObjectHashMap<AnnotationPointGeometry> myAnnotationDots = new TLongObjectHashMap<>();

    /** The Callout drag change support. */
    private final WeakChangeSupport<CalloutDragListener> myCalloutDragChangeSupport;

    /**
     * The map of call outs to a pair that consist of the tile geometry and the
     * group geometry (Tile and attachment line). Rather than maintain two
     * different maps, uses pair to keep track of tile geometries and allow much
     * easier accessors.
     */
    private final Map<Callout, Pair<TileGeometry, PolylineGeometry>> myCallOuts = New.map();

    /** The list of lines that make up the cross-hair. */
    private final List<PolylineGeometry> myCrossHairLines = New.list();

    /** The tile which is currently being dragged. */
    private TileGeometry myDragTile;

    /** Listen to events from the main viewer to redraw our cross-hair. */
    private final ViewChangeListener myMainViewListener;

    /** The bounding box at the time the mouse drag began. */
    private GeoScreenBoundingBox myMouseDownBox;

    /** Listener for pick changes. */
    private final DefaultPickListener myPickListener = new DefaultPickListener();

    /** The boolean to determine if cross-hairs should be drawn or not. */
    private boolean myShouldDisplayCrossHair;

    /** The location at which the mouse drag began. */
    private Point myTileDragMouseDown;

    /** The tool box. */
    private final Toolbox myToolbox;

    /** Executor to handle view changes. */
    private final ProcrastinatingExecutor myViewChangeExecutor;

    /**
     * Create a call out from the given {@link MutableMapAnnotationPoint}.
     *
     * @param point The map point to use for call out creation.
     * @return The call out.
     */
    public static Callout createCallOut(MutableMapAnnotationPoint point)
    {
        LatLonAlt location;
        if (point.hasAltitude())
        {
            location = LatLonAlt.createFromDegreesMeters(point.getLat(), point.getLon(), point.getAltitude(),
                    Altitude.ReferenceLevel.ELLIPSOID);
        }
        else
        {
            location = LatLonAlt.createFromDegrees(point.getLat(), point.getLon());
        }

        CalloutImpl callOut = new CalloutImpl(point.getId(), createText(point), location, point.getFont());
        callOut.setAnchorOffset(new Vector2i(point.getxOffset(), point.getyOffset()));
        callOut.setBorderColor(point.getColor());
        callOut.setTextColor(point.getFontColor());
        callOut.setBackgroundColor(point.isFilled() ? point.getBackgroundColor() : new Color(0, 0, 0, 0));
        callOut.setCornerRadius(10);

        return callOut;
    }

    /**
     * Create the list of strings representing the options for the given map
     * point.
     *
     * @param point The map point.
     * @return The list of strings.
     */
    private static List<String> createText(MutableMapAnnotationPoint point)
    {
        List<String> textLines = New.list();
        // Need to break this into multiple methods to make checkstyle happy.
        textLines.addAll(createTopHalf(point));
        textLines.addAll(createBottomHalf(point));
        return textLines;
    }

    /**
     * Helper method to create the top half of the list of strings representing
     * the options for the given map point.
     *
     * @param point The map point.
     * @return A list of strings.
     */
    private static List<String> createTopHalf(MutableMapAnnotationPoint point)
    {
        List<String> textLines = New.list();
        boolean showFieldTitles = point.getAnnoSettings().isFieldTitle();
        // Depending on what annotation settings are checked, form our text.
        if (point.getAnnoSettings().isTitle() && !point.getTitle().isEmpty())
        {
            textLines.add(point.getTitle());
        }
        if (point.getAnnoSettings().isDesc() && !point.getDescription().isEmpty())
        {
            textLines.add(StringUtilities.addHTMLLineBreaks(point.getDescription(), 25));
        }
        if (point.getAnnoSettings().isMgrs() && !point.getMGRS().isEmpty())
        {
            textLines.add(format("MGRS: ", point.getMGRS(), showFieldTitles));
        }
        if (point.getAnnoSettings().isDms())
        {
            textLines.add(format(LAT_LABEL, DegreesMinutesSeconds.getShortLabelString(point.getLat(), 16, 2, 'N', 'S'),
                    showFieldTitles));
            textLines.add(format(LON_LABEL, DegreesMinutesSeconds.getShortLabelString(point.getLon(), 16, 2, 'E', 'W'),
                    showFieldTitles));
        }

        return textLines;
    }

    /**
     * Helper method to create the bottom half of the list of strings
     * representing the options for the given map point.
     *
     * @param point The map point.
     * @return A list of strings.
     */
    private static List<String> createBottomHalf(MutableMapAnnotationPoint point)
    {
        List<String> textLines = New.list();
        boolean showFieldTitles = point.getAnnoSettings().isFieldTitle();
        // Depending on what annotation settings are checked, form our text.
        if (point.getAnnoSettings().isLatLon())
        {
            textLines.add(format(LAT_LABEL, String.valueOf(point.getLat()) + LatLonAlt.DEGREE_SYMBOL, showFieldTitles));
            textLines.add(format(LON_LABEL, String.valueOf(point.getLon()) + LatLonAlt.DEGREE_SYMBOL, showFieldTitles));
        }
        if (point.getAnnoSettings().isAltitude())
        {
            textLines.add(format("Altitude: ", point.getAltitude() + " m", showFieldTitles));
        }
        return textLines;
    }

    /**
     * Formats the text.
     *
     * @param title the title
     * @param value the value
     * @param includeTitle whether to include the title
     * @return the text
     */
    private static String format(String title, String value, boolean includeTitle)
    {
        String text = value;
        if (includeTitle)
        {
            text = title + value;
        }
        return text;
    }

    /**
     * Constructor.
     *
     * @param toolbox The tool box.
     * @param executor Executor shared by HUD components.
     */
    public MapPointTransformer(Toolbox toolbox, ScheduledExecutorService executor)
    {
        super(null);
        myToolbox = toolbox;
        myViewChangeExecutor = new ProcrastinatingExecutor(executor);
        myCalloutDragChangeSupport = new WeakChangeSupport<>();
        myMainViewListener = (viewer, type) ->
        {
            if (type == ViewChangeType.WINDOW_RESIZE && myShouldDisplayCrossHair)
            {
                myViewChangeExecutor.execute(MapPointTransformer.this::drawCrossHairs);
            }
        };

        // Register as a listener for view change events
        toolbox.getMapManager().getViewChangeSupport().addViewChangeListener(myMainViewListener);

        if (myToolbox.getControlRegistry() != null)
        {
            ControlContext gluiCtx = myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
            gluiCtx.addPickListener(myPickListener);
        }
    }

    /**
     * Adds the {@link CalloutDragListener}.
     *
     * Note: the listener is held as a weak reference.
     *
     * @param listener the listener
     */
    public void addCalloutDragListener(CalloutDragListener listener)
    {
        myCalloutDragChangeSupport.addListener(listener);
    }

    /**
     * Reset the color of the bubble based on whether the point is selected.
     *
     * @param co The call out for which to change the color.
     */
    public void changeBubbleColor(Callout co)
    {
        Pair<TileGeometry, PolylineGeometry> callPair = myCallOuts.get(co);
        if (callPair != null)
        {
            Color bubbleColor = co.isBorderHighlighted() ? Color.WHITE
                    : co.getBorderColor() == null ? MapPointTransformerHelper.ourBackgroundColor : co.getBorderColor();
            callPair.getSecondObject().getRenderProperties().setColor(bubbleColor);
        }
    }

    @Override
    public void close()
    {
        super.close();
        if (myToolbox.getMapManager() != null)
        {
            myToolbox.getMapManager().getViewChangeSupport().removeViewChangeListener(myMainViewListener);
        }

        if (myToolbox.getControlRegistry() != null)
        {
            ControlContext gluiCtx = myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
            gluiCtx.removePickListener(myPickListener);
        }
    }

    /**
     * Delete the given call out.
     *
     * @param callOut The call out to delete.
     */
    public void deleteCallOut(Callout callOut)
    {
        // Remove from map display
        removeCallOut(callOut);
        // Remove from data structure
        myCallOuts.remove(callOut);
    }

    /**
     * Display the given call out.
     *
     * @param callOut The call out to display.
     * @param date The date of the callout, or null if there isn't one.
     */
    public void displayCallOut(Callout callOut, TimeSpan date)
    {
        Pair<TileGeometry, PolylineGeometry> geomPair;
        if (myCallOuts.containsKey(callOut))
        {
            geomPair = MapPointTransformerHelper.updateGroupGeometry(callOut, myCallOuts.get(callOut).getFirstObject());
        }
        else
        {
            geomPair = MapPointTransformerHelper.createGeometryPair(callOut, date);
        }

        if (geomPair != null)
        {
            myCallOuts.put(callOut, geomPair);
            Collection<Geometry> adds = New.collection();
            adds.add(geomPair.getFirstObject());
            adds.add(geomPair.getSecondObject());
            publishGeometries(adds, Collections.<Geometry>emptySet());
        }
    }

    /**
     * Draw cross hairs in the middle of the screen.
     */
    public void drawCrossHairs()
    {
        myShouldDisplayCrossHair = true;

        List<PolylineGeometry> previousGeoms = New.list();
        previousGeoms.addAll(myCrossHairLines);
        myCrossHairLines.clear();

        int height = myToolbox.getMapManager().getStandardViewer().getViewportHeight();
        int width = myToolbox.getMapManager().getStandardViewer().getViewportWidth();

        myCrossHairLines.add(createWestLine(width, height));
        myCrossHairLines.add(createEastLine(width, height));
        myCrossHairLines.add(createSouthLine(width, height));
        myCrossHairLines.add(createNorthLine(width, height));

        publishGeometries(myCrossHairLines, previousGeoms);
    }

    /**
     * Draw a dot for the specified map point.
     *
     * @param mapPoint The map point to use.
     * @param time The time span
     */
    public void drawDot(MutableMapAnnotationPoint mapPoint, TimeSpan time)
    {
        List<Geometry> previousGeom = New.list();

        if (myAnnotationDots.contains(mapPoint.getId()))
        {
            previousGeom.add(myAnnotationDots.get(mapPoint.getId()));
        }

        PointGeometry.Builder<GeographicPosition> pointBuilder = new PointGeometry.Builder<>();
        pointBuilder.setDataModelId(mapPoint.getId());
        PointRenderProperties props = new DefaultPointRenderProperties(ZOrderRenderProperties.TOP_Z, true, true, true);
        props.setColor(mapPoint.getColor());
        props.setSize(10);

        LatLonAlt location;

        if (mapPoint.hasAltitude())
        {
            location = LatLonAlt.createFromDegreesMeters(mapPoint.getLat(), mapPoint.getLon(), mapPoint.getAltitude(),
                    Altitude.ReferenceLevel.ELLIPSOID);
        }
        else
        {
            location = LatLonAlt.createFromDegrees(mapPoint.getLat(), mapPoint.getLon());
        }

        GeographicPosition anchor = new GeographicPosition(location);
        pointBuilder.setPosition(anchor);

        Constraints constraints = null;
        if (time != null)
        {
            TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(mapPoint.getTime());
            constraints = new Constraints(timeConstraint);
        }
        AnnotationPointGeometry point = new AnnotationPointGeometry(pointBuilder, props, constraints);
        myAnnotationDots.put(mapPoint.getId(), point);
        publishGeometries(Collections.singleton(point), previousGeom);
    }

    /**
     * Handle a mouse dragged event.
     *
     * @param mouseEvent Mouse event.
     */
    public void handleMouseDragged(MouseEvent mouseEvent)
    {
        if (myDragTile != null)
        {
            Entry<Callout, Pair<TileGeometry, PolylineGeometry>> entry = getPairForTile(myDragTile);
            if (entry == null)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Unable to find map point dragged geometry");
                }
                return;
            }

            Collection<Geometry> removes = New.collection(2);
            removes.add(entry.getValue().getFirstObject());
            removes.add(entry.getValue().getSecondObject());

            final Callout draggedCallOut = entry.getKey();

            GeographicBoxAnchor oldAnchor = myMouseDownBox.getAnchor();
            Vector2i oldOffset = oldAnchor.getAnchorOffset() == null ? Vector2i.ORIGIN : oldAnchor.getAnchorOffset();
            final Vector2i attachmentOffset = oldOffset
                    .add(new Vector2i((int)(mouseEvent.getPoint().getX() - myTileDragMouseDown.getX()),
                            (int)(mouseEvent.getPoint().getY() - myTileDragMouseDown.getY())));

            GeographicBoxAnchor anchor = new GeographicBoxAnchor(oldAnchor.getGeographicAnchor(), attachmentOffset,
                    oldAnchor.getHorizontalAlignment(), oldAnchor.getVerticalAlignment());

            GeoScreenBoundingBox gsbb = new GeoScreenBoundingBox(myMouseDownBox.getUpperLeft(), myMouseDownBox.getLowerRight(),
                    anchor);

            TileGeometry newTile = MapPointTransformerHelper.createRepositionedTile(myDragTile, gsbb);

            TimeSpan date = null;
            if (newTile.getConstraints() != null)
            {
                date = newTile.getConstraints().getTimeConstraint().getTimeSpan();
            }

            // Check if border should be highlighted or not.
            PolylineGeometry newLine = MapPointTransformerHelper.createScreenBubble(gsbb, draggedCallOut, date);

            Collection<Geometry> adds = New.collection(2);
            adds.add(newTile);
            adds.add(newLine);
            publishGeometries(adds, removes);
            myDragTile = newTile;

            myCallOuts.put(draggedCallOut, Pair.create(newTile, newLine));

            //@formatter:off
            myCalloutDragChangeSupport.notifyListeners(listener -> listener.callOutDragged(draggedCallOut, attachmentOffset.getX(), attachmentOffset.getY()));
            //@formatter:on
        }
    }

    /**
     * Handle a mouse pressed event.
     *
     * @param mouseEvent Mouse event.
     */
    public void handleMousePressed(MouseEvent mouseEvent)
    {
        PickEvent latestEvent = myPickListener.getLatestEvent();
        if (latestEvent != null && latestEvent.getPickedGeometry() instanceof TileGeometry
                && getPairForTile((TileGeometry)latestEvent.getPickedGeometry()) != null)
        {
            myDragTile = (TileGeometry)latestEvent.getPickedGeometry();
            myTileDragMouseDown = mouseEvent.getPoint();
            myMouseDownBox = (GeoScreenBoundingBox)myDragTile.getBounds();
        }
    }

    /**
     * Handle a mouse released event.
     *
     * @param mouseEvent Mouse event.
     */
    public void handleMouseReleased(MouseEvent mouseEvent)
    {
        myDragTile = null;
        myTileDragMouseDown = null;
        myMouseDownBox = null;
    }

    /**
     * Determine whether a tile I own is currently targeted for mouse events.
     *
     * @return True when I will consume mouse events related to my targeted
     *         tile.
     */
    public boolean isTargeted()
    {
        return myDragTile != null;
    }

    /**
     * Remove the given call out from the map.
     *
     * @param callOut The call out to remove.
     */
    public void removeCallOut(Callout callOut)
    {
        Pair<TileGeometry, PolylineGeometry> callPair = myCallOuts.get(callOut);
        if (callPair != null)
        {
            Collection<Geometry> removes = New.collection(2);
            removes.add(callPair.getFirstObject());
            removes.add(callPair.getSecondObject());
            publishGeometries(Collections.<Geometry>emptySet(), removes);
        }
    }

    /**
     * Removes the {@link CalloutDragListener}.
     *
     * @param listener the listener
     */
    public void removeCalloutDragListener(CalloutDragListener listener)
    {
        myCalloutDragChangeSupport.removeListener(listener);
    }

    /**
     * Remove the cross hairs.
     */
    public void removeCrossHairs()
    {
        myShouldDisplayCrossHair = false;
        publishGeometries(Collections.<Geometry>emptySet(), myCrossHairLines);
    }

    /**
     * Remove the dot.
     *
     * @param id the id of the dot to remove.
     */
    public void removeDot(long id)
    {
        if (myAnnotationDots.contains(id))
        {
            publishGeometries(Collections.<Geometry>emptySet(), Collections.singleton(myAnnotationDots.get(id)));
            myAnnotationDots.remove(id);
        }
    }

    /**
     * Create the east portion of cross hair lines.
     *
     * @param width The viewport width.
     * @param height The viewport height.
     * @return The line.
     */
    private PolylineGeometry createEastLine(int width, int height)
    {
        ScreenPosition eastCenter = new ScreenPosition(width / 2 + ourInnerDist, height / 2);
        ScreenPosition east = new ScreenPosition(width / 2 + ourOuterDist, height / 2);

        List<ScreenPosition> eastPoints = New.list(2);
        eastPoints.add(eastCenter);
        eastPoints.add(east);

        PolylineGeometry.Builder<ScreenPosition> eastLineBuilder = new PolylineGeometry.Builder<>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
        props.setColor(Color.CYAN);
        props.setWidth(1);
        eastLineBuilder.setVertices(eastPoints);
        PolylineGeometry eastLine = new PolylineGeometry(eastLineBuilder, props, null);

        return eastLine;
    }

    /**
     * Create the north portion of cross hair lines.
     *
     * @param width The viewport width.
     * @param height The viewport height.
     * @return The line.
     */
    private PolylineGeometry createNorthLine(int width, int height)
    {
        ScreenPosition northCenter = new ScreenPosition(width / 2, height / 2 + ourInnerDist);
        ScreenPosition north = new ScreenPosition(width / 2, height / 2 + ourOuterDist);

        List<ScreenPosition> northPoints = New.list(2);
        northPoints.add(northCenter);
        northPoints.add(north);

        PolylineGeometry.Builder<ScreenPosition> northLineBuilder = new PolylineGeometry.Builder<>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
        props.setColor(Color.CYAN);
        props.setWidth(1);
        northLineBuilder.setVertices(northPoints);
        PolylineGeometry northLine = new PolylineGeometry(northLineBuilder, props, null);

        return northLine;
    }

    /**
     * Create the south portion of cross hair lines.
     *
     * @param width The viewport width.
     * @param height The viewport height.
     * @return The line.
     */
    private PolylineGeometry createSouthLine(int width, int height)
    {
        ScreenPosition south = new ScreenPosition(width / 2, height / 2 - ourOuterDist);
        ScreenPosition southCenter = new ScreenPosition(width / 2, height / 2 - ourInnerDist);

        List<ScreenPosition> southPoints = New.list(2);
        southPoints.add(south);
        southPoints.add(southCenter);

        PolylineGeometry.Builder<ScreenPosition> southLineBuilder = new PolylineGeometry.Builder<>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
        props.setColor(Color.CYAN);
        props.setWidth(1);
        southLineBuilder.setVertices(southPoints);
        PolylineGeometry southLine = new PolylineGeometry(southLineBuilder, props, null);

        return southLine;
    }

    /**
     * Create the west portion of cross hair lines.
     *
     * @param width The viewport width.
     * @param height The viewport height.
     * @return The line.
     */
    private PolylineGeometry createWestLine(int width, int height)
    {
        ScreenPosition west = new ScreenPosition(width / 2 - ourOuterDist, height / 2);
        ScreenPosition westCenter = new ScreenPosition(width / 2 - ourInnerDist, height / 2);

        List<ScreenPosition> westPoints = New.list(2);
        westPoints.add(west);
        westPoints.add(westCenter);

        PolylineGeometry.Builder<ScreenPosition> westLineBuilder = new PolylineGeometry.Builder<>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
        props.setColor(Color.CYAN);
        props.setWidth(1);
        westLineBuilder.setVertices(westPoints);
        PolylineGeometry westLine = new PolylineGeometry(westLineBuilder, props, null);

        return westLine;
    }

    /**
     * Helper method to find the map entry corresponding to the given tile
     * geometry.
     *
     * @param tile The tile geometry to look for.
     * @return The call out to group geometry map entry.
     */
    private Entry<Callout, Pair<TileGeometry, PolylineGeometry>> getPairForTile(TileGeometry tile)
    {
        for (Entry<Callout, Pair<TileGeometry, PolylineGeometry>> entry : myCallOuts.entrySet())
        {
            if (entry.getValue().getFirstObject().equals(tile))
            {
                return entry;
            }
        }
        return null;
    }
}
