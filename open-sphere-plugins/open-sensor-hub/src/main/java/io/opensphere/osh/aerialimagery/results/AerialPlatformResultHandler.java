package io.opensphere.osh.aerialimagery.results;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.aerialimagery.transformer.AerialImageryTransformer;
import io.opensphere.osh.aerialimagery.util.Constants;
import io.opensphere.osh.model.OSHDataTypeInfo;
import io.opensphere.osh.model.Offering;
import io.opensphere.osh.model.Output;
import io.opensphere.osh.results.ResultHandler;
import io.opensphere.osh.util.OSHImageQuerier;

/**
 * A {@link ResultHandler} class that knows how to deal with platform location
 * and orientation information including camera orientation information.
 */
public class AerialPlatformResultHandler implements ResultHandler, EventListener<ActiveDataGroupsChangedEvent>
{
    /**
     * The field of view height.
     */
    private static final double ourFOVHeight = 85;

    /**
     * The field of view width.
     */
    private static final double ourFOVWidth = 64.6;

    /**
     * The gimbal orientation output name.
     */
    private static final String ourGimbalAttitudeName = "gimbalAtt";

    /**
     * The platform's orientation output name.
     */
    private static final String ourPlatformAttitudeName = "platformAtt";

    /**
     * The platform's location output name.
     */
    private static final String ourPlatformLocationName = "platformLoc";

    /**
     * Combines the sensor data into one {@link PlatformMetadata} containing all
     * the data.
     */
    private final MetadataCombiner myCombiner = new MetadataCombiner();

    /**
     * Calculates the footprint.
     */
    private final FootprintCalculator myFootprintCalc;

    /**
     * Parses the gimbal orientation data.
     */
    private final GimbalOrientationParser myGimbalParser;

    /**
     * Manages the OSH layers that may be linked together such as an OSH video
     * layer linked with an OSH UAV metadata layer.
     */
    private final LayerLinker myLayerLinker;

    /**
     * Parses the location data.
     */
    private final LocationParser myLocationParser;

    /**
     * Parses the platform orientation data.
     */
    private final PlatformOrientationParser myPlatformOrientationParser;

    /**
     * Used to query for video images.
     */
    private final OSHImageQuerier myQuerier;

    /**
     * The data registry.
     */
    private final DataRegistry myRegistry;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * The video layer.
     */
    private final Map<String, List<DataTypeInfo>> myVideoLayer = Collections.synchronizedMap(New.map());

    /**
     * Constructs a new {@link AerialPlatformResultHandler}.
     *
     * @param toolbox The system toolbox.
     * @param querier Used to query for video images.
     */
    public AerialPlatformResultHandler(Toolbox toolbox, OSHImageQuerier querier)
    {
        myToolbox = toolbox;
        myQuerier = querier;
        myRegistry = myToolbox.getDataRegistry();
        myLocationParser = new LocationParser(myToolbox.getUIRegistry());
        myGimbalParser = new GimbalOrientationParser(myToolbox.getUIRegistry());
        myPlatformOrientationParser = new PlatformOrientationParser(myToolbox.getUIRegistry());
        myFootprintCalc = new FootprintCalculator();
        myLayerLinker = new LayerLinker(toolbox.getPreferencesRegistry());
        myToolbox.getEventManager().subscribe(ActiveDataGroupsChangedEvent.class, this);
    }

    @Override
    public List<Output> canHandle(List<Output> outputs)
    {
        List<Output> canHandles = New.list();

        for (Output output : outputs)
        {
            if (ourPlatformLocationName.equals(output.getName()) || ourPlatformAttitudeName.equals(output.getName())
                    || ourGimbalAttitudeName.equals(output.getName()))
            {
                canHandles.add(output);
            }
        }

        return canHandles;
    }

    @Override
    public String getQueryProperty(Offering offering, Output output)
    {
        String filterString = "Location";
        if (ourPlatformAttitudeName.equals(output.getName()))
        {
            filterString = "PlatformOrientation";
        }
        else if (ourGimbalAttitudeName.equals(output.getName()))
        {
            filterString = "Gimbal";
        }

        String property = null;

        for (String aProperty : offering.getObservableProperties())
        {
            if (aProperty.contains(filterString))
            {
                property = aProperty;
            }
        }

        return property;
    }

    @Override
    public void handleGroupActivation(DataTypeInfo dataType, ActivationState state)
    {
        if (state != ActivationState.ACTIVE)
        {
            myToolbox.getTransformerRegistry().removeObjectsForSource(dataType);
        }
        else if (state == ActivationState.ACTIVE)
        {
            String typeKey = dataType.getTypeKey();
            synchronized (myVideoLayer)
            {
                if (!myVideoLayer.containsKey(typeKey))
                {
                    myVideoLayer.put(typeKey, New.list());
                }
            }
            myToolbox.getTransformerRegistry().addObjectsForSource(dataType,
                    New.list(new AerialImageryTransformer(myToolbox, myQuerier, dataType, myVideoLayer.get(typeKey))));
        }
    }

    @Override
    public void handleResults(OSHDataTypeInfo dataType, List<Output> outputs, List<CancellableInputStream> streams)
        throws IOException
    {
        int index = 0;
        List<PlatformMetadata> locations = New.list();
        List<PlatformMetadata> platformOrientations = New.list();
        List<PlatformMetadata> gimbalOrientations = New.list();
        for (CancellableInputStream stream : streams)
        {
            Output output = outputs.get(index);

            if (ourPlatformLocationName.equals(output.getName()))
            {
                myLocationParser.parse(output, stream, locations);
            }
            else if (ourPlatformAttitudeName.equals(output.getName()))
            {
                myPlatformOrientationParser.parse(output, stream, platformOrientations);
            }
            else if (ourGimbalAttitudeName.equals(output.getName()))
            {
                myGimbalParser.parse(output, stream, gimbalOrientations);
            }

            index++;
        }

        List<PlatformMetadata> metadatas = myCombiner.combineData(locations, platformOrientations, gimbalOrientations);
        index = 0;
        for (PlatformMetadata metadata : metadatas)
        {
            metadata.setFootprint(myFootprintCalc.calculateFootprint2(metadata, ourFOVWidth, ourFOVHeight));
            index++;
        }

        depositMetadatas(dataType, metadatas);
    }

    @Override
    public void initializeType(OSHDataTypeInfo dataType)
    {
    }

    @Override
    public void notify(ActiveDataGroupsChangedEvent event)
    {
        for (DataGroupInfo dataGroup : event.getActivatedGroups())
        {
            if (dataGroup.hasMembers(false))
            {
                for (DataTypeInfo dataType : dataGroup.getMembers(false))
                {
                    String typeKey = dataType.getTypeKey();
                    String linkedLayer = myLayerLinker.getLinkedLayerId(typeKey);
                    if (StringUtils.isNotEmpty(linkedLayer))
                    {
                        synchronized (myVideoLayer)
                        {
                            if (!myVideoLayer.containsKey(linkedLayer))
                            {
                                myVideoLayer.put(linkedLayer, New.list());
                            }
                        }

                        myVideoLayer.get(linkedLayer).add(dataType);
                    }
                }
            }
        }
    }

    /**
     * Deposits the passed in metadata.
     *
     * @param category The category of the deposit.
     * @param metadata The metadata to deposit.
     */
    private void depositMetadata(DataModelCategory category, PlatformMetadata metadata)
    {
        SerializableAccessor<PlatformMetadata, Long> timestampAccessor = SerializableAccessor
                .<PlatformMetadata, Long>getSingletonAccessor(Constants.METADATA_TIMESTAMP_PROPERTY_DESCRIPTOR,
                        Long.valueOf(metadata.getTime().getTime()));
        SerializableAccessor<PlatformMetadata, PlatformMetadata> metadataAccessor = SerializableAccessor
                .getHomogeneousAccessor(Constants.PLATFORM_METADATA_DESCRIPTOR);
        Collection<? extends PropertyAccessor<PlatformMetadata, ?>> propertyAccessors = Arrays.asList(metadataAccessor,
                timestampAccessor);

        DefaultCacheDeposit<PlatformMetadata> deposit = new DefaultCacheDeposit<>(category, propertyAccessors, New.list(metadata),
                true, CacheDeposit.SESSION_END, false);
        myRegistry.addModels(deposit);
    }

    /**
     * Deposits the metadatas into the data registry.
     *
     * @param dataType The data type the metadatas are for.
     * @param metadatas The metadatas to deposit.
     */
    private void depositMetadatas(OSHDataTypeInfo dataType, List<PlatformMetadata> metadatas)
    {
        DataModelCategory category = new DataModelCategory(dataType.getUrl(), Constants.PLATFORM_METADATA_FAMILY,
                dataType.getTypeKey());

        for (PlatformMetadata metadata : metadatas)
        {
            depositMetadata(category, metadata);
        }
    }
}
