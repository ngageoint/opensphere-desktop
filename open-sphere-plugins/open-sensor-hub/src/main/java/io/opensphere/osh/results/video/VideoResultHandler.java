package io.opensphere.osh.results.video;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import io.opensphere.controlpanels.animation.event.ShowTimelineEvent;
import io.opensphere.controlpanels.animation.model.ViewPreference;
import io.opensphere.core.Toolbox;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.PlayState;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.osh.model.BinaryEncoding;
import io.opensphere.osh.model.OSHDataTypeInfo;
import io.opensphere.osh.model.Offering;
import io.opensphere.osh.model.Output;
import io.opensphere.osh.results.ResultHandler;
import io.opensphere.osh.util.AnimationPlayer;
import io.opensphere.osh.util.OSHQuerier;

/**
 * Handles video and series of images.
 */
public class VideoResultHandler implements ResultHandler
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The data registry querier. */
    private final OSHQuerier myQuerier;

    /** The video frame controller. */
    private final VideoFrameController myVideoFrameController;

    /** The play state listeners, to prevent garbage collection. */
    private final List<ChangeListener<PlayState>> myPlayStateListeners = New.list();

    /** Whether we're handling the first image. */
    private boolean myFirstTime = true;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param querier The data registry querier
     */
    public VideoResultHandler(Toolbox toolbox, OSHQuerier querier)
    {
        myToolbox = toolbox;
        myQuerier = querier;
        myVideoFrameController = new VideoFrameController(toolbox, querier);
        myVideoFrameController.open();
    }

    @Override
    public List<Output> canHandle(List<Output> outputs)
    {
        List<Output> canHandles = New.list();
        for (Output output : outputs)
        {
            if (output.getProperties().contains("http://sensorml.com/ont/swe/property/VideoFrame"))
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
                DefaultBasicVisualizationInfo.LOADS_TO_TIMELINE_ONLY, Color.WHITE, false));

        dataType.setMapVisualizationInfo(new DefaultMapFeatureVisualizationInfo(MapVisualizationType.MOTION_IMAGERY));

        dataType.setOrderKey(new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY, dataType.getTypeKey()));

        dataType.getStreamingSupport().setStreamingEnabled(true);
        ChangeListener<PlayState> playStateListener = (obs, o, n) -> handlePlayStateChange(dataType, n);
        myPlayStateListeners.add(playStateListener);
        dataType.getStreamingSupport().getPlayState().addListener(playStateListener);

        myToolbox.getUIRegistry().getTimelineRegistry().addLayer(dataType.getOrderKey(), dataType.getDisplayName(),
                dataType.getBasicVisualizationInfo().getTypeColor(), dataType.isVisible());
    }

    @Override
    public String getQueryProperty(Offering offering, Output output)
    {
        return "http://sensorml.com/ont/swe/property/VideoFrame";
    }

    @Override
    public void handleResults(OSHDataTypeInfo dataType, List<Output> outputs, List<CancellableInputStream> streams)
        throws IOException
    {
        myVideoFrameController.addDataType(dataType);

        try (CancellableTaskActivity ta = CancellableTaskActivity.createActive("Querying OpenSensorHub results"))
        {
            myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);

            int index = 0;
            for (Output output : outputs)
            {
                CancellableInputStream stream = streams.get(index);
                Output resultTemplate = dataType.getResultTemplate(output);
                boolean isVideo = resultTemplate.getFields().stream().anyMatch(f -> "H264".equals(f.getDataType()));
                dataType.setVideo(isVideo);

                VideoProcessor processor = isVideo ? new VideoVideoProcessor(myToolbox, myQuerier)
                        : new ImageVideoProcessor(myToolbox, myQuerier);

                List<String> dataTypes = ((BinaryEncoding)resultTemplate.getEncoding()).getDataTypes();
                List<VideoFieldHandler> fieldHandlers = getFieldHandlers(dataTypes);
                processor.processData(dataType, stream, ta, fieldHandlers);
                index++;
            }
        }
    }

    @Override
    public void handleGroupActivation(DataTypeInfo dataType, ActivationState state)
    {
        if (state == ActivationState.INACTIVE)
        {
            myVideoFrameController.hideWindow(dataType);
        }
    }

    /**
     * Gets the handlers for the data types.
     *
     * @param dataTypes the data types
     * @return the fields
     */
    private List<VideoFieldHandler> getFieldHandlers(Collection<String> dataTypes)
    {
        List<VideoFieldHandler> handlers = New.list(dataTypes.size());
        for (String dataType : dataTypes)
        {
            VideoFieldHandler handler;
            switch (dataType)
            {
                case "http://www.opengis.net/def/dataType/OGC/0/double":
                    handler = new DoubleTimeFieldHandler();
                    break;
                case "JPEG":
                case "H264":
                    handler = new BinaryFieldHandler();
                    break;
                default:
                    handler = null;
            }
            handlers.add(handler);
        }
        return handlers;
    }

    /**
     * Handles a change in the play state.
     *
     * @param dataType the data type
     * @param playState the play state
     */
    private void handlePlayStateChange(OSHDataTypeInfo dataType, PlayState playState)
    {
        AnimationPlayer animationPlayer = new AnimationPlayer(myToolbox);
        if (playState == PlayState.FORWARD)
        {
            myVideoFrameController.showWindow(dataType);

            ThreadUtilities.runCpu(() -> animationPlayer.playHistoricalVideo(dataType));

            if (myFirstTime)
            {
                myFirstTime = false;
                myToolbox.getEventManager().publishEvent(new ShowTimelineEvent(ViewPreference.TIMELINE));
            }
        }
        else
        {
            animationPlayer.stop(dataType);
        }
    }
}
