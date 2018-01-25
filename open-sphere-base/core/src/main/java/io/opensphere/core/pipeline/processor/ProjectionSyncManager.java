package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * For processors which are sensitive to projection changes, ensure that all
 * processors are rendering the same projection at any given time.
 */
public class ProjectionSyncManager
{
    /** The projection which is being rendered by the the active renderers. */
    private Projection myActiveProjection;

    /**
     * The projection which is about to become active and the processors which
     * are participating in the projection change. Whenever this is non-null,
     * the processors have reported that the are ready and if projection
     * switching is not locked, the projection will become active immediately.
     * If projection switching is locked the pending projection will be made
     * active immediately upon unlock.
     */
    private Pair<Projection, Set<GeometryProcessor<? extends Geometry>>> myPendingProjection;

    /** The projection which has the latest time stamp. */
    private Projection myNewestProjection;

    /**
     * The processors which are currently participating in projection
     * synchronization.
     */
    private final Set<GeometryProcessor<? extends Geometry>> myProcessors = New.set();

    /**
     * When locked, do not allow the current projection to be changed to a new
     * projection.
     */
    private boolean myProjectionLocked;

    /**
     * A map of projections to processors which are ready to render that
     * projection.
     */
    private final Map<Projection, Set<GeometryProcessor<? extends Geometry>>> myProjectionSnapshots = New.map();

    /**
     * Check to see if this projection is the newest projection.
     *
     * @param projectionSnapshot the projection which might be current.
     * @return true when the projection is the newest projection.
     */
    public synchronized boolean isProjectionCurrent(Projection projectionSnapshot)
    {
        if (projectionSnapshot == null || myNewestProjection == null)
        {
            return true;
        }
        return Utilities.sameInstance(projectionSnapshot, myNewestProjection);
    }

    /**
     * Do not allow the current projection to be changed to a new projection.
     *
     * @return the projection which will remain active until unlocking.
     */
    public synchronized Projection lockProjection()
    {
        myProjectionLocked = true;
        return myActiveProjection;
    }

    /**
     * Set the processors which will participate in projection synchronization.
     *
     * @param processors the processors which will participate in projection
     *            synchronization.
     */
    public synchronized void setProcessors(Collection<GeometryProcessor<? extends Geometry>> processors)
    {
        myProcessors.clear();

        // Only add processors which are sensitive to projection changes.
        for (GeometryProcessor<? extends Geometry> proc : processors)
        {
            if (proc.sensitiveToProjectionChanges())
            {
                myProcessors.add(proc);
            }
        }

        for (Set<GeometryProcessor<? extends Geometry>> ready : myProjectionSnapshots.values())
        {
            Iterator<GeometryProcessor<? extends Geometry>> iter = ready.iterator();
            while (iter.hasNext())
            {
                GeometryProcessor<? extends Geometry> proc = iter.next();
                if (!myProcessors.contains(proc))
                {
                    iter.remove();
                }
            }
        }

        // Removal of a processor may result in all of the processors for a
        // snapshot being ready.
        long latestProjectionReady = -1;
        Iterator<Entry<Projection, Set<GeometryProcessor<? extends Geometry>>>> iter = myProjectionSnapshots.entrySet()
                .iterator();
        while (iter.hasNext())
        {
            Entry<Projection, Set<GeometryProcessor<? extends Geometry>>> entry = iter.next();
            if (myProcessors.size() <= entry.getValue().size())
            {
                if (projectionIsReady(entry.getKey(), myProcessors))
                {
                    iter.remove();
                }
                latestProjectionReady = Math.max(latestProjectionReady, entry.getKey().getActivationTimestamp());
            }
        }

        if (latestProjectionReady > -1)
        {
            cleanProjections(latestProjectionReady);
        }
    }

    /**
     * Set the projectionShapshot to be the active snapshot. This will cause the
     * snapshot's activation time to be updated to the current time and all
     * processors should become ready to render their geometries based on this
     * projection.
     *
     * @param projectionSnapshot the projectionShapshot to make active.
     */
    public synchronized void setProjectionSnapshot(Projection projectionSnapshot)
    {
        projectionSnapshot.setActivationTimestamp();
        if (myProjectionSnapshots.get(projectionSnapshot) == null)
        {
            Set<GeometryProcessor<? extends Geometry>> ready = New.set();
            myProjectionSnapshots.put(projectionSnapshot, ready);
        }
        myNewestProjection = projectionSnapshot;
    }

    /** Allow the current projection to be changed to a new projection. */
    public synchronized void unlockProjection()
    {
        myProjectionLocked = false;
        switchProjection();
    }

    /**
     * Notification that a processor is ready to use a projection snapshot. If
     * the snapshot is the same as the most recent one, it is added to the ready
     * processors. If there is no projection change pending, the processor is
     * immediately told to switch the given projection.
     *
     * @param processor The processor which is ready to switch projections.
     * @param projectionSnapshot The projection to which the processor is ready
     *            to switch.
     */
    protected synchronized void projectionReady(GeometryProcessor<? extends Geometry> processor, Projection projectionSnapshot)
    {
        if (!processor.sensitiveToProjectionChanges() || Utilities.sameInstance(myActiveProjection, projectionSnapshot))
        {
            return;
        }

        if (myProcessors.isEmpty() || !myProcessors.contains(processor))
        {
            processor.switchToProjection(projectionSnapshot);
            return;
        }

        Set<GeometryProcessor<? extends Geometry>> readyProcessors = myProjectionSnapshots.get(projectionSnapshot);
        if (readyProcessors != null)
        {
            readyProcessors.add(processor);
            // If all of the processors are ready, then tell them to switch.
            if (myProcessors.size() <= readyProcessors.size())
            {
                if (projectionIsReady(projectionSnapshot, myProcessors))
                {
                    myProjectionSnapshots.remove(projectionSnapshot);
                }
                cleanProjections(projectionSnapshot.getActivationTimestamp());
            }
        }
    }

    /**
     * Clean out old projections which come before the given timestamp.
     *
     * @param timestamp The time before which projections are no longer current.
     */
    private synchronized void cleanProjections(long timestamp)
    {
        Iterator<Entry<Projection, Set<GeometryProcessor<? extends Geometry>>>> iter = myProjectionSnapshots.entrySet()
                .iterator();
        while (iter.hasNext())
        {
            Entry<Projection, Set<GeometryProcessor<? extends Geometry>>> entry = iter.next();
            if (entry.getKey().getActivationTimestamp() < timestamp)
            {
                iter.remove();
            }
        }
    }

    /**
     * A new projection is ready for rendering. Inform interested parties and
     * clean up any older projections.
     *
     * @param projectionShapshot The projection which is ready for rendering.
     * @param processors The processors which are participating in the
     *            projection change.
     * @return true when the projection should be removed from the pending
     *         snapshots.
     */
    private synchronized boolean projectionIsReady(Projection projectionShapshot,
            Set<GeometryProcessor<? extends Geometry>> processors)
    {
        boolean shouldRemove = false;
        Set<GeometryProcessor<? extends Geometry>> procs = New.set(processors);
        if (myPendingProjection == null
                || myPendingProjection.getFirstObject().getActivationTimestamp() < projectionShapshot.getActivationTimestamp())
        {
            myPendingProjection = new Pair<>(projectionShapshot, procs);
            shouldRemove = true;
            if (!myProjectionLocked)
            {
                switchProjection();
            }
        }
        return shouldRemove;
    }

    /**
     * Tell all of the interested parties that the most recently ready
     * projection should become the currently rendered projection.
     */
    private synchronized void switchProjection()
    {
        if (myPendingProjection != null && !myProjectionLocked)
        {
            myActiveProjection = myPendingProjection.getFirstObject();
            for (GeometryProcessor<? extends Geometry> proc : myPendingProjection.getSecondObject())
            {
                proc.switchToProjection(myActiveProjection);
            }
            TileDataBuilder.setActiveProjection(myActiveProjection);
            myPendingProjection = null;
        }
    }
}
