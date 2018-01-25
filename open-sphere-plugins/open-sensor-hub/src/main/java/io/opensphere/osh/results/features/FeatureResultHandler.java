package io.opensphere.osh.results.features;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.util.Aggregator;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.ExceptionUtilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.PlayState;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.osh.model.BinaryEncoding;
import io.opensphere.osh.model.Encoding;
import io.opensphere.osh.model.Field;
import io.opensphere.osh.model.OSHDataTypeInfo;
import io.opensphere.osh.model.Offering;
import io.opensphere.osh.model.Output;
import io.opensphere.osh.model.PropertyInfo;
import io.opensphere.osh.results.ResultHandler;
import io.opensphere.osh.util.AnimationPlayer;
import io.opensphere.osh.util.OSHQuerier;

/**
 * Handles features.
 */
public class FeatureResultHandler implements ResultHandler
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FeatureResultHandler.class);

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The UI registry. */
    private final UIRegistry myUIRegistry;

    /** The mantle data type controller. */
    private final DataTypeController myDataTypeController;

    /** The data registry querier. */
    private final OSHQuerier myQuerier;

    /** The play state listeners, to prevent garbage collection. */
    private final List<ChangeListener<PlayState>> myPlayStateListeners = New.list();

    /** Map of data type to stream. */
    private final Map<DataTypeInfo, CancellableInputStream> myDataTypeToStreamMap = Collections.synchronizedMap(New.map());

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param dataTypeController The mantle data type controller
     * @param querier The data registry querier
     */
    public FeatureResultHandler(Toolbox toolbox, DataTypeController dataTypeController, OSHQuerier querier)
    {
        myToolbox = toolbox;
        myUIRegistry = toolbox.getUIRegistry();
        myDataTypeController = dataTypeController;
        myQuerier = querier;
    }

    @Override
    public List<Output> canHandle(List<Output> outputs)
    {
        List<Output> canHandles = New.list();

        for (Output output : outputs)
        {
            if (output.getFields().stream().anyMatch(f -> "lat".equals(f.getName())))
            {
                canHandles.add(output);
            }
        }

        return canHandles;
    }

    @Override
    public void initializeType(OSHDataTypeInfo dataType)
    {
        dataType.setBasicVisualizationInfo(new DefaultBasicVisualizationInfo(LoadsTo.TIMELINE,
                DefaultBasicVisualizationInfo.LOADS_TO_STATIC_AND_TIMELINE, Color.ORANGE, true));

        dataType.setMapVisualizationInfo(new DefaultMapFeatureVisualizationInfo(MapVisualizationType.POINT_ELEMENTS));

        dataType.setOrderKey(new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY,
                DefaultOrderCategory.FEATURE_CATEGORY, dataType.getTypeKey()));

        if (dataType.isNrtStreaming())
        {
            dataType.getStreamingSupport().setStreamingEnabled(true);
            ChangeListener<PlayState> playStateListener = (obs, o, n) -> handlePlayStateChange(dataType, n);
            myPlayStateListeners.add(playStateListener);
            dataType.getStreamingSupport().getPlayState().addListener(playStateListener);
        }

        DefaultMetaDataInfo metaData = new DefaultMetaDataInfo();
        for (Field field : dataType.getOutput().getFields())
        {
            String name = field.getLabel() != null ? field.getLabel() : field.getName();
            if (!PropertyInfo.isExcluded(field))
            {
                PropertyInfo propertyInfo = PropertyInfo.getProperty(field);
                metaData.addKey(name, propertyInfo != null ? propertyInfo.getPropertyClass() : String.class, null);
                if (propertyInfo != null)
                {
                    metaData.setSpecialKey(name, propertyInfo.getSpecialKey(), null);
                }
            }
        }
        metaData.copyKeysToOriginalKeys();
        dataType.setMetaDataInfo(metaData);
    }

    @Override
    public String getQueryProperty(Offering offering, Output output)
    {
        String property = offering.getObservableProperties().stream().filter(p -> p.contains("Location")).findAny().orElse(null);
        if (property == null)
        {
            property = output.getProperties().stream().filter(p -> p.contains("AVLData")).findAny().orElse(null);
        }
        return property;
    }

    @Override
    public void handleResults(OSHDataTypeInfo dataType, List<Output> outputs, List<CancellableInputStream> streams)
        throws IOException
    {
        int batchSize = dataType.isNrtStreaming() ? 1 : 1000;
        for (int i = 0; i < streams.size(); i++)
        {
            CancellableInputStream stream = streams.get(i);
            try (CancellableTaskActivity ta = CancellableTaskActivity.createActive("Querying OpenSensorHub results"))
            {
                myUIRegistry.getMenuBarRegistry().addTaskActivity(ta);

                Encoding encoding = dataType.getResultTemplate(outputs.get(i)).getEncoding();
                FeatureProcessor processor = encoding instanceof BinaryEncoding ? new BinaryFeatureProcessor()
                        : new TextFeatureProcessor();
                processor.processData(dataType, stream, ta,
                        new Aggregator<>(batchSize, results -> handleResults(dataType, results)));
            }
        }
    }

    @Override
    public void handleGroupActivation(DataTypeInfo dataType, ActivationState state)
    {
        if (state == ActivationState.INACTIVE)
        {
            dataType.getStreamingSupport().getPlayState().set(PlayState.STOP);
        }
    }

    /**
     * Handles the results.
     *
     * @param dataType the data type
     * @param results the results
     */
    private void handleResults(OSHDataTypeInfo dataType, Collection<? extends List<? extends Serializable>> results)
    {
        myDataTypeController.addDataElements(new OSHDataElementProvider(dataType, results), null, null, this);
    }

    /**
     * Handles a change in the play state.
     *
     * @param dataType the data type
     * @param playState the play state
     */
    private void handlePlayStateChange(OSHDataTypeInfo dataType, PlayState playState)
    {
        ThreadUtilities.runBackground(() ->
        {
            AnimationPlayer animationPlayer = new AnimationPlayer(myToolbox);
            if (playState == PlayState.FORWARD)
            {
                animationPlayer.playStreamingFeatures(dataType);

                boolean needRequery;
                do
                {
                    needRequery = queryStreaming(dataType);
                }
                while (needRequery);
            }
            else
            {
                CancellableInputStream stream = myDataTypeToStreamMap.remove(dataType);
                if (stream != null)
                {
                    stream.cancel();
                }

                animationPlayer.stop(dataType);
            }
        });
    }

    /**
     * Performs a streaming query.
     *
     * @param dataType the data type
     * @return whether a re-query is needed
     */
    private boolean queryStreaming(OSHDataTypeInfo dataType)
    {
        TimeSpan timeSpan = TimeSpan.newUnboundedStartTimeSpan(System.currentTimeMillis() + Constants.MILLIS_PER_DAY);
        String property = getQueryProperty(dataType.getOffering(), dataType.getOutput());

        CancellableInputStream stream = null;
        try
        {
            stream = myQuerier.getResults(dataType.getUrl(), dataType.getOffering(), property, timeSpan);
            myDataTypeToStreamMap.put(dataType, stream);
            try
            {
                handleResults(dataType, Collections.singletonList(dataType.getOutput()), Collections.singletonList(stream));
            }
            catch (IOException e)
            {
                if (stream.isCancelled())
                {
                    LOGGER.info("Streaming canceled for " + dataType.getDisplayName());
                }
                else
                {
                    LOGGER.error(e);
                    Notify.error("Failed to query OpenSensorHub server: " + e.getMessage());
                }
            }
        }
        catch (QueryException e)
        {
            LOGGER.error(e);
            Notify.error("Failed to query OpenSensorHub server: " + ExceptionUtilities.getRootCause(e).getMessage());
        }

        boolean needRequery = stream != null && !stream.isCancelled();
        return needRequery;
    }
}
