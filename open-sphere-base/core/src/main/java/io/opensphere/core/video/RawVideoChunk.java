package io.opensphere.core.video;

import java.io.InputStream;
import java.util.List;

import gnu.trove.list.TLongList;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.ListOfBytesInputStream;
import io.opensphere.core.util.io.ListOfBytesOutputStream;

/**
 * A set of video frames in order which can be played consecutively. The first
 * frame of the chunk should always be a key frame.
 */
public class RawVideoChunk implements VideoChunk
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The maximum duration for a video table, 20 minutes.
     */
    private static final long ourMaxTableDuration = 1200000;

    /**
     * The time at which the last frame should be replaced including the last
     * frames duration (milliseconds since epoch).
     */
    private final long myEndTime;

    /**
     * The time stamps at which the key frames occur, these values are in the
     * streams native start time, but converted to milliseconds.
     */
    private final long[] myKeyFrames;

    /** The time at which the first frame occurs (milliseconds since epoch). */
    private final long myStartTime;

    /** The video block including a header. */
    private final List<byte[]> myVideo;

    /**
     * The total number of bytes in this video chunk.
     */
    private final int myVideoLength;

    /**
     * The maximum number of bytes contained in one byte[] element within
     * myVideo.
     */
    private final int myChunkSize;

    /**
     * Constructor.
     *
     * @param video The video block including a header.
     * @param keyFrames The times at which the key frame occur within the chunk.
     * @param startTime The time at which the first frame occurs (milliseconds
     *            since epoch).
     * @param endTime The time at which the last frame should be replaced
     *            including the last frames duration (milliseconds since epoch).
     */
    public RawVideoChunk(ListOfBytesOutputStream video, TLongList keyFrames, long startTime, long endTime)
    {
        myVideo = video.toArrays();
        myVideoLength = video.size();
        myChunkSize = video.getChunkSize();
        myKeyFrames = keyFrames.toArray();
        myStartTime = startTime;
        myEndTime = endTime;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getSimpleName()).append('[').append(TimeSpan.get(myStartTime, myEndTime)).append(' ')
                .append(myVideoLength).append("B]");
        return sb.toString();
    }

    /**
     * Get the endTime.
     *
     * @return the endTime.
     */
    public TimeInstant getEndTime()
    {
        return TimeInstant.get(myEndTime);
    }

    /**
     * Get the time stamps at which the key frames occur, these values are in
     * the streams native start time, but converted to milliseconds.
     *
     * @return the keyFrames.
     */
    public long[] getKeyFrames()
    {
        return myKeyFrames.clone();
    }

    @Override
    public List<PropertyAccessor<VideoChunk, ?>> getPropertyAccessors(TimeSpan streamSpan)
    {
        List<PropertyAccessor<VideoChunk, ?>> accessors = New.list(2);

        TimeSpan span = streamSpan == null ? TimeSpan.get(myStartTime, myEndTime) : streamSpan;

        if (streamSpan != null && span.isBounded() && span.getDuration().compareTo(new Minutes(20)) > 0)
        {
            long startTime = span.getStart();
            while (startTime + ourMaxTableDuration < span.getEnd() && myStartTime >= startTime + ourMaxTableDuration)
            {
                startTime += ourMaxTableDuration;
            }

            long endTime = startTime + ourMaxTableDuration;
            if (endTime > span.getEnd())
            {
                endTime = span.getEnd();
            }

            span = TimeSpan.get(startTime, endTime);
        }

        accessors.add(new VideoChunkSpanAccessor(span));
        accessors.add(SerializableAccessor.getHomogeneousAccessor(VideoChunk.VIDEO_CHUNK_PROPERTY_DESCRIPTOR));
        return accessors;
    }

    /**
     * Get the startTime.
     *
     * @return the startTime.
     */
    public TimeInstant getStartTime()
    {
        return TimeInstant.get(myStartTime);
    }

    @Override
    public TimeSpan getTimeSpan()
    {
        return TimeSpan.get(myStartTime, myEndTime);
    }

    /**
     * Get a stream which is backed by the video data without making a copy.
     *
     * @return A stream which provides the video data.
     */
    public InputStream getVideoAsStream()
    {
        return new ListOfBytesInputStream(myVideo, myVideoLength, myChunkSize);
    }
}
