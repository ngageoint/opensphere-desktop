package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import net.jcip.annotations.ThreadSafe;

/**
 * Manages coordinated removal of geometries from the {@link PickManager}.
 */
@ThreadSafe
public class PickManagerGeometryRemover
{
    /** The executor. */
    private final Executor myExecutor;

    /**
     * A batch of geometries to be removed after the next call to
     * {@link #flush()} .
     */
    private volatile Collection<Geometry> myNextFlush;

    /** Atomic updater for {@link #myNextFlush}. */
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<PickManagerGeometryRemover, Collection> NEXT_FLUSH_UPDATER = AtomicReferenceFieldUpdater
            .newUpdater(PickManagerGeometryRemover.class, Collection.class, "myNextFlush");

    /** The pick manager. */
    private final PickManager myPickManager;

    /**
     * Queue of tiles that need to be removed from the pick manager.
     */
    private final LinkedBlockingQueue<Geometry> myPickManagerRemoves = new LinkedBlockingQueue<>();

    /**
     * Constructor.
     *
     * @param pickManager The pick manager.
     * @param executor The executor.
     */
    public PickManagerGeometryRemover(PickManager pickManager, Executor executor)
    {
        myPickManager = Utilities.checkNull(pickManager, "pickManager");
        myExecutor = Utilities.checkNull(executor, "executor");
    }

    /**
     * Add a geometry to be queued for removal from the pick manager.
     *
     * @param geom The geometry.
     */
    public void add(Geometry geom)
    {
        myPickManagerRemoves.add(geom);
    }

    /**
     * Add some geometries to be queued for removal from the pick manager.
     *
     * @param geoms The geometries.
     */
    public void addAll(Collection<? extends Geometry> geoms)
    {
        myPickManagerRemoves.addAll(geoms);
    }

    /** Schedule the gathered geometries to be removed from the pick manager. */
    public void flush()
    {
        if (myNextFlush != null)
        {
            @SuppressWarnings("unchecked")
            final Collection<Geometry> pickManagerRemoves = NEXT_FLUSH_UPDATER.getAndSet(this, null);
            if (pickManagerRemoves != null)
            {
                myExecutor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        myPickManager.removeGeometries(pickManagerRemoves);
                    }
                });
            }
        }
    }

    /**
     * Gather the geometries that have been added to the queue so they will be
     * removed from the pick manager after the next call to {@link #flush()}.
     */
    public synchronized void gather()
    {
        if (!myPickManagerRemoves.isEmpty())
        {
            @SuppressWarnings("unchecked")
            Collection<Geometry> nextFlush = NEXT_FLUSH_UPDATER.getAndSet(this, null);
            if (nextFlush == null)
            {
                nextFlush = New.collection(myPickManagerRemoves.size());
            }
            myPickManagerRemoves.drainTo(nextFlush);

            if (!nextFlush.isEmpty() && !NEXT_FLUSH_UPDATER.compareAndSet(this, null, nextFlush))
            {
                throw new IllegalStateException("myNextFlush must be null");
            }
        }
    }
}
