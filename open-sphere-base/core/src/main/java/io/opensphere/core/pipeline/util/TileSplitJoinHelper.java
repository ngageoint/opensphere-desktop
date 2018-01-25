package io.opensphere.core.pipeline.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.viewer.Viewer;

/**
 * Helper class that handles some of the details of tile splitting and joining.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class TileSplitJoinHelper
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TileSplitJoinHelper.class);

    /**
     * When a tile is larger than this percentage of the tile in the center of
     * the view which has been split, the tile will also be split for
     * consistency.
     */
    private static final double SPLIT_CONSISTENCY_FACTOR;

    /** An executor that procrastinates before running tasks. */
    private final Executor myProcrastinatingExecutor;

    /** Flag indicating if the helper has been stopped. */
    private volatile boolean myStopped;

    static
    {
        final String consistProp = System.getProperty("opensphere.pipeline.tileprocessing.splitConsistencyFactor");
        double consistency;
        try
        {
            consistency = Double.parseDouble(consistProp);
        }
        catch (final NumberFormatException e)
        {
            LOGGER.warn("Could not read opensphere.pipeline.tileprocessing.splitConsistencyFactor");
            final double uselessIntermediateVariable = .8;
            consistency = uselessIntermediateVariable;
        }
        SPLIT_CONSISTENCY_FACTOR = consistency;
    }

    /**
     * Constructor.
     *
     * @param executor An executor to use for splitting/joining.
     */
    public TileSplitJoinHelper(ScheduledExecutorService executor)
    {
        myProcrastinatingExecutor = new ProcrastinatingExecutor(executor, 300);
    }

    /**
     * Find tiles that need to be either split or joined and add or remove tiles
     * from the processor as appropriate.
     *
     * @param geoms The collection of geometries which are to be split or
     *            merged.
     */
    public synchronized void doSplitsAndJoins(Collection<? extends AbstractTileGeometry<?>> geoms)
    {
        final Set<AbstractTileGeometry<?>> removes = New.set();
        final Set<AbstractTileGeometry<?>> adds = New.set();

        doSplitsAndJoins(geoms, adds, removes);

        if (!adds.isEmpty() || !removes.isEmpty())
        {
            receiveGeometries(adds, removes);
        }
    }

    /**
     * Find tiles that need to be either split or joined and add them to the
     * appropriate return collection.
     *
     * @param geoms The collection of geometries which are to be split or
     *            merged.
     * @param adds Return collection of adds.
     * @param removes Return collection of removes.
     */
    public synchronized void doSplitsAndJoins(Collection<? extends AbstractTileGeometry<?>> geoms,
            Set<AbstractTileGeometry<?>> adds, Set<AbstractTileGeometry<?>> removes)
    {
        triageGeometries(geoms, adds, removes);

        final Set<AbstractTileGeometry<?>> geomsPlusAdds = New.set(geoms);
        geomsPlusAdds.addAll(adds);
        final Set<AbstractTileGeometry<?>> auxAdds = New.set();
        adjustSplitLevels(geomsPlusAdds, auxAdds);
        adds.addAll(auxAdds);

        // Don't remove anything which is being added
        if (!removes.isEmpty() && !adds.isEmpty())
        {
            removes.removeAll(adds);
        }

        // Orphan tiles aggressively to avoid having them accidently re-inserted
        // by the tile processor.
        for (final AbstractTileGeometry<?> geom : removes)
        {
            geom.getParent().clearChildren();
        }
    }

    /**
     * Schedule a new split/join operation to occur. The time delay until it
     * occurs will be no less than the time on the executor, but may be longer
     * if this method is called again before execution.
     */
    public void scheduleSplitJoin()
    {
        myProcrastinatingExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                if (!myStopped)
                {
                    doSplitsAndJoins(getProcessorGeometries());
                }
            }
        });
    }

    /** Allow scheduling split/join operations. */
    public void start()
    {
        myStopped = false;
    }

    /** Stop any scheduled split/join operations. */
    public void stop()
    {
        myStopped = true;
    }

    /**
     * Get all of the geometries being processed by the processor.
     *
     * @return All of the geometries being processed by the processor.
     */
    protected abstract Collection<? extends AbstractTileGeometry<?>> getProcessorGeometries();

    /**
     * Get the current projection. This may be <code>null</code> if geographic
     * positions are not supported.
     *
     * @return The current projection.
     */
    protected abstract Projection getProjection();

    /**
     * Get the set of geometries that are ready to be displayed.
     *
     * @return The ready geometries.
     */
    protected abstract Collection<? extends AbstractTileGeometry<?>> getReadyGeometries();

    /**
     * Get the viewer.
     *
     * @return The viewer.
     */
    protected abstract Viewer getViewer();

    /**
     * Determine if a geometry is currently in view.
     *
     * @param geom The geometry.
     * @return <code>true</code> if the geometry is on-screen.
     */
    protected abstract boolean isInView(AbstractTileGeometry<?> geom);

    /**
     * Add and remove geometries from the processor.
     *
     * @param adds New geometries which are required to do splits or joins.
     * @param removes Geometries which are no longer needed.
     */
    protected abstract void receiveGeometries(Collection<? extends AbstractTileGeometry<?>> adds,
            Collection<? extends AbstractTileGeometry<?>> removes);

    /**
     * Adjust the split level of some tiles to make the splitting more uniform
     * within the view.
     *
     * @param adds Tiles which are top level or are already being added.
     * @param auxAdds Tiles which will be added for uniformity.
     */
    private void adjustSplitLevels(Set<AbstractTileGeometry<?>> adds, Set<AbstractTileGeometry<?>> auxAdds)
    {
        final Viewer viewer = getViewer();
        if (viewer == null)
        {
            return;
        }
        final Projection projection = getProjection();

        // Find the deepest generation which has a tile that is in view and keep
        // track of the in view tiles.
        int maxGeneration = 0;
        final Collection<AbstractTileGeometry<?>> inViewAdds = New.collection();
        for (final AbstractTileGeometry<?> geom : adds)
        {
            if (isInView(geom))
            {
                inViewAdds.add(geom);
                maxGeneration = Math.max(maxGeneration, geom.getGeneration());
            }
        }

        // Get the smallest tile pixel width which is at the (deepest - 1)
        // generation for geometries which are in view. This is the size which
        // will be used to determine relative splitting.
        double relativeSplitTilePixelWidth = Double.MAX_VALUE;
        for (final AbstractTileGeometry<?> geom : inViewAdds)
        {
            final AbstractTileGeometry<?> parent = geom.getParent();
            if (parent != null && geom.getGeneration() == maxGeneration)
            {
                relativeSplitTilePixelWidth = Math.min(relativeSplitTilePixelWidth, getPixelWidth(parent, viewer, projection));
            }
        }

        final double tileFactor = relativeSplitTilePixelWidth * SPLIT_CONSISTENCY_FACTOR;
        for (final AbstractTileGeometry<?> geom : inViewAdds)
        {
            if (geom.getGeneration() < maxGeneration)
            {
                final Collection<? extends AbstractTileGeometry<?>> children = geom.getChildren(false);
                if (children.isEmpty() || !adds.contains(children.iterator().next()))
                {
                    final double tilePixelSize = getPixelWidth(geom, viewer, projection);
                    if (tilePixelSize > tileFactor)
                    {
                        auxAdds.addAll(geom.getChildren(true));
                    }
                }
            }
        }
    }

    /**
     * Check the size of a tile geometry.
     *
     * @param geom The geometry to check.
     * @param viewer The current viewer.
     * @param projection The current projection.
     * @return
     *         <ul>
     *         <li>{@code 1} if the geometry should be split</li>
     *         <li>{@code -1} if the geometry should be joined</li>
     *         <li>{@code 0} if the geometry should not be changed</li>
     *         </ul>
     */
    private int checkTileSize(AbstractTileGeometry<?> geom, Viewer viewer, Projection projection)
    {
        int result = 0;

        final double pixelWidth = getPixelWidth(geom, viewer, projection);
        if (pixelWidth > geom.getMaximumDisplaySize())
        {
            result = 1;
        }
        else if (pixelWidth < geom.getMinimumDisplaySize())
        {
            result = -1;
        }

        return result;
    }

    /**
     * Get the approximate with in pixels of the tile.
     *
     * @param geom The tile whose width is desired.
     * @param viewer The current viewer.
     * @param projection The current projection.
     * @return the approximate width in pixels.
     */
    private double getPixelWidth(AbstractTileGeometry<?> geom, Viewer viewer, Projection projection)
    {
        // TODO look in the cache for the bounding ellipsoid since it is already
        // generated for in-view checks.
        return viewer
                .getPixelWidth(projection.getBoundingEllipsoid((GeographicBoundingBox)geom.getBounds(), Vector3d.ORIGIN, false));
    }

    /**
     * Triage the input geometries into those that need spitting, those that
     * need joining, and those that are fine.
     *
     * @param input The geometries to triage.
     * @param adds The output geometries that need to be added to the processor
     *            because of splitting.
     * @param removes The output geometries that need to be removed from the
     *            processor because of joining.
     */
    private void triageGeometries(Collection<? extends AbstractTileGeometry<?>> input, Set<AbstractTileGeometry<?>> adds,
            Set<AbstractTileGeometry<?>> removes)
    {
        final Viewer viewer = getViewer();
        if (viewer == null)
        {
            return;
        }
        final Projection projection = getProjection();

        final Set<AbstractTileGeometry<?>> skip = New.set();
        for (final AbstractTileGeometry<?> geom : input)
        {
            if ((geom.isDivisible() || geom.getParent() != null) && geom.getBounds() instanceof GeographicBoundingBox
                    && !skip.contains(geom))
            {
                if (!isInView(geom))
                {
                    // Even if this geometry is not on screen it should be left
                    // in the processor since its siblings might be on screen.
                    // However, its descendants can be removed.
                    if (geom.hasChildren())
                    {
                        final Collection<AbstractTileGeometry<?>> removeDescendants = New.collection();
                        geom.getDescendants(removeDescendants);
                        removes.addAll(removeDescendants);
                        skip.addAll(removeDescendants);
                    }
                }
                else
                {
                    int result = 0;
                    // Check to see if we are being asked to hold at a specific
                    // generation and if the division override is enabled.
                    final int holdGen = geom.getDivisionHoldGeneration();
                    if (geom.isDivisionOverride() && holdGen >= 0)
                    {
                        // We will direct tile splits based on the hold
                        // generation and override rather than relying on
                        // checkTileSize.
                        result = geom.getGeneration() < holdGen ? 1 : holdGen > geom.getGeneration() ? -1 : 0;
                    }
                    else
                    {
                        result = checkTileSize(geom, viewer, projection);
                    }
                    if (result == 1)
                    {
                        // Split
                        adds.addAll(geom.getChildren(true));
                    }
                    else if (result == -1 && geom.getParent() != null)
                    {
                        // Join
                        final Set<AbstractTileGeometry<?>> removeDescendants = New.set();
                        geom.getParent().getDescendants(removeDescendants);
                        removes.addAll(removeDescendants);
                        skip.addAll(removeDescendants);
                    }
                    else
                    {
                        // No Change
                        if (geom.hasChildren())
                        {
                            final Set<AbstractTileGeometry<?>> removeDescendants = New.set();
                            geom.getDescendants(removeDescendants);
                            removes.addAll(removeDescendants);
                            skip.addAll(removeDescendants);
                        }
                    }
                }
            }
        }

        if (!adds.isEmpty())
        {
            final Set<AbstractTileGeometry<?>> childAdds = new HashSet<>();
            // There should be no child removes since all of the descendants of
            // removes have already been added to the remove list.
            final Set<AbstractTileGeometry<?>> childRemoves = new HashSet<>();
            triageGeometries(adds, childAdds, childRemoves);
            if (!childRemoves.isEmpty())
            {
                removes.addAll(childRemoves);
            }
            if (!childAdds.isEmpty())
            {
                adds.addAll(childAdds);
            }
        }
    }
}
