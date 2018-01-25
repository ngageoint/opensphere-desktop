package io.opensphere.core.util.callout;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.opensphere.core.callout.Callout;
import io.opensphere.core.callout.CalloutDragListener;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.geometry.GeoScreenBubbleGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.messaging.DefaultGenericPublisher;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Manager class that handles callouts (text bubbles) associated with other
 * geometries. This class keeps track of the tiles and labels associated with
 * the callouts and handles events that cause the callouts to be repositioned.
 *
 * @param <E> The type of the key used when adding and removing callouts.
 */
public class CalloutManager<E> extends DefaultGenericPublisher<Geometry>
{
    /** Handler for callbacks from the drag manager. */
    private final CalloutDragHandler myDragHandler = new CalloutDragHandler()
    {
        @Override
        public GeoScreenBubbleGeometry getAssociatedBubble(TileGeometry tile)
        {
            synchronized (myTileToLineMap)
            {
                return myTileToLineMap.get(tile);
            }
        }

        @Override
        public boolean handles(TileGeometry geom)
        {
            synchronized (myTileToLineMap)
            {
                return myTileToLineMap.containsKey(geom);
            }
        }

        @Override
        public void replaceCallout(TileGeometry replace, TileGeometry newTile, GeoScreenBubbleGeometry newBubble)
        {
            synchronized (myTileToLineMap)
            {
                Collection<Geometry> adds = New.collection(2);
                Collection<Geometry> removes = New.collection(2);

                GeoScreenBubbleGeometry line = myTileToLineMap.get(replace);
                if (line != null)
                {
                    removes.add(replace);
                    removes.add(line);
                }
                adds.add(newTile);
                adds.add(newBubble);

                myTileToLineMap.put(newTile, newBubble);

                CalloutKey<E> key = myTileToKeyMap.remove(replace);
                CollectionUtilities.multiMapAdd(myKeyToTilesMap, key.getKey(), newTile, false);
                myTileToKeyMap.put(newTile, key);

                sendObjects(this, adds, removes);

                if (myDragListener != null)
                {
                    myDragListener.calloutDragged(key.getKey(), newBubble.getBoundingBox().getAnchor().getAnchorOffset(),
                            key.getIndex());
                }
            }
        }
    };

    /** Change support for callout dragging. */
    private final CalloutDragListener<E> myDragListener;

    /** Manager for drag events. */
    private final CalloutDragManager myDragManager;

    /** Map of keys to callout tiles. */
    private final Map<E, List<TileGeometry>> myKeyToTilesMap = New.map();

    /** Map of callout tiles to keys. */
    private final Map<TileGeometry, CalloutKey<E>> myTileToKeyMap = New.map();

    /** Map of callout tiles to callout lines. */
    private final Map<TileGeometry, GeoScreenBubbleGeometry> myTileToLineMap = New.map();

    /**
     * Constructor.
     *
     * @param controlRegistry The system control registry used to registry for
     *            drag events.
     * @param bindingCategory The category for the mouse bindings.
     * @param executor The executor for drag actions.
     */
    public CalloutManager(ControlRegistry controlRegistry, String bindingCategory, ScheduledThreadPoolExecutor executor)
    {
        this(controlRegistry, bindingCategory, executor, (CalloutDragListener<E>)null);
    }

    /**
     * Constructor.
     *
     * @param controlRegistry The system control registry used to registry for
     *            drag events.
     * @param bindingCategory The category for the mouse bindings.
     * @param executor The executor for drag actions.
     * @param dragListener Optional listener to be notified of callout dragging.
     */
    public CalloutManager(ControlRegistry controlRegistry, String bindingCategory, ScheduledThreadPoolExecutor executor,
            CalloutDragListener<E> dragListener)
    {
        myDragManager = new CalloutDragManager(controlRegistry, myDragHandler, bindingCategory, executor);
        myDragListener = dragListener;
    }

    /**
     * Add a callout. Generate the necessary geometries and add them to the
     * return collection.
     *
     * @param key Optional key that may be used to reference the callout later.
     * @param callout The callout.
     * @param adds Return collection of added geometries.
     */
    public void addCallout(E key, Callout callout, Collection<? super Geometry> adds)
    {
        addCallouts(key, Collections.singleton(callout), adds);
    }

    /**
     * Add callouts. Generate the necessary geometries and add them to the
     * return collection.
     *
     * @param key Optional key that may be used to reference the callout later.
     * @param callouts The callouts.
     * @param adds Return collection of added geometries.
     */
    public void addCallouts(E key, Collection<? extends Callout> callouts, Collection<? super Geometry> adds)
    {
        synchronized (myTileToLineMap)
        {
            int index = 0;
            for (Callout callout : callouts)
            {
                TileGeometry tile = CalloutGeometryUtil.createCalloutTile(callout);
                GeoScreenBoundingBox gsbb = (GeoScreenBoundingBox)tile.getBounds();
                GeoScreenBubbleGeometry line = CalloutGeometryUtil.createTextBubble(gsbb, callout);

                CollectionUtilities.multiMapAdd(myKeyToTilesMap, key, tile, false);
                myTileToKeyMap.put(tile, new CalloutKey<E>(key, index));
                myTileToLineMap.put(tile, line);

                adds.add(tile);
                adds.add(line);

                index++;
            }
        }
    }

    /**
     * Close the manager. Remove all geometries.
     */
    public void close()
    {
        myDragManager.close();

        synchronized (myTileToLineMap)
        {
            if (!myTileToLineMap.isEmpty())
            {
                Collection<Geometry> removes = New.collection(myTileToLineMap.size() * 2);
                for (Entry<TileGeometry, GeoScreenBubbleGeometry> entry : myTileToLineMap.entrySet())
                {
                    removes.add(entry.getKey());
                    removes.add(entry.getValue());
                }
                sendObjects(this, Collections.<Geometry>emptySet(), removes);
                myTileToLineMap.clear();
            }
            myKeyToTilesMap.clear();
            myTileToKeyMap.clear();
        }
    }

    /**
     * Remove the callout associated with the given key.
     *
     * @param key The key.
     * @param removes Return collection of removed geometries.
     */
    public void removeCallouts(E key, Collection<? super Geometry> removes)
    {
        synchronized (myTileToLineMap)
        {
            List<TileGeometry> tiles = myKeyToTilesMap.remove(key);
            if (tiles != null)
            {
                for (TileGeometry tile : tiles)
                {
                    myTileToKeyMap.remove(tile);
                    GeoScreenBubbleGeometry line = myTileToLineMap.remove(tile);

                    removes.add(tile);
                    removes.add(line);
                }
            }
        }
    }
}
