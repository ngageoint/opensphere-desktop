package io.opensphere.mantle.transformer.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.TimeManager;
import io.opensphere.core.geometry.ConstrainableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.plugin.selection.SelectionCommand;
import io.opensphere.mantle.transformer.impl.worker.DataElementTransformerWorkerDataProvider;

/**
 * The Class PolygonRegionCommandWorker.
 */
public abstract class PolygonRegionCommandWorker implements Runnable
{
    /** The my command. */
    private final SelectionCommand myCommand;

    /** The my intersecting set. */
    private final Set<Long> myIntersectingSet = New.set();

    /** The my non intersecting set. */
    private final Set<Long> myNonIntersectingSet = New.set();

    /** The Provider. */
    private final DataElementTransformerWorkerDataProvider myProvider;

    /** The my region. */
    private final List<Polygon> myRegions;

    /** The Time manager. */
    private final TimeManager myTimeManager;

    /** The Use time in intersection check. */
    private final boolean myUseTimeInIntersectionCheck;

    /**
     * Instantiates a new selection command worker.
     *
     * @param provider the provider
     * @param regions the regions
     * @param command the command
     * @param useTimeInIntersectionCheck the use time in intersection check
     */
    public PolygonRegionCommandWorker(DataElementTransformerWorkerDataProvider provider, List<Polygon> regions,
            SelectionCommand command, boolean useTimeInIntersectionCheck)
    {
        myProvider = provider;
        myRegions = regions;
        myCommand = command;
        myTimeManager = myProvider.getToolbox().getTimeManager();
        myUseTimeInIntersectionCheck = useTimeInIntersectionCheck;
    }

    /**
     * Gets the command.
     *
     * @return the command
     */
    public SelectionCommand getCommand()
    {
        return myCommand;
    }

    /**
     * Gets the intersecting id set.
     *
     * @return the intersecting id set
     */
    public Set<Long> getIntersectingIdSet()
    {
        return myIntersectingSet;
    }

    /**
     * Gets the non intersecting id set.
     *
     * @return the non intersecting id set
     */
    public Set<Long> getNonIntersectingIdSet()
    {
        return myNonIntersectingSet;
    }

    /**
     * Gets the provider.
     *
     * @return the provider
     */
    public DataElementTransformerWorkerDataProvider getProvider()
    {
        return myProvider;
    }

    /**
     * Gets the regions.
     *
     * @return the regions
     */
    public List<Polygon> getRegion()
    {
        return myRegions;
    }

    /**
     * Passes time constraint check.
     *
     * @param g the g
     * @return true, if successful
     */
    public boolean passesTimeConstraintCheck(Geometry g)
    {
        boolean pass = true;
        if (g instanceof ConstrainableGeometry)
        {
            ConstrainableGeometry cg = (ConstrainableGeometry)g;
            Constraints constraints = cg.getConstraints();
            if (myTimeManager != null && constraints != null && constraints.getTimeConstraint() != null
                    && !constraints.getTimeConstraint().check(myTimeManager.getPrimaryActiveTimeSpans()))
            {
                if (constraints.getTimeConstraint().getKey() == null)
                {
                    pass = false;
                }
                else
                {
                    Collection<? extends TimeSpan> secondary = myTimeManager
                            .getSecondaryActiveTimeSpans(constraints.getTimeConstraint().getKey());
                    if (secondary == null || !constraints.getTimeConstraint().check(secondary))
                    {
                        pass = false;
                    }
                }
            }
        }
        return pass;
    }

    /**
     * Process.
     */
    public abstract void process();

    @Override
    public final void run()
    {
        ReentrantLock lock = myProvider.getGeometrySetLock();
        lock.lock();
        try
        {
            determineIntersectingSets(myUseTimeInIntersectionCheck);
            process();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Determine intersecting sets.
     *
     * @param useTimeConstraints true to use time constraints as part of the
     *            intersection check.
     */
    private void determineIntersectingSets(boolean useTimeConstraints)
    {
        GeometryFactory gf = new GeometryFactory();
        for (Geometry g : myProvider.getGeometrySet())
        {
            boolean intersecting = true;
            if (useTimeConstraints)
            {
                intersecting = passesTimeConstraintCheck(g);
            }

            if (intersecting)
            {
                intersecting = g.jtsIntersectionTests(new Geometry.JTSIntersectionTests(true, true, false), myRegions, gf);
            }

            if (intersecting)
            {
                myIntersectingSet.add(Long.valueOf(g.getDataModelId() & myProvider.getDataModelIdFromGeometryIdBitMask()));
            }
            else
            {
                myNonIntersectingSet.add(Long.valueOf(g.getDataModelId() & myProvider.getDataModelIdFromGeometryIdBitMask()));
            }
        }
    }
}
