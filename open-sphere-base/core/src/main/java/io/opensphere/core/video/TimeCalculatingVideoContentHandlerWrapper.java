package io.opensphere.core.video;

import java.util.function.Consumer;

import org.apache.log4j.Logger;

import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.EqualsHelper;
import net.jcip.annotations.GuardedBy;

/**
 * A wrapper for a video content handler that calculates the presentation time
 * for the video content and sends it to a time consumer.
 *
 * @param <T> The type of content handled.
 */
public class TimeCalculatingVideoContentHandlerWrapper<T> extends AbstractVideoContentHandler<T>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TimeCalculatingVideoContentHandlerWrapper.class);

    /** The first PTS in the stream (in milliseconds). */
    @GuardedBy("this")
    private long myFirstPtsMillis = Long.MIN_VALUE;

    /** Consumer for the latest frame time. */
    private final Consumer<TimeInstant> myLatestFrameTimeConsumer;

    /** The latest presentation time stamp. */
    @GuardedBy("this")
    private long myLatestPtsMillis;

    /** The latest time delivered to the consumer. */
    @GuardedBy("this")
    private TimeInstant myLatestTime;

    /** The start time. */
    private final TimeInstant myStartTime;

    /** The video content handler. */
    private final VideoContentHandler<? super T> myVideoContentHandler;

    /**
     * Constructor.
     *
     * @param videoContentHandler The wrapped handler, which is not closed by
     *            the wrapper.
     * @param startTime The time of the first packet in the stream.
     * @param latestFrameTimeConsumer The consumer for the time of the latest
     *            packet in the stream.
     */
    public TimeCalculatingVideoContentHandlerWrapper(VideoContentHandler<? super T> videoContentHandler, TimeInstant startTime,
            Consumer<TimeInstant> latestFrameTimeConsumer)
    {
        myStartTime = Utilities.checkNull(startTime, "startTime");
        myVideoContentHandler = Utilities.checkNull(videoContentHandler, "videoContentHandler");
        myLatestFrameTimeConsumer = latestFrameTimeConsumer;
    }

    /**
     * Get the latest presentation time stamp.
     *
     * @return The pts in milliseconds.
     */
    public synchronized long getLatestPtsMillis()
    {
        return myLatestPtsMillis;
    }

    /**
     * Get the latest time delivered to the consumer.
     *
     * @return The latest time.
     */
    public synchronized TimeInstant getLatestTime()
    {
        return myLatestTime;
    }

    @Override
    public synchronized void handleContent(T image, long ptsMillis)
    {
        if (myFirstPtsMillis == Long.MIN_VALUE)
        {
            myFirstPtsMillis = ptsMillis;
        }

        long ptsOffsetMillis = ptsMillis - myFirstPtsMillis;
        myVideoContentHandler.handleContent(image, ptsMillis);

        myLatestPtsMillis = ptsMillis;
        myLatestTime = TimeInstant.get(myStartTime.getEpochMillis() + ptsOffsetMillis);
        if (myLatestFrameTimeConsumer != null)
        {
            myLatestFrameTimeConsumer.accept(myLatestTime);
        }
        if (LOGGER.isTraceEnabled())
        {
            if (image == null)
            {
                LOGGER.trace("No image produced for time " + TimeInstant.get(myStartTime.getEpochMillis() + ptsOffsetMillis));
            }
            else
            {
                LOGGER.trace("Decoded image for time " + TimeInstant.get(myStartTime.getEpochMillis() + ptsOffsetMillis));
            }
        }
    }

    /**
     * Determine if this has the same parameters as the arguments.
     *
     * @param videoContentHandler The wrapped handler.
     * @param startTime The time of the first packet in the stream.
     * @param latestFrameTime The consumer for the time of the latest packet in
     *            the stream.
     * @return {@code true} if compatible.
     */
    public boolean isCompatibleWith(VideoContentHandler<? super ImageIOImage> videoContentHandler, TimeInstant startTime,
            Consumer<TimeInstant> latestFrameTime)
    {
        return EqualsHelper.equals(videoContentHandler, myVideoContentHandler, startTime, myStartTime, latestFrameTime,
                myLatestFrameTimeConsumer);
    }
}
