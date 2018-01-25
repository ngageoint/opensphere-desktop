package io.opensphere.mantle.transformer.impl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import gnu.trove.set.hash.TLongHashSet;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.mantle.data.element.event.consolidated.AbstractConsolidatedDataElementChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementColorChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementHighlightChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementSelectionChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementVisibilityChangeEvent;
import io.opensphere.mantle.data.geom.factory.impl.MapGeometrySupportGeometryFactory;
import io.opensphere.mantle.transformer.impl.worker.DefaultUpdateGeometriesWorker;
import io.opensphere.mantle.transformer.impl.worker.DeriveUpdateColorGeometriesWorker;
import io.opensphere.mantle.transformer.impl.worker.PublishUnpublishGeometrySetWorker;
import io.opensphere.mantle.transformer.util.GeometrySetUtil;

/**
 * The Class ConsolidatedDataElementEventHandler.
 */
class ConsolidatedDataElementEventHandler implements Runnable
{
    /**
     * The transformer on which the worker will operate.
     */
    private final DefaultMapDataElementTransformer myDefaultMapDataElementTransformer;

    /** The event. */
    private final AbstractConsolidatedDataElementChangeEvent myEvent;

    /** The Factory. */
    private final MapGeometrySupportGeometryFactory myFactory;

    /**
     * Instantiates a new consolidated data element event handler.
     *
     * @param evt the evt
     * @param factory the factory on which the worker will operate.
     * @param defaultMapDataElementTransformer The transformer on which the
     *            worker will operate.
     */
    public ConsolidatedDataElementEventHandler(DefaultMapDataElementTransformer defaultMapDataElementTransformer,
            AbstractConsolidatedDataElementChangeEvent evt, MapGeometrySupportGeometryFactory factory)
    {
        myDefaultMapDataElementTransformer = defaultMapDataElementTransformer;
        myEvent = evt;
        myFactory = factory;
    }

    @Override
    public void run()
    {
        if (!(myEvent instanceof ConsolidatedDataElementHighlightChangeEvent))
        {
            List<Long> idsOfInterest = null;
            if (myEvent.getDataTypeKeys().size() == 1)
            {
                idsOfInterest = myEvent.getRegistryIds();
            }
            else
            {
                idsOfInterest = CollectionUtilities.intersectionAsList(myDefaultMapDataElementTransformer.getIdSet(),
                        myEvent.getRegistryIds());
            }

            if (!idsOfInterest.isEmpty())
            {
                if (myEvent instanceof ConsolidatedDataElementVisibilityChangeEvent)
                {
                    processVisibilityChangeEvent((ConsolidatedDataElementVisibilityChangeEvent)myEvent, idsOfInterest);
                }
                else if (myEvent instanceof ConsolidatedDataElementSelectionChangeEvent)
                {
                    DeriveUpdateColorGeometriesWorker worker = new DeriveUpdateColorGeometriesWorker(
                            myDefaultMapDataElementTransformer, idsOfInterest);
                    worker.run();
                }
                else if (myEvent instanceof ConsolidatedDataElementColorChangeEvent)
                {
                    ConsolidatedDataElementColorChangeEvent colorEvent = (ConsolidatedDataElementColorChangeEvent)myEvent;
                    if (!Utilities.sameInstance(myEvent.getSource(), myDefaultMapDataElementTransformer)
                            && !colorEvent.isExternalOnly())
                    {
                        DeriveUpdateColorGeometriesWorker worker = new DeriveUpdateColorGeometriesWorker(
                                myDefaultMapDataElementTransformer, idsOfInterest);
                        worker.run();
                    }
                }
                else
                {
                    DefaultUpdateGeometriesWorker update = new DefaultUpdateGeometriesWorker(myDefaultMapDataElementTransformer,
                            myFactory, idsOfInterest);
                    update.run();
                }
            }
        }
    }

    /**
     * Process visibility change.
     *
     * @param vEvent the v event
     * @param idsOfInterest the ids of interest
     */
    private void processVisibilityChangeEvent(ConsolidatedDataElementVisibilityChangeEvent vEvent, List<Long> idsOfInterest)
    {
        long[] idsOfInterestArray = CollectionUtilities.toLongArray(idsOfInterest);
        TLongHashSet toShowIdSet = new TLongHashSet(vEvent.getVisibleIdSet().size());
        toShowIdSet.addAll(vEvent.getVisibleIdSet().toArray());
        toShowIdSet.retainAll(idsOfInterestArray);
        if (!toShowIdSet.isEmpty())
        {
            Set<Geometry> found = GeometrySetUtil.findGeometrySetWithIds(
                    myDefaultMapDataElementTransformer.getHiddenGeometrySet(),
                    myDefaultMapDataElementTransformer.getGeometrySetLock(), CollectionUtilities.listView(toShowIdSet.toArray()),
                    GeometrySetUtil.ALL_BITS_MASK);
            if (!found.isEmpty())
            {
                ReentrantLock geometrySetLock = myDefaultMapDataElementTransformer.getGeometrySetLock();
                geometrySetLock.lock();
                try
                {
                    myDefaultMapDataElementTransformer.getGeometrySet().addAll(found);
                    myDefaultMapDataElementTransformer.getHiddenGeometrySet().removeAll(found);
                }
                finally
                {
                    geometrySetLock.unlock();
                }

                if (DefaultMapDataElementTransformer.PUBLISH_CHANGES_TO_GEOMETRY_REGISTRY)
                {
                    PublishUnpublishGeometrySetWorker worker = new PublishUnpublishGeometrySetWorker(
                            myDefaultMapDataElementTransformer, found, true);
                    worker.run();
                }
            }
        }

        TLongHashSet toHideIDSet = new TLongHashSet(vEvent.getInvisibleIdSet().size());
        toHideIDSet.addAll(vEvent.getInvisibleIdSet().toArray());
        toHideIDSet.retainAll(idsOfInterestArray);
        if (!toHideIDSet.isEmpty())
        {
            Set<Geometry> found = GeometrySetUtil.findGeometrySetWithIds(myDefaultMapDataElementTransformer.getGeometrySet(),
                    myDefaultMapDataElementTransformer.getGeometrySetLock(), CollectionUtilities.listView(toHideIDSet.toArray()),
                    GeometrySetUtil.ALL_BITS_MASK);
            if (!found.isEmpty())
            {
                ReentrantLock geometrySetLock = myDefaultMapDataElementTransformer.getGeometrySetLock();
                geometrySetLock.lock();
                try
                {
                    myDefaultMapDataElementTransformer.getGeometrySet().removeAll(found);
                    myDefaultMapDataElementTransformer.getHiddenGeometrySet().addAll(found);
                }
                finally
                {
                    geometrySetLock.unlock();
                }

                if (DefaultMapDataElementTransformer.PUBLISH_CHANGES_TO_GEOMETRY_REGISTRY)
                {
                    PublishUnpublishGeometrySetWorker worker = new PublishUnpublishGeometrySetWorker(
                            myDefaultMapDataElementTransformer, found, false);
                    worker.run();
                }
            }
        }
    }
}
