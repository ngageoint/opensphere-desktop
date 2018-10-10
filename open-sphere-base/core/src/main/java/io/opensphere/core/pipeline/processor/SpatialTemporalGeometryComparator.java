package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.ActiveTimeSpanChangeListener;
import io.opensphere.core.TimeManager.ActiveTimeSpans;
import io.opensphere.core.TimeManager.Fade;
import io.opensphere.core.geometry.ConstrainableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Comparator that orders geometries in the following manner:
 * <ol>
 * <li>If one geometry has a time constraint and the other does not, put the one
 * with the time constraint second.</li>
 * <li>If both geometries have time constraints and one time constraint is
 * closer to the current time, put that one first.</li>
 * <li>If one geometry has geographic positions and the other does not, put the
 * one with geographic positions second.</li>
 * <li>If both geometries have geographic positions and one is closer to the
 * center of the view, put that one first.</li>
 * </ol>
 */
public class SpatialTemporalGeometryComparator implements Comparator<Geometry>
{
    /** The center time. */
    private volatile TimeSpan myCenterTime;

    /** The map context. */
    private final MapContext<?> myMapContext;

    /**
     * The point around which to order the geometries, positions closer to the
     * center will be given higher precedence.
     */
    private volatile Vector3d myModelCenter;

    /** The listener for time changes. */
    private final ActiveTimeSpanChangeListener myTimeListener = active -> myCenterTime = TimeSpan
            .get(active.getPrimary().getExtent().getMidpoint());

    /** The listener for view changes. */
    private final ViewChangeListener myViewChangeListener = new ViewChangeListener()
    {
        @Override
        public void viewChanged(Viewer viewer, ViewChangeType type)
        {
            setModelCenter(viewer);
        }
    };

    /**
     * Constructor.
     *
     * @param mapContext The map context.
     * @param timeManager The time manager.
     */
    public SpatialTemporalGeometryComparator(MapContext<?> mapContext, final TimeManager timeManager)
    {
        myMapContext = mapContext;
        myMapContext.getViewChangeSupport().addViewChangeListener(myViewChangeListener);
        timeManager.addActiveTimeSpanChangeListener(myTimeListener);
        myTimeListener.activeTimeSpansChanged(new ActiveTimeSpans()
        {
            @Override
            public int getDirection()
            {
                return 0;
            }

            @Override
            public Fade getFade()
            {
                return timeManager.getFade();
            }

            @Override
            public TimeSpanList getPrimary()
            {
                return timeManager.getPrimaryActiveTimeSpans();
            }

            @Override
            public Map<Object, Collection<? extends TimeSpan>> getSecondary()
            {
                return timeManager.getSecondaryActiveTimeSpans();
            }
        });
    }

    @Override
    public int compare(Geometry o1, Geometry o2)
    {
        int result = compareByTime(o1, o2);

        if (result == 0)
        {
            result = compareBySpace(o1, o2);
        }

        return result;
    }

    /**
     * Compare two geometries by space.
     *
     * @param o1 The first geometry.
     * @param o2 The second geometry.
     * @return o1 &lt; o2 ? -1 : o1 &gt; o2 ? 1 : 0
     */
    protected int compareBySpace(Geometry o1, Geometry o2)
    {
        Position refPt1 = o1.getReferencePoint();
        GeographicPosition pos1 = refPt1 instanceof GeographicPosition ? (GeographicPosition)refPt1 : null;
        Position refPt2 = o2.getReferencePoint();
        GeographicPosition pos2 = refPt2 instanceof GeographicPosition ? (GeographicPosition)refPt2 : null;

        if (pos1 == null)
        {
            return pos2 == null ? 0 : -1;
        }
        else if (pos2 == null)
        {
            return 1;
        }
        else
        {
            int result;
            // Convert to sea-level if necessary to avoid computing terrain
            // intersections.
            LatLonAlt lla1 = pos1.getLatLonAlt();
            if (lla1.getAltitudeReference() == Altitude.ReferenceLevel.TERRAIN)
            {
                pos1 = new GeographicPosition(
                        LatLonAlt.createFromDegrees(lla1.getLatD(), lla1.getLonD(), Altitude.ReferenceLevel.ELLIPSOID));
            }
            Vector3d model1 = myMapContext.getProjection().convertToModel(pos1, Vector3d.ORIGIN);

            LatLonAlt lla2 = pos2.getLatLonAlt();
            if (lla2.getAltitudeReference() == Altitude.ReferenceLevel.TERRAIN)
            {
                pos2 = new GeographicPosition(
                        LatLonAlt.createFromDegrees(lla2.getLatD(), lla2.getLonD(), Altitude.ReferenceLevel.ELLIPSOID));
            }
            Vector3d model2 = myMapContext.getProjection().convertToModel(pos2, Vector3d.ORIGIN);

            Vector3d centerPoint = getModelCenter();
            double dist1 = model1.subtract(centerPoint).getLength();
            double dist2 = model2.subtract(centerPoint).getLength();
            result = dist1 < dist2 ? -1 : dist1 == dist2 ? 0 : 1;
            return result;
        }
    }

    /**
     * Compare two geometries by time.
     *
     * @param o1 The first geometry.
     * @param o2 The second geometry.
     * @return o1 &lt; o2 ? -1 : o1 &gt; o2 ? 1 : 0
     */
    protected int compareByTime(Geometry o1, Geometry o2)
    {
        int result;
        TimeConstraint tc1 = o1 instanceof ConstrainableGeometry ? ((ConstrainableGeometry)o1).getConstraints() == null ? null
                : ((ConstrainableGeometry)o1).getConstraints().getTimeConstraint() : null;
        TimeConstraint tc2 = o2 instanceof ConstrainableGeometry ? ((ConstrainableGeometry)o2).getConstraints() == null ? null
                : ((ConstrainableGeometry)o2).getConstraints().getTimeConstraint() : null;
        if (tc1 == null)
        {
            result = tc2 == null ? 0 : -1;
        }
        else if (tc2 == null)
        {
            result = 1;
        }
        else
        {
            Duration delta = tc1.getTimeSpan().getGapBetween(myCenterTime)
                    .subtract(tc2.getTimeSpan().getGapBetween(myCenterTime));
            result = delta.compareTo(Seconds.ZERO);
        }
        return result;
    }

    /**
     * Get the center used for ordering, positions closer to the center will be
     * given higher precedence.
     *
     * @return The center point.
     */
    protected Vector3d getModelCenter()
    {
        if (myModelCenter == null)
        {
            setModelCenter(myMapContext.getStandardViewer());
        }
        return myModelCenter;
    }

    /**
     * Set the model center based on the viewer. In general, this might be the
     * center of the view. When the center of the view does not intersect the
     * model, the nearest model position position will be used.
     *
     * @param viewer The viewer which determines the center.
     */
    private void setModelCenter(Viewer viewer)
    {
        myModelCenter = viewer.getModelIntersection();
        if (myModelCenter == null)
        {
            myModelCenter = viewer.getClosestModelPosition();
        }
    }
}
