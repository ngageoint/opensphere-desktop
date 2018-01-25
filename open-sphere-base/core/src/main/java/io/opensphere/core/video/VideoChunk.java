package io.opensphere.core.video;

import java.io.Serializable;
import java.util.List;

import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.time.TimeSpan;

/** An interface for classes which represent chunks of video. */
public interface VideoChunk extends Serializable
{
    /** The property descriptor for the chunks of video. */
    PropertyDescriptor<VideoChunk> VIDEO_CHUNK_PROPERTY_DESCRIPTOR = new PropertyDescriptor<>("VIDEO_CHUNK", VideoChunk.class);

    /**
     * Create the property accessors for this video chunk.
     *
     * @param streamSpan The time span for the stream, or {@code null} if
     *            unknown.
     *
     * @return The accessors.
     */
    List<PropertyAccessor<VideoChunk, ?>> getPropertyAccessors(TimeSpan streamSpan);

    /**
     * Get the start and end times of this chunk as a time span.
     *
     * @return the time span covered by this chunk.
     */
    TimeSpan getTimeSpan();

    /** The time span accessor for raw video chunks. */
    class VideoChunkSpanAccessor extends TimeSpanAccessor<VideoChunk>
    {
        /**
         * Construct the time span accessor.
         *
         * @param extent A time span that comprises all of the spans provided by
         *            this accessor.
         */
        public VideoChunkSpanAccessor(TimeSpan extent)
        {
            super(extent);
        }

        @Override
        public TimeSpan access(VideoChunk input)
        {
            return input.getTimeSpan();
        }
    }
}
