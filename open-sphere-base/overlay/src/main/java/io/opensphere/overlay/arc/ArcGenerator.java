package io.opensphere.overlay.arc;

import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.callout.Callout;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultPickListener;
import io.opensphere.core.control.PickListener.PickEvent;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeographicPositionsContextKey;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.GeoScreenBubbleGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.callout.CalloutGeometryUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** Dynamically generate arcs and their associated labels. */
public class ArcGenerator
{
    /** The manager for actions associated with completed arcs. */
    private final ContextActionManager myActionManager;

    /**
     * When true, this generator has been shutdown and will not generate arcs.
     */
    private boolean myClosed;

    /** The current arc which has not been terminated. */
    private Arc myCurrentArc;

    /** The drag callout geometries for the current arc. */
    private Collection<? extends Geometry> myCurrentDragCalloutGeometries;

    /**
     * The fixed callout geometries for the current arc (not the drag callout
     * geometries).
     */
    private final Collection<Geometry> myFixedCalloutGeometries = New.collection();

    /** Listener for pick changes. */
    private final DefaultPickListener myPickListener = new DefaultPickListener();

    /** The tool box used by plugins to interact with the rest of the system. */
    private final Toolbox myToolbox;

    /** The transformer for publishing my associated geometries. */
    private final Transformer myTransformer;

    /**
     * Constructor.
     *
     * @param transformer The transformer for publishing my associated
     *            geometries.
     * @param toolbox The tool box used by plugins to interact with the rest of
     *            the system.
     */
    public ArcGenerator(Transformer transformer, Toolbox toolbox)
    {
        myTransformer = transformer;
        myToolbox = toolbox;
        if (myToolbox.getUIRegistry() != null)
        {
            myActionManager = myToolbox.getUIRegistry().getContextActionManager();
        }
        else
        {
            myActionManager = null;
        }

        if (myToolbox.getControlRegistry() != null)
        {
            ControlContext gluiCtx = myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
            gluiCtx.addPickListener(myPickListener);
        }
    }

    /**
     * Tell whether an arc is currently being drawn.
     *
     * @return true when an arc is currently being drawn.
     */
    public boolean arcInProgress()
    {
        return myCurrentArc != null;
    }

    /**
     * Remove the current arc and un-publish any geometries associated with the
     * partially completed arc.
     */
    public void clearCurrentArc()
    {
        if (myCurrentArc != null)
        {
            Collection<Geometry> removes = New.collection();
            removes.addAll(myCurrentArc.getAllGeometries());
            if (myCurrentDragCalloutGeometries != null)
            {
                removes.addAll(myCurrentDragCalloutGeometries);
                myCurrentDragCalloutGeometries = null;
            }
            removes.addAll(myFixedCalloutGeometries);
            myFixedCalloutGeometries.clear();
            myTransformer.publishGeometries(Collections.<Geometry>emptyList(), removes);
        }
        myCurrentArc = null;
    }

    /** Cleanup and shutdown this arc generator. */
    public void close()
    {
        myClosed = true;
        clearCurrentArc();
        if (myToolbox.getControlRegistry() != null)
        {
            ControlContext gluiCtx = myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
            gluiCtx.removePickListener(myPickListener);
        }
    }

    /** Cancel any arc which is currently in progress. */
    public void handleCancellation()
    {
        if (activeArcInProgress())
        {
            clearCurrentArc();
        }
    }

    /**
     * Complete the arc and publish it to the context manager. All geometries
     * associated with the arc will be removed and it will be the responsibility
     * of the action provider for the context to publish any desired geometries.
     */
    public void handleCompletion()
    {
        if (activeArcInProgress())
        {
            // Exclude the last position and remove segments which have no
            // length
            List<Pair<GeographicPosition, AbstractRenderableGeometry>> vertices = myCurrentArc.getVertices();
            List<Pair<GeographicPosition, AbstractRenderableGeometry>> filteredVertices = New.list(vertices.size());
            GeographicPosition lastGeo = null;
            for (int i = 0; i < vertices.size() - 1; ++i)
            {
                Pair<GeographicPosition, AbstractRenderableGeometry> vertex = vertices.get(i);
                if (!vertex.getFirstObject().equals(lastGeo))
                {
                    filteredVertices.add(vertex);
                    lastGeo = vertex.getFirstObject();
                }
            }

            clearCurrentArc();

            if (!filteredVertices.isEmpty())
            {
                final GeographicPositionsContextKey key = new GeographicPositionsContextKey(filteredVertices);
                EventQueueUtilities.runOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        myActionManager.getActionContext(ContextIdentifiers.ARC_CONTEXT, GeographicPositionsContextKey.class)
                                .doAction(key, null, 0, 0, null);
                    }
                });
            }
        }
    }

    /**
     * Handle a mouse moved event. If a line is currently being drawn, this will
     * show where the current line lies.
     *
     * @param mouseEvent Mouse event.
     */
    public void handleMouseMoved(MouseEvent mouseEvent)
    {
        if (activeArcInProgress())
        {
            Pair<GeographicPosition, AbstractRenderableGeometry> position = determinePosition(mouseEvent);
            if (position.getFirstObject() != null)
            {
                Collection<Geometry> removes = New.collection();
                removes.add(myCurrentArc.getLine());
                if (myCurrentDragCalloutGeometries != null)
                {
                    removes.addAll(myCurrentDragCalloutGeometries);
                }
                myCurrentArc = new Arc(myCurrentArc, position, true, false);
                myCurrentDragCalloutGeometries = createCalloutGeometries(myCurrentArc.createLastSegmentCallout());
                Collection<Geometry> adds = New.collection();
                adds.add(myCurrentArc.getLine());
                adds.addAll(myCurrentDragCalloutGeometries);

                myTransformer.publishGeometries(adds, removes);
            }
        }
    }

    /**
     * Create a new segment for an in progress arc or start a new arc if none is
     * in progress.
     *
     * @param mouseEvent Mouse event.
     */
    public void handleNewSegment(MouseEvent mouseEvent)
    {
        if (isActive())
        {
            Pair<GeographicPosition, AbstractRenderableGeometry> position = determinePosition(mouseEvent);
            if (position.getFirstObject() != null)
            {
                if (myCurrentArc == null)
                {
                    List<Pair<GeographicPosition, AbstractRenderableGeometry>> positions = New.list(2);
                    positions.add(position);
                    positions.add(position);
                    myCurrentArc = new Arc(positions, getPreferredUnits(), false);
                    myTransformer.publishGeometries(myCurrentArc.getAllGeometries(), Collections.<Geometry>emptyList());
                }
                else
                {
                    // if the last segment is zero length, do not add another
                    // zero length segment
                    List<Pair<GeographicPosition, AbstractRenderableGeometry>> vertices = myCurrentArc.getVertices();
                    if (vertices.get(vertices.size() - 1).equals(vertices.get(vertices.size() - 2)))
                    {
                        return;
                    }

                    Collection<Geometry> adds = New.collection();
                    Collection<Geometry> removes = New.collection();

                    if (myCurrentDragCalloutGeometries != null)
                    {
                        removes.add(myCurrentArc.getLine());
                        removes.addAll(myCurrentDragCalloutGeometries);
                    }

                    // remove the last point and replace it with the current one
                    // to make sure we get the picked geometry if there is one.
                    myCurrentArc = new Arc(myCurrentArc, position, true, false);
                    adds.addAll(createCalloutGeometries(myCurrentArc.createLastSegmentCallout()));
                    myFixedCalloutGeometries.addAll(adds);

                    // now add the new point
                    myCurrentArc = new Arc(myCurrentArc, position, false, false);
                    myCurrentDragCalloutGeometries = createCalloutGeometries(myCurrentArc.createLastSegmentCallout());
                    adds.addAll(myCurrentDragCalloutGeometries);
                    adds.add(myCurrentArc.getLine());

                    myTransformer.publishGeometries(adds, removes);
                }
            }
        }
    }

    /**
     * Return true when drawing new arcs is allowed.
     *
     * @return true when drawing new arcs is allowed.
     */
    public boolean isActive()
    {
        return !myClosed
                && myActionManager.getActionContext(ContextIdentifiers.ARC_CONTEXT, GeographicPositionsContextKey.class).isUsed();
    }

    /**
     * Create geometries for a callout.
     *
     * @param callout The callout.
     * @return The geometries.
     */
    protected Collection<? extends Geometry> createCalloutGeometries(Callout callout)
    {
        TileGeometry tile = CalloutGeometryUtil.createCalloutTile(callout);
        GeoScreenBoundingBox gsbb = (GeoScreenBoundingBox)tile.getBounds();
        GeoScreenBubbleGeometry line = CalloutGeometryUtil.createTextBubble(gsbb, callout);

        return Arrays.<Geometry>asList(tile, line);
    }

    /**
     * Determines if an arc is actively being drawn.
     *
     * @return if an arc is actively being drawn
     */
    private boolean activeArcInProgress()
    {
        return arcInProgress() && isActive();
    }

    /**
     * Determine the geographic position to use based on the cursor position.
     * When applicable, the position may "snap to" a picked geometry.
     *
     * @param mouseEvent The mouse event which has occurred.
     * @return The geographic position to use for the current cursor position
     */
    private Pair<GeographicPosition, AbstractRenderableGeometry> determinePosition(MouseEvent mouseEvent)
    {
        PickEvent lastPickEvent = myPickListener.getLatestEvent();
        PointGeometry pickedPoint = null;
        if (lastPickEvent != null && lastPickEvent.getPickedGeometry() instanceof PointGeometry)
        {
            pickedPoint = (PointGeometry)lastPickEvent.getPickedGeometry();
        }

        GeographicPosition position;
        if (pickedPoint != null && GeographicPosition.class.isAssignableFrom(pickedPoint.getPositionType()))
        {
            position = (GeographicPosition)pickedPoint.getPosition();
        }
        else
        {
            position = myToolbox.getMapManager().convertToPosition(new Vector2i(mouseEvent.getPoint()), ReferenceLevel.TERRAIN);
        }

        return new Pair<GeographicPosition, AbstractRenderableGeometry>(position, pickedPoint);
    }

    /**
     * Get the currently preferred units.
     *
     * @return The units.
     */
    private Class<? extends Length> getPreferredUnits()
    {
        return myToolbox.getUnitsRegistry().getPreferredUnits(Length.class);
    }
}
