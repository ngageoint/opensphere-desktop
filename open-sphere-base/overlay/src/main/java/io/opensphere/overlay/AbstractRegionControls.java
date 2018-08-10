package io.opensphere.overlay;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.MapManager;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.control.BoundEventListener;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.viewer.impl.DynamicViewer;

/**
 * Abstract base class containing common functionality for region controls.
 */
public abstract class AbstractRegionControls
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractRegionControls.class);

    /**
     * The latitude (positive or negative) at which locking to the pole is
     * possible.
     */
    private static final double POLE_LIMIT = 85.;

    /**
     * A saved longitude used to determine which way around the earth the box is
     * going.
     */
    private volatile Double myBoundaryLon;

    /** Listeners for the control context. */
    private final Collection<BoundEventListener> myControlContextListeners = New.collection();

    /** Flag indicating if the region is locked to cover the pole. */
    private volatile boolean myLockToPole;

    /** The system map manager. */
    private final MapManager myMapManager;

    /**
     * The saved positions.
     */
    private final List<LatLonAlt> myPositions = New.list();

    /** Executor for handling query region events. */
    private final ProcrastinatingExecutor myQueryRegionExecutor;

    /**
     * The transformer used to display geometries.
     */
    private final SelectionRegionTransformer myTransformer;

    /** The system units registry. */
    private final UnitsRegistry myUnitsRegistry;

    /**
     * Constructor.
     *
     * @param mapManager The system map manager.
     * @param unitsRegistry The system units registry.
     * @param transformer The transformer used to generate geometries.
     */
    public AbstractRegionControls(MapManager mapManager, UnitsRegistry unitsRegistry, SelectionRegionTransformer transformer)
    {
        myMapManager = mapManager;
        myUnitsRegistry = unitsRegistry;
        myTransformer = transformer;

        ScheduledExecutorService schedExec = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("Overlay Plugin"),
                SuppressableRejectedExecutionHandler.getInstance());
        myQueryRegionExecutor = new ProcrastinatingExecutor(schedExec);
    }

    /**
     * Clean up my registered listeners.
     *
     * @param uiRegistry the ui registry
     * @param controlRegistry The control registry.
     */
    public void close(UIRegistry uiRegistry, ControlRegistry controlRegistry)
    {
        ControlContext controlContext = controlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        controlContext.removeListeners(myControlContextListeners);
    }

    /**
     * Add a control context listener to the collection to be cleaned up when
     * {@link #close(UIRegistry, ControlRegistry)} is called.
     *
     * @param listener The listener.
     */
    protected final void addControlContextListener(BoundEventListener listener)
    {
        myControlContextListeners.add(listener);
    }

    /**
     * Add the end point to the list of positions and return a new list.
     *
     * @param positions The positions.
     * @param endPoint The end point.
     * @return The positions plus the end point.
     */
    protected List<LatLonAlt> addEndPoint(List<? extends LatLonAlt> positions, Point endPoint)
    {
        List<LatLonAlt> llas = new ArrayList<>(positions.size() + 1);
        llas.addAll(positions);

        if (endPoint != null)
        {
            GeographicPosition position = getMapManager().convertToPosition(new Vector2i(endPoint), ReferenceLevel.ELLIPSOID);

            if (position == null)
            {
                return null;
            }
            llas.add(position.getLatLonAlt());
        }
        return llas;
    }

    /**
     * Add a point to the region.
     *
     * @param point The point to add.
     */
    protected void addPosition(Point point)
    {
        GeographicPosition position = getMapManager().convertToPosition(new Vector2i(point), ReferenceLevel.ELLIPSOID);
        if (position != null)
        {
            LatLonAlt latLonAlt = position.getLatLonAlt();
            if (myPositions.isEmpty() || !myPositions.get(myPositions.size() - 1).equals(latLonAlt))
            {
                myPositions.add(latLonAlt);
            }
        }
    }

    /**
     * Completes drawing the current line.
     */
    protected void finishLine()
    {
        /* intentionally blank */
    }

    /**
     * Finish the region.
     */
    protected void finishRegion()
    {
        myBoundaryLon = null;
        myLockToPole = false;
        myPositions.clear();
    }

    /**
     * Get the bounding box defined by the given begin and end coordinates.
     *
     * @param begin The begin coordinates.
     * @param end The end coordinates.
     * @return The bounding box.
     */
    protected GeographicBoundingBox getBoundingBox(LatLonAlt begin, LatLonAlt end)
    {
        double beginLon = begin.getLonD();
        double endLon = end.getLonD();
        double beginToEnd = LatLonAlt.longitudeDifference(beginLon, endLon);
        if (Math.abs(end.getLatD()) <= Math.abs(begin.getLatD()))
        {
            myLockToPole = false;
        }
        else if (Math.abs(end.getLatD()) > POLE_LIMIT && beginToEnd > 90.)
        {
            myLockToPole = true;
        }

        double bottom;
        double top;
        if (begin.getLatD() < end.getLatD())
        {
            bottom = begin.getLatD();
            top = myLockToPole ? 90. : end.getLatD();
        }
        else
        {
            bottom = myLockToPole ? -90. : end.getLatD();
            top = begin.getLatD();
        }

        // If the box is more than 90 degrees wide, save the boundary point.
        if (myBoundaryLon == null)
        {
            if (beginToEnd > 90.)
            {
                myBoundaryLon = Double.valueOf(endLon);
            }
        }

        // If the bounding box is now less than 90 degrees wide and the
        // end point is on the same side of the begin point as the saved
        // longitude, clear the saved longitude.
        else if (beginToEnd < LatLonAlt.longitudeDifference(beginLon, myBoundaryLon.doubleValue())
                && LatLonAlt.longitudeDifference(endLon, myBoundaryLon.doubleValue()) <= 90.)
        {
            myBoundaryLon = null;
        }

        double left;
        double right;
        if (myLockToPole)
        {
            left = -180.;
            right = 180.;
        }
        else
        {
            double[] leftAndRight = determineLeftAndRight(beginLon, endLon);

            left = leftAndRight[0];
            right = leftAndRight[1];
        }

        GeographicBoundingBox bbox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(bottom, left),
                LatLonAlt.createFromDegrees(top, right));
        return bbox;
    }

    /**
     * Get the JTS bounding box geometry defined by the given begin and end
     * coordinates.
     *
     * @param begin The begin coordinates.
     * @param end The end coordinates.
     * @return The JTS geometry.
     */
    protected Polygon getBoundingBoxGeometry(LatLonAlt begin, LatLonAlt end)
    {
        GeographicBoundingBox bbox = getBoundingBox(begin, end);

        LatLonAlt lowerLeft = bbox.getLowerLeft().getLatLonAlt();
        LatLonAlt upperRight = bbox.getUpperRight().getLatLonAlt();
        Polygon geom = JTSUtilities.createPolygon(lowerLeft.getLonD(), upperRight.getLonD(), lowerLeft.getLatD(),
                upperRight.getLatD(), new GeometryFactory());
        return geom;
    }

    /**
     * Get the color for the box.
     *
     * @return The color.
     */
    protected abstract Color getColor();

    /**
     * Get the system map manager.
     *
     * @return The map manager.
     */
    protected final MapManager getMapManager()
    {
        return myMapManager;
    }

    /**
     * Get the current positions in the region.
     *
     * @return The positions.
     */
    protected List<? extends LatLonAlt> getPositions()
    {
        return myPositions;
    }

    /**
     * Get the queryRegionExecutor.
     *
     * @return the queryRegionExecutor
     */
    protected ProcrastinatingExecutor getQueryRegionExecutor()
    {
        return myQueryRegionExecutor;
    }

    /**
     * Get the standard viewer for the map.
     *
     * @return The viewer.
     */
    protected final DynamicViewer getStandardViewer()
    {
        return getMapManager().getStandardViewer();
    }

    /**
     * Get the transformer.
     *
     * @return The transformer.
     */
    protected SelectionRegionTransformer getTransformer()
    {
        return myTransformer;
    }

    /**
     * Get the units registry.
     *
     * @return The units registry.
     */
    protected final UnitsRegistry getUnitsRegistry()
    {
        return myUnitsRegistry;
    }

    /**
     * Set the region to be a bounding box.
     *
     * @param begin One corner of the box.
     * @param end The opposite corner of the box.
     */
    protected void setBoundingBoxRegion(LatLonAlt begin, LatLonAlt end)
    {
        getTransformer().setRegion(getBoundingBoxGeometry(begin, end), getColor(), null, null, null);
    }

    /**
     * Set the zoom box in the transformer based on the beginning and ending
     * mouse points.
     *
     * @param positions The positions.
     * @param endPoint The end point.
     */
    protected synchronized void setSelectionBox(List<? extends LatLonAlt> positions, MouseEvent endPoint)
    {
        if (myPositions.isEmpty())
        {
            return;
        }
        List<LatLonAlt> llas = addEndPoint(positions, endPoint.getPoint());
        if (llas != null)
        {
            setBoundingBoxRegion(llas.get(0), llas.get(1));
        }
    }

    /**
     * Determine which longitude is on the left of the box and which longitude
     * is on the right, based on the current value of {@link #myBoundaryLon}.
     *
     * @param beginLon The beginning longitude.
     * @param endLon The ending longitude.
     * @return An array containing the left value at index 0 and the right value
     *         at index 1.
     */
    private double[] determineLeftAndRight(double beginLon, double endLon)
    {
        double[] vals;
        boolean beginAtLeft;
        if (beginLon < endLon ^ Math.abs(endLon - beginLon) > 180.)
        {
            beginAtLeft = myBoundaryLon == null || myBoundaryLon.doubleValue() > beginLon ^ myBoundaryLon.doubleValue() > endLon
                    ^ Math.abs(endLon - beginLon) > 180.;
        }
        else
        {
            beginAtLeft = myBoundaryLon != null && myBoundaryLon.doubleValue() > beginLon ^ myBoundaryLon.doubleValue() >= endLon
                    ^ Math.abs(endLon - beginLon) <= 180.;
        }

        if (beginAtLeft)
        {
            vals = new double[] { beginLon, endLon };
        }
        else
        {
            vals = new double[] { endLon, beginLon };
        }
        return vals;
    }
}
