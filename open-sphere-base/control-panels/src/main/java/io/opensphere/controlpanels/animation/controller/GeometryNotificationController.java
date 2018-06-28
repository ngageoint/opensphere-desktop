package io.opensphere.controlpanels.animation.controller;

import java.util.Collection;
import java.util.function.Predicate;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.geometry.ConstrainableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;

/**
 * Notifies the user when inactive geometries are added. An inactive geometry is
 * one that is not shown on the map because its time constraints place it
 * outside of the primary and secondary time spans.
 */
@ThreadSafe
class GeometryNotificationController implements Service
{
    /** The minimum time to wait between user notifications. */
    private static final long NOTIFICATION_FREQUENCY_MS = 600000;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** Geometry registry subscriber. */
    private final GenericSubscriber<Geometry> myGeometrySubscriber;

    /** The "active" time spans. */
    @GuardedBy("this")
    private final Collection<TimeSpan> myTimeSpans = New.set();

    /** The predicate. */
    @GuardedBy("this")
    private final InactiveGeometryPredicate myPredicate = new InactiveGeometryPredicate();

    /** The last user notification time (milliseconds). */
    @GuardedBy("this")
    private long myLastNotificationTime;

    /** Whether to show the toast message. */
    @GuardedBy("this")
    private boolean myShowToast = true;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public GeometryNotificationController(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myGeometrySubscriber = new GenericSubscriber<Geometry>()
        {
            @Override
            public void receiveObjects(Object source, Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
            {
                handleAddedGeometries(adds);
            }
        };
    }

    @Override
    public void open()
    {
        myToolbox.getGeometryRegistry().addSubscriber(myGeometrySubscriber);
    }

    @Override
    public void close()
    {
        myToolbox.getGeometryRegistry().removeSubscriber(myGeometrySubscriber);
    }

    /**
     * Handles added geometries.
     *
     * @param adds the added geometries
     */
    private synchronized void handleAddedGeometries(Collection<? extends Geometry> adds)
    {
        updateTimeSpans();

        boolean inactiveGeometriesAdded = adds.stream().anyMatch(myPredicate);

        long now = System.currentTimeMillis();
        if (inactiveGeometriesAdded && now - myLastNotificationTime > NOTIFICATION_FREQUENCY_MS)
        {
            UserMessageEvent.info(myToolbox.getEventManager(), "Data has been added outside of the active time.", false, this,
                    myShowToast);
            myLastNotificationTime = now;
            myShowToast = false;
        }
    }

    /**
     * Updates the set of "active" time spans.
     */
    private void updateTimeSpans()
    {
        myTimeSpans.clear();

        AnimationPlan currentPlan = myToolbox.getAnimationManager().getCurrentPlan();
        if (currentPlan != null)
        {
            myTimeSpans.addAll(currentPlan.getTimeCoverage());
        }
        else
        {
            myTimeSpans.addAll(myToolbox.getTimeManager().getPrimaryActiveTimeSpans());
        }

        Collection<? extends TimeSpan> secondarySpans = myToolbox.getTimeManager()
                .getSecondaryActiveTimeSpans(TimeManager.WILDCARD_CONSTRAINT_KEY);
        if (secondarySpans != null)
        {
            myTimeSpans.addAll(secondarySpans);
        }
    }

    /**
     * Predicate that determines if the given geometry is inactive.
     */
    private class InactiveGeometryPredicate implements Predicate<Geometry>
    {
        @Override
        public boolean test(Geometry geom)
        {
            boolean isInactive = false;
            if (geom instanceof ConstrainableGeometry)
            {
                Constraints constraints = ((ConstrainableGeometry)geom).getConstraints();
                isInactive = constraints != null && constraints.getTimeConstraint() != null
                        && !constraints.getTimeConstraint().check(myTimeSpans);
            }
            return isInactive;
        }
    }
}
