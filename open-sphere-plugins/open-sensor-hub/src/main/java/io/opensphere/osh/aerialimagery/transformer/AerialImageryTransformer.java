package io.opensphere.osh.aerialimagery.transformer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animationhelper.RefreshListener;
import io.opensphere.core.animationhelper.TimeRefreshNotifier;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.aerialimagery.transformer.geometrybuilders.BuilderFactory;
import io.opensphere.osh.aerialimagery.transformer.geometrybuilders.GeometryBuilder;
import io.opensphere.osh.aerialimagery.transformer.modelproviders.ModelProvider;
import io.opensphere.osh.aerialimagery.transformer.modelproviders.ModelProviderFactory;
import io.opensphere.osh.util.OSHImageQuerier;
import io.opensphere.osh.util.OSHQuerier;

/**
 *
 * Transformer responsible for showing a UAV on the map including its
 * georectified imagery and its camera's footprint on the globe.
 */
public class AerialImageryTransformer extends DefaultTransformer
        implements RefreshListener, EventListener<DataTypeInfoColorChangeEvent>
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(AerialImageryTransformer.class);

    /**
     * Factory that creates geometry builders.
     */
    private final BuilderFactory myBuilderFactory;

    /**
     * The system {@link EventManager}.
     */
    private final EventManager myEventManager;

    /**
     * The collection of 1 layer containing the video data for the metadata.
     */
    private final List<DataTypeInfo> myLinkedLayer;

    /**
     * The previous refresh time.
     */
    private long myPreviousRefreshTime;

    /**
     * The list of model providers.
     */
    private final List<ModelProvider> myProviders;

    /**
     * The published geometries mapped by their time.
     */
    private final Map<Long, List<Geometry>> myPublishedGeometries = Collections.synchronizedMap(New.map());

    /**
     * Contains the current time in timeline.
     */
    private final TimeManager myTimeManager;

    /**
     * Helps with time change notifications.
     */
    private final TimeRefreshNotifier myTimeNotifier;

    /**
     * The uav data type.
     */
    private final DataTypeInfo myUAVLayer;

    /**
     * Constructs a new aerial imagery transformer.
     *
     * @param toolbox The toolbox.
     * @param querier The {@link OSHQuerier} used to query video images.
     * @param uavLayer The uav layer to draw geometries for.
     * @param linkedLayer The collection of 1 layer containing the video data
     *            for the metadata.
     */
    public AerialImageryTransformer(Toolbox toolbox, OSHImageQuerier querier, DataTypeInfo uavLayer,
            List<DataTypeInfo> linkedLayer)
    {
        super(toolbox.getDataRegistry());
        myEventManager = toolbox.getEventManager();
        myLinkedLayer = linkedLayer;
        myProviders = new ModelProviderFactory(toolbox.getDataRegistry(), querier).createProviders();
        myUAVLayer = uavLayer;
        myTimeManager = toolbox.getTimeManager();
        myBuilderFactory = new BuilderFactory(toolbox.getOrderManagerRegistry(), toolbox.getMapManager());
        myTimeNotifier = new TimeRefreshNotifier(this, myTimeManager, toolbox.getAnimationManager());
    }

    @Override
    public void close()
    {
        myEventManager.unsubscribe(DataTypeInfoColorChangeEvent.class, this);
        myTimeNotifier.close();
        List<Geometry> toRemove = New.list();
        for (List<Geometry> geometries : myPublishedGeometries.values())
        {
            toRemove.addAll(geometries);
        }

        toRemove.addAll(myBuilderFactory.close());
        publishGeometries(New.list(), toRemove);
        myPublishedGeometries.clear();
        super.close();
    }

    /**
     * Gets the linked layer which should be the video layer.
     *
     * @return The video layer.
     */
    public List<DataTypeInfo> getLinkedLayer()
    {
        return myLinkedLayer;
    }

    @Override
    public synchronized void notify(DataTypeInfoColorChangeEvent event)
    {
        if (!myLinkedLayer.isEmpty() && event.getDataTypeInfo().equals(myLinkedLayer.get(0)))
        {
            myPreviousRefreshTime = 0;
            refreshNow();
        }
    }

    @Override
    public void open()
    {
        super.open();
        refreshNow();
        myEventManager.subscribe(DataTypeInfoColorChangeEvent.class, this);
    }

    @Override
    public synchronized void refresh(boolean forceIt)
    {
        long currentTime = myTimeManager.getActiveTimeSpans().getPrimary().get(0).getEnd();
        if (currentTime != myPreviousRefreshTime)
        {
            myPreviousRefreshTime = currentTime;
            List<Geometry> toPublish = New.list();
            Long metadataTime = null;
            PlatformMetadata metadata = null;
            DataTypeInfo videoLayer = null;

            if (!myLinkedLayer.isEmpty())
            {
                videoLayer = myLinkedLayer.get(0);
            }

            for (ModelProvider provider : myProviders)
            {
                metadata = provider.getModel(myUAVLayer, videoLayer, currentTime, metadata);
                if (metadata != null)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(metadata.toString());
                    }

                    metadataTime = Long.valueOf(metadata.getTime().getTime());

                    List<GeometryBuilder> builders = myBuilderFactory.createBuilders(metadata);
                    for (GeometryBuilder builder : builders)
                    {
                        if (builder.cachePublishedGeometries())
                        {
                            if (!myPublishedGeometries.containsKey(metadataTime))
                            {
                                toPublish.addAll(builder.buildGeometries(metadata, myUAVLayer, videoLayer).getFirstObject());
                            }
                        }
                        else
                        {
                            Pair<List<Geometry>, List<Geometry>> addsAndRemoves = builder.buildGeometries(metadata, myUAVLayer,
                                    videoLayer);
                            if (!addsAndRemoves.getFirstObject().isEmpty() || !addsAndRemoves.getSecondObject().isEmpty())
                            {
                                publishGeometries(addsAndRemoves.getFirstObject(), addsAndRemoves.getSecondObject());
                            }
                        }
                    }
                }
            }

            if (!toPublish.isEmpty())

            {
                myPublishedGeometries.put(metadataTime, toPublish);
                publishGeometries(toPublish, New.list());
            }
        }
    }

    @Override
    public void refreshNow()
    {
        refresh(false);
    }
}
