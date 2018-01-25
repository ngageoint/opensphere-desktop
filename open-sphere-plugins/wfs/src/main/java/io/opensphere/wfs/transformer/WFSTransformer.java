package io.opensphere.wfs.transformer;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent.Type;
import io.opensphere.wfs.placenames.PlaceNameLayerManager;
import io.opensphere.wfs.placenames.PlaceNamesEvent;

/**
 * Transforms {@code WFSPoint}s to {@code PointGeometry}s, and displays a
 * selection region.
 */
public class WFSTransformer extends DefaultTransformer
{
    /** Listener for events pertaining to data type events. */
    private EventListener<AbstractDataTypeInfoChangeEvent> myDataTypeInfoListener;

    /**
     * My collection of place names that I am displaying. Accessed by server
     * name and then by layer name.
     */
    private final Map<String, Map<String, Collection<LabelGeometry>>> myGeometries;

    /** Listener for events which contain place names to publish. */
    private EventListener<PlaceNamesEvent> myPlaceNamesEventListener;

    /**
     * References to facilities that may be used by the plug-in to interact with
     * the rest of the system.
     */
    private Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox References to facilities that may be used by the plug-in
     *            to interact with the rest of the system.
     */
    public WFSTransformer(Toolbox toolbox)
    {
        this(toolbox.getDataRegistry(), toolbox.getEventManager());
        myToolbox = toolbox;
        if (toolbox.getEventManager() != null)
        {
            myDataTypeInfoListener = createDataTypeInfoListener();
            toolbox.getEventManager().subscribe(AbstractDataTypeInfoChangeEvent.class, myDataTypeInfoListener);
            myPlaceNamesEventListener = createPlaceNamesEventListener();
            toolbox.getEventManager().subscribe(PlaceNamesEvent.class, myPlaceNamesEventListener);
        }
        else
        {
            myPlaceNamesEventListener = null;
            myDataTypeInfoListener = null;
        }
    }

    /**
     * Constructor.
     *
     * @param dataRegistry The data model repository.
     * @param eventManager The event manager.
     */
    protected WFSTransformer(DataRegistry dataRegistry, EventManager eventManager)
    {
        super(dataRegistry);
        myGeometries = new HashMap<>();

        if (eventManager != null)
        {
            myDataTypeInfoListener = createDataTypeInfoListener();
            eventManager.subscribe(AbstractDataTypeInfoChangeEvent.class, myDataTypeInfoListener);

            myPlaceNamesEventListener = createPlaceNamesEventListener();
            eventManager.subscribe(PlaceNamesEvent.class, myPlaceNamesEventListener);
        }
        else
        {
            myPlaceNamesEventListener = null;
            myDataTypeInfoListener = null;
        }
    }

    @Override
    public void close()
    {
        if (myToolbox.getEventManager() != null)
        {
            if (myPlaceNamesEventListener != null)
            {
                myToolbox.getEventManager().unsubscribe(PlaceNamesEvent.class, myPlaceNamesEventListener);
            }
            if (myDataTypeInfoListener != null)
            {
                myToolbox.getEventManager().unsubscribe(AbstractDataTypeInfoChangeEvent.class, myDataTypeInfoListener);
            }
        }

        super.close();
    }

    /**
     * Transform some WFS features into geometries.
     *
     * @param ids The feature ids.
     * @param timeSpan The time span comprising the features.
     * @param pos The locations of the features.
     * @param color The colors.
     * @param zOrder The Z-Order.
     * @param geometries The output geometries.
     */
    public void transform(long[] ids, TimeSpan timeSpan, GeographicPosition[] pos, Color[] color, int zOrder,
            Collection<? super Geometry> geometries)
    {
        // TODO currently there is one color per ID, even when all of the colors
        // are the same. We need to be able to group them so that the render
        // properties will only be created once for the group.
        PointGeometry.Builder<GeographicPosition> pointBuilder = new PointGeometry.Builder<GeographicPosition>();
        final float size = 3f;

        // TODO currently this is using a single time span for all the features
        Constraints constraints = new Constraints(TimeConstraint.getTimeConstraint(timeSpan));

        for (int index = 0; index < ids.length; ++index)
        {
            pointBuilder.setPosition(pos[index]);
            pointBuilder.setDataModelId(ids[index]);
            PointRenderProperties props = new DefaultPointRenderProperties(zOrder, true, true, false);
            props.setSize(size);
            props.setColor(color[index]);
            geometries.add(new PointGeometry(pointBuilder, props, constraints));
        }
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    protected Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Creates the data type info change listener.
     *
     * @return The event listener.
     */
    private EventListener<AbstractDataTypeInfoChangeEvent> createDataTypeInfoListener()
    {
        return new EventListener<AbstractDataTypeInfoChangeEvent>()
        {
            @Override
            public void notify(AbstractDataTypeInfoChangeEvent event)
            {
                if (event.getType() == Type.VISIBILITY_CHANGED
                        && PlaceNameLayerManager.SOURCE.equals(event.getDataTypeInfo().getSourcePrefix()))
                {
                    String server = event.getDataTypeInfo().getTypeKey();
                    String layer = event.getDataTypeInfo().getTypeName();

                    synchronized (myGeometries)
                    {
                        if (((Boolean)event.getValue()).booleanValue())
                        {
                            if (myGeometries.containsKey(server) && myGeometries.get(server).containsKey(layer))
                            {
                                // Add these geometries
                                publishGeometries(New.collection(myGeometries.get(server).get(layer)),
                                        Collections.<LabelGeometry>emptyList());
                            }
                        }
                        else
                        {
                            if (myGeometries.containsKey(server) && myGeometries.get(server).containsKey(layer))
                            {
                                // Remove these geometries
                                publishGeometries(Collections.<LabelGeometry>emptyList(),
                                        New.collection(myGeometries.get(server).get(layer)));
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * Creates the place names listener.
     *
     * @return The place names event listener.
     */
    private EventListener<PlaceNamesEvent> createPlaceNamesEventListener()
    {
        return new EventListener<PlaceNamesEvent>()
        {
            @Override
            public void notify(PlaceNamesEvent event)
            {
                Collection<LabelGeometry> adds = Collections.unmodifiableCollection(event.getAdds());
                Collection<LabelGeometry> removes = Collections.unmodifiableCollection(event.getRemoves());

                // Take care of the adds
                if (!adds.isEmpty())
                {
                    synchronized (myGeometries)
                    {
                        if (!myGeometries.containsKey(event.getServerName()))
                        {
                            myGeometries.put(event.getServerName(), new HashMap<String, Collection<LabelGeometry>>());
                        }
                        if (myGeometries.get(event.getServerName()).containsKey(event.getLayerName()))
                        {
                            myGeometries.get(event.getServerName()).get(event.getLayerName())
                                    .addAll(New.collection(event.getAdds()));
                        }
                        else
                        {
                            myGeometries.get(event.getServerName()).put(event.getLayerName(), New.collection(event.getAdds()));
                        }
                    }
                    if (event.isActive())
                    {
                        publishGeometries(adds, Collections.<LabelGeometry>emptyList());
                    }
                }

                // Take care of the removes
                if (!removes.isEmpty())
                {
                    // Remove from the map
                    publishGeometries(Collections.<LabelGeometry>emptyList(), removes);

                    synchronized (myGeometries)
                    {
                        if (myGeometries.containsKey(event.getServerName()))
                        {
                            if (myGeometries.get(event.getServerName()).containsKey(event.getLayerName()))
                            {
                                myGeometries.get(event.getServerName()).get(event.getLayerName()).removeAll(removes);
                            }
                            // Clean up layers (if needed)
                            if (myGeometries.get(event.getServerName()).get(event.getLayerName()).isEmpty())
                            {
                                myGeometries.get(event.getServerName()).remove(event.getLayerName());
                            }
                            // Clean up servers (if needed)
                            if (myGeometries.get(event.getServerName()).isEmpty())
                            {
                                myGeometries.remove(event.getServerName());
                            }
                        }
                    }
                }
            }
        };
    }
}
