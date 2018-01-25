package io.opensphere.osh.results.video;

import java.util.Collections;

import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.timeline.StyledTimelineDatum;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.util.OSHQuerier;

/** Abstract VideoProcessor. */
public abstract class AbstractVideoProcessor implements VideoProcessor
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The data registry querier. */
    private final OSHQuerier myQuerier;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param querier The data registry querier
     */
    public AbstractVideoProcessor(Toolbox toolbox, OSHQuerier querier)
    {
        myToolbox = toolbox;
        myQuerier = querier;
    }

    /**
     * Writes video data to the data registry and timeline registry.
     *
     * @param dataType the data type
     * @param videoData the video data
     * @param nextTime the time of the next frame
     */
    protected void writeVideoData(DataTypeInfo dataType, VideoData videoData, long nextTime)
    {
        TimeSpan timeSpan = TimeSpan.get(videoData.getTime(), nextTime);
        myQuerier.depositImage(dataType, videoData.getData(), timeSpan);
        StyledTimelineDatum datum = new StyledTimelineDatum(timeSpan.getEnd(), timeSpan);
        myToolbox.getUIRegistry().getTimelineRegistry().addData(dataType.getOrderKey(), Collections.singleton(datum));
    }
}
